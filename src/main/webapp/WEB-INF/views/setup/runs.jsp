<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="s" uri="jlab.tags.smoothness" %>
<c:set var="title" value="Runs"/>
<c:set var="dayPattern" value="dd-MMM-yyyy"/>
<s:setup-page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/runs.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/runs.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <p>A run is a stretch of days the accelerator is scheduled to be on: consecutive days whose program
                is not OFF in each month's latest published schedule.  A month without a published schedule counts
                as OFF.  Report date range selectors offer the current and previous runs, from
                <a href="${pageContext.request.contextPath}/rest/runs">rest/runs</a>.</p>
            <p>Runs are recalculated whenever a schedule is published and when the app starts.  Where the actual run
                differs from the schedule, edit it to set its days by hand: a run set by hand stays as it is when
                the schedules change, and takes the place of the scheduled run that shares its days.  Remove it to
                go back to the schedule.</p>
            <ul class="key-value-list">
                <li>
                    <div class="li-key">Current Run</div>
                    <div class="li-value">
                        <c:choose>
                            <c:when test="${currentRun ne null}">
                                <fmt:formatDate value="${currentRun.startDay}" pattern="${dayPattern}"/> to
                                <fmt:formatDate value="${currentRun.lastDay}" pattern="${dayPattern}"/>
                            </c:when>
                            <c:otherwise>None</c:otherwise>
                        </c:choose>
                    </div>
                </li>
                <li>
                    <div class="li-key">Previous Run</div>
                    <div class="li-value">
                        <c:choose>
                            <c:when test="${previousRun ne null}">
                                <fmt:formatDate value="${previousRun.startDay}" pattern="${dayPattern}"/> to
                                <fmt:formatDate value="${previousRun.lastDay}" pattern="${dayPattern}"/>
                            </c:when>
                            <c:otherwise>None</c:otherwise>
                        </c:choose>
                    </div>
                </li>
            </ul>
            <s:editable-row-table-controls/>
            <c:choose>
                <c:when test="${fn:length(runList) > 0}">
                    <table id="run-table" class="data-table stripped-table uniselect-table editable-row-table">
                        <thead>
                        <tr>
                            <th>First Day</th>
                            <th>Last Day</th>
                            <th>Days</th>
                            <th>Source</th>
                            <th>Published Schedules</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${runList}" var="run">
                            <fmt:formatDate value="${run.startDay}" pattern="${dayPattern}" var="firstDay"/>
                            <fmt:formatDate value="${run.lastDay}" pattern="${dayPattern}" var="lastDay"/>
                            <tr data-id="${run.runId}" data-first-day="${firstDay}" data-last-day="${lastDay}"
                                data-manual="${run.manual}">
                                <td><c:out value="${firstDay}"/></td>
                                <td><c:out value="${lastDay}"/></td>
                                <td>${run.days}</td>
                                <td>${run.manual ? 'Set by hand' : 'Schedule'}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${run.asScheduled}">Same</c:when>
                                        <c:when test="${run.scheduled ne null}">
                                            <span class="schedule-differs"><fmt:formatDate
                                                    value="${run.scheduledStartDay}" pattern="${dayPattern}"/> to
                                            <fmt:formatDate value="${run.scheduledLastDay}"
                                                            pattern="${dayPattern}"/></span>
                                        </c:when>
                                        <c:otherwise>No run</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p>There are no runs.</p>
                </c:otherwise>
            </c:choose>
            <form id="recalculate-form" method="post" action="runs">
                <input type="hidden" name="action" value="recalculate"/>
                <button type="submit">Recalculate from Schedules</button>
            </form>
        </section>
        <s:editable-row-table-dialog>
            <section>
                <form id="row-form">
                    <ul class="key-value-list">
                        <li>
                            <div class="li-key">
                                <label for="row-first-day">First Day</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="row-first-day" class="date-input" placeholder="DD-MMM-YYYY"
                                       autocomplete="off"/>
                            </div>
                        </li>
                        <li>
                            <div class="li-key">
                                <label for="row-last-day">Last Day</label>
                            </div>
                            <div class="li-value">
                                <input type="text" id="row-last-day" class="date-input" placeholder="DD-MMM-YYYY"
                                       autocomplete="off"/>
                            </div>
                        </li>
                    </ul>
                    <p id="row-form-note">The run is then kept as set here when the schedules change.</p>
                </form>
            </section>
        </s:editable-row-table-dialog>
    </jsp:body>
</s:setup-page>
