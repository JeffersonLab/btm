<%@tag description="Hall Hourly Availability Tag" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="btm" uri="http://jlab.org/btm/functions" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@attribute name="editable" required="true" type="java.lang.Boolean" %>
<%@attribute name="hall" required="true" type="java.lang.String" %>
<%@attribute name="purposeList" required="true" type="java.util.List" %>
<c:if test="${editable}">
    <div class="table-button-panel" data-hall="${hall}">
        <button type="button" class="open-add-dialog-button no-selection-row-action">Add</button>
        <button type="button" class="open-edit-dialog-button selected-row-action" disabled="disabled">Edit</button>
        <button type="button" class="delete-button selected-row-action" disabled="disabled">Remove</button>
        <button type="button" class="unselect-button selected-row-action" disabled="disabled">Unselect</button>
    </div>
</c:if>
<table class="hall-program-table data-table stripped-table${editable ? ' uniselect-table editable-row-table' : ''}">
    <thead>
    <tr>
        <th>Name</th>
        <th>Alias</th>
        <th>URL</th>
        <th>Experiment</th>
        <th>Active</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${purposeList}" var="purpose">
        <tr data-program-id="${purpose.expHallShiftPurposeId}">
            <td><c:out value="${purpose.name}"/></td>
            <td><c:out value="${purpose.alias}"/></td>
            <td>
                <c:if test="${!empty purpose.url}">
                <a href="${fn:escapeXml(purpose.url)}"><c:out value="${purpose.url}"/><a/>
                    </c:if>
            </td>
            <td><c:out value="${purpose.experiment ? 'Yes' : 'No'}"/></td>
            <td><c:out value="${purpose.active ? 'Yes' : 'No'}"/></td>
        </tr>
    </c:forEach>
    </tbody>
</table>