/**
 *
 */
package com.visfresh.reports.geomap;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class OpenStreetMapBuilder extends AbstractGeoMapBuiler {
    public static int MAX_ZOOM = 18;
    private String serviceUrl = "http://tile.openstreetmap.org/";

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
