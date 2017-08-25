/**
 *
 */
package com.visfresh.services;

import java.util.function.Consumer;

import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SingleShipmentService {
    /**
     * @param shipmentId shipment ID.
     * @param sn device serial number.
     * @param tripCount shipment trip count.
     * @return single shipment data without readings.
     */
    SingleShipmentBean createLiteData(long shipmentId);
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
    /**
     * @param bean
     * @param c
     */
    void processReadings(SingleShipmentBean bean, Consumer<SingleShipmentLocationBean> c);
}
