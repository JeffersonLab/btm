<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="s" uri="jlab.tags.smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="State Timeline"/>
<c:set var="timePattern" value="dd-MMM-yyyy HH:mm:ss"/>
<s:page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/state-timeline.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/state-timeline.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <form class="filter-form" method="get" action="state-timeline">
                <input type="hidden" class="offset-input" name="offset" value="0"/>
            </form>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <div id="current-program">
                <div id="current-program-label">Current Program</div>
                <c:choose>
                    <c:when test="${current ne null}">
                        <div id="current-program-name" class="program-${fn:toLowerCase(current.program)}"><c:out
                                value="${current.program.label}"/></div>
                        <div id="current-program-since">since <fmt:formatDate value="${current.startDate}"
                                                                              pattern="${timePattern}"/>
                            (${btm:formatHoursBetween(current.startDate, null)} hours)
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div id="current-program-name" class="program-none">Not Recorded</div>
                    </c:otherwise>
                </c:choose>
            </div>
            <p id="program-source">
                <c:choose>
                    <c:when test="${programPv eq null}">
                        Program changes are not recorded from EPICS, since no program PV is configured; this
                        timeline is kept by hand.
                    </c:when>
                    <c:when test="${programPvEntry eq null}">
                        <span class="ui-state-error-text">No value has been received yet from the program PV
                            <c:out value="${programPv}"/>, which program changes are recorded from.</span>
                    </c:when>
                    <c:otherwise>
                        Program changes are recorded from the PV <c:out value="${programPv}"/>:
                        <span class="${programPvEntry.connected and programPvRecognized ? '' : 'ui-state-error-text'}">
                            <c:out value="${programPvEntry.status}"/>, value
                            &quot;<c:out value="${programPvValue}"/>&quot;<c:if
                                test="${not programPvRecognized}"> (not a program, so it is ignored)</c:if>
                        </span>
                        as of <fmt:formatDate value="${programPvEntry.ts}" pattern="${timePattern}"/>.
                    </c:otherwise>
                </c:choose>
                Downtime is recorded in DTM, not here: timesheets count DTM downtime during Studies, NPES Restore
                and ACC as Internal Down.
            </p>
            <h3>History</h3>
            <c:if test="${editable}">
                <s:editable-row-table-controls/>
            </c:if>
            <c:choose>
                <c:when test="${fn:length(intervalList) > 0}">
                    <table id="interval-table"
                           class="data-table stripped-table${editable ? ' uniselect-table editable-row-table' : ''}">
                        <thead>
                        <tr>
                            <th>Program</th>
                            <th>Start</th>
                            <th>End</th>
                            <th>Hours</th>
                            <th>Recorded By</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${intervalList}" var="interval">
                            <fmt:formatDate value="${interval.startDate}" pattern="${timePattern}" var="start"/>
                            <fmt:formatDate value="${interval.startDate}" pattern="dd MMM yyyy HH:mm:ss z"
                                            var="startWithZone"/>
                            <fmt:formatDate value="${interval.endDate}" pattern="dd MMM yyyy HH:mm:ss z"
                                            var="endWithZone"/>
                            <tr data-id="${interval.ccProgramIntervalId}" data-program="${interval.program}"
                                data-start="${start}">
                                <td><c:out value="${interval.program.label}"/></td>
                                <td title="${startWithZone}"><c:out value="${start}"/></td>
                                <td title="${endWithZone}">
                                    <c:choose>
                                        <c:when test="${interval.endDate eq null}">
                                            <span class="current-interval">Current</span>
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatDate value="${interval.endDate}" pattern="${timePattern}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${btm:formatHoursBetween(interval.startDate, interval.endDate)}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${interval.modifiedBy eq null}">
                                            EPICS
                                        </c:when>
                                        <c:otherwise>
                                            <fmt:formatDate value="${interval.modifiedDate}"
                                                            pattern="${s:getFriendlyDateTimePattern()}"
                                                            var="modified"/>
                                            <span title="${modified}"><c:out
                                                    value="${s:formatUsername(interval.modifiedBy)}"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                    <div class="event-controls">
                        <span id="interval-count">${paginator.startNumber} - ${paginator.endNumber} of ${paginator.totalRecords}</span>
                        <button class="previous-button" type="button" data-offset="${paginator.previousOffset}"
                                value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous
                        </button>
                        <button class="next-button" type="button" data-offset="${paginator.nextOffset}"
                                value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next
                        </button>
                    </div>
                </c:when>
                <c:otherwise>
                    <p>No program has been recorded.</p>
                </c:otherwise>
            </c:choose>
            <c:if test="${not editable}">
                <span class="login-message">You must authenticate as a Crew Chief or Operability Manager to edit</span>
            </c:if>
        </section>
        <s:editable-row-table-dialog>
            <section>
                <form id="row-form">
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key">
                                <label for="row-program">Program</label>
                            </div>
                            <div class="li-value">
                                <select id="row-program">
                                    <c:forEach items="${programList}" var="program">
                                        <option value="${program}"><c:out value="${program.label}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">
                                <label for="row-start">Start</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="row-start" placeholder="DD-MMM-YYYY hh:mm:ss"
                                       autocomplete="off"/>
                            </div>
                        </li>
                    </ul>
                    <p id="row-form-note">The interval before the start ends there. Seconds are optional.</p>
                </form>
            </section>
        </s:editable-row-table-dialog>
    </jsp:body>
</s:page>
