jlab = jlab || {};

jlab.series = {
    charge: {
        delivered: {
            a: {data: []},
            b: {data: []},
            c: {data: []},
            d: {data: []}
        }
    },
    current: {
        delivered: {
            a: {data: []},
            b: {data: []},
            c: {data: []},
            d: {data: []}
        }
    },
    /* Each scheduled day's hall programs: {x: day start in local milliseconds, a: ..., b: ..., c: ..., d: ...} */
    programs: []
};

jlab.hallCurrentData = [
    {
        label: 'Delivered',
        color: 'rgb(169,169,169, 0.5)',
        data: [],
        points: {show: false},
        lines: {show: true, steps: true, lineWidth: 1},
        dashes: {
            show: false,
            dashLength: 10
        },
        shadowSize: 0
    }
];

/* Create hall current data by deep copying template */
jlab.currentDataA = JSON.parse(JSON.stringify(jlab.hallCurrentData));
jlab.currentDataA[0].data = jlab.series.current.delivered.a.data;

jlab.currentDataB = JSON.parse(JSON.stringify(jlab.hallCurrentData));
jlab.currentDataB[0].data = jlab.series.current.delivered.b.data;

jlab.currentDataC = JSON.parse(JSON.stringify(jlab.hallCurrentData));
jlab.currentDataC[0].data = jlab.series.current.delivered.c.data;

jlab.currentDataD = JSON.parse(JSON.stringify(jlab.hallCurrentData));
jlab.currentDataD[0].data = jlab.series.current.delivered.d.data;


jlab.hallChargeData = [
    {
        label: 'Delivered',
        color: 'black',
        data: [],
        lines: {show: true, lineWidth: 1}
    }
];

/* Create hall charge data by deep copying template */
jlab.chargeDataA = JSON.parse(JSON.stringify(jlab.hallChargeData));
jlab.chargeDataA[0].data = jlab.series.charge.delivered.a.data;

jlab.chargeDataB = JSON.parse(JSON.stringify(jlab.hallChargeData));
jlab.chargeDataB[0].data = jlab.series.charge.delivered.b.data;

jlab.chargeDataC = JSON.parse(JSON.stringify(jlab.hallChargeData));
jlab.chargeDataC[0].data = jlab.series.charge.delivered.c.data;

jlab.chargeDataD = JSON.parse(JSON.stringify(jlab.hallChargeData));
jlab.chargeDataD[0].data = jlab.series.charge.delivered.d.data;

jlab.addYAxisLabelExtra = function (placeholder, label) {
    var yaxisLabel = $("<div class='axis-label y-axis-label'></div>")
        .text(label)
        .appendTo(placeholder);
    yaxisLabel.css("margin-top", yaxisLabel.width() / 2);
};

jlab.addAxisLabels = function () {
    /*jlab.addXAxisLabel("Time");*/
    jlab.addYAxisLabelExtra($("#charta-charge-placeholder"), "C");
    jlab.addYAxisLabelExtra($("#chartb-charge-placeholder"), "mC");
    jlab.addYAxisLabelExtra($("#chartc-charge-placeholder"), "C");
    jlab.addYAxisLabelExtra($("#chartd-charge-placeholder"), "mC");

    jlab.addYAxisLabelExtra($("#charta-current-placeholder"), "µA");
    jlab.addYAxisLabelExtra($("#chartb-current-placeholder"), "nA");
    jlab.addYAxisLabelExtra($("#chartc-current-placeholder"), "µA");
    jlab.addYAxisLabelExtra($("#chartd-current-placeholder"), "nA");
};

jlab.toUserDateStringUtc = function (x) {
    var year = x.getUTCFullYear(),
        month = x.getUTCMonth(),
        day = x.getUTCDate();
    return jlab.triCharMonthNames[month] + ' ' + jlab.pad(day, 2) + ' ' + year;
};
jlab.toUserDateTimeStringUtc = function (x) {
    var year = x.getUTCFullYear(),
        month = x.getUTCMonth(),
        day = x.getUTCDate(),
        hour = x.getUTCHours(),
        minute = x.getUTCMinutes();
    return jlab.triCharMonthNames[month] + ' ' + jlab.pad(day, 2) + ' ' + year + ' ' + jlab.pad(hour, 2) + ':' + jlab.pad(minute, 2);
};
jlab.fetchMyaData = function (params) {

    var url = jlab.contextPath + '/myquery/interval',
        data = {
            c: params.pv,
            b: params.start,
            e: params.end
        },
        dataType = "json",
        options = {url: url, type: 'GET', data: data, dataType: dataType, timeout: 60000, cache: true};

    var promise = $.ajax(options);
    promise.done(function (json) {
        /*console.log(json);*/
        /*console.log(params.pv, ' count: ', json.count, '; downsampled: ', json.data.length);*/

        if (typeof json.datatype === 'undefined') {
            alert('PV ' + pv + ' not found');
            return;
        }

        if (!(json.datatype === 'DBR_DOUBLE' || json.datatype === 'DBR_FLOAT' || json.datatype === 'DBR_SHORT' || json.datatype === 'DBR_LONG')) {
            alert('datatype not a number: ' + json.datatype);
            return;
        }

        if (json.datasize !== 1) { /*This check is probably unnecessary since only vectors are strings*/
            alert('datasize not scalar: ' + json.datasize);
            return;
        }

            for (var i = 0; i < json.data.length; i++) {
                var record = json.data[i],
                    timestamp = record.d,
                    current = parseFloat(record.v),
                    charge = parseFloat(record.i);



                if(Number.isNaN(current)) {
                    /*console.log('ignoring NaN: ', record);*/
                } else {
                    if(params.scaler) {
                        //current = current * params.scaler;
                        charge = charge * params.scaler;
                    }

                    params.currentstore.data.push([timestamp, current]);
                    params.chargestore.data.push([timestamp, charge]);
                }
            }
    });
    return promise;
};

function reflect(promise) {
    return promise.then(
        (v) => {
        return { status: 'fulfilled', value: v };
},
    (error) => {
        return { status: 'rejected', reason: error };
    }
)
}

jlab.fetchMultiple = function (params) {
    if (params.pvs.length > 0) {
        /*$.mobile.loading("show", {textVisible: true, theme: "b"});*/

        var promises = [];

        for (var i = 0; i < params.pvs.length; i++) {
            var pv = params.pvs[i],
                currentstore = params.currentstores[i],
                chargestore = params.chargestores[i],
                scaler = params.scalers[i];

            var newParams = {};
            newParams = Object.assign(newParams, params);

            newParams.pv = pv;
            newParams.currentstore = currentstore;
            newParams.chargestore = chargestore;
            newParams.scaler = scaler;

            var promise = jlab.fetchMyaData(newParams);

            promises.push(promise);
        }

        $("#busy-wait").show();

        /* Attempting to allow all */
        Promise.all(promises).then(values => {
            $("#busy-wait").hide();

            var options = jlab.defaultHallChartOptions;

            /* Deep copy object */
            var chargeChartOptions = JSON.parse(JSON.stringify(jlab.defaultHallChartOptions));

            chargeChartOptions.xaxis.labelHeight = 1;
            /*chargeChartOptions.xaxis.show = true;*/
            /*chargeChartOptions.xaxis.reserveSpace = true;*/


            jlab.hallAChargeChart = $.plot($("#charta-charge-placeholder"), jlab.chargeDataA, chargeChartOptions);
            jlab.hallBChargeChart = $.plot($("#chartb-charge-placeholder"), jlab.chargeDataB, chargeChartOptions);
            jlab.hallCChargeChart = $.plot($("#chartc-charge-placeholder"), jlab.chargeDataC, chargeChartOptions);
            jlab.hallDChargeChart = $.plot($("#chartd-charge-placeholder"), jlab.chargeDataD, chargeChartOptions);

            options.yaxis.ticks = 2;


            jlab.hallACurrentChart = $.plot($("#charta-current-placeholder"), jlab.currentDataA, options);
            jlab.hallBCurrentChart = $.plot($("#chartb-current-placeholder"), jlab.currentDataB, options);
            jlab.hallCCurrentChart = $.plot($("#chartc-current-placeholder"), jlab.currentDataC, options);
            jlab.hallDCurrentChart = $.plot($("#chartd-current-placeholder"), jlab.currentDataD, options);


            /*console.log(jlab.currentDataC);*/

            /*for(var i = 0; i < jlab.currentDataC[1].data.length; i++) {
                console.log((new Date(jlab.currentDataC[1].data[i][0])).toISOString(), jlab.currentDataC[1].data[i][1]);
            }*/

            /*If Hall A chart is resized assume all have been and hide x axis labels again*/
            jlab.hallAChargeChart.getPlaceholder().resize(function() {
                $(".charge-placeholder .flot-x-axis").hide();
            });

            $(".charge-placeholder .flot-x-axis").hide();

            jlab.addAxisLabels();


            jlab.doTooltip();

            $("body").addClass("done");

        }).catch(error => {
            console.log("Unable to query MYA data", error);
            $("#busy-wait").hide();
            $(".error-box").text('Unable to query MYA');
        });
    }
};

jlab.initLineChart = function() {
    var start = jlab.fromFriendlyDateString($("#start").val()),
        end = jlab.fromFriendlyDateString($("#end").val()),
        nowMillis = Date.now();

    /* Querying archiver with future bounds prevents caching so we never query past most recent hour */
    if(end.getTime() > nowMillis) {
        end = new Date(nowMillis);
        end.setMinutes(0);
        end.setSeconds(0);
        end.setMilliseconds(0);
    }

    /*console.log(start, end);*/

    var params = {
        pvs: ['IBC1H04CRCUR2', 'IBC2C24CRCUR3', 'IBC3H00CRCUR4', 'IBCAD00CRCUR6'],
        currentstores: [jlab.series.current.delivered.a, jlab.series.current.delivered.b, jlab.series.current.delivered.c, jlab.series.current.delivered.d],
        chargestores: [jlab.series.charge.delivered.a, jlab.series.charge.delivered.b, jlab.series.charge.delivered.c, jlab.series.charge.delivered.d],
        scalers: [0.000001, 0.000001, 0.000001, 0.000001],
        start: jlab.toIsoDateString(start),
        end: jlab.toIsoDateString(end),
    };

    jlab.fetchMultiple(params);
};

jlab.defaultHallChartOptions = {
    legend: {
        show: false
    },
    lines: {
        lineWidth: 4
    },
    yaxis: {
        labelWidth: 38,
        tickDecimals: 0
    },
    xaxis: {
        ticks: 5,
        mode: "time",
        timeBase: "milliseconds",
        timezone: null /*UTC data (timezone-less) - converted to America/New_York server side*/
    },
    crosshair: {
        mode: 'x'
    },
    grid: {
        borderWidth: 1,
        hoverable: true,
        borderColor: 'gray',
        backgroundColor: {colors: ["#fff", "#eee"]}
    }
};

jlab.lockAll = function(item) {
    var point = {
        x: item.datapoint[0],
        y: item.datapoint[1]
    };


    jlab.hallAChargeChart.lockCrosshair(point);
    jlab.hallBChargeChart.lockCrosshair(point);
    jlab.hallCChargeChart.lockCrosshair(point);
    jlab.hallDChargeChart.lockCrosshair(point);

    jlab.hallACurrentChart.lockCrosshair(point);
    jlab.hallBCurrentChart.lockCrosshair(point);
    jlab.hallCCurrentChart.lockCrosshair(point);
    jlab.hallDCurrentChart.lockCrosshair(point);

    /*console.log(item);*/

    /*jlab.hallAChargeChart.highlight(point);
    jlab.hallBChargeChart.highlight(point);
    jlab.hallCChargeChart.highlight(point);
    jlab.hallDChargeChart.highlight(point);

    jlab.hallACurrentChart.highlight(point);
    jlab.hallBCurrentChart.highlight(point);
    jlab.hallCCurrentChart.highlight(point);
    jlab.hallDCurrentChart.highlight(point);*/
};

jlab.unlockAll = function(pos) {
    jlab.hallAChargeChart.unlockCrosshair();
    jlab.hallBChargeChart.unlockCrosshair();
    jlab.hallCChargeChart.unlockCrosshair();
    jlab.hallDChargeChart.unlockCrosshair();

    jlab.hallACurrentChart.unlockCrosshair();
    jlab.hallBCurrentChart.unlockCrosshair();
    jlab.hallCCurrentChart.unlockCrosshair();
    jlab.hallDCurrentChart.unlockCrosshair();

    jlab.hallAChargeChart.setCrosshair(pos);
    jlab.hallBChargeChart.setCrosshair(pos);
    jlab.hallCChargeChart.setCrosshair(pos);
    jlab.hallDChargeChart.setCrosshair(pos);

    jlab.hallACurrentChart.setCrosshair(pos);
    jlab.hallBCurrentChart.setCrosshair(pos);
    jlab.hallCCurrentChart.setCrosshair(pos);
    jlab.hallDCurrentChart.setCrosshair(pos);
};

jlab.cleanupAll = function() {
    jlab.hallAChargeChart.clearCrosshair();
    jlab.hallBChargeChart.clearCrosshair();
    jlab.hallCChargeChart.clearCrosshair();
    jlab.hallDChargeChart.clearCrosshair();

    jlab.hallACurrentChart.clearCrosshair();
    jlab.hallBCurrentChart.clearCrosshair();
    jlab.hallCCurrentChart.clearCrosshair();
    jlab.hallDCurrentChart.clearCrosshair();

    jlab.latestPosition = null;

    jlab.chartALegend.charge.innerHTML = '';
    jlab.chartALegend.current.innerHTML = '';

    jlab.chartBLegend.charge.innerHTML = '';
    jlab.chartBLegend.current.innerHTML = '';

    jlab.chartCLegend.charge.innerHTML = '';
    jlab.chartCLegend.current.innerHTML = '';

    jlab.chartDLegend.charge.innerHTML = '';
    jlab.chartDLegend.current.innerHTML = '';
};

jlab.hideTooltipAll = function() {
    $(".date-tooltip").hide();
    $(".program-tooltip").hide();
};

jlab.chartALegend = {
    charge: document.getElementById('key-a-charge-delivered'),
    current: document.getElementById('key-a-current-delivered')
};

jlab.chartBLegend = {
    charge: document.getElementById('key-b-charge-delivered'),
    current: document.getElementById('key-b-current-delivered')
};

jlab.chartCLegend = {
    charge: document.getElementById('key-c-charge-delivered'),
    current: document.getElementById('key-c-current-delivered')
};

jlab.chartDLegend = {
    charge: document.getElementById('key-d-charge-delivered'),
    current: document.getElementById('key-d-current-delivered')
};

jlab.updateSelected = function() {
    jlab.updateSelectedTimeout = null;

    if (jlab.latestPosition != null) {
        jlab.updatePrograms(jlab.latestPosition.x);

        jlab.updateSelectedInChart(jlab.hallAChargeChart, jlab.latestPosition, jlab.chartALegend.charge, 'C');
        jlab.updateSelectedInChart(jlab.hallACurrentChart, jlab.latestPosition, jlab.chartALegend.current, 'µA');
        jlab.updateSelectedInChart(jlab.hallBChargeChart, jlab.latestPosition, jlab.chartBLegend.charge, 'mC');
        jlab.updateSelectedInChart(jlab.hallBCurrentChart, jlab.latestPosition, jlab.chartBLegend.current, 'nA');
        jlab.updateSelectedInChart(jlab.hallCChargeChart, jlab.latestPosition, jlab.chartCLegend.charge, 'C');
        jlab.updateSelectedInChart(jlab.hallCCurrentChart, jlab.latestPosition, jlab.chartCLegend.current, 'µA');
        jlab.updateSelectedInChart(jlab.hallDChargeChart, jlab.latestPosition, jlab.chartDLegend.charge, 'mC');
        jlab.updateSelectedInChart(jlab.hallDCurrentChart, jlab.latestPosition, jlab.chartDLegend.current, 'nA');

        jlab.updateSelectedDateTime(jlab.latestPosition);
    }
};

/**
 * Finds the scheduled programs for the day containing x.
 *
 * @param x time in local milliseconds, as the charts plot it
 * @returns {object|null} the day's programs, or null if no published schedule covers that day
 */
jlab.findPrograms = function(x) {
    var days = jlab.series.programs,
        found = null;

    for (var i = 0; i < days.length && days[i].x <= x; i++) {
        found = days[i];
    }

    /* Local milliseconds have no daylight saving shifts, so every day is 24 hours long */
    if (found !== null && x >= found.x + 24 * 60 * 60 * 1000) {
        found = null;
    }

    return found;
};

jlab.updatePrograms = function(x) {
    var programs = jlab.findPrograms(x);

    ['a', 'b', 'c', 'd'].forEach(function(hall) {
        var tooltip = $("#program-tooltip-" + hall);

        if (programs === null) {
            tooltip.hide();
        } else {
            tooltip.html(programs[hall]).show();
        }
    });
};

jlab.updateSelectedDateTime = function(pos) {
    var formattedX = jlab.toUserDateTimeStringUtc(new Date(pos.x));
    $(".date-tooltip").html(formattedX).show();
};

jlab.updateSelectedInChart = function(plot, pos, legendCell, units) {
    var j, series = plot.getData()[0];

    if(series.data.length === 0) {
        return;
    }

    for (j = 0; j < series.data.length; ++j) {
        if (series.data[j][0] > pos.x) {
            break;
        }
    }

    // Now Interpolate
    var y,
        p1 = series.data[j - 1],
        p2 = series.data[j];

    if (p1 == null) {
        y = p2[1];
    } else if (p2 == null) {
        y = p1[1];
    } else if(series.lines.steps === true) {
        y = p1[1]; /*If points are stepped then don't interpolate, just grab nearest previous point*/
    } else {
        y = p1[1] + (p2[1] - p1[1]) * (pos.x - p1[0]) / (p2[0] - p1[0]);
    }
    legendCell.innerHTML = y.toFixed(2) + '' + units;
};

jlab.doTooltip = function() {
    /*$("<div id='tooltip'></div>").css({
        position: "absolute",
        display: "none",
        border: "1px solid #fdd",
        padding: "2px",
        "background-color": "#fee",
        opacity: 0.80
    }).appendTo("body");*/

    jlab.updateSelectedTimeout = null;
    jlab.latestPosition = null;

    $(".current-placeholder, .charge-placeholder").bind("plothover", function (event, pos, item) {

        /*var chartId = this.id;*/

        if (!pos.x || !pos.y) {
            return;
        }

        if (!jlab.updateSelectedTimeout) {
            jlab.updateSelectedTimeout = setTimeout(jlab.updateSelected, 200);
        }

        if (item) {
                var x = jlab.toUserDateStringUtc(new Date(item.datapoint[0])),
                    y = item.datapoint[1].toFixed(2);

                /*$("#tooltip").html(item.series.label + " " + x + " = " + y)
                    .css({top: item.pageY+5, left: item.pageX+5})
                    .fadeIn(200);*/
                jlab.lockAll(item);

                jlab.latestPosition = {x: item.datapoint[0], y: item.datapoint[1]};
        } else {
                /*$("#tooltip").hide();*/
                jlab.unlockAll(pos);
                jlab.latestPosition = pos;
        }
    });

    /*plothovercleanup*/
    $(".current-placeholder, .charge-placeholder").bind("mouseout", function (event, pos, item) {
        /*$("#tooltip").hide();*/
        jlab.cleanupAll();
        jlab.hideTooltipAll();
    });
};

$(document).on("smoothnessready", function(){
    let start, end;

    if(jlab.currentRun != null) {
        start = jlab.currentRun.start;
        end = jlab.currentRun.end;
    } else {
        let range = jlab.decodeRange('0week', false);
        start = range.start;
        end = range.end;
    }

    let defaultParams = {start: jlab.toFriendlyDateString(start), end: jlab.toFriendlyDateString(end), print: 'N', fullscreen: 'N'};
    let redirecting = jlab.initParams(defaultParams);
    if(!redirecting) {
        jlab.initLineChart();
    }
});
