<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><c:out value="${title}"/></title>
</head>
<body>
<h2>Accelerator</h2>
<table border="1">
    <thead>
    <tr>
        <th></th>
        <th title="Machine is capable of beam delivery">PHYSICS</th>
        <th title="Beam Studies / machine development">STUDIES</th>
        <th title="Machine restoration after system failure">RESTORE</th>
        <th title="Accelerator Configuration Change">ACC</th>
        <th title="System Failures + FSDs">DOWN</th>
        <th title="Scheduled Accelerator Maintenance">OFF (SAM)</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <th>Planned</th>
        <td>${btm:formatDuration(accAvailability.pdShiftTotals.upSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.pdShiftTotals.studiesSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.pdShiftTotals.restoreSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.pdShiftTotals.accSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.pdShiftTotals.downSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.pdShiftTotals.sadSeconds, durationUnits)}</td>
    </tr>
    <tr>
        <th>Measured</th>
        <td>${btm:formatDuration(accAvailability.epicsShiftTotals.upSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.epicsShiftTotals.studiesSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.epicsShiftTotals.restoreSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.epicsShiftTotals.accSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.epicsShiftTotals.downSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.epicsShiftTotals.sadSeconds, durationUnits)}</td>
    </tr>
    <tr>
        <th>Reported</th>
        <td>${btm:formatDuration(accAvailability.shiftTotals.upSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.shiftTotals.studiesSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.shiftTotals.restoreSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.shiftTotals.accSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.shiftTotals.downSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(accAvailability.shiftTotals.sadSeconds, durationUnits)}</td>
    </tr>
    </tbody>
</table>
<h2>Halls</h2>
<table border="1">
    <thead>
    <tr>
        <th colspan="2"></th>
        <th title="Beam available for physics (CW or TUNE mode beam)">UP</th>
        <th title="Hall is tuning (CW or TUNE mode beam)">TUNE</th>
        <th title="Beam Not Requested">BNR</th>
        <th title="System Failures + FSDs">DOWN</th>
        <th title="Scheduled Off">OFF</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${hallAvailabilityList}" var="availability">
        <tr>
            <th style="border-bottom: 1px solid lightgray;" rowspan="3">Hall ${availability.hall}</th>
            <th>Planned</th>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
        </tr>
        <tr>
            <th>Measured</th>
            <td>${btm:formatDuration(availability.epicsShiftTotals.upSeconds, durationUnits)}</td>
            <td>${btm:formatDuration(availability.epicsShiftTotals.tuneSeconds, durationUnits)}</td>
            <td>${btm:formatDuration(availability.epicsShiftTotals.bnrSeconds, durationUnits)}</td>
            <td>${btm:formatDuration(availability.epicsShiftTotals.downSeconds, durationUnits)}</td>
            <td>${btm:formatDuration(availability.epicsShiftTotals.offSeconds, durationUnits)}</td>
        </tr>
        <tr>
            <th style="border-bottom: 1px solid lightgray;">Reported</th>
            <td style="border-bottom: 1px solid lightgray;">${btm:formatDuration(availability.shiftTotals.upSeconds, durationUnits)}</td>
            <td style="border-bottom: 1px solid lightgray;">${btm:formatDuration(availability.shiftTotals.tuneSeconds, durationUnits)}</td>
            <td style="border-bottom: 1px solid lightgray;">${btm:formatDuration(availability.shiftTotals.bnrSeconds, durationUnits)}</td>
            <td style="border-bottom: 1px solid lightgray;">${btm:formatDuration(availability.shiftTotals.downSeconds, durationUnits)}</td>
            <td style="border-bottom: 1px solid lightgray;">${btm:formatDuration(availability.shiftTotals.offSeconds, durationUnits)}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<h2>Multiplicity</h2>
<table border="1">
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
        <td>${btm:formatDuration(multiplicityAvailability.epicsShiftTotals.fourHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.epicsShiftTotals.threeHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.epicsShiftTotals.twoHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.epicsShiftTotals.oneHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.epicsShiftTotals.anyHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.epicsShiftTotals.allHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.epicsShiftTotals.downHardSeconds, durationUnits)}</td>
    </tr>
    <tr>
        <th>Reported</th>
        <td>${btm:formatDuration(multiplicityAvailability.shiftTotals.fourHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.shiftTotals.threeHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.shiftTotals.twoHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.shiftTotals.oneHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.shiftTotals.anyHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.shiftTotals.allHallUpSeconds, durationUnits)}</td>
        <td>${btm:formatDuration(multiplicityAvailability.shiftTotals.downHardSeconds, durationUnits)}</td>
    </tr>
    </tbody>
</table>
<h2>Shift Information</h2>
<table border="1">
    <tbody>
    <tr>
        <th>Crew Chief</th>
        <td><c:out value="${shiftInfo.crewChief}"/></td>
    </tr>
    <tr>
        <th>Operators</th>
        <td><c:out value="${shiftInfo.operators}"/></td>
    </tr>
    <tr>
        <th>Program</th>
        <td><c:out value="${shiftInfo.program}"/></td>
    </tr>
    <tr>
        <th>Program Deputy</th>
        <td><c:out value="${shiftInfo.programDeputy}"/></td>
    </tr>
    <tr>
        <th>Comments</th>
        <td><c:out value="${shiftInfo.remark}"/></td>
    </tr>
    </tbody>
</table>
<p>All times are in hours</p>
<p>See Also: <a href="${timesheetUrl}">Hourly Detail</a></p>
</body>
</html>