<%@tag description="Experimenter Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hall" required="true" type="org.jlab.smoothness.persistence.enumeration.Hall" %>
<%@attribute name="hourList" required="true" type="java.util.List" %>
<div id="exp-reasons-panel">
    <form>
        <c:choose>
            <c:when test="${fn:length(hourList) > 0}">
                <table class="data-table stripped-table">
                    <thead>
                    <tr>
                        <th class="hour-header"></th>
                        <th>Reason</th>
                        <th class="duration-header">Duration</th>
                    </tr>
                    </thead>
                    <tfoot>
                    <tr>
                        <th></th>
                        <th>Shift Total</th>
                        <th></th>
                    </tr>
                    </tfoot>
                    <tbody>
                    <c:forEach items="${hourList}" var="hour">
                        <tr>
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z"
                                            var="fullDate"/>
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                            <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate
                                    value="${hour.dayAndHour}"
                                    pattern="HH"/></th>
                            <td></td>
                            <td></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p>No Reasons</p>
            </c:otherwise>
        </c:choose>
    </form>
</div>