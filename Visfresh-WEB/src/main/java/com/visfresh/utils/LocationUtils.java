/**
 *
 */
package com.visfresh.utils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class LocationUtils {
    /**
     * Default constructor.
     */
    private LocationUtils() {
        super();
    }

    public static double distFrom(final double lat1, final double lng1, final double lat2, final double lng2) {
        final double earthRadius = 6371000; // meters
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLng = Math.toRadians(lng2 - lng1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
