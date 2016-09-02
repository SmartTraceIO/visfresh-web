/**
 *
 */
package com.visfresh.reports.geomap;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractGeoMapBuiler {
    protected static final int TILE_SIZE = 256;

    /**
     * @param area
     * @param targetSize
     * @return
     */
    public int calculateZoom(final List<Point2D> points, final Dimension targetSize, final int maxZoom) {
        int bestZoom = Math.min(maxZoom, getMaxZoom());

        int zoom = maxZoom;
        while (zoom > 0) {
            bestZoom = zoom;

            //calculate area for given zoom
            final Rectangle r = getMapBounds(points, zoom);
            if (r.width <= targetSize.width && r.height <= targetSize.height) {
                break;
            }
            zoom--;
        }

        return bestZoom;
    }

    /**
     * @return
     */
    public abstract int getMaxZoom();

    /**
     * @param points
     * @param zoom
     * @return
     */
    public Rectangle getMapBounds(final List<Point2D> points, final int zoom) {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;

        for (final Point2D p : points) {
            minx = Math.min(lon2position(p.getX(), zoom), minx);
            maxx = Math.max(lon2position(p.getX(), zoom), maxx);

            miny = Math.min(lat2position(p.getY(), zoom), miny);
            maxy = Math.max(lat2position(p.getY(), zoom), maxy);
        }

        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    public static int lat2position(final double lat, final int z) {
        final double ymax = TILE_SIZE * (1 << z);
        return (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * ymax);
    }
    public static double position2lon(final int x, final int z) {
        final double xmax = TILE_SIZE * (1 << z);
        return x / xmax * 360.0 - 180;
    }
    public static double position2lat(final int y, final int z) {
        final double ymax = TILE_SIZE * (1 << z);
        return Math.toDegrees(Math.atan(Math.sinh(Math.PI - (2.0 * Math.PI * y) / ymax)));
    }
    public static int lon2position(final double lon, final int z) {
        final double xmax = TILE_SIZE * (1 << z);
        return (int) Math.floor((lon + 180) / 360 * xmax);
    }

    /**
     *
     */
    public AbstractGeoMapBuiler() {
        super();
    }

    /**
     * @param gOrig
     * @param mapPosition
     * @param zoom
     * @param width
     * @param height
     * @throws IOException
     */
    public abstract void paint(final Graphics2D gOrig, final Point mapPosition, final int zoom,
            final int width, final int height) throws IOException;
}
