<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Accelerator Beam Time"/>
<t:report-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <style type="text/css">
            .program-row td, .program-row th {
                height: 50px;
                vertical-align: top;
            }

            .program-row th, .off-row th {
                text-align: right;
                white-space: nowrap;
            }

            .chart-legend tbody tr, .chart-legend tbody th, .chart-legend tbody td {
                border-bottom: none;
            }
        </style>
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
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/beam-time-summary.js"></script>
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
                <form id="filter-form" method="get" action="beam-time-summary">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
                    </fieldset>
                    <input id="filter-form-submit-button" type="submit" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 id="page-header-title"><c:out value="${title}"/></h2>
            <c:choose>
                <c:when test="${start == null || end == null}">
                    <div class="message-box">Select a start date and end date to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box"><c:out value="${selectionMessage}"/></div>
                    <c:if test="${period > 0}">
                        <s:chart-widget>
                            <table class="chart-legend">
                                <thead>
                                <tr>
                                    <th></th>
                                    <th></th>
                                    <th title="Actual Hours">Hrs</th>
                                    <th title="Percent of total hours">%</th>
                                    <th title="PD Scheduled Hours">PD Sched. Hrs</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">Physics</td>
                                    <td><fmt:formatNumber value="${totals.upSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(totals.upSeconds / 3600) / period * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${accScheduledArray[4] / 3600}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">Studies</td>
                                    <td><fmt:formatNumber value="${totals.studiesSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(totals.studiesSeconds / 3600) / period * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${accScheduledArray[0] / 3600}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">SAD Restore</td>
                                    <td><fmt:formatNumber value="${totals.restoreSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(totals.restoreSeconds / 3600) / period * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${accScheduledArray[1] / 3600}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">ACC</td>
                                    <td><fmt:formatNumber value="${totals.accSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(totals.accSeconds / 3600) / period * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${accScheduledArray[2] / 3600}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label" title="Studies / Restore / ACC Down (not a Physics Down)">
                                        Internal Down
                                    </td>
                                    <td><fmt:formatNumber value="${totals.downSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(totals.downSeconds / 3600) / period * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td>-</td>
                                </tr>
                                <tr class="program-row">
                                    <th colspan="2">Program Time:</th>
                                    <td><fmt:formatNumber value="${programHours}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${programHours / period * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td>-</td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">SAD</td>
                                    <td><fmt:formatNumber value="${totals.sadSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(totals.sadSeconds / 3600) / period * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${accScheduledArray[3] / 3600}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">Implicit Off</td>
                                    <td><fmt:formatNumber value="${unknownHours}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${unknownHours / period * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td>-</td>
                                </tr>
                                <tr class="off-row">
                                    <th colspan="2">Off Time:</th>
                                    <td><fmt:formatNumber value="${offHours}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${offHours / period * 100}" pattern="#,##0.0"/>%)</td>
                                    <td><fmt:formatNumber value="${accScheduledArray[3] / 3600}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                </tbody>
                            </table>
                        </s:chart-widget>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
    </jsp:body>
</t:report-page>