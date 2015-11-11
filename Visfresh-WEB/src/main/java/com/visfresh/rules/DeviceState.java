/**
 *
 */
package com.visfresh.rules;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private RulesState temperatureAlerts = new RulesState();
    private RulesState shipmentAutoStart = new RulesState();
    private List<TemperaturePoint> temperatureHistory = new LinkedList<TemperaturePoint>();

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
     * @return the shipmentAutoStart
     */
    public RulesState getShipmentAutoStart() {
        return shipmentAutoStart;
    }
    /**
     * @param shipmentAutoStart the shipmentAutoStart to set
     */
    public void setShipmentAutoStart(final RulesState shipmentAutoStart) {
        this.shipmentAutoStart = shipmentAutoStart;
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
}
