/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

import com.visfresh.entities.AlertType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@SuppressWarnings("serial")
public class TemperatureChartRenderer extends XYLineAndShapeRenderer {
    private Map<Integer, List<AlertType>> alertsFired = new HashMap<>();
    private Map<AlertType, BufferedImage> images = new HashMap<>();
    private Map<Integer, BufferedImage> complexImages = new HashMap<>();

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

    public void addFiredAlerts(final int index, final AlertType... alerts) {
        List<AlertType> list = alertsFired.get(index);
        if (list == null) {
            list = new LinkedList<>();
            alertsFired.put(index, list);
        }
        for (final AlertType type : alerts) {
            list.add(type);
            possibleLoadImage(type);
        }
    }
    /**
     * @param type
     */
    private void possibleLoadImage(final AlertType type) {
        BufferedImage image = images.get(type);
        if (image == null) {
            image = loadAlertImage(type);
        }
        images.put(type, image);
    }
    /**
     * @param type
     * @return
     */
    public static BufferedImage loadAlertImage(final AlertType type) {
        final String resourcePath = "reports/images/shipment/alert";

        final URL url = TemperatureChartRenderer.class.getClassLoader().getResource(
                resourcePath + type.name() + ".png");
        if (url == null) {
            throw new RuntimeException("Image not found for alert " + type);
        }

        try {
            return ImageIO.read(url);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load image", e);
        }
    }
    /* (non-Javadoc)
     * @see org.jfree.chart.renderer.xy.XYLineAndShapeRenderer#drawFirstPassShape(java.awt.Graphics2D, int, int, int, java.awt.Shape)
     */
    @Override
    protected void drawFirstPassShape(final Graphics2D g2, final int pass, final int series,
            final int item, final Shape shape) {
        super.drawFirstPassShape(g2, pass, series, item, shape);
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
            final BufferedImage im = getRenderedImage(item);
            if (im != null) {
                final int w = im.getWidth();
                final int h = im.getHeight();

                g2.drawImage(im,
                    (int) Math.round(transX1 - w/2.),
                    (int) Math.round(transY1 - h/2),
                    null);
            }
        }
    }
    /**
     * @param column
     * @return
     */
    private BufferedImage getRenderedImage(final int column) {
        final BufferedImage im = this.complexImages.get(column);
        if (im != null) {
            return im;
        }

        final List<AlertType> alerts = alertsFired.get(column);
        if (alerts != null) {
            if (alerts.size() == 1) {
                return images.get(alerts.get(0));
            }

            //calculate image width
            int w = 0;
            int h = 0;

            int i = 0;
            for (final AlertType a : alerts) {
                final BufferedImage image = images.get(a);

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
                for (final AlertType a : alerts) {
                    g.drawImage(images.get(a), i, 0, null);
                    i++;
                }
            } finally {
                g.dispose();
            }

            complexImages.put(column, result);
            return result;
        }
        return null;
    }
}
