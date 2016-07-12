/**
 *
 */
package com.visfresh.services;

import java.util.List;

import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ArrivalService {
    /**
     * @param loc location.
     * @param l current coordinates.
     * @return true if given coordinates are inside of given location.
     */
    boolean isNearLocation(LocationProfile loc, Location l);
    /**
     * @param loc location.
     * @param l current coordinates.
     * @param session shipment session.
     * @return true if the shipment can be switched to arrived state
     */
    boolean handleNearLocation(LocationProfile loc, Location l,
            ShipmentSession session);
    /**
     * @param loc location.
     * @param session shipment session.
     */
    void clearLocationHistory(LocationProfile loc, ShipmentSession session);
    /**
     * @param session shipment session.
     * @return list of entered locations.
     */
    List<LocationProfile> getEnteredLocations(ShipmentSession session);
}
