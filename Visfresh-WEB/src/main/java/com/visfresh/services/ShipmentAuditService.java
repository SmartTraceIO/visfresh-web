/**
 *
 */
package com.visfresh.services;

import java.util.Map;

import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentAuditService {
    /**
     * @param shipmentId shipment.
     * @param user user, can be null.
     * @param action action.
     * @param details action details, can be null.
     */
    void handleShipmentAction(Long shipmentId, User user,
            ShipmentAuditAction action, Map<String, String> details);
    void addAuditListener(ShipmentAuditListener l);
    void removeAuditListener(ShipmentAuditListener l);
}
