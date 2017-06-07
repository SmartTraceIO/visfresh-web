/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ActionTaken implements EntityWithId<Long> {
    private Long id;
    private Long alert;
    private Long confirmedBy;
    private Long verifiedBy;
    private String action;
    private String comments;
    private Date time;
    private Long shipment;

    /**
     * Default constructor.
     */
    public ActionTaken() {
        super();
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
     * @return the alert
     */
    public Long getAlert() {
        return alert;
    }
    /**
     * @param alert the alert to set
     */
    public void setAlert(final Long alert) {
        this.alert = alert;
    }
    /**
     * @return the confirmedBy
     */
    public Long getConfirmedBy() {
        return confirmedBy;
    }
    /**
     * @param confirmedBy the confirmedBy to set
     */
    public void setConfirmedBy(final Long confirmedBy) {
        this.confirmedBy = confirmedBy;
    }
    /**
     * @return the verifiedBy
     */
    public Long getVerifiedBy() {
        return verifiedBy;
    }
    /**
     * @param verifiedBy the verifiedBy to set
     */
    public void setVerifiedBy(final Long verifiedBy) {
        this.verifiedBy = verifiedBy;
    }
    /**
     * @return the action
     */
    public String getAction() {
        return action;
    }
    /**
     * @param action the action to set
     */
    public void setAction(final String action) {
        this.action = action;
    }
    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }
    /**
     * @param comments the comments to set
     */
    public void setComments(final String comments) {
        this.comments = comments;
    }
    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final Date time) {
        this.time = time;
    }
    /**
     * @return the shipmentId
     */
    public Long getShipment() {
        return shipment;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setShipment(final Long shipmentId) {
        this.shipment = shipmentId;
    }
}
