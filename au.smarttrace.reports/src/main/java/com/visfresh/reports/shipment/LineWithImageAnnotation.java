/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LineWithImageAnnotation extends AbstractLineWithIconAnnotation {
    private static final long serialVersionUID = -376593698817644608L;

    private final BufferedImage image;

    /**
     * Default constructor.
     */
    public LineWithImageAnnotation(final BufferedImage image, final double value) {
        super(value);
        this.image = image;
    }

    /**
     * @param g
     * @param x
     * @param y
     */
    @Override
    protected void drawIcon(final Graphics2D g, final int x, final int y) {
        g.drawImage(
                createNotTransparentImage(),
                x - image.getWidth() / 2,
                y - image.getHeight() - 2,
                null);
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
