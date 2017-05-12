/**
 *
 */
package com.visfresh.reports;

import net.sf.jasperreports.renderers.AbstractRenderer;
import net.sf.jasperreports.renderers.Graphics2DRenderable;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractGraphics2DRenderer extends AbstractRenderer implements Graphics2DRenderable {
    private static final long serialVersionUID = 6383281333631006950L;

    /**
     * Default constructor.
     */
    public AbstractGraphics2DRenderer() {
        super();
    }
}
