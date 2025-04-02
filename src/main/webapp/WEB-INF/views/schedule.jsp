<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="s" uri="http://jlab.org/jsp/smoothness"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="title" value="Program Schedule"/>
<s:page title="${title}">
    <jsp:attribute name="stylesheets">
        <link rel="stylesheet" type="text/css"
              href="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/css/schedule.css"/>
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script type="text/javascript"
                src="${pageContext.request.contextPath}/resources/v${initParam.releaseNumber}/js/schedule.js"></script>
    </jsp:attribute>
    <jsp:body>
        <t:monthly-schedule title="${title}" month="${start}" version="${version}" schedule="${schedule}" fullscreenAvailable="true"/>
        <div id="toolbar" style="display: none;">
            <button type="button" id="toolbar-copy-button">Copy</button>
            <a href="#" id="clipboard-link">Clipboard</a>
            <hr/>
            <button type="button" id="toolbar-paste-button">Paste</button>
            <label>X</label>
            <select id="toolbar-paste-count">
                <option>1</option>
                <option>2</option>
                <option>3</option>
                <option>4</option>
                <option>5</option>
                <option>6</option>
                <option>7</option>
                <option>8</option>
                <option>9</option>
                <option>10</option>
                <option>11</option>
                <option>12</option>
                <option>13</option>
                <option>14</option>
                <option>15</option>
                <option>16</option>
                <option>17</option>
                <option>18</option>
                <option>19</option>
                <option>20</option>
                <option>21</option>
                <option>22</option>
                <option>23</option>
                <option>24</option>
                <option>25</option>
                <option>26</option>
                <option>27</option>
                <option>28</option>
                <option>29</option>
                <option>30</option>
                <option>31</option>
            </select>
            <hr/>
            <button type="button" id="toolbar-save-button" class="ajax-submit">Save</button>
            <button type="button" id="toolbar-cancel-button">Cancel</button>
        </div>
        <div class="dialog" id="clipboard-dialog" title="Schedule Clipboard">
            <div id="clipboard-panel">
                <a href="#" id="clipboard-clear-link">Clear</a>
                <ul class="key-value-list">
                    <li>
                        <div class="li-key">
                            <span>Accelerator Program:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-acc-program"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>GeV / Pass:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-gev-pass"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Min Hall Count:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-min-hall-count"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall A Program:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-a-program"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall B Program:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-b-program"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall C Program:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-c-program"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall D Program:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-d-program"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall A GeV:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-a-gev"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall B GeV:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-b-gev"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall C GeV:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-c-gev"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall D GeV:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-d-gev"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall A μA:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-a-microamps"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall B μA:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-b-microamps"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall C μA:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-c-microamps"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall D μA:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-d-microamps"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall A Polarized:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-a-polarized"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall B Polarized:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-b-polarized"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall C Polarized:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-c-polarized"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall D Polarized:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-d-polarized"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall A Passes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-a-passes"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall B Passes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-b-passes"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall C Passes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-c-passes"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall D Passes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-d-passes"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall Priority:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-priority"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall A Notes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-a-notes"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall B Notes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-b-notes"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall C Notes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-c-notes"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Hall D Notes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-d-notes"></span>
                        </div>
                    </li>
                    <li>
                        <div class="li-key">
                            <span>Notes:</span>
                        </div>
                        <div class="li-value">
                            <span id="clipboard-notes"></span>
                        </div>
                    </li>
                </ul>
            </div>
            <div id="empty-message">-- Empty --</div>
        </div>
    </jsp:body>
</s:page>
