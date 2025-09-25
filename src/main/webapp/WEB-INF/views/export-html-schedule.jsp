<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<fmt:formatDate value="${schedule.startDay}" pattern="MMMM yyyy" var="formattedMonth"/>
<c:if test="${schedule.publishedDate ne null}">
    <fmt:formatDate value="${schedule.publishedDate}" pattern="dd MMM yyyy" var="formattedPublishedDate"/>
</c:if>
<c:set var="title"
       value="${formattedMonth} Schedule, Version ${schedule.version} ${schedule.publishedDate eq null ? '(Tentative)' : '(Published '.concat(formattedPublishedDate).concat(')')}"/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><c:out value="${title}"/></title>
    <style type="text/css">
        table {
            border-collapse: collapse;
            border: 1px solid black;
        }

        th,
        td {
            border: 1px solid black;
            padding: 0.25em;
        }
    </style>
</head>
<body>
<h1><c:out value="${title}"/></h1>
<table>
    <thead>
    <tr>
        <th rowspan="3">Date</th>
        <th colspan="2">Accelerator</th>
        <th colspan="26">Hall</th>
        <th rowspan="3">General Notes</th>
    </tr>
    <tr>
        <th rowspan="2">Program</th>
        <th rowspan="2">GeV / Pass</th>
        <th rowspan="2">Min Hall Count</th>
        <th colspan="4">Program / Experiment</th>
        <th colspan="4">GeV</th>
        <th colspan="4">μA</th>
        <th colspan="4">Polarized</th>
        <th colspan="4">Passes</th>
        <th rowspan="2">Priority</th>
        <th colspan="4">Notes</th>
    </tr>
    <tr>
        <th>A</th>
        <th>B</th>
        <th>C</th>
        <th>D</th>
        <th>A</th>
        <th>B</th>
        <th>C</th>
        <th>D</th>
        <th>A</th>
        <th>B</th>
        <th>C</th>
        <th>D</th>
        <th>A</th>
        <th>B</th>
        <th>C</th>
        <th>D</th>
        <th>A</th>
        <th>B</th>
        <th>C</th>
        <th>D</th>
        <th>A</th>
        <th>B</th>
        <th>C</th>
        <th>D</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${schedule.scheduleDayList}" var="scheduleDay">
        <tr>
            <td><fmt:formatDate pattern="EEE dd" value="${scheduleDay.dayMonthYear}"/></td>
            <td><c:out value="${scheduleDay.accProgram}"/></td>
            <td>${btm:formatKiloToGiga(scheduleDay.kiloVoltsPerPass)}</td>
            <td>${scheduleDay.minHallCount}</td>
            <td><c:out value="${purposeMap[scheduleDay.hallAProgramId].name}"/></td>
            <td><c:out value="${purposeMap[scheduleDay.hallBProgramId].name}"/></td>
            <td><c:out value="${purposeMap[scheduleDay.hallCProgramId].name}"/></td>
            <td><c:out value="${purposeMap[scheduleDay.hallDProgramId].name}"/></td>
            <td>${btm:formatKiloToGiga(scheduleDay.hallAKiloVolts)}</td>
            <td>${btm:formatKiloToGiga(scheduleDay.hallBKiloVolts)}</td>
            <td>${btm:formatKiloToGiga(scheduleDay.hallCKiloVolts)}</td>
            <td>${btm:formatKiloToGiga(scheduleDay.hallDKiloVolts)}</td>
            <td>${btm:formatNanoToMicro(scheduleDay.hallANanoAmps)}</td>
            <td>${btm:formatNanoToMicro(scheduleDay.hallBNanoAmps)}</td>
            <td>${btm:formatNanoToMicro(scheduleDay.hallCNanoAmps)}</td>
            <td>${btm:formatNanoToMicro(scheduleDay.hallDNanoAmps)}</td>
            <td>${scheduleDay.hallAPolarized == null ? '' : scheduleDay.hallAPolarized ? '✔' : ''}</td>
            <td>${scheduleDay.hallBPolarized == null ? '' : scheduleDay.hallBPolarized ? '✔' : ''}</td>
            <td>${scheduleDay.hallCPolarized == null ? '' : scheduleDay.hallCPolarized ? '✔' : ''}</td>
            <td>${scheduleDay.hallDPolarized == null ? '' : scheduleDay.hallDPolarized ? '✔' : ''}</td>
            <td>${scheduleDay.hallAPasses}</td>
            <td>${scheduleDay.hallBPasses}</td>
            <td>${scheduleDay.hallCPasses}</td>
            <td>${scheduleDay.hallDPasses > 0 ? scheduleDay.hallDPasses - 0.5 : ''}</td>
            <td>${btm:formatPriority(scheduleDay.hallAPriority, scheduleDay.hallBPriority, scheduleDay.hallCPriority, scheduleDay.hallDPriority, scheduleDay.hallAKiloVolts, scheduleDay.hallBKiloVolts, scheduleDay.hallCKiloVolts, scheduleDay.hallDKiloVolts)}</td>
            <td><c:out value="${scheduleDay.hallANote}"/></td>
            <td><c:out value="${scheduleDay.hallBNote}"/></td>
            <td><c:out value="${scheduleDay.hallCNote}"/></td>
            <td><c:out value="${scheduleDay.hallDNote}"/></td>
            <td><c:out value="${scheduleDay.note}"/></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>