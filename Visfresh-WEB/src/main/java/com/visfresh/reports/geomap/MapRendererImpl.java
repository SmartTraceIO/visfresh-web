/**
 *
 */
package com.visfresh.reports.geomap;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
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
import com.visfresh.entities.Location;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.reports.shipment.ImagePaintingSupport;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.reports.shipment.ShipmentReportBuilder;
import com.visfresh.services.EventsOptimizer;
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
    private final int iconSize = 16;
    private EventsOptimizer optimizer = new EventsOptimizer();

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

        final int iconSize = 17;
        int w1 = width - iconSize;
        if (w1 < 1) {//not use extended size for icons
            w1 = width;
        }
        int h1 = height - iconSize;
        if (h1 < 1) {//not use extended size for icons
            h1 = height;
        }

        final int zoom = builder.calculateZoom(coords, new Dimension(w1, h1), 10);

        final Rectangle r = builder.getMapBounds(coords, zoom);

        final Point p = new Point(
                (int) (r.getX() - (viewArea.getWidth() - r.getWidth()) / 2.),
                (int) (r.getY() - (viewArea.getHeight() - r.getHeight()) / 2.));

        //use image buffer for avoid of problems with alpha chanel.
        final BufferedImage mapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = mapImage.createGraphics();

        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            final Composite comp = g.getComposite();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

            //set transparency before draw map
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.8f));

            paintMap(g, p, width, height, zoom);

            //restore transparency
            g.setComposite(comp);

//            final List<ShortTrackerEvent> readings = optimizer.optimize(bean.getReadings());
            final List<ShortTrackerEvent> readings = bean.getReadings();

            paintPath(g, readings, p, zoom);
            paintMarkers(g, readings, p, zoom);
        } catch (final IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            g.dispose();
        }

        //draw image to graphics context
        gOrigin.drawImage(mapImage,
                (int) viewArea.getX(),
                (int) viewArea.getY(),
                null);
    }

    /**
     * @param g
     * @param p
     * @param width
     * @param height
     * @param zoom
     * @throws IOException
     */
    private void paintMap(final Graphics2D g, final Point p, final int width,
            final int height, final int zoom) throws IOException {
        builder.paint(g, p, zoom, width, height);
    }
    /**
     * @param g
     * @param readings
     * @param mapLocation
     * @param zoom
     */
    private void paintMarkers(final Graphics2D g, final List<ShortTrackerEvent> readings,
            final Point mapLocation, final int zoom) {
        //draw alerts
        final ImagePaintingSupport support = new ImagePaintingSupport();
        for (final Alert a : ShipmentReportBuilder.filterAlerts(bean.getAlerts())) {
            final ShortTrackerEvent e = EntityUtils.getEntity(readings, a.getTrackerEventId());
            if (e != null) {
                support.addFiredAlerts(e.getTime(), a.getType());
            }
        }

        //add arrival notification
        if (bean.getArrival() != null) {
            support.addArrival(bean.getArrival().getNotifiedAt());
        }
        //add last reading
        final int readingsCount = readings.size();
        if (readingsCount > 0) {
            support.addLastReading(readings.get(readingsCount - 1).getTime(),
                    bean.getDeviceColor());
        }

        for (final ShortTrackerEvent p : readings) {
            final BufferedImage im = support.getRenderedImage(p.getTime(), iconSize);

            if (im != null) {
                final Location loc = new Location(p.getLatitude(), p.getLongitude());
                drawMapImage(g, im, mapLocation, loc, zoom);
            }
        }

        //draw start location
        final Location startLocation = bean.getShippedFromLocation();
        if (startLocation != null) {
            drawMapImage(g, ImagePaintingSupport.scaleImage(
                    ImagePaintingSupport.loadReportPngImage("tinyShippedFrom"), iconSize),
                    mapLocation, startLocation, zoom);
        }

        //draw end location
        final Location endLocation = bean.getShippedToLocation();
        if (endLocation != null) {
            final int size = iconSize - 2;
            final BufferedImage image = ImagePaintingSupport.scaleImage(
                    ImagePaintingSupport.loadReportPngImage("tinyShippedTo"), size);
            ImagePaintingSupport.flip(image);

            final int x = Math.round(OpenStreetMapBuilder.lon2position(
                    endLocation.getLongitude(), zoom) - mapLocation.x);
            final int y = Math.round(OpenStreetMapBuilder.lat2position(
                    endLocation.getLatitude(), zoom) - mapLocation.y);
            g.drawImage(image, x - size, y - size, null);
        }
    }

    /**
     * @param g
     * @param readings
     * @param mapLocation
     * @param zoom
     */
    private void paintPath(final Graphics2D g, final List<ShortTrackerEvent> readings,
            final Point mapLocation, final int zoom) {
        //create path shape
        final GeneralPath path = new GeneralPath();
        for (final ShortTrackerEvent p : readings) {
            final int x = Math.round(OpenStreetMapBuilder.lon2position(
                    p.getLongitude(), zoom) - mapLocation.x);
            final int y = Math.round(OpenStreetMapBuilder.lat2position(
                    p.getLatitude(), zoom) - mapLocation.y);
            final Point2D cp = path.getCurrentPoint();
            if (cp == null) {
                path.moveTo(x, y);
            } else if (Math.round(cp.getX()) != x || Math.round(cp.getY()) != y) {
                path.lineTo(x, y);
            }
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2.f));
        g.setColor(bean.getDeviceColor());
        g.draw(path);
    }

    /**
     * @param g
     * @param im
     * @param mapLocation
     * @param loc
     * @param zoom
     */
    private void drawMapImage(final Graphics2D g, final BufferedImage im,
            final Point mapLocation, final Location loc, final int zoom) {
        final int offset = iconSize / 2;
        final int x = Math.round(OpenStreetMapBuilder.lon2position(
                loc.getLongitude(), zoom) - mapLocation.x);
        final int y = Math.round(OpenStreetMapBuilder.lat2position(
                loc.getLatitude(), zoom) - mapLocation.y);
        g.drawImage(im, x - offset, y - offset, null);
    }
}
