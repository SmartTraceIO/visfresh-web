/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.io.shipment.SingleShipmentBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SingleShipmentBeanDao {
    /**
     * @param bean shipment bean.
     */
    void saveShipmentBean(SingleShipmentBean bean);
    /**
     * @param shipmentId shipment ID.
     * @return list with given shipment bean and sibling beans.
     */
    List<SingleShipmentBean> getShipmentBeanIncludeSiblings(Long shipmentId);
    /**
     * @param sn serial number.
     * @param tripCount trip count.
     * @return list with given shipment bean and sibling beans.
     */
    List<SingleShipmentBean> getShipmentBeanIncludeSiblings(String sn, Integer tripCount);
    /**
     * @param device
     */
    void clearShipmentBeanForDevice(String device);
    /**
     * @param shipment
     */
    void clearShipmentBean(Long shipment);
}
