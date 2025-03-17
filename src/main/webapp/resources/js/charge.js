jlab = jlab || {};

jlab.series = {
    charge: {
        delivered: {
            a: {data: []},
            b: {data: []},
            c: {data: []},
            d: {data: []}
        },
        scheduled: {
            a: {data: []},
            b: {data: []},
            c: {data: []},
            d: {data: []}
        }
    },
    current:{
        delivered: {
            a: {data: []},
            b: {data: []},
            c: {data: []},
            d: {data: []}
        },
        scheduled: {
            a: {data: []},
            b: {data: []},
            c: {data: []},
            d: {data: []}
        }
    },
    programs: {
        a: [],
        b: [],
        c: [],
        d: []
    }
};

jlab.ratioData = [{
        label: 'Hall A Delivered',
        color: 'blue',
        data: jlab.series.charge.delivered.a.data,
        dashes: {
            show: true,
            lineWidth: 4,
            dashLength: 10
        }
    },
    {
        label: 'Hall A Scheduled',
        color: 'blue',
        data: jlab.series.charge.scheduled.a.data,
        lines: {show: true}
    },
    {
        label: 'Hall B Delivered',
        color: 'red',
        data: jlab.series.charge.delivered.b.data,
        dashes: {
            show: true,
            lineWidth: 4,
            dashLength: 10
        }
    },
    {
        label: 'Hall B Scheduled',
        color: 'red',
        data: jlab.series.charge.scheduled.b.data,
        lines: {show: true}
    },
    {
        label: 'Hall C Delivered',
        color: 'green',
        data: jlab.series.charge.delivered.c.data,
        dashes: {
            show: true,
            lineWidth: 4,
            dashLength: 10
        }
    },
    {
        label: 'Hall C Scheduled',
        color: 'green',
        data: jlab.series.charge.scheduled.c.data,
        lines: {show: true}
    },
    {
        label: 'Hall D Delivered',
        color: 'orange',
        data: jlab.series.charge.delivered.d.data,
        dashes: {
            show: true,
            lineWidth: 4,
            dashLength: 10
        }
    },
    {
        label: 'Hall D Scheduled',
        color: 'orange',
        data: jlab.series.charge.scheduled.d.data,
        lines: {show: true}
    }];

jlab.hallCurrentData = [
    {
        label: 'Scheduled',
        color: 'gray',
        data: [],
        lines: {show: true, steps: true}
    },
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
jlab.currentDataA[0].data = jlab.series.current.scheduled.a.data;
jlab.currentDataA[1].data = jlab.series.current.delivered.a.data;

jlab.currentDataB = JSON.parse(JSON.stringify(jlab.hallCurrentData));
jlab.currentDataB[0].data = jlab.series.current.scheduled.b.data;
jlab.currentDataB[1].data = jlab.series.current.delivered.b.data;

jlab.currentDataC = JSON.parse(JSON.stringify(jlab.hallCurrentData));
jlab.currentDataC[0].data = jlab.series.current.scheduled.c.data;
jlab.currentDataC[1].data = jlab.series.current.delivered.c.data;

jlab.currentDataD = JSON.parse(JSON.stringify(jlab.hallCurrentData));
jlab.currentDataD[0].data = jlab.series.current.scheduled.d.data;
jlab.currentDataD[1].data = jlab.series.current.delivered.d.data;


jlab.hallChargeData = [
    {
        label: 'Scheduled',
        color: 'gray',
        data: [],
        lines: {show: true},
    },
    {
    label: 'Delivered',
    color: 'black',
    data: [],
    dashes: {
        show: true,
        lineWidth: 4,
        dashLength: 10
    },
    lines: {show: true, lineWidth: 0} /*Without lines or points no plothover possible, so we use 0 width line.  Could use 0 radius point instead*/
}];

/* Create hall charge data by deep copying template */
jlab.chargeDataA = JSON.parse(JSON.stringify(jlab.hallChargeData));
jlab.chargeDataA[0].data = jlab.series.charge.scheduled.a.data;
jlab.chargeDataA[1].data = jlab.series.charge.delivered.a.data;

jlab.chargeDataB = JSON.parse(JSON.stringify(jlab.hallChargeData));
jlab.chargeDataB[0].data = jlab.series.charge.scheduled.b.data;
jlab.chargeDataB[1].data = jlab.series.charge.delivered.b.data;

jlab.chargeDataC = JSON.parse(JSON.stringify(jlab.hallChargeData));
jlab.chargeDataC[0].data = jlab.series.charge.scheduled.c.data;
jlab.chargeDataC[1].data = jlab.series.charge.delivered.c.data;

jlab.chargeDataD = JSON.parse(JSON.stringify(jlab.hallChargeData));
jlab.chargeDataD[0].data = jlab.series.charge.scheduled.d.data;
jlab.chargeDataD[1].data = jlab.series.charge.delivered.d.data;

jlab.addYAxisLabelExtra = function (placeholder, label) {
    var yaxisLabel = $("<div class='axis-label y-axis-label'></div>")
        .text(label)
        .appendTo(placeholder);
    yaxisLabel.css("margin-top", yaxisLabel.width() / 2);
};

jlab.addAxisLabels = function () {
    /*jlab.addXAxisLabel("Time");*/
    jlab.addYAxisLabel("Percent of Scheduled");
    jlab.addYAxisLabelExtra($("#charta-charge-placeholder"), "Coulombs");
    jlab.addYAxisLabelExtra($("#chartb-charge-placeholder"), "MilliCoulombs");
    jlab.addYAxisLabelExtra($("#chartc-charge-placeholder"), "Coulombs");
    jlab.addYAxisLabelExtra($("#chartd-charge-placeholder"), "MilliCoulombs");

    jlab.addYAxisLabelExtra($("#charta-current-placeholder"), "µA");
    jlab.addYAxisLabelExtra($("#chartb-current-placeholder"), "nA");
    jlab.addYAxisLabelExtra($("#chartc-current-placeholder"), "µA");
    jlab.addYAxisLabelExtra($("#chartd-current-placeholder"), "nA");
};

/**
 * Flot has built-in step function, but it doesn't add points to series exposed in API.  This function does, which makes
 * interpolation more accurate.
 *
 * @param data flot data
 * @returns {[]}
 */
jlab.stepFlotLine = function(data) {
    var out = [];
    var prev = null;

    if(data.length > 1) {
        out.push(data[0]);
        prev = data[0];
        for (let i = 1; i < data.length; i++) {
            out.push([data[i][0], prev[1]]);
            //out.push([prev[0], data[i][1]]);
            out.push(data[i]);
            prev = data[i];
        }
    }

    return out;
}
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
        options = {url: url, type: 'GET', data: data, dataType: dataType, timeout: 60000};

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

            jlab.doLineChart();
            jlab.updateKey();

            var options = jlab.defaultHallChartOptions;

            /* Deep copy object */
            var chargeChartOptions = JSON.parse(JSON.stringify(jlab.defaultHallChartOptions));

            chargeChartOptions.xaxis.labelHeight = 1;
            /*chargeChartOptions.xaxis.show = true;*/
            /*chargeChartOptions.xaxis.reserveSpace = true;*/


            var scale = $("#scale").val();

            if(scale == '') {
                scale = 1;
            }

            jlab.scaleHallScheduled(scale);





            jlab.hallAChargeChart = $.plot($("#charta-charge-placeholder"), jlab.chargeDataA, chargeChartOptions);
            jlab.hallBChargeChart = $.plot($("#chartb-charge-placeholder"), jlab.chargeDataB, chargeChartOptions);
            jlab.hallCChargeChart = $.plot($("#chartc-charge-placeholder"), jlab.chargeDataC, chargeChartOptions);
            jlab.hallDChargeChart = $.plot($("#chartd-charge-placeholder"), jlab.chargeDataD, chargeChartOptions);

            /*jlab.currentDataA[0].data = jlab.series.current.scheduled.a.data = jlab.stepFlotLine(jlab.series.current.scheduled.a.data);
            jlab.currentDataB[0].data = jlab.series.current.scheduled.b.data = jlab.stepFlotLine(jlab.series.current.scheduled.b.data);
            jlab.currentDataC[0].data = jlab.series.current.scheduled.c.data = jlab.stepFlotLine(jlab.series.current.scheduled.c.data);
            jlab.currentDataD[0].data = jlab.series.current.scheduled.d.data = jlab.stepFlotLine(jlab.series.current.scheduled.d.data);*/





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

jlab.toScientificNotationHTML = function(number, units) {
        var result = 0;

        /*result = number.toExponential(2);*/

        if(number !== undefined && number !== 0) {
            var exp = number.toExponential(1),
                tokens = exp.split("e"),
                sci = tokens[0] + "x10",
                supNum = tokens[1] * 1;

                if(supNum < 0) {  /* Negative exponent */
                    if(Math.abs(supNum) > 4) { /* 5+ zeros */
                        result = sci + '<sup>' + supNum + '</sup>' + units;
                    } else { /* 4 or less zeros */
                        result = number.toFixed(4) + units;
                    }
                } else { /* Non-negative exponent */
                    if(supNum > 4) { /* 5+ zeros */
                        result = sci + '<sup>' + supNum + '</sup>' + units;
                    } else if(supNum > 3) { /* 4 zeros */
                        result = number.toFixed(0) + units;
                    } else if(supNum > 2) { /* 3 zeros */
                        result = parseFloat(number.toFixed(1)) + units; /*parseFloat trims trailing zeros*/
                    } else if(supNum > 1) { /* 2 zeros */
                        result = parseFloat(number.toFixed(2)) + units;
                    } else if(supNum > 0) { /* 1 zero */
                        result = parseFloat(number.toFixed(3)) + units;
                    } else { /* 0 zeros */
                        result = parseFloat(number.toFixed(4)) + units;
                    }
                }
        }

        return result;
};

jlab.updateKey = function() {

    var stores = [jlab.series.charge.delivered.a, jlab.series.charge.delivered.b, jlab.series.charge.delivered.c, jlab.series.charge.delivered.d],
        units = ['C', 'mC', 'C', 'mC'];

    $(".chart-legend").find("tr:not(.sub-head-row)").each(function(i, row){
        var max = 0,
            num = 0;
        if(stores[i].data.length > 0) {
            max = stores[i].data[stores[i].data.length - 1][1],
                num = jlab.toScientificNotationHTML(max, units[i]);
        }
        $(this).find("td:nth-child(4)").html(num);
    });


    stores = [jlab.scheduledATotal, jlab.scheduledBTotal, jlab.scheduledCTotal, jlab.scheduledDTotal];
    units = ['C', 'mC', 'C', 'mC'];

    $(".chart-legend").find("tr:not(.sub-head-row)").each(function(i, row){
        var max = stores[i][1],
            num = jlab.toScientificNotationHTML(max, units[i]);
        $(this).find("td:nth-child(3)").html(num);
    });
}

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

jlab.createRatioData = function(scale) {
    var deliveredA = jlab.ratioData[0].data = [];
    var scheduledA = jlab.ratioData[1].data = [];
    var deliveredB = jlab.ratioData[2].data = [];
    var scheduledB = jlab.ratioData[3].data = [];
    var deliveredC = jlab.ratioData[4].data = [];
    var scheduledC = jlab.ratioData[5].data = [];
    var deliveredD = jlab.ratioData[6].data = [];
    var scheduledD = jlab.ratioData[7].data = [];

    var source = jlab.series.charge.scheduled.a.data;
    jlab.scheduledATotal = 0;
    if(source.length > 0) {
        jlab.scheduledATotal = [source[source.length - 1][0], source[source.length - 1][1] * scale];
    }

    source = jlab.series.charge.scheduled.b.data;
    jlab.scheduledBTotal = 0;
    if(source.length > 0) {
        jlab.scheduledBTotal = [source[source.length - 1][0], source[source.length - 1][1] * scale];
    }

    source = jlab.series.charge.scheduled.c.data;
    jlab.scheduledCTotal = 0;
    if(source.length > 0) {
        jlab.scheduledCTotal = [source[source.length - 1][0], source[source.length - 1][1] * scale];
    }

    source = jlab.series.charge.scheduled.d.data;
    jlab.scheduledDTotal = 0;
    if(source.length > 0) {
        jlab.scheduledDTotal = [source[source.length - 1][0], source[source.length - 1][1] * scale];
    }

    jlab.createRatioForSeries(jlab.series.charge.delivered.a.data, deliveredA, jlab.scheduledATotal, 1);
    jlab.createRatioForSeries(jlab.series.charge.scheduled.a.data, scheduledA, jlab.scheduledATotal, scale);
    jlab.createRatioForSeries(jlab.series.charge.delivered.b.data, deliveredB, jlab.scheduledBTotal, 1);
    jlab.createRatioForSeries(jlab.series.charge.scheduled.b.data, scheduledB, jlab.scheduledBTotal, scale);
    jlab.createRatioForSeries(jlab.series.charge.delivered.c.data, deliveredC, jlab.scheduledCTotal, 1);
    jlab.createRatioForSeries(jlab.series.charge.scheduled.c.data, scheduledC, jlab.scheduledCTotal, scale);
    jlab.createRatioForSeries(jlab.series.charge.delivered.d.data, deliveredD, jlab.scheduledDTotal, 1);
    jlab.createRatioForSeries(jlab.series.charge.scheduled.d.data, scheduledD, jlab.scheduledDTotal, scale);
};

jlab.createRatioForSeries = function(source, out, scheduledTotal, scale) {
    if(source.length > 0) {
        var totalRecord = scheduledTotal;
        //var totalRecord = source[source.length - 1];
        for(var i = 0; i < source.length; i++) {
            var record = source[i];
            //var ratio = record[1];
            //var ratio = totalRecord[1];
            var ratio = (record[1] * scale) / (totalRecord[1]) * 100;
            out.push([record[0], ratio]);
        }
    }
};

jlab.doLineChart = function () {

    /*console.log(jlab.flotData);*/

    /*if (jlab.series.charge.a.data.length === 0) {
        $('<div>No data to chart</div>').insertBefore($("#chart-placeholder"));
        return;
    }*/

    var scale = $("#scale").val();

    if(scale == '') {
        scale = 1;
    }

    jlab.createRatioData(scale);

    /*console.log(jlab.ratioData);*/
    /*console.log(jlab.series);*/

    $(".chart-wrap").addClass("has-y-axis-label").addClass("has-x-axis-label");

    jlab.ratioChart = $.plot($("#chart-placeholder"), jlab.ratioData, {
        lines: {
            lineWidth: 4
        },
        legend: {
            show: false,
            position: 'nw',
            noColumns: 1,
            labelFormatter: function(label, series) {
                if(label === 'Scheduled') {
                    return null;
                }
                return label;
            }
        },
        yaxes: [{
            min: 0
        }, {
            position: 'right', /*Last x-axis label may wrap if we don't do this*/
            reserveSpace: true
        }],
        xaxis: {
            mode: "time",
            timeBase: "milliseconds",
            ticks: 6,
            timezone: null /*UTC data (timezone-less) - converted to America/New_York server side*/
        },
        grid: {
            borderWidth: 1,
            hoverable: false,
            borderColor: 'gray',
            backgroundColor: {colors: ["#fff", "#eee"]}
        }
    });
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

    jlab.chartALegend.charge.series[0].innerHTML = '';
    jlab.chartALegend.charge.series[1].innerHTML = '';
    jlab.chartALegend.current.series[0].innerHTML = '';
    jlab.chartALegend.current.series[1].innerHTML = '';

    jlab.chartBLegend.charge.series[0].innerHTML = '';
    jlab.chartBLegend.charge.series[1].innerHTML = '';
    jlab.chartBLegend.current.series[0].innerHTML = '';
    jlab.chartBLegend.current.series[1].innerHTML = '';

    jlab.chartCLegend.charge.series[0].innerHTML = '';
    jlab.chartCLegend.charge.series[1].innerHTML = '';
    jlab.chartCLegend.current.series[0].innerHTML = '';
    jlab.chartCLegend.current.series[1].innerHTML = '';

    jlab.chartDLegend.charge.series[0].innerHTML = '';
    jlab.chartDLegend.charge.series[1].innerHTML = '';
    jlab.chartDLegend.current.series[0].innerHTML = '';
    jlab.chartDLegend.current.series[1].innerHTML = '';
};

jlab.hideTooltipAll = function() {
    $(".date-tooltip").hide();
    $(".program-tooltip").hide();
};

jlab.chartALegend = {
    charge: {
        scheduled: document.getElementById('key-a-charge-scheduled'),
        delivered: document.getElementById('key-a-charge-delivered')
    }, current: {
        scheduled: document.getElementById('key-a-current-scheduled'),
        delivered: document.getElementById('key-a-current-delivered')
    }
};
jlab.chartALegend.charge.series = [jlab.chartALegend.charge.scheduled, jlab.chartALegend.charge.delivered];
jlab.chartALegend.current.series = [jlab.chartALegend.current.scheduled, jlab.chartALegend.current.delivered];

jlab.chartBLegend = {
    charge: {
        scheduled: document.getElementById('key-b-charge-scheduled'),
        delivered: document.getElementById('key-b-charge-delivered')
    }, current: {
        scheduled: document.getElementById('key-b-current-scheduled'),
        delivered: document.getElementById('key-b-current-delivered')
    }
};
jlab.chartBLegend.charge.series = [jlab.chartBLegend.charge.scheduled, jlab.chartBLegend.charge.delivered];
jlab.chartBLegend.current.series = [jlab.chartBLegend.current.scheduled, jlab.chartBLegend.current.delivered];

jlab.chartCLegend = {
    charge: {
        scheduled: document.getElementById('key-c-charge-scheduled'),
        delivered: document.getElementById('key-c-charge-delivered')
    }, current: {
        scheduled: document.getElementById('key-c-current-scheduled'),
        delivered: document.getElementById('key-c-current-delivered')
    }
};
jlab.chartCLegend.charge.series = [jlab.chartCLegend.charge.scheduled, jlab.chartCLegend.charge.delivered];
jlab.chartCLegend.current.series = [jlab.chartCLegend.current.scheduled, jlab.chartCLegend.current.delivered];

jlab.chartDLegend = {
    charge: {
        scheduled: document.getElementById('key-d-charge-scheduled'),
        delivered: document.getElementById('key-d-charge-delivered')
    }, current: {
        scheduled: document.getElementById('key-d-current-scheduled'),
        delivered: document.getElementById('key-d-current-delivered')
    }
};
jlab.chartDLegend.charge.series = [jlab.chartDLegend.charge.scheduled, jlab.chartDLegend.charge.delivered];
jlab.chartDLegend.current.series = [jlab.chartDLegend.current.scheduled, jlab.chartDLegend.current.delivered];

jlab.updateSelected = function() {
    jlab.updateSelectedTimeout = null;

    if (jlab.latestPosition != null) {
        var closestXIndex = jlab.findClosestXIndex(jlab.hallAChargeChart, jlab.latestPosition); /*Just use Hall A chart scheduled, as all halls have same X axis for scheduled (but not for delivered!)*/
        jlab.updatePrograms(closestXIndex);

        jlab.updateSelectedInChart(jlab.hallAChargeChart, jlab.latestPosition, jlab.chartALegend.charge.series, 'C', closestXIndex);
        jlab.updateSelectedInChart(jlab.hallACurrentChart, jlab.latestPosition, jlab.chartALegend.current.series, 'µA', closestXIndex);
        jlab.updateSelectedInChart(jlab.hallBChargeChart, jlab.latestPosition, jlab.chartBLegend.charge.series, 'mC', closestXIndex);
        jlab.updateSelectedInChart(jlab.hallBCurrentChart, jlab.latestPosition, jlab.chartBLegend.current.series, 'nA', closestXIndex);
        jlab.updateSelectedInChart(jlab.hallCChargeChart, jlab.latestPosition, jlab.chartCLegend.charge.series, 'C', closestXIndex);
        jlab.updateSelectedInChart(jlab.hallCCurrentChart, jlab.latestPosition, jlab.chartCLegend.current.series, 'µA', closestXIndex);
        jlab.updateSelectedInChart(jlab.hallDChargeChart, jlab.latestPosition, jlab.chartDLegend.charge.series, 'mC', closestXIndex);
        jlab.updateSelectedInChart(jlab.hallDCurrentChart, jlab.latestPosition, jlab.chartDLegend.current.series, 'nA', closestXIndex);

        jlab.updateSelectedDateTime(jlab.latestPosition);
    }
};

jlab.updatePrograms = function(index) {

    /*Take previous nearest*/
    if(index > 0) {
        index = index - 1;
    }

    $("#program-tooltip-a").html(jlab.series.programs.a[index]).show();
    $("#program-tooltip-b").html(jlab.series.programs.b[index]).show();
    $("#program-tooltip-c").html(jlab.series.programs.c[index]).show();
    $("#program-tooltip-d").html(jlab.series.programs.d[index]).show();
}

jlab.updateSelectedDateTime = function(pos) {
    var formattedX = jlab.toUserDateTimeStringUtc(new Date(pos.x));
    $(".date-tooltip").html(formattedX).show();
};

jlab.findClosestXIndex = function(plot, pos) {
    var j = 0, dataset = plot.getData();

    if(dataset.length > 0) {
        var series = dataset[0];
        // Find the nearest points, x-wise
        for (j = 0; j < series.data.length; ++j) {
            if (series.data[j][0] > pos.x) {
                break;
            }
        }
    }

    return j;
};

jlab.updateSelectedInChart = function(plot, pos, legendSeries, units, j) {

    var i, j, dataset = plot.getData();

    for (i = 0; i < dataset.length; ++i) {
        var series = dataset[i];

        if(series.data.length === 0) {
            return;
        }

        /*If scheduled series, which is at index 0, then use passed in closest point index j since it is a shared value.
        Otherwise series contains delivered data and number of points is variable so we must search for nearest
         point each time */
        if(i > 0) {
            for (j = 0; j < series.data.length; ++j) {
                if (series.data[j][0] > pos.x) {
                    break;
                }
            }
        }

        // Now Interpolate
        var y,
            p1 = series.data[j - 1],
            p2 = series.data[j];

        /*if(debug) {
            console.log(
                jlab.toUserDateStringUtc(new Date(series.data[j - 2][0])),
                jlab.toUserDateStringUtc(new Date(p1[0])),
                jlab.toUserDateStringUtc(new Date(p2[0])),
                jlab.toUserDateStringUtc(new Date(series.data[j + 1][0])),
                jlab.toUserDateStringUtc(new Date(series.data[j + 2][0]))
            );
        }*/

        if (p1 == null) {
            y = p2[1];
        } else if (p2 == null) {
            y = p1[1];
        } else if(series.lines.steps === true) {
            y = p1[1]; /*If points are stepped then don't interpolate, just grab nearest previous point*/
        } else {
            y = p1[1] + (p2[1] - p1[1]) * (pos.x - p1[0]) / (p2[0] - p1[0]);
        }
        legendSeries[i].innerHTML = y.toFixed(2) + '' + units;
    }
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

jlab.doScale = function(scale, source, destination) {
    for(var i = 0; i < source.length; i++) {
        var point = [source[i][0], source[i][1] * scale];
        destination.push(point);
    }
};

jlab.scaleHallScheduled = function(scale) {
    jlab.currentDataA[0].data = [];
    jlab.doScale(scale, jlab.series.current.scheduled.a.data, jlab.currentDataA[0].data);

    jlab.chargeDataA[0].data = [];
    jlab.doScale(scale, jlab.series.charge.scheduled.a.data, jlab.chargeDataA[0].data);

    jlab.currentDataB[0].data = [];
    jlab.doScale(scale, jlab.series.current.scheduled.b.data, jlab.currentDataB[0].data);

    jlab.chargeDataB[0].data = [];
    jlab.doScale(scale, jlab.series.charge.scheduled.b.data, jlab.chargeDataB[0].data);

    jlab.currentDataC[0].data = [];
    jlab.doScale(scale, jlab.series.current.scheduled.c.data, jlab.currentDataC[0].data);

    jlab.chargeDataC[0].data = [];
    jlab.doScale(scale, jlab.series.charge.scheduled.c.data, jlab.chargeDataC[0].data);

    jlab.currentDataD[0].data = [];
    jlab.doScale(scale, jlab.series.current.scheduled.d.data, jlab.currentDataD[0].data);

    jlab.chargeDataD[0].data = [];
    jlab.doScale(scale, jlab.series.charge.scheduled.d.data, jlab.chargeDataD[0].data);
};

jlab.scaleScheduled = function(scale) {

    jlab.scaleHallScheduled(scale);

    jlab.createRatioData(scale);

    jlab.redrawAll();
};

jlab.redrawAll = function() {
    jlab.hallACurrentChart.setData(jlab.currentDataA);
    jlab.hallACurrentChart.setupGrid();
    jlab.hallACurrentChart.draw();

    jlab.hallAChargeChart.setData(jlab.chargeDataA);
    jlab.hallAChargeChart.setupGrid();
    jlab.hallAChargeChart.draw();

    jlab.hallBCurrentChart.setData(jlab.currentDataB);
    jlab.hallBCurrentChart.setupGrid();
    jlab.hallBCurrentChart.draw();

    jlab.hallBChargeChart.setData(jlab.chargeDataB);
    jlab.hallBChargeChart.setupGrid();
    jlab.hallBChargeChart.draw();

    jlab.hallCCurrentChart.setData(jlab.currentDataC);
    jlab.hallCCurrentChart.setupGrid();
    jlab.hallCCurrentChart.draw();

    jlab.hallCChargeChart.setData(jlab.chargeDataC);
    jlab.hallCChargeChart.setupGrid();
    jlab.hallCChargeChart.draw();

    jlab.hallDCurrentChart.setData(jlab.currentDataD);
    jlab.hallDCurrentChart.setupGrid();
    jlab.hallDCurrentChart.draw();

    jlab.hallDChargeChart.setData(jlab.chargeDataD);
    jlab.hallDChargeChart.setupGrid();
    jlab.hallDChargeChart.draw();

    jlab.ratioChart.setData(jlab.ratioData);
    jlab.ratioChart.setupGrid();
    jlab.ratioChart.draw();

    $(".charge-placeholder .flot-x-axis").hide();

    jlab.updateKey();
};

$(document).on("change", "#scale", function(){

    var scale = $("#scale").val();

    if(scale == '') {
        scale = 1;
    }

    jlab.scaleScheduled(scale);
});

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

    let defaultParams = {start: jlab.toFriendlyDateString(start), end: jlab.toFriendlyDateString(end), scale: '0.5', print: 'N', fullscreen: 'N'};
    let redirecting = jlab.initParams(defaultParams);
    if(!redirecting) {
        jlab.initLineChart();
    }
});

$(function () {
    /*console.log(jlab.toScientificNotationHTML(0.00000012345678, ''));
    console.log(jlab.toScientificNotationHTML(0.0000012345678, ''));
    console.log(jlab.toScientificNotationHTML(0.000012345678, ''));
    console.log(jlab.toScientificNotationHTML(0.00012345678, ''));
    console.log(jlab.toScientificNotationHTML(0.0012345678, ''));
    console.log(jlab.toScientificNotationHTML(0.012345678, ''));
    console.log(jlab.toScientificNotationHTML(0.12345678, ''));
    console.log(jlab.toScientificNotationHTML(1.2345678, ''));
    console.log(jlab.toScientificNotationHTML(12.345678, ''));
    console.log(jlab.toScientificNotationHTML(123.45678, ''));
    console.log(jlab.toScientificNotationHTML(1234.5678, ''));
    console.log(jlab.toScientificNotationHTML(12345.678, ''));
    console.log(jlab.toScientificNotationHTML(123456.78, ''));
    console.log(jlab.toScientificNotationHTML(1234567.8, ''));
    console.log(jlab.toScientificNotationHTML(12345678, ''));*/
});