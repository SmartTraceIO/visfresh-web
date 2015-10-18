/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceData {
    private Device device;
    private final List<AbstractAlert> alerts = new LinkedList<AbstractAlert>();
    private final List<TrackerEvent> events = new LinkedList<TrackerEvent>();

    /**
     * Default constructor.
     */
    public DeviceData() {
        super();
    }

    /**
     * @return the alerts
     */
    public List<AbstractAlert> getAlerts() {
        return alerts;
    }
    /**
     * @return the events
     */
    public List<TrackerEvent> getEvents() {
        return events;
    }
    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }
    /**
     * @param device the device to set
     */
    public void setDevice(final Device device) {
        this.device = device;
    }
}
