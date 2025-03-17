jlab.addAxisLabels = function () {
    jlab.addXAxisLabel("Hall");
    jlab.addYAxisLabel("Percent");
};

jlab.addTooltips = function () {
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");

    $("#chart-placeholder").bind("plothover", function (event, pos, item) {

        if (item) {
            var x = item.datapoint[0].toFixed(2) * 1,
                y = item.datapoint[1].toFixed(2) * 1,
                label = '',
                grouping = '';

            if (item.series.label !== '') {
                label = " {" + item.series.label + "}";
            }

            if (x < 0.5) {
                grouping = ''; /*Any Up*/
            } else if (x < 1.5) {
                grouping = 'Hall A';
            } else if (x < 2.5) {
                grouping = 'Hall B';
            } else if (x < 3.5) {
                grouping = 'Hall C';
            } else if (x < 4.5) {
                grouping = 'Hall D';
            }

            $("#tooltip").html(grouping + " " + label + " (" + y + "%)")
                .css({top: item.pageY - 30, left: item.pageX + 5})
                .fadeIn(200);
        } else {
            $("#tooltip").stop().hide();
        }
    });
};

doBarChart = function () {
    var aaSeries = [],
        eaSeries = [],
        saSeries = [],
        colors = ['white', 'blue', 'red', 'green'];


    $(".chart-legend tbody tr").each(function (index, value) {

        $(this).find("th:first-child .color-box").css("background-color", colors[index]);

        if (index === 0) { // HEADER

        } else if (index === 1) { // AA
            var a = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, '')),
                b = parseFloat($("td:nth-child(4)", value).text().replace(/,/g, '')),
                c = parseFloat($("td:nth-child(5)", value).text().replace(/,/g, '')),
                d = parseFloat($("td:nth-child(6)", value).text().replace(/,/g, ''));
            aaSeries.push([1, a]);
            aaSeries.push([2, b]);
            aaSeries.push([3, c]);
            aaSeries.push([4, d]);

        } else if (index === 2) { // EA
            var a = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, '')),
                b = parseFloat($("td:nth-child(4)", value).text().replace(/,/g, '')),
                c = parseFloat($("td:nth-child(5)", value).text().replace(/,/g, '')),
                d = parseFloat($("td:nth-child(6)", value).text().replace(/,/g, ''));
            eaSeries.push([1, a]);
            eaSeries.push([2, b]);
            eaSeries.push([3, c]);
            eaSeries.push([4, d]);
        } else if (index === 3) { // SA
            var a = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, '')),
                b = parseFloat($("td:nth-child(4)", value).text().replace(/,/g, '')),
                c = parseFloat($("td:nth-child(5)", value).text().replace(/,/g, '')),
                d = parseFloat($("td:nth-child(6)", value).text().replace(/,/g, ''));
            saSeries.push([1, a]);
            saSeries.push([2, b]);
            saSeries.push([3, c]);
            saSeries.push([4, d]);
        }
    });

    var ds = [
        {
            label: 'AA',
            color: colors[1],
            data: aaSeries,
            bars: {
                order: 1
            }
        },
        {
            label: 'EA',
            color: colors[2],
            data: eaSeries,
            bars: {
                order: 2
            }
        },
        {
            label: 'SA',
            color: colors[3],
            data: saSeries,
            bars: {
                order: 3
            }
        }
    ];

    var orderOffset = -0.1;

    $(".chart-wrap").addClass("has-y-axis-label").addClass("has-x-axis-label");

    jlab.flotplot = $.plot($("#chart-placeholder"), ds, {
        series: {
            bars: {
                show: true,
                align: 'center',
                barWidth: 0.2,
                fill: 1
            }
        },
        xaxis: {
            ticks: [[1 + orderOffset, 'Hall A'], [2 + orderOffset, 'Hall B'], [3 + orderOffset, 'Hall C'], [4 + orderOffset, 'Hall D']],
            tickLength: 0,
            min: 0.4,
            max: 4.4
        },
        yaxis: {
            min: 0,
            max: 100,
            ticks: 5,
            tickDecimals: 0
        },
        grid: {
            borderWidth: 1,
            borderColor: 'gray',
            hoverable: true,
            backgroundColor: {colors: ["#fff", "#eee"]}
        },
        legend: {
            show: false
        }
    });

    jlab.addAxisLabels();
    jlab.addTooltips();
};
$(function () {
    var chart = $("#availability-chart").val();

    if (chart === 'table') {
        /*Do nothing*/
    } else {
        doBarChart();
    }

    $("#explanation-link").button({
        icons: {
            secondary: "ui-icon-extlink"
        }
    });
    jlab.fakeDecimalAlign($(".chart-legend tr:not(.sub-head-row)"), ["td:nth-child(3)", "td:nth-child(4)", "td:nth-child(5)", "td:nth-child(6)"]);
});