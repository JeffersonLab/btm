<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%> 
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions"%>
<c:set var="title" value="ExpHour Audit"/>
<t:report-page title="${title}">
    <jsp:attribute name="stylesheets">       
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>        
    <jsp:body>
        <section>
            <s:filter-flyout-widget requiredMessage="true">
                <form class="filter-form" method="get" action="exp-hour">
                    <fieldset>
                        <legend>Filter</legend>
                        <ul class="key-value-list">                      
                            <li>
                                <div class="li-key">
                                    <label class="required-field" for="entity-id">Entity ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="entity-id" name="entityId" value="${fn:escapeXml(param.entityId)}"/>
                                </div>
                            </li>
                            <li>
                                <div class="li-key">
                                    <label for="revision-id">Revision ID</label>
                                </div>
                                <div class="li-value">
                                    <input type="text" id="revision-id" name="revisionId" value="${fn:escapeXml(param.revisionId)}"/>
                                </div>
                            </li>                         
                        </ul>
                    </fieldset>
                        <input type="hidden" class="offset-input" name="offset" value="0"/>
                        <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>
            </s:filter-flyout-widget>
            <h2 class="page-header-title">Activity Audit: ExpHour <c:out value="${selectionMessage}"/></h2>
            <ul class="bracket-horizontal-nav">
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit">Transactions</a></li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/exp-shift">ExpShift</a></li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/cc-shift">CcShift</a></li>
                <li>ExpHour</li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/cc-acc-hour">CcAccHour</a></li>
            </ul>                             
            <c:choose>
                <c:when test="${param.entityId == null}">
                    <div class="message-box">Provide an entity ID to continue</div>
                </c:when>
                <c:when test="${fn:length(entityList) == 0}">
                    <div class="message-box">Found 0 entity revisions</div>
                </c:when>
                <c:otherwise>                                
                    <div class="message-box">Showing entity revisions <fmt:formatNumber value="${paginator.startNumber}"/> - <fmt:formatNumber value="${paginator.endNumber}"/> of <fmt:formatNumber value="${paginator.totalRecords}"/></div>
                    <table id="revision-table" class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Revision #:</th>
                                    <c:forEach items="${entityList}" var="entity" varStatus="status">
                                    <th>
                                        <c:out value="${status.count + paginator.offset}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                        </thead>
                        <tfoot>
                            <tr>
                                <th>Modified By:</th>
                                    <c:forEach items="${entityList}" var="entity">
                                    <th>
                                        <c:out value="${entity.revision.username != null ? s:formatUsername(entity.revision.username) : entity.revision.username}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Modified Date:</th>
                                    <c:forEach items="${entityList}" var="entity">
                                    <th>
                                        <fmt:formatDate pattern="${s:getFriendlyDateTimePattern()}" value="${entity.revision.revisionDate}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Computer:</th>
                                    <c:forEach items="${entityList}" var="entity">
                                    <th>
                                        <c:out value="${btm:getHostnameFromIp(entity.revision.address)}"/>
                                    </th>
                                </c:forEach>
                            </tr>                            
                            <tr>
                                <th>Revision ID:</th>
                                    <c:forEach items="${entityList}" var="entity">
                                    <th>
                                        <c:out value="${entity.revision.id}"/>
                                    </th>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>Revision Type:</th>
                                    <c:forEach items="${entityList}" var="entity">
                                    <th>
                                        <c:out value="${entity.type}"/>
                                    </th>
                                </c:forEach>
                            </tr>                            
                        </tfoot>
                        <tbody>
                            <tr>                  
                                <th>ABU (${durationUnits.label}):</th>
                                    <c:forEach items="${entityList}" var="entity">
                                    <td>
                                        <c:out value="${btm:formatDuration(entity.abuSeconds, durationUnits)}"/>
                                    </td>
                                </c:forEach>
                            </tr>                             
                            <tr>                  
                                <th>BANU (${durationUnits.label}):</th>
                                    <c:forEach items="${entityList}" var="entity">
                                    <td>
                                        <c:out value="${btm:formatDuration(entity.banuSeconds, durationUnits)}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>BNA (${durationUnits.label}):</th>
                                <c:forEach items="${entityList}" var="entity">
                                    <td>
                                        <c:out value="${btm:formatDuration(entity.bnaSeconds, durationUnits)}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>ACC (${durationUnits.label}):</th>
                                <c:forEach items="${entityList}" var="entity">
                                    <td>
                                        <c:out value="${btm:formatDuration(entity.accSeconds, durationUnits)}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>OFF (${durationUnits.label}):</th>
                                <c:forEach items="${entityList}" var="entity">
                                    <td>
                                        <c:out value="${btm:formatDuration(entity.offSeconds, durationUnits)}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>ER (${durationUnits.label}):</th>
                                <c:forEach items="${entityList}" var="entity">
                                    <td>
                                        <c:out value="${btm:formatDuration(entity.erSeconds, durationUnits)}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>PCC (${durationUnits.label}):</th>
                                <c:forEach items="${entityList}" var="entity">
                                    <td>
                                        <c:out value="${btm:formatDuration(entity.pccSeconds, durationUnits)}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                            <tr>
                                <th>UED (${durationUnits.label}):</th>
                                <c:forEach items="${entityList}" var="entity">
                                    <td>
                                        <c:out value="${btm:formatDuration(entity.uedSeconds, durationUnits)}"/>
                                    </td>
                                </c:forEach>
                            </tr>
                        </tbody>
                    </table>
                    <div class="entity-controls">
                        <button class="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>
                        <button class="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>
                    </div>
                </c:otherwise>
            </c:choose>
        </section>
    </jsp:body>         
</t:report-page>
