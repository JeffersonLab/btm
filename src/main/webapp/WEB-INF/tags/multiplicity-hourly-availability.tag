<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="availability" required="true"
             type="org.jlab.btm.persistence.projection.MultiplicityShiftAvailability" %>
<h3>Measured</h3>
<div>
    <c:choose>
        <c:when test="${fn:length(availability.epicsHourList) > 0}">
            <table class="data-table stripped-table">
                <thead>
                <tr>
                    <th></th>
                    <th class="duration-header">FOUR UP</th>
                    <th class="duration-header">THREE UP</th>
                    <th class="duration-header">TWO UP</th>
                    <th class="duration-header">ONE UP</th>
                    <th class="duration-header">ANY UP</th>
                    <th class="duration-header">ALL ON UP</th>
                    <th class="duration-header">DOWN HARD</th>
                </tr>
                </thead>
                <tfoot>
                <tr>
                    <th>Shift Total</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.fourHallUpSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.threeHallUpSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.twoHallUpSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.oneHallUpSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.anyHallUpSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.allHallUpSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(availability.epicsShiftTotals.downHardSeconds, durationUnits)}</th>
                </tr>
                </tfoot>
                <tbody>
                <c:forEach items="${availability.epicsHourList}" var="hour">
                    <tr>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                        <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                       pattern="HH"/></th>
                        <td>${btm:formatDuration(hour.fourHallUpSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.threeHallUpSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.twoHallUpSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.oneHallUpSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.anyHallUpSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.allHallUpSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.downHardSeconds, durationUnits)}</td>
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
<h3 id="multi-reported-hourly-detail">Reported</h3>
<div>
    <div class="accordion-table-wrapper">
        <table id="multiplicity-hourly-table" class="data-table stripped-table editable-table" data-type="multi">
            <thead>
            <tr>
                <th></th>
                <th class="duration-header">FOUR UP</th>
                <th class="duration-header">THREE UP</th>
                <th class="duration-header">TWO UP</th>
                <th class="duration-header">ONE UP</th>
                <th class="duration-header">ANY UP</th>
                <th class="duration-header">ALL ON UP</th>
                <th class="duration-header">DOWN HARD</th>
                <th>Source</th>
                <th style="width: 50px;"></th>
            </tr>
            </thead>
            <tfoot>
            <tr>
                <th>Shift Total</th>
                <th>${btm:formatDuration(availability.shiftTotals.fourHallUpSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.threeHallUpSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.twoHallUpSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.oneHallUpSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.anyHallUpSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.allHallUpSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(availability.shiftTotals.downHardSeconds, durationUnits)}</th>
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
                    <td><span><c:out value="${btm:formatDuration(hour.fourHallUpSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.fourHallUpSeconds, durationUnits)}"/></td>
                    <td><span><c:out
                            value="${btm:formatDuration(hour.threeHallUpSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.threeHallUpSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.twoHallUpSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.twoHallUpSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.oneHallUpSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.oneHallUpSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.anyHallUpSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.anyHallUpSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.allHallUpSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.allHallUpSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.downHardSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.downHardSeconds, durationUnits)}"/></td>
                    <th class="source-td"><c:out value="${hour.source.label}"/></th>
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
            <button type="button" class="hour-edit-button"${editable ? '' : ' disabled="disabled"'}>Edit</button>
            <button type="button" id="save-multiplicity-button" class="ajax-submit" style="display: none;">Save</button>
            <button type="button" class="hour-cancel-button" style="display: none;">Cancel</button>
        </div>
    </div>
</div>