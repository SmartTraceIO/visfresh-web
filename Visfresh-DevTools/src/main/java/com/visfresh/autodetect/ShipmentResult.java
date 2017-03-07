/**
 *
 */
package com.visfresh.autodetect;

import java.io.PrintStream;
import java.util.List;

import com.visfresh.model.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentResult {
    private List<LocationResult> locationResults;
    private Shipment shipment;
    private List<DeviceMessage> messages;
    private double minDistance;

    /**
     * Default constructor.
     */
    public ShipmentResult() {
        super();
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
     * @return
     */
    protected PrintStream getOutput() {
        return System.out;
    }
    /**
     * @return the locationResults
     */
    public List<LocationResult> getLocationResults() {
        return locationResults;
    }
    /**
     * @param locationResults the locationResults to set
     */
    public void setLocationResults(final List<LocationResult> locationResults) {
        this.locationResults = locationResults;
    }
    /**
     * @return the messages
     */
    public List<DeviceMessage> getMessages() {
        return messages;
    }
    /**
     * @param messages the messages to set
     */
    public void setMessages(final List<DeviceMessage> messages) {
        this.messages = messages;
    }
    /**
     * @return the minDinstance
     */
    public double getMinDistance() {
        return minDistance;
    }
    /**
     * @param minDinstance the minDinstance to set
     */
    public void setMinDistance(final double minDinstance) {
        this.minDistance = minDinstance;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (getShipment() != null) {
            return getShipment().toString();
        }
        return super.toString();
    }
}
