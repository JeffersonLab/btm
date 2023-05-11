<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="availability" required="true"
             type="org.jlab.btm.persistence.projection.AcceleratorShiftAvailability" %>
<h3>Measured</h3>
<div>
    <c:choose>
        <c:when test="${fn:length(availability.epicsHourList) > 0}">
            <table class="data-table stripped-table">
                <thead>
                <tr>
                    <th rowspan="2"></th>
                    <th rowspan="2" class="duration-header">PHYSICS MODE</th>
                    <th colspan="4">INTERNAL MODE</th>
                    <th rowspan="2" class="duration-header">SAD MODE (OFF)</th>
                </tr>
                <tr>
                    <th class="duration-header">STUDIES</th>
                    <th class="duration-header">SAD RESTORE</th>
                    <th class="duration-header">ACC</th>
                    <th class="duration-header">INTERNAL DOWN</th>
                </tr>
                </thead>
                <tfoot>
                <tr>
                    <th>Shift Total</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.upSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.studiesSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.restoreSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.accSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.downSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.sadSeconds, durationUnits)}</th>
                </tr>
                </tfoot>
                <tbody>
                <c:forEach items="${availability.epicsHourList}" var="hour">
                    <tr>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                        <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                       pattern="HH"/></th>
                        <td>${btm:formatDuration(hour.upSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.studiesSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.restoreSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.accSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.downSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.sadSeconds, durationUnits)}</td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <p>No EPICS/BOOM Data</p>
        </c:otherwise>
    </c:choose>
</div>
<h3 id="acc-reported-hourly-detail">Reported</h3>
<div>
    <div class="accordion-table-wrapper">
        <table id="acc-hourly-table" class="data-table stripped-table editable-table" data-type="acc">
            <thead>
            <tr>
                <th rowspan="2"></th>
                <th rowspan="2" class="duration-header">PHYSICS MODE</th>
                <th colspan="4" style="width: 320px;">INTERNAL MODE</th>
                <th rowspan="2" class="duration-header">SAD MODE (OFF)</th>
                <th rowspan="2">Hour Total</th>
                <th rowspan="2">Source</th>
                <th rowspan="2" style="width: 50px;"></th>
            </tr>
            <tr>
                <th class="duration-header">STUDIES</th>
                <th class="duration-header">SAD RESTORE</th>
                <th class="duration-header">ACC</th>
                <th class="duration-header">INTERNAL DOWN</th>
            </tr>
            </thead>
            <tfoot>
            <tr>
                <th>Shift Total</th>
                <th>${btm:formatDuration(availability.shiftTotals.upSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.studiesSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.restoreSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.accSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.downSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.sadSeconds, durationUnits)}</th>
                <th></th>
                <th></th>
                <th></th>
            </tr>
            </tfoot>
            <tbody>
            <c:forEach items="${availability.hourList}" var="hour">
                <tr>
                    <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                    <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                    <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                   pattern="HH"/></th>
                    <td><span><c:out value="${btm:formatDuration(hour.upSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.upSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.studiesSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.studiesSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.restoreSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.restoreSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.accSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.accSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.downSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.downSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.sadSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.sadSeconds, durationUnits)}"/></td>
                    <th></th>
                    <th class="source-td"><c:out value="${hour.source}"/></th>
                    <th>
                        <span title="Edit (Single Row)" class="ui-icon ui-icon-pencil"></span>
                        <span title="Save" class="ui-icon ui-icon-check"></span>
                        <span title="Cancel" class="ui-icon ui-icon-close"></span>
                    </th>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <div class="accordion-button-panel">
            <button type="button" id="edit-acc-button"
                    class="hour-edit-button"${editable ? '' : ' disabled="disabled"'}>Edit
            </button>
            <button type="button" id="save-acc-button" class="ajax-submit" style="display: none;">Save</button>
            <button type="button" id="cancel-acc-button" class="hour-cancel-button" style="display: none;">Cancel
            </button>
        </div>
    </div>
</div>