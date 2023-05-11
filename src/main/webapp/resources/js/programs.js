var jlab = jlab || {};
jlab.btm = jlab.btm || {};

jlab.btm.addProgram = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var $saveButton = $("#add-program-button"),
        $cancelButton = $saveButton.next(),
        hall = $("#add-program-hall").text(),
        name = $("#add-program-name").val(),
        alias = $("#add-program-alias").val(),
        url = $("#add-program-url").val(),
        experiment = $("#add-program-experiment").val(),
        active = $("#add-program-active").val();

    jlab.requestStart();

    $saveButton.html("<span class=\"button-indicator\"></span>");
    $saveButton.attr("disabled", "disabled");
    $cancelButton.attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/add-program",
        type: "POST",
        data: {
            hall: hall,
            name: name,
            alias: alias,
            url: url,
            experiment: experiment,
            active: active
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to add program: ' + $(".reason", data).html());
        } else {
            /* Success */
            window.location.reload(false);
        }

    });

    request.error(function (xhr, textStatus) {
        window.console && console.log('Unable to add program: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to add program: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $saveButton.html("Save");
        $saveButton.removeAttr("disabled");
        $cancelButton.removeAttr("disabled");
    });
};

jlab.btm.editProgram = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var $saveButton = $("#edit-program-button"),
        $cancelButton = $saveButton.next(),
        id = $("#program-id").val(),
        name = $("#edit-program-name").val(),
        alias = $("#edit-program-alias").val(),
        url = $("#edit-program-url").val(),
        experiment = $("#edit-program-experiment").val(),
        active = $("#edit-program-active").val();

    jlab.requestStart();

    $saveButton.html("<span class=\"button-indicator\"></span>");
    $saveButton.attr("disabled", "disabled");
    $cancelButton.attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-program",
        type: "POST",
        data: {
            id: id,
            name: name,
            alias: alias,
            url: url,
            experiment: experiment,
            active: active
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to edit program: ' + $(".reason", data).html());
        } else {
            /* Success */
            window.location.reload(false);
        }

    });

    request.error(function (xhr, textStatus) {
        window.console && console.log('Unable to edit program: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to edit program: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $saveButton.html("Save");
        $saveButton.removeAttr("disabled");
        $cancelButton.removeAttr("disabled");
    });
};

jlab.btm.removeProgram = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var $deleteButton = $("#add-program-button"),
        programId = $(this).closest(".ui-tabs-panel").find("tr.selected-row").attr("data-program-id");

    jlab.requestStart();

    $deleteButton.html("<span class=\"button-indicator\"></span>");
    $deleteButton.attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/remove-program",
        type: "POST",
        data: {
            programId: programId
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to remove program: ' + $(".reason", data).html());
        } else {
            /* Success */
            window.location.reload(false);
        }

    });

    request.error(function (xhr, textStatus) {
        window.console && console.log('Unable to remove program: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to remove program: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $deleteButton.html("Save");
        $deleteButton.removeAttr("disabled");
    });
};

$(document).on("click", ".open-add-dialog-button", function () {
    $("#add-program-hall").text($(this).closest(".table-button-panel").attr("data-hall").toUpperCase());
    $("#add-program-name").val('');
    $("#add-program-alias").val('');
    $("#add-program-url").val('');
    $("#add-program-experiment").val('Y');
    $("#add-program-active").val('Y');
    $("#add-dialog").dialog("open");
});

$(document).on("click", ".open-edit-dialog-button", function () {
    var hall = $(this).closest(".table-button-panel").attr("data-hall").toUpperCase(),
        $selectedRow = $(this).closest(".ui-tabs-panel").find("tr.selected-row");
    $("#program-id").val($selectedRow.attr("data-program-id"));
    $("#edit-program-hall").text(hall);
    $("#edit-program-name").val($selectedRow.find("td:nth-child(1)").text());
    $("#edit-program-alias").val($selectedRow.find("td:nth-child(2)").text());
    $("#edit-program-url").val($selectedRow.find("td:nth-child(3) a").text());
    $("#edit-program-experiment").val($selectedRow.find("td:nth-child(4)").text().charAt(0));
    $("#edit-program-active").val($selectedRow.find("td:nth-child(5)").text().charAt(0));
    $("#edit-dialog").dialog("open");
    return false;
});

$(document).on("click", ".delete-button", function () {
    jlab.btm.removeProgram.call(this);
    return false;
});

$(document).on("click", "#add-program-button", function () {
    jlab.btm.addProgram();
});

$(document).on("click", "#edit-program-button", function () {
    jlab.btm.editProgram();
});
$(document).on("click", ".unselect-button", function () {
    $(".uniselect-table tr.selected-row").removeClass("selected-row");
    $(".selected-row-action").prop("disabled", true);
    $(".no-selection-row-action").prop("disabled", false);
});
$(document).on("click", ".ui-tabs-anchor", function () {
    $(".unselect-button").click();
});
$(document).on("change", "#inactive-hidden", function () {
    if ($(this).is(":checked")) {
        $("tbody td:nth-child(5):contains('No')").closest("tr").hide();
    } else {
        $("tbody td:nth-child(5):contains('No')").closest("tr").show();
    }
});
$(function () {
    $(".tabset").tabs({
        beforeActivate: function (event, ui) {
            window.location.hash = ui.newPanel.selector;
        }
    });

    $(".dialog").dialog({
        autoOpen: false,
        width: 540,
        height: 380,
        minWidth: 540,
        minHeight: 380,
        modal: true
    });

    $("#inactive-hidden").change();
}); 