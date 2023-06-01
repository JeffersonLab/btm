var jlab = jlab || {};
jlab.btm = jlab.btm || {};

jlab.btm.editShiftInfo = function (saveAll) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var startDayAndHour = $("#shift-start-hour").val(),
        hall = $("#shift-hall").val(),
        leader = $("#leader").val(),
        workers = $("#workers").val(),
        program = $("#program").val(),
        comments = $("#comments").val(),
        success = false;

    jlab.requestStart();

    $("#save-shift-info-button").html("<span class=\"button-indicator\"></span>");
    $("#save-shift-info-button").attr("disabled", "disabled");
    $("#cancel-shift-info-button").attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-exp-shift-info",
        type: "POST",
        data: {
            startDayAndHour: startDayAndHour,
            hall: hall,
            leader: leader,
            workers: workers,
            program: program,
            comments: comments
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to save shift information: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveShiftSuccess();
            success = true;
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save shift information: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save shift information: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $("#save-shift-info-button").html("Save");
        $("#save-shift-info-button").removeAttr("disabled");
        $("#cancel-shift-info-button").removeAttr("disabled");

        if (success) {
            $("#shift-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");
            if (saveAll) {
                jlab.btm.doSaveAll();
            }
        } else {
            var $signButton = $("#sign-button");
            $signButton.html("Sign");
            $signButton.removeAttr("disabled");
        }
    });
};

jlab.btm.doSaveShiftSuccess = function () {
    var $cancelButton = $("#cancel-shift-info-button"),
        $saveButton = $cancelButton.prev(),
        $editButton = $cancelButton.prev().prev(),
        $form = $cancelButton.closest(".form-wrapper");

    $editButton.show();
    $saveButton.hide();
    $cancelButton.hide();
    $form.find(".li-value span").each(function () {
        $(this).text($(this).next().val());
    })

    $("#program-span").text($("#program option:selected").text());
    $("#program").attr("data-purpose-id", $("#program").val());

    $form.find(".li-value span").show();
    $form.find(".li-value .input").hide();
};

jlab.btm.validateHourTableRowTotal = function ($table) {
    var tableType = $table.attr("data-type"),
        units = $("#units").attr("data-units");

    if (tableType === 'exp') {
        $table.find("tbody tr").each(function () {
            jlab.btm.validateAndUpdateExpRowTotal($(this), units);
        });
    }
};

jlab.btm.validateAndUpdateExpRowTotal = function ($tr, units) {
    var conversion;

    if (units === 'HOURS') {
        conversion = 3600;
    } else if (units === 'MINUTES') {
        conversion = 60;
    } else { /*SECONDS*/
        conversion = 1;
    }

    var abu = $tr.find("td:nth-child(2) input").val() * 1,
        banu = $tr.find("td:nth-child(3) input").val() * 1,
        bna = $tr.find("td:nth-child(4) input").val() * 1,
        acc = $tr.find("td:nth-child(5) input").val() * 1,
        off = $tr.find("td:nth-child(6) input").val() * 1,
        abu = Math.round(abu * conversion),
        banu = Math.round(banu * conversion),
        bna = Math.round(bna * conversion),
        acc = Math.round(acc * conversion),
        off = Math.round(off * conversion),
        total = (abu + banu + bna + acc + off) * 1,
        totalForDisplay = (total / conversion).toFixed(4) * 1,
        error = false;

    $tr.find("th:nth-child(7)").text(totalForDisplay);

    if (total !== 3600) {
        error = true;
    }


    var er = $tr.find("td:nth-child(8) input").val() * 1,
        pcc = $tr.find("td:nth-child(9) input").val() * 1,
        ued = $tr.find("td:nth-child(10) input").val() * 1,
        off = $tr.find("td:nth-child(6) input").val() * 1,
        er = Math.round(er * conversion),
        pcc = Math.round(pcc * conversion),
        ued = Math.round(ued * conversion),
        off = Math.round(off * conversion),
        total = (er + pcc + ued + off) * 1,
        totalForDisplay = (total / conversion).toFixed(4) * 1;

    $tr.find("th:nth-child(12)").text(totalForDisplay);

    if (total !== 3600) {
        error = true;
    }

    // Second off column is just mirror image of first
    var $th = $tr.find("th:nth-child(11)");
    $th.text($tr.find("td:nth-child(6) input").val());
    jlab.btm.updateMirrorColumnTotal($th);

    if (error === true) {
        $tr.addClass("ui-state-error");
    } else {
        $tr.removeClass("ui-state-error");
    }
};

jlab.btm.editExpHour = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var $saveButton = $(this),
        $row = $saveButton.closest("tr");

    var hourArray = [],
        abuArray = [],
        banuArray = [],
        bnaArray = [],
        accArray = [],
        offArray = [],
        erArray = [],
        pccArray = [],
        uedArray = [],
        commentsArray = [],
        $table = $("#exp-hourly-table"),
        success = false;

    var units = $("#units").attr("data-units"),
        hall = $table.attr("data-hall"),
        hour = $row.find("th").attr("data-hour"),
        abu = jlab.btm.parseSeconds($row.find("td:nth-child(2) input").val(), units),
        banu = jlab.btm.parseSeconds($row.find("td:nth-child(3) input").val(), units),
        bna = jlab.btm.parseSeconds($row.find("td:nth-child(4) input").val(), units),
        acc = jlab.btm.parseSeconds($row.find("td:nth-child(5) input").val(), units),
        off = jlab.btm.parseSeconds($row.find("td:nth-child(6) input").val(), units),
        er = jlab.btm.parseSeconds($row.find("td:nth-child(8) input").val(), units),
        pcc = jlab.btm.parseSeconds($row.find("td:nth-child(9) input").val(), units),
        ued = jlab.btm.parseSeconds($row.find("td:nth-child(10) input").val(), units),
        comments =  '';

    hourArray.push(hour);
    abuArray.push(abu);
    banuArray.push(banu);
    bnaArray.push(bna);
    accArray.push(acc);
    offArray.push(off);
    erArray.push(er);
    pccArray.push(pcc);
    uedArray.push(ued);
    commentsArray.push(comments);

    jlab.requestStart();

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-exp-hours",
        type: "POST",
        data: {
            hall: hall,
            'hour[]': hourArray,
            'abu[]': abuArray,
            'banu[]': banuArray,
            'bna[]': bnaArray,
            'acc[]': accArray,
            'off[]': offArray,
            'er[]': erArray,
            'pcc[]': pccArray,
            'ued[]': uedArray,
            'comments[]': commentsArray
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to save availability hours: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveHourRowSuccess($row, $saveButton);

            var complete = $table.find(".source-td:contains('EPICS')").length === 0;

            if (complete) {
                $("#availability-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");
            }

            success = true;
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save availability hours: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save availability hours: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
    });
};

jlab.btm.updateMirrorColumnTotal = function ($th) {
    var index = $th.parent().children().index($th),
        $thList = $th.closest("tbody").find("tr th:nth-child(" + (index + 1) + ")"),
        total = 0;

    $thList.each(function () {
        total = total + $(this).text() * 1;
    });

    total = total.toFixed(3) * 1;

    $th.closest("table").find("tfoot th:nth-child(" + (index + 1) + ")").text(total);
}

$(document).on("click", "#edit-shift-info-button", function () {
    var $editButton = $(this),
        $saveButton = $(this).next(),
        $cancelButton = $(this).next().next(),
        $form = $(this).closest(".form-wrapper");

    $editButton.hide();
    $saveButton.show();
    $cancelButton.show();
    $form.find(".li-value span").hide();
    $form.find(".li-value .input").show();
});

$(document).on("click", "#cancel-shift-info-button", function () {
    var $cancelButton = $(this),
        $saveButton = $(this).prev(),
        $editButton = $(this).prev().prev(),
        $form = $(this).closest(".form-wrapper");

    $editButton.show();
    $saveButton.hide();
    $cancelButton.hide();
    $form.find(".li-value span").show();
    $form.find(".li-value .input").hide();

    $form.find(".li-value input, .li-value textarea").each(function () {
        $(this).val($(this).prev().text());
    });

    $("#program").val($("#program").attr("data-purpose-id"));
});

$(document).on("click", "#save-shift-info-button", function () {
    jlab.btm.editShiftInfo();
});

$(document).on("click", "#exp-hourly-table .ui-icon-check", function () {
    jlab.btm.editExpHour.call(this);
});

$(document).on("change", "#exp-hourly-table input[type=text]", function () {
    jlab.btm.validateAndUpdateExpRowTotal($(this).closest("tr"), $("#units").attr("data-units"));
    jlab.btm.updateColumnTotal($(this).closest("td"));
});

$(document).on("click", ".ui-icon-pencil", function () {
    var $row = $(this).closest("tr"),
        index = $row.parent().children().index($row),
        $table = $("#comments-table"),
        $commentRow = $table.find("tbody tr:nth-child(" + (index + 1) + ")");

    $commentRow.find("td span").hide();
    $commentRow.find("textarea").show();
});

$(function () {
    jlab.btm.validateHourTableRowTotal($("#exp-hourly-table"));
});