/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.reports.shipment.ShipmentReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentReportDao {
    /**
     * @param s shipment.
     * @param user user.
     * @return shipment report bean.
     */
    ShipmentReportBean createReport(Shipment s, User user);
}
