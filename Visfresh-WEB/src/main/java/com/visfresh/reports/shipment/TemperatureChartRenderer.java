/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

import com.visfresh.entities.Alert;
import com.visfresh.entities.ShortTrackerEvent;
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
    protected void drawSecondaryPass(final Graphics2D g, final XYPlot plot,
            final XYDataset dataset, final int pass, final int series, final int item,
            final ValueAxis domainAxis, final Rectangle2D dataArea, final ValueAxis rangeAxis,
            final CrosshairState crosshairState, final EntityCollection entities) {
        super.drawSecondaryPass(g, plot, dataset, pass, series, item, domainAxis,
                dataArea, rangeAxis, crosshairState, entities);
        // get the data point...
        final double x1 = dataset.getXValue(series, item);
        final double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        final double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        if (getItemShapeVisible(series, item)) {
            final Long d = (Long) dataset.getX(series, item);
            final BufferedImage im = support.getRenderedImage(new Date(d), ICON_SIZE);
            if (im != null) {
                final int w = im.getWidth();
                final int h = im.getHeight();

                final RenderingHints hints = g.getRenderingHints();
                try {
                    g.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);

                    g.drawImage(im,
                        (int) Math.round(transX1 - w/2.),
                        (int) Math.round(transY1 - h/2.),
                        null);
                } finally {
                    g.setRenderingHints(hints);
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
                support.addFiredAlerts(DateTimeUtils.convertToTimeZone(e.getTime(), timeZone),
                        a.getType());
            }
        }

        final int size = readings.size();
        if (size > 0) {
            support.addLastReading(
                    DateTimeUtils.convertToTimeZone(readings.get(size - 1).getTime(), timeZone));
        }
        if (arrival != null) {
            support.addArrival(DateTimeUtils.convertToTimeZone(arrival.getTime(), timeZone));
        }
    }
}
