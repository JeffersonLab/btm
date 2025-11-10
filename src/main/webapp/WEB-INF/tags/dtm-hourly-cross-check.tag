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
            <th>COMPUTED</th>
        </tr>
        <tr>
            <th>{DELIVERED,BLOCKED,TUNING}</th>
            <th>{BLOCKED}</th>
            <th>{PHYSICS, INTERNAL DOWN}</th>
            <th>{PHYSICS}</th>
            <th>{PHYSICS-TUNING}</th>
        </tr>
        <tr>
            <th class="duration-header">PHYSICS</th>
            <th class="duration-header">INTERNAL DOWN</th>
            <th class="duration-header">BLOCKED</th>
            <th class="duration-header">TUNING</th>
            <th class="duration-header">DELIVERED</th>
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
                    <td><span><c:out value="${btm:formatDuration(btmHour.upSeconds - dtmHour.tuneSeconds, durationUnits)}"/></span></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <ul class="reason-list">
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <c:if test="${hour.lowProgramPassed}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.lowProgramMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.highTuningPassed}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highTuningMessage}"/>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>