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
    Default(),
    /**
     * new shipment data has been uploaded and the device is en-route to destination
     */
    InProgress(),
    /**
     * new shipment data has been uploaded and the device has reached its destination
     * (device may or may not be switched off)
     */
    Ended(true),
    /**
     * new shipment data has been uploaded and the device has reached its destination
     * (device may or may not be switched off)
     */
    Arrived(true),
    /**
     * new shipment data has been uploaded but for a future time of shipment
     */
    Pending();

    private final boolean isFinal;

    /**
     * Default constructor.
     */
    private ShipmentStatus() {
        this(false);
    }
    /**
     * @param isFinal final status.
     */
    private ShipmentStatus(final boolean isFinal) {
        this.isFinal = isFinal;
    }
    /**
     * @return
     */
    public boolean isFinal() {
        return isFinal;
    }
}
