<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="check" required="true" type="org.jlab.btm.persistence.projection.CcHallCrossCheck" %>
<table class="data-table">
    <thead>
    <tr>
        <th></th>
        <th title="Half of ABU + BANU is no more than the sum of UP, TUNE, BNR if there is at last half an hour of UP: ((ABU + BANU) / 2 ≤ UP + TUNE + BNR) OR UP ≤ 0.5">
            High ABU / Low UP
        </th>
        <th title="ABU + BANU is at least half as much as UP + TUNE + BNR if there is at least half an hour of UP: (ABU ≥ UP / 2) OR UP ≤ 0.5">
            Low ABU / High UP
        </th>
        <th title="BNR is no more than ten minutes over BANU: BNR ≤ BANU + 10min">Low BANU</th>
        <th title="BNA is no more than ten minutes over the sum of DOWN, UP, and TUNE: BNA ≤ DOWN + UP + TUNE + 10min">
            High BNA / Low DOWN + UP + TUNE
        </th>
        <th title="Experimenter OFF is no more than 10 minutes over Operations measured OFF: EXP-OFF ≤ OPS-OFF + 10min">
            High EXP OFF / Low OPS OFF
        </th>
    </tr>
    </thead>
    <tbody>
    <c:set var="halls" value="${fn:split('A,B,C,D', ',')}"/>
    <c:forEach begin="0" end="3" step="1" varStatus="status">
        <tr>
            <th>Hall ${halls[status.index]}</th>
            <td class="${check.highAbu[status.index] ? '' : 'ui-state-error'}">${check.highAbu[status.index] ? '✔' : 'X'}</td>
            <td class="${check.lowAbu[status.index] ? '' : 'ui-state-error'}">${check.lowAbu[status.index] ? '✔' : 'X'}</td>
            <td class="${check.lowBanu[status.index] ? '' : 'ui-state-error'}">${check.lowBanu[status.index] ? '✔' : 'X'}</td>
            <td class="${check.highBna[status.index] ? '' : 'ui-state-error'}">${check.highBna[status.index] ? '✔' : 'X'}</td>
            <td class="${check.highOff[status.index] ? '' : 'ui-state-error'}">${check.highOff[status.index] ? '✔' : 'X'}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<ul class="reason-list">
    <c:forEach begin="0" end="3" step="1" varStatus="status">
        <c:if test="${!check.highAbu[status.index]}">
            <li>
                <c:out value="${check.highAbuMessage[status.index]}"/>
            </li>
        </c:if>
        <c:if test="${!check.lowAbu[status.index]}">
            <li>
                <c:out value="${check.lowAbuMessage[status.index]}"/>
            </li>
        </c:if>
        <c:if test="${!check.lowBanu[status.index]}">
            <li>
                <c:out value="${check.lowBanuMessage[status.index]}"/>
            </li>
        </c:if>
        <c:if test="${!check.highBna[status.index]}">
            <li>
                <c:out value="${check.highBnaMessage[status.index]}"/>
            </li>
        </c:if>
        <c:if test="${!check.highOff[status.index]}">
            <li>
                <c:out value="${check.highOffMessage[status.index]}"/>
            </li>
        </c:if>
    </c:forEach>
</ul>