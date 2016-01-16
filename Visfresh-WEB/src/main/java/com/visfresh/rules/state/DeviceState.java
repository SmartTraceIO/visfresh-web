/**
 *
 */
package com.visfresh.rules.state;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private RulesState temperatureAlerts = new RulesState();
    private Long shipmentId;

    /**
     * Default constructor.
     */
    public DeviceState() {
        super();
    }

    /**
     * @return the temperatureAlerts
     */
    public RulesState getTemperatureAlerts() {
        return temperatureAlerts;
    }
    /**
     * @param temperatureAlerts the temperatureAlerts to set
     */
    public void setTemperatureAlerts(final RulesState temperatureAlerts) {
        this.temperatureAlerts = temperatureAlerts;
    }
    /**
     * Some operations if new shipment started. I.e. cleaning of alerts history.
     * @param s shipments.
     */
    public void possibleNewShipment(final Shipment s) {
        if (shipmentId == null || !shipmentId.equals(s.getId())) {
            shipmentId = s.getId();
            this.temperatureAlerts.clear();
        }
    }
    /**
     * @return the shipmentId
     */
    public Long getShipmentId() {
        return shipmentId;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setShipmentId(final Long shipmentId) {
        this.shipmentId = shipmentId;
    }
}
