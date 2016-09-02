/**
 *
 */
package com.visfresh.reports.shipment;

import static com.visfresh.utils.LocalizationUtils.convertToUnits;
import static com.visfresh.utils.LocalizationUtils.getDegreeSymbol;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import net.sf.dynamicreports.report.builder.chart.Charts;
import net.sf.dynamicreports.report.builder.chart.TimeSeriesChartBuilder;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.constant.TimePeriod;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.defaults.Defaults;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleInsets;

import com.visfresh.controllers.UtilitiesController;
import com.visfresh.entities.Alert;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.EntityUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@SuppressWarnings("serial")
public class TemperatureChartRenderer extends XYLineAndShapeRenderer {
    private static final int ICON_SIZE = 20;
    private final AlertPaintingSupport support = new AlertPaintingSupport();
    private Map<Long, BufferedImage> topMarkers = new HashMap<>();

    /**
     * Default constructor.
     */
    public TemperatureChartRenderer() {
        super();
    }
    /**
     * @param lines
     * @param shapes
     */
    public TemperatureChartRenderer(final boolean lines, final boolean shapes) {
        super(lines, shapes);
    }

    /* (non-Javadoc)
     * @see org.jfree.chart.renderer.xy.XYLineAndShapeRenderer#drawSecondaryPass(java.awt.Graphics2D, org.jfree.chart.plot.XYPlot, org.jfree.data.xy.XYDataset, int, int, int, org.jfree.chart.axis.ValueAxis, java.awt.geom.Rectangle2D, org.jfree.chart.axis.ValueAxis, org.jfree.chart.plot.CrosshairState, org.jfree.chart.entity.EntityCollection)
     */
    @Override
    protected void drawSecondaryPass(final Graphics2D gOrigin, final XYPlot plot,
            final XYDataset dataset, final int pass, final int series, final int item,
            final ValueAxis domainAxis, final Rectangle2D dataArea, final ValueAxis rangeAxis,
            final CrosshairState crosshairState, final EntityCollection entities) {
        super.drawSecondaryPass(gOrigin, plot, dataset, pass, series, item, domainAxis,
                dataArea, rangeAxis, crosshairState, entities);
        // get the data point...
        final double x1 = dataset.getXValue(series, item);
        final double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
        final double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());

        if (getItemShapeVisible(series, item)) {
            final Long d = (Long) dataset.getX(series, item);
            final BufferedImage im = support.getRenderedImage(new Date(d), ICON_SIZE);
            if (im != null) {
                final Graphics2D g = (Graphics2D) gOrigin.create();
                try {
                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

                    g.drawImage(im,
                        (int) Math.round(transX1 - im.getWidth() / 2.),
                        (int) Math.round(transY1 - im.getHeight() / 2.),
                        null);
                } finally {
                    g.dispose();
                }
            }
        }
    }
    /**
     * @param readings
     * @param alerts
     * @param timeZone
     * @param renderer
     */
    public void addAlertsData(final List<ShortTrackerEvent> readings, final List<Alert> alerts,
            final ArrivalBean arrival, final TimeZone timeZone) {
        for (final Alert a : alerts) {
            final ShortTrackerEvent e = EntityUtils.getEntity(readings, a.getTrackerEventId());
            if (e != null) {
                support.addFiredAlerts(e.getTime(), a.getType());
            }
        }

        final int size = readings.size();
        if (size > 0) {
            support.addLastReading(readings.get(size - 1).getTime());
        }
        if (arrival != null) {
            support.addArrival(arrival.getTime());
        }
    }
    /**
     * @param bean
     * @return
     */
    private static DRIChartCustomizer createCustomizer(final ShipmentReportBean bean, final User user) {
        return new DRIChartCustomizer() {
            @Override
            public void customize(final JFreeChart chart, final ReportParameters reportParameters) {
                //set date format to date axis
                final DateAxis dateAxis = (DateAxis) chart.getXYPlot().getDomainAxis();
                dateAxis.setDateFormatOverride(DateTimeUtils.createPrettyFormat(
                        user.getLanguage(), user.getTimeZone()));
                dateAxis.setAutoRange(true);

                //install TemperatureChartRenderer to chart
                final TemperatureChartRenderer renderer = new TemperatureChartRenderer();
                renderer.addAlertsData(bean.getReadings(),
                        ShipmentReportBuilder.filterAlerts(bean.getAlerts()), bean.getArrival(),
                        user.getTimeZone());
                renderer.setSeriesShape(0, new Rectangle(0, 0));
                renderer.setLegendShape(0, new Rectangle(-1, -1, 2, 2));

                chart.getXYPlot().setRenderer(0, renderer);

                final TemperatureUnits tunits = user.getTemperatureUnits();
                //add green line
                final IntervalMarker greenLine = new IntervalMarker(
                        convertToUnits(bean.getLowerTemperatureLimit(), tunits),
                        convertToUnits(bean.getUpperTemperatureLimit(), tunits),
                        new Color(202, 255, 181, 150));
                chart.getXYPlot().addRangeMarker(greenLine, Layer.BACKGROUND);

                //correct tick count for temperature axis
                final ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis(0);

                final TickUnits tu = new TickUnits();
                final NumberFormat format = createTemperatureAxisUnitFromat(
                        rangeAxis.getUpperBound(), rangeAxis.getLowerBound());

                tu.add(new NumberTickUnit(5., format));
                tu.add(new NumberTickUnit(1., format));
                tu.add(new NumberTickUnit(0.1, format));
                tu.add(new NumberTickUnit(0.01, format));
                rangeAxis.setStandardTickUnits(tu);

                //expand range Axis for draw start and end location icons
                rangeAxis.getPlot().setInsets(new RectangleInsets(
                        ShipmentReportBuilder.LOCATION_IMAGE_SIZE + 2, 0, 0, 0));

                if (bean.getDateShipped() != null) {
                    final long value = bean.getDateShipped().getTime();
                    chart.getXYPlot().addDomainMarker(new ValueMarker(
                            value, Color.BLACK, new BasicStroke(2f)), Layer.FOREGROUND);
                    renderer.topMarkers.put(value, ShipmentReportBuilder.createShippedFromImage());
                }
                if (bean.getDateArrived() != null) {
                    final long value = bean.getDateArrived().getTime();
                    chart.getXYPlot().addDomainMarker(new ValueMarker(
                            value, Color.BLACK, new BasicStroke(2f)), Layer.FOREGROUND);
                    renderer.topMarkers.put(value, ShipmentReportBuilder.createShippedToImage());
                }
            }
        };
    }
    /* (non-Javadoc)
     * @see org.jfree.chart.renderer.xy.AbstractXYItemRenderer#drawDomainLine(java.awt.Graphics2D, org.jfree.chart.plot.XYPlot, org.jfree.chart.axis.ValueAxis, java.awt.geom.Rectangle2D, double, java.awt.Paint, java.awt.Stroke)
     */
    @Override
    public void drawDomainLine(final Graphics2D g2, final XYPlot plot, final ValueAxis axis,
            final Rectangle2D dataArea, final double value, final Paint paint, final Stroke stroke) {
        super.drawDomainLine(g2, plot, axis, dataArea, value, paint, stroke);

        //draw top level markers
        final Shape clip = g2.getClip();
        try {
            g2.setClip(null);

            final ValueAxis rangeAxis = plot.getRangeAxis();
            final int y = (int) rangeAxis.valueToJava2D(rangeAxis.getUpperBound(),
                    dataArea, plot.getRangeAxisEdge());

            for (final Map.Entry<Long, BufferedImage> e : topMarkers.entrySet()) {
                final int x = (int) plot.getDomainAxis().valueToJava2D(
                        e.getKey(), dataArea, plot.getDomainAxisEdge());

                Image im = e.getValue();
                final int iconSize = ShipmentReportBuilder.LOCATION_IMAGE_SIZE;
                if (e.getValue().getWidth() != iconSize || e.getValue().getHeight() != iconSize) {
                    im = e.getValue().getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
                }

                g2.drawImage(im, x - iconSize / 2, y - iconSize - 2, null);
            }
        } finally {
            g2.setClip(clip);
        }
    }
    /**
     * @return
     */
    protected static NumberFormat createTemperatureAxisUnitFromat(
            final double upperBound, final double lowerBound) {
        final DecimalFormat fmt = new DecimalFormat("#0.0");
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
        fmt.setDecimalFormatSymbols(decimalFormatSymbols);
        return fmt;
    }
    /**
     * @param bean
     * @param user
     * @return
     */
    public static TimeSeriesChartBuilder createCompatibleChart(
            final ShipmentReportBean bean, final User user) {
        final TemperatureUnits units = user.getTemperatureUnits();
        final TimeZone timeZone = user.getTimeZone();

        final String time = "time";
        final String temperature = "temperature";

        final TimeSeriesChartBuilder chart = Charts.timeSeriesChart();
        chart
            .setTimePeriod(Columns.column(time, Date.class))
            .setTimeAxisFormat(Charts.axisFormat().setLabel(
                    "Time (" + timeZone.getID() + " "
                    + UtilitiesController.createOffsetString(timeZone.getRawOffset()) + ")"))
            .setTimePeriodType(TimePeriod.MILLISECOND)
            .series(Charts.serie(Columns.column(temperature, java.lang.Double.class))
                .setLabel("Temperature " + getDegreeSymbol(units)))
            .seriesColors(bean.getDeviceColor());

        //add data
        final DRDataSource ds = new DRDataSource(new String[]{time, temperature});
        for (final ShortTrackerEvent e : getNormalizedReadings(bean)) {
            ds.add(e.getTime(), convertToUnits(e.getTemperature(), units));
        }

        chart.setDataSource(ds);
        chart.addCustomizer(createCustomizer(bean, user));
        //set chart height
        chart.setHeight(Defaults.getDefaults().getChartHeight() * 16 / 10);

        return chart;
    }
    /**
     * @param bean
     * @return
     */
    private static List<ShortTrackerEvent> getNormalizedReadings(final ShipmentReportBean bean) {
        final Map<String, ShortTrackerEvent> map = new HashMap<>();
        final List<ShortTrackerEvent> readings = new LinkedList<>();
        final DateFormat df = new SimpleDateFormat("yyyy:MM:dd:HH:mm");

        for (final ShortTrackerEvent e : bean.getReadings()) {
            final String key = df.format(e.getTime());

            final ShortTrackerEvent existing = map.get(key);
            if (existing == null) {
                //add reading
                readings.add(e);
                //register existing reading for given time
                map.put(key, e);
            } else {
                //use average temperature if more then one event associated
                //to one time point and ignore given event for avoid of summarizing
                //the value by chart builder.
                existing.setTemperature((existing.getTemperature() + e.getTemperature()) / 2.);
            }
        }

        return readings;
    }
}
