/**
 *
 */
package com.visfresh.rules.state;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSession {
    private static final String PREFIX = "_DS_";
    private static final String ARRIVAL_RPOCESSED = PREFIX + "arrivalProcessed";

    private final RulesState temperatureAlerts = new RulesState();
    private final Map<String, String> shipmentProperties = new ConcurrentHashMap<String, String>();
    private boolean alertsSuppressed;
    private boolean batteryLowProcessed;

    /**
     * Default constructor.
     */
    public ShipmentSession() {
        super();
    }

    /**
     * @return the temperatureAlerts
     */
    public RulesState getTemperatureAlerts() {
        return temperatureAlerts;
    }
    public void setArrivalProcessed(final boolean p) {
        shipmentProperties.put(ARRIVAL_RPOCESSED, p ? "true" : "false");
    }
    /**
     * @return the arrivalProcessed
     */
    public boolean isArrivalProcessed() {
        return "true".equals(shipmentProperties.get(ARRIVAL_RPOCESSED));
    }
    public String getShipmentProperty(final String key) {
        return shipmentProperties.get(key);
    }
    public void setShipmentProperty(final String key, final String value) {
        if (value == null) {
            shipmentProperties.remove(key);
        } else {
            shipmentProperties.put(key, value);
        }
    }
    public Set<String> getShipmentKeys() {
        return new HashSet<>(shipmentProperties.keySet());
    }
    /**
     * @param suppressed
     */
    public void setAlertsSuppressed(final boolean suppressed) {
        this.alertsSuppressed = suppressed;
    }
    /**
     * @return the alertsSuppressed
     */
    public boolean isAlertsSuppressed() {
        return alertsSuppressed;
    }
    /**
     * @return
     */
    public boolean isBatteryLowProcessed() {
        return batteryLowProcessed;
    }
    public void setBatteryLowProcessed(final boolean processed) {
        this.batteryLowProcessed = processed;
    }
}
