/**
 *
 */
package com.visfresh.reports.geomap;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RenderedMap {
    private BufferedImage map;
    private int zoom;
    private Point mapLocation;

    /**
     * Default constructor.
     */
    public RenderedMap() {
        super();
    }

    public BufferedImage getMap() {
        return map;
    }
    public void setMap(final BufferedImage map) {
        this.map = map;
    }
    public int getZoom() {
        return zoom;
    }
    public void setZoom(final int zoom) {
        this.zoom = zoom;
    }
    public void setMapLocation(final Point point) {
        mapLocation = point;
    }
    public Point getMapLocation() {
        return mapLocation;
    }
}
