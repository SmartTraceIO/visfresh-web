/**
 *
 */
package com.visfresh.controllers.lite;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipment {
    private Long shipmentId;
    private String deviceSN;
    private int tripCount;

    private String shippedFrom;
    private String shippedTo;

    private Date estArrivalDate;

    private Date actualArrivalDate;

    private int percentageComplete;

    private final Map<AlertType, Integer> alertSummary = new HashMap<AlertType, Integer>();
    private ShipmentStatus status;

    private Date shipmentDate;

    //temperature limits
    private double lowerTemperatureLimit = 0.;
    private double upperTemperatureLimit = 5.;

    private final List<LiteKeyLocation> keyLocations = new LinkedList<>();
    private int siblingCount;

    /**
     * Default constructor.
     */
    public LiteShipment() {
        super();
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
     * @return the deviceSN
     */
    public String getDeviceSN() {
        return deviceSN;
    }
    /**
     * @param deviceSN the deviceSN to set
     */
    public void setDeviceSN(final String deviceSN) {
        this.deviceSN = deviceSN;
    }
    /**
     * @return the tripCount
     */
    public int getTripCount() {
        return tripCount;
    }
    /**
     * @param tripCount the tripCount to set
     */
    public void setTripCount(final int tripCount) {
        this.tripCount = tripCount;
    }
    /**
     * @return the shippedFrom
     */
    public String getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final String shippedFrom) {
        this.shippedFrom = shippedFrom;
    }
    /**
     * @return the shippedTo
     */
    public String getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final String shippedTo) {
        this.shippedTo = shippedTo;
    }
    /**
     * @return the percentageComplete
     */
    public int getPercentageComplete() {
        return percentageComplete;
    }
    /**
     * @param percentageComplete the percentageComplete to set
     */
    public void setPercentageComplete(final int percentageComplete) {
        this.percentageComplete = percentageComplete;
    }
    /**
     * @return the status
     */
    public ShipmentStatus getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(final ShipmentStatus status) {
        this.status = status;
    }
    /**
     * @return the lowerTemperatureLimit
     */
    public double getLowerTemperatureLimit() {
        return lowerTemperatureLimit;
    }
    /**
     * @param lowerTemperatureLimit the lowerTemperatureLimit to set
     */
    public void setLowerTemperatureLimit(final double lowerTemperatureLimit) {
        this.lowerTemperatureLimit = lowerTemperatureLimit;
    }
    /**
     * @return the upperTemperatureLimit
     */
    public double getUpperTemperatureLimit() {
        return upperTemperatureLimit;
    }
    /**
     * @param upperTemperatureLimit the upperTemperatureLimit to set
     */
    public void setUpperTemperatureLimit(final double upperTemperatureLimit) {
        this.upperTemperatureLimit = upperTemperatureLimit;
    }
    /**
     * @return the alertSummary
     */
    public Map<AlertType, Integer> getAlertSummary() {
        return alertSummary;
    }
    /**
     * @return the keyLocations
     */
    public List<LiteKeyLocation> getKeyLocations() {
        return keyLocations;
    }

    /**
     * @return the estArrivalDate
     */
    public Date getEstArrivalDate() {
        return estArrivalDate;
    }

    /**
     * @param estArrivalDate the estArrivalDate to set
     */
    public void setEstArrivalDate(final Date estArrivalDate) {
        this.estArrivalDate = estArrivalDate;
    }

    /**
     * @return the actualArrivalDate
     */
    public Date getActualArrivalDate() {
        return actualArrivalDate;
    }

    /**
     * @param actualArrivalDate the actualArrivalDate to set
     */
    public void setActualArrivalDate(final Date actualArrivalDate) {
        this.actualArrivalDate = actualArrivalDate;
    }

    /**
     * @return the shipmentDate
     */
    public Date getShipmentDate() {
        return shipmentDate;
    }

    /**
     * @param shipmentDate the shipmentDate to set
     */
    public void setShipmentDate(final Date shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    /**
     * @return sibling count.
     */
    public int getSiblingCount() {
        return siblingCount;
    }
    /**
     * @param siblingCount the siblingCount to set
     */
    public void setSiblingCount(final int siblingCount) {
        this.siblingCount = siblingCount;
    }
}
