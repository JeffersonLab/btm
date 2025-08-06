<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Hall Availability"/>
<t:report-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/hall-availability.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.pie.min.js"></script>
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.pie.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/flot-order-bars.js"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/hall-availability.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div id="report-page-actions">
                <a id="explanation-link" target="_blank"
                   href="${settings.get('HALL_AVAIL_DOC_URL')}">Explanation</a>
                <button id="fullscreen-button">Full Screen</button>
                <div id="export-widget">
                    <button id="export-menu-button">Export</button>
                    <ul id="export-menu">
                        <li id="image-menu-item">Image</li>
                        <li id="print-menu-item">Print</li>
                    </ul>
                </div>
            </div>
            <s:filter-flyout-widget requiredMessage="true">
                <form class="filter-form" method="get" action="hall-availability">
                    <fieldset>
                        <legend>Filter</legend>
                        <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key"><label class="required-field" for="availability-chart">Chart</label>
                                </div>
                                <div class="li-value">
                                    <select id="availability-chart" name="availability-chart">
                                        <option value="bar"${chart eq 'bar' ? ' selected="selected"' : ''}>Bar</option>
                                        <option value="table"${chart eq 'table' ? ' selected="selected"' : ''}>Table
                                        </option>
                                    </select>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a start date and end date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box"><c:out value="${selectionMessage}"/></div>
                    <c:if test="${period > 0}">
                        <c:choose>
                            <c:when test="${chart eq 'table'}">
                                <div class="data-table-panel chart-wrap-backdrop">
                                    <table class="data-table stripped-table graph-data-table">
                                        <thead>
                                        <tr>
                                            <th rowspan="2">Hall</th>
                                            <th colspan="5">Accelerator Beam Time (Hours)</th>
                                            <th colspan="3">Experiment Beam Time (Hours)</th>
                                        </tr>
                                        <tr>
                                            <th>ABU</th>
                                            <th>BANU</th>
                                            <th>BNA</th>
                                            <th>OFF</th>
                                            <th>Implicit OFF</th>
                                            <th>ER</th>
                                            <th>PCC</th>
                                            <th>UED</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${totalsList}" var="totals" varStatus="status">
                                            <tr>
                                                <th><c:out value="${totals.hall}"/></th>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${totals.abuSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${totals.banuSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${totals.bnaSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${totals.offSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${expUnknownList[status.index]}"/></td>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${totals.erSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${totals.pccSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${totals.uedSeconds / 3600}"/></td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                    <table class="data-table stripped-table graph-data-table">
                                        <thead>
                                        <tr>
                                            <th rowspan="2">Hall</th>
                                            <th colspan="5">Availability (Hours)</th>
                                        </tr>
                                        <tr>
                                            <th>PD Scheduled</th>
                                            <th>Experimenter Program Time (T)</th>
                                            <th>Accelerator Availability (AA)</th>
                                            <th>Experiment Availability (EA)</th>
                                            <th>Simultaneous Availability (SA)</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${totalsList}" var="totals" varStatus="status">
                                            <tr>
                                                <th><c:out value="${totals.hall}"/></th>
                                                <td><fmt:formatNumber pattern="###,##0.0"
                                                                      value="${hallScheduledArray[status.index] / 3600}"/></td>
                                                <c:set var="t"
                                                       value="${totals.erSeconds + totals.pccSeconds + totals.uedSeconds}"/>
                                                <td><fmt:formatNumber pattern="###,##0.0" value="${t / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="##0.0"
                                                                      value="${t == 0 ? 0 : (totals.abuSeconds + totals.banuSeconds) / (t) * 100}"/>%
                                                </td>
                                                <td><fmt:formatNumber pattern="##0.0"
                                                                      value="${t == 0 ? 0 : (totals.erSeconds + totals.pccSeconds) / t * 100}"/>%
                                                </td>
                                                <td><fmt:formatNumber pattern="##0.0"
                                                                      value="${t == 0 ? 0 : totals.abuSeconds / (t) * 100}"/>%
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <s:chart-widget>
                                    <table class="chart-legend">
                                        <tbody>
                                        <tr class="sub-head-row">
                                            <th colspan="2"></th>
                                            <th>A</th>
                                            <th>B</th>
                                            <th>C</th>
                                            <th>D</th>
                                        </tr>
                                        <tr>
                                            <th>
                                                <div class="color-box"></div>
                                            </th>
                                            <th>AA</th>
                                            <c:forEach items="${totalsList}" var="totals">
                                                <c:set var="t"
                                                       value="${totals.erSeconds + totals.pccSeconds + totals.uedSeconds}"/>
                                                <td><fmt:formatNumber pattern="##0.0"
                                                                      value="${t == 0 ? 0 : (totals.abuSeconds + totals.banuSeconds) / (t) * 100}"/>%
                                                </td>
                                            </c:forEach>
                                        </tr>
                                        <tr>
                                            <th>
                                                <div class="color-box"></div>
                                            </th>
                                            <th>EA</th>
                                            <c:forEach items="${totalsList}" var="totals">
                                                <c:set var="t"
                                                       value="${totals.erSeconds + totals.pccSeconds + totals.uedSeconds}"/>
                                                <td><fmt:formatNumber pattern="##0.0"
                                                                      value="${t == 0 ? 0 : (totals.erSeconds + totals.pccSeconds) / t * 100}"/>%
                                                </td>
                                            </c:forEach>
                                        </tr>
                                        <tr>
                                            <th>
                                                <div class="color-box"></div>
                                            </th>
                                            <th>SA</th>
                                            <c:forEach items="${totalsList}" var="totals">
                                                <c:set var="t"
                                                       value="${totals.erSeconds + totals.pccSeconds + totals.uedSeconds}"/>
                                                <td><fmt:formatNumber pattern="##0.0"
                                                                      value="${t == 0 ? 0 : totals.abuSeconds / (t) * 100}"/>%
                                                </td>
                                            </c:forEach>
                                        </tr>
                                        </tbody>
                                    </table>
                                </s:chart-widget>
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
    </jsp:body>
</t:report-page>