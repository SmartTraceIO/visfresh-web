/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentDao extends DaoBase<Shipment, Long> {
    /**
     * @param startDate
     * @param endDate
     * @param onlyWithAlerts
     * @return
     */
    List<ShipmentData> getShipmentData(Date startDate, Date endDate,
            String onlyWithAlerts);
}
