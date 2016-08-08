/**
 *
 */
package com.visfresh.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortTrackerEventWithAlerts extends ShortTrackerEvent {
    private final Set<AlertType> alerts = new HashSet<>();

    /**
     * Default constructor.
     */
    public ShortTrackerEventWithAlerts() {
        super();
    }
    /**
     * @param e tracker event.
     */
    public ShortTrackerEventWithAlerts(final TrackerEvent e) {
        super(e);
    }
    /**
     * @param e
     */
    public ShortTrackerEventWithAlerts(final ShortTrackerEvent e) {
        super();
        this.setBattery(e.getBattery());
        this.setCreatedOn(e.getCreatedOn());
        this.setDeviceImei(e.getDeviceImei());
        this.setId(e.getId());
        this.setLatitude(e.getLatitude());
        this.setLongitude(e.getLongitude());
        this.setShipmentId(e.getShipmentId());
        this.setTemperature(e.getTemperature());
        this.setTime(e.getTime());
        this.setType(e.getType());
    }
    /**
     * @return the alerts
     */
    public Set<AlertType> getAlerts() {
        return alerts;
    }
}
