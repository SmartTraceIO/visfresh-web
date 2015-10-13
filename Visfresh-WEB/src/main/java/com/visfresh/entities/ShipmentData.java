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
public class ShipmentData {
    /**
     * Shipment.
     */
    private Shipment shipment;
    /**
     * List of data from different devices asssociated by given shipment.
     */
    private final List<DeviceData> deviceData = new LinkedList<DeviceData>();

    /**
     * Default constructor.
     */
    public ShipmentData() {
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
     * @return the deviceData
     */
    public List<DeviceData> getDeviceData() {
        return deviceData;
    }
}
