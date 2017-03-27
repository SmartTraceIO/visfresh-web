/**
 *
 */
package com.visfresh.dao.impl.lite;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@FunctionalInterface
public interface ShipmentSwitchListener {
    /**
     * @param oldShipmentId ID of switched from shipment.
     * @param newShipmentId ID of switched to shipment.
     */
    void shipmentSwitched(Long oldShipmentId, Long newShipmentId);
}
