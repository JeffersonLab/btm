<%@tag description="Cross Check Summary" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hallAvailabilityList" required="true" type="java.util.List" %>
<%@attribute name="expHallHourTotalsList" required="true" type="java.util.List" %>
<%@attribute name="modeCheck" required="true" type="org.jlab.btm.persistence.projection.CcBeamModeCrossCheck" %>
<%@attribute name="accCheck" required="true"
             type="org.jlab.btm.persistence.projection.CcAcceleratorCrossCheck" %>
<%@attribute name="hallCheck" required="true" type="org.jlab.btm.persistence.projection.CcHallCrossCheck" %>
<%@attribute name="multiCheck" required="true"
             type="org.jlab.btm.persistence.projection.CcMultiplicityCrossCheck" %>
<fmt:formatDate var="startFmt" value="${startHour}" pattern="dd-MMM-yyyy HH:mm"/>
<fmt:formatDate var="endFmt" value="${startOfNextShift}" pattern="dd-MMM-yyyy HH:mm"/>
<table id="comparison-table" class="data-table" data-start="${startFmt}" data-end="${endFmt}">
    <thead>
    <tr>
        <th></th>
        <th colspan="2">Timesheet Status</th>
        <th rowspan="2"></th>
        <th colspan="5">Experimenter Hall Time Accounting</th>
        <th rowspan="2"></th>
        <th rowspan="2">Cross Check Status</th>
    </tr>
    <tr>
        <th></th>
        <th class="duration-header">Experimenter</th>
        <th class="duration-header">Crew Chief</th>
        <th title="Acceptable Beam Used">ABU</th>
        <th title="Beam Available, but Not in Use">BANU</th>
        <th title="Beam Not Acceptable (or not available)">BNA</th>
        <th title="Accelerator Configuration Change">ACC</th>
        <th title="The hall is not expected to be receiving beam">OFF</th>
    </tr>
    </thead>
    <tbody>
    <fmt:formatDate pattern="dd-MMM-yyyy" value="${day}" var="expDate"/>
    <c:forEach items="${hallAvailabilityList}" var="availability" varStatus="status">
        <c:set var="expHallShift" value="${expHallHourTotalsList.get(status.index)}"/>
        <tr>
            <th>Hall ${availability.hall}</th>
            <c:url var="expUrl"
                   value="/timesheet/e${fn:toLowerCase(availability.hall)}/${expDate}/${fn:toLowerCase(shift)}/${fn:toLowerCase(durationUnits)}"/>
            <c:choose>
                <c:when test="${expHallShift.hourCount eq hoursInShift}">
                    <td><a target="_blank" href="${expUrl}">Complete<span class="ui-icon ui-icon-extlink"></span></a>
                    </td>
                </c:when>
                <c:otherwise>
                    <td title="${expHallShift.hourCount} saved hours" class="ui-state-highlight"><a target="_blank"
                                                                                                    href="${expUrl}">Incomplete<span
                            class="ui-icon ui-icon-extlink"></span></a></td>
                </c:otherwise>
            </c:choose>
            <c:choose>
                <c:when test="${fn:length(hallAvailabilityList.get(status.index).dbHourList) eq hoursInShift}">
                    <td>Complete</td>
                </c:when>
                <c:otherwise>
                    <td class="ui-state-highlight">Incomplete</td>
                </c:otherwise>
            </c:choose>
            <th></th>
            <td class="exp-td">${btm:formatDurationLossy(expHallHourTotalsList.get(status.index).abuSeconds, durationUnits)}</td>
            <td class="exp-td">${btm:formatDurationLossy(expHallHourTotalsList.get(status.index).banuSeconds, durationUnits)}</td>
            <td class="exp-td">${btm:formatDurationLossy(expHallHourTotalsList.get(status.index).bnaSeconds, durationUnits)}</td>
            <td class="exp-td">${btm:formatDurationLossy(expHallHourTotalsList.get(status.index).accSeconds, durationUnits)}</td>
            <td class="exp-td">${btm:formatDurationLossy(expHallHourTotalsList.get(status.index).offSeconds, durationUnits)}</td>
            <th></th>
            <td class="${modeCheck.hallPassed[status.index] && accCheck.hallPassed[status.index] && hallCheck.hallPassed[status.index] && multiCheck.hallPassed[status.index] ? '' : 'ui-state-error'}">${modeCheck.hallPassed[status.index] && accCheck.hallPassed[status.index] && hallCheck.hallPassed[status.index] && multiCheck.hallPassed[status.index] ? '✔' : 'X'}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>