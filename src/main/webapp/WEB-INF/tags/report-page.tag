<%@tag description="The Report Page Template Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="title" %>
<%@attribute name="stylesheets" fragment="true" %>
<%@attribute name="scripts" fragment="true" %>
<s:page title="${title}" category="Reports">
    <jsp:attribute name="stylesheets">       
        <jsp:invoke fragment="stylesheets"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <jsp:invoke fragment="scripts"/>
    </jsp:attribute>
    <jsp:attribute name="secondaryNavigation">
                                <ul>
                                    <li${'/reports/beam-time-summary' eq currentPath ? ' class="current-secondary"' : ''}><a
                                            href="${pageContext.request.contextPath}/reports/beam-time-summary">Beam Summary</a>
                                    </li>
                                    <li${'/reports/physics-summary' eq currentPath ? ' class="current-secondary"' : ''}><a
                                            href="${pageContext.request.contextPath}/reports/physics-summary">Physics
                                        Summary</a></li>
                                    <li${'/reports/hall-availability' eq currentPath ? ' class="current-secondary"' : ''}><a
                                            href="${pageContext.request.contextPath}/reports/hall-availability">Hall
                                        Availability</a></li>
                                    <li${'/reports/charge' eq currentPath ? ' class="current-secondary"' : ''}><a
                                            href="${pageContext.request.contextPath}/reports/charge">Charge</a></li>
                                    <li${'/reports/bt-calendar' eq currentPath ? ' class="current-secondary"' : ''}><a
                                            href="${pageContext.request.contextPath}/reports/bt-calendar">Review Calendar</a>
                                    </li>
                                    <li${'/reports/activity-audit' eq currentPath ? ' class="current-secondary"' : ''}><a
                                            href="${pageContext.request.contextPath}/reports/activity-audit">Activity Audit</a>
                                    </li>
                                    <li${'/reports/ca-status' eq currentPath ? ' class="current-secondary"' : ''}><a
                                            href="${pageContext.request.contextPath}/reports/ca-status">CA Status</a>
                                    </li>
                                </ul>
    </jsp:attribute>
    <jsp:body>
        <jsp:doBody/>
    </jsp:body>
</s:page>
