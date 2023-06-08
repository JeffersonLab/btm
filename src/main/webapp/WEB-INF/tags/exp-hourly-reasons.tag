<%@tag description="Experimenter Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hall" required="true" type="org.jlab.smoothness.persistence.enumeration.Hall" %>
<%@attribute name="hourList" required="true" type="java.util.List" %>
<div id="exp-reasons-panel">
    <form>
        <c:choose>
            <c:when test="${fn:length(hourList) > 0}">
                <table class="data-table stripped-table">
                    <thead>
                    <tr>
                        <th class="hour-header"></th>
                        <th>Reason</th>
                        <th class="duration-header">Duration</th>
                        <th style="width: 50px;"></th>
                    </tr>
                    </thead>
                    <tfoot>
                    <tr>
                        <th></th>
                        <th>Shift Total</th>
                        <th style="text-align: right;">${btm:formatDuration(explanationSecondsTotal, durationUnits)}</th>
                        <th></th>
                    </tr>
                    </tfoot>
                    <tbody>
                    <c:forEach items="${hourList}" var="explanation">
                        <c:set value="${explanation.expHour}" var="hour"/>
                        <tr data-explanation-id="${explanation.expHourReasonTimeId}">
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z"
                                            var="fullDate"/>
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                            <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate
                                    value="${hour.dayAndHour}"
                                    pattern="HH"/></th>
                            <td><c:out value="${explanation.expReason.name}"/></td>
                            <td><c:out value="${btm:formatDuration(explanation.seconds, durationUnits)}"/></td>
                            <th><span title="Delete" class="ui-icon ui-icon-minusthick"></span></th>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p>None</p>
            </c:otherwise>
        </c:choose>
        <h4>UED Reason Discrepancies</h4>
        <c:choose>
            <c:when test="${fn:length(status.reasonDiscrepancyList) > 0}">
                <table class="data-table stripped-table">
                    <thead>
                        <tr>
                            <th class="hour-header"></th>
                            <th>UED</th>
                            <th>Reason Total</th>
                            <th>Difference</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${status.reasonDiscrepancyList}" var="hour">
                        <tr class="ui-state-error">
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z"
                                            var="fullDate"/>
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                            <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate
                                    value="${hour.dayAndHour}"
                                    pattern="HH"/></th>
                            <td>${btm:formatDuration(hour.uedSeconds, durationUnits)}</td>
                            <td>${btm:formatDuration(hour.reasonTotalSeconds, durationUnits)}</td>
                            <td>${btm:formatDuration(hour.differenceSeconds, durationUnits)}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                None
            </c:otherwise>
        </c:choose>
        <div class="form-button-panel">
            <button id="add-reason-button" type="button"${status.reasonDiscrepancyList.size() > 0 ? '' : ' disabled="disabled"'}>Add</button>
        </div>
    </form>
</div>
<div id="reason-dialog" class="dialog" title="Add Reason Not Ready">
    <form>
        <ul class="key-value-list">
            <li>
                <div class="li-key"><span>Hour:</span></div>
                <div class="li-value">
                    <select id="reason-hour">
                        <option></option>
                        <c:forEach items="${status.reasonDiscrepancyList}" var="discrepancy">
                            <fmt:formatDate pattern="yyyy-MM-dd HH" value="${discrepancy.dayAndHour}" var="dayAndHour"/>
                            <fmt:formatDate pattern="HH" value="${discrepancy.dayAndHour}" var="hour"/>
                            <option value="${dayAndHour}">${hour}</option>
                        </c:forEach>
                    </select>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Reason:</span></div>
                <div class="li-value">
                    <select id="reason">
                        <option></option>
                        <c:forEach items="${reasonList}" var="reason">
                            <option value="${reason.expReasonId}">${reason.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </li>
            <li>
                <div class="li-key"><span>Duration:</span></div>
                <div class="li-value">
                    <input id="reason-duration" type="text"/>
                </div>
            </li>
        </ul>
        <div class="dialog-button-panel">
            <button id="save-reason-button" class="dialog-save-button" type="button">Save</button>
        </div>
    </form>
</div>