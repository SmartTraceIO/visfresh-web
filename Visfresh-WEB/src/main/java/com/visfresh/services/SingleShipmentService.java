/**
 *
 */
package com.visfresh.services;

import com.visfresh.io.shipment.SingleShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SingleShipmentService {
    /**
     * @param shipmentId Shipment ID.
     * @return single shipment data.
     */
    SingleShipmentData getShipmentData(long shipmentId);
    /**
     * @param sn shipment serial number.
     * @param tripCount trip count.
     * @return single shipment data.
     */
    SingleShipmentData getShipmentData(String sn, int tripCount);
}
