<%@tag description="Experimenter Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hall" required="true" type="org.jlab.smoothness.persistence.enumeration.Hall" %>
<%@attribute name="hourList" required="true" type="java.util.List" %>
<%@attribute name="epicsHourList" required="true" type="java.util.List" %>
<%@attribute name="totals" required="true" type="org.jlab.btm.persistence.projection.ExpHallHourTotals" %>
<%@attribute name="epicsTotals" required="true" type="org.jlab.btm.persistence.projection.ExpHallHourTotals" %>
<h3>Hall ${hall} Measured</h3>
<div>
    <c:choose>
        <c:when test="${fn:length(epicsHourList) > 0}">
            <table class="data-table stripped-table hall-hourly-epics-table">
                <thead>
                <tr>
                    <th rowspan="2"></th>
                    <th colspan="5">Accelerator Beam Time</th>
                    <th rowspan="2">Hour Total</th>
                    <th colspan="4">Experiment Beam Time</th>
                    <th rowspan="2">Hour Total</th>
                </tr>
                <tr>
                    <th class="duration-header">ABU</th>
                    <th class="duration-header">BANU</th>
                    <th class="duration-header">BNA</th>
                    <th class="duration-header">ACC</th>
                    <th class="duration-header">OFF</th>
                    <th class="duration-header">ER</th>
                    <th class="duration-header">PCC</th>
                    <th class="duration-header">UED</th>
                    <th class="duration-header">OFF</th>
                </tr>
                </thead>
                <tfoot>
                <tr>
                    <th>Shift Total</th>
                    <th>${btm:formatDuration(epicsTotals.abuSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.banuSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.bnaSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.accSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.offSeconds, durationUnits)}</th>
                    <th></th>
                    <th>${btm:formatDuration(epicsTotals.erSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.pccSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.uedSeconds, durationUnits)}</th>
                    <th>${btm:formatDuration(epicsTotals.offSeconds, durationUnits)}</th>
                    <th></th>
                </tr>
                </tfoot>
                <tbody>
                <c:forEach items="${epicsHourList}" var="hour">
                    <tr>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                        <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                       pattern="HH"/></th>
                        <td>${btm:formatDuration(hour.abuSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.banuSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.bnaSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.accSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.offSeconds, durationUnits)}</td>
                        <td></td>
                        <td>${btm:formatDuration(hour.erSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.pccSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.uedSeconds, durationUnits)}</td>
                        <td>${btm:formatDuration(hour.offSeconds, durationUnits)}</td>
                        <td></td>
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
        <table id="exp-hourly-table"
               class="data-table stripped-table editable-table" data-type="exp" data-hall="${hall}">
            <thead>
            <tr>
                <th rowspan="2"></th>
                <th colspan="5">Accelerator Beam Time</th>
                <th rowspan="2">Hour Total</th>
                <th colspan="4">Experiment Beam Time</th>
                <th rowspan="2">Hour Total</th>
                <th rowspan="2">Source</th>
                <th rowspan="2" style="width: 50px;"></th>
            </tr>
            <tr>
                <th class="duration-header">ABU</th>
                <th class="duration-header">BANU</th>
                <th class="duration-header">BNA</th>
                <th class="duration-header">ACC</th>
                <th class="duration-header">OFF</th>
                <th class="duration-header">ER</th>
                <th class="duration-header">PCC</th>
                <th class="duration-header">UED</th>
                <th class="duration-header">OFF</th>
            </tr>
            </thead>
            <tfoot>
            <tr>
                <th>Shift Total</th>
                <th>${btm:formatDuration(totals.abuSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.banuSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.bnaSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.accSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.offSeconds, durationUnits)}</th>
                <th></th>
                <th>${btm:formatDuration(totals.erSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.pccSeconds, durationUnits)}</th>
                <th>${btm:formatDuration(totals.uedSeconds, durationUnits)}</th>
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
                    <td>
                        <span><c:out value="${btm:formatDuration(hour.abuSeconds, durationUnits)}"/></span>
                        <input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.abuSeconds, durationUnits)}"/>
                    </td>
                    <td>
                        <span><c:out value="${btm:formatDuration(hour.banuSeconds, durationUnits)}"/></span>
                        <input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.banuSeconds, durationUnits)}"/>
                    </td>
                    <td>
                        <span><c:out value="${btm:formatDuration(hour.bnaSeconds, durationUnits)}"/></span>
                        <input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.bnaSeconds, durationUnits)}"/>
                    </td>
                    <td>
                        <span><c:out value="${btm:formatDuration(hour.accSeconds, durationUnits)}"/></span>
                        <input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.accSeconds, durationUnits)}"/>
                    </td>
                    <td>
                        <span><c:out value="${btm:formatDuration(hour.offSeconds, durationUnits)}"/></span>
                        <input
                            style="display: none;" type="text"
                            value="${btm:formatDuration(hour.offSeconds, durationUnits)}"/>
                    </td>
                    <th></th>
                    <td>
                        <span><c:out value="${btm:formatDuration(hour.erSeconds, durationUnits)}"/></span>
                        <input
                                style="display: none;" type="text"
                                value="${btm:formatDuration(hour.erSeconds, durationUnits)}"/>
                    </td>
                    <td>
                        <span><c:out value="${btm:formatDuration(hour.pccSeconds, durationUnits)}"/></span>
                        <input
                                style="display: none;" type="text"
                                value="${btm:formatDuration(hour.pccSeconds, durationUnits)}"/>
                    </td>
                    <td>
                        <span><c:out value="${btm:formatDuration(hour.uedSeconds, durationUnits)}"/></span>
                        <input
                                style="display: none;" type="text"
                                value="${btm:formatDuration(hour.uedSeconds, durationUnits)}"/>
                    </td>
                    <th class="mirror-th">
                        <span><c:out value="${btm:formatDuration(hour.offSeconds, durationUnits)}"/></span>
                    </th>
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

        <table class="data-table stripped-table editable-table">
            <thead>
                <tr>
                    <th></th>
                    <th>Comments</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${hourList}" var="hour">
                <tr>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                        <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                    <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                   pattern="HH"/></th>
                    <td>
                        <span><c:out value="${hour.remark}"/></span>
                        <input
                                style="display: none;" type="text"
                                value="${hour.remark}"/>
                    </td>
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