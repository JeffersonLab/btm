<%@tag description="Primary Navigation Tag" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@taglib prefix="s" uri="jlab.tags.smoothness"%>
<ul>
    <li${fn:startsWith(currentPath, '/timesheet') ? ' class="current-primary"' : ''}>
        <a href="${pageContext.request.contextPath}/timesheet">Timesheets</a>
    </li>
    <li${fn:startsWith(currentPath, '/schedule') ? ' class="current-primary"' : ''}>
        <a href="${pageContext.request.contextPath}/schedule">Schedule</a>
    </li>
    <li${'/programs' eq currentPath ? ' class="current-primary"' : ''}>
        <a href="${pageContext.request.contextPath}/programs">Programs</a>
    </li>
    <li${'/metrics' eq currentPath ? ' class="current-primary"' : ''}>
        <a href="${pageContext.request.contextPath}/metrics">Metrics</a>
    </li>
    <li${fn:startsWith(currentPath, '/reports') ? ' class="current-primary"' : ''}>
        <a href="${pageContext.request.contextPath}/reports/beam-time-summary">Reports</a>
    </li>
    <c:if test="${pageContext.request.isUserInRole('btm-admin')}">
        <li${fn:startsWith(currentPath, '/setup') ? ' class="current-primary"' : ''}><a
                href="${pageContext.request.contextPath}/setup/settings">Setup</a></li>
    </c:if>
    <li${'/help' eq currentPath ? ' class="current-primary"' : ''}>
        <a href="${pageContext.request.contextPath}/help">Help</a>
    </li>
</ul>