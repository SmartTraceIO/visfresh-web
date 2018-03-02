/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.reports.shipment.ShipmentReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentReportDao {
    /**
     * @param s shipment.
     * @param company TODO
     * @return shipment report bean.
     */
    ShipmentReportBean createReport(Shipment s, Company company);
}
