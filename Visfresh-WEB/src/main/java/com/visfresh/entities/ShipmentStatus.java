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
     * new shipment data has been uploaded but for a future time of shipment
     */
    Pending("Pending");

    private final String displayName;

    /**
     * @param displayName status display name.
     */
    ShipmentStatus(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }
    /**
     * @param name the status display name.
     * @return status for given display name.
     */
    public static ShipmentStatus getByDisplayName(final String name) {
        for (final ShipmentStatus s : values()) {
            if (s.getDisplayName().equals(name)) {
                return s;
            }
        }

        throw new IllegalArgumentException("Undefinded status: " + name);
    }
}
