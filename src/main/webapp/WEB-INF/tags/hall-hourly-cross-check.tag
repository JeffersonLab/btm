<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hall" required="true" type="org.jlab.smoothness.persistence.enumeration.Hall" %>
<%@attribute name="hourCrossCheckList" required="true" type="java.util.List" %>
<h3>Hall ${hall}</h3>
<div>
    <h5>Beam Mode and Multiplicity Check</h5>
    <table class="data-table stripped-table">
        <thead>
        <tr>
            <th></th>
            <th title="The sum of UP + TUNE + BNR + DOWN is less than ten minutes over Physics: PHYSICS + 10min &gt; UP + TUNE + BNR + DOWN">
                Low Physics / High UP + TUNE + BNR + DOWN
            </th>
            <th title="UP is not more than ten minutes over ANY UP: UP ≤ ANY UP + 10min">High UP / Low ANY UP</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <tr>
                <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                <th title="${fullDate}"><fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/></th>
                <td class="${hour.highHallPhysics ? 'ui-state-error' : ''}">${hour.highHallPhysics ? 'X' : '✔'}</td>
                <td class="${hour.highUp ? 'ui-state-error' : ''}">${hour.highUp ? 'X' : '✔'}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <ul class="reason-list">
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <c:if test="${hour.highUp}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highUpMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.highHallPhysics}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highHallPhysicsMessage}"/>
                </li>
            </c:if>
        </c:forEach>
    </ul>
    <h5>Accelerator Time Check: Experimenter vs Operations</h5>
    <table class="data-table stripped-table">
        <thead>
        <tr>
            <th></th>
            <th title="ABU is no more than ten minutes over the sum of PHYSICS, RESTORE, and STUDIES: ABU ≤ PHYSICS + RESTORE + STUDIES (Bonus ABU during RESTORE/STUDIES) + 10min">
                High ABU / Low PHYSICS + RESTORE + STUDIES
            </th>
            <th title="BANU is no more than ten minutes over PHYSICS: BANU ≤ PHYSICS + 10min">High BANU / Low PHYSICS
            </th>
            <th title="BNA is no more than ten minutes over PHYSICS: BNA ≤ PHYSICS + 10min">High BNA / Low PHYSICS</th>
            <th title="Experimenter reported ACC is no more than ten minutes over Operations reported ACC: EXP-ACC ≤ OPS-ACC + 10min">
                High EXP ACC / Low OPS ACC
            </th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <tr>
                <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                <th title="${fullDate}"><fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/></th>
                <td class="${hour.highAbuAccCheck ? 'ui-state-error' : ''}">${hour.highAbuAccCheck ? 'X' : '✔'}</td>
                <td class="${hour.highBanuAccCheck ? 'ui-state-error' : ''}">${hour.highBanuAccCheck ? 'X' : '✔'}</td>
                <td class="${hour.highBnaAccCheck ? 'ui-state-error' : ''}">${hour.highBnaAccCheck ? 'X' : '✔'}</td>
                <td class="${hour.highAccAccCheck ? 'ui-state-error' : ''}">${hour.highAccAccCheck ? 'X' : '✔'}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <ul class="reason-list">
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <c:if test="${hour.highAbuAccCheck}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highAbuAccMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.highBanuAccCheck}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highBanuAccMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.highBnaAccCheck}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highBnaAccMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.highAccAccCheck}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highAccAccMessage}"/>
                </li>
            </c:if>
        </c:forEach>
    </ul>
    <h5>Hall Time Check: Experimenter vs Operations</h5>
    <table class="data-table stripped-table">
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
            <th title="ACC is no more than ten minutes over Operations measured OFF: ACC ≤ OFF + 10min">High EXP ACC /
                Low OPS OFF
            </th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <tr>
                <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                <th title="${fullDate}"><fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/></th>
                <td class="${hour.highAbu ? 'ui-state-error' : ''}">${hour.highAbu ? 'X' : '✔'}</td>
                <td class="${hour.lowAbu ? 'ui-state-error' : ''}">${hour.lowAbu ? 'X' : '✔'}</td>
                <td class="${hour.lowBanu ? 'ui-state-error' : ''}">${hour.lowBanu ? 'X' : '✔'}</td>
                <td class="${hour.highBna ? 'ui-state-error' : ''}">${hour.highBna ? 'X' : '✔'}</td>
                <td class="${hour.highOff ? 'ui-state-error' : ''}">${hour.highOff ? 'X' : '✔'}</td>
                <td class="${hour.highAcc ? 'ui-state-error' : ''}">${hour.highAcc ? 'X' : '✔'}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <ul class="reason-list">
        <c:forEach items="${hourCrossCheckList}" var="hour">
            <c:if test="${hour.highAbu}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highAbuMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.lowAbu}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.lowAbuMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.lowBanu}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.lowBanuMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.highBna}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highBnaMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.highOff}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highOffMessage}"/>
                </li>
            </c:if>
            <c:if test="${hour.highAcc}">
                <li>
                    <span>[<fmt:formatDate value="${hour.dayAndHour}" pattern="HH"/>]</span> <c:out
                        value="${hour.highAccMessage}"/>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>