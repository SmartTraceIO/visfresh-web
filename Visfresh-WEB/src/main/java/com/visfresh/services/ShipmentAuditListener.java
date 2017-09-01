/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.ShipmentAuditItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentAuditListener {
    /**
     * @param item shipment audit item.
     */
    void auditItemCreated(ShipmentAuditItem item);
}
