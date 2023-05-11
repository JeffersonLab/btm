<%@tag description="Monthly Schedule Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="title" required="true" type="java.lang.String" %>
<%@attribute name="month" required="true" type="java.util.Date" %>
<%@attribute name="version" required="true" type="java.lang.Integer" %>
<%@attribute name="schedule" required="true" type="org.jlab.btm.persistence.entity.MonthlySchedule" %>
<%@attribute name="fullscreenAvailable" required="true" type="java.lang.Boolean" %>
<fmt:formatDate value="${month}" pattern="MMMM yyyy" var="formattedDate"/>
<div>
    <ul class="fork-option-pair quick-nav">
        <li>
            <a href="${previousUrl}" class="right-fork-option">Previous</a>
        </li>
        <li>
            <a href="${nextUrl}" class="left-fork-option">Next</a>
        </li>
    </ul>
    <h2><c:out value="${title}"/></h2>
</div>
<section>
    <div id="report-page-actions">
        <select id="view-select" name="view" form="filter-form">
            <option value="calendar"${param.view eq 'calendar' ? ' selected="selected"' : ''}>Calendar</option>
            <option value="table"${param.view eq 'table' ? ' selected="selected"' : ''}>Table</option>
        </select>
        <c:if test="${fullscreenAvailable}">
            <button id="fullscreen-button">Full Screen</button>
        </c:if>
    </div>
    <div>
        <s:filter-flyout-widget ribbon="true">
            <form id="filter-form" action="schedule" method="get">
                <fieldset>
                    <legend>Filter</legend>
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key"><label for="date">Date</label></div>
                            <div class="li-value"><input id="date" class="monthpicker" placeholder="MMMM YYYY"
                                                         type="text" value="${formattedDate}"/></div>
                        </li>
                        <li>
                            <div class="li-key"><label for="version">Version #</label></div>
                            <div class="li-value">
                                <select id="version">
                                    <c:choose>
                                        <c:when test="${fn:length(scheduleList) eq 0}">
                                            <option value=" ">None</option>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${scheduleList}" var="s">
                                                <c:choose>
                                                    <c:when test="${s.publishedDate ne null}">
                                                        <fmt:formatDate pattern="'Published' dd MMM yyyy"
                                                                        value="${s.publishedDate}"
                                                                        var="publishedDateStr"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set value="Tentative" var="publishedDateStr"/>
                                                    </c:otherwise>
                                                </c:choose>
                                                <option value="${s.version}"${version eq s.version ? ' selected="selected"' : ''}>${s.version}
                                                    (${publishedDateStr})
                                                </option>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </select>
                                <span id="version-indicator" class="form-control-indicator"></span>
                            </div>
                        </li>
                    </ul>
                </fieldset>
                <input id="print-input" type="hidden" name="print" value="${param.print}"/>
                <input id="fullscreen-input" type="hidden" name="fullscreen" value="${param.fullscreen}"/>
                <button id="filter-form-submit-button">Apply</button>
            </form>
        </s:filter-flyout-widget>

        <ul class="filterable-breadcrumb">
            <li>
                <span class="crumb"><c:out value="${formattedDate}"/></span>
            </li>
            <li>
                <span class="crumb">Version ${version}
                    <c:if test="${schedule ne null}">
                        <c:choose>
                            <c:when test="${schedule.publishedDate ne null}">
                                (Published <fmt:formatDate value="${schedule.publishedDate}" pattern="dd MMM yyyy"/>)
                            </c:when>
                            <c:otherwise>
                                (Tentative)
                            </c:otherwise>
                        </c:choose>
                    </c:if>
                </span>
            </li>
        </ul>
    </div>
    <c:choose>
        <c:when test="${schedule eq null}">
            <div id="no-schedule-message">-- No Schedule Found --</div>
            <c:if test="${pageContext.request.isUserInRole('schcom')}">
                <div id="form-panel">
                    <form id="create-form" class="create-from-scratch" method="post" action="/btm/create-schedule">
                        <fmt:formatDate value="${month}" pattern="MMMM-yyyy" var="urlDate"/>
                        <input type="hidden" name="date" value="${fn:toLowerCase(urlDate)}"/>
                        <input type="submit" value="Create" id="create-button"/>
                    </form>
                    <form id="upload-form" style="float: right;" method="post" action="/btm/schedule/upload"
                          enctype="multipart/form-data">
                        <input type="button" value="Upload" onclick="document.getElementById('fileInput').click();"/>
                        <input id="fileInput" style="display: none;" type="file" name="upload"/>
                    </form>
                </div>
            </c:if>
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${schedule.publishedDate eq null and not (pageContext.request.isUserInRole('schcom'))}">
                    <div id="no-schedule-message">-- Committee Member Login Required for Tentative Schedule --</div>
                </c:when>
                <c:when test="${calendar ne null}">
                    <t:schedule-calendar calendar="${calendar}" monthHeader="${formattedDate.toString()}"/>
                </c:when>
                <c:otherwise>
                    <t:schedule-table schedule="${schedule}"/>
                </c:otherwise>
            </c:choose>
            <div id="form-panel">
                <form id="html-form" method="get" action="/btm/schedule/schedule.html">
                    <input type="hidden" name="scheduleId" value="${schedule.monthlyScheduleId}"/>
                    <input type="submit" value="HTML"/>
                </form>
                <form id="excel-form" method="get" action="/btm/schedule/schedule.xlsx">
                    <input type="hidden" name="scheduleId" value="${schedule.monthlyScheduleId}"/>
                    <input type="submit" value="Excel"/>
                </form>
                <c:if test="${pageContext.request.isUserInRole('schcom')}">
                    <form id="upload-form" style="float: right; margin-left: 0.5em;" method="post" action="/btm/schedule/upload"
                          enctype="multipart/form-data">
                        <input type="button" value="Upload" onclick="document.getElementById('fileInput').click();"/>
                        <input id="fileInput" style="display: none;" type="file" name="upload"/>
                    </form>
                    <c:choose>
                        <c:when test="${schedule.publishedDate eq null}">
                            <form id="publish-form" method="post" action="/btm/publish-schedule">
                                <input type="hidden" name="scheduleId" value="${schedule.monthlyScheduleId}"/>
                                <input type="hidden" name="fullscreen" value="${param.fullscreen}"/>
                                <input type="submit" value="Publish" id="publish-button"/>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <form id="new-version-form" method="post" action="/btm/new-schedule-version">
                                <input type="hidden" name="scheduleId" value="${schedule.monthlyScheduleId}"/>
                                <input type="hidden" name="fullscreen" value="${param.fullscreen}"/>
                                <input type="submit" value="New Version" id="new-version-button"/>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </c:if>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<div id="exit-fullscreen-panel">
    <button id="exit-fullscreen-button">Exit Full Screen</button>
</div>