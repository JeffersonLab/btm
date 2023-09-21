<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Accumulated Charge"/>
<t:report-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/charge.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <c:choose>
            <c:when test="${'CDN' eq resourceLocation}">
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.time.min.js"></script>
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.crosshair.min.js"></script>
                <script src="${cdnContextPath}/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:when>
            <c:otherwise><!-- LOCAL -->
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.time.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.crosshair.min.js"></script>
                <script src="${pageContext.request.contextPath}/resources/jquery-plugins/flot/0.8.3/jquery.flot.resize.min.js"></script>
            </c:otherwise>
        </c:choose>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/jquery.flot.dashes.js"></script>
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/charge.js"></script>
        <script>
            var dataA = jlab.series.charge.scheduled.a.data,
                dataB = jlab.series.charge.scheduled.b.data,
                dataC = jlab.series.charge.scheduled.c.data,
                dataD = jlab.series.charge.scheduled.d.data;
            <c:forEach items="${scheduledChargeData.chargeListA}" var="record">
                dataA.push([${record.timestamp}, ${record.coulombs}]);
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListB}" var="record">
                dataB.push([${record.timestamp}, ${record.nC * 0.000001}]);
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListC}" var="record">
                dataC.push([${record.timestamp}, ${record.coulombs}]);
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListD}" var="record">
                dataD.push([${record.timestamp}, ${record.nC * 0.000001}]);
            </c:forEach>

            dataA = jlab.series.current.scheduled.a.data;
            dataB = jlab.series.current.scheduled.b.data;
            dataC = jlab.series.current.scheduled.c.data;
            dataD = jlab.series.current.scheduled.d.data;

            <c:forEach items="${scheduledChargeData.chargeListA}" var="record">
            dataA.push([${record.timestamp}, ${record.nA * 0.001}]);
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListB}" var="record">
            dataB.push([${record.timestamp}, ${record.nA}]);
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListC}" var="record">
            dataC.push([${record.timestamp}, ${record.nA * 0.001}]);
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListD}" var="record">
            dataD.push([${record.timestamp}, ${record.nA}]);
            </c:forEach>

            dataA = jlab.series.programs.a;
            dataB = jlab.series.programs.b;
            dataC = jlab.series.programs.c;
            dataD = jlab.series.programs.d;

            <c:forEach items="${scheduledChargeData.chargeListA}" var="record">
            dataA.push('${record.program}');
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListB}" var="record">
            dataB.push('${record.program}');
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListC}" var="record">
            dataC.push('${record.program}');
            </c:forEach>
            <c:forEach items="${scheduledChargeData.chargeListD}" var="record">
            dataD.push('${record.program}');
            </c:forEach>
        </script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div id="report-page-actions">
                <button id="fullscreen-button">Full Screen</button>
                <div id="export-widget">
                    <button id="export-menu-button">Export</button>
                    <ul id="export-menu">
                        <li id="image-menu-item" data-wait-for-selector="body.done">Image</li>
                        <li id="print-menu-item">Print</li>
                    </ul>
                </div>
            </div>
            <s:filter-flyout-widget requiredMessage="true">
                <form id="filter-form" method="get" action="charge">
                    <fieldset>
                        <legend>Filter</legend>
                        <s:date-range/>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key"><label for="scale">Schedule Scale Factor</label></div>
                                <div class="li-value">
                                    <input id="scale" name="scale" type="number" value="${param.scale}" min="0.1" max="2" step="0.1"/>
                                </div>
                            </li>
                        </ul>
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
                    <div class="error-box"></div>
                    <c:if test="${period > 0}">
                                <s:chart-widget>
                                    <table class="chart-legend">
                                        <tbody>
                                        <tr class="sub-head-row">
                                            <th colspan="2"><div class="key-header hall-key-header">Hall</div></th>
                                            <th><div class="key-header scheduled-key-header">Scheduled</div></th>
                                            <th><div class="key-header delivered-key-header">Delivered</div></th>
                                        </tr>
                                        <tr>
                                            <th>
                                                <div class="color-box" style="background-color: blue;"></div>
                                            </th>
                                            <th>A</th>
                                            <td></td>
                                            <td></td>
                                        </tr>
                                        <tr>
                                            <th>
                                                <div class="color-box" style="background-color: red;"></div>
                                            </th>
                                            <th>B</th>
                                            <td></td>
                                            <td></td>
                                        </tr>
                                        <tr>
                                            <th>
                                                <div class="color-box" style="background-color: green;"></div>
                                            </th>
                                            <th>C</th>
                                            <td></td>
                                            <td></td>
                                        </tr>
                                        <tr>
                                            <th>
                                                <div class="color-box" style="background-color: orange;"></div>
                                            </th>
                                            <th>D</th>
                                            <td></td>
                                            <td></td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </s:chart-widget>
                    </c:if>
                </c:otherwise>
            </c:choose>
            <table class="quad-table">
                <tbody>
                <tr>
                    <td>
                        <h3>Hall A</h3>
                        <div class="custom-wrap has-y-axis-label">
                            <table class="custom-key">
                                <tbody>
                                <tr>
                                    <th><div class="key-header scheduled-key-header">Scheduled</div></th>
                                    <th><div class="key-header delivered-key-header">Delivered</div></th>
                                </tr>
                                <tr><th id="key-a-current-scheduled"></th><th id="key-a-current-delivered"></th></tr>
                                <tr><th id="key-a-charge-scheduled"></th><th id="key-a-charge-delivered"></th></tr>
                                </tbody>
                            </table>
                            <div id="charta-charge-placeholder" class="charge-placeholder"></div>
                            <div id="charta-current-placeholder" class="current-placeholder"></div>
                            <div id="date-tooltip-a" class="date-tooltip"></div>
                            <div id="program-tooltip-a" class="program-tooltip"></div>
                        </div>
                    </td>
                    <td>
                        <h3>Hall B</h3>
                        <div class="custom-wrap has-y-axis-label">
                            <table class="custom-key">
                                <tbody>
                                <tr>
                                    <th><div class="key-header scheduled-key-header">Scheduled</div></th>
                                    <th><div class="key-header delivered-key-header">Delivered</div></th>
                                </tr>
                                <tr><th id="key-b-current-scheduled"></th><th id="key-b-current-delivered"></th></tr>
                                <tr><th id="key-b-charge-scheduled"></th><th id="key-b-charge-delivered"></th></tr>
                                </tbody>
                            </table>
                            <div id="chartb-charge-placeholder" class="charge-placeholder"></div>
                            <div id="chartb-current-placeholder" class="current-placeholder"></div>
                            <div id="date-tooltip-b" class="date-tooltip"></div>
                            <div id="program-tooltip-b" class="program-tooltip"></div>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <h3>Hall C</h3>
                        <div class="custom-wrap has-y-axis-label">
                            <table class="custom-key">
                                <tbody>
                                <tr>
                                    <th><div class="key-header scheduled-key-header">Scheduled</div></th>
                                    <th><div class="key-header delivered-key-header">Delivered</div></th>
                                </tr>
                                <tr><th id="key-c-current-scheduled"></th><th id="key-c-current-delivered"></th></tr>
                                <tr><th id="key-c-charge-scheduled"></th><th id="key-c-charge-delivered"></th></tr>
                                </tbody>
                            </table>
                            <div id="chartc-charge-placeholder" class="charge-placeholder"></div>
                            <div id="chartc-current-placeholder" class="current-placeholder"></div>
                            <div id="date-tooltip-c" class="date-tooltip"></div>
                            <div id="program-tooltip-c" class="program-tooltip"></div>
                        </div>
                    </td>
                    <td>
                        <h3>Hall D</h3>
                        <div class="custom-wrap has-y-axis-label">
                            <table class="custom-key">
                                <tbody>
                                <tr>
                                    <th><div class="key-header scheduled-key-header">Scheduled</div></th>
                                    <th><div class="key-header delivered-key-header">Delivered</div></th>
                                </tr>
                                <tr><th id="key-d-current-scheduled"></th><th id="key-d-current-delivered"></th></tr>
                                <tr><th id="key-d-charge-scheduled"></th><th id="key-d-charge-delivered"></th></tr>
                                </tbody>
                            </table>
                            <div id="chartd-charge-placeholder" class="charge-placeholder"></div>
                            <div id="chartd-current-placeholder" class="current-placeholder"></div>
                            <div id="date-tooltip-d" class="date-tooltip"></div>
                            <div id="program-tooltip-d" class="program-tooltip"></div>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
        <div id="busy-wait">
            <img src="${pageContext.request.contextPath}/resources/img/indicator16x16.gif"/>Querying Archiver for Current and Integrating.  Please wait...
        </div>
    </jsp:body>
</t:report-page>