/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LineWithInterimStopAnnotation extends AbstractLineWithIconAnnotation {
    private static final long serialVersionUID = -376593698817644608L;

    private final int number;

    /**
     * Default constructor.
     */
    public LineWithInterimStopAnnotation(final int number, final double value) {
        super(value);
        this.number = number;
    }

    /**
     * @param g
     * @param x
     * @param y
     */
    @Override
    protected void drawIcon(final Graphics2D g, final int x, final int y) {
        final int size = 16;
        final RoundedNumberRenderer r = new RoundedNumberRenderer();
        g.setStroke(new BasicStroke(1f));
        r.render(g, new Rectangle(x - size / 2, y - size - 2, size, size), number);
    }
}
