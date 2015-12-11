/**
 *
 */
package com.visfresh.io.shipment;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentTimeItem implements Comparable<SingleShipmentTimeItem> {
    private TrackerEvent event;
    private final List<Alert> alerts = new LinkedList<Alert>();
    private final List<Arrival> arrivals = new LinkedList<Arrival>();

    /**
     * Default constructor.
     */
    public SingleShipmentTimeItem() {
        super();
    }

    /**
     * @return the event
     */
    public TrackerEvent getEvent() {
        return event;
    }
    /**
     * @param event the event to set
     */
    public void setEvent(final TrackerEvent event) {
        this.event = event;
    }
    /**
     * @return the alert
     */
    public List<Alert> getAlerts() {
        return alerts;
    }
    /**
     * @return the arrival
     */
    public List<Arrival> getArrivals() {
        return arrivals;
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final SingleShipmentTimeItem o) {
        return event.compareTo(o.event);
    }
}
