/**
 *
 */
package com.visfresh.reports;

import java.io.IOException;
import java.io.OutputStream;

import com.visfresh.entities.User;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.performance.PerformanceReportBuilder;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.reports.shipment.ShipmentReportBuilder;

import net.sf.jasperreports.engine.JRException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JasperDrReportBuilder implements PdfReportBuilder {
    private PerformanceReportBuilder performanceBuilder;
    private ShipmentReportBuilder shipmentBuilder;

    /**
     * Default constructor.
     */
    public JasperDrReportBuilder() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.reports.PdfReportBuilder#createPerformanceReport(com.visfresh.reports.performance.PerformanceReportBean, com.visfresh.entities.User, java.io.OutputStream)
     */
    @Override
    public void createPerformanceReport(final PerformanceReportBean bean, final User user,
            final OutputStream out) throws IOException {
        performanceBuilder.createReport(bean, user, out);
    }
    /* (non-Javadoc)
     * @see com.visfresh.reports.PdfReportBuilder#createPerformanceReport(com.visfresh.reports.performance.PerformanceReportBean, com.visfresh.entities.User, java.io.OutputStream)
     */
    @Override
    public void createShipmentReport(final ShipmentReportBean bean, final User user,
            final OutputStream out) throws IOException {
        try {
            shipmentBuilder.createReport(bean, user, out);
        } catch (final JRException e) {
            throw new RuntimeException(e);
        }
    }
}
