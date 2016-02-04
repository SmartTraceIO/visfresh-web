/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum ShipmentStatus {
    /**
     * device has been switched on and default shipment data has been applied
     */
    Default("Default"),
    /**
     * new shipment data has been uploaded and the device is en-route to destination
     */
    InProgress("In Progress"),
    /**
     * new shipment data has been uploaded and the device has reached its destination
     * (device may or may not be switched off)
     */
    Complete("Complete"),
    /**
     * new shipment data has been uploaded and the device has reached its destination
     * (device may or may not be switched off)
     */
    Arrived("Arrived"),
    /**
     * new shipment data has been uploaded but for a future time of shipment
     */
    Pending("Pending");

    private final String label;

    /**
     * @param label label.
     */
    private ShipmentStatus(final String label) {
        this.label = label;
    }
    /**
     * @return
     */
    public String getLabel() {
        return label;
    }
}
