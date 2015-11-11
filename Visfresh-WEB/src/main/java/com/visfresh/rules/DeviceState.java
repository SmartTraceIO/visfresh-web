/**
 *
 */
package com.visfresh.rules;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private RulesState temperatureAlerts = new RulesState();
    private RulesState shipmentAutoStart = new RulesState();

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
}
