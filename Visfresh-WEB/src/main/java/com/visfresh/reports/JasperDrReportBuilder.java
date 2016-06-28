/**
 *
 */
package com.visfresh.reports;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.visfresh.entities.User;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.performance.PerformanceReportBuilder;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.reports.shipment.ShipmentReportBuilder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {
        PerformanceReportBuilder.class,
        ShipmentReportBuilder.class})
public class JasperDrReportBuilder implements PdfReportBuilder {
    @Autowired
    private PerformanceReportBuilder performanceBuilder;
    @Autowired
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
        shipmentBuilder.createReport(bean, user, out);
    }
}
