/**
 *
 */
package com.visfresh.impl.services;

import java.util.HashSet;
import java.util.Set;

import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSiblingInfo implements EntityWithId<Long>{
    private final Set<Long> siblings = new HashSet<>();
    private Long shipmentId;
    private boolean isBeacon;

    /**
     * Default constructor.
     */
    public ShipmentSiblingInfo() {
        super();
    }

    /**
     * @return
     */
    public Set<Long> getSiblings() {
        return siblings;
    }

    /**
     * @return shipment ID.
     */
    @Override
    public Long getId() {
        return shipmentId;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setId(final Long shipmentId) {
        this.shipmentId = shipmentId;
    }
    /**
     * @param isBeacon the isBeacon to set
     */
    public void setBeacon(final boolean isBeacon) {
        this.isBeacon = isBeacon;
    }
    /**
     * @return the isBeacon
     */
    public boolean isBeacon() {
        return isBeacon;
    }
}
