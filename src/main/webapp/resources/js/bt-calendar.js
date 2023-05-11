var monthNames = ["January", "February", "March", "April",
    "May", "June", "July", "August", "September",
    "October", "November", "December"];
setMonthPicker = function () {
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
};

checkShift = function ($queue, index) {

    var $anchor = $queue.eq(index);

    if ($anchor.length > 0) {

        var $cell = $anchor.closest(".detail-holder-cell"),
            $dialogReasonList = $cell.find(".reason-list"),
            url = $anchor.attr("href"),
            shift = "Owl";

        if (url.indexOf("day") >= 0) {
            shift = "Day";
        } else if (url.indexOf("swing") >= 0) {
            shift = "Swing";
        }

        $anchor.addClass("button-indicator");
        $anchor.text("");

        var request = jQuery.ajax({
            url: url,
            type: "GET",
            data: {
                crosscheck: 'Y'
            },
            dataType: "html"
        });

        request.done(function (data) {
            var signed = $("#cross-check-summary-tab", data).attr("data-signature") === "true",
                validationErrors = !signed ||
                    $("#comparison-table td:last-child.ui-state-error", data).length > 0
                    || $("#dtm-btm-table td:last-child.ui-state-error", data).length > 0;
            var review = $("#view-reviewer-comments", data).text(),
                reviewed = review.length > 0;
            if (validationErrors) {
                if (reviewed) {
                    $anchor.text("*");
                    $anchor.attr("title", review);
                } else {
                    $anchor.text("X");
                    $cell.addClass("bad-data-day");

                    var shiftReasons = $(".reason-list li", data);

                    if (!signed) {
                        $dialogReasonList.append($("<li>" + shift + ": Crew Chief Signature Missing</li>"));
                    }

                    shiftReasons.each(function () {
                        $(this).prepend(shift + ": ");
                    });

                    $dialogReasonList.append(shiftReasons);
                }
            } else {
                $anchor.text("✔");
            }

            checkShift($queue, index + 1);
        });

        request.error(function (xhr, textStatus) {
            window.console && console.log('Unable to query cross checks: Text Status: ' + textStatus + ', Ready State: ' + xhr.readyState + ', HTTP Status Code: ' + xhr.status);
            $anchor.text("?");
        });

        request.always(function () {
            $anchor.removeClass("button-indicator");
        });
    }
};

doChecks = function () {

    var $queue = $("#calendar-table .shift-check"),
        index = 0;

    checkShift($queue, index);
};
$(document).on("click", ".date-link", function () {

    var $cell = $(this).closest(".detail-holder-cell"),
        $dialog = $cell.find(".reason-dialog");

    $dialog.dialog({
        width: 500,
        height: 250,
        modal: true,
        autoOpen: false,
        close: function () {
            $dialog.dialog("destroy");
        }
    });

    $dialog.dialog("open");

    return false;
});
$(function () {
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
        },
        beforeShow: function (input, inst) {
            setMonthPicker.call(this);
        }
    });

    doChecks();
});