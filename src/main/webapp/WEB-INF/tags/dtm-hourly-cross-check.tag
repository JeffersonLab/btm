<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hourCrossCheckList" required="true" type="java.util.List" %>
<%@attribute name="dtmHourList" required="true" type="java.util.List" %>
<%@attribute name="btmHourList" required="true" type="java.util.List" %>
<h3>BTM vs DTM</h3>
<div>
    <table class="data-table stripped-table">
        <thead>
        <tr>
            <th rowspan="3"></th>
            <th colspan="2">BTM</th>
            <th rowspan="3" style="width: 5px;"></th>
            <th colspan="2">DTM</th>
            <th rowspan="3" style="width: 5px;"></th>
            <th colspan="2">COMPUTED</th>
        </tr>
        <tr>
            <th>{RESEARCH,TUNING,PHYSICS DOWN}</th>
            <th>{BLOCKED}</th>
            <th>{PHYSICS, INTERNAL DOWN}</th>
            <th>{PHYSICS}</th>
            <th>{BLOCKED - INTERNAL DOWN}</th>
            <th>{PHYSICS - TUNING - PHYSICS DOWN}</th>
        </tr>
        <tr>
            <th class="duration-header">PHYSICS</th>
            <th class="duration-header">INTERNAL DOWN</th>
            <th class="duration-header">BLOCKED</th>
            <th class="duration-header">TUNING</th>
            <th class="duration-header" title="Blocked - Internal Down">PHYSICS DOWN</th>
            <th class="duration-header" title="Physics - Tuning - Physics Down">RESEARCH</th>
        </tr>
        </thead>
        <tbody>
            <c:forEach items="${btmHourList}" var="btmHour" varStatus="status">
                <c:set value="${dtmHourList.get(status.index)}" var="dtmHour"/>
                <tr>
                    <fmt:formatDate value="${btmHour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                    <fmt:formatDate value="${btmHour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                    <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${btmHour.dayAndHour}"
                                                                                   pattern="HH"/></th>
                    <td><span><c:out value="${btm:formatDuration(btmHour.upSeconds, durationUnits)}"/></span></td>
                    <td><span><c:out value="${btm:formatDuration(btmHour.downSeconds, durationUnits)}"/></span></td>
                    <th></th>
                    <td><span><c:out value="${btm:formatDuration(dtmHour.blockedSeconds, durationUnits)}"/></span></td>
                    <td><span><c:out value="${btm:formatDuration(dtmHour.tuneSeconds, durationUnits)}"/></span></td>
                    <th></th>
                    <td><span><c:out value="${btm:formatDuration(dtmHour.blockedSeconds - btmHour.downSeconds, durationUnits)}"/></span></td>
                    <td><span><c:out value="${btm:formatDuration(btmHour.upSeconds - dtmHour.tuneSeconds - (dtmHour.blockedSeconds - btmHour.downSeconds), durationUnits)}"/></span></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <h5>Downtime Check</h5>
    <table class="data-table stripped-table">
        <thead>
        <tr>
            <th></th>
            <th title="The sum of physics + internal down + 10 minutes is less than blocked event downtime; physics + internal down + 10 minutes < blocked">
                Low Program
            </th>
            <th title="The sum of physics + 10 minutes is less than tuning event downtime; physics + 10 minutes < tuning">High Tuning</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <tr>
            <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
            <th title="${fullDate}"><fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/></th>
            <td class="${not hour.lowProgramPassed ? 'ui-state-error' : ''}">${not hour.lowProgramPassed ? 'X' : '✔'}</td>
            <td class="${not hour.highTuningPassed ? 'ui-state-error' : ''}">${not hour.highTuningPassed ? 'X' : '✔'}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <ul class="reason-list">
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <c:if test="${not hour.lowProgramPassed}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.lowProgramMessage}"/>
                </li>
            </c:if>
            <c:if test="${not hour.highTuningPassed}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highTuningMessage}"/>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>