/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserProfile {
    private final List<Shipment> shipments = new LinkedList<Shipment>();

    /**
     * @param user.
     */
    public UserProfile() {
        super();
    }

    /**
     * @return the shipments
     */
    public List<Shipment> getShipments() {
        return shipments;
    }
}
