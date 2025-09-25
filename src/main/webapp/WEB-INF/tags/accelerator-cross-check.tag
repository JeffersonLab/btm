<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="btm" uri="jlab.tags.btm" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="check" required="true" type="org.jlab.btm.persistence.projection.CcAcceleratorCrossCheck" %>
<table class="data-table">
    <thead>
    <tr>
        <th></th>
        <th title="ABU is no more than ten minutes over the sum of PHYSICS, RESTORE, and STUDIES: ABU ≤ PHYSICS + RESTORE + STUDIES (Bonus ABU during RESTORE/STUDIES) + 10min">
            High ABU / Low PHYSICS + RESTORE + STUDIES
        </th>
        <th title="BANU is no more than ten minutes over PHYSICS: BANU ≤ PHYSICS + 10min">High BANU / Low PHYSICS</th>
        <th title="BNA is no more than ten minutes over PHYSICS: BNA ≤ PHYSICS + 10min">High BNA / Low PHYSICS</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="halls" value="${fn:split('A,B,C,D', ',')}"/>
    <c:forEach begin="0" end="3" step="1" varStatus="status">
        <tr>
            <th>Hall ${halls[status.index]}</th>
            <td class="${check.highAbu[status.index] ? '' : 'ui-state-error'}">${check.highAbu[status.index] ? '✔' : 'X'}</td>
            <td class="${check.highBanu[status.index] ? '' : 'ui-state-error'}">${check.highBanu[status.index] ? '✔' : 'X'}</td>
            <td class="${check.highBna[status.index] ? '' : 'ui-state-error'}">${check.highBna[status.index] ? '✔' : 'X'}</td>
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
        <c:if test="${!check.highBanu[status.index]}">
            <li>
                <c:out value="${check.highBanuMessage[status.index]}"/>
            </li>
        </c:if>
        <c:if test="${!check.highBna[status.index]}">
            <li>
                <c:out value="${check.highBnaMessage[status.index]}"/>
            </li>
        </c:if>
    </c:forEach>
</ul>