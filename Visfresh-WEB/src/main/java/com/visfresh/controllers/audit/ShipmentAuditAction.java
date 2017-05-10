/**
 *
 */
package com.visfresh.controllers.audit;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum ShipmentAuditAction {
    Autocreated,
    ManuallyCreated,
    Viewed,
    Updated,
    LoadedForEdit,
    SuppressedAlerts,
    ViewedLite,
    ManuallyCreatedFromAutostart,
    ViewAccessDenied,
    AddedNote,
    DeletedNote,
    UpdatedNote
}
