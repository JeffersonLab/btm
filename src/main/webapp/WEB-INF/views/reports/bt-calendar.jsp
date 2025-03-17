<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Review Calendar"/>
<t:report-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/bt-calendar.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/bt-calendar.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <fmt:formatDate var="todayFmt" value="${today}" pattern="dd-MMM-yyyy HH:mm"/>
            <fmt:formatDate var="sevenDaysAgoFmt" value="${sevenDaysAgo}" pattern="dd-MMM-yyyy HH:mm"/>
            <fmt:formatDate var="formattedDate" value="${start}" pattern="MMMM yyyy"/>
            <fmt:formatDate var="previousMonthFmt" value="${previousMonth}" pattern="MMMM yyyy"/>
            <fmt:formatDate var="nextMonthFmt" value="${nextMonth}" pattern="MMMM yyyy"/>
            <c:url var="previousUrl" value="/reports/bt-calendar">
                <c:param name="date" value="${previousMonthFmt}"/>
                <c:param name="print" value="${param.print}"/>
                <c:param name="fullscreen" value="${param.fullscreen}"/>
                <c:param name="qualified" value=""/>
            </c:url>
            <c:url var="nextUrl" value="/reports/bt-calendar">
                <c:param name="date" value="${nextMonthFmt}"/>
                <c:param name="print" value="${param.print}"/>
                <c:param name="fullscreen" value="${param.fullscreen}"/>
                <c:param name="qualified" value=""/>
            </c:url>
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
                <form class="filter-form" method="get" action="bt-calendar">
                    <fieldset>
                        <legend>Time</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="date" title="Inclusive">Month</label>
                                </div>
                                <div class="li-value">
                                    <div class="li-value"><input id="date" name="date" class="monthpicker"
                                                                 placeholder="MMMM YYYY" type="text"
                                                                 value="${formattedDate}"/></div>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
        </section>
        <section>
            <div class="float-breadbox">
                <ul>
                    <li>
                        <a href="${previousUrl}">Previous</a>
                    </li>
                    <li>
                        <a href="${nextUrl}">Next</a>
                    </li>
                </ul>
            </div>
            <c:choose>
                <c:when test="${start == null}">
                    <div class="message-box">Select a month to continue</div>
                </c:when>
                <c:otherwise>
                    <div class="message-box"><c:out value="${selectionMessage}"/></div>
                    <table id="calendar-table" class="data-table">
                        <thead>
                        <tr>
                            <th>&nbsp;</th>
                            <th class="weekday-header">Sun</th>
                            <th class="weekday-header">Mon</th>
                            <th class="weekday-header">Tue</th>
                            <th class="weekday-header">Wed</th>
                            <th class="weekday-header">Thu</th>
                            <th class="weekday-header">Fri</th>
                            <th class="weekday-header">Sat</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${weekList}" var="week">
                            <tr>
                                <th class="left-header-cell">
                                    <table class="left-header-table">
                                        <tbody>
                                        <tr>
                                            <td>&nbsp;</td>
                                        </tr>
                                        <tr>
                                            <td>Program Hrs:</td>
                                        </tr>
                                        <tr>
                                            <td>Down Hrs:</td>
                                        </tr>
                                        <tr>
                                            <td>Owl/Day/Swing:</td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </th>
                                <c:forEach items="${week.dayList}" var="day">
                                    <c:set var="programHours" value="${day.accTotal.programSeconds / 3600}"/>
                                    <c:set var="possibleDownHours" value="${day.accTotal.possibleDownSeconds / 3600}"/>
                                    <c:set var="downHours" value="${day.downTotal.eventSeconds / 3600}"/>
                                    <fmt:formatDate var="startFmt" value="${day.day}" pattern="dd-MMM-yyyy HH:mm"/>
                                    <fmt:formatDate var="endFmt" value="${day.calculateNextDay()}"
                                                    pattern="dd-MMM-yyyy HH:mm"/>
                                    <c:url var="programUrl" value="/reports/beam-time-summary">
                                        <c:param name="start" value="${startFmt}"/>
                                        <c:param name="end" value="${endFmt}"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                    </c:url>
                                    <c:url var="downUrl" value="/reports/downtime-summary" context="/dtm">
                                        <c:param name="start" value="${startFmt}"/>
                                        <c:param name="end" value="${endFmt}"/>
                                        <c:param name="type" value="1"/>
                                        <c:param name="print" value="${param.print}"/>
                                        <c:param name="fullscreen" value="${param.fullscreen}"/>
                                        <c:param name="qualified" value=""/>
                                    </c:url>
                                    <td class="detail-holder-cell">
                                        <div class="dialog reason-dialog" title="Needs Review Reason">
                                            <ul class="reason-list">
                                            </ul>
                                        </div>
                                        <table class="day-detail-table">
                                            <tbody>
                                            <tr>
                                                <td class="${(downHours - 0.5) > possibleDownHours  ? 'bad-availability' : ''}"><a
                                                        href="#" class="date-link"><fmt:formatDate value="${day.day}"
                                                                                                   pattern="dd"/></a>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    <a target="_blank" href="${programUrl}"><fmt:formatNumber
                                                            value="${programHours}" pattern="###,##0.0"/></a>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td><a target="_blank" href="${downUrl}"><fmt:formatNumber
                                                        value="${downHours}" pattern="###,##0.0"/></a></td>
                                            </tr>
                                            <tr>
                                                <fmt:formatDate var="dateFmt" value="${day.day}" pattern="dd-MMM-yyyy"/>
                                                <c:url var="owlUrl" value="/timesheet/cc/${dateFmt}/owl/hours"/>
                                                <c:url var="dayUrl" value="/timesheet/cc/${dateFmt}/day/hours"/>
                                                <c:url var="swingUrl"
                                                       value="/timesheet/cc/${dateFmt}/swing/hours"/>
                                                <td><a target="_blank" class="${day.future ? '' : 'shift-check'}"
                                                       href="${owlUrl}">-</a>/<a target="_blank"
                                                                                 class="${day.future ? '' : 'shift-check'}"
                                                                                 href="${dayUrl}">-</a>/<a
                                                        target="_blank" class="${day.future ? '' : 'shift-check'}"
                                                        href="${swingUrl}">-</a></td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </td>
                                </c:forEach>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                    <table id="bad-data-key-table">
                        <tbody>
                        <tr>
                            <td>
                                <div style="background-color: pink;" class="color-box"></div>
                            </td>
                            <td>DTM discrepancy&nbsp;</td>
                            <td>
                                <div style="background-color: #ffffaa;" class="color-box"></div>
                            </td>
                            <td>Data needs review</td>
                        </tr>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </section>
        <div id="exit-fullscreen-panel">
            <button id="exit-fullscreen-button">Exit Full Screen</button>
        </div>
    </jsp:body>
</t:report-page>