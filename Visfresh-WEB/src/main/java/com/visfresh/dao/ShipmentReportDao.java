/**
 *
 */
package com.visfresh.dao;

import java.util.List;

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
     * @param usersReceivedReports list of users received reports.
     * @return shipment report bean.
     */
    ShipmentReportBean createReport(Shipment s, List<User> usersReceivedReports);
}
