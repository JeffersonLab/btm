var jlab = jlab || {};

jlab.mousePosition = {x: 0, y: 0};

$(document).on("mousemove", ".graph-panel", function (e) {
    jlab.mousePosition.x = e.clientX || e.pageX;
    jlab.mousePosition.y = e.clientY || e.pageY;
});

jlab.addPieTooltips = function ($graph) {
    $("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");

    $graph.bind("plothover", function (event, pos, item) {

        if (item) {
            var x = item.datapoint[0].toFixed(1) * 1,
                y = item.datapoint[1],
                label = item.series.label;

            $("#tooltip").html(label + " [" + y[0][1] + "] (" + x + "%)")
                .css({top: jlab.mousePosition.y - 30, left: jlab.mousePosition.x + 5})
                .fadeIn(200);
        } else {
            $("#tooltip").stop().hide();
        }
    });
};

jlab.addAxisLabels = function () {
    jlab.addXAxisLabel("Hall");
    jlab.addYAxisLabel("Hours");
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
                y = (item.datapoint[1] - item.datapoint[2]).toFixed(2) * 1, /*might be stacked*/
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

            $("#tooltip").html(grouping + " " + label + " (" + y + ")")
                .css({top: item.pageY - 30, left: item.pageX + 5})
                .fadeIn(200);
        } else {
            $("#tooltip").stop().hide();
        }
    });
};

doBarChart = function () {
    var anyUpSeries = [],
        schedSeries = [],
        upSeries = [],
        abuSeries = [],
        banuSeries = [],
        colors = ['magenta', 'white', 'blue', 'green', 'white', 'yellow', 'orange'];


    $(".chart-legend tbody tr").each(function (index, value) {

        $(this).find("th:first-child .color-box").css("background-color", colors[index]);

        if (index === 0) { // ANY UP
            var anyUp = parseFloat($("td:last-child", value).text().replace(/,/g, ''));
            anyUpSeries.push([0.85, anyUp]);

        } else if (index === 1) { // DIVIDER

        } else if (index === 2) { // SCHED
            var aSched = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, '')),
                bSched = parseFloat($("td:nth-child(4)", value).text().replace(/,/g, '')),
                cSched = parseFloat($("td:nth-child(5)", value).text().replace(/,/g, '')),
                dSched = parseFloat($("td:nth-child(6)", value).text().replace(/,/g, ''));
            schedSeries.push([1, aSched]);
            schedSeries.push([2, bSched]);
            schedSeries.push([3, cSched]);
            schedSeries.push([4, dSched]);
        } else if (index === 3) { // UP
            var aUp = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, '')),
                bUp = parseFloat($("td:nth-child(4)", value).text().replace(/,/g, '')),
                cUp = parseFloat($("td:nth-child(5)", value).text().replace(/,/g, '')),
                dUp = parseFloat($("td:nth-child(6)", value).text().replace(/,/g, ''));
            upSeries.push([1, aUp]);
            upSeries.push([2, bUp]);
            upSeries.push([3, cUp]);
            upSeries.push([4, dUp]);
        } else if (index === 4) { // DIVIDER

        } else if (index === 5) { // ABU
            var a = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, '')),
                b = parseFloat($("td:nth-child(4)", value).text().replace(/,/g, '')),
                c = parseFloat($("td:nth-child(5)", value).text().replace(/,/g, '')),
                d = parseFloat($("td:nth-child(6)", value).text().replace(/,/g, ''));
            abuSeries.push([1.02, a]);
            abuSeries.push([2.02, b]);
            abuSeries.push([3.02, c]);
            abuSeries.push([4.02, d]);
        } else if (index === 6) { // BANU
            var a = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, '')),
                b = parseFloat($("td:nth-child(4)", value).text().replace(/,/g, '')),
                c = parseFloat($("td:nth-child(5)", value).text().replace(/,/g, '')),
                d = parseFloat($("td:nth-child(6)", value).text().replace(/,/g, ''));
            banuSeries.push([1.02, a]);
            banuSeries.push([2.02, b]);
            banuSeries.push([3.02, c]);
            banuSeries.push([4.02, d]);
        }
    });

    var ds = [
        {
            label: 'Any Up',
            color: colors[0],
            data: anyUpSeries,
            bars: {
                barWidth: 0.6,
                order: 1
            }
        },
        {
            label: 'Sched.',
            color: colors[2],
            data: schedSeries,
            bars: {
                order: 2
            }
        },
        {
            label: 'UP',
            color: colors[3],
            data: upSeries,
            bars: {
                order: 3
            }
        },
        {
            label: 'ABU',
            color: colors[5],
            data: abuSeries,
            stack: true,
            bars: {
                order: 4
            }
        },
        {
            label: 'BANU',
            color: colors[6],
            data: banuSeries,
            stack: true,
            bars: {
                order: 4
            }
        }
    ];

    $("#chart-wrap").addClass("has-y-axis-label").addClass("has-x-axis-label");

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
            ticks: [[0, 'Any Up'], [1, 'Hall A'], [2, 'Hall B'], [3, 'Hall C'], [4, 'Hall D']],
            tickLength: 0,
            min: -0.34,
            max: 4.41
        },
        yaxis: {
            ticks: 4,
            tickDecimals: 0,
            min: 0
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

doPieChart = function () {
    var ds = [],
        colors = ['yellow', 'orange', 'red', 'purple', 'brown', 'lightgray'],
        $graph = $("#chart-placeholder"),
        $table = $(".chart-legend");

    $table.find("tbody tr").each(function (index, value) {

        $(this).find("th:first-child .color-box").css("background-color", colors[index]);

        var program = $("th:nth-child(2)", value).text().trim();
        /*program = program.substring(0, program.length - 1);*/
        var duration = parseFloat($("td:nth-child(3)", value).text().replace(/,/g, ''));

        ds.push({
            label: program,
            data: duration,
            color: colors[index]
        });
    });

    $("#chart-wrap").addClass("chart-wrap-backdrop");

    jlab.flotplot = $.plot($graph, ds, {
        series: {
            pie: {
                show: true,
                label: {
                    show: false
                }
            }
        },
        legend: {
            show: false
        },
        grid: {
            hoverable: true
        }
    });

    jlab.addPieTooltips($graph);
};
$(function () {
    var data = $("#physics-data").val();

    if (data === 'a') {
        doPieChart();
    } else if (data === 'b') {
        doPieChart();
    } else if (data === 'c') {
        doPieChart();
    } else if (data === 'd') {
        doPieChart();
    } else if (data === 'table') {
        /*Do nothing*/
    } else {
        doBarChart();
    }

    jlab.fakeDecimalAlign($(".chart-legend .per-hall-row, .chart-legend .total-row"), ["td:nth-child(3)", "td:nth-child(4)", "td:nth-child(5)", "td:nth-child(6)"]);
});