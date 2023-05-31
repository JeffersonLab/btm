<%@tag description="Experimenter Timesheet Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<h3>Utilization and Availability</h3>
<div class="accordion">
    <form>
        <t:exp-hourly-availability hall="${availability.hall}"
                                   hourList="${availability.hourList}"
                                   epicsHourList="${availability.epicsHourList}"
                                   totals="${availability.shiftTotals}"
                                   epicsTotals="${availability.epicsShiftTotals}"/>
    </form>
</div>
<h3>Reasons not Ready</h3>
<p>TODO</p>
<h3>Shift Information</h3>
<div class="form-wrapper">
    <ul id="shift-info-key-value-list" class="key-value-list">
        <li>
            <div class="li-key"><span>Leader:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.leader}"/></span>
                <input id="leader" type="text" value="${fn:escapeXml(shiftInfo.leader)}"
                       style="display: none;"/>
            </div>
        </li>
        <li>
            <div class="li-key"><span>Workers:</span></div>
            <div class="li-value">
                <span><c:out value="${shiftInfo.workers}"/></span>
                <input id="workers" type="text" value="${fn:escapeXml(shiftInfo.workers)}"
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