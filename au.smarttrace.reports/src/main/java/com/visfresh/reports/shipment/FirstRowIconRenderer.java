/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.visfresh.reports.AbstractGraphics2DRenderer;
import com.visfresh.reports.ImagePaintingSupport;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class FirstRowIconRenderer extends AbstractGraphics2DRenderer {
    private static final long serialVersionUID = -6796826878702077466L;
    private final ImageRenderingInfo renderingInfo;
    private final Color background;

    /**
     *
     */
    public FirstRowIconRenderer(final ImageRenderingInfo renderingInfo, final Color background) {
        super();
        this.renderingInfo = renderingInfo;
        this.background = background;
    }

    @Override
    public void render(final JasperReportsContext ctxt, final Graphics2D g, final Rectangle2D r)
            throws JRException {
        final BufferedImage im = loadReportPngImage(
                ImagePaintingSupport.expandPngResourceName(renderingInfo.getResource()));
        if (renderingInfo.shouldFlip()) {
            ImagePaintingSupport.flip(im);
        }

        final Rectangle rect = calculateImageRect(im.getWidth(null), im.getHeight(null),
                r);
//        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.drawImage(im, rect.x, rect.y, rect.width, rect.height, null);
    }

    /**
     * @param resource
     * @return
     */
    private BufferedImage loadReportPngImage(final String name) {
        final URL url = ImagePaintingSupport.class.getClassLoader().getResource(name);
        if (url == null) {
            throw new RuntimeException("Image not found: " + name);
        }

        try {
            final BufferedImage origin = ImageIO.read(url);
            //for now the image has loaded with byte data buffer need convert it to int buffer
            //for avoid the painting problems
            final BufferedImage image = new BufferedImage(origin.getWidth(), origin.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            final Graphics2D g = image.createGraphics();
            try {
                g.setBackground(background);
                g.clearRect(0, 0, image.getWidth(), image.getHeight());

                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.drawImage(origin, 0, 0, null);
            } finally {
                g.dispose();
            }

            return image;
        } catch (final IOException e) {
            throw new RuntimeException("Unable to load image", e);
        }
    }

    private Rectangle calculateImageRect(final int imWidth, final int imHeight, final Rectangle2D r) {
        final int margins = ShipmentReportBuilder.DEFAULT_PADDING;

        final Rectangle viewRect = r.getBounds();
        viewRect.x += margins;
        viewRect.y += margins;
        viewRect.width -= 2 * margins;
        viewRect.height -= 2 * margins;

        final double scale = Math.min((double) viewRect.width/ imWidth,
                (double) viewRect.height / imHeight);
        final double w = imWidth * scale;
        final double h = imHeight * scale;

        final Rectangle resultRect = new Rectangle(
                (int) Math.round(viewRect.x + (viewRect.width - w) / 2),
                viewRect.y,
                (int) Math.round(w),
                (int) Math.round(h)
        );

        return resultRect;
    }
}
