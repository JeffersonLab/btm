<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions"%>
<t:report-page title="Activity Audit">
    <jsp:attribute name="stylesheets"> 
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script>
            $(document).on("click", ".default-clear-panel", function () {
                $("#type").val('');
                $("#shift").val('');
                $("#date").val('');
                $("#start").val('');
                $("#end").val('');
                $("#date-range").val('custom').trigger('change');
                return false;
            });
        </script>
    </jsp:attribute>        
    <jsp:body>
        <section>
            <s:filter-flyout-widget clearButton="true">
                <form class="filter-form" method="get" action="activity-audit">
                    <fieldset>
                        <legend>Modified Between</legend>
                        <s:date-range datetime="${true}" sevenAmOffset="${true}"/>
                    </fieldset>
                    <fieldset>
                        <legend>Timesheet</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key"><label for="type">Type</label></div>
                                <div class="li-value">
                                    <select id="type" name="type">
                                        <option value="">&nbsp;</option>
                                        <option value="cc"${param.type eq 'cc' ? ' selected="selected"' : ''}>Crew Chief</option>
                                        <option value="ea"${param.type eq 'ea' ? ' selected="selected"' : ''}>Experimenter A</option>
                                        <option value="eb"${param.type eq 'eb' ? ' selected="selected"' : ''}>Experimenter B</option>
                                        <option value="ec"${param.type eq 'ec' ? ' selected="selected"' : ''}>Experimenter C</option>
                                        <option value="ed"${param.type eq 'ed' ? ' selected="selected"' : ''}>Experimenter D</option>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key"><label for="date">Date</label></div>
                                <div class="li-value"><input id="date" name="date" class="datepicker" placeholder="DD-MMM-YYYY"
                                                             type="text" value="${fn:escapeXml(param.date)}"/></div>
                            </li>
                            <li>
                                <div class="li-key"><label for="shift">Shift</label></div>
                                <div class="li-value">
                                    <select id="shift" name="shift">
                                        <option value="">&nbsp;</option>
                                        <option value="owl"${param.shift eq 'owl' ? ' selected="selected"' : ''}>Owl
                                        </option>
                                        <option value="day"${param.shift eq 'day' ? ' selected="selected"' : ''}>Day
                                        </option>
                                        <option value="swing"${param.shift eq 'swing' ? ' selected="selected"' : ''}>
                                            Swing
                                        </option>
                                    </select>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <input type="hidden" class="offset-input" name="offset" value="0"/>
                    <input class="filter-form-submit-button" type="submit" value="Apply"/>
                </form>   
            </s:filter-flyout-widget>
            <h2 id="page-header-title">Activity Audit: Transactions</h2>
            <ul class="bracket-horizontal-nav">
                <li>Transactions</li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/exp-shift">ExpShift</a></li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/cc-shift">CCShift</a></li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/exp-hour">ExpHour</a></li>
                <li><a href="${pageContext.request.contextPath}/reports/activity-audit/cc-acc-hour">CcAccHour</a></li>
            </ul>
            <div class="message-box"><c:out value="${selectionMessage}"/></div>
            <div>
                <c:if test="${fn:length(revisionList) > 0}">     
                    <table class="data-table stripped-table">
                        <thead>
                            <tr>
                                <th>Revision ID</th>
                                <th>Modified Date</th>
                                <th>Modified By</th>
                                <th>Computer</th>
                                <th style="min-width: 300px;">Changes</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${revisionList}" var="revision">
                                <tr>
                                    <td><c:out value="${revision.id}"/></td>
                                    <td><fmt:formatDate value="${revision.revisionDate}" pattern="${s:getFriendlyDateTimePattern()}"/></td>
                                    <td><c:out value="${revision.username != null ? s:formatUsername(revision.username) : revision.username}"/></td>
                                    <td><c:out value="${btm:getHostnameFromIp(revision.address)}"/></td>
                                    <td>
                                        <c:if test="${fn:length(revision.changeList) > 0}">
                                            <ul class="table-cell-list">
                                                <c:forEach items="${revision.changeList}" var="change">
                                                    <li class="table-cell-list-item">
                                                        <a title="${change.entityClass.simpleName} Audit" href="${pageContext.request.contextPath}${change.url}"><c:out value="${change.type} ${change.entityClass.simpleName} ${change.name}"/></a>
                                                    </li>
                                                </c:forEach>
                                            </ul>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                    <div class="event-controls">
                        <button class="previous-button" type="button" data-offset="${paginator.previousOffset}" value="Previous"${paginator.previous ? '' : ' disabled="disabled"'}>Previous</button>
                        <button class="next-button" type="button" data-offset="${paginator.nextOffset}" value="Next"${paginator.next ? '' : ' disabled="disabled"'}>Next</button>
                    </div>
                </c:if>
            </div>                    
        </section>
    </jsp:body>         
</t:report-page>
