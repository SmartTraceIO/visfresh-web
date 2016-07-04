/**
 *
 */
package com.visfresh.utils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationTestUtils {
    /**
     * @param lat the latitude.
     * @param distance the distance.
     * @return the longitude delta for given latitude.
     */
    public static double getLongitudeDiff(final double lat, final double distance) {
        double min = 0;
        double max = 360;
        double lon = 25.;

        while (true) {
            final double d = LocationUtils.getDistanceMeters(lat, 0., lat, lon) - distance;
            if (Math.abs(d - distance) < 0.0000001) {
                break;
            }

            if (d > distance) {
                max = lon;
            }
            if (d < distance) {
                min = lon;
            }
            lon = (max + min) / 2.;
        }

        return lon;
    }
}
