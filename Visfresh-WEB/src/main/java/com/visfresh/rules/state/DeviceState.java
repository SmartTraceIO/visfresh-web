/**
 *
 */
package com.visfresh.rules.state;

import java.util.Date;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private RulesState temperatureAlerts = new RulesState();
    private volatile Long shipmentId;
    private volatile boolean arrivalProcessed;
    private volatile Date startShipmentDate;
    private volatile boolean oldShipmentsClean;

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
     * @param temperatureAlerts the temperatureAlerts to set
     */
    public void setTemperatureAlerts(final RulesState temperatureAlerts) {
        this.temperatureAlerts = temperatureAlerts;
    }
    /**
     * Some operations if new shipment started. I.e. cleaning of alerts history.
     * @param s shipments.
     */
    public void possibleNewShipment(final Shipment s) {
        if (shipmentId == null || !shipmentId.equals(s.getId())) {
            shipmentId = s.getId();
            startShipmentDate = new Date();
            temperatureAlerts.clear();
            arrivalProcessed = false;
            setOldShipmentsClean(false);
        }
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
    public void setArrivalProcessed(final boolean p) {
        arrivalProcessed = p;
    }
    /**
     * @return the arrivalProcessed
     */
    public boolean isArrivalProcessed() {
        return arrivalProcessed;
    }
    /**
     * @return start shipment date.
     */
    public Date getStartShipmentDate() {
        return startShipmentDate;
    }
    /**
     * @param startShipmentDate the startShipmentDate to set
     */
    public void setStartShipmentDate(final Date startShipmentDate) {
        this.startShipmentDate = startShipmentDate;
    }
    /**
     * @return true if old shipments alredy clean.
     */
    public boolean isOldShipmentsClean() {
        return oldShipmentsClean;
    }
    /**
     * @param clean the oldShipmentsClean to set
     */
    public void setOldShipmentsClean(final boolean clean) {
        this.oldShipmentsClean = clean;
    }
}
