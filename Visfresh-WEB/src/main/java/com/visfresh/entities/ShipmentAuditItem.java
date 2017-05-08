/**
 *
 */
package com.visfresh.entities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.visfresh.controllers.audit.ShipmentAuditAction;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentAuditItem implements EntityWithId<Long> {
    private Long id;
    private Date time;
    private Long userId;
    private long shipmentId;
    private ShipmentAuditAction action;
    private final Map<String, String> additionalInfo = new HashMap<>();

    /**
     * Default constructor.
     */
    public ShipmentAuditItem() {
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
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }
    /**
     * @param userId the userId to set
     */
    public void setUserId(final Long userId) {
        this.userId = userId;
    }
    /**
     * @return the shipmentId
     */
    public long getShipmentId() {
        return shipmentId;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setShipmentId(final long shipmentId) {
        this.shipmentId = shipmentId;
    }
    /**
     * @return the action
     */
    public ShipmentAuditAction getAction() {
        return action;
    }
    /**
     * @param action the action to set
     */
    public void setAction(final ShipmentAuditAction action) {
        this.action = action;
    }
    /**
     * @return the additionalInfo
     */
    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }
}
