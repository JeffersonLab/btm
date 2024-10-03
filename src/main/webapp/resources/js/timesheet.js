var jlab = jlab || {};
jlab.btm = jlab.btm || {};

$(document).on("click", "#filter-form-submit-button", function () {
    if ($("#date").val() === '') {
        return false;
    }

    var date = new Date($("#date").val()),
        urlMonthNum = date.getMonth(),
        urlMonth = jlab.TRI_CHAR_MONTH[urlMonthNum],
        urlDay = (date.getDate() < 10 ? '0' + date.getDate() : date.getDate()),
        urlDate = urlDay + "-" + urlMonth + "-" + date.getFullYear();

    window.location.href = jlab.contextPath + '/timesheet/' + encodeURIComponent($("#type").val()) + '/' + encodeURIComponent(urlDate) + '/' + encodeURIComponent($("#shift").val()) + '/' + encodeURIComponent($("#units").val());
    return false;
});

$(document).on("click", "#status-label", function () {
    $("#status-dialog").dialog("open");
    return false;
});

jlab.btm.parseSeconds = function (duration, units) {
    if (duration === '') {
        return 0;
    } else if (units === 'SECONDS') {
        return duration;
    } else if (units === 'MINUTES') {
        return Math.round(duration * 60);
    } else {
        return Math.round(duration * 3600);
    }
};

jlab.btm.showAccordionPanel = function (h3) {
    $(h3).removeClass("ui-state-default ui-corner-bottom").addClass("ui-accordion-header-active ui-state-active")
        .find("> .ui-icon").removeClass("ui-icon-triangle-1-e").addClass("ui-icon-triangle-1-s").end()
        .next().addClass("ui-accordion-content-active").show();
};

jlab.btm.resetAccordion = function () {
    $(".accordion h3").removeClass("ui-accordion-header-active ui-state-active").addClass("ui-state-default ui-corner-bottom")
        .find("> .ui-icon").removeClass("ui-icon-triangle-1-s").addClass("ui-icon-triangle-1-e").end()
        .next().removeClass("ui-accordion-content-active").hide();

    jlab.btm.showAccordionPanel($(".accordion h3.initially-open-header"));
};

jlab.btm.doSaveHourRowSuccess = function ($row, $saveButton) {
    var $cancelButton = $saveButton.next(),
        $editButton = $saveButton.prev();

    $editButton.css('display', 'inline-block');
    $saveButton.hide();
    $cancelButton.hide();

    $row.find("td span").each(function () {
        var newValue = $(this).next().val();

        newValue = (newValue === '') ? 0 : newValue * 1;

        $(this).text(newValue);
    });

    $row.find("td span").show();
    $row.find("input").hide();

    $row.find(".source-td").text("DB");
};

jlab.btm.doSaveHourTableSuccess = function ($table, $saveButton, skipLast) {
    var $cancelButton = $saveButton.next(),
        $editButton = $saveButton.prev();

    $editButton.show();
    $saveButton.hide();
    $cancelButton.hide();

    $table.find("tbody span").each(function () {
        var newValue = $(this).next().val();

        newValue = (newValue === '') ? 0 : newValue * 1;

        $(this).text(newValue);
    });

    $table.find(".ui-icon-pencil").css('display', 'inline-block');
    $table.find(".ui-icon-close, .ui-icon-check").hide();

    $table.find("tbody td span").show();
    $table.find("input").hide();

    let selector = "tbody tr";

    if(skipLast) {
        selector = "tbody tr:not(:last-child)";
    }

    $table.find(selector).each(function () {
        $(this).find(".source-td").text("DB");
    });
};

jlab.btm.validateSaveFutureShift = function () {
    var now = new Date(),
        year = $("#endOfShift").attr("data-year"),
        month = $("#endOfShift").attr("data-month") - 1, /*Javascript starts at zero / Java starts at 1*/
        day = $("#endOfShift").attr("data-day"),
        hour = $("#endOfShift").attr("data-hour"),
        endOfCurrentShift = new Date(year, month, day, hour, 0, 0, 0),
        result = true;
    /*console.log('now: ' + now);
     console.log('endOfCurrentShift: ' + endOfCurrentShift);*/
    if (now.getTime() < endOfCurrentShift.getTime()) {
        result = confirm('The shift covered by this timesheet has not ended yet (is this the correct timesheet?).  Are you sure you want to save?');
    }

    return result;
};

$(document).on("click", ".ui-icon-pencil", function () {
    var $editButton = $(this),
        $saveButton = $(this).next(),
        $cancelButton = $(this).next().next(),
        $row = $(this).closest("tr");

    $editButton.hide();
    $saveButton.css('display', 'inline-block');
    $cancelButton.css('display', 'inline-block');
    $row.find("td span").hide();
    $row.find("input").show();
});

$(document).on("click", ".ui-icon-close", function () {
    var $cancelButton = $(this),
        $saveButton = $(this).prev(),
        $editButton = $(this).prev().prev(),
        $row = $(this).closest("tr"),
        $table = $row.closest("table");

    $editButton.css('display', 'inline-block');
    $saveButton.hide();
    $cancelButton.hide();
    $row.find("td span").show();
    $row.find("input").hide();

    $row.find("input").each(function () {
        $(this).val($(this).prev().text());
    });
    jlab.btm.validateHourTableRowTotal($table);
    jlab.btm.updateAllDurationColumnTotals($table);
});

jlab.btm.updateColumnTotal = function ($td) {
    var index = $td.parent().children().index($td),
        $tdList = $td.closest("tbody").find("tr td:nth-child(" + (index + 1) + ")"),
        total = 0;

    $tdList.each(function () {
        total = total + $(this).find("input").val() * 1;
    });

    total = total.toFixed(3) * 1;

    $td.closest("table").find("tfoot th:nth-child(" + (index + 1) + ")").text(total);
}

jlab.btm.updateAllDurationColumnTotals = function ($table) {
    $table.find("tbody tr:first-child td input").each(function () {
        jlab.btm.updateColumnTotal($(this).closest("td"));
    });
};

$(function () {
    $(".tabset").tabs();

    $(".datepicker").datepicker({
        dateFormat: 'dd M yy'
    });

    $(".accordion")
        .addClass("ui-accordion ui-accordion-icons ui-widget ui-helper-reset")
        .find("h3")
        .addClass("ui-accordion-header ui-helper-reset ui-state-default ui-corner-top ui-corner-bottom")
        .hover(function () {
            $(this).toggleClass("ui-state-hover");
        })
        .click(function () {
            $(this).toggleClass("ui-accordion-header-active ui-state-active ui-state-default ui-corner-bottom")
                .find("> .ui-icon").toggleClass("ui-icon-triangle-1-e ui-icon-triangle-1-s").end()
                .next().toggleClass("ui-accordion-content-active").slideToggle();
            return false;
        })
        .next()
        .addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom")
        .hide();

    $(".accordion").find("h3").prepend('<span class="ui-icon ui-icon-triangle-1-e"></span>');

    jlab.btm.resetAccordion();

    $("#shift-info-dialog").dialog({
        autoOpen: false,
        height: 400,
        width: 500,
        minHeight: 400,
        minWidth: 500
    });

    $("#status-dialog").dialog({
        autoOpen: false,
        height: 400,
        width: 550,
        minHeight: 400,
        minWidth: 550
    });

    $("#timesheet-body").show();
});