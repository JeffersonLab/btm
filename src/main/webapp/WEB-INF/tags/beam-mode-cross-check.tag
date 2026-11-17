<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="check" required="true" type="org.jlab.btm.persistence.projection.CcBeamModeCrossCheck" %>
<table class="data-table">
    <thead>
    <tr>
        <th></th>
        <th title="The hall's UP + TUNE + BNR + DOWN is no more than ten minutes over the accelerator's PHYSICS: UP + TUNE + BNR + DOWN ≤ PHYSICS + 10min">
            Low Physics / High UP + TUNE + BNR + DOWN
        </th>
    </tr>
    </thead>
    <tbody>
    <c:set var="halls" value="${fn:split('A,B,C,D', ',')}"/>
    <c:forEach begin="0" end="3" step="1" varStatus="status">
        <tr>
            <th>Hall ${halls[status.index]}</th>
            <td class="${check.highHallPhysics[status.index] ? '' : 'ui-state-error'}">${check.highHallPhysics[status.index] ? '✔' : 'X'}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<ul class="reason-list">
    <c:forEach begin="0" end="3" step="1" varStatus="status">
        <c:if test="${!check.highHallPhysics[status.index]}">
            <li>
                <c:out value="${check.highHallPhysicsMessage[status.index]}"/>
            </li>
        </c:if>
    </c:forEach>
</ul>        