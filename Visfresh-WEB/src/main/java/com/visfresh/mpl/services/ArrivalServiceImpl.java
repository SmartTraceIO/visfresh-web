/**
 *
 */
package com.visfresh.mpl.services;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.ArrivalService;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ArrivalServiceImpl implements ArrivalService {
    /**
     * Default constructor.
     */
    public ArrivalServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#isNearLocation(com.visfresh.entities.LocationProfile, com.visfresh.entities.Location)
     */
    @Override
    public boolean isNearLocation(final LocationProfile loc, final Location l) {
        if (loc != null) {
            final double distance = getNumberOfMetersForArrival(
                    l.getLatitude(), l.getLongitude(), loc);
            return distance < 1.0;
        }
        return false;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#handleNearLocation(com.visfresh.entities.LocationProfile, com.visfresh.entities.Location, com.visfresh.rules.state.ShipmentSession)
     */
    @Override
    public boolean handleNearLocation(final LocationProfile loc, final Location l,
            final ShipmentSession session) {
        return true;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#clearLocationHistory(com.visfresh.entities.LocationProfile, com.visfresh.rules.state.ShipmentSession)
     */
    @Override
    public void clearLocationHistory(final LocationProfile loc,
            final ShipmentSession session) {
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalService#clearLocationHistory(com.visfresh.entities.LocationProfile, com.visfresh.rules.state.ShipmentSession)
     */
    @Override
    public List<LocationProfile> getEnteredLocations(final ShipmentSession session) {
        return new LinkedList<>();
    }
    /**
     * @param latitude
     * @param longitude
     * @param endLocation
     * @return
     */
    private static int getNumberOfMetersForArrival(final double latitude,
            final double longitude, final LocationProfile endLocation) {
        final Location end = endLocation.getLocation();
        double distance = LocationUtils.getDistanceMeters(latitude, longitude, end.getLatitude(), end.getLongitude());
        distance = Math.max(0., distance - endLocation.getRadius());
        return (int) Math.round(distance);
    }
}
