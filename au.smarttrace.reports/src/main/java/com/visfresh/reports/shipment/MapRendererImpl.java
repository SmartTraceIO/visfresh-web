/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Location;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.reports.AbstractGraphics2DRenderer;
import com.visfresh.reports.Colors;
import com.visfresh.reports.ImagePaintingSupport;
import com.visfresh.reports.geomap.AbstractGeoMapBuiler;
import com.visfresh.reports.geomap.GoogleGeoMapBuiler;
import com.visfresh.utils.EntityUtils;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MapRendererImpl extends AbstractGraphics2DRenderer {
    private static final long serialVersionUID = 3416741245999507093L;
    private final ShipmentReportBean bean;
    private final AbstractGeoMapBuiler builder;
    private final int iconSize = 16;
    private final RoundedNumberRenderer interimStopRenderer = new RoundedNumberRenderer();

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
            final Graphics2D gOrigin, final Rectangle2D viewArea) throws JRException, MapRenderingException {
        render(gOrigin, viewArea);
    }
    /**
     * @param gOrigin
     * @param viewArea
     */
    public void render(final Graphics2D gOrigin, final Rectangle2D viewArea) throws MapRenderingException {
        final List<Location> coords = new LinkedList<>();
        for (final ShortTrackerEvent e : bean.getReadings()) {
            coords.add(new Location(e.getLatitude(), e.getLongitude()));
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

            final List<ShortTrackerEvent> readings = optimize(bean.getReadings());

            paintPath(g, coords, p, zoom);
            paintArrows(g, coords, p, zoom, viewArea);
            paintMarkers(g, readings, p, zoom);
        } catch (final IOException ioe) {
            throw new MapRenderingException(ioe);
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
     * @param readings
     * @return
     */
    protected List<ShortTrackerEvent> optimize(final List<ShortTrackerEvent> readings) {
//      return optimizer.optimize(bean.getReadings());
        return readings;
    }

    /**
     * @param g
     * @param points
     * @param mapLocation
     * @param zoom
     * @param viewArea TODO
     */
    private void paintArrows(final Graphics2D g, final List<Location> points,
            final Point mapLocation, final int zoom, final Rectangle2D viewArea) {
        if (points.isEmpty()) {
            return;
        }

        g.setColor(Colors.shadeColor(bean.getDeviceColor(), -0.3));
        g.setStroke(new BasicStroke(1.f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        //south west
        final double maxdist = new Point(0, 0).distance(viewArea.getWidth(), viewArea.getHeight()) / 10.;

        final int step = points.size() / 7;
        int count = 0;

        final Iterator<Location> iter = points.iterator();

        Point prev = AbstractGeoMapBuiler.toMapPosition(iter.next(), zoom);
        Point marker = prev;

        while (iter.hasNext()) {
            final Point current = AbstractGeoMapBuiler.toMapPosition(iter.next(), zoom);

            if (!prev.equals(current)) {
                if (marker.distance(current) > maxdist || count >= step) {
                    drawArrow(g, mapLocation, prev, current);
                    count = 0;
                    marker = current;
                } else {
                    count++;
                }

                prev = current;
            }
        }
    }

    /**
     * @param g graphics.
     * @param l1 first location.
     * @param l2 second location.
     */
    private void drawArrow(final Graphics2D g, final Point mapLocation, final Point l1, final Point l2) {
        //center
        final double x = (l2.x + l1.x) / 2.;
        final double y = (l2.y + l1.y) / 2.;

        //direction vector.
        double lx = l2.x - l1.x;
        double ly = l2.y - l1.y;

        final double norm = Math.sqrt(lx * lx + ly * ly);
        lx /= norm;
        ly /= norm;

        //arrow size
        final double size = 8;

        //arrow polygon
        final Polygon p = new Polygon();
        final double s2 = size / 2.;
//        final double d = (size + 0.5) / 2;
        final double d = s2;

        p.addPoint(
                (int) Math.round(x - lx * s2 + ly * d),
                (int) Math.round(y - ly * s2 - lx * d));
        p.addPoint(
                (int) Math.round(x - lx * s2 / 1.5),
                (int) Math.round(y - ly * s2 / 1.5));
        p.addPoint(
                (int) Math.round(x - lx * s2 - ly * d),
                (int) Math.round(y - ly * s2 + lx * d));
        p.addPoint(
                (int) Math.round(x + lx * s2),
                (int) Math.round(y + ly * s2));
        p.translate(-mapLocation.x, -mapLocation.y);

        g.fillPolygon(p);
        g.drawPolygon(p);
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
        for (final AlertBean a : ShipmentReportBuilder.filterAlerts(bean.getAlerts())) {
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
        //draw interim stops
        int i = 1;
        final Iterator<InterimStop> iter = bean.getInterimStops().iterator();
        while (iter.hasNext()) {
            final InterimStop stop = iter.next();
            drawInterimStop(g, mapLocation, stop.getLocation().getLocation(), i, zoom);
            i++;
        }

        //draw start location
        final LocationProfileBean startLocation = bean.getShippedFrom();
        if (startLocation != null) {
            drawMapImage(g, ImagePaintingSupport.scaleImage(
                    ImagePaintingSupport.loadReportPngImage("tinyShippedFrom"), iconSize),
                    mapLocation, startLocation.getLocation(), zoom);
        }

        //draw end location
        final LocationProfileBean endLocation = bean.getShippedTo();
        if (endLocation != null) {
            final int size = iconSize - 2;
            final BufferedImage image;

            int shift;
            if (bean.getStatus().isFinal()) {
                image = ImagePaintingSupport.scaleImage(
                        ImagePaintingSupport.loadReportPngImage("tinyShippedTo"), size);
                ImagePaintingSupport.flip(image);
                shift = size;
            } else {
                image = ImagePaintingSupport.scaleImage(
                        ImagePaintingSupport.loadReportPngImage("shippedToToBeDetermined"), size);
                shift = (int) Math.round(size * 0.8);
            }

            final int x = Math.round(AbstractGeoMapBuiler.lon2position(
                    endLocation.getLocation().getLongitude(), zoom) - mapLocation.x);
            final int y = Math.round(AbstractGeoMapBuiler.lat2position(
                    endLocation.getLocation().getLatitude(), zoom) - mapLocation.y);
            g.drawImage(image, x - shift, y - size, null);
        }
    }

    /**
     * @param g
     * @param readings
     * @param mapLocation
     * @param zoom
     */
    private void paintPath(final Graphics2D g, final List<Location> readings,
            final Point mapLocation, final int zoom) {
        //create path shape
        final GeneralPath path = new GeneralPath();
        for (final Location p : readings) {
            final int x = Math.round(AbstractGeoMapBuiler.lon2position(
                    p.getLongitude(), zoom) - mapLocation.x);
            final int y = Math.round(AbstractGeoMapBuiler.lat2position(
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
        final int x = Math.round(AbstractGeoMapBuiler.lon2position(
                loc.getLongitude(), zoom) - mapLocation.x);
        final int y = Math.round(AbstractGeoMapBuiler.lat2position(
                loc.getLatitude(), zoom) - mapLocation.y);
        g.drawImage(im, x - offset, y - offset, null);
    }
    /**
     * @param g graphics context.
     * @param mapLocation
     * @param loc location.
     * @param num interim stop number.
     * @param zoom current map zoom.
     */
    private void drawInterimStop(final Graphics2D g, final Point mapLocation, final Location loc,
            final int num, final int zoom) {
        g.setColor(Color.BLACK);

        final int size = iconSize + 2;
        final int offset = size / 2;
        final int x = Math.round(AbstractGeoMapBuiler.lon2position(
                loc.getLongitude(), zoom) - mapLocation.x) - offset;
        final int y = Math.round(AbstractGeoMapBuiler.lat2position(
                loc.getLatitude(), zoom) - mapLocation.y) - offset;

        interimStopRenderer.render(g, new Rectangle(x, y, size, size), num);
    }
}
