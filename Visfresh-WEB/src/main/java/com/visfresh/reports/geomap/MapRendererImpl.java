/**
 *
 */
package com.visfresh.reports.geomap;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.renderers.AbstractRenderer;
import net.sf.jasperreports.renderers.Graphics2DRenderable;

import com.visfresh.entities.Alert;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.reports.shipment.AlertPaintingSupport;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.reports.shipment.ShipmentReportBuilder;
import com.visfresh.utils.EntityUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MapRendererImpl extends AbstractRenderer implements
        Graphics2DRenderable {
    private static final long serialVersionUID = 3416741245999507093L;
    private final ShipmentReportBean bean;
    private final AbstractGeoMapBuiler builder;

    /**
     * Default constructor.
     */
    public MapRendererImpl(final ShipmentReportBean bean) {
        super();
        this.bean = bean;
//        builder = new OpenStreetMapBuilder();
        builder = new GoogleGeoMapBuiler();
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.renderers.Graphics2DRenderable#render(net.sf.jasperreports.engine.JasperReportsContext, java.awt.Graphics2D, java.awt.geom.Rectangle2D)
     */
    @Override
    public void render(final JasperReportsContext context,
            final Graphics2D gOrigin, final Rectangle2D viewArea) throws JRException {

        final List<Point2D> coords = new LinkedList<>();
        for (final ShortTrackerEvent e : bean.getReadings()) {
            coords.add(new Point2D.Double(e.getLongitude(), e.getLatitude()));
        }

        final int width = (int) Math.floor(viewArea.getWidth());
        final int height = (int) Math.floor(viewArea.getHeight());

        final int zoom = builder.calculateZoom(coords, new Dimension(width, height), 9);

        final Rectangle r = builder.getMapBounds(coords, zoom);

        final Point p = new Point(
                (int) (r.getX() - (viewArea.getWidth() - r.getWidth()) / 2.),
                (int) (r.getY() - (viewArea.getHeight() - r.getHeight()) / 2.));

        final Graphics2D g = (Graphics2D) gOrigin.create(
                (int) viewArea.getX(),
                (int) viewArea.getY(),
                width,
                height);
        try {
            final Composite comp = g.getComposite();

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.6f));
            builder.paint(g, p, zoom, width, height);

            g.setComposite(comp);
            paintMarkers(bean, g, p, zoom);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            g.dispose();
        }
    }

    /**
     * @param rim
     * @param bean
     * @param size
     * @return
     */
    private void paintMarkers(final ShipmentReportBean bean,
            final Graphics2D g, final Point loc, final int zoom) {
        //create path shape
        final GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        for (final ShortTrackerEvent p : bean.getReadings()) {
            final int x = Math.round(OpenStreetMapBuilder.lon2position(
                    p.getLongitude(), zoom) - loc.x);
            final int y = Math.round(OpenStreetMapBuilder.lat2position(
                    p.getLatitude(), zoom) - loc.y);
            final Point2D cp = path.getCurrentPoint();
            if (cp == null) {
                path.moveTo(x, y);
            } else if (Math.round(cp.getX()) != x || Math.round(cp.getY()) != y) {
                path.lineTo(x, y);
            }
        }

        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setStroke(new BasicStroke(2.f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(bean.getDeviceColor());
        g.draw(path);

        //draw alerts
        final AlertPaintingSupport support = new AlertPaintingSupport();
        for (final Alert a : ShipmentReportBuilder.filterAlerts(bean.getAlerts())) {
            final ShortTrackerEvent e = EntityUtils.getEntity(bean.getReadings(), a.getTrackerEventId());
            if (e != null) {
                support.addFiredAlerts(e.getTime(), a.getType());
            }
        }

        final int readingsCount = bean.getReadings().size();
        if (readingsCount > 0) {
            support.addLastReading(bean.getReadings().get(readingsCount - 1).getTime());
        }
        if (bean.getArrival() != null) {
            support.addArrival(bean.getArrival().getTime());
        }

        for (final ShortTrackerEvent p : bean.getReadings()) {
            final int iconSize = 16;
            final BufferedImage im = support.getRenderedImage(p.getTime(), iconSize);

            if (im != null) {
                final int offset = iconSize / 2;
                final int x = Math.round(OpenStreetMapBuilder.lon2position(
                        p.getLongitude(), zoom) - loc.x);
                final int y = Math.round(OpenStreetMapBuilder.lat2position(
                        p.getLatitude(), zoom) - loc.y);
                g.drawImage(im, x - offset, y - offset, null);
            }
        }
    }
}
