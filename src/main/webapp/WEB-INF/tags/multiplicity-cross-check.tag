<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="check" required="true" type="org.jlab.btm.persistence.projection.CrewChiefMultiplicityCrossCheck" %>
<table class="data-table">
    <thead>
    <tr>
        <th></th>
        <th title="UP is not more than ten minutes over ANY UP: UP ≤ ANY UP + 10min">High UP / Low ANY UP</th>
    </tr>
    </thead>
    <tbody>
    <c:set var="halls" value="${fn:split('A,B,C,D', ',')}"/>
    <c:forEach begin="0" end="3" step="1" varStatus="status">
        <tr>
            <th>Hall ${halls[status.index]}</th>
            <td class="${check.highUp[status.index] ? '' : 'ui-state-error'}">${check.highUp[status.index] ? '✔' : 'X'}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<ul class="reason-list">
    <c:forEach begin="0" end="3" step="1" varStatus="status">
        <c:if test="${!check.highUp[status.index]}">
            <li>
                <c:out value="${check.highUpMessage[status.index]}"/>
            </li>
        </c:if>
    </c:forEach>
</ul>        