<%@tag description="Schedule Table Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="calendar" required="true" type="org.jlab.btm.presentation.util.WallCalendar" %>
<%@attribute name="monthHeader" required="true" %>
<h3><c:out value="${monthHeader}"/></h3>
<div class="key">
    <div class="priority-hall-color"></div><div class="key-text">Priority</div>
    <img alt="Polarized" width="16" height="16" src="${pageContext.request.contextPath}/resources/img/Polarized.png"/><div class="key-text">Polarized</div>
    <div class="key-text detail-key shift-detail">GeV / μA / Pass</div>
</div>
<table id="calendar-table" class="data-table">
    <thead>
    <tr>
        <th>&nbsp;</th>
        <th class="weekday-header">Sun</th>
        <th class="weekday-header">Mon</th>
        <th class="weekday-header">Tue</th>
        <th class="weekday-header">Wed</th>
        <th class="weekday-header">Thu</th>
        <th class="weekday-header">Fri</th>
        <th class="weekday-header">Sat</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${calendar.weeks}" var="week">
        <tr>
            <th class="left-header-cell">
                <table class="left-header-table">
                    <tbody>
                    <tr>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td>
                            <div class="top-section">Acc.</div>
                            <div class="shift-detail"></div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <div class="top-section">Hall A</div>
                            <div class="shift-detail"></div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <div class="top-section">Hall B</div>
                            <div class="shift-detail"></div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <div class="top-section">Hall C</div>
                            <div class="shift-detail"></div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <div class="top-section">Hall D</div>
                            <div class="shift-detail"></div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </th>
            <c:forEach items="${week}" var="day">
                <c:set var="scheduleDay" value="${calendar.getNote(day)}"/>
                <td class="detail-holder-cell">
                    <table class="day-detail-table">
                        <tbody>
                        <tr>
                            <td><span${btm:isToday(scheduleDay.dayMonthYear) ? ' class="today"' : ''}><fmt:formatDate value="${scheduleDay.dayMonthYear}" pattern="dd"/></span></td>
                        </tr>
                        <tr>
                            <td>
                                <div class="top-section"><c:out value="${scheduleDay.accProgram}"/></div>
                                <div class="shift-detail"></div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div class="top-section${scheduleDay.hallAPriority eq 0 ? ' priority-hall' : ''}" title="${purposeMap[scheduleDay.hallAProgramId].name}">
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
                                <div class="shift-detail">
                                <c:if test="${scheduleDay.hallAKiloVolts > 0}">
                                    <c:out value="${btm:formatKiloToGiga(scheduleDay.hallAKiloVolts)}"/> /
                                    <c:out value="${btm:formatNanoToMicro(scheduleDay.hallANanoAmps)}"/> /
                                    <c:out value="${scheduleDay.hallAPasses}"/>
                                </c:if>
                                <c:if test="${scheduleDay.hallAPolarized}">
                                    <img alt="Polarized" width="16" height="16" src="${pageContext.request.contextPath}/resources/img/Polarized.png"/>
                                </c:if>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div class="top-section${scheduleDay.hallBPriority eq 0 ? ' priority-hall' : ''}" title="${purposeMap[scheduleDay.hallBProgramId].name}">
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
                                <div class="shift-detail">
                                <c:if test="${scheduleDay.hallBKiloVolts > 0}">
                                    <c:out value="${btm:formatKiloToGiga(scheduleDay.hallBKiloVolts)}"/> /
                                    <c:out value="${btm:formatNanoToMicro(scheduleDay.hallBNanoAmps)}"/> /
                                    <c:out value="${scheduleDay.hallBPasses}"/>
                                </c:if>
                                <c:if test="${scheduleDay.hallBPolarized}">
                                    <img alt="Polarized" width="16" height="16" src="${pageContext.request.contextPath}/resources/img/Polarized.png"/>
                                </c:if>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div class="top-section${scheduleDay.hallCPriority eq 0 ? ' priority-hall' : ''}" title="${purposeMap[scheduleDay.hallCProgramId].name}">
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
                                <div class="shift-detail">
                                <c:if test="${scheduleDay.hallCKiloVolts > 0}">
                                    <c:out value="${btm:formatKiloToGiga(scheduleDay.hallCKiloVolts)}"/> /
                                    <c:out value="${btm:formatNanoToMicro(scheduleDay.hallCNanoAmps)}"/> /
                                    <c:out value="${scheduleDay.hallCPasses}"/>
                                </c:if>
                                <c:if test="${scheduleDay.hallCPolarized}">
                                    <img alt="Polarized" width="16" height="16" src="${pageContext.request.contextPath}/resources/img/Polarized.png"/>
                                </c:if>
                            </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div class="top-section${scheduleDay.hallDPriority eq 0 ? ' priority-hall' : ''}" title="${purposeMap[scheduleDay.hallDProgramId].name}">
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
                                <div class="shift-detail">
                                <c:if test="${scheduleDay.hallDKiloVolts > 0}">
                                    <c:out value="${btm:formatKiloToGiga(scheduleDay.hallDKiloVolts)}"/> /
                                    <c:out value="${btm:formatNanoToMicro(scheduleDay.hallDNanoAmps)}"/> /
                                    <c:out value="${scheduleDay.hallDPasses eq null ? '' : scheduleDay.hallDPasses - 0.5}"/>
                                </c:if>
                                <c:if test="${scheduleDay.hallDPolarized}">
                                    <img alt="Polarized" width="16" height="16" src="${pageContext.request.contextPath}/resources/img/Polarized.png"/>
                                </c:if>
                            </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </c:forEach>
        </tr>
    </c:forEach>
    </tbody>
</table>