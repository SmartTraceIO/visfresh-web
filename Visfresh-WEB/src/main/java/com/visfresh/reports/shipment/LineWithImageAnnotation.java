/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LineWithImageAnnotation extends AbstractXYAnnotation {
    private static final long serialVersionUID = -376593698817644608L;

    private final BufferedImage image;
    private final double value;

    /**
     * Default constructor.
     */
    public LineWithImageAnnotation(final BufferedImage image, final double value) {
        super();
        this.image = image;
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.jfree.chart.annotations.XYAnnotation#draw(java.awt.Graphics2D, org.jfree.chart.plot.XYPlot, java.awt.geom.Rectangle2D, org.jfree.chart.axis.ValueAxis, org.jfree.chart.axis.ValueAxis, int, org.jfree.chart.plot.PlotRenderingInfo)
     */
    @Override
    public void draw(final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea,
            final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex,
            final PlotRenderingInfo info) {
        final int x = (int) domainAxis.valueToJava2D(value, dataArea, plot.getDomainAxisEdge());

        final int y1 = (int) rangeAxis.valueToJava2D(rangeAxis.getUpperBound(), dataArea, plot.getRangeAxisEdge());
        final int y2 = (int) rangeAxis.valueToJava2D(rangeAxis.getLowerBound(), dataArea, plot.getRangeAxisEdge());

        final Rectangle clip = g2.getClipBounds();
        final Composite oldComposite = g2.getComposite();

        //temporary unclip drawing area.
        g2.setClip(null);

        try {
            //Draw line
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            g2.drawLine(x, y1, x, y2);

            //draw image
            g2.drawImage(
                    createNotTransparentImage(),
                    x - image.getWidth() / 2,
                    y1 - image.getHeight() - 2,
                    null);
        } finally {

            g2.setComposite(oldComposite);
            g2.setClip(clip);
        }
    }

    /**
     * @return
     */
    private BufferedImage createNotTransparentImage() {
        final BufferedImage im = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g = im.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, im.getWidth(), im.getHeight());
            g.drawImage(image, 0, 0, null);
        } finally {
            g.dispose();
        }

        return im;
    }
}
