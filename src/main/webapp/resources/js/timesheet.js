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

    window.location.href = jlab.contextPath + '/timesheet/' + $("#type").val() + '/' + urlDate + '/' + $("#shift").val() + '/' + $("#units").val();
    return false;
});

$(document).on("click", "#status-label", function () {
    $("#status-dialog").dialog("open");
    return false;
});

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
        width: 500,
        minHeight: 400,
        minWidth: 500
    });
});