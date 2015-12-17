/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentDao extends EntityWithCompanyDaoBase<Shipment, Long> {
    /**
     * @param imei device IMEI.
     * @return active shipment for given devcie.
     */
    Shipment findActiveShipment(String imei);
    /**
     * @param imei device IMEI.
     * @return active shipments for given devcie.
     */
    List<Shipment> findActiveShipments(String imei);
}
