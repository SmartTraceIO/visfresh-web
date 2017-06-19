/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.visfresh.reports.AbstractGraphics2DRenderer;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopIconRenderer extends AbstractGraphics2DRenderer {
    private static final long serialVersionUID = -6796826878702077466L;
    private final int number;
    private final RoundedNumberRenderer renderer = new RoundedNumberRenderer();

    /**
     *
     */
    public InterimStopIconRenderer(final int number) {
        super();
        this.number = number;
    }

    @Override
    public void render(final JasperReportsContext ctxt, final Graphics2D g2, final Rectangle2D r)
            throws JRException {
        renderer.setMargins(ShipmentReportBuilder.DEFAULT_PADDING);

//        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.BLACK);
        renderer.render(g2, r.getBounds(), number);
    }
}
