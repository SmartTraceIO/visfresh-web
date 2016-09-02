/**
 *
 */
package com.visfresh.reports.geomap;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class OpenStreetMapBuilder extends AbstractGeoMapBuiler {
    public static int MAX_ZOOM = 18;
    private String serviceUrl = "http://tile.openstreetmap.org/";
    private final TileCache cache = new TileCache();

    private static class Tile {
        public final int x, y, z;
        public Tile(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            result = prime * result + z;
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
            return true;
        }

    }

    private static class TileCache {
        private static final int CACHE_SIZE = 256;

        @SuppressWarnings("serial")
        private LinkedHashMap<Tile,BufferedImage> map = new LinkedHashMap<Tile,BufferedImage>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(final java.util.Map.Entry<Tile,BufferedImage> eldest) {
                return size() > CACHE_SIZE;
            }
        };
        public void put(final int x, final int y, final int z, final BufferedImage image) {
            map.put(new Tile(x, y, z), image);
        }
        public BufferedImage get(final int x, final int y, final int z) {
            return map.get(new Tile(x, y, z));
        }
    }

    /**
     * Default constructor.
     */
    public OpenStreetMapBuilder() {
        super();
    }

    //-------------------------------------------------------------------------

    public String getTileString(final int xtile, final int ytile, final int zoom) {
        final String number = ("" + zoom + "/" + xtile + "/" + ytile);
        final String url = serviceUrl + number + ".png";
        return url;
    }
    @Override
    public void paint(final Graphics2D gOrig,
            final Point mapPosition,
            final int zoom,
            final int width,
            final int height) throws IOException {

        final int tileCount = 1 << zoom;

        final Graphics2D g = (Graphics2D) gOrig.create();
        try {
            final int x0 = (int) Math.floor(((double) mapPosition.x) / TILE_SIZE);
            final int y0 = (int) Math.floor(((double) mapPosition.y) / TILE_SIZE);
            final int x1 = (int) Math.ceil(((double) mapPosition.x + width) / TILE_SIZE);
            final int y1 = (int) Math.ceil(((double) mapPosition.y + height) / TILE_SIZE);

            int dy = y0 * TILE_SIZE - mapPosition.y;

            for (int y = y0; y < y1 && dy < height; ++y) {
                int dx = x0 * TILE_SIZE - mapPosition.x;
                for (int x = x0; x < x1 && dx < width; ++x) {
                    if (y < tileCount) {
                        final int numx = x < tileCount? x : x % tileCount;

                        BufferedImage image = cache.get(numx, y, zoom);
                        if (image == null) {
                            final String url = getTileString(numx, y, zoom);
                            //System.err.println("loading: " + url);
                            image = ImageIO.read(new URL(url));
                            cache.put(numx, y, zoom, image);
                        }

                        if (image != null) {
                            g.drawImage(image, dx, dy, null);
                        }
                    }

                    dx += TILE_SIZE;
                }
                dy += TILE_SIZE;
            }
        } finally {
            g.dispose();
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.reports.geomap.AbstractGeoMapBuiler#getMaxZoom()
     */
    @Override
    public int getMaxZoom() {
        return MAX_ZOOM;
    }
}
