/**
 *
 */
package com.visfresh.rules.state;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.Shipment;
import com.visfresh.rules.TemperaturePoint;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private RulesState temperatureAlerts = new RulesState();
    private List<TemperaturePoint> temperatureHistory = new LinkedList<TemperaturePoint>();
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
     * @param point temperature point.
     */
    public void addToHistory(final TemperaturePoint point) {
        temperatureHistory.add(point);
    }
    /**
     * Cleares the temperature history.
     */
    public void clearTemperatureHistory() {
        temperatureHistory.clear();
    }
    /**
     * Some operations if new shipment started. I.e. cleaning of alerts history.
     * @param s shipments.
     */
    public void possibleNewShipment(final Shipment s) {
        if (shipmentId == null || shipmentId.equals(s.getId())) {
            shipmentId = s.getId();
            this.temperatureAlerts.clear();
            this.temperatureHistory.clear();
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
