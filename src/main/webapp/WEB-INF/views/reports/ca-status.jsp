<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="CA Status"/>
<t:report-page title="${title}">  
    <jsp:attribute name="stylesheets">
        <style>
            td:nth-child(3) {
                word-break: break-word;
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
    </jsp:attribute>
    <jsp:body>
        <section>
            <h2 class="page-header-title"><c:out value="${title}"/></h2>
            <p>The measured time accounting values are provided to this app via Experimental Physics and Industrial Control System (EPICS) Channel Access (CA) Process Variables (PVs).</p>
            <p>Often when an EPICS Input/Output Controller (IOC) reboots monitors don't come back properly (especially if a PV is accessed via a gateway, which many of these are). If any of the monitors show anything other than "NORMAL", a Crew Chief or Admin can Forcibly Reset the CA Client Context via the button here:</p>
            <form action="${pageContext.request.contextPath}/ajax/ca-reset" method="post">
                <button type="submit" ${pageContext.request.isUserInRole('cc') || pageContext.request.isUserInRole('btm-admin') ? '' : 'disabled="disabled"'}>Forcibly Reset</button>
            </form>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>PV</th>
                        <th>Last Update</th>
                        <th>Monitor Status</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${cache.map}" var="entry">
                        <tr>
                            <td>${entry.key}</td>
                            <td><fmt:formatDate value="${entry.value.ts}" pattern="${s:getFriendlyDateTimePattern()}"/></td>
                            <td>${entry.value.status.name} (${entry.value.status.severity.name} - ${entry.value.status.message})</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </section>
    </jsp:body>
</t:report-page>