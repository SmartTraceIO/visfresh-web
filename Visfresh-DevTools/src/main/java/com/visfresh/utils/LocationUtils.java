/**
 *
 */
package com.visfresh.utils;

import com.visfresh.model.Location;
import com.visfresh.model.LocationProfile;

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

    public static double getDistanceMeters(final double lat1, final double lng1, final double lat2, final double lng2) {
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

    /**
     * @param l1
     * @param l2
     * @return
     */
    public static double getDistanceMeters(final Location l1, final Location l2) {
        return getDistanceMeters(l1.getLatitude(), l1.getLongitude(), l2.getLatitude(), l2.getLongitude());
    }
    /**
     * @param lat the latitude.
     * @param meters the distance.
     * @return the longitude delta for given latitude.
     */
    public static double getLongitudeDiff(final double lat, final double meters) {
        final int maxIterations = 100000;
        int i = 0;
        double min = 0;
        double max = 360;
        double lon = 25.;

        while (true) {
            final double d = LocationUtils.getDistanceMeters(lat, 0., lat, lon) - meters;
            if (Math.abs(d - meters) < 0.0000001) {
                break;
            }

            if (d > meters) {
                max = lon;
            }
            if (d < meters) {
                min = lon;
            }
            lon = (max + min) / 2.;

            i++;
            if (i > maxIterations) {
                throw new RuntimeException("Max iterations " + maxIterations + " exceed");
            }
        }

        return lon;
    }
    public static boolean isNearLocation(final LocationProfile loc, final Location l) {
        if (loc != null) {
            final int distance = getNumberOfMetersForArrival(
                    l.getLatitude(), l.getLongitude(), loc);
            return distance == 0;
        }
        return false;
    }
    /**
     * @param latitude
     * @param longitude
     * @param endLocation
     * @return
     */
    public static int getNumberOfMetersForArrival(final double latitude,
            final double longitude, final LocationProfile endLocation) {
        final Location end = endLocation.getLocation();
        double distance = LocationUtils.getDistanceMeters(latitude, longitude, end.getLatitude(), end.getLongitude());
        distance = Math.max(0., distance - endLocation.getRadius());
        return (int) Math.round(distance);
    }

    public static void main(final String[] args) {
//        | 356689 | 2016-08-09 18:05:54 | AUT  | -33.858152 | 151.052989 |
//        mysql> select name, latitude, longitude, radius from locationprofiles where id = 48;
//        +---------------------------+--------------------+-------------------+--------+
//        | name                      | latitude           | longitude         | radius |
//        +---------------------------+--------------------+-------------------+--------+
//        | DeCosti Office - Lidcombe | -33.85938228926081 | 151.0558270812362 |   1000 |
//        +---------------------------+--------------------+-------------------+--------+
        System.out.println(getDistanceMeters(-33.812193, 151.109783, -33.85938228926081, 151.0558270812362));
    }
}
