<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="${type.label} Timesheet"/>
<fmt:formatDate value="${day}" pattern="dd MMM yyyy" var="formattedDate"/>
<t:page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/timesheet.css"/>
        <style type="text/css">
            #comparison-table .ui-icon {
                background-image: url("${pageContext.request.contextPath}/resources/jquery-ui-1.13.2/images/ui-icons_2e83ff_256x240.png");
            }
        </style>
    </jsp:attribute>
    <jsp:attribute name="scripts">
            <script type="text/javascript"
                    src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/timesheet.js">
            </script>
            <c:choose>
                <c:when test="${type eq 'CC'}">
                    <script type="text/javascript"
                            src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/cc-timesheet.js">
                    </script>
                </c:when>
                <c:otherwise>
                    <script type="text/javascript"
                            src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/exp-timesheet.js">
                    </script>
                </c:otherwise>
            </c:choose>
    </jsp:attribute>
    <jsp:body>
        <section>
            <div class="float-breadbox">
                <ul>
                    <li>
                        <a href="${previousUrl}">Previous</a>
                    </li>
                    <li>
                        <a href="${nextUrl}">Next</a>
                    </li>
                </ul>
            </div>
            <s:filter-flyout-widget ribbon="true">
                <form id="filter-form" action="timesheet" method="get">
                    <fieldset>
                        <legend>Filter</legend>
                        <ul class="key-value-list">
                            <li>
                                <div class="li-key"><label for="type">Type</label></div>
                                <div class="li-value">
                                    <select id="type">
                                        <option value="cc"${type eq 'CC' ? ' selected="selected"' : ''}>Crew Chief</option>
                                        <option value="ea"${type eq 'EA' ? ' selected="selected"' : ''}>Experimenter A</option>
                                        <option value="eb"${type eq 'EB' ? ' selected="selected"' : ''}>Experimenter B</option>
                                        <option value="ec"${type eq 'EC' ? ' selected="selected"' : ''}>Experimenter C</option>
                                        <option value="ed"${type eq 'ED' ? ' selected="selected"' : ''}>Experimenter D</option>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key"><label for="date">Date</label></div>
                                <div class="li-value"><input id="date" class="datepicker" placeholder="DD MMM YYYY"
                                                             type="text" value="${formattedDate}"/></div>
                            </li>
                            <li>
                                <div class="li-key"><label for="shift">Shift</label></div>
                                <div class="li-value">
                                    <select id="shift">
                                        <option value="owl"${shift eq 'OWL' ? ' selected="selected"' : ''}>Owl
                                        </option>
                                        <option value="day"${shift eq 'DAY' ? ' selected="selected"' : ''}>Day
                                        </option>
                                        <option value="swing"${shift eq 'SWING' ? ' selected="selected"' : ''}>
                                            Swing
                                        </option>
                                    </select>
                                </div>
                            </li>
                            <li>
                                <div class="li-key"><label for="units">Units</label></div>
                                <div class="li-value">
                                    <select id="units" data-units="${durationUnits}">
                                        <option value="hours"${durationUnits eq 'HOURS' ? ' selected="selected"' : ''}>
                                            Hours
                                        </option>
                                        <option value="minutes"${durationUnits eq 'MINUTES' ? ' selected="selected"' : ''}>
                                            Minutes
                                        </option>
                                        <option value="seconds"${durationUnits eq 'SECONDS' ? ' selected="selected"' : ''}>
                                            Seconds
                                        </option>
                                    </select>
                                </div>
                            </li>
                        </ul>
                    </fieldset>
                    <button id="filter-form-submit-button">Apply</button>
                </form>
            </s:filter-flyout-widget>
            <h2 id="page-header-title">Timesheet</h2>
            <div class="message-box"><c:out value="${message}"/></div>
        </section>
        <section class="${editable ? 'editable-timesheet' : ''}">
            <fmt:formatDate pattern="yyyy" value="${endHour}" var="endOfShiftYear"/>
            <fmt:formatDate pattern="MM" value="${endHour}" var="endOfShiftMonth"/>
            <fmt:formatDate pattern="dd" value="${endHour}" var="endOfShiftDay"/>
            <fmt:formatDate pattern="HH" value="${endHour}" var="endOfShiftHour"/>
            <div id="endOfShift" data-year="${endOfShiftYear}" data-month="${endOfShiftMonth}"
                 data-day="${endOfShiftDay}" data-hour="${endOfShiftHour}"></div>
            <div id="status-div"><a id="status-label" href="#">Status:</a>
                <c:choose>
                    <c:when test="${fn:length(signatureList) > 0}">
                        <span class="complete-status">Complete</span>
                    </c:when>
                    <c:otherwise>
                        <span class="incomplete-status">Incomplete</span>
                    </c:otherwise>
                </c:choose>
            </div>
            <c:choose>
                <c:when test="${type eq 'CC'}">
                    <t:cc-timesheet/>
                </c:when>
                <c:otherwise>
                    <t:exp-timesheet/>
                </c:otherwise>
            </c:choose>
        </section>
    </jsp:body>
</t:page>