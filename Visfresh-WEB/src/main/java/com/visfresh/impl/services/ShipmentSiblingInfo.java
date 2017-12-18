/**
 *
 */
package com.visfresh.impl.services;

import java.util.HashSet;
import java.util.Set;

import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSiblingInfo implements EntityWithId<Long>{
    private final Set<Long> siblings = new HashSet<>();
    private Long shipmentId;

    /**
     * Default constructor.
     */
    public ShipmentSiblingInfo() {
        super();
    }

    /**
     * @param s
     */
    public ShipmentSiblingInfo(final Shipment s) {
        super();
        this.shipmentId = s.getId();
        if (s.getSiblings() != null) {
            this.siblings.addAll(s.getSiblings());
        }
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
}
