/**
 *
 */
package com.visfresh.rules.state;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatistics {
    private Long shipmentId;

    /**
     * Default constructor.
     */
    public ShipmentStatistics() {
        this(null);
    }

    /**
     * @param id shipment ID.
     */
    public ShipmentStatistics(final Long id) {
        super();
        this.setShipmentId(id);
    }

    /**
     * @param id the shipment ID.
     */
    public void setShipmentId(final Long id) {
        this.shipmentId = id;
    }
    /**
     * @return the shipmentId
     */
    public Long getShipmentId() {
        return shipmentId;
    }
}
