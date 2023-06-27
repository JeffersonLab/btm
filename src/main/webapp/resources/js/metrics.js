var jlab = jlab || {};
jlab.btm = jlab.btm || {};
$(function () {
    $(".tabset").tabs({
        beforeActivate: function (event, ui) {
            window.location.hash = ui.newPanel.selector;
        }
    });

    $("#metrics-body").show();
}); 