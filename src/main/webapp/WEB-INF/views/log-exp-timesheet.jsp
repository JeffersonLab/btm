<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><c:out value="${title}"/></title>
</head>
<body>
<h2>Utilization and Availability</h2>
<table border="1">
    <thead>
    <tr>
        <th rowspan="2" class="hour-header"></th>
        <th colspan="5">Accelerator Beam Time</th>
        <th rowspan="2">Hour Total</th>
        <th colspan="4">Experiment Beam Time</th>
        <th rowspan="2">Hour Total</th>
    </tr>
    <tr>
        <th class="duration-header" title="Acceptable Beam in Use">ABU</th>
        <th class="duration-header" title="Beam Available, but Not Used">BANU</th>
        <th class="duration-header" title="Beam Not Available or unacceptable">BNA</th>
        <th class="duration-header" title="Accelerator Configuration Change">ACC</th>
        <th class="duration-header">OFF</th>
        <th class="duration-header" title="Experiment Ready">ER</th>
        <th class="duration-header" title="Planned Configuration Change">PCC</th>
        <th class="duration-header" title="Unplanned Experiment Down">UED</th>
        <th class="duration-header">OFF</th>
    </tr>
    </thead>
    <tfoot>
    <tr>
        <th>Shift Total</th>
        <th>${btm:formatDuration(availability.shiftTotals.abuSeconds, durationUnits)}</th>
        <th>${btm:formatDuration(availability.shiftTotals.banuSeconds, durationUnits)}</th>
        <th>${btm:formatDuration(availability.shiftTotals.bnaSeconds, durationUnits)}</th>
        <th>${btm:formatDuration(availability.shiftTotals.accSeconds, durationUnits)}</th>
        <th>${btm:formatDuration(availability.shiftTotals.offSeconds, durationUnits)}</th>
        <th></th>
        <th>${btm:formatDuration(availability.shiftTotals.erSeconds, durationUnits)}</th>
        <th>${btm:formatDuration(availability.shiftTotals.pccSeconds, durationUnits)}</th>
        <th>${btm:formatDuration(availability.shiftTotals.uedSeconds, durationUnits)}</th>
        <th>${btm:formatDuration(availability.shiftTotals.offSeconds, durationUnits)}</th>
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
        </tr>
    </c:forEach>
    </tbody>
</table>

<table border="1">
    <thead>
    <tr>
        <th class="hour-header"></th>
        <th>Comments</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${availability.hourList}" var="hour">
        <tr>
            <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
            <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
            <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                           pattern="HH"/></th>
            <td>
                <span><c:out value="${hour.remark}"/></span>
                <textarea style="display: none;">${hour.remark}</textarea>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<h2>Reasons not Ready</h2>
<table border="1">
    <thead>
    <tr>
        <th class="hour-header"></th>
        <th>Reason</th>
        <th class="duration-header">Duration</th>
    </tr>
    </thead>
    <tfoot>
    <tr>
        <th></th>
        <th>Shift Total</th>
        <th style="text-align: right;">${btm:formatDuration(explanationSecondsTotal, durationUnits)}</th>
        <th></th>
    </tr>
    </tfoot>
    <tbody>
    <c:forEach items="${explanationList}" var="explanation">
        <c:set value="${explanation.expHour}" var="hour"/>
        <tr data-explanation-id="${explanation.expUedExplanationId}">
            <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z"
                            var="fullDate"/>
            <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
            <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate
                    value="${hour.dayAndHour}"
                    pattern="HH"/></th>
            <td><c:out value="${explanation.expReason.name}"/></td>
            <td><c:out value="${btm:formatDuration(explanation.seconds, durationUnits)}"/></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<h2>Shift Information</h2>
<table border="1">
    <tbody>
    <tr>
        <th>Leader</th>
        <td><c:out value="${shiftInfo.leader}"/></td>
    </tr>
    <tr>
        <th>Workers</th>
        <td><c:out value="${shiftInfo.workers}"/></td>
    </tr>
    <tr>
        <th>Program</th>
        <td><c:out value="${shiftInfo.expProgram.name}"/></td>
    </tr>
    <tr>
        <th>Comments</th>
        <td><c:out value="${shiftInfo.remark}"/></td>
    </tr>
    </tbody>
</table>
<p>All times are in hours</p>
<p>See: <a href="${timesheetUrl}">BTM</a></p>
</body>
</html>