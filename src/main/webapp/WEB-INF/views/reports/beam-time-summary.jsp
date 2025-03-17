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
            .sched-info {
                font-size: 0.8em;
                margin-bottom: 2em;
            }
            .flyout-panel {
                border: 1px solid black;
                width: 500px;
                height: 250px;
                position: absolute;
                right: -548px;
                top: -155px;
                z-index: 2;
                background-color: white;
                border-radius: 0.5em;
                box-shadow: 0.5em 0.5em 0.5em #979797;
                padding: 16px;
                font-size: 16px;

                text-align: left;
            }
            #flyouts {
                display: none;
            }
            .definition-flyout-handle {
                position: relative;
            }
            .flyout-panel:after {
                content: '';
                width: 0;
                height: 0;
                border-top: 20px solid transparent;
                border-bottom: 20px solid transparent;
                border-right: 20px solid white;
                top: 50%;
                margin-top: -20px;
                position: absolute;
                left: -20px;
            }
            .flyout-panel:before {
                content: '';
                width: 0;
                height: 0;
                border-top: 21px solid transparent;
                border-bottom: 21px solid transparent;
                border-right: 21px solid black;
                top: 50%;
                margin-top: -21px;
                position: absolute;
                left: -21px;
            }
            .close-bubble {
                float: right;
                min-width: inherit;
            }
            .definition-bubble-title {
                margin-bottom: 1em;
            }
            .definition-bubble-body {
                font-weight: normal;
                height: 200px;
                overflow: auto;
                font-size: 12px;
            }
            .flyout-parent {
                display: inline-block;
                width: 20px;
            }
            .fullscreen .flyout-parent {
                display: none;
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
        <script type="text/javascript">
            $(document).on("click", ".flyout-link", function () {
                $(".definition-flyout-handle").remove();
                var flyout = $("." + $(this).attr("data-flyout-type") + " .flyout-panel").clone();
                $(this).parent().append('<div class="definition-flyout-handle"></div>');
                $(".definition-flyout-handle").append(flyout);
                return false;
            });
            $(document).on("click", ".close-bubble", function () {
                $(".definition-flyout-handle").remove();
                return false;
            });
        </script>
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
                        <li id="excel-menu-item">Excel</li>
                    </ul>
                </div>
            </div>
            <s:filter-flyout-widget requiredMessage="true">
                <form class="filter-form" method="get" action="beam-time-summary">
                    <fieldset>
                        <legend>Time</legend>
                        <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
                        <div class="sched-info">Schedule granularities differ.  The PD Shift Plan is queried by adjusting the date range to CC shift boundaries (7:00, 15:00, 23:00).  The PAC Schedule is queried by adjusting the date range to day boundaries (midnight).  Select start and end dates within SADs to avoid boundary concerns.</div>
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
                    <div class="message-box"><c:out value="${selectionMessage}"/><div class="flyout-parent"><a class="flyout-link" data-flyout-type="sched-flyout" href="#">*</a></div></div>
                    <c:if test="${ccSum ne null and ccSum.periodHours > 0}">
                        <s:chart-widget>
                            <table class="chart-legend">
                                <thead>
                                <tr>
                                    <th></th>
                                    <th></th>
                                    <th title="Crew Chief Reported Hours">CC Hrs</th>
                                    <th title="Crew Chief Percent of Total Hours">CC %</th>
                                    <th title="PD Scheduled Hours">PD Sched. Hrs</th>
                                    <th title="PAC Scheduled Hours">PAC Sched. Hrs</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">Physics</td>
                                    <td><fmt:formatNumber value="${ccSum.upSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(ccSum.upSeconds / 3600) / ccSum.periodHours * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${pdSum.physicsSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td><fmt:formatNumber value="${pacSum.physicsDays * 24}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">Studies</td>
                                    <td><fmt:formatNumber value="${ccSum.studiesSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(ccSum.studiesSeconds / 3600) / ccSum.periodHours * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${pdSum.studiesSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td><fmt:formatNumber value="${pacSum.studiesDays * 24}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">SAM Restore</td>
                                    <td><fmt:formatNumber value="${ccSum.restoreSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(ccSum.restoreSeconds / 3600) / ccSum.periodHours * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${pdSum.restoreSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td><fmt:formatNumber value="${pacSum.restoreDays * 24}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">ACC</td>
                                    <td><fmt:formatNumber value="${ccSum.accSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(ccSum.accSeconds / 3600) / ccSum.periodHours * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${pdSum.accSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td><fmt:formatNumber value="${pacSum.accDays * 24}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label" title="Studies / Restore / ACC Down (not a Physics Down)">
                                        Internal Down
                                    </td>
                                    <td><fmt:formatNumber value="${ccSum.downSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(ccSum.downSeconds / 3600) / ccSum.periodHours * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td>-</td>
                                    <td>-</td>
                                </tr>
                                <tr class="program-row">
                                    <th colspan="2">Program Time:</th>
                                    <td><fmt:formatNumber value="${ccSum.programSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(ccSum.programSeconds / 3600) / ccSum.periodHours * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${pdSum.programSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td><fmt:formatNumber value="${pacSum.programDays * 24}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">SAM</td>
                                    <td><fmt:formatNumber value="${ccSum.sadSeconds / 3600}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${(ccSum.sadSeconds / 3600) / ccSum.periodHours * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${pdSum.offSeconds / 3600}"
                                                          pattern="#,##0.0"/></td>
                                    <td><fmt:formatNumber value="${pacSum.offDays * 24}"
                                                          pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="data-row">
                                    <th>
                                        <div class="color-box"></div>
                                    </th>
                                    <td class="legend-label">Implicit Off</td>
                                    <td><fmt:formatNumber value="${ccSum.implicitOffHours}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${ccSum.implicitOffHours / ccSum.periodHours * 100}"
                                                           pattern="#,##0.0"/>%)
                                    </td>
                                    <td><fmt:formatNumber value="${pdSum.implicitOffHours}" pattern="#,##0.0"/></td>
                                    <td><fmt:formatNumber value="${pacSum.implicitOffHours}" pattern="#,##0.0"/></td>
                                </tr>
                                <tr class="off-row">
                                    <th colspan="2">Off Time:</th>
                                    <td><fmt:formatNumber value="${ccSum.totalOffHours}" pattern="#,##0.0"/></td>
                                    <td>(<fmt:formatNumber value="${ccSum.totalOffHours / ccSum.periodHours * 100}" pattern="#,##0.0"/>%)</td>
                                    <td><fmt:formatNumber value="${pdSum.totalOffHours}" pattern="#,##0.0"/></td>
                                    <td><fmt:formatNumber value="${pacSum.totalOffHours}" pattern="#,##0.0"/></td>
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
        <div id="flyouts">
            <div class="sched-flyout">
                <div class="flyout-panel">
                    <button class="close-bubble">X</button>
                    <div class="definition-bubble-title">Range and Period</div>
                    <div class="definition-bubble-body">
                        <span>PD</span>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">Start:</div>
                                <div class="li-value"><fmt:formatDate pattern="dd-MMM-yyyy HH" value="${pdSum.start}"/></div>
                            </li>
                            <li>
                                <div class="li-key">End:</div>
                                <div class="li-value"><fmt:formatDate pattern="dd-MMM-yyyy HH" value="${pdSum.end}"/></div>
                            </li>
                            <li>
                                <div class="li-key">Period:</div>
                                <div class="li-value"><fmt:formatNumber pattern="#,##0.0" value="${pdSum.periodHours}"/> hours</div>
                            </li>
                        </ul>
                        <span>PAC</span>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">Start:</div>
                                <div class="li-value"><fmt:formatDate pattern="dd-MMM-yyyy HH" value="${pacSum.start}"/></div>
                            </li>
                            <li>
                                <div class="li-key">End:</div>
                                <div class="li-value"><fmt:formatDate pattern="dd-MMM-yyyy HH" value="${pacSum.end}"/></div>
                            </li>
                            <li>
                                <div class="li-key">Period:</div>
                                <div class="li-value"><fmt:formatNumber pattern="#,##0.0" value="${pacSum.periodHours}"/> hours</div>
                            </li>
                        </ul>
                        <span>CC</span>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">Start:</div>
                                <div class="li-value"><fmt:formatDate pattern="dd-MMM-yyyy HH" value="${start}"/></div>
                            </li>
                            <li>
                                <div class="li-key">End:</div>
                                <div class="li-value"><fmt:formatDate pattern="dd-MMM-yyyy HH" value="${end}"/></div>
                            </li>
                            <li>
                                <div class="li-key">Period:</div>
                                <div class="li-value"><fmt:formatNumber pattern="#,##0.0" value="${ccSum.periodHours}"/> hours</div>
                            </li>
                        </ul>
                        <p><b>Note</b>: Start times are inclusive and End times are exclusive.</p>
                    </div>
                </div>
            </div>
        </div>
        <fmt:formatDate var="startFmt" value="${start}" pattern="${s:getFriendlyDateTimePattern()}"/>
        <fmt:formatDate var="endFmt" value="${end}" pattern="${s:getFriendlyDateTimePattern()}"/>
        <form id="excel-form" method="get" action="${pageContext.request.contextPath}/excel/beam-summary.xlsx">
            <input type="hidden" name="start" value="${startFmt}"/>
            <input type="hidden" name="end" value="${endFmt}"/>
            <button id="excel" type="submit" style="display: none;">Excel</button>
        </form>
    </jsp:body>
</t:report-page>