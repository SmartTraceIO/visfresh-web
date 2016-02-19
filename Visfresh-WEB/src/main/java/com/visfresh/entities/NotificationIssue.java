/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class NotificationIssue implements EntityWithId<Long>, Comparable<NotificationIssue> {
    /**
     * Entity ID.
     */
    private Long id;
    /**
     * Date of occurrence.
     */
    private Date date;
    /**
     * Device.
     */
    private Device device;
    /**
     * Shipment;
     */
    private Shipment shipment;
    /**
     * The trackere event ID.
     */
    private Long trackerEventId;

    /**
     * Default constructor.
     */
    public NotificationIssue() {
        super();
    }
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(final Date date) {
        this.date = date;
    }
    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the device
     */
    public Device getDevice() {
        return device;
    }
    /**
     * @param device the device
     */
    public void setDevice(final Device device) {
        this.device = device;
    }
    /**
     * @return the shipment
     */
    public Shipment getShipment() {
        return shipment;
    }
    /**
     * @param shipment the shipment to set
     */
    public void setShipment(final Shipment shipment) {
        this.shipment = shipment;
    }
    /**
     * @return the trackerEventId
     */
    public Long getTrackerEventId() {
        return trackerEventId;
    }
    /**
     * @param trackerEventId the trackerEventId to set
     */
    public void setTrackerEventId(final Long trackerEventId) {
        this.trackerEventId = trackerEventId;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final NotificationIssue o) {
        return getDate().compareTo(o.getDate());
    }
}
