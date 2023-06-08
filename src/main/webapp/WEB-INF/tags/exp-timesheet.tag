<%@tag description="Experimenter Timesheet Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<h3>Utilization and Availability</h3>
<t:exp-hourly-availability hall="${availability.hall}"
                           hourList="${availability.hourList}"
                           epicsHourList="${availability.epicsHourList}"
                           totals="${availability.shiftTotals}"
                           epicsTotals="${availability.epicsShiftTotals}"/>
<h3>Reasons not Ready</h3>
<t:exp-hourly-reasons hall="${reasons.hall}"
                      hourList="${reasons.hourList}"/>
<h3>Shift Information</h3>
<div class="form-wrapper">
    <ul id="shift-info-key-value-list" class="key-value-list">
        <li>
            <div class="li-key"><span>Leader:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.leader}"/></span>
                <input class="input" id="leader" type="text" value="${fn:escapeXml(shiftInfo.leader)}"
                       style="display: none;"/>
            </div>
        </li>
        <li>
            <div class="li-key"><span>Workers:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.workers}"/></span>
                <input class="input" id="workers" type="text" value="${fn:escapeXml(shiftInfo.workers)}"
                       style="display: none;"/>
            </div>
        </li>
        <li>
            <div class="li-key"><span>Program:</span></div>
            <div class="li-value">
                <span id="program-span"><c:out value="${shiftInfo.expShiftPurpose.name}"/></span>
                <select class="input" id="program" style="display: none;" data-purpose-id="${shiftInfo.expShiftPurpose.expHallShiftPurposeId}">
                    <option></option>
                    <optgroup label="General">
                        <c:forEach items="${nonexperimentList}" var="purpose">
                            <option value="${purpose.expHallShiftPurposeId}"${purpose.expHallShiftPurposeId eq shiftInfo.expShiftPurpose.expHallShiftPurposeId ? ' selected="selected"' : ''}>${purpose.name}</option>
                        </c:forEach>
                    </optgroup>
                    <optgroup label="Experiments">
                        <c:forEach items="${experimentList}" var="purpose">
                            <option value="${purpose.expHallShiftPurposeId}"${purpose.expHallShiftPurposeId eq shiftInfo.expShiftPurpose.expHallShiftPurposeId ? ' selected="selected"' : ''}>${purpose.name}${purpose.alias eq null ? '' : ' ('.concat(purpose.alias).concat(')')}</option>
                        </c:forEach>
                    </optgroup>
                </select>
            </div>
        </li>
        <li>
            <div class="li-key"><span>Comments:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.remark}"/></span>
                <textarea class="input" id="comments" style="display: none;">${fn:escapeXml(shiftInfo.remark)}</textarea>
            </div>
        </li>
    </ul>
    <input type="hidden" id="shift-start-hour" value="${shiftStartHourStr}"/>
    <input type="hidden" id="shift-hall" value="${availability.hall}"/>
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
<div id="status-dialog" class="dialog" title="Timesheet Status">
    <form>
        <ul class="key-value-list">
            <li>
                <div class="li-key"><span>Utilization and Availability:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.availabilityComplete}">
                            <span id="availability-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="availability-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Reasons not Ready:</span></div>
                <div class="li-value">
                    <c:choose>
                        <c:when test="${status.reasonsNotReadyComplete}">
                            <span id="reasons-not-ready-status-value" class="complete-status">Complete</span>
                        </c:when>
                        <c:otherwise>
                            <span id="reasons-not-ready-status-value" class="incomplete-status">Incomplete</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </li>
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
</div>