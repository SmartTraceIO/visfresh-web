/**
 *
 */
package com.visfresh.rules.state;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private static final String PREFIX = "_DS_";
    private static final String SHIPMENT_ID = PREFIX + "shipmentId";
    private static final String ARRIVAL_RPOCESSED = PREFIX + "arrivalProcessed";
    private static final String OLD_SHIPMENTS_CLEAN = PREFIX + "oldShipmentsClean";

    private final RulesState temperatureAlerts = new RulesState();
    private final Map<String, String> shipmentProperties = new HashMap<String, String>();
    private Location lastLocation;

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
     * Some operations if new shipment started. I.e. cleaning of alerts history.
     * @param s shipments.
     */
    public void possibleNewShipment(final Shipment s) {
        final Long shipmentId = getShipmentId();
        if (shipmentId == null || !shipmentId.equals(s.getId())) {
            this.shipmentProperties.clear();
            temperatureAlerts.clear();

            shipmentProperties.put(SHIPMENT_ID, s.getId().toString());
        }
    }
    /**
     * @return the shipmentId
     */
    public Long getShipmentId() {
        final String str = shipmentProperties.get(SHIPMENT_ID);
        return str == null ? null : Long.valueOf(str);
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
    /**
     * @return true if old shipments alredy clean.
     */
    public boolean isOldShipmentsClean() {
        return "true".equals(shipmentProperties.get(OLD_SHIPMENTS_CLEAN));
    }
    /**
     * @param clean the oldShipmentsClean to set
     */
    public void setOldShipmentsClean(final boolean clean) {
        shipmentProperties.put(OLD_SHIPMENTS_CLEAN, clean ? "true" : "false");
    }
    public String getShipmentProperty(final String key) {
        return shipmentProperties.get(key);
    }
    public void setShipmentProperty(final String key, final String value) {
        if (key == null) {
            shipmentProperties.remove(key);
        } else {
            shipmentProperties.put(key, value);
        }
    }
    public Set<String> getShipmentKeys() {
        return new HashSet<>(shipmentProperties.keySet());
    }
    /**
     * @return the lastLocation
     */
    public Location getLastLocation() {
        return lastLocation;
    }
    /**
     * @param loc the lastLocation to set
     */
    public void setLastLocation(final Location loc) {
        this.lastLocation = loc;
    }
}
