var jlab = jlab || {};
/*These are lower-case URL-friendly version of jlab.triCharMonthNames*/
jlab.TRI_CHAR_MONTH = ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 'jul', 'aug', 'sep', 'oct', 'nov', 'dec'];
/*Make sure to right-align your cells and use the same number of numbers after the decimal in all cells, then call this function to decimal align centered*/
jlab.fakeDecimalAlign = function ($rows, cellSelectors) {
    var maxTextWidths = [];
    for (var columnIndex = 0; columnIndex < cellSelectors.length; columnIndex++) {
        maxTextWidths[columnIndex] = 0;
    }

    $rows.each(function () {
        for (var columnIndex = 0; columnIndex < cellSelectors.length; columnIndex++) {
            var $td = $(this).find(cellSelectors[columnIndex]);
            $td.wrapInner('<span></span>');
            var length = $td.find("span").width();
            if (length > maxTextWidths[columnIndex]) {
                maxTextWidths[columnIndex] = length;
            }
        }
    });

    $rows.each(function () {
        for (var columnIndex = 0; columnIndex < cellSelectors.length; columnIndex++) {
            var $td = $(this).find(cellSelectors[columnIndex]);
            $td.find('span').css('margin-right', ($td.width() - maxTextWidths[columnIndex]) / 2 + 'px');
        }
    });
};