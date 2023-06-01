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