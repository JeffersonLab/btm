<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Program Schedule"/>
<c:set var="externalCdnPrefix" value=""/>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><c:out value="${title}"/></title>
    <link rel="shortcut icon"
          href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/img/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="${externalCdnPrefix}/jquery-ui/1.10.3/theme/smoothness/jquery-ui.min.css"/>
    <link rel="stylesheet" type="text/css" href="${externalCdnPrefix}/jlab-theme/smoothness/1.6/css/smoothness.min.css"/>
    <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/btm.css"/>
    <link rel="stylesheet" type="text/css"
          href="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/css/schedule.css"/>
    <style type="text/css">
        header {
            overflow: auto;
        }

        #tiny-nav {
            margin: 0.25em 0;
        }

        #cebaf-logo {
            background-color: black;
        }

        body #page {
            max-width: none;
            position: absolute;
        }

        body:not(.fullscreen) section #table-wrap {
            height: auto;
            overflow: visible;
        }

        body #sticky-header {
            height: 50px;
            margin-top: 0;
            margin-bottom: -62px;
        }
    </style>
</head>
<body>
<c:if test="${initParam.notification ne null}">
    <div id="notification-bar"><c:out value="${initParam.notification}"/></div>
</c:if>
<div id="page">
    <header>
        <div id="cebaf-logo">
            <a href="//cebaf.jlab.org"><img src="https://cebaf.jlab.org/files/cebaf_logo.png" alt="CEBAF"/></a>
        </div>
        <div id="tiny-nav">
            <a href="https://www.jlab.org">JLAB</a> |
            <a href="https://cebaf.jlab.org">CEBAF</a>
            <div id="auth">
                <c:url value="https://accweb.acc.jlab.org/btm/sso" var="loginUrl">
                    <c:param name="returnUrl" value="/btm/schedule"/>
                </c:url>
                <a href="${loginUrl}">Login (Onsite Only)</a>
            </div>
        </div>
    </header>
    <div id="content">
        <div id="content-liner">
            <t:monthly-schedule title="${title}" month="${start}" version="${version}" schedule="${schedule}" fullscreenAvailable="false"/>
        </div>
    </div>
</div>
<script type="text/javascript" src="${externalCdnPrefix}/jquery/1.10.2.min.js"></script>
<script type="text/javascript" src="${externalCdnPrefix}/jquery-ui/1.10.3/jquery-ui.min.js"></script>
<script type="text/javascript" src="${externalCdnPrefix}/jlab-theme/smoothness/1.6/js/smoothness.min.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/btm.js"></script>
<script type="text/javascript">
    jlab.contextPath = '${pageContext.request.contextPath}';
</script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/resources/v${initParam.resourceVersionNumber}/js/schedule.js"></script>
</body>
</html>