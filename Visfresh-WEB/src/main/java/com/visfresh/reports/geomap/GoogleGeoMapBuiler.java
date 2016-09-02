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
public class GoogleGeoMapBuiler extends AbstractGeoMapBuiler {
    /**
     * Default constructor.
     */
    public GoogleGeoMapBuiler() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.reports.geomap.AbstractGeoMapBuiler#getMaxZoom()
     */
    @Override
    public int getMaxZoom() {
        return 21;
    }
    /* (non-Javadoc)
     * @see com.visfresh.reports.geomap.AbstractGeoMapBuiler#paint(java.awt.Graphics2D, java.awt.Point, int, int, int)
     */
    @Override
    public void paint(final Graphics2D gOrig, final Point pos, final int zoom, final int width,
            final int height) throws IOException {
        final int w = Math.min(width, 612);
        final int cx = pos.x + w / 2;
        final int h = Math.min(height, 612);
        final int cy = pos.y + h / 2;

        BufferedImage img = cache.get(pos.x, pos.y, w, h, zoom);
        if (img == null) {

            final String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?center="
                    + position2lat(cy, zoom)
                    + ","
                    + position2lon(cx, zoom)
                    + "&zoom="
                    + zoom
                    + "&size="
                    + w
                    + "x"
                    + h
                    + "&maptype=roadmap";

            img = ImageIO.read(new URL(imageUrl));
            cache.put(pos.x, pos.y, w, h, zoom, img);
        }

        gOrig.drawImage(img, 0, 0, null);
//
//        for a Map of a specific Geographic point(latitude and longitude)
//
//        latitude, longitude, zoom level (0-21) and size (0-612) can be easily adapted
    }
}
