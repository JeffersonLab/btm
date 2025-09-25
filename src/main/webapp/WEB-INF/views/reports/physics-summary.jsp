<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="s" uri="jlab.tags.smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Physics Time Accounting"/>
<t:report-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/physics-summary.css"/>
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
        <!-- Order bars must come before stack plugin! -->
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/flot-order-bars.js"></script>
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.stack.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.stack.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/physics-summary.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div id="report-page-actions">
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
                <form class="filter-form" method="get" action="physics-summary">
                    <fieldset>
                        <legend>Filter</legend>
                        <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
                        <div class="sched-info">Sched. only accurate on shift boundaries (7:00, 15:00, 23:00)</div>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key"><label class="required-field" for="physics-data">Data</label></div>
                                <div class="li-value">
                                    <select id="physics-data" name="physics-data">
                                        <option value="available"${data eq 'available' ? ' selected="selected"' : ''}>
                                            Available
                                        </option>
                                        <option value="a"${data eq 'a' ? ' selected="selected"' : ''}>Hall A</option>
                                        <option value="b"${data eq 'b' ? ' selected="selected"' : ''}>Hall B</option>
                                        <option value="c"${data eq 'c' ? ' selected="selected"' : ''}>Hall C</option>
                                        <option value="d"${data eq 'd' ? ' selected="selected"' : ''}>Hall D</option>
                                        <option value="table"${data eq 'table' ? ' selected="selected"' : ''}>Table
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
                            <c:when test="${data eq 'a'}">
                                <t:hall-pie-chart hall="a" totals="${physicsTotalsList[0]}"
                                                  unknown="${expUnknownList[0]}"/>
                            </c:when>
                            <c:when test="${data eq 'b'}">
                                <t:hall-pie-chart hall="b" totals="${physicsTotalsList[1]}"
                                                  unknown="${expUnknownList[1]}"/>
                            </c:when>
                            <c:when test="${data eq 'c'}">
                                <t:hall-pie-chart hall="c" totals="${physicsTotalsList[2]}"
                                                  unknown="${expUnknownList[2]}"/>
                            </c:when>
                            <c:when test="${data eq 'd'}">
                                <t:hall-pie-chart hall="d" totals="${physicsTotalsList[3]}"
                                                  unknown="${expUnknownList[3]}"/>
                            </c:when>
                            <c:when test="${data eq 'table'}">
                                <div class="data-table-panel chart-wrap-backdrop">
                                    <h3>Multiplicity (Hours)</h3>
                                    <table id="multiplicity-hourly-table" class="data-table">
                                        <tbody>
                                        <tr>
                                            <th>FOUR UP:</th>
                                            <td><fmt:formatNumber pattern="#,##0.0"
                                                                  value="${multiplicityTotals.fourUpSeconds / 3600}"/></td>
                                        </tr>
                                        <tr>
                                            <th>THREE UP:</th>
                                            <td><fmt:formatNumber pattern="#,##0.0"
                                                                  value="${multiplicityTotals.threeUpSeconds / 3600}"/></td>
                                        </tr>
                                        <tr>
                                            <th class="sub-total">TWO UP:</th>
                                            <td><fmt:formatNumber pattern="#,##0.0"
                                                                  value="${multiplicityTotals.twoUpSeconds / 3600}"/></td>
                                        </tr>
                                        <tr>
                                            <th class="sub-total">ONE UP:</th>
                                            <td><fmt:formatNumber pattern="#,##0.0"
                                                                  value="${multiplicityTotals.oneUpSeconds / 3600}"/></td>
                                        </tr>
                                        <tr>
                                            <th>ANY UP:</th>
                                            <td><fmt:formatNumber pattern="#,##0.0"
                                                                  value="${multiplicityTotals.anyUpSeconds / 3600}"/></td>
                                        </tr>
                                        <tr>
                                            <th>ALL ON UP:</th>
                                            <td><fmt:formatNumber pattern="#,##0.0"
                                                                  value="${multiplicityTotals.allUpSeconds / 3600}"/></td>
                                        </tr>
                                        <tr>
                                            <th>DOWN HARD:</th>
                                            <td><fmt:formatNumber pattern="#,##0.0"
                                                                  value="${multiplicityTotals.downHardSeconds / 3600}"/></td>
                                        </tr>
                                        </tbody>
                                    </table>
                                    <h3>Hall Beam Time (Hours)</h3>
                                    <table id="graph-data-table" class="data-table stripped-table">
                                        <thead>
                                        <tr>
                                            <th rowspan="2">Hall</th>
                                            <th colspan="5">Experimenter</th>
                                            <th colspan="6">Crew Chief</th>
                                            <th>Program Deputy</th>
                                        </tr>
                                        <tr>
                                            <th>ABU</th>
                                            <th>BANU</th>
                                            <th>BNA</th>
                                            <th>OFF</th>
                                            <th>Implicit OFF</th>
                                            <th>UP</th>
                                            <th>TUNE</th>
                                            <th>BNR</th>
                                            <th>DOWN</th>
                                            <th>OFF</th>
                                            <th>Implicit OFF</th>
                                            <th>Scheduled</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${physicsTotalsList}" var="totals" varStatus="status">
                                            <tr>
                                                <th><c:out value="${totals.hall}"/></th>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${totals.abuSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${totals.banuSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${totals.bnaSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${totals.expOffSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${expUnknownList[status.index]}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${totals.upSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${totals.bnrSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${totals.downSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${totals.opOffSeconds / 3600}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${opUnknownList[status.index]}"/></td>
                                                <td><fmt:formatNumber pattern="#,##0.0"
                                                                      value="${scheduledArray[status.index] / 3600}"/></td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <s:chart-widget>
                                    <table class="chart-legend availability-legend">
                                        <tbody>
                                        <tr>
                                            <th>
                                                <div class="color-box"></div>
                                            </th>
                                            <th class="legend-label" colspan="4">Any Up</th>
                                            <td style="text-align: center;"><fmt:formatNumber
                                                    value="${multiplicityTotals.anyUpSeconds / 3600}"
                                                    pattern="#,##0.0"/></td>
                                        </tr>
                                        <tr class="sub-head-row">
                                            <th colspan="2"></th>
                                            <th>A</th>
                                            <th>B</th>
                                            <th>C</th>
                                            <th>D</th>
                                        </tr>
                                        <tr class="per-hall-row">
                                            <th>
                                                <div class="color-box"></div>
                                            </th>
                                            <th class="legend-label">Sched.</th>
                                            <c:forEach items="${scheduledArray}" var="seconds">
                                                <td><fmt:formatNumber value="${seconds / 3600}" pattern="#,##0.0"/></td>
                                            </c:forEach>
                                        </tr>
                                        <tr class="per-hall-row">
                                            <th>
                                                <div class="color-box"></div>
                                            </th>
                                            <th class="legend-label">UP</th>
                                            <c:forEach items="${physicsTotalsList}" var="totals">
                                                <td><fmt:formatNumber value="${totals.upSeconds / 3600}"
                                                                      pattern="#,##0.0"/></td>
                                            </c:forEach>
                                        </tr>
                                        <tr class="sub-head-row">
                                            <th colspan="6"></th>
                                        </tr>
                                        <tr class="per-hall-row">
                                            <th>
                                                <div class="color-box"></div>
                                            </th>
                                            <th class="legend-label">ABU</th>
                                            <c:forEach items="${physicsTotalsList}" var="totals">
                                                <td><fmt:formatNumber value="${totals.abuSeconds / 3600}"
                                                                      pattern="#,##0.0"/></td>
                                            </c:forEach>
                                        </tr>
                                        <tr class="per-hall-row">
                                            <th>
                                                <div class="color-box"></div>
                                            </th>
                                            <th class="legend-label">BANU</th>
                                            <c:forEach items="${physicsTotalsList}" var="totals">
                                                <td><fmt:formatNumber value="${totals.banuSeconds / 3600}"
                                                                      pattern="#,##0.0"/></td>
                                            </c:forEach>
                                        </tr>
                                        <tr class="total-row">
                                            <th colspan="2"></th>
                                            <c:forEach items="${physicsTotalsList}" var="totals" varStatus="status">
                                                <td><fmt:formatNumber
                                                        value="${(totals.abuSeconds + totals.banuSeconds) / 3600}"
                                                        pattern="#,##0.0"/></td>
                                            </c:forEach>
                                        </tr>
                                        <tr class="percent-row">
                                            <th colspan="2"></th>
                                            <c:forEach items="${physicsTotalsList}" var="totals" varStatus="status">
                                                <td>
                                                    <c:if test="${scheduledArray[status.index] > 0}">
                                                        <fmt:formatNumber
                                                                value="${((totals.abuSeconds + totals.banuSeconds) / scheduledArray[status.index]) * 100}"
                                                                pattern="#,##0"/>%
                                                    </c:if>
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