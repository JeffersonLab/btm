var jlab = jlab || {};
jlab.editableRowTable = jlab.editableRowTable || {};
jlab.editableRowTable.entity = 'Interval';
jlab.editableRowTable.dialog = jlab.editableRowTable.dialog || {};
jlab.editableRowTable.dialog.width = 500;
jlab.editableRowTable.dialog.height = 300;

jlab.btm = jlab.btm || {};

jlab.btm.changeInterval = function (action, data, $dialog) {
    jlab.doAjaxJsonPostRequest(jlab.contextPath + "/ajax/" + action + "-program-interval", data, $dialog, true);
};

jlab.btm.intervalFormData = function () {
    return {
        program: $("#row-program").val(),
        start: $("#row-start").val()
    };
};

$(document).on("table-row-add", function () {
    jlab.btm.changeInterval("add", jlab.btm.intervalFormData(), $("#table-row-dialog"));
});

$(document).on("table-row-edit", function () {
    var data = jlab.btm.intervalFormData();

    data.id = $(".editable-row-table tr.selected-row").attr("data-id");

    jlab.btm.changeInterval("edit", data, $("#table-row-dialog"));
});

$(document).on("click", "#open-edit-row-dialog-button", function () {
    var $selectedRow = $(".editable-row-table tr.selected-row");

    if ($selectedRow.length < 1) {
        return;
    }

    $("#row-program").val($selectedRow.attr("data-program"));
    $("#row-start").val($selectedRow.attr("data-start"));
});

$(document).on("click", "#remove-row-button", function () {
    var $selectedRow = $(".editable-row-table tr.selected-row");

    if ($selectedRow.length < 1) {
        return;
    }

    if (confirm("Remove the " + $selectedRow.find("td:first-child").text().trim() + " interval that starts "
        + $selectedRow.attr("data-start") + "?  The interval before it will run until the one after it.")) {
        jlab.btm.changeInterval("remove", {id: $selectedRow.attr("data-id")}, null);
    }
});
