/**
 *
 */
package com.visfresh.reports;

import java.io.IOException;
import java.io.OutputStream;

import com.visfresh.entities.User;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.shipment.ShipmentReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface PdfReportBuilder {
    void createPerformanceReport(PerformanceReportBean bean, User user, final OutputStream out)
            throws IOException;
    void createShipmentReport(ShipmentReportBean bean, User user,
            OutputStream out) throws IOException;
}
