<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<div id="cross-check-section" class="tabset">
    <ul>
        <li><a href="#cross-check-summary-tab">Shift Summary</a></li>
        <li><a href="#cross-check-details-tab">Details</a></li>
        <li><a href="#cross-check-hourly-tab">Hourly</a></li>
    </ul>
    <div id="cross-check-summary-tab" data-signature="${fn:length(signatureList) > 0}">
        <div id="cross-check-summary-panel">

            <table class="data-table stripped-table cross-check-details">
                <thead>
                <tr>
                    <th colspan="2">BTM</th>
                    <th rowspan="3" style="width: 5px;"></th>
                    <th colspan="2">DTM</th>
                    <th rowspan="3" style="width: 5px;"></th>
                    <th colspan="2">COMPUTED</th>
                </tr>
                <tr>
                    <th>{RESEARCH,TUNING,PHYSICS DOWN}</th>
                    <th>{BLOCKED}</th>
                    <th>{PHYSICS, INTERNAL DOWN}</th>
                    <th>{PHYSICS}</th>
                    <th>{BLOCKED - INTERNAL DOWN}</th>
                    <th>{PHYSICS - TUNING - PHYSICS DOWN}</th>
                </tr>
                <tr>
                    <th class="duration-header">PHYSICS</th>
                    <th class="duration-header">INTERNAL DOWN</th>
                    <th class="duration-header">
                        <fmt:formatDate value="${startHour}" pattern="dd-MMM-yyyy HH:mm" var="startStr"/>
                        <fmt:formatDate value="${endHour}" pattern="dd-MMM-yyyy HH:mm" var="endStr"/>
                        <c:url var="url" value="${env['FRONTEND_SERVER_URL']}/dtm/reports/downtime-summary">
                            <c:param name="type" value="1"/>
                            <c:param name="start" value="${startStr}"/>
                            <c:param name="end" value="${endStr}"/>
                        </c:url>
                        <a target="_blank" href="${url}">BLOCKED</a>
                    </th>
                    <th class="duration-header">
                        <c:url var="url" value="${env['FRONTEND_SERVER_URL']}/dtm/reports/downtime-summary">
                            <c:param name="type" value="9"/>
                            <c:param name="start" value="${startStr}"/>
                            <c:param name="end" value="${endStr}"/>
                        </c:url>
                        <a target="_blank" href="${url}">TUNING</a>
                    </th>
                    <th class="duration-header" title="Blocked - Internal Down">PHYSICS DOWN</th>
                    <th class="duration-header" title="Physics - Tuning - Physics Down">RESEARCH</th>
                </tr>
                </thead>
                <tbody>
                    <tr>
                        <td><span><c:out value="${btm:formatDuration(accAvailability.shiftTotals.upSeconds, durationUnits)}"/></span></td>
                        <td><span><c:out value="${btm:formatDuration(accAvailability.shiftTotals.downSeconds, durationUnits)}"/></span></td>
                        <th></th>
                        <td><span><c:out value="${btm:formatDuration(dtmTotals.eventSeconds, durationUnits)}"/></span></td>
                        <td><span><c:out value="${btm:formatDuration(tuningHours.eventSeconds, durationUnits)}"/></span></td>
                        <th></th>
                        <td><span><c:out value="${btm:formatDuration(dtmTotals.eventSeconds - accAvailability.shiftTotals.downSeconds, durationUnits)}"/></span></td>
                        <td><span><c:out value="${btm:formatDuration(accAvailability.shiftTotals.upSeconds - tuningHours.eventSeconds - (dtmTotals.eventSeconds - accAvailability.shiftTotals.downSeconds), durationUnits)}"/></span></td>
                    </tr>
                </tbody>
            </table>
            <table id="dtm-btm-table"  class="data-table">
                <tbody>
                    <tr>
                        <th title="The sum of physics + internal down + 10 minutes is less than blocked event downtime; physics + internal down + 10 minutes < blocked">Low Program Check</th>
                        <td class="${downCrossCheck.isLowProgramPassed() ? '' : 'ui-state-error'}">${downCrossCheck.isLowProgramPassed() ? '✔' : 'X'}</td>
                    </tr>
                    <tr>
                    <th title="The sum of physics + 10 minutes is less than tuning event downtime; physics + 10 minutes < tuning">High Tuning Check</th>
                        <td class="${downCrossCheck.isHighTuningPassed() ? '' : 'ui-state-error'}">${downCrossCheck.isHighTuningPassed() ? '✔' : 'X'}</td>
                    </tr>
                </tbody>
            </table>
            <ul class="reason-list">
                <c:if test="${!downCrossCheck.isLowProgramPassed()}">
                    <li>
                        <c:out value="${downCrossCheck.lowProgramMessage}"/>
                    </li>
                </c:if>
                <c:if test="${!downCrossCheck.isHighTuningPassed()}">
                    <li>
                        <c:out value="${downCrossCheck.highTuningMessage}"/>
                    </li>
                </c:if>
            </ul>
            <h5>Hall Time Accounting</h5>
            <t:cross-check-summary hallAvailabilityList="${hallAvailabilityList}"
                                   expHallHourTotalsList="${expHallHourTotalsList}" modeCheck="${modeCrossCheck}"
                                   accCheck="${accCrossCheck}" hallCheck="${hallCrossCheck}"
                                   multiCheck="${multiCrossCheck}"/>
            <div id="as-of">As of <fmt:formatDate value="${now}" pattern="dd MMM yyyy HH:mm:ss"/> (<a id="reload-page"
                                                                                                      href="#">Reload</a>)
            </div>
            <p id="call-hall-note">Note: Experimenter CC Shift Hours Status above only indicates if hours needed for a CC shift report are saved (last hour of shift, shift info, and signatures are ignored).
                See Experimenter timesheet for full status.  You must call each hall with incomplete CC Shift Hours that is scheduled for
                physics and ask them to submit all but the last hour of their shift (&quot;Edit CC Hours&quot; button) and the last hour from the previous
                shift as this is needed for the Crew Chief Shift Log</p>
            <h5>Crew Chief Comments</h5>
            <div id="cross-check-comments-block">
                <div id="view-cross-check-comments"><c:out value="${crossCheckComment.crewChiefRemark}"/></div>
                <textarea id="edit-cross-check-comments" style="display: none;" maxlength="2048"><c:out
                        value="${crossCheckComment.crewChiefRemark}"/></textarea>
            </div>
            <div class="form-button-panel">
                <button id="edit-cross-comment-button" type="button"${editable ? '' : ' disabled="disabled"'}>Edit
                </button>
                <button id="save-cross-comment-button" type="button" style="display: none;">Save</button>
                <button id="cancel-cross-comment-button" type="button" style="display: none;">Cancel</button>
            </div>
            <h5>Reviewer Comments</h5>
            <div id="reviewer-comments-block">
                <div id="view-reviewer-comments"><c:out value="${crossCheckComment.reviewerRemark}"/></div>
                <textarea id="edit-reviewer-comments" style="display: none;" maxlength="2048"><c:out
                        value="${crossCheckComment.reviewerRemark}"/></textarea>
            </div>
            <div class="form-button-panel">
                <button id="edit-reviewer-comment-button" type="button"${editable ? '' : ' disabled="disabled"'}>Edit
                </button>
                <button id="save-reviewer-comment-button" type="button" style="display: none;">Save</button>
                <button id="cancel-reviewer-comment-button" type="button" style="display: none;">Cancel</button>
            </div>
        </div>
    </div>
    <div id="cross-check-details-tab" class="cross-check-details">
        <div id="cross-check-details-panel">
            <h5>Accelerator Time Check: Beam Mode</h5>
            <t:beam-mode-cross-check check="${modeCrossCheck}"/>
            <h5>Accelerator Time Check: Experimenter vs Operations</h5>
            <t:accelerator-cross-check check="${accCrossCheck}"/>
            <h5>Hall Time Check: Experimenter vs Operations</h5>
            <t:hall-cross-check check="${hallCrossCheck}"/>
            <h5>Multiplicity Time Check</h5>
            <t:multiplicity-cross-check check="${multiCrossCheck}"/>
        </div>
    </div>
    <div id="cross-check-hourly-tab" class="cross-check-details">
        <div id="cross-check-hourly-detail-panel">
            <div id="dtm-btm-hourly-accordion" class="accordion">
                <t:dtm-hourly-cross-check hourCrossCheckList="${downHourlyCrossCheckList}" dtmHourList="${dtmHourList}" btmHourList="${accAvailability.hourList}"/>
            </div>
            <div class="accordion">
                <t:hall-hourly-cross-check hall="A" hourCrossCheckList="${hallAHourCrossCheckList}" expHourList="${expHallAvailabilityList.get(0).hourList}" ccHourList="${hallAvailabilityList.get(0).hourList}"/>
            </div>
            <div class="accordion">
                <t:hall-hourly-cross-check hall="B" hourCrossCheckList="${hallBHourCrossCheckList}" expHourList="${expHallAvailabilityList.get(1).hourList}" ccHourList="${hallAvailabilityList.get(1).hourList}"/>
            </div>
            <div class="accordion">
                <t:hall-hourly-cross-check hall="C" hourCrossCheckList="${hallCHourCrossCheckList}" expHourList="${expHallAvailabilityList.get(2).hourList}" ccHourList="${hallAvailabilityList.get(2).hourList}"/>
            </div>
            <div class="accordion">
                <t:hall-hourly-cross-check hall="D" hourCrossCheckList="${hallDHourCrossCheckList}" expHourList="${expHallAvailabilityList.get(3).hourList}" ccHourList="${hallAvailabilityList.get(3).hourList}"/>
            </div>
        </div>
    </div>
</div>