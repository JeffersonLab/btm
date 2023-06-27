<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Metrics"/>
<t:page title="${title}">  
    <jsp:attribute name="stylesheets"> 
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/metrics.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts"> 
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/metrics.js"></script>
    </jsp:attribute>
    <jsp:body>
        <section>
            <h2><c:out value="${title}"/></h2>
            <p>Measuring Physics Program Progress</p>
            <div id="metrics-body" style="display: none;">
                <div class="tabset">
                    <ul>
                        <li><a href="#cc">Crew Chief</a></li>
                        <li><a href="#exp">Experimenter</a></li>
                    </ul>
                    <div id="cc">
                        <h3>Hall Progress</h3>
                        <p>Measured as a mutually exclusive duration per hour</p>
                        <table class="data-table stripped-table">
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Description</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>UP</td>
                                <td>Continuous Wave (CW) current measured on Hall Beam Current Monitor (BCM).
                                </td>
                            </tr>
                            <tr>
                                <td>TUNE</td>
                                <td>Pulsed current measured on Hall BCM.</td>
                            </tr>
                            <tr>
                                <td>BNR</td>
                                <td>Beam Not Requested.  This metric captures the time in which the machine is capable of
                                    delivering beam to a hall, but is not.  This generally occurs when a hall requests a procedure
                                    that does not allow beam such as a target change.  The automated tracking of this metric
                                    is done by BOOM, which effectively uses a stop watch with manual Crew Chief
                                    intervention.
                                </td>
                            </tr>
                            <tr>
                                <td>Physics Down</td>
                                <td>Component failures and Fast Shutdown System (FSD) trips that occur while attempting to deliver beam to a hall which is scheduled to receive it accumulate as Physics Down.</td>
                            </tr>
                            <tr>
                                <td>OFF</td>
                                <td>Hall is scheduled to be off for holiday, maintenance, or budget restrictions.</td>
                            </tr>
                            </tbody>
                        </table>
                        <h3>Multiplicity</h3>
                        <p>Measured as duration per hour</p>
                        <table class="data-table stripped-table">
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Description</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>One UP</td>
                                <td>Exactly one hall BCM is receiving beam.
                                </td>
                            </tr>
                            <tr>
                                <td>Two UP</td>
                                <td>Exactly two hall BCMs are receiving beam.</td>
                            </tr>
                            <tr>
                                <td>Three UP</td>
                                <td>Exactly three hall BCMs are receiving beam.
                                </td>
                            </tr>
                            <tr>
                                <td>Four UP</td>
                                <td>Exactly four hall BCMs are receiving beam.</td>
                            </tr>
                            <tr>
                                <td>Any UP</td>
                                <td>At least one hall BCM is receiving beam.</td>
                            </tr>
                            <tr>
                                <td>All UP</td>
                                <td>All halls that are supposed to be receiving beam are.  Specifically at least one hall
                                    BCM must be receiving beam and any other halls must either be receiving beam or
                                    otherwise be explicitly OFF (per BOOM forced OFF)</td>
                            </tr>
                            <tr>
                                <td>Down Hard</td>
                                <td>An FSD, component failure, or excessive tuning, affecting the entire machine (unlike a hall specific failure).
                                    All halls which are not scheduled off should be recording hall-specific Physics Down during Down Hard. Down Hard applies to both
                                    Physics and Internal programs, and if running an Internal program then Internal Down should be recorded
                                    during Down Hard. BTM Down Hard correlates with DTM accelerator event downtime.
                                    Down Hard generally starts counting on an FSD trip.</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div id="exp">
                        <h3>Hall Accelerator Beam Progress</h3>
                        <p>Measured as a mutually exclusive duration per hour</p>
                        <table class="data-table stripped-table">
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Description</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>ABU</td>
                                <td>Acceptable Beam Used.  Hall is receiving beam and is satisfied with the quality.
                                    Beam could be used for anything including HARP scans, Moeller measurements, data
                                    acquisition (DAQ), or any hall procedure with beam.
                                </td>
                            </tr>
                            <tr>
                                <td>BANU</td>
                                <td>Beam Available, but Not Used.  The Hall is scheduled for beam, but has requested no beam, and the accelerator is capable of delivering it.</td>
                            </tr>
                            <tr>
                                <td>BNA</td>
                                <td>Beam Not Acceptable.  The hall is scheduled for beam but is not receiving beam, or it is of unacceptable quality.
                                </td>
                            </tr>
                            <tr>
                                <td>ACC</td>
                                <td>Accelerator Configuration Change.</td>
                            </tr>
                            <tr>
                                <td>OFF</td>
                                <td>Hall is scheduled to be off for holiday, maintenance, or budget restrictions.</td>
                            </tr>
                            </tbody>
                        </table>
                        <h3>Hall Experiment Progress</h3>
                        <p>Measured as a mutually exclusive duration per hour</p>
                        <table class="data-table stripped-table">
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Description</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td>ER</td>
                                <td>Experiment Ready.
                                </td>
                            </tr>
                            <tr>
                                <td>PCC</td>
                                <td>Planned Configuration Change.</td>
                            </tr>
                            <tr>
                                <td>UED</td>
                                <td>Unplanned Experiment Down.
                                </td>
                            </tr>
                            <tr>
                                <td>OFF</td>
                                <td>Hall is scheduled to be off for holiday, maintenance, or budget restrictions.</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </section>
    </jsp:body>
</t:page>
