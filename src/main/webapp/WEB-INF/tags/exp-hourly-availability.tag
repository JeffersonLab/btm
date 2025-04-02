<%@tag description="Experimenter Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="hall" required="true" type="org.jlab.smoothness.persistence.enumeration.Hall" %>
<%@attribute name="hourList" required="true" type="java.util.List" %>
<%@attribute name="epicsHourList" required="true" type="java.util.List" %>
<%@attribute name="totals" required="true" type="org.jlab.btm.persistence.projection.ExpHourTotals" %>
<%@attribute name="epicsTotals" required="true" type="org.jlab.btm.persistence.projection.ExpHourTotals" %>
<div id="exp-avail-panel" class="accordion">
    <form>
        <h3>Hall ${hall} Planned</h3>
        <div class="plan-div">
            <p>
                All of the metrics are based on comparing what actually happened vs a plan.   The plan can change rapidly to ensure the machine utilization is maximized.  For
                experiment timekeepers this means you may need to stay in contact with the Crew Chief during dynamic program times.
            </p>
            <p>
                There are at least four plans, each one informs the next, and in increasing granularity they are:
            </p>
            <ul>
                <li><a target="_blank" href="https://ace.jlab.org/btm/schedule">Nuclear Physics Experiment Scheduling (NPES) Schedule</a> - day resolution, months in advance</li>
                <li><a target="_blank" href="https://cebaf.jlab.org/files/ops/accboard/">MCC Whiteboard</a> - shift resolution, week in advance</li>
                <li><a target="_blank" href="https://ace.jlab.org/apps/pd/shift-plans">PD Shift Plan</a> - shift resolution, shift in advance</li>
                <li>Program Time - minute-by-minute Crew Chief plan</li>
            </ul>
            <p>
                <b>NOTE 1</b>: The Crew Chief plan is the one you actually use for time accounting.  A few points:
            </p>
            <ul>
                <li>The Crew Chief plan is not written down in advance as the Crew Chief makes decisions to maximize machine utilization, guided by the other plans</li>
                <li>The PD Shift Plan is tailored for the Crew Chief, and it does not line up exactly with your shift (Crew Chief shifts start and end one hour before yours) - you may wish to review the next PD shift plan to see how your last hour is scheduled</li>
                <li>If you have any questions please call the hall Run Coordinator or Crew Chief</li>
            </ul>
            <p>
                <b>NOTE 2</b>: The Experiment schedule is created by the <a href="https://www.jlab.org/physics/experiments/NPEScommittee">NPES committee</a>.  The NPES committee relies on the <a href="https://www.jlab.org/physics/PAC">Program Advisory Committee (PAC)</a> for candidates as vetted by the <a href="https://www.jlab.org/physics/TAC">Technical Advisory Committee (TAC)</a>.
            </p>
        </div>
        <h3>Hall ${hall} Measured</h3>
        <div>
            <c:choose>
                <c:when test="${fn:length(epicsHourList) > 0}">
                    <table class="data-table stripped-table hall-hourly-epics-table">
                        <thead>
                        <tr>
                            <th rowspan="2" class="hour-header"></th>
                            <th colspan="5">Accelerator Beam Time</th>
                            <th rowspan="2">Hour Total</th>
                            <th colspan="4">Experiment Beam Time</th>
                            <th rowspan="2">Hour Total</th>
                        </tr>
                        <tr>
                            <th class="duration-header">ABU</th>
                            <th class="duration-header">BANU</th>
                            <th class="duration-header">BNA</th>
                            <th class="duration-header">ACC</th>
                            <th class="duration-header">OFF</th>
                            <th class="duration-header">ER</th>
                            <th class="duration-header">PCC</th>
                            <th class="duration-header">UED</th>
                            <th class="duration-header">OFF</th>
                        </tr>
                        </thead>
                        <tfoot>
                        <tr>
                            <th>Shift Total</th>
                            <th>${btm:formatDuration(epicsTotals.abuSeconds, durationUnits)}</th>
                            <th>${btm:formatDuration(epicsTotals.banuSeconds, durationUnits)}</th>
                            <th>${btm:formatDuration(epicsTotals.bnaSeconds, durationUnits)}</th>
                            <th>${btm:formatDuration(epicsTotals.accSeconds, durationUnits)}</th>
                            <th>${btm:formatDuration(epicsTotals.offSeconds, durationUnits)}</th>
                            <th></th>
                            <th>${btm:formatDuration(epicsTotals.erSeconds, durationUnits)}</th>
                            <th>${btm:formatDuration(epicsTotals.pccSeconds, durationUnits)}</th>
                            <th>${btm:formatDuration(epicsTotals.uedSeconds, durationUnits)}</th>
                            <th>${btm:formatDuration(epicsTotals.offSeconds, durationUnits)}</th>
                            <th></th>
                        </tr>
                        </tfoot>
                        <tbody>
                        <c:forEach items="${epicsHourList}" var="hour">
                            <tr>
                                <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z"
                                                var="fullDate"/>
                                <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                                <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate
                                        value="${hour.dayAndHour}"
                                        pattern="HH"/></th>
                                <td>${btm:formatDuration(hour.abuSeconds, durationUnits)}</td>
                                <td>${btm:formatDuration(hour.banuSeconds, durationUnits)}</td>
                                <td>${btm:formatDuration(hour.bnaSeconds, durationUnits)}</td>
                                <td>${btm:formatDuration(hour.accSeconds, durationUnits)}</td>
                                <td>${btm:formatDuration(hour.offSeconds, durationUnits)}</td>
                                <th>${btm:formatDuration(hour.calculateAcceleratorTotal(), durationUnits)}</th>
                                <td>${btm:formatDuration(hour.erSeconds, durationUnits)}</td>
                                <td>${btm:formatDuration(hour.pccSeconds, durationUnits)}</td>
                                <td>${btm:formatDuration(hour.uedSeconds, durationUnits)}</td>
                                <td>${btm:formatDuration(hour.offSeconds, durationUnits)}</td>
                                <th>${btm:formatDuration(hour.calculateExperimentTotal(), durationUnits)}</th>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <p>No EPICS/BOOM Data</p>
                </c:otherwise>
            </c:choose>
        </div>
        <h3 id="hall-${hall}-reported-hourly-detail" class="initially-open-header">Hall ${hall} Reported</h3>
        <div>
            <div class="accordion-table-wrapper">
                <table id="exp-hourly-table"
                       class="data-table stripped-table editable-table" data-type="exp" data-hall="${hall}">
                    <thead>
                    <tr>
                        <th rowspan="2" class="hour-header"></th>
                        <th colspan="5">Accelerator Beam Time</th>
                        <th rowspan="2">Hour Total</th>
                        <th colspan="4">Experiment Beam Time</th>
                        <th rowspan="2">Hour Total</th>
                        <th rowspan="2">Source</th>
                        <th rowspan="2" style="width: 50px;"></th>
                    </tr>
                    <tr>
                        <th class="duration-header" title="Acceptable Beam in Use">ABU</th>
                        <th class="duration-header" title="Beam Available, but Not Used">BANU</th>
                        <th class="duration-header" title="Beam Not Available or unacceptable">BNA</th>
                        <th class="duration-header" title="Accelerator Configuration Change">ACC</th>
                        <th class="duration-header">OFF<div class="flyout-parent"><a class="flyout-link" data-flyout-type="off-flyout" href="#">*</a></div></th>
                        <th class="duration-header" title="Experiment Ready">ER</th>
                        <th class="duration-header" title="Planned Configuration Change">PCC</th>
                        <th class="duration-header" title="Unplanned Experiment Down">UED</th>
                        <th class="duration-header">OFF</th>
                    </tr>
                    </thead>
                    <tfoot>
                    <tr>
                        <th>Shift Total</th>
                        <th>${btm:formatDuration(totals.abuSeconds, durationUnits)}</th>
                        <th>${btm:formatDuration(totals.banuSeconds, durationUnits)}</th>
                        <th>${btm:formatDuration(totals.bnaSeconds, durationUnits)}</th>
                        <th>${btm:formatDuration(totals.accSeconds, durationUnits)}</th>
                        <th>${btm:formatDuration(totals.offSeconds, durationUnits)}</th>
                        <th></th>
                        <th>${btm:formatDuration(totals.erSeconds, durationUnits)}</th>
                        <th>${btm:formatDuration(totals.pccSeconds, durationUnits)}</th>
                        <th>${btm:formatDuration(totals.uedSeconds, durationUnits)}</th>
                        <th>${btm:formatDuration(totals.offSeconds, durationUnits)}</th>
                        <th></th>
                        <th></th>
                        <th></th>
                    </tr>
                    </tfoot>
                    <tbody>
                    <c:forEach items="${hourList}" var="hour">
                        <tr>
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                            <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                           pattern="HH"/></th>
                            <td>
                                <span><c:out value="${btm:formatDuration(hour.abuSeconds, durationUnits)}"/></span>
                                <input
                                        style="display: none;" type="text"
                                        value="${btm:formatDuration(hour.abuSeconds, durationUnits)}"/>
                            </td>
                            <td>
                                <span><c:out value="${btm:formatDuration(hour.banuSeconds, durationUnits)}"/></span>
                                <input
                                        style="display: none;" type="text"
                                        value="${btm:formatDuration(hour.banuSeconds, durationUnits)}"/>
                            </td>
                            <td>
                                <span><c:out value="${btm:formatDuration(hour.bnaSeconds, durationUnits)}"/></span>
                                <input
                                        style="display: none;" type="text"
                                        value="${btm:formatDuration(hour.bnaSeconds, durationUnits)}"/>
                            </td>
                            <td>
                                <span><c:out value="${btm:formatDuration(hour.accSeconds, durationUnits)}"/></span>
                                <input
                                        style="display: none;" type="text"
                                        value="${btm:formatDuration(hour.accSeconds, durationUnits)}"/>
                            </td>
                            <td>
                                <span><c:out value="${btm:formatDuration(hour.offSeconds, durationUnits)}"/></span>
                                <input
                                        style="display: none;" type="text"
                                        value="${btm:formatDuration(hour.offSeconds, durationUnits)}"/>
                            </td>
                            <th></th>
                            <td>
                                <span><c:out value="${btm:formatDuration(hour.erSeconds, durationUnits)}"/></span>
                                <input
                                        style="display: none;" type="text"
                                        value="${btm:formatDuration(hour.erSeconds, durationUnits)}"/>
                            </td>
                            <td>
                                <span><c:out value="${btm:formatDuration(hour.pccSeconds, durationUnits)}"/></span>
                                <input
                                        style="display: none;" type="text"
                                        value="${btm:formatDuration(hour.pccSeconds, durationUnits)}"/>
                            </td>
                            <td>
                                <span><c:out value="${btm:formatDuration(hour.uedSeconds, durationUnits)}"/></span>
                                <input
                                        style="display: none;" type="text"
                                        value="${btm:formatDuration(hour.uedSeconds, durationUnits)}"/>
                            </td>
                            <th class="mirror-th">
                                <span><c:out value="${btm:formatDuration(hour.offSeconds, durationUnits)}"/></span>
                            </th>
                            <th></th>
                            <th class="source-td"><c:out value="${hour.source.label}"/></th>
                            <th>
                                <span title="Edit (Single Row)" class="ui-icon ui-icon-pencil"></span>
                                <span title="Save" class="ui-icon ui-icon-check"></span>
                                <span title="Cancel" class="ui-icon ui-icon-close"></span>
                            </th>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>

                <table id="comments-table" class="data-table stripped-table editable-table">
                    <thead>
                    <tr>
                        <th class="hour-header"></th>
                        <th>Comments</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${hourList}" var="hour">
                        <tr>
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="dd MMM yyyy HH:mm z" var="fullDate"/>
                            <fmt:formatDate value="${hour.dayAndHour}" pattern="yyyy-MM-dd-HH-z" var="isoDate"/>
                            <th title="${fullDate}" data-hour="${isoDate}"><fmt:formatDate value="${hour.dayAndHour}"
                                                                                           pattern="HH"/></th>
                            <td>
                                <span><c:out value="${hour.remark}"/></span>
                                <textarea style="display: none;">${hour.remark}</textarea>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <div class="accordion-button-panel">
                    <button type="button" id="reload-button" style="float:left;">Reload EPICS Measured</button>
                    <button type="button" id="edit-all-button"
                            class="hour-edit-button"${editable ? '' : ' disabled="disabled"'}>Edit All
                    </button>
                    <button type="button" class="save-hall-button ajax-submit" style="display: none;"
                            id="exp-save-button">Save
                    </button>
                    <button type="button" class="hour-cancel-button" style="display: none;">Cancel</button>

                    <button type="button" id="edit-cc-only-button"
                            class="hour-edit-button"${editable ? '' : ' disabled="disabled"'}>Edit CC Hours
                    </button>
                    <button type="button" class="save-hall-button ajax-submit" style="display: none;"
                            id="exp-save-cc-button">Save
                    </button>
                    <button type="button" class="hour-cancel-button" style="display: none;">Cancel</button>
                </div>
            </div>
        </div>
    </form>
</div>
