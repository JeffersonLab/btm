<%@tag description="CC Timesheet Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="s" uri="jlab.tags.smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<h3>Utilization and Availability</h3>
<div class="tabset" id="avail-util-tabset">
    <ul>
        <li><a href="#summary-tab">Shift Summary</a></li>
        <li id="hourly-tab-li"><a href="#hourly-tab">Hourly</a></li>
    </ul>
    <div id="summary-tab">
        <h5>Accelerator Program</h5>
        <table id="acc-summary-table" class="data-table">
            <thead>
            <tr>
                <th rowspan="2"></th>
                <th rowspan="2"
                    title="Machine program intention is to deliver beam to at least one experimental hall (even if down - choose physics down)">
                    PHYSICS
                    (Delivered + Tuning + Down)
                </th>
                <th colspan="4">INTERNAL</th>
                <th rowspan="2"
                    title="Machine is scheduled to be off for holiday, maintenance, or budget restrictions (Scheduled Accelerator Maintenance)">
                    OFF (SAM)
                </th>
            </tr>
            <tr>
                <th title="Machine is being studied/tested/developed.  This does not include opportunistic beam studies.">
                    STUDIES
                </th>
                <th title="Machine is being restored after being OFF as scheduled by NPES.  This is not to be confused with recovery after component failure, which is rolled up into down">
                    NPES RESTORE
                </th>
                <th title="Machine is changing configurations (Accelerator Configuration Change)">ACC</th>
                <th title="Machine is down due to component failures + FSDs that occurred during Studies, Restore, and ACC, but not during Physics">
                    INTERNAL DOWN
                </th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <th>Planned</th>
                <td>${btm:formatDurationLossy(accAvailability.pdShiftTotals.upSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.pdShiftTotals.studiesSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.pdShiftTotals.restoreSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.pdShiftTotals.accSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.pdShiftTotals.downSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.pdShiftTotals.sadSeconds, durationUnits)}</td>
            </tr>
            <tr>
                <th>Measured</th>
                <td>${btm:formatDurationLossy(accAvailability.epicsShiftTotals.upSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.epicsShiftTotals.studiesSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.epicsShiftTotals.restoreSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.epicsShiftTotals.accSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.epicsShiftTotals.downSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.epicsShiftTotals.sadSeconds, durationUnits)}</td>
            </tr>
            <tr>
                <th>Reported</th>
                <td>${btm:formatDurationLossy(accAvailability.shiftTotals.upSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.shiftTotals.studiesSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.shiftTotals.restoreSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.shiftTotals.accSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.shiftTotals.downSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(accAvailability.shiftTotals.sadSeconds, durationUnits)}</td>
            </tr>
            </tbody>
        </table>
        <h5>Hall Physics Progress</h5>
        <table id="hall-summary-table" class="data-table">
            <thead>
            <tr>
                <th colspan="2"></th>
                <th title="Beam available for physics (CW or TUNE mode beam, but measured as CW)">UP {ABU}
                </th>
                <th title="Hall is tuning (CW or pulse/tune mode beam, but measured as pulse/tune mode)">
                    TUNE {ABU}
                </th>
                <th title="Beam Not Requested">BNR {BANU}</th>
                <th title="Component failures + FSDs that occurred while attempting to deliver beam to a particular hall (only counted if hall is intended to receive beam)">
                    PHYSICS DOWN {BNA}
                </th>
                <th title="Scheduled Off (this includes ACC)">OFF</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${hallAvailabilityList}" var="availability">
                <tr>
                    <th rowspan="3">Hall ${availability.hall}</th>
                    <th>Planned</th>
                    <td>${btm:formatDurationLossy(availability.pdShiftTotals.upSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.pdShiftTotals.tuneSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.pdShiftTotals.bnrSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.pdShiftTotals.downSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.pdShiftTotals.offSeconds, durationUnits)}</td>
                </tr>
                <tr>
                    <th>Measured</th>
                    <td>${btm:formatDurationLossy(availability.epicsShiftTotals.upSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.epicsShiftTotals.tuneSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.epicsShiftTotals.bnrSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.epicsShiftTotals.downSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.epicsShiftTotals.offSeconds, durationUnits)}</td>
                </tr>
                <tr>
                    <th>Reported</th>
                    <td>${btm:formatDurationLossy(availability.shiftTotals.upSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.shiftTotals.tuneSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.shiftTotals.bnrSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.shiftTotals.downSeconds, durationUnits)}</td>
                    <td>${btm:formatDurationLossy(availability.shiftTotals.offSeconds, durationUnits)}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <h5>Multiplicity Physics Progress</h5>
        <table id="multi-summary-table" class="data-table">
            <thead>
            <tr>
                <th></th>
                <th>FOUR UP</th>
                <th>THREE UP</th>
                <th>TWO UP</th>
                <th>ONE UP</th>
                <th>ANY UP</th>
                <th>ALL ON UP</th>
                <th>DOWN HARD</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <th>Measured</th>
                <td>${btm:formatDurationLossy(multiplicityAvailability.epicsShiftTotals.fourHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.epicsShiftTotals.threeHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.epicsShiftTotals.twoHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.epicsShiftTotals.oneHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.epicsShiftTotals.anyHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.epicsShiftTotals.allHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.epicsShiftTotals.downHardSeconds, durationUnits)}</td>
            </tr>
            <tr>
                <th>Reported</th>
                <td>${btm:formatDurationLossy(multiplicityAvailability.shiftTotals.fourHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.shiftTotals.threeHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.shiftTotals.twoHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.shiftTotals.oneHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.shiftTotals.anyHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.shiftTotals.allHallUpSeconds, durationUnits)}</td>
                <td>${btm:formatDurationLossy(multiplicityAvailability.shiftTotals.downHardSeconds, durationUnits)}</td>
            </tr>
            </tbody>
        </table>
    </div>
    <div id="hourly-tab">
        <form>
            <h5>Accelerator Program</h5>
            <div class="accordion">
                <t:acc-hourly-availability availability="${accAvailability}"/>
            </div>
            <h5>Halls Physics Progress</h5>
            <div class="accordion">
                <c:forEach items="${hallAvailabilityList}" var="availability">
                    <t:hall-hourly-availability hall="${availability.hall}"
                                                hourList="${availability.hourList}"
                                                epicsHourList="${availability.epicsHourList}"
                                                totals="${availability.shiftTotals}"
                                                epicsTotals="${availability.epicsShiftTotals}"/>
                </c:forEach>
            </div>
            <h5>Multiplicity Physics Progress</h5>
            <div class="accordion">
                <t:multiplicity-hourly-availability availability="${multiplicityAvailability}"/>
            </div>
        </form>
    </div>
</div>
<h4>Cross Checks</h4>
<a id="comparison"></a>
<jsp:include page="/WEB-INF/includes/cross-check-panel.jsp"/>
<h3>Shift Information</h3>
<div id="shift-info-source">
                <span>Source: <span id="shift-info-source-value"><c:out
                        value="${shiftInfo.source eq null ? 'NONE' : shiftInfo.source}"/></span></span>
    <c:if test="${epicsShiftInfo ne null}">
        <a id="view-epics-shift-info-link" style="${shiftInfo.source eq 'EPICS' ? 'display: none;' : ''}"
           href="#">(View EPICS)</a>
    </c:if>
</div>
<div class="form-wrapper">
    <ul id="shift-info-key-value-list" class="key-value-list">
        <li>
            <div class="li-key"><span>Crew Chief:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.crewChief}"/></span>
                <input id="crew-chief" type="text" value="${fn:escapeXml(shiftInfo.crewChief)}"
                       style="display: none;"/>
            </div>
        </li>
        <li>
            <div class="li-key"><span>Operators:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.operators}"/></span>
                <input id="operators" type="text" value="${fn:escapeXml(shiftInfo.operators)}"
                       style="display: none;"/>
            </div>
        </li>
        <li>
            <div class="li-key"><span>Program:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.program}"/></span>
                <input id="program" type="text" value="${fn:escapeXml(shiftInfo.program)}"
                       style="display: none;"/>
            </div>
        </li>
        <li>
            <div class="li-key"><span>Program Deputy:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.programDeputy}"/></span>
                <input id="program-deputy" type="text" value="${fn:escapeXml(shiftInfo.programDeputy)}"
                       style="display: none;"/>
            </div>
        </li>
        <li>
            <div class="li-key"><span>Comments:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.remark}"/></span>
                <input id="comments" type="text" value="${fn:escapeXml(shiftInfo.remark)}"
                       style="display: none;"/>
            </div>
        </li>
    </ul>
    <input type="hidden" id="shift-start-hour" value="${shiftStartHourStr}"/>
    <div class="form-button-panel">
        <button id="edit-shift-info-button" type="button"${editable ? '' : ' disabled="disabled"'}>Edit
        </button>
        <button id="save-shift-info-button" class="ajax-submit" type="button" style="display: none;">Save
        </button>
        <button id="cancel-shift-info-button" type="button" style="display: none;">Cancel</button>
    </div>
</div>
<h3>Signatures</h3>
<c:choose>
    <c:when test="${fn:length(signatureList) > 0}">
        <table id="signature-table" class="data-table">
            <thead>
            <tr>
                <th>User</th>
                <th>Role</th>
                <th>Date</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${signatureList}" var="signature">
                <tr>
                    <td><c:out value="${s:formatUsername(signature.signedBy)}"/></td>
                    <td><c:out value="${signature.signedRole.label}"/></td>
                    <td><fmt:formatDate value="${signature.signedDate}" pattern="dd MMM yyyy HH:mm"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:when>
    <c:otherwise>
        <p>None</p>
    </c:otherwise>
</c:choose>
<button id="sign-button" type="button" class="ajax-submit"${editable ? '' : ' disabled="disabled"'}>Sign</button>
<c:if test="${not editable}">
    <span class="login-message">You must authenticate as a Crew Chief or Operability Manager to edit</span>
</c:if>
<div id="shift-info-dialog" class="dialog" title="View EPICS Shift Information">
    <section>
    <form>
        <ul class="key-value-list">
            <li>
                <div class="li-key"><span>Crew Chief:</span></div>
                <div class="li-value"><c:out value="${epicsShiftInfo.crewChief}"/></div>
            </li>
            <li>
                <div class="li-key"><span>Operators:</span></div>
                <div class="li-value"><c:out value="${epicsShiftInfo.operators}"/></div>
            </li>
            <li>
                <div class="li-key"><span>Program:</span></div>
                <div class="li-value"><c:out value="${epicsShiftInfo.program}"/></div>
            </li>
            <li>
                <div class="li-key"><span>Program Deputy:</span></div>
                <div class="li-value"><c:out value="${epicsShiftInfo.programDeputy}"/></div>
            </li>
            <li>
                <div class="li-key"><span>Comments:</span></div>
                <div class="li-value"><c:out value="${epicsShiftInfo.remark}"/></div>
            </li>
        </ul>
        <div class="dialog-button-panel">
            <button class="dialog-close-button" type="button">OK</button>
        </div>
    </form>
    </section>
</div>
<div id="status-dialog" class="dialog" title="Timesheet Status">
    <section>
    <form>
        <span id="availability-status-header">Beam Availability and Utilization:</span>
        <ul id="availability-status-list" class="key-value-list">
            <li>
                <div class="li-key"><span>Accelerator:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.acceleratorComplete}">
                            <span id="accelerator-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="accelerator-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Hall A:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.hallAComplete}">
                            <span id="hall-a-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="hall-a-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Hall B:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.hallBComplete}">
                            <span id="hall-b-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="hall-b-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Hall C:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.hallCComplete}">
                            <span id="hall-c-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="hall-c-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Hall D:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.hallDComplete}">
                            <span id="hall-d-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="hall-d-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Multiplicity:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.multiplicityComplete}">
                            <span id="multiplicity-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="multiplicity-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
        </ul>
        <ul class="key-value-list">
            <li>
                <div class="li-key"><span>Shift Information:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.shiftInfoComplete}">
                            <span id="shift-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="shift-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Signature:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.signatureComplete}">
                            <span id="signature-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="signature-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
        </ul>
        <div class="dialog-button-panel">
            <button class="dialog-close-button" type="button">OK</button>
        </div>
    </form>
    </section>
</div>