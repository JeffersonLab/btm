var jlab = jlab || {};
jlab.editableRowTable = jlab.editableRowTable || {};
jlab.editableRowTable.entity = 'Run';
jlab.editableRowTable.dialog = jlab.editableRowTable.dialog || {};
jlab.editableRowTable.dialog.width = 500;
jlab.editableRowTable.dialog.height = 300;

jlab.btm = jlab.btm || {};

jlab.btm.changeRun = function (action, data, $dialog) {
    jlab.doAjaxJsonPostRequest(jlab.contextPath + "/setup/ajax/" + action + "-run", data, $dialog, true);
};

jlab.btm.runFormData = function () {
    return {
        firstDay: $("#row-first-day").val(),
        lastDay: $("#row-last-day").val()
    };
};

$(document).on("table-row-add", function () {
    jlab.btm.changeRun("add", jlab.btm.runFormData(), $("#table-row-dialog"));
});

$(document).on("table-row-edit", function () {
    var data = jlab.btm.runFormData();

    data.id = $(".editable-row-table tr.selected-row").attr("data-id");

    jlab.btm.changeRun("edit", data, $("#table-row-dialog"));
});

$(document).on("click", "#open-add-row-dialog-button", function () {
    $("#row-first-day").val("");
    $("#row-last-day").val("");
});

$(document).on("click", "#open-edit-row-dialog-button", function () {
    var $selectedRow = $(".editable-row-table tr.selected-row");

    if ($selectedRow.length < 1) {
        return;
    }

    $("#row-first-day").val($selectedRow.attr("data-first-day"));
    $("#row-last-day").val($selectedRow.attr("data-last-day"));
});

$(document).on("click", "#remove-row-button", function () {
    var $selectedRow = $(".editable-row-table tr.selected-row");

    if ($selectedRow.length < 1) {
        return;
    }

    if ($selectedRow.attr("data-manual") !== "true") {
        alert("This run comes from the published schedules, which would add it again.  Edit it to set its "
            + "actual days instead.");
        return;
    }

    if (confirm("Remove the run from " + $selectedRow.attr("data-first-day") + " to "
        + $selectedRow.attr("data-last-day") + "?  The published schedules then apply to its days again.")) {
        jlab.btm.changeRun("remove", {id: $selectedRow.attr("data-id")}, null);
    }
});
