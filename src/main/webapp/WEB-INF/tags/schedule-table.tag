<%@tag description="Schedule Table Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="schedule" required="true" type="org.jlab.btm.persistence.entity.MonthlySchedule" %>
<div id="table-wrap">
    <div id="sticky-header">
        <table class="data-table stripped-table">
            <thead>
            <tr class="fixed-size-first-row">
                <th class="date"></th>
                <th class="program"></th>
                <th class="energy-pass"></th>
                <th class="count"></th>
                <th class="program"></th>
                <th class="program"></th>
                <th class="program"></th>
                <th class="program"></th>
                <th class="energy"></th>
                <th class="energy"></th>
                <th class="energy"></th>
                <th class="energy"></th>
                <th class="current"></th>
                <th class="current"></th>
                <th class="current"></th>
                <th class="current"></th>
                <th class="polarized"></th>
                <th class="polarized"></th>
                <th class="polarized"></th>
                <th class="polarized"></th>
                <th class="passes"></th>
                <th class="passes"></th>
                <th class="passes"></th>
                <th class="passes"></th>
                <th class="priority"></th>
                <th class="notes"></th>
                <th class="notes"></th>
                <th class="notes"></th>
                <th class="notes"></th>
                <th class="general"></th>
            </tr>
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
        </table>
    </div>
    <table id="schedule-table"
           class="data-table stripped-table${(pageContext.request.isUserInRole('schcom')) && schedule.publishedDate eq null ? ' editable-schedule selectable' : ''}"
           data-schedule-id="${schedule.monthlyScheduleId}">
        <thead>
        <tr class="fixed-size-first-row">
            <th class="date"></th>
            <th class="program"></th>
            <th class="energy-pass"></th>
            <th class="count"></th>
            <th class="program"></th>
            <th class="program"></th>
            <th class="program"></th>
            <th class="program"></th>
            <th class="energy"></th>
            <th class="energy"></th>
            <th class="energy"></th>
            <th class="energy"></th>
            <th class="current"></th>
            <th class="current"></th>
            <th class="current"></th>
            <th class="current"></th>
            <th class="polarized"></th>
            <th class="polarized"></th>
            <th class="polarized"></th>
            <th class="polarized"></th>
            <th class="passes"></th>
            <th class="passes"></th>
            <th class="passes"></th>
            <th class="passes"></th>
            <th class="priority"></th>
            <th class="notes"></th>
            <th class="notes"></th>
            <th class="notes"></th>
            <th class="notes"></th>
            <th class="general"></th>
        </tr>
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
        <tfoot>
        <tr>
            <td></td>
            <td>
                    <span class="write">
                        <select>
                            <option> </option>
                            <option>PHYSICS</option>
                            <option>FACDEV</option>
                            <option>STUDIES</option>
                            <option>RESTORE</option>
                            <option>ACC</option>
                            <option>DOWN</option>
                            <option>OFF</option>
                            <option>TBD</option>
                        </select>
                    </span>
            </td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td>
                    <div class="write numeric">
                        <select>
                            <option> </option>
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                        </select>
                    </div>
            </td>
            <td>
                    <span class="write">
                        <select>
                            <option> </option>
                            <c:forEach items="${hallAPurposeList}" var="purpose">
                                <option value="${purpose.expHallShiftPurposeId}"><c:out value="${purpose.name}"/><c:out
                                        value="${(purpose.alias ne null) ? ' ('.concat(purpose.alias).concat(')') : ''}"/></option>
                            </c:forEach>
                        </select>
                    </span>
            </td>
            <td>
                    <span class="write">
                        <select>
                            <option> </option>
                            <c:forEach items="${hallBPurposeList}" var="purpose">
                                <option value="${purpose.expHallShiftPurposeId}"><c:out value="${purpose.name}"/><c:out
                                        value="${(purpose.alias ne null) ? ' ('.concat(purpose.alias).concat(')') : ''}"/></option>
                            </c:forEach>
                        </select>
                    </span>
            </td>
            <td>
                    <span class="write">
                        <select>
                            <option> </option>
                            <c:forEach items="${hallCPurposeList}" var="purpose">
                                <option value="${purpose.expHallShiftPurposeId}"><c:out value="${purpose.name}"/><c:out
                                        value="${(purpose.alias ne null) ? ' ('.concat(purpose.alias).concat(')') : ''}"/></option>
                            </c:forEach>
                        </select>
                    </span>
            </td>
            <td>
                    <span class="write">
                        <select>
                            <option> </option>
                            <c:forEach items="${hallDPurposeList}" var="purpose">
                                <option value="${purpose.expHallShiftPurposeId}"><c:out value="${purpose.name}"/><c:out
                                        value="${(purpose.alias ne null) ? ' ('.concat(purpose.alias).concat(')') : ''}"/></option>
                            </c:forEach>
                        </select>
                    </span>
            </td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td><div class="write numeric"><input type="text"/></div></td>
            <td><div class="write checkmark"><input type="checkbox"/></div></td>
            <td><div class="write checkmark"><input type="checkbox"/></div></td>
            <td><div class="write checkmark"><input type="checkbox"/></div></td>
            <td><div class="write checkmark"><input type="checkbox"/></div></td>
            <td>
                    <div class="write numeric">
                        <select>
                            <option> </option>
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                            <option>5</option>
                        </select>
                    </div>
            </td>
            <td>
                    <span class="write">
                        <select>
                            <option> </option>
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                            <option>5</option>
                        </select>
                    </span>
            </td>
            <td>
                    <span class="write">
                        <select>
                            <option> </option>
                            <option>1</option>
                            <option>2</option>
                            <option>3</option>
                            <option>4</option>
                            <option>5</option>
                        </select>
                    </span>
            </td>
            <td>
                    <span class="write">
                        <select>
                            <option> </option>
                            <option value="1">0.5</option>
                            <option value="6">5.5</option>
                        </select>
                    </span>
            </td>
            <td>
                    <span class="write">
                        <input type="text" maxlength="7"/>
                    </span>
            </td>
            <td><span class="write"><input type="text" maxlength="256"/></span></td>
            <td><span class="write"><input type="text" maxlength="256"/></span></td>
            <td><span class="write"><input type="text" maxlength="256"/></span></td>
            <td><span class="write"><input type="text" maxlength="256"/></span></td>
            <td><span class="write"><input type="text" maxlength="256"/></span></td>
        </tr>
        </tfoot>
        <tbody>
        <c:forEach items="${schedule.scheduleDayList}" var="scheduleDay">
            <tr>
                <fmt:formatDate pattern="dd MMM yyyy" value="${scheduleDay.dayMonthYear}" var="fullDayFormatted"/>
                <td title="${fullDayFormatted}"><fmt:formatDate pattern="EEE dd"
                                                                value="${scheduleDay.dayMonthYear}"/></td>
                <td><span class="read"><c:out value="${scheduleDay.accProgram}"/></span></td>
                <td><div class="read numeric">${btm:formatKiloToGiga(scheduleDay.kiloVoltsPerPass)}</div></td>
                <td><div class="read numeric">${scheduleDay.minHallCount}</div></td>
                <td>
                    <div class="read" data-id="${scheduleDay.hallAProgramId}">
                        <c:choose>
                            <c:when test="${!empty purposeMap[scheduleDay.hallAProgramId].url}">
                                <a href="${purposeMap[scheduleDay.hallAProgramId].url}"><c:out
                                        value="${purposeMap[scheduleDay.hallAProgramId].name}"/></a>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${purposeMap[scheduleDay.hallAProgramId].name}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
                <td>
                    <div class="read" data-id="${scheduleDay.hallBProgramId}">
                        <c:choose>
                            <c:when test="${!empty purposeMap[scheduleDay.hallBProgramId].url}">
                                <a href="${purposeMap[scheduleDay.hallBProgramId].url}"><c:out
                                        value="${purposeMap[scheduleDay.hallBProgramId].name}"/></a>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${purposeMap[scheduleDay.hallBProgramId].name}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
                <td>
                    <div class="read" data-id="${scheduleDay.hallCProgramId}">
                        <c:choose>
                            <c:when test="${!empty purposeMap[scheduleDay.hallCProgramId].url}">
                                <a href="${purposeMap[scheduleDay.hallCProgramId].url}"><c:out
                                        value="${purposeMap[scheduleDay.hallCProgramId].name}"/></a>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${purposeMap[scheduleDay.hallCProgramId].name}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
                <td>
                    <div class="read" data-id="${scheduleDay.hallDProgramId}">
                        <c:choose>
                            <c:when test="${!empty purposeMap[scheduleDay.hallDProgramId].url}">
                                <a href="${purposeMap[scheduleDay.hallDProgramId].url}"><c:out
                                        value="${purposeMap[scheduleDay.hallDProgramId].name}"/></a>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${purposeMap[scheduleDay.hallDProgramId].name}"/>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </td>
                <td><div class="read numeric">${btm:formatKiloToGiga(scheduleDay.hallAKiloVolts)}</div></td>
                <td><div class="read numeric">${btm:formatKiloToGiga(scheduleDay.hallBKiloVolts)}</div></td>
                <td><div class="read numeric">${btm:formatKiloToGiga(scheduleDay.hallCKiloVolts)}</div></td>
                <td><div class="read numeric">${btm:formatKiloToGiga(scheduleDay.hallDKiloVolts)}</div></td>
                <td><div class="read numeric">${btm:formatNanoToMicro(scheduleDay.hallANanoAmps)}</div></td>
                <td><div class="read numeric">${btm:formatNanoToMicro(scheduleDay.hallBNanoAmps)}</div></td>
                <td><div class="read numeric">${btm:formatNanoToMicro(scheduleDay.hallCNanoAmps)}</div></td>
                <td><div class="read numeric">${btm:formatNanoToMicro(scheduleDay.hallDNanoAmps)}</div></td>
                <td><div
                        class="read checkmark">${scheduleDay.hallAPolarized == null ? '' : scheduleDay.hallAPolarized ? '✔' : ''}</div>
                </td>
                <td><div
                        class="read checkmark">${scheduleDay.hallBPolarized == null ? '' : scheduleDay.hallBPolarized ? '✔' : ''}</div>
                </td>
                <td><div
                        class="read checkmark">${scheduleDay.hallCPolarized == null ? '' : scheduleDay.hallCPolarized ? '✔' : ''}</div>
                </td>
                <td><div
                        class="read checkmark">${scheduleDay.hallDPolarized == null ? '' : scheduleDay.hallDPolarized ? '✔' : ''}</div>
                </td>
                <td><div class="read numeric">${scheduleDay.hallAPasses}</div></td>
                <td><div class="read numeric">${scheduleDay.hallBPasses}</div></td>
                <td><div class="read numeric">${scheduleDay.hallCPasses}</div></td>
                <td><div class="read numeric">${scheduleDay.hallDPasses > 0 ? scheduleDay.hallDPasses - 0.5 : ''}</div></td>
                <td><span
                        class="read">${btm:formatPriority(scheduleDay.hallAPriority, scheduleDay.hallBPriority, scheduleDay.hallCPriority, scheduleDay.hallDPriority, scheduleDay.hallAKiloVolts, scheduleDay.hallBKiloVolts, scheduleDay.hallCKiloVolts, scheduleDay.hallDKiloVolts)}</span>
                </td>
                <td>
                    <div class="read" title="${fn:escapeXml(scheduleDay.hallANote)}"><c:out
                            value="${scheduleDay.hallANote}"/></div>
                </td>
                <td>
                    <div class="read" title="${fn:escapeXml(scheduleDay.hallBNote)}"><c:out
                            value="${scheduleDay.hallBNote}"/></div>
                </td>
                <td>
                    <div class="read" title="${fn:escapeXml(scheduleDay.hallCNote)}"><c:out
                            value="${scheduleDay.hallCNote}"/></div>
                </td>
                <td>
                    <div class="read" title="${fn:escapeXml(scheduleDay.hallDNote)}"><c:out
                            value="${scheduleDay.hallDNote}"/></div>
                </td>
                <td>
                    <div class="read" title="${fn:escapeXml(scheduleDay.note)}"><c:out
                            value="${scheduleDay.note}"/></div>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>