/**
 *
 */
package com.visfresh.reports.performance;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleEdge;
import org.springframework.stereotype.Component;

import com.visfresh.dao.impl.TimeAtom;
import com.visfresh.dao.impl.TimeRanges;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.reports.Colors;
import com.visfresh.reports.ReportUtils;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.chart.BarChartBuilder;
import net.sf.dynamicreports.report.builder.chart.Charts;
import net.sf.dynamicreports.report.builder.chart.PieChartBuilder;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListCellBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.BorderBuilder;
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.LineStyle;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.charts.util.PieLabelGenerator;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignField;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class PerformanceReportBuilder {
    private static final int DEFAULT_FONT_SIZE = 8;
    private static final int DEFAULT_PADDING = 3;
    protected RuleBundle ruleBundle = new RuleBundle();

    /**
     * Default constructor.
     */
    public PerformanceReportBuilder() {
        super();
    }

    public void createReport(final PerformanceReportBean bean,
            final User user, final OutputStream out) throws IOException {
        final JasperReportBuilder report = createReport(bean, user);
        try {
            report.toPdf(out);
        } catch (final DRException e) {
            throw new RuntimeException("Failed to build report", e);
        }
    }

    public JasperReportBuilder createReport(final PerformanceReportBean bean, final User user) throws IOException {

        final JasperReportBuilder report = DynamicReports.report();
        report.setDetailSplitType(SplitType.IMMEDIATE);
        report.setShowColumnTitle(false);
        report.setPageMargin(DynamicReports.margin(10).setBottom(0));
        report.addPageFooter(ReportUtils.createPageFooter());

        report.title(createTitle(bean, user));

        final VerticalListBuilder body = Components.verticalList();
        report.detail(body);

        for (final AlertProfileStats alertProfileStats : bean.getAlertProfiles()) {
            addShipmentsWithAlerts(body, alertProfileStats, user, bean.getTimeAtom());
            body.add(Components.gap(1, 5));

            try {
                addTemperatureHistory(body, alertProfileStats, user, bean.getTimeAtom());
                body.add(Components.gap(1, 5));
            } catch (final JRException e) {
                e.printStackTrace();
            }

            addPercentageOfTimeOutsideRanges(body, alertProfileStats, user, bean.getTimeAtom());
            body.add(Components.gap(1, 5));

            try {
                addThreeBiggestExceptions(body, alertProfileStats, user);
            } catch (final JRException e) {
                e.printStackTrace();
            }

            body.add(Components.pageBreak());
        }

        //this data source is not used, but required for
        //show the content
        report.setDataSource(Arrays.asList(bean));
        return report;
    }
    /**
     * @param body
     * @param temperatureExceptions
     * @param user
     * @throws JRException
     */
    private void addThreeBiggestExceptions(final VerticalListBuilder body,
            final AlertProfileStats alertProfileData, final User user) throws JRException {
        final List<BiggestTemperatureException> temperatureExceptions = alertProfileData.getTemperatureExceptions();
        final VerticalListBuilder list = createStyledVerticalListWithTitle(temperatureExceptions.size()
                + " biggest exceptions *", alertProfileData.getName());
        list.add(Components.gap(1, 3));

        final JasperReportBuilder report = new JasperReportBuilder();

        final String[] columns = {
                "shipmentNumber",
                "dateShipped",
                "shippedTo",
                "alertsFired",
                "avg",
                "sd",
                "timeBelow",
                "timeAbove",
                "totalTime"
        };
        final TemperatureUnits units = user.getTemperatureUnits();

        final StyleBuilder[] styles = new StyleBuilder[columns.length];
        @SuppressWarnings("unchecked")
        final TextColumnBuilder<String>[] cols = new TextColumnBuilder[styles.length];

        final ConditionalStyleBuilder font = Styles.conditionalStyle(ReportUtils.firstRowCondition)
                .setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE).bold());

        for (int i = 0; i < columns.length; i++) {
            final BorderBuilder headerBorder = Styles.border(Styles.pen(0f, LineStyle.SOLID));
            headerBorder.setBottomPen(Styles.pen1Point());
            final StyleBuilder style = createStyleByFont(DEFAULT_FONT_SIZE, false)
                    .setPadding(DEFAULT_PADDING)
                    .setBorder(headerBorder);
            style.addConditionalStyle(font);
            styles[i] = style;

            final TextColumnBuilder<String> columnBuilder = Columns.column(columns[i], String.class);
            columnBuilder.setStretchWithOverflow(true);

            cols[i] = columnBuilder;
        }

        final DateFormat dateShippedFormatter = DateTimeUtils.createPrettyFormat(
                user.getLanguage(), user.getTimeZone());
        final DRDataSource ds = new DRDataSource(columns);
        ds.add(
            "Tracker(trip)",
            "Date Shipped",
            "Shipped To",
            "Alerts Fired",
            "Avg Temp",
            "SD",
            "Time below " + getTemperatureString(alertProfileData.getLowerTemperatureLimit(), units, ""),
            "Time above " + getTemperatureString(alertProfileData.getUpperTemperatureLimit(), units, ""),
            "Time monitored"
        );

        for (final BiggestTemperatureException stats : temperatureExceptions) {
            final TemperatureStats temp = stats.getTemperatureStats();
            ds.add(
                    stats.getSerialNumber() + "(" + stats.getTripCount() + ")",
                    dateShippedFormatter.format(stats.getDateShipped()),
                    stats.getShippedTo() == null ? "" : stats.getShippedTo(),
                    ruleBundle.getAlertsFiredString(stats.getAlertsFired(), units),
                    getTemperatureString(temp.getAvgTemperature(), units, "No Readings"),
                    getSdString(temp.getStandardDevitation(), units, "No Readings"),
                    LocalizationUtils.formatByOneDecimal(
                            temp.getTimeBelowLowerLimit() / (60 * 60 * 1000.)) + "hrs",
                    LocalizationUtils.formatByOneDecimal(
                            temp.getTimeAboveUpperLimit() / (60 * 60 * 1000.)) + "hrs",
                    LocalizationUtils.formatByOneDecimal(
                            temp.getTotalTime() / (60 * 60 * 1000.)) + "hrs"
                    );
        }

        //init column sizes from first row labels
        //first row is in fact the table header
        ds.next();
        final JRDesignField field = new JRDesignField();
        field.setValueClass(String.class);
        field.setDescription("Temporaty field for access the data source");

        for (int i = 0; i < cols.length; i++) {
            final TextColumnBuilder<String> c = cols[i];
            field.setName(columns[i]);
            final String value = (String) ds.getFieldValue(field);
            c.setColumns(Math.max(8, value.length() + 2));
        }

        ds.moveFirst();

        //apply styles
        ReportUtils.customizeTableStyles(styles, true);
        for (int i = 0; i < styles.length; i++) {
            cols[i].setStyle(styles[i]);
        }

        report.columns(cols);
        report.setDataSource(ds);
        report.setHighlightDetailOddRows(true);
        report.setDetailOddRowStyle(Styles.simpleStyle().setBackgroundColor(Colors.CELL_BG));
        report.setShowColumnTitle(false);

        //create subreport with border
        final VerticalListBuilder sub = Components.verticalList(Components.subreport(report));
        final BorderBuilder border = Styles.border(Styles.pen1Point().setLineColor(Colors.DEFAULT_GREEN));
        list.setStyle(Styles.style().setBorder(border));
        list.add(sub);

        body.add(list);
    }

    /**
     * @param text
     * @return
     */
    private TextFieldBuilder<String> createRemark(final String text) {
        final TextFieldBuilder<String> b = Components.text(text);
        b.setStyle(createStyleByFont(DEFAULT_FONT_SIZE, false).setPadding(DEFAULT_PADDING));
        b.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT);
        return b;
    }


    /**
     * @param body
     * @param monthlyData
     * @param user
     */
    @SuppressWarnings("serial")
    private void addPercentageOfTimeOutsideRanges(final VerticalListBuilder body,
            final AlertProfileStats alertProfileStats, final User user, final TimeAtom atom) {
        final VerticalListBuilder vl = createStyledVerticalListWithTitle(
                "Percentage of time outside ranges *", alertProfileStats.getName());
        vl.add(Components.gap(1, 3));

        final HorizontalListBuilder list = Components.horizontalList();
        vl.add(list);

        //colors
        final Color[] colors = {
            new Color(60, 146, 243), // blue
            new Color(231, 35, 72) // red
        };

        final double oneHour = 60 * 60 * 1000l;

        //add charts
        final BarChartBuilder chart = Charts.barChart();
        chart.addSeriesColor(colors);
        chart.setShowValues(true);
        chart.addCustomizer(new DRIChartCustomizer() {
            @Override
            public void customize(final JFreeChart chart, final ReportParameters reportParameters) {
                final CategoryPlot plot = (CategoryPlot) chart.getPlot();

                //update category axis.
                final CategoryAxis categoryAxis = plot.getDomainAxis();
                categoryAxis.setTickLabelsVisible(false);
                categoryAxis.setAxisLineVisible(false);
                categoryAxis.setTickMarksVisible(false);

                //update value axis
                final NumberAxis axis = (NumberAxis) plot.getRangeAxis();
                axis.setTickLabelsVisible(false);
                axis.setAxisLineVisible(false);
                axis.setTickMarksVisible(false);
                axis.setRange(0., 110.);

                //update bar renderer.
                final BarRenderer r = (BarRenderer) plot.getRenderer();
                r.setBarPainter(new StandardBarPainter() {
                    /* (non-Javadoc)
                     * @see org.jfree.chart.renderer.category.StandardBarPainter#paintBar(java.awt.Graphics2D, org.jfree.chart.renderer.category.BarRenderer, int, int, java.awt.geom.RectangularShape, org.jfree.ui.RectangleEdge)
                     */
                    @Override
                    public void paintBar(final Graphics2D g2, final BarRenderer renderer,
                            final int row, final int column, final RectangularShape bar,
                            final RectangleEdge base) {
                        super.paintBar(g2, renderer, row, column, roundBar(bar), base);
                    }
                    /* (non-Javadoc)
                     * @see org.jfree.chart.renderer.category.StandardBarPainter#paintBarShadow(java.awt.Graphics2D, org.jfree.chart.renderer.category.BarRenderer, int, int, java.awt.geom.RectangularShape, org.jfree.ui.RectangleEdge, boolean)
                     */
                    @Override
                    public void paintBarShadow(final Graphics2D g2,
                            final BarRenderer renderer, final int row, final int column,
                            final RectangularShape bar, final RectangleEdge base,
                            final boolean pegShadow) {
                        super.paintBarShadow(g2, renderer, row, column, roundBar(bar), base, pegShadow);
                    }
                    private RectangularShape roundBar(final RectangularShape bar) {
                        final double arc = Math.min(20., bar.getHeight() / 2.);
                        return new RoundRectangle2D.Double(bar.getX(), bar.getY(), bar.getWidth(), bar.getHeight(),
                                arc, arc);
                    }
                });
                final DecimalFormat fmt = new DecimalFormat("#0.0");
                final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
                fmt.setDecimalFormatSymbols(decimalFormatSymbols);

                final StandardCategoryItemLabelGenerator labelGenerator = new StandardCategoryItemLabelGenerator(
                        "{3}% ({2}hrs)", fmt) {
                    /* (non-Javadoc)
                     * @see org.jfree.chart.labels.AbstractCategoryItemLabelGenerator#createItemArray(org.jfree.data.category.CategoryDataset, int, int)
                     */
                    @Override
                    protected Object[] createItemArray(final CategoryDataset dataset,
                            final int row, final int column) {
                        final Object[] array = super.createItemArray(dataset, row, column);
                        final TemperatureStats stats = alertProfileStats.getTimedData().get(column)
                                .getTemperatureStats();
                        final Number value = dataset.getValue(row, column);

                        if (stats.getTotalTime() > 0 && value != null) {
                            final double time = row == 0 ? stats.getTimeBelowLowerLimit() : stats.getTimeAboveUpperLimit();
                            array[2] = LocalizationUtils.formatByOneDecimal(time / oneHour);
                            array[3] = LocalizationUtils.formatByOneDecimal(time / stats.getTotalTime() * 100);
                        } else {
                            array[2] = "0.0";
                            array[3] = "0.0";
                        }

                        return array;
                    }
                };
                r.setSeriesItemLabelGenerator(0, labelGenerator);
                r.setSeriesItemLabelGenerator(1, labelGenerator);
            }
        });

        final DRDataSource ds = new DRDataSource("month", "below", "above");

        chart.addSerie(Charts.serie("below", Double.class).setLabel("Below "
                + getTemperatureString(alertProfileStats.getLowerTemperatureLimit(),
                        user.getTemperatureUnits(), "")));
        chart.addSerie(Charts.serie("above", Double.class).setLabel("Above "
                + getTemperatureString(alertProfileStats.getUpperTemperatureLimit(),
                        user.getTemperatureUnits(), "")));
        chart.setCategory("month", String.class);

        for (final TimeRangesTemperatureStats stats : alertProfileStats.getTimedData()) {
            final String month = getRangesTitle(stats.getTimeRanges(), user, atom);

            //set data source
            final TemperatureStats as = stats.getTemperatureStats();
            if (as.getTotalTime() > 0) {
                ds.add(month,
                        (double) as.getTimeBelowLowerLimit() / as.getTotalTime() * 100.,
                        (double) as.getTimeAboveUpperLimit() / as.getTotalTime() * 100.);
            } else {
                ds.add(month, 0.0, 0.0);
            }
        }

        chart.setDataSource(ds);

        list.add(chart);
        list.add(Components.gap(1, 3));

        body.add(vl);
    }

    /**
     * @param body
     * @param monthlyData
     * @param user
     */
    @SuppressWarnings("serial")
    private void addShipmentsWithAlerts(final VerticalListBuilder body,
            final AlertProfileStats alertProfileStats, final User user, final TimeAtom atom) {
        final VerticalListBuilder vl = createStyledVerticalListWithTitle("Shipments With Alerts *",
                alertProfileStats.getName());
        vl.add(Components.gap(1, 3));

        final HorizontalListBuilder list = Components.horizontalList();
        vl.add(list);

        //colors
        final Color[] colors = {
                new Color(155, 187, 89),
                new Color(192, 80, 78),
                new Color(79, 129, 189),
                new Color(126, 99, 160)
        };

        //add charts
        for (final TimeRangesTemperatureStats stats : alertProfileStats.getTimedData()) {
            final PieChartBuilder chart = Charts.pieChart();
            chart.addSerie(Charts.serie("value", Integer.class));
            chart.setTitle(getRangesTitle(stats.getTimeRanges(), user, atom));
            chart.setTitleFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE + 3).bold());
            chart.setKey("key", String.class);
            chart.setShowValues(true);
            chart.setShowLegend(false);
            chart.addCustomizer(new DRIChartCustomizer() {
                @Override
                public void customize(final JFreeChart chart, final ReportParameters reportParameters) {
                    final PiePlot plot = (PiePlot) chart.getPlot();
                    plot.setIgnoreNullValues(true);
                    plot.setIgnoreZeroValues(true);
                    plot.setLabelGenerator(new PieLabelGenerator(new HashMap<Comparable<?>, String>()){
                        @Override
                        public String generateSectionLabel(final PieDataset ds,
                                @SuppressWarnings("rawtypes") final Comparable key) {
                            return String.valueOf(ds.getValue(key).intValue());
                        }
                    });
                }
            });

            //set data source
            final ReportsWithAlertStats as = stats.getAlertStats();
            final DRDataSource ds = new DRDataSource("key", "value");
            ds.add("notAlerts", as.getNotAlerts());
            ds.add("hotAlerts", as.getHotAlerts());
            ds.add("coldAlerts", as.getColdAlerts());
            ds.add("hotAndColdAlerts", as.getHotAndColdAlerts());

            chart.setDataSource(ds);

            //set colors
            chart.addSeriesColorByName("notAlerts", colors[0]);
            chart.addSeriesColorByName("hotAlerts", colors[1]);
            chart.addSeriesColorByName("coldAlerts", colors[2]);
            chart.addSeriesColorByName("hotAndColdAlerts", colors[3]);


            list.add(chart);
        }

        //add title component
        final HorizontalListBuilder titles = Components.horizontalList();
        titles.setGap(3);
        titles.setStyle(Styles.style()
                .setLeftPadding(150)
                .setBottomPadding(4));

        titles.add(createCyrcle(colors[0]));
        titles.add(createSerieTitle("No alerts"));

        titles.add(createCyrcle(colors[1]));
        titles.add(createSerieTitle("Hot alerts"));

        titles.add(createCyrcle(colors[2]));
        titles.add(createSerieTitle("Cold alerts"));

        titles.add(createCyrcle(colors[3]));
        titles.add(createSerieTitle("Hot and cold alerts"));

        titles.add(Components.filler());

        vl.add(titles);

        body.add(vl);
    }

    /**
     * @param timeRanges
     * @return
     */
    private String getRangesTitle(final TimeRanges timeRanges, final User user, final TimeAtom atom) {
        switch (atom) {
            case Month:
                final DateFormat monthFmt = DateTimeUtils.createDateFormat(
                        "MMM yyyy", user.getLanguage(), user.getTimeZone());
                return monthFmt.format(new Date((timeRanges.getEndTime() + timeRanges.getStartTime()) / 2));
            case Week:
                final DateFormat weekFmt = new SimpleDateFormat("dd:mm");
                return weekFmt.format(timeRanges.getStartTime())
                        + "-" + weekFmt.format(new Date(timeRanges.getEndTime()));
            case Quarter:
                final DateFormat quarterFmt = DateTimeUtils.createDateFormat("MMM", user.getLanguage(), user.getTimeZone());
                final StringBuilder sb = new StringBuilder();

                //first month
                final Calendar c = new GregorianCalendar();
                c.setTimeInMillis(timeRanges.getStartTime());
                sb.append(quarterFmt.format(c.getTime())).append(' ');

                //second month
                c.add(Calendar.MONTH, 1);
                sb.append(quarterFmt.format(c.getTime())).append(' ');

                //second month
                c.add(Calendar.MONTH, 1);
                sb.append(quarterFmt.format(c.getTime()));

                return sb.toString();
            default:
                throw new RuntimeException("Unsupported " + atom);
        }
    }

    /**
     * @param body
     * @param monthlyData
     * @param user
     * @throws JRException
     */
    private void addTemperatureHistory(final VerticalListBuilder body,
            final AlertProfileStats alertProfileData, final User user, final TimeAtom atom) throws JRException {
        final VerticalListBuilder list = createStyledVerticalListWithTitle("Temperature history *",
                alertProfileData.getName());
        list.add(Components.gap(1, 3));

        final JasperReportBuilder report = new JasperReportBuilder();

        final String[] columns = {
                "month",
                "avg",
                "sd",
                "min",
                "max",
                "timeBelow",
                "timeAbove",
                "totalTime"
        };
        final StyleBuilder[] styles = new StyleBuilder[columns.length];
        @SuppressWarnings("unchecked")
        final TextColumnBuilder<String>[] cols = new TextColumnBuilder[styles.length];

        final ConditionalStyleBuilder font = Styles.conditionalStyle(ReportUtils.firstRowCondition)
                .setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE).bold());

        for (int i = 0; i < columns.length; i++) {
            final BorderBuilder headerBorder = Styles.border(Styles.pen(0f, LineStyle.SOLID));
            headerBorder.setBottomPen(Styles.pen1Point());
            final StyleBuilder style = Styles.style(createStyleByFont(DEFAULT_FONT_SIZE, false)
                    .setPadding(DEFAULT_PADDING))
                    .setBorder(headerBorder);
            style.addConditionalStyle(font);
            styles[i] = style;

            final TextColumnBuilder<String> columnBuilder = Columns.column(columns[i], String.class);
            columnBuilder.setStretchWithOverflow(true);
            cols[i] = columnBuilder;
        }

//      Avg Temp SD Min Temp Max Temp Time below 0C Time above 5C Time monitored
//      3.3°C 0.6 0.2°C 5.3°C 1.1hrs 0.2hrs 23.3hrs
        final DRDataSource ds = new DRDataSource(columns);
        final TemperatureUnits units = user.getTemperatureUnits();

        ds.add("",
                "Avg Temp",
                "SD",
                "Min Temp",
                "Max Temp",
                "Time below " + getTemperatureString(alertProfileData.getLowerTemperatureLimit(), units, ""),
                "Time above " + getTemperatureString(alertProfileData.getUpperTemperatureLimit(), units, ""),
                "Time monitored");

        for (final TimeRangesTemperatureStats stats : alertProfileData.getTimedData()) {
            final TemperatureStats temp = stats.getTemperatureStats();
            ds.add(
                    getRangesTitle(stats.getTimeRanges(), user, atom),
                    getTemperatureString(temp.getAvgTemperature(), units, "No Readings"),
                    getSdString(temp.getStandardDevitation(), units, "No Readings"),
                    getTemperatureString(temp.getMinimumTemperature(), units, "No Readings"),
                    getTemperatureString(temp.getMaximumTemperature(), units, "No Readings"),
                    LocalizationUtils.formatByOneDecimal(
                            temp.getTimeBelowLowerLimit() / (60 * 60 * 1000.)) + "hrs",
                    LocalizationUtils.formatByOneDecimal(
                            temp.getTimeAboveUpperLimit() / (60 * 60 * 1000.)) + "hrs",
                    LocalizationUtils.formatByOneDecimal(
                            temp.getTotalTime() / (60 * 60 * 1000.)) + "hrs"
                    );
        }

        //init column sizes from first row labels
        //first row is in fact the table header
        ds.next();
        final JRDesignField field = new JRDesignField();
        field.setValueClass(String.class);
        field.setDescription("Temporaty field for access the data source");

        for (int i = 0; i < cols.length; i++) {
            final TextColumnBuilder<String> c = cols[i];
            field.setName(columns[i]);
            final String value = (String) ds.getFieldValue(field);
            c.setColumns(Math.max(8, value.length() + 2));
        }

        ds.moveFirst();

        //apply styles
        ReportUtils.customizeTableStyles(styles, true);
        for (int i = 0; i < styles.length; i++) {
            cols[i].setStyle(styles[i]);
        }

        report.columns(cols);
        report.setDataSource(ds);
        report.setHighlightDetailOddRows(true);
        report.setDetailOddRowStyle(Styles.simpleStyle().setBackgroundColor(Colors.CELL_BG));
        report.setShowColumnTitle(false);

        //create subreport with border
        final VerticalListBuilder sub = Components.verticalList(Components.subreport(report));
        final BorderBuilder border = Styles.border(Styles.pen1Point().setLineColor(Colors.DEFAULT_GREEN));
        list.setStyle(Styles.style().setBorder(border));
        list.add(sub);

        body.add(list);
    }

    /**
     * @param text
     * @return
     */
    private HorizontalListCellBuilder createSerieTitle(final String text) {
        final TextFieldBuilder<String> textComponent = Components.text(text);
        textComponent.setColumns(text.length() - 3);
        return Components.hListCell(textComponent.setStyle(
                createStyleByFont(DEFAULT_FONT_SIZE, false))).widthFixed();
    }
    /**
     * @param color
     * @return
     */
    protected HorizontalListCellBuilder createCyrcle(final Color color) {
        return Components.hListCell(Components.ellipse().setFixedDimension(6, 6)
                .setPen(Styles.penThin())
                .setStyle(Styles.style().setBackgroundColor(color))).widthFixed();
    }

    /**
     * @param bean report bean.
     * @param user user.
     * @return
     */
    private VerticalListBuilder createTitle(final PerformanceReportBean bean, final User user) {
        //title
        final VerticalListBuilder titles = Components.verticalList();

//      SHIPMENT REPORT
        TextFieldBuilder<String> f = Components.text("PERFORMANCE REPORT");
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 3, true));

        titles.add(f);
//      Primo Moraitis Fresh
        f = Components.text(bean.getCompanyName());
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 2, true));

        titles.add(f);
//      Tracker 122(4) - as of 6:45 13 Aug 2016 (EST)
        final DateFormat fmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());
        f = Components.text("As of "
                + fmt.format(new Date()) + " (" + user.getTimeZone().getID() + ")");
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 1, true));
        titles.add(f);

        return titles;
    }
    /**
     * @param size
     * @param isBold
     * @return
     */
    private StyleBuilder createStyleByFont(final int size, final boolean isBold) {
        FontBuilder font = Styles.font().setFontSize(size);
        if (isBold) {
            font = font.bold();
        }

        return Styles.style().setFont(font);
    }
    /**
     * @param titleText
     */
    private VerticalListBuilder createStyledVerticalListWithTitle(final String titleText, final String alertProfileName) {
        final VerticalListBuilder list = Components.verticalList();

        //add table border
        final PenBuilder pen = Styles.pen1Point().setLineColor(Colors.DEFAULT_GREEN);
        final BorderBuilder border = Styles.border(pen);
        list.setStyle(Styles.style().setBorder(border));

        //add table title
        final StyleBuilder headerStyle = Styles.style();
        headerStyle.setBackgroundColor(Colors.DEFAULT_GREEN);
        headerStyle.setPadding(Styles.padding(DEFAULT_PADDING));

        final BorderBuilder titleBorder = Styles.border(Styles.pen().setLineColor(Colors.DEFAULT_GREEN));
        titleBorder.setBottomPen(Styles.pen1Point().setLineColor(Color.BLACK).setLineWidth(2f)
                .setLineStyle(LineStyle.SOLID));
        headerStyle.setBorder(titleBorder);

        final HorizontalListBuilder header = Components.horizontalList().setStyle(headerStyle);

        //header left and right text
        final TextFieldBuilder<String> title = Components.text(titleText).setStyle(
                createStyleByFont(DEFAULT_FONT_SIZE, true));
        title.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        title.setColumns(1);
        header.add(title);

        //Shipments with “Chilled Beef" profile (exc. cooldown and post-arrival)
        final String remarkText = "* Shipments with \"" + alertProfileName + "\" profile (exc. cooldown and post-arrival)";
        final TextFieldBuilder<String> remark = createRemark(remarkText).setStyle(
                        createStyleByFont(DEFAULT_FONT_SIZE, false));
        remark.setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT);
        remark.setColumns(2);
        header.add(remark);

        list.add(header);

        return list;
    }
    /**
     * @param t
     * @param units
     * @return
     */
    private String getTemperatureString(final Double t, final TemperatureUnits units, final String defValue) {
        if (t == null) {
            return defValue;
        }
        return LocalizationUtils.getTemperatureString(t, units);
    }
    /**
     * @param t
     * @param units
     * @param defValue
     * @return
     */
    private String getSdString(final Double t, final TemperatureUnits units, final String defValue) {
        if (t == null) {
            return defValue;
        }
        return LocalizationUtils.getTemperatureString(t, units);
    }
}
