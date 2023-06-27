<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hall" required="true" type="java.lang.String" %>
<%@attribute name="totals" required="true" type="org.jlab.btm.persistence.projection.PhysicsSummaryTotals" %>
<%@attribute name="unknown" required="true" type="java.lang.Double" %>
<s:chart-widget>
    <div class="key-panel">
        <table class="chart-legend pie-legend">
            <tbody>
            <tr>
                <th>
                    <div class="color-box"></div>
                </th>
                <th class="legend-label">ABU</th>
                <td><fmt:formatNumber value="${totals.abuSeconds / 3600}" pattern="#,##0.0"/></td>
                <td>(<fmt:formatNumber value="${(totals.abuSeconds / 3600) / period * 100}" pattern="#,##0.0"/>%)</td>
            </tr>
            <tr>
                <th>
                    <div class="color-box"></div>
                </th>
                <th class="legend-label">BANU</th>
                <td><fmt:formatNumber value="${totals.banuSeconds / 3600}" pattern="#,##0.0"/></td>
                <td>(<fmt:formatNumber value="${(totals.banuSeconds / 3600) / period * 100}" pattern="#,##0.0"/>%)</td>
            </tr>
            <tr>
                <th>
                    <div class="color-box"></div>
                </th>
                <th class="legend-label">BNA</th>
                <td><fmt:formatNumber value="${totals.bnaSeconds / 3600}" pattern="#,##0.0"/></td>
                <td>(<fmt:formatNumber value="${(totals.bnaSeconds / 3600) / period * 100}" pattern="#,##0.0"/>%)</td>
            </tr>
            <tr>
                <th>
                    <div class="color-box"></div>
                </th>
                <th class="legend-label">ACC</th>
                <td><fmt:formatNumber value="${totals.accSeconds / 3600}" pattern="#,##0.0"/></td>
                <td>(<fmt:formatNumber value="${(totals.accSeconds / 3600) / period * 100}" pattern="#,##0.0"/>%)</td>
            </tr>
            <tr>
                <th>
                    <div class="color-box"></div>
                </th>
                <th class="legend-label">OFF</th>
                <td><fmt:formatNumber value="${totals.expOffSeconds / 3600}" pattern="#,##0.0"/></td>
                <td>(<fmt:formatNumber value="${(totals.expOffSeconds / 3600) / period * 100}" pattern="#,##0.0"/>%)
                </td>
            </tr>
            <tr>
                <th>
                    <div class="color-box"></div>
                </th>
                <th class="legend-label">Implicit OFF</th>
                <td><fmt:formatNumber value="${unknown}" pattern="#,##0.0"/></td>
                <td>(<fmt:formatNumber value="${unknown / period * 100}" pattern="#,##0.0"/>%)</td>
            </tr>
            </tbody>
        </table>
    </div>
</s:chart-widget>