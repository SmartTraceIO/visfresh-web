/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RoundedNumberRenderer {
    private int margins = ShipmentReportBuilder.DEFAULT_PADDING;

    /**
     * Default constructor.
     */
    public RoundedNumberRenderer() {
        super();
    }

    public void render(final Graphics g, final Rectangle rect, final int number) {
        final Graphics2D g2 = (Graphics2D) g.create();
        try {
            renderImpl(g2, rect, number);
        } finally {
            g2.dispose();
        }
    }
    /**
     * @param g
     * @param rect
     * @param number
     */
    private void renderImpl(final Graphics2D g, final Rectangle rect, final int number) {
        final int margins = this.margins;

        //set rendering hints
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //draw circle
        final int d = Math.min(rect.width - 2 * margins, rect.height - 2 * margins);
        g.drawOval(
                rect.x + (int) Math.round((rect.width - d) / 2.),
                rect.y + margins,
                d, d);

        //draw text
        final String str = Integer.toString(number);

        final TextLayout tl = new TextLayout(str, g.getFont(), g.getFontRenderContext());
        //calculate origin cipher dimension
        final double w0 = tl.getVisibleAdvance();
        final double h0 = tl.getAscent() + tl.getDescent() - tl.getLeading();

        final double k = d / Math.sqrt(w0 * w0 + h0 * h0);

        final double w = w0 * k;
        final double h = h0 * k;

        final int tx = (int) Math.round((rect.width - w) / 2.);
        final int ty = (int) Math.round((d - h) / 2. + margins);

        final Graphics2D tg = (Graphics2D) g.create(rect.x + tx, rect.y + ty,
                (int) Math.round(w),
                (int) Math.round(h));
        try {
            final AffineTransform tr = (AffineTransform) tg.getTransform().clone();
            tr.scale(k, k);
            tg.setTransform(tr);

            tl.draw(tg, 0, tl.getAscent());
        } finally {
            tg.dispose();
        }
    }
    /**
     * @param margins the margins to set
     */
    public void setMargins(final int margins) {
        this.margins = margins;
    }
    /**
     * @return the margins
     */
    public int getMargins() {
        return margins;
    }
}
