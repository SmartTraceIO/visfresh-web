/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentDao extends DaoBase<Shipment, Long> {
    /**
     * @param company company.
     * @param startDate
     * @param endDate
     * @param onlyWithAlerts
     * @return
     */
    List<ShipmentData> getShipmentData(Company company, Date startDate,
            Date endDate, boolean onlyWithAlerts);

    /**
     * @param imei device IMEI.
     * @return active shipment for given devcie.
     */
    Shipment findActiveShipment(String imei);
}
