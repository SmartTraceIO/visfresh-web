/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentDao extends DaoBase<Shipment, Long> {
    /**
     * @param imei device IMEI.
     * @return active shipment for given devcie.
     */
    Shipment findActiveShipment(String imei);

    /**
     * @param company company.
     * @return list of shipments.
     */
    List<Shipment> findByCompany(Company company);
}
