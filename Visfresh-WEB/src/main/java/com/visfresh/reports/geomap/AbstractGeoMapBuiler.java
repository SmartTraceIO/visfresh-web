/**
 *
 */
package com.visfresh.reports.geomap;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractGeoMapBuiler {
    protected static final int TILE_SIZE = 256;
    protected final TileCache cache = new TileCache();

    protected static class Tile {
        public final int x, y, z, w, h;
        public Tile(final int x, final int y, final int z) {
            this(x, y, z, TILE_SIZE, TILE_SIZE);
        }
        public Tile(final int x, final int y, final int z, final int w, final int h) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.h = h;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            result = prime * result + z;
            result = prime * result + w;
            result = prime * result + h;
            return result;
        }
        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Tile other = (Tile) obj;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            if (z != other.z)
                return false;
            if (w != other.w)
                return false;
            if (h != other.h)
                return false;
            return true;
        }

    }

    protected static class TileCache {
        private static final int CACHE_SIZE = 256;

        @SuppressWarnings("serial")
        private LinkedHashMap<Tile,BufferedImage> map = new LinkedHashMap<Tile,BufferedImage>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final java.util.Map.Entry<Tile,BufferedImage> eldest) {
                return size() > CACHE_SIZE;
            }
        };
        public void put(final int x, final int y, final int w, final int h, final int z, final BufferedImage image) {
            map.put(new Tile(x, y, z), image);
        }
        public void put(final int x, final int y, final int z, final BufferedImage image) {
            put(x, y, TILE_SIZE, TILE_SIZE, z, image);
        }
        public BufferedImage get(final int x, final int y, final int w, final int h, final int z) {
            return map.get(new Tile(x, y, z));
        }
        public BufferedImage get(final int x, final int y, final int z) {
            return get(x, y, TILE_SIZE, TILE_SIZE, z);
        }
    }

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
