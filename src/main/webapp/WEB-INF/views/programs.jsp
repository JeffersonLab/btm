<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Programs"/>
<t:page title="${title}">  
    <jsp:attribute name="stylesheets"> 
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/programs.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts"> 
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/programs.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <div>
                <div id="active-control-box"><label for="inactive-hidden">Hide Inactive</label><input
                        id="inactive-hidden" type="checkbox" checked="checked"/></div>
                <div class="tabset">
                    <ul>
                        <li><a href="#acc">Accelerator</a></li>
                        <li><a href="#a">Hall A</a></li>
                        <li><a href="#b">Hall B</a></li>
                        <li><a href="#c">Hall C</a></li>
                        <li><a href="#d">Hall D</a></li>
                    </ul>
                    <div id="acc">
                        <table class="data-table stripped-table">
                            <thead>
                            <tr>
                                <th colspan="2">Name/Mode</th>
                                <th>Description</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td colspan="2">PHYSICS MODE</td>
                                <td>Machine is provided to the Physics division with the goal of delivering beam to at
                                    least one experimental hall (even if down - choose Physics, then Physics Down for
                                    hall)
                                </td>
                            </tr>
                            <tr>
                                <td rowspan="4">INTERNAL MODE</td>
                                <td>Studies</td>
                                <td>Machine is being studied/tested/developed</td>
                            </tr>
                            <tr>
                                <td>SAD Restore</td>
                                <td>Machine is being restored after being OFF (SAD). SAD Restore is not to be confused
                                    with recovery after component failure, which is rolled up into down. Similarly,
                                    recovery from studies is counted as studies and should be scheduled accordingly.
                                    Excessive recovery (tuning) beyond the schedule is recorded as Down.
                                </td>
                            </tr>
                            <tr>
                                <td>ACC</td>
                                <td>Accelerator Configuration Change: Machine is changing configurations</td>
                            </tr>
                            <tr>
                                <td title="Studies / Restore / ACC Down">Internal Down</td>
                                <td>Machine is down due to component failures + FSDs that occurred during Studies,
                                    Restore, and ACC (but not during Physics). Excessive tuning is recorded as down.
                                    Unlike other programs this one is generally not scheduled.
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">SAD MODE (OFF)</td>
                                <td>Machine is scheduled to be off for holiday, maintenance, or budget restrictions
                                    (Scheduled Accelerator Down)
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div id="a">
                        <t:hall-programs purposeList="${hallAPurposeList}" hall="a" editable="${editable}"/>
                    </div>
                    <div id="b">
                        <t:hall-programs purposeList="${hallBPurposeList}" hall="b" editable="${editable}"/>
                    </div>
                    <div id="c">
                        <t:hall-programs purposeList="${hallCPurposeList}" hall="c" editable="${editable}"/>
                    </div>
                    <div id="d">
                        <t:hall-programs purposeList="${hallDPurposeList}" hall="d" editable="${editable}"/>
                    </div>
                </div>
            </div>
        </section>
        <div id="add-dialog" class="dialog" title="Add Program">
            <form>
                <ul class="key-value-list">
                    <li>
                        <div class="li-key">
                            Hall:
                        </div>
                        <div class="li-value">
                            <span id="add-program-hall"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="add-program-name">Name</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="add-program-name"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="add-program-alias">Alias</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="add-program-alias"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="add-program-url">URL</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="add-program-url"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="add-program-experiment">Experiment</label>
                        </div>
                        <div class="li-value">
                            <select id="add-program-experiment">
                                <option value="Y">Yes</option>
                                <option value="N">No</option>
                            </select>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="add-program-active">Active</label>
                        </div>
                        <div class="li-value">
                            <select id="add-program-active">
                                <option value="Y">Yes</option>
                                <option value="N">No</option>
                            </select>
                        </div>
                    </li>
                </ul>
                <div class="dialog-button-panel">
                    <button type="button" id="add-program-button" class="dialog-submit-button">Save</button>
                    <button type="button" class="dialog-close-button">Cancel</button>
                </div>
            </form>
        </div>
        <div id="edit-dialog" class="dialog" title="Edit Program">
            <form>
                <ul class="key-value-list">
                    <li>
                        <div class="li-key">
                            Hall:
                        </div>
                        <div class="li-value">
                            <span id="edit-program-hall"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="edit-program-name">Name</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="edit-program-name"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="edit-program-alias">Alias</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="edit-program-alias"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="edit-program-url">URL</label>
                        </div>
                        <div class="li-value">
                            <input type="text" id="edit-program-url"/>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="edit-program-experiment">Experiment</label>
                        </div>
                        <div class="li-value">
                            <select id="edit-program-experiment">
                                <option value="Y">Yes</option>
                                <option value="N">No</option>
                            </select>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <label for="edit-program-active">Active</label>
                        </div>
                        <div class="li-value">
                            <select id="edit-program-active">
                                <option value="Y">Yes</option>
                                <option value="N">No</option>
                            </select>
                        </div>
                    </li>
                </ul>
                <div class="dialog-button-panel">
                    <button type="button" id="edit-program-button" class="dialog-submit-button">Save</button>
                    <button type="button" class="dialog-close-button">Cancel</button>
                </div>
                <input type="hidden" id="program-id" value=""/>
            </form>
        </div>
    </jsp:body>
</t:page>
