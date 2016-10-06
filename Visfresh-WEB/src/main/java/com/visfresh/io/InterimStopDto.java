/**
 *
 */
package com.visfresh.io;

import java.util.Date;

import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopDto {
    private Long id;
    private Long shipmentId;
    private Long locationId;
    private Date date;
    private int time;

    /**
     * Default constructor.
     */
    public InterimStopDto() {
        super();
    }
    /**
     * Default constructor.
     */
    public InterimStopDto(final Shipment s, final InterimStop stp) {
        super();
        setId(stp.getId());
        setShipmentId(s.getId());
        setLocationId(stp.getLocation().getId());
        setDate(stp.getDate());
        setTime(stp.getTime());
    }

    /**
     * @return the id
     */
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
    /**
     * @return the locationId
     */
    public Long getLocationId() {
        return locationId;
    }
    /**
     * @param locationId the locationId to set
     */
    public void setLocationId(final Long locationId) {
        this.locationId = locationId;
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
     * @return the time
     */
    public int getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final int time) {
        this.time = time;
    }
}
