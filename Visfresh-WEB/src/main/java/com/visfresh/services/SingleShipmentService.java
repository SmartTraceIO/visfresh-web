/**
 *
 */
package com.visfresh.services;

import com.visfresh.io.shipment.SingleShipmentBean;

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
    SingleShipmentBean createLiteData(Long shipmentId, String sn, Integer tripCount);

    /**
     * @param bean bean.
     * @param includeReadings whether or not should include the readings to result bean
     */
    void addReadingsInfo(SingleShipmentBean bean, boolean includeReadings);
}
