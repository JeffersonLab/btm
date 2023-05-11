<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hall" required="true" type="org.jlab.smoothness.persistence.enumeration.Hall" %>
<%@attribute name="hourList" required="true" type="java.util.List" %>
<%@attribute name="epicsHourList" required="true" type="java.util.List" %>
<%@attribute name="totals" required="true" type="org.jlab.btm.persistence.projection.OpHallShiftTotals" %>
<%@attribute name="epicsTotals" required="true" type="org.jlab.btm.persistence.projection.OpHallShiftTotals" %>
<h3>Hall ${hall} Measured</h3>
<div>
    <c:choose>
        <c:when test="${fn:length(epicsHourList) > 0}">
            <table class="data-table stripped-table hall-hourly-epics-table">
                <thead>
                <tr>
                    <th></th>
                    <th>UP {ABU}</th>
                    <th>TUNE {ABU}</th>
                    <th>BNR {BANU}</th>
                    <th>PHYSICS DOWN {BNA}</th>
                    <th>OFF {ACC,OFF}</th>
                </tr>
                </thead>
                <tfoot>
                <tr>
                    <th>Shift Total</th>
                    <th>${btm:formatDuration(epicsTotals.upSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.tuneSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.bnrSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.downSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.offSeconds, durationUnits)}</th>
                </tr>
                </tfoot>
                <tbody>
                <c:forEach items="${epicsHourList}" var="hour">
                    <tr>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                        <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                       pattern="HH"/></th>
                        <td>${btm:formatDuration(hour.upSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.tuneSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.bnrSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.downSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.offSeconds, durationUnits)}</td>
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
<h3 id="hall-${hall}-reported-hourly-detail" class="initially-open-header">Hall ${hall} Reported</h3>
<div>
    <div class="accordion-table-wrapper">
        <table id="hall-${fn:toLowerCase(hall)}-hourly-table"
               class="data-table stripped-table hall-hourly-table editable-table" data-type="hall" data-hall="${hall}">
            <thead>
            <tr>
                <th></th>
                <th class="duration-header">UP {ABU}</th>
                <th class="duration-header">TUNE {ABU}</th>
                <th class="duration-header">BNR {BANU}</th>
                <th class="duration-header">PHYSICS DOWN {BNA}</th>
                <th class="duration-header">OFF {ACC,OFF}</th>
                <th>Hour Total</th>
                <th>Source</th>
                <th style="width: 50px;"></th>
            </tr>
            </thead>
            <tfoot>
            <tr>
                <th>Shift Total</th>
                <th>${btm:formatDuration(totals.upSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.tuneSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.bnrSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.downSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.offSeconds, durationUnits)}</th>
                <th></th>
                <th></th>
                <th></th>
            </tr>
            </tfoot>
            <tbody>
            <c:forEach items="${hourList}" var="hour">
                <tr>
                    <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                    <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                    <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                   pattern="HH"/></th>
                    <td><span><c:out value="${btm:formatDuration(hour.upSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.upSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.tuneSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.tuneSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.bnrSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.bnrSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.downSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.downSeconds, durationUnits)}"/></td>
                    <td><span><c:out value="${btm:formatDuration(hour.offSeconds, durationUnits)}"/></span><input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.offSeconds, durationUnits)}"/></td>
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
            <button type="button" class="hour-edit-button"${editable ? '' : ' disabled="disabled"'}>Edit</button>
            <button type="button" class="save-hall-button ajax-submit" style="display: none;"
                    id="hall-${fn:toLowerCase(hall)}-save-button">Save
            </button>
            <button type="button" class="hour-cancel-button" style="display: none;">Cancel</button>
        </div>
    </div>
</div>