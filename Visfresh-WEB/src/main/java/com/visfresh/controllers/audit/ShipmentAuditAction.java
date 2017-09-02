/**
 *
 */
package com.visfresh.controllers.audit;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum ShipmentAuditAction {
    Autocreated(true),
    ManuallyCreated(true),
    Viewed,
    Updated(true),
    LoadedForEdit,
    SuppressedAlerts(true),
    ViewedLite,
    ManuallyCreatedFromAutostart(true),
    ViewAccessDenied,
    AddedNote(true),
    DeletedNote(true),
    UpdatedNote(true);

    final boolean changesShipment;
    ShipmentAuditAction() {
        this(false);
    }
    ShipmentAuditAction(final boolean changesShipment) {
        this.changesShipment = changesShipment;
    }
    /**
     * @return the changesShipment
     */
    public boolean isChangesShipment() {
        return changesShipment;
    }
}
