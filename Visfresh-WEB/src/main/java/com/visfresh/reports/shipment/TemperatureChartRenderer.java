/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<Date, BufferedImage> complexImages = new HashMap<>();
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
    protected void drawSecondaryPass(final Graphics2D g2, final XYPlot plot,
            final XYDataset dataset, final int pass, final int series, final int item,
            final ValueAxis domainAxis, final Rectangle2D dataArea, final ValueAxis rangeAxis,
            final CrosshairState crosshairState, final EntityCollection entities) {
        super.drawSecondaryPass(g2, plot, dataset, pass, series, item, domainAxis,
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
            final BufferedImage im = getRenderedImage(new Date(d));
            if (im != null) {
                final int w = im.getWidth();
                final int h = im.getHeight();

                g2.drawImage(im,
                    (int) Math.round(transX1 - w/2.),
                    (int) Math.round(transY1 - h/2.),
                    null);
            }
        }
    }
    /**
     * @param date
     * @return
     */
    private BufferedImage getRenderedImage(final Date date) {
        final BufferedImage im = this.complexImages.get(date);
        if (im != null) {
            return im;
        }

        final List<BufferedImage> images = support.getAlertImages(date);
        if (images != null) {
            if (images.size() == 1) {
                return images.get(0);
            }

            //calculate image width
            int w = 0;
            int h = 0;

            int i = 0;
            for (final BufferedImage image : images) {
                h = Math.max(image.getHeight(), h);
                w = Math.max(i + image.getWidth(), w);

                i++;
            }

            //check empty images
            if (w == 0 || h == 0) {
                return null;
            }

            //render complex image
            final BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            final Graphics2D g = result.createGraphics();
            try {
                i = 0;
                for (final BufferedImage a : images) {
                    g.drawImage(a, i, 0, null);
                    i++;
                }
            } finally {
                g.dispose();
            }

            complexImages.put(date, result);
            return result;
        }
        return null;
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
