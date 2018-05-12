/**
 *
 */
package com.visfresh.impl.services;

import java.util.List;

import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MatchedStartLocationChecker extends AutoStartShipmentServiceImpl {

    /**
     * Default constructor.
     */
    public MatchedStartLocationChecker() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.services.AutoStartShipmentServiceImpl#getSortedMatchedStartLocations(java.util.List, java.lang.Double, java.lang.Double)
     */
    @Override
    public List<LocationProfile> getSortedMatchedStartLocations(
            final List<LocationProfile> profiles, final Double latitude, final Double longitude) {
        return super.getSortedMatchedStartLocations(profiles, latitude, longitude);
    }

    public static void main(final String[] args) {
        //{
        //    "locationId": 46,
        //    "locationName": "PM Fresh (Homebush)",
        //    "companyName": "Primo MF",
        //    "notes": null,
        //    "address": "30 Carter St, Lidcombe NSW 2141, Australia",
        //    "location": {
        //        "lat": -33.84992433727756,
        //        "lon": 151.05560392141342
        //    },
        //    "radiusMeters": 1000,
        //    "startFlag": "Y",
        //    "interimFlag": "N",
        //    "endFlag": "Y"
        //}
        final LocationProfile lp = new LocationProfile();
        lp.setId(46L);
        lp.setName("PM Fresh (Homebush)");
        lp.setCompanyName("Primo MF");
        lp.setAddress("30 Carter St, Lidcombe NSW 2141, Australia");
        lp.setRadius(1000);
        lp.getLocation().setLatitude(-33.84992433727756);
        lp.getLocation().setLongitude(151.05560392141342);

        final Location[] locs = {
            new Location(-33.7986168, 151.1744258),
            new Location(-33.7986168, 151.1744258),
            new Location(-33.7986167, 151.1744257),
            new Location(-33.7986167, 151.1744257),
            new Location(-33.7985878, 151.1743999),
            new Location(-33.7985878, 151.1743999),
            new Location(-33.7986173, 151.1744253),
            new Location(-33.7986161,  151.174426),
            new Location(-33.7986168, 151.1744258),
            new Location(-33.7986218, 151.1744202)
        };

        for (final Location l : locs) {
            final int distance = (int) LocationUtils.getDistanceMeters(
                lp.getLocation().getLatitude(),
                lp.getLocation().getLongitude(),
                l.getLatitude(),
                l.getLongitude());

            System.out.println("Location " + l + ", min distance: "
                    + distance + ", matchers: " + (Math.max(0, distance - lp.getRadius()) == 0));
        }
    }
}
