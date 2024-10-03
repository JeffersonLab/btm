var jlab = jlab || {};
jlab.btm = jlab.btm || {};

jlab.btm.gigaToKilo = function (giga) {
    return giga === '' ? '' : giga * 1000000;
};

jlab.btm.microToNano = function (micro) {
    return micro === '' ? '' : micro * 1000;
};

jlab.btm.isValidKiloVolts = function (kiloVolts) {
    return (kiloVolts === '' || ($.isNumeric(kiloVolts) && kiloVolts >= 0 && kiloVolts <= 13000000));
};

jlab.btm.isValidNanoAmps = function (nanoAmps) {
    return (nanoAmps === '' || ($.isNumeric(nanoAmps) && nanoAmps >= 0 && nanoAmps <= 500000));
};

jlab.btm.validateRow = function (row) {

    if (row.accProgram === '') {
        alert('Please choose an accelerator program');
        return false;
    }

    if (row.kiloVoltsPerPass !== '' && (!$.isNumeric(row.kiloVoltsPerPass) || row.kiloVoltsPerPass < 0 || row.kiloVoltsPerPass > 3000000)) {
        alert('GeV / Pass must be a value between 0.000 and 3.000 or empty');
        return false;
    }

    if (row.hallAProgramId === '') {
        alert('Please choose a hall A program');
        return false;
    }

    if (row.hallBProgramId === '') {
        alert('Please choose a hall B program');
        return false;
    }

    if (row.hallCProgramId === '') {
        alert('Please choose a hall C program');
        return false;
    }

    if (row.hallDProgramId === '') {
        alert('Please choose a hall D program');
        return false;
    }

    if (!jlab.btm.isValidKiloVolts(row.hallAKiloVolts)) {
        alert('Hall A GeV must be a value between 0.000 and 13.000 or empty');
        return false;
    }

    if (!jlab.btm.isValidKiloVolts(row.hallBKiloVolts)) {
        alert('Hall B GeV must be a value between 0.000 and 13.000 or empty');
        return false;
    }

    if (!jlab.btm.isValidKiloVolts(row.hallCKiloVolts)) {
        alert('Hall C GeV must be a value between 0.000 and 13.000 or empty');
        return false;
    }

    if (!jlab.btm.isValidKiloVolts(row.hallDKiloVolts)) {
        alert('Hall D GeV must be a value between 0.000 and 13.000 or empty');
        return false;
    }

    if (!jlab.btm.isValidNanoAmps(row.hallANanoAmps)) {
        alert('Hall A μA must be a value between 0.000 and 500.000 or empty');
        return false;
    }

    if (!jlab.btm.isValidNanoAmps(row.hallBNanoAmps)) {
        alert('Hall B μA must be a value between 0.000 and 500.000 or empty');
        return false;
    }

    if (!jlab.btm.isValidNanoAmps(row.hallCNanoAmps)) {
        alert('Hall C μA must be a value between 0.000 and 500.000 or empty');
        return false;
    }

    if (!jlab.btm.isValidNanoAmps(row.hallDNanoAmps)) {
        alert('Hall D μA must be a value between 0.000 and 500.000 or empty');
        return false;
    }

    if (row.hallDPasses === '6' && $.isNumeric(row.hallAPasses) && $.isNumeric(row.hallBPasses) && $.isNumeric(row.hallCPasses) && !(row.hallAPasses === '5' || row.hallBPasses === '5' || row.hallCPasses === '5')) {
        alert('If four hall operation then at least one of hall A,B,C must be 5 pass');
        return false;
    }

    return true;
};

jlab.btm.saveRow = function (row) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var success = false,
        $saveButton = $("#toolbar-save-button"),
        $cancelButton = $("#toolbar-cancel-button"),
        $copyButton = $("#toolbar-copy-button"),
        $pasteButton = $("#toolbar-paste-button");

    if (!jlab.btm.validateRow(row)) {
        return;
    }

    jlab.requestStart();

    $saveButton.html("<span class=\"button-indicator\"></span>");
    $saveButton.attr("disabled", "disabled");
    $cancelButton.attr("disabled", "disabled");
    $copyButton.attr("disabled", "disabled");
    $pasteButton.attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-schedule-row",
        type: "POST",
        data: row,
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to save schedule row: ' + $(".reason", data).html());
        } else {
            /* Success */
            success = true;
            window.location.reload();
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save schedule row: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save schedule row: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();

        if (success) {
            /* Do nothing as we're about to refresh page */
        } else {
            $saveButton.html("Save");
            $saveButton.removeAttr("disabled");
            $cancelButton.removeAttr("disabled");
            $copyButton.removeAttr("disabled");
            $pasteButton.removeAttr("disabled");
        }
    });
};

jlab.btm.updateVersionList = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    jlab.requestStart();

    $("#version-indicator").html("<span class=\"button-indicator\"></span>");

    var month = $("#date").val().toLowerCase().replace(' ', '-');

    var request = jQuery.ajax({
        url: "/btm/rest/monthly-schedule",
        type: "GET",
        data: {
            month: month
        },
        dataType: "json"
    });

    request.done(function (json) {
        $("#version").empty();
        if ($(json).length === 0) {
            $("#version").append('<option value=" ">None</option>');
        } else {
            $(json).each(function () {
                $("#version").append('<option value="' + this.version + '">' + this.version + (this.published === '' ? ' (Tentative)' : ' (Published ' + this.published + ')') + '</option>');
            });
        }
        $("#version").prop("selectedIndex", 0);
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to update version list: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to update version list: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $("#version-indicator").html('');
    });
};

jlab.btm.toolbarSaveAction = function () {

    var $tr = $("tr.ui-selected"),
        priority = $tr.find("td:nth-child(25) input").val();

        if(priority == null) {
            priority = "";
        }

        priority = priority.replace(new RegExp(',', 'g'), ''); /*Remove commas*/

        console.log(priority);

        var row = {
            date: $tr.find("td:nth-child(1)").attr("title").replace(/\s+/g, '-'),
            scheduleId: $("#schedule-table").attr("data-schedule-id"),
            accProgram: $tr.find("td:nth-child(2) select").val(),
            kiloVoltsPerPass: jlab.btm.gigaToKilo($tr.find("td:nth-child(3) input").val()),
            minHallCount: $tr.find("td:nth-child(4) select").val(),
            hallAProgramId: $tr.find("td:nth-child(5) select").val(),
            hallBProgramId: $tr.find("td:nth-child(6) select").val(),
            hallCProgramId: $tr.find("td:nth-child(7) select").val(),
            hallDProgramId: $tr.find("td:nth-child(8) select").val(),
            hallAKiloVolts: jlab.btm.gigaToKilo($tr.find("td:nth-child(9) input").val()),
            hallBKiloVolts: jlab.btm.gigaToKilo($tr.find("td:nth-child(10) input").val()),
            hallCKiloVolts: jlab.btm.gigaToKilo($tr.find("td:nth-child(11) input").val()),
            hallDKiloVolts: jlab.btm.gigaToKilo($tr.find("td:nth-child(12) input").val()),
            hallANanoAmps: jlab.btm.microToNano($tr.find("td:nth-child(13) input").val()),
            hallBNanoAmps: jlab.btm.microToNano($tr.find("td:nth-child(14) input").val()),
            hallCNanoAmps: jlab.btm.microToNano($tr.find("td:nth-child(15) input").val()),
            hallDNanoAmps: jlab.btm.microToNano($tr.find("td:nth-child(16) input").val()),
            hallAPolarized: $tr.find("td:nth-child(17) input").is(":checked") ? 'Y' : 'N',
            hallBPolarized: $tr.find("td:nth-child(18) input").is(":checked") ? 'Y' : 'N',
            hallCPolarized: $tr.find("td:nth-child(19) input").is(":checked") ? 'Y' : 'N',
            hallDPolarized: $tr.find("td:nth-child(20) input").is(":checked") ? 'Y' : 'N',
            hallAPasses: $tr.find("td:nth-child(21) select").val(),
            hallBPasses: $tr.find("td:nth-child(22) select").val(),
            hallCPasses: $tr.find("td:nth-child(23) select").val(),
            hallDPasses: $tr.find("td:nth-child(24) select").val(),
            hallAPriority: priority.indexOf('A') === -1 ? '' : priority.indexOf('A'),
            hallBPriority: priority.indexOf('B') === -1 ? '' : priority.indexOf('B'),
            hallCPriority: priority.indexOf('C') === -1 ? '' : priority.indexOf('C'),
            hallDPriority: priority.indexOf('D') === -1 ? '' : priority.indexOf('D'),
            hallANotes: $tr.find("td:nth-child(26) input").val(),
            hallBNotes: $tr.find("td:nth-child(27) input").val(),
            hallCNotes: $tr.find("td:nth-child(28) input").val(),
            hallDNotes: $tr.find("td:nth-child(29) input").val(),
            notes: $tr.find("td:nth-child(30) input").val(),
            count: 1
        };

    jlab.btm.saveRow(row);
};

jlab.btm.toolbarPasteAction = function () {
    if (localStorage.getItem("accProgram") === null) {
        alert("Clipboard is empty");
    } else {
        var $tr = $("tr.ui-selected"),
            priority = localStorage.getItem("priority"),
            row = {
                date: $tr.find("td:nth-child(1)").attr("title").replace(/\s+/g, '-'),
                scheduleId: $("#schedule-table").attr("data-schedule-id"),
                accProgram: localStorage.getItem("accProgram"),
                kiloVoltsPerPass: jlab.btm.gigaToKilo(localStorage.getItem("gevPerPass")),
                minHallCount: localStorage.getItem("minHallCount"),
                hallAProgramId: localStorage.getItem("hallAProgramId"),
                hallBProgramId: localStorage.getItem("hallBProgramId"),
                hallCProgramId: localStorage.getItem("hallCProgramId"),
                hallDProgramId: localStorage.getItem("hallDProgramId"),
                hallAKiloVolts: jlab.btm.gigaToKilo(localStorage.getItem("hallAGev")),
                hallBKiloVolts: jlab.btm.gigaToKilo(localStorage.getItem("hallBGev")),
                hallCKiloVolts: jlab.btm.gigaToKilo(localStorage.getItem("hallCGev")),
                hallDKiloVolts: jlab.btm.gigaToKilo(localStorage.getItem("hallDGev")),
                hallANanoAmps: jlab.btm.microToNano(localStorage.getItem("hallAMicroAmps")),
                hallBNanoAmps: jlab.btm.microToNano(localStorage.getItem("hallBMicroAmps")),
                hallCNanoAmps: jlab.btm.microToNano(localStorage.getItem("hallCMicroAmps")),
                hallDNanoAmps: jlab.btm.microToNano(localStorage.getItem("hallDMicroAmps")),
                hallAPolarized: localStorage.getItem("hallAPolarized") === '✔' ? 'Y' : 'N',
                hallBPolarized: localStorage.getItem("hallBPolarized") === '✔' ? 'Y' : 'N',
                hallCPolarized: localStorage.getItem("hallCPolarized") === '✔' ? 'Y' : 'N',
                hallDPolarized: localStorage.getItem("hallDPolarized") === '✔' ? 'Y' : 'N',
                hallAPasses: localStorage.getItem("hallAPasses"),
                hallBPasses: localStorage.getItem("hallBPasses"),
                hallCPasses: localStorage.getItem("hallCPasses"),
                hallDPasses: localStorage.getItem("hallDPasses") === '' || localStorage.getItem("hallDPasses") === null ? '' : localStorage.getItem("hallDPasses") * 1 + 0.5,
                hallAPriority: priority.indexOf('A') === -1 ? '' : priority.indexOf('A'),
                hallBPriority: priority.indexOf('B') === -1 ? '' : priority.indexOf('B'),
                hallCPriority: priority.indexOf('C') === -1 ? '' : priority.indexOf('C'),
                hallDPriority: priority.indexOf('D') === -1 ? '' : priority.indexOf('D'),
                hallANotes: localStorage.getItem("hallANotes"),
                hallBNotes: localStorage.getItem("hallBNotes"),
                hallCNotes: localStorage.getItem("hallCNotes"),
                hallDNotes: localStorage.getItem("hallDNotes"),
                notes: localStorage.getItem("notes"),
                count: $("#toolbar-paste-count").val()
            };

        jlab.btm.saveRow(row);
    }
};

jlab.btm.toolbarCopyAction = function () {
    var $tr = $("tr.ui-selected");

    localStorage.setItem("accProgram", $tr.find("td:nth-child(2) .read").text());
    localStorage.setItem("gevPerPass", $tr.find("td:nth-child(3) .read").text());
    localStorage.setItem("minHallCount", $tr.find("td:nth-child(4) .read").text());
    localStorage.setItem("hallAProgramId", $tr.find("td:nth-child(5) .read").attr("data-id"));
    localStorage.setItem("hallBProgramId", $tr.find("td:nth-child(6) .read").attr("data-id"));
    localStorage.setItem("hallCProgramId", $tr.find("td:nth-child(7) .read").attr("data-id"));
    localStorage.setItem("hallDProgramId", $tr.find("td:nth-child(8) .read").attr("data-id"));
    localStorage.setItem("hallAProgram", $tr.find("td:nth-child(5) .read").text());
    localStorage.setItem("hallBProgram", $tr.find("td:nth-child(6) .read").text());
    localStorage.setItem("hallCProgram", $tr.find("td:nth-child(7) .read").text());
    localStorage.setItem("hallDProgram", $tr.find("td:nth-child(8) .read").text());
    localStorage.setItem("hallAGev", $tr.find("td:nth-child(9) .read").text());
    localStorage.setItem("hallBGev", $tr.find("td:nth-child(10) .read").text());
    localStorage.setItem("hallCGev", $tr.find("td:nth-child(11) .read").text());
    localStorage.setItem("hallDGev", $tr.find("td:nth-child(12) .read").text());
    localStorage.setItem("hallAMicroAmps", $tr.find("td:nth-child(13) .read").text());
    localStorage.setItem("hallBMicroAmps", $tr.find("td:nth-child(14) .read").text());
    localStorage.setItem("hallCMicroAmps", $tr.find("td:nth-child(15) .read").text());
    localStorage.setItem("hallDMicroAmps", $tr.find("td:nth-child(16) .read").text());
    localStorage.setItem("hallAPolarized", $tr.find("td:nth-child(17) .read").text());
    localStorage.setItem("hallBPolarized", $tr.find("td:nth-child(18) .read").text());
    localStorage.setItem("hallCPolarized", $tr.find("td:nth-child(19) .read").text());
    localStorage.setItem("hallDPolarized", $tr.find("td:nth-child(20) .read").text());
    localStorage.setItem("hallAPasses", $tr.find("td:nth-child(21) .read").text());
    localStorage.setItem("hallBPasses", $tr.find("td:nth-child(22) .read").text());
    localStorage.setItem("hallCPasses", $tr.find("td:nth-child(23) .read").text());
    localStorage.setItem("hallDPasses", $tr.find("td:nth-child(24) .read").text());
    localStorage.setItem("priority", $tr.find("td:nth-child(25) .read").text());
    localStorage.setItem("hallANotes", $tr.find("td:nth-child(26) .read").text());
    localStorage.setItem("hallBNotes", $tr.find("td:nth-child(27) .read").text());
    localStorage.setItem("hallCNotes", $tr.find("td:nth-child(28) .read").text());
    localStorage.setItem("hallDNotes", $tr.find("td:nth-child(29) .read").text());
    localStorage.setItem("notes", $tr.find("td:nth-child(30) .read").text());
};

jlab.btm.doCancelRowSelection = function () {
    $("tbody tr").removeClass("ui-selected");
    $("tbody .write").remove();
    $("#toolbar").hide('slide', {direction: 'left'}, 250);
    $("table").addClass("selectable");
};

jlab.btm.doFilterFormSubmit = function () {
    if ($("#date").val() === '') {
        return false;
    }

    var urlDate = $("#date").val().toLowerCase().replace(' ', '-'),
        version = $("#version").val(),
        urlVersion = '';

    if (jQuery.isNumeric(version)) {
        urlVersion = version;
    }

    var params = {};

    if (($('#print-input').val() === 'Y') && ($('#fullscreen-input').val() === 'Y')) {
        params.print = 'Y';
        params.fullscreen = 'Y';
    }

    if ($("#view-select").val() === 'table') {
        params.view = 'table';
    }

    var queryString = $.param(params);

    if (queryString.length > 0) {
        queryString = '?' + queryString;
    }

    window.location.href = jlab.contextPath + '/schedule/' + encodeURIComponent(urlDate) + '/' + encodeURIComponent(urlVersion) + queryString;
};

$(document).on("click", "#toolbar-copy-button", function () {
    jlab.btm.toolbarCopyAction();
    jlab.btm.doCancelRowSelection();
});

$(document).on("click", "#toolbar-paste-button", function () {
    jlab.btm.toolbarPasteAction();
});

$(document).on("click", "#clipboard-clear-link", function () {
    localStorage.clear();
    $("#clipboard-dialog").dialog("close");
    return false;
});

$(document).on("click", "#clipboard-link", function () {
    if (localStorage.getItem("accProgram") === null) {
        $("#clipboard-dialog").addClass("empty-clipboard");
    } else {
        $("#clipboard-dialog").removeClass("empty-clipboard");
        $("#clipboard-acc-program").text(localStorage.getItem("accProgram"));
        $("#clipboard-gev-pass").text(localStorage.getItem("gevPerPass"));
        $("#clipboard-min-hall-count").text(localStorage.getItem("minHallCount"));
        $("#clipboard-a-program").text(localStorage.getItem("hallAProgram"));
        $("#clipboard-b-program").text(localStorage.getItem("hallBProgram"));
        $("#clipboard-c-program").text(localStorage.getItem("hallCProgram"));
        $("#clipboard-d-program").text(localStorage.getItem("hallDProgram"));
        $("#clipboard-a-gev").text(localStorage.getItem("hallAGev"));
        $("#clipboard-b-gev").text(localStorage.getItem("hallBGev"));
        $("#clipboard-c-gev").text(localStorage.getItem("hallCGev"));
        $("#clipboard-d-gev").text(localStorage.getItem("hallDGev"));
        $("#clipboard-a-microamps").text(localStorage.getItem("hallAMicroAmps"));
        $("#clipboard-b-microamps").text(localStorage.getItem("hallBMicroAmps"));
        $("#clipboard-c-microamps").text(localStorage.getItem("hallCMicroAmps"));
        $("#clipboard-d-microamps").text(localStorage.getItem("hallDMicroAmps"));
        $("#clipboard-a-polarized").text(localStorage.getItem("hallAPolarized"));
        $("#clipboard-b-polarized").text(localStorage.getItem("hallBPolarized"));
        $("#clipboard-c-polarized").text(localStorage.getItem("hallCPolarized"));
        $("#clipboard-d-polarized").text(localStorage.getItem("hallDPolarized"));
        $("#clipboard-a-passes").text(localStorage.getItem("hallAPasses"));
        $("#clipboard-b-passes").text(localStorage.getItem("hallBPasses"));
        $("#clipboard-c-passes").text(localStorage.getItem("hallCPasses"));
        $("#clipboard-d-passes").text(localStorage.getItem("hallDPasses"));
        $("#clipboard-priority").text(localStorage.getItem("priority"));
        $("#clipboard-a-notes").text(localStorage.getItem("hallANotes"));
        $("#clipboard-b-notes").text(localStorage.getItem("hallBNotes"));
        $("#clipboard-c-notes").text(localStorage.getItem("hallCNotes"));
        $("#clipboard-d-notes").text(localStorage.getItem("hallDNotes"));
        $("#clipboard-notes").text(localStorage.getItem("notes"));
    }

    $("#clipboard-dialog").dialog("open");

    return false;
});

$(document).on("click", "#toolbar-save-button", function () {
    jlab.btm.toolbarSaveAction();
});

$(document).on("click", "#filter-form-submit-button", function () {
    jlab.btm.doFilterFormSubmit();
    return false;
});

$(document).on("change", "#view-select", function () {
    jlab.btm.doFilterFormSubmit();
    return false;
});

$(document).on("click", "#toolbar-cancel-button", function () {
    jlab.btm.doCancelRowSelection();
});

$(document).on("click", "#publish-button", function () {
    return confirm('Are you sure you want to publish?   You cannot modify this version once published.');
});

$(document).on("change", "#fileInput", function () {

    var form = document.getElementById("upload-form");

    var request = jQuery.ajax({
        url: "/btm/schedule/upload",
        type: "POST",
        data: new FormData(form),
        processData: false,
        contentType: false,
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to upload: ' + $(".reason", data).html());
        } else {
            var msg = 'Schedule successfully uploaded: \n';

            $(".schedule", data).each(function () {
                msg = msg + $(".date", this).html() + ' version ' + $(".version", this).html() + '\n';
            });

            alert(msg);

            window.location.reload();
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to upload: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to upload: server did not handle request');
    });
});

$(document).on("click", ".editable-schedule tbody tr", function () {
    var $row = $(this),
        $editRow = $("tfoot tr"),
        rowIndex = $row.index(),
        lastRowIndex = $(".editable-schedule tbody tr:last-child").index(),
        maxPasteCount = lastRowIndex - rowIndex + 1;

    if ($(".ui-selected").length > 0) {
        return;
    }

    $row.addClass("ui-selected");
    $("table").removeClass("selectable");

    $("#toolbar-paste-count").empty();
    for (var i = 1; i <= maxPasteCount; i++) {
        $("#toolbar-paste-count").append('<option>' + i + '</option>');
    }

    $("#toolbar").show('slide', {direction: 'left'}, 250);

    var $td = $row.find("td:nth-child(2)");
    $td.append($editRow.find("td:nth-child(2) .write").clone()).find("select").val($td.find(".read").text());

    $td = $row.find("td:nth-child(3)");
    $td.append($editRow.find("td:nth-child(3) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(4)");
    $td.append($editRow.find("td:nth-child(4) .write").clone()).find("select").val($td.find(".read").text());

    $td = $row.find("td:nth-child(5)");
    $td.append($editRow.find("td:nth-child(5) .write").clone()).find("select").val($td.find(".read").attr("data-id"));

    $td = $row.find("td:nth-child(6)");
    $td.append($editRow.find("td:nth-child(6) .write").clone()).find("select").val($td.find(".read").attr("data-id"));

    $td = $row.find("td:nth-child(7)");
    $td.append($editRow.find("td:nth-child(7) .write").clone()).find("select").val($td.find(".read").attr("data-id"));

    $td = $row.find("td:nth-child(8)");
    $td.append($editRow.find("td:nth-child(8) .write").clone()).find("select").val($td.find(".read").attr("data-id"));

    $td = $row.find("td:nth-child(9)");
    $td.append($editRow.find("td:nth-child(9) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(10)");
    $td.append($editRow.find("td:nth-child(10) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(11)");
    $td.append($editRow.find("td:nth-child(11) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(12)");
    $td.append($editRow.find("td:nth-child(12) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(13)");
    $td.append($editRow.find("td:nth-child(13) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(14)");
    $td.append($editRow.find("td:nth-child(14) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(15)");
    $td.append($editRow.find("td:nth-child(15) .write").clone()).find("input").val($td.find(".read").text());

    /*Chrome has a weird bug where it will initially layout the input boxes with default width; adding an additional width style to one of them seems to fix it!*/
    $td = $row.find("td:nth-child(16)");
    $td.append($editRow.find("td:nth-child(16) .write").clone()).find("input").val($td.find(".read").text()).width("31px");

    $td = $row.find("td:nth-child(17)");
    $td.append($editRow.find("td:nth-child(17) .write").clone()).find("input").prop('checked', $td.find(".read").text() === '✔');

    $td = $row.find("td:nth-child(18)");
    $td.append($editRow.find("td:nth-child(18) .write").clone()).find("input").prop('checked', $td.find(".read").text() === '✔');

    $td = $row.find("td:nth-child(19)");
    $td.append($editRow.find("td:nth-child(19) .write").clone()).find("input").prop('checked', $td.find(".read").text() === '✔');

    $td = $row.find("td:nth-child(20)");
    $td.append($editRow.find("td:nth-child(20) .write").clone()).find("input").prop('checked', $td.find(".read").text() === '✔');

    $td = $row.find("td:nth-child(21)");
    $td.append($editRow.find("td:nth-child(21) .write").clone()).find("select").val($td.find(".read").text());

    $td = $row.find("td:nth-child(22)");
    $td.append($editRow.find("td:nth-child(22) .write").clone()).find("select").val($td.find(".read").text());

    $td = $row.find("td:nth-child(23)");
    $td.append($editRow.find("td:nth-child(23) .write").clone()).find("select").val($td.find(".read").text());

    $td = $row.find("td:nth-child(24)");
    $td.append($editRow.find("td:nth-child(24) .write").clone()).find("select").val($td.find(".read").text() * 1 + 0.5);

    $td = $row.find("td:nth-child(25)");
    $td.append($editRow.find("td:nth-child(25) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(26)");
    $td.append($editRow.find("td:nth-child(26) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(27)");
    $td.append($editRow.find("td:nth-child(27) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(28)");
    $td.append($editRow.find("td:nth-child(28) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(29)");
    $td.append($editRow.find("td:nth-child(29) .write").clone()).find("input").val($td.find(".read").text());

    $td = $row.find("td:nth-child(30)");
    $td.append($editRow.find("td:nth-child(30) .write").clone()).find("input").val($td.find(".read").text());

    return false;
});

$(function () {
    var monthNames = ["January", "February", "March", "April",
        "May", "June", "July", "August", "September",
        "October", "November", "December"];
    $(".monthpicker").datepicker({
        monthNamesShort: monthNames,
        dateFormat: 'MM yy',
        changeMonth: true,
        changeYear: true,
        showButtonPanel: true,
        onClose: function () {
            var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
            var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
            $(this).val($.datepicker.formatDate('MM yy', new Date(year, month, 1)));
            jlab.btm.updateVersionList();
        },
        beforeShow: function (input, inst) {
            var datestr, year, month, monthStr, tokens;
            if ((datestr = $(this).val()).length > 0) {
                tokens = datestr.split(" ");
                if (tokens.length > 1) {
                    year = tokens[1] * 1;
                    monthStr = tokens[0];
                    month = monthNames.indexOf(monthStr);
                }
                $(this).datepicker('option', 'defaultDate', new Date(year, month, 1));
                $(this).datepicker('setDate', new Date(year, month, 1));
            }
        }
    });

    $("#clipboard-dialog").dialog({
        autoOpen: false,
        width: 540,
        height: 380,
        minWidth: 540,
        minHeight: 380,
        modal: true
    });
}); 