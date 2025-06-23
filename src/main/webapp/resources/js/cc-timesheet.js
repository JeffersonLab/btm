var jlab = jlab || {};
jlab.btm = jlab.btm || {};


jlab.btm.validateAndUpdateHallRowTotal = function ($tr, units) {
    var conversion;

    if (units === 'HOURS') {
        conversion = 3600;
    } else if (units === 'MINUTES') {
        conversion = 60;
    } else { /*SECONDS*/
        conversion = 1;
    }

    var up = $tr.find("td:nth-child(2) input").val() * 1,
        tune = $tr.find("td:nth-child(3) input").val() * 1,
        bnr = $tr.find("td:nth-child(4) input").val() * 1,
        down = $tr.find("td:nth-child(5) input").val() * 1,
        off = $tr.find("td:nth-child(6) input").val() * 1,
        up = Math.round(up * conversion),
        tune = Math.round(tune * conversion),
        bnr = Math.round(bnr * conversion),
        down = Math.round(down * conversion),
        off = Math.round(off * conversion),
        total = (up + tune + bnr + down + off) * 1,
        totalForDisplay = (total / conversion).toFixed(4) * 1,
        error = false;

    $tr.find("th:nth-child(7)").text(totalForDisplay);

    if (total !== 3600) {
        error = true;
    }

    if (error === true) {
        $tr.addClass("ui-state-error");
    } else {
        $tr.removeClass("ui-state-error");
    }
};

jlab.btm.validateAndUpdateAccRowTotal = function ($tr, units) {
    var conversion;

    if (units === 'HOURS') {
        conversion = 3600;
    } else if (units === 'MINUTES') {
        conversion = 60;
    } else { /*SECONDS*/
        conversion = 1;
    }

    var up = $tr.find("td:nth-child(2) input").val() * 1,
        tuning = $tr.find("td:nth-child(3) input").val() * 1,
        studies = $tr.find("td:nth-child(4) input").val() * 1,
        restore = $tr.find("td:nth-child(5) input").val() * 1,
        acc = $tr.find("td:nth-child(6) input").val() * 1,
        down = $tr.find("td:nth-child(7) input").val() * 1,
        off = $tr.find("td:nth-child(8) input").val() * 1,
        up = Math.round(up * conversion),
        tuning = Math.round(tuning * conversion),
        studies = Math.round(studies * conversion),
        restore = Math.round(restore * conversion),
        acc = Math.round(acc * conversion),
        down = Math.round(down * conversion),
        off = Math.round(off * conversion),
        total = (up + tuning + studies + restore + acc + down + off) * 1,
        totalForDisplay = (total / conversion).toFixed(4) * 1,
        error = false;

    $tr.find("th:nth-child(9)").text(totalForDisplay);

    if (total !== 3600) {
        error = true;
    }

    if (error === true) {
        $tr.addClass("ui-state-error");
    } else {
        $tr.removeClass("ui-state-error");
    }
};

jlab.btm.validateMultiRow = function ($tr, units) {
    var threeUp = $tr.find("td:nth-child(2) input").val() * 1,
        twoUp = $tr.find("td:nth-child(3) input").val() * 1,
        oneUp = $tr.find("td:nth-child(4) input").val() * 1,
        anyUp = $tr.find("td:nth-child(5) input").val() * 1,
        allUp = $tr.find("td:nth-child(6) input").val() * 1,
        downHard = $tr.find("td:nth-child(7) input").val() * 1,
        error = false,
        max = 1;

    if (units === 'MINUTES') {
        max = 60;
    }

    if (units === 'SECONDS') {
        max = 3600;
    }

    if (threeUp > max || twoUp > max || oneUp > max || anyUp > max || allUp > max || downHard > max) {
        error = true;
    } else if (!jQuery.isNumeric(threeUp) || !jQuery.isNumeric(twoUp) || !jQuery.isNumeric(oneUp) || !jQuery.isNumeric(anyUp) || !jQuery.isNumeric(allUp) || !jQuery.isNumeric(downHard)) {
        error = true;
    }

    if (error === true) {
        $tr.addClass("ui-state-error");
    } else {
        $tr.removeClass("ui-state-error");
    }
};

jlab.btm.validateCrossCheckStatusThenSave = function () {
    var request = jQuery.ajax({
        url: window.location,
        type: "GET",
        data: {
            crosscheck: 'Y'
        },
        dataType: "html"
    });

    request.done(function (data) {
        var result = true;

        $("#cross-check-summary-panel").replaceWith($("#cross-check-summary-panel", data));
        $("#cross-check-details-panel").replaceWith($("#cross-check-details-panel", data));

        if ($("#comparison-table td:last-child.ui-state-error").length > 0
            || $("#dtm-btm-table td:last-child.ui-state-error", data).length > 0) {
            result = confirm("The cross checks failed (does the time accounting make sense?).   Are you sure you want to save?");

            if (result && $("#view-cross-check-comments").text().trim() === '') {
                alert('You must provide an explanation (cross check comment) before saving a timesheet with cross check discrepancies');
                result = false;
            }
        }

        if (result) {
            jlab.btm.doSaveAll();
        } else {
            var $button = $("#sign-button");
            $button.html("Sign");
            $button.removeAttr("disabled");
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to reload cross check summary/details: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to reload cross check summary/details: server did not handle request');

        var $button = $("sign-button");
        $button.html("Sign");
        $button.removeAttr("disabled");
    });
};

jlab.btm.validateShiftInfoForm = function () {
    return true;
};

jlab.btm.validateAccHourForm = function () {
    return true;
};

jlab.btm.validateHallHourForm = function () {
    return true;
};

jlab.btm.editAccHours = function (saveAll) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    if (!jlab.btm.validateAccHourForm()) {
        return;
    }

    if (!saveAll && !jlab.btm.validateSaveFutureShift()) {
        return;
    }

    var hourArray = [],
        upArray = [],
        tuningArray = [],
        sadArray = [],
        downArray = [],
        studiesArray = [],
        restoreArray = [],
        accArray = [],
        $table = $("#acc-hourly-table"),
        $button = $("#save-acc-button"),
        success = false;

    $table.find("tbody tr").each(function () {
        var $row = $(this),
            units = $("#units").attr("data-units"),
            hour = $row.find("th").attr("data-hour"),
            up = jlab.btm.parseSeconds($row.find("td:nth-child(2) input").val(), units),
            tuning = jlab.btm.parseSeconds($row.find("td:nth-child(3) input").val(), units),
            studies = jlab.btm.parseSeconds($row.find("td:nth-child(4) input").val(), units),
            restore = jlab.btm.parseSeconds($row.find("td:nth-child(5) input").val(), units),
            acc = jlab.btm.parseSeconds($row.find("td:nth-child(6) input").val(), units),
            down = jlab.btm.parseSeconds($row.find("td:nth-child(7) input").val(), units),
            sad = jlab.btm.parseSeconds($row.find("td:nth-child(8) input").val(), units);

        hourArray.push(hour);
        upArray.push(up);
        tuningArray.push(tuning);
        studiesArray.push(studies);
        restoreArray.push(restore);
        accArray.push(acc);
        downArray.push(down);
        sadArray.push(sad);
    });

    jlab.requestStart();

    $button.html("<span class=\"button-indicator\"></span>");
    $button.attr("disabled", "disabled");
    $("#cancel-acc-button").attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-acc-hours",
        type: "POST",
        data: {
            'hour[]': hourArray,
            'up[]': upArray,
            'tuning[]': tuningArray,
            'studies[]': studiesArray,
            'restore[]': restoreArray,
            'acc[]': accArray,
            'down[]': downArray,
            'sad[]': sadArray
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            $("#hourly-tab-li a").click();
            var $h3 = $table.closest(".ui-accordion-content").prev("h3");
            jlab.btm.showAccordionPanel($h3);
            location.hash = '#' + $h3.attr("id");
            alert('Unable to save accelerator availability hours: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveHourTableSuccess($table, $button);
            jlab.btm.copyAccShiftTotalToSummary();
            success = true;
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save accelerator availability hours: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save accelerator availability hours: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $button.html("Save");
        $button.removeAttr("disabled");
        $("#cancel-acc-button").removeAttr("disabled");

        if (success) {
            $("#accelerator-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");
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

jlab.btm.editAccHour = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var $saveButton = $(this),
        $row = $saveButton.closest("tr");

    var hourArray = [],
        upArray = [],
        tuningArray = [],
        sadArray = [],
        downArray = [],
        studiesArray = [],
        restoreArray = [],
        accArray = [],
        $table = $("#acc-hourly-table"),
        success = false;

    var units = $("#units").attr("data-units"),
        hour = $row.find("th").attr("data-hour"),
        up = jlab.btm.parseSeconds($row.find("td:nth-child(2) input").val(), units),
        tuning = jlab.btm.parseSeconds($row.find("td:nth-child(3) input").val(), units),
        studies = jlab.btm.parseSeconds($row.find("td:nth-child(4) input").val(), units),
        restore = jlab.btm.parseSeconds($row.find("td:nth-child(5) input").val(), units),
        acc = jlab.btm.parseSeconds($row.find("td:nth-child(6) input").val(), units),
        down = jlab.btm.parseSeconds($row.find("td:nth-child(7) input").val(), units),
        sad = jlab.btm.parseSeconds($row.find("td:nth-child(8) input").val(), units);

    hourArray.push(hour);
    upArray.push(up);
    tuningArray.push(tuning);
    studiesArray.push(studies);
    restoreArray.push(restore);
    accArray.push(acc);
    downArray.push(down);
    sadArray.push(sad);

    jlab.requestStart();

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-acc-hours",
        type: "POST",
        data: {
            'hour[]': hourArray,
            'up[]': upArray,
            'tuning[]': tuningArray,
            'studies[]': studiesArray,
            'restore[]': restoreArray,
            'acc[]': accArray,
            'down[]': downArray,
            'sad[]': sadArray
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            $("#hourly-tab-li a").click();
            var $h3 = $table.closest(".ui-accordion-content").prev("h3");
            jlab.btm.showAccordionPanel($h3);
            location.hash = '#' + $h3.attr("id");
            alert('Unable to save accelerator availability hours: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveHourRowSuccess($row, $saveButton);
            jlab.btm.copyAccShiftTotalToSummary();

            var complete = $table.find(".source-td:contains('EPICS')").length === 0;

            if (complete) {
                $("#accelerator-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");
            }

            success = true;
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save accelerator availability hours: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save accelerator availability hours: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
    });
};

jlab.btm.editMultiHours = function (saveAll) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    if (!saveAll && !jlab.btm.validateSaveFutureShift()) {
        return;
    }

    var hourArray = [],
        fourUpArray = [],
        threeUpArray = [],
        twoUpArray = [],
        oneUpArray = [],
        anyUpArray = [],
        allUpArray = [],
        downHardArray = [],
        $table = $("#multiplicity-hourly-table"),
        $button = $("#save-multiplicity-button"),
        success = false;

    $table.find("tbody tr").each(function () {
        var $row = $(this),
            units = $("#units").attr("data-units"),
            hour = $row.find("th").attr("data-hour"),
            fourUp = jlab.btm.parseSeconds($row.find("td:nth-child(2) input").val(), units),
            threeUp = jlab.btm.parseSeconds($row.find("td:nth-child(3) input").val(), units),
            twoUp = jlab.btm.parseSeconds($row.find("td:nth-child(4) input").val(), units),
            oneUp = jlab.btm.parseSeconds($row.find("td:nth-child(5) input").val(), units),
            anyUp = jlab.btm.parseSeconds($row.find("td:nth-child(6) input").val(), units),
            allUp = jlab.btm.parseSeconds($row.find("td:nth-child(7) input").val(), units),
            downHard = jlab.btm.parseSeconds($row.find("td:nth-child(8) input").val(), units);

        hourArray.push(hour);
        fourUpArray.push(fourUp);
        threeUpArray.push(threeUp);
        twoUpArray.push(twoUp);
        oneUpArray.push(oneUp);
        anyUpArray.push(anyUp);
        allUpArray.push(allUp);
        downHardArray.push(downHard);
    });

    jlab.requestStart();

    $button.html("<span class=\"button-indicator\"></span>");
    $button.attr("disabled", "disabled");
    $button.next().attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-multi-hours",
        type: "POST",
        data: {
            'hour[]': hourArray,
            'fourUp[]': fourUpArray,
            'threeUp[]': threeUpArray,
            'twoUp[]': twoUpArray,
            'oneUp[]': oneUpArray,
            'anyUp[]': anyUpArray,
            'allUp[]': allUpArray,
            'downHard[]': downHardArray
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            $("#hourly-tab-li a").click();
            var $h3 = $table.closest(".ui-accordion-content").prev("h3");
            jlab.btm.showAccordionPanel($h3);
            location.hash = '#' + $h3.attr("id");
            alert('Unable to save multiplicity hours: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveHourTableSuccess($table, $button);
            jlab.btm.copyMultiShiftTotalToSummary();
            success = true;
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save multiplicity hours: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save multiplicity hours: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $button.html("Save");
        $button.removeAttr("disabled");
        $button.next().removeAttr("disabled");

        if (success) {
            $("#multiplicity-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");
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

jlab.btm.editMultiHour = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var $saveButton = $(this),
        $row = $saveButton.closest("tr"),
        $table = $row.closest("table");

    var hourArray = [],
        fourUpArray = [],
        threeUpArray = [],
        twoUpArray = [],
        oneUpArray = [],
        anyUpArray = [],
        allUpArray = [],
        downHardArray = [],
        $table = $("#multiplicity-hourly-table"),
        success = false;

    var units = $("#units").attr("data-units"),
        hour = $row.find("th").attr("data-hour"),
        fourUp = jlab.btm.parseSeconds($row.find("td:nth-child(2) input").val(), units),
        threeUp = jlab.btm.parseSeconds($row.find("td:nth-child(3) input").val(), units),
        twoUp = jlab.btm.parseSeconds($row.find("td:nth-child(4) input").val(), units),
        oneUp = jlab.btm.parseSeconds($row.find("td:nth-child(5) input").val(), units),
        anyUp = jlab.btm.parseSeconds($row.find("td:nth-child(6) input").val(), units),
        allUp = jlab.btm.parseSeconds($row.find("td:nth-child(7) input").val(), units),
        downHard = jlab.btm.parseSeconds($row.find("td:nth-child(8) input").val(), units);

    hourArray.push(hour);
    fourUpArray.push(fourUp);
    threeUpArray.push(threeUp);
    twoUpArray.push(twoUp);
    oneUpArray.push(oneUp);
    anyUpArray.push(anyUp);
    allUpArray.push(allUp);
    downHardArray.push(downHard);

    jlab.requestStart();

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-multi-hours",
        type: "POST",
        data: {
            'hour[]': hourArray,
            'fourUp[]': fourUpArray,
            'threeUp[]': threeUpArray,
            'twoUp[]': twoUpArray,
            'oneUp[]': oneUpArray,
            'anyUp[]': anyUpArray,
            'allUp[]': allUpArray,
            'downHard[]': downHardArray
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            $("#hourly-tab-li a").click();
            var $h3 = $table.closest(".ui-accordion-content").prev("h3");
            jlab.btm.showAccordionPanel($h3);
            location.hash = '#' + $h3.attr("id");
            alert('Unable to save multiplicity hours: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveHourRowSuccess($row, $saveButton);
            jlab.btm.copyMultiShiftTotalToSummary();

            var complete = $table.find(".source-td:contains('EPICS')").length === 0;

            if (complete) {
                $("#multiplicity-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");
            }

            success = true;
        }

    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save multiplicity hours: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save multiplicity hours: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
    });
};

jlab.btm.signTimesheet = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var $button = $("#sign-button"),
        startDayAndHour = $("#shift-start-hour").val(),
        leaveSpinning = false;

    jlab.requestStart();

    $button.html("<span class=\"button-indicator\"></span>");
    $button.attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/sign-cc-timesheet",
        type: "POST",
        data: {
            startDayAndHour: startDayAndHour
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to sign timesheet: ' + $(".reason", data).html());
        } else {
            /* Success */
            /*$("#signature-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");*/
            /*$("#status-div span").text("Complete").removeClass("incomplete-status").addClass("complete-status");*/
            leaveSpinning = true;
            window.location.reload(true);
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to sign timesheet: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to sign timesheet: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        if (!leaveSpinning) {
            $button.html("Sign");
            $button.removeAttr("disabled");
        }
    });
};

jlab.btm.editHallHours = function ($table, $button, saveAll) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    if (!jlab.btm.validateHallHourForm()) {
        return;
    }

    if (!saveAll && !jlab.btm.validateSaveFutureShift()) {
        return;
    }

    var hourArray = [],
        upArray = [],
        tuneArray = [],
        bnrArray = [],
        downArray = [],
        offArray = [],
        hall = $table.attr("data-hall"),
        success = false;

    $table.find("tbody tr").each(function () {
        var $row = $(this),
            units = $("#units").attr("data-units"),
            hour = $row.find("th").attr("data-hour"),
            up = jlab.btm.parseSeconds($row.find("td:nth-child(2) input").val(), units),
            tune = jlab.btm.parseSeconds($row.find("td:nth-child(3) input").val(), units),
            bnr = jlab.btm.parseSeconds($row.find("td:nth-child(4) input").val(), units),
            down = jlab.btm.parseSeconds($row.find("td:nth-child(5) input").val(), units),
            off = jlab.btm.parseSeconds($row.find("td:nth-child(6) input").val(), units);

        hourArray.push(hour);
        upArray.push(up);
        tuneArray.push(tune);
        bnrArray.push(bnr);
        downArray.push(down);
        offArray.push(off);
    });

    jlab.requestStart();

    $button.html("<span class=\"button-indicator\"></span>");
    $button.attr("disabled", "disabled");
    $button.next().attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-hall-hours",
        type: "POST",
        data: {
            'hour[]': hourArray,
            'up[]': upArray,
            'tune[]': tuneArray,
            'bnr[]': bnrArray,
            'down[]': downArray,
            'off[]': offArray,
            hall: hall
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            $("#hourly-tab-li a").click();
            var $h3 = $table.closest(".ui-accordion-content").prev("h3");
            jlab.btm.showAccordionPanel($h3);
            location.hash = '#' + $h3.attr("id");
            alert('Unable to save hall ' + hall + ' availability hours: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveHourTableSuccess($table, $button);
            jlab.btm.copyHallShiftTotalToSummary(hall, $table);
            jlab.btm.updateComparisonStatus(hall);
            success = true;
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save hall ' + hall + ' availability hours: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save hall ' + hall + ' availability hours: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $button.html("Save");
        $button.removeAttr("disabled");
        $button.next().removeAttr("disabled");

        if (success) {
            $("#hall-" + hall.toLowerCase() + "-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");
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

jlab.btm.editHallHour = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var $saveButton = $(this),
        $row = $saveButton.closest("tr"),
        $table = $row.closest("table");

    var hourArray = [],
        upArray = [],
        tuneArray = [],
        bnrArray = [],
        downArray = [],
        offArray = [],
        hall = $table.attr("data-hall"),
        success = false;

    var units = $("#units").attr("data-units"),
        hour = $row.find("th").attr("data-hour"),
        up = jlab.btm.parseSeconds($row.find("td:nth-child(2) input").val(), units),
        tune = jlab.btm.parseSeconds($row.find("td:nth-child(3) input").val(), units),
        bnr = jlab.btm.parseSeconds($row.find("td:nth-child(4) input").val(), units),
        down = jlab.btm.parseSeconds($row.find("td:nth-child(5) input").val(), units),
        off = jlab.btm.parseSeconds($row.find("td:nth-child(6) input").val(), units);

    hourArray.push(hour);
    upArray.push(up);
    tuneArray.push(tune);
    bnrArray.push(bnr);
    downArray.push(down);
    offArray.push(off);

    jlab.requestStart();

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-hall-hours",
        type: "POST",
        data: {
            'hour[]': hourArray,
            'up[]': upArray,
            'tune[]': tuneArray,
            'bnr[]': bnrArray,
            'down[]': downArray,
            'off[]': offArray,
            hall: hall
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            $("#hourly-tab-li a").click();
            var $h3 = $table.closest(".ui-accordion-content").prev("h3");
            jlab.btm.showAccordionPanel($h3);
            location.hash = '#' + $h3.attr("id");
            alert('Unable to save hall ' + hall + ' availability hours: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveHourRowSuccess($row, $saveButton);
            jlab.btm.copyHallShiftTotalToSummary(hall, $table);

            var complete = $table.find(".source-td:contains('EPICS')").length === 0;

            if (complete) {
                jlab.btm.updateComparisonStatus(hall);
                $("#hall-" + hall.toLowerCase() + "-status-value").text("Complete").removeClass("incomplete-status").addClass("complete-status");
            }

            success = true;
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save hall ' + hall + ' availability hours: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save hall ' + hall + ' availability hours: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
    });
};

jlab.btm.editShiftInfo = function (saveAll) {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    if (!jlab.btm.validateShiftInfoForm()) {
        return;
    }

    if (!saveAll && !jlab.btm.validateSaveFutureShift()) {
        return;
    }

    var startDayAndHour = $("#shift-start-hour").val(),
        crewChief = $("#crew-chief").val(),
        operators = $("#operators").val(),
        program = $("#program").val(),
        programDeputy = $("#program-deputy").val(),
        comments = $("#comments").val(),
        success = false;

    jlab.requestStart();

    $("#save-shift-info-button").html("<span class=\"button-indicator\"></span>");
    $("#save-shift-info-button").attr("disabled", "disabled");
    $("#cancel-shift-info-button").attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-cc-shift-info",
        type: "POST",
        data: {
            startDayAndHour: startDayAndHour,
            crewChief: crewChief,
            operators: operators,
            program: program,
            programDeputy: programDeputy,
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

jlab.btm.editCrossComment = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var startDayAndHour = $("#shift-start-hour").val(),
        comments = $("#edit-cross-check-comments").val(),
        success = false;

    jlab.requestStart();

    $("#save-cross-comment-button").height($("#save-cross-comment-button").height());
    $("#save-cross-comment-button").width($("#save-cross-comment-button").width());
    $("#save-cross-comment-button").html("<span class=\"button-indicator\"></span>");
    $("#save-cross-comment-button").attr("disabled", "disabled");
    $("#cancel-cross-comment-button").attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-cross-check-comment",
        type: "POST",
        data: {
            startDayAndHour: startDayAndHour,
            comments: comments
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to save shift information: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveCrossCheckCommentSuccess();
            success = true;
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save cross check comments: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save cross check comments: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $("#save-cross-comment-button").html("Save");
        $("#save-cross-comment-button").removeAttr("disabled");
        $("#cancel-cross-comment-button").removeAttr("disabled");
    });
};

jlab.btm.editReviewerComment = function () {
    if (jlab.isRequest()) {
        window.console && console.log("Ajax already in progress");
        return;
    }

    var startDayAndHour = $("#shift-start-hour").val(),
        comments = $("#edit-reviewer-comments").val(),
        success = false;

    jlab.requestStart();

    $("#save-reviewer-comment-button").height($("#save-cross-comment-button").height());
    $("#save-reviewer-comment-button").width($("#save-cross-comment-button").width());
    $("#save-reviewer-comment-button").html("<span class=\"button-indicator\"></span>");
    $("#save-reviewer-comment-button").attr("disabled", "disabled");
    $("#cancel-reviewer-comment-button").attr("disabled", "disabled");

    var request = jQuery.ajax({
        url: "/btm/ajax/edit-reviewer-comment",
        type: "POST",
        data: {
            startDayAndHour: startDayAndHour,
            comments: comments
        },
        dataType: "html"
    });

    request.done(function (data) {
        if ($(".status", data).html() !== "Success") {
            alert('Unable to save reviewer comment: ' + $(".reason", data).html());
        } else {
            /* Success */
            jlab.btm.doSaveReviewerCommentSuccess();
            success = true;
        }
    });

    request.fail(function (xhr, textStatus) {
        window.console && console.log('Unable to save reviewer comments: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
        alert('Unable to save reviewer comments: server did not handle request');
    });

    request.always(function () {
        jlab.requestEnd();
        $("#save-reviewer-comment-button").html("Save");
        $("#save-reviewer-comment-button").removeAttr("disabled");
        $("#cancel-reviewer-comment-button").removeAttr("disabled");
    });
};

jlab.btm.copyAccShiftTotalToSummary = function () {
    var $summaryRow = $("#acc-summary-table tbody tr:nth-child(3)"),
        $hourlyRow = $("#acc-hourly-table tfoot tr");

    $summaryRow.find("td:nth-child(2)").text($hourlyRow.find("th:nth-child(2)").text());
    $summaryRow.find("td:nth-child(3)").text($hourlyRow.find("th:nth-child(3)").text());
    $summaryRow.find("td:nth-child(4)").text($hourlyRow.find("th:nth-child(4)").text());
    $summaryRow.find("td:nth-child(5)").text($hourlyRow.find("th:nth-child(5)").text());
    $summaryRow.find("td:nth-child(6)").text($hourlyRow.find("th:nth-child(6)").text());
    $summaryRow.find("td:nth-child(7)").text($hourlyRow.find("th:nth-child(7)").text());
};

jlab.btm.copyHallShiftTotalToSummary = function (hall, $hourlyTable) {
    var rowIndex = 3;

    if (hall === 'B') {
        rowIndex = 6;
    } else if (hall === 'C') {
        rowIndex = 9;
    } else if (hall === 'D') {
        rowIndex = 12;
    }

    var $summaryRow = $("#hall-summary-table tbody tr:nth-child(" + rowIndex + ")"),
        $hourlyRow = $hourlyTable.find("tfoot tr");

    $summaryRow.find("td:nth-child(2)").text(($hourlyRow.find("th:nth-child(2)").text() * 1).toFixed(2) * 1);
    $summaryRow.find("td:nth-child(3)").text(($hourlyRow.find("th:nth-child(3)").text() * 1).toFixed(2) * 1);
    $summaryRow.find("td:nth-child(4)").text(($hourlyRow.find("th:nth-child(4)").text() * 1).toFixed(2) * 1);
    $summaryRow.find("td:nth-child(5)").text(($hourlyRow.find("th:nth-child(5)").text() * 1).toFixed(2) * 1);
    $summaryRow.find("td:nth-child(6)").text(($hourlyRow.find("th:nth-child(6)").text() * 1).toFixed(2) * 1);
};

jlab.btm.updateComparisonStatus = function (hall) {
    var rowIndex = 1;

    if (hall === 'B') {
        rowIndex = 2;
    } else if (hall === 'C') {
        rowIndex = 3;
    } else if (hall === 'D') {
        rowIndex = 4;
    }

    var $row = $("#comparison-table tbody tr:nth-child(" + rowIndex + ")");

    $row.find("td:nth-child(3)").text('Complete').removeClass("ui-state-highlight");
};

jlab.btm.copyMultiShiftTotalToSummary = function () {
    var $summaryRow = $("#multi-summary-table tbody tr:nth-child(2)"),
        $hourlyRow = $("#multiplicity-hourly-table tfoot tr");

    $summaryRow.find("td:nth-child(2)").text($hourlyRow.find("th:nth-child(2)").text());
    $summaryRow.find("td:nth-child(3)").text($hourlyRow.find("th:nth-child(3)").text());
    $summaryRow.find("td:nth-child(4)").text($hourlyRow.find("th:nth-child(4)").text());
    $summaryRow.find("td:nth-child(5)").text($hourlyRow.find("th:nth-child(5)").text());
    $summaryRow.find("td:nth-child(6)").text($hourlyRow.find("th:nth-child(6)").text());
    $summaryRow.find("td:nth-child(7)").text($hourlyRow.find("th:nth-child(7)").text());
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
    });

    $form.find(".li-value span").show();
    $form.find(".li-value input").hide();

    $("#shift-info-source-value").text("DB");
    $("#view-epics-shift-info-link").show();
};

jlab.btm.validateHourTableRowTotal = function ($table) {
    var tableType = $table.attr("data-type"),
        units = $("#units").attr("data-units");

    if (tableType === 'hall') {
        $table.find("tbody tr").each(function () {
            jlab.btm.validateAndUpdateHallRowTotal($(this), units);
        });
    } else if (tableType === 'acc') {
        $table.find("tbody tr").each(function () {
            jlab.btm.validateAndUpdateAccRowTotal($(this), units);
        });
    } else if (tableType === 'multi') {
        $table.find("tbody tr").each(function () {
            jlab.btm.validateMultiRow($(this), units);
        });
    }
};

jlab.btm.resetAccHourlyValidation = function () {
    $("#acc-hourly-table tbody tr").each(function () {
        jlab.btm.validateAndUpdateAccRowTotal($(this), $("#units").attr("data-units"));
    });
};

jlab.btm.doSaveAll = function () {
    if ($("#accelerator-status-value").hasClass('incomplete-status')) {
        jlab.btm.editAccHours(true);
        return;
    }
    var halls = ['a', 'b', 'c', 'd'];
    for (var i = 0; i < halls.length; i++) {
        if ($("#hall-" + halls[i] + "-status-value").hasClass('incomplete-status')) {
            var $table = $("#hall-" + halls[i] + "-hourly-table"),
                $button = $("#hall-" + halls[i] + "-save-button");
            jlab.btm.editHallHours($table, $button, true);
            return;
        }
    }
    if ($("#multiplicity-status-value").hasClass('incomplete-status')) {
        jlab.btm.editMultiHours(true);
        return;
    }
    if ($("#shift-status-value").hasClass('incomplete-status')) {
        jlab.btm.editShiftInfo(true);
        return;
    }

    jlab.btm.signTimesheet();
};

jlab.btm.doSaveCrossCheckCommentSuccess = function () {
    var $cancelButton = $("#cancel-cross-comment-button"),
        $saveButton = $cancelButton.prev(),
        $editButton = $cancelButton.prev().prev();

    $editButton.show();
    $saveButton.hide();
    $cancelButton.hide();
    $("#view-cross-check-comments").text($("#edit-cross-check-comments").val());

    $("#view-cross-check-comments").show();
    $("#edit-cross-check-comments").hide();
};

$(document).on("click", "#edit-cross-comment-button", function () {
    var $editButton = $(this),
        $saveButton = $(this).next(),
        $cancelButton = $(this).next().next();

    $editButton.hide();
    $saveButton.show();
    $cancelButton.show();
    $("#view-cross-check-comments").hide();
    $("#edit-cross-check-comments").show();
});

$(document).on("click", "#cancel-cross-comment-button", function () {
    var $cancelButton = $(this),
        $saveButton = $(this).prev(),
        $editButton = $(this).prev().prev();

    $editButton.show();
    $saveButton.hide();
    $cancelButton.hide();
    $("#view-cross-check-comments").show();
    $("#edit-cross-check-comments").hide();

    $("#edit-cross-check-comments").val($("#view-cross-check-comments").text());
});

$(document).on("click", "#save-cross-comment-button", function () {
    jlab.btm.editCrossComment();
});

jlab.btm.doSaveReviewerCommentSuccess = function () {
    var $cancelButton = $("#cancel-reviewer-comment-button"),
        $saveButton = $cancelButton.prev(),
        $editButton = $cancelButton.prev().prev();

    $editButton.show();
    $saveButton.hide();
    $cancelButton.hide();
    $("#view-reviewer-comments").text($("#edit-reviewer-comments").val());

    $("#view-reviewer-comments").show();
    $("#edit-reviewer-comments").hide();
};

$(document).on("click", "#edit-reviewer-comment-button", function () {
    var $editButton = $(this),
        $saveButton = $(this).next(),
        $cancelButton = $(this).next().next();

    $editButton.hide();
    $saveButton.show();
    $cancelButton.show();
    $("#view-reviewer-comments").hide();
    $("#edit-reviewer-comments").show();
});

$(document).on("click", "#cancel-reviewer-comment-button", function () {
    var $cancelButton = $(this),
        $saveButton = $(this).prev(),
        $editButton = $(this).prev().prev();

    $editButton.show();
    $saveButton.hide();
    $cancelButton.hide();
    $("#view-reviewer-comments").show();
    $("#edit-reviewer-comments").hide();

    $("#edit-reviewer-comments").val($("#view-reviewer-comments").text());
});

$(document).on("click", "#save-reviewer-comment-button", function () {
    jlab.btm.editReviewerComment();
});

$(document).on("click", "#edit-shift-info-button", function () {
    var $editButton = $(this),
        $saveButton = $(this).next(),
        $cancelButton = $(this).next().next(),
        $form = $(this).closest(".form-wrapper");

    $editButton.hide();
    $saveButton.show();
    $cancelButton.show();
    $form.find(".li-value span").hide();
    $form.find(".li-value input").show();
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
    $form.find(".li-value input").hide();

    $form.find(".li-value input").each(function () {
        $(this).val($(this).prev().text());
    });
});

$(document).on("click", "#reload-page", function () {
    window.location.href = '#comparison';
    window.location.reload(false);
    return false;
});

$(document).on("click", "#save-shift-info-button", function () {
    jlab.btm.editShiftInfo();
});

$(document).on("click", "#sign-button", function () {
    if (!jlab.btm.validateSaveFutureShift()) {
        return;
    }

    var $button = $("#sign-button");
    $button.html("<span class=\"button-indicator\"></span>");
    $button.attr("disabled", "disabled");

    jlab.btm.validateCrossCheckStatusThenSave();
});

$(document).on("click", "#save-acc-button", function () {
    jlab.btm.editAccHours();
});

$(document).on("click", ".save-hall-button", function () {
    jlab.btm.editHallHours($(this).closest(".accordion-table-wrapper").find("table"), $(this));
});

$(document).on("click", "#save-multiplicity-button", function () {
    jlab.btm.editMultiHours();
});

$(document).on("click", "#acc-hourly-table .ui-icon-check", function () {
    jlab.btm.editAccHour.call(this);
});

$(document).on("click", ".hall-hourly-table .ui-icon-check", function () {
    jlab.btm.editHallHour.call(this);
});

$(document).on("click", "#multiplicity-hourly-table .ui-icon-check", function () {
    jlab.btm.editMultiHour.call(this);
});

$(document).on("click", ".hour-edit-button", function () {
    var $editButton = $(this),
        $saveButton = $(this).next(),
        $cancelButton = $(this).next().next(),
        $table = $(this).closest(".accordion-table-wrapper").find("table");

    $editButton.hide();
    $saveButton.show();
    $cancelButton.show();
    $table.find("tbody span").hide();
    $table.find("input").show();
});

$(document).on("click", ".hour-cancel-button", function () {
    var $cancelButton = $(this),
        $saveButton = $(this).prev(),
        $editButton = $(this).prev().prev(),
        $table = $(this).closest(".accordion-table-wrapper").find("table");

    $table.find(".ui-icon-pencil").css('display', 'inline-block');
    $table.find(".ui-icon-close, .ui-icon-check").hide();

    $editButton.show();
    $saveButton.hide();
    $cancelButton.hide();
    $table.find("tbody td span").show();
    $table.find("input").hide();

    $table.find("input").each(function () {
        $(this).val($(this).prev().text());
    });
    jlab.btm.validateHourTableRowTotal($table);
    jlab.btm.updateAllDurationColumnTotals($table);
});

$(document).on("change", "#acc-hourly-table input[type=text]", function () {
    jlab.btm.validateAndUpdateAccRowTotal($(this).closest("tr"), $("#units").attr("data-units"));
    jlab.btm.updateColumnTotal($(this).closest("td"));
});

$(document).on("change", ".hall-hourly-table input[type=text]", function () {
    jlab.btm.validateAndUpdateHallRowTotal($(this).closest("tr"), $("#units").attr("data-units"));
    jlab.btm.updateColumnTotal($(this).closest("td"));
});

$(document).on("change", "#multiplicity-hourly-table input[type=text]", function () {
    jlab.btm.validateMultiRow($(this).closest("tr"), $("#units").attr("data-units"));
    jlab.btm.updateColumnTotal($(this).closest("td"));
});

$(document).on("click", "#view-epics-shift-info-link", function () {
    $("#shift-info-dialog").dialog("open");
    return false;
});

$(document).on("click", "#comparison-table .ui-state-error", function () {
    /*$("#cross-check-section").tabs("option", "active", 2);*/
    $('#cross-check-section a[href="#cross-check-details-tab"]')[0].click();
});

$(document).on("click", "#dtm-btm-table .ui-state-error", function () {
    var startFmt = $("#comparison-table").attr("data-start"),
        endFmt = $("#comparison-table").attr("data-end"),
        url = '/dtm/reports/event-downtime?transport='
            + '&start=' + encodeURIComponent(startFmt)
            + '&end=' + encodeURIComponent(endFmt)
            + '&type=1'
            + '&chart=table'
            + '&data=downtime'
            + '&qualified=';

    window.open(url);
});

$(function () {
    jlab.btm.validateHourTableRowTotal($("#acc-hourly-table"));
    $(".hall-hourly-table").each(function () {
        jlab.btm.validateHourTableRowTotal($(this));
    });
});