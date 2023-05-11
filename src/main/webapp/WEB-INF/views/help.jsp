<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Help"/>
<t:page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/help.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/help.js"></script>
    </jsp:attribute>
    <jsp:body>
        <s:help-panel title="${title}">
            <ul>
                <li><a href="https://accwiki.acc.jlab.org/pub/SWDocs/BeamTimeAccounting/Machine_Time_Accounting_User_Guide.pdf">User Guide</a></li>
            </ul>
        </s:help-panel>
    </jsp:body>
</t:page>