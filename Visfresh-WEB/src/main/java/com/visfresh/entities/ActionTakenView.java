/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ActionTakenView extends ActionTaken {
    private Date alertTime;
    private AlertRule alertRule;
    private String confirmedByEmail;
    private String confirmedByName;
    private String verifiedByEmail;
    private String verifiedByName;
    private String shipmentSn;
    private int shipmentTripCount;

    /**
     * Default constructor.
     */
    public ActionTakenView() {
        super();
    }

    /**
     * @return the alertTime
     */
    public Date getAlertTime() {
        return alertTime;
    }
    /**
     * @param alertTime the alertTime to set
     */
    public void setAlertTime(final Date alertTime) {
        this.alertTime = alertTime;
    }
    /**
     * @return the alertDescription
     */
    public AlertRule getAlertRule() {
        return alertRule;
    }
    /**
     * @param r alert rule.
     */
    public void setAlertRule(final AlertRule r) {
        this.alertRule = r;
    }
    /**
     * @return the confirmedByEmail
     */
    public String getConfirmedByEmail() {
        return confirmedByEmail;
    }
    /**
     * @param confirmedByEmail the confirmedByEmail to set
     */
    public void setConfirmedByEmail(final String confirmedByEmail) {
        this.confirmedByEmail = confirmedByEmail;
    }
    /**
     * @return the confirmedByName
     */
    public String getConfirmedByName() {
        return confirmedByName;
    }
    /**
     * @param confirmedByName the confirmedByName to set
     */
    public void setConfirmedByName(final String confirmedByName) {
        this.confirmedByName = confirmedByName;
    }
    /**
     * @return the verifiedByEmail
     */
    public String getVerifiedByEmail() {
        return verifiedByEmail;
    }
    /**
     * @param verifiedByEmail the verifiedByEmail to set
     */
    public void setVerifiedByEmail(final String verifiedByEmail) {
        this.verifiedByEmail = verifiedByEmail;
    }
    /**
     * @return the verifiedByName
     */
    public String getVerifiedByName() {
        return verifiedByName;
    }
    /**
     * @param verifiedByName the verifiedByName to set
     */
    public void setVerifiedByName(final String verifiedByName) {
        this.verifiedByName = verifiedByName;
    }
    /**
     * @return the shipmentSn
     */
    public String getShipmentSn() {
        return shipmentSn;
    }
    /**
     * @param shipmentSn the shipmentSn to set
     */
    public void setShipmentSn(final String shipmentSn) {
        this.shipmentSn = shipmentSn;
    }
    /**
     * @return the shipmentTripCount
     */
    public int getShipmentTripCount() {
        return shipmentTripCount;
    }
    /**
     * @param shipmentTripCount the shipmentTripCount to set
     */
    public void setShipmentTripCount(final int shipmentTripCount) {
        this.shipmentTripCount = shipmentTripCount;
    }
}
