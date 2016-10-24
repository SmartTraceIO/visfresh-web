/**
 *
 */
package com.visfresh.mock;

import java.io.IOException;
import java.io.OutputStream;

import org.springframework.stereotype.Component;

import com.visfresh.entities.User;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.shipment.ShipmentReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockPdfReportBuilder implements PdfReportBuilder {
    /**
     * Default constructor.
     */
    public MockPdfReportBuilder() {
        super();
    }
    @Override
    public void createShipmentReport(final ShipmentReportBean bean, final User user,
            final OutputStream out) throws IOException {
        out.write("create shipment report response pdf".getBytes());
    }

    @Override
    public void createPerformanceReport(final PerformanceReportBean bean, final User user,
            final OutputStream out) throws IOException {
        out.write("create performance report response pdf".getBytes());
    }
}
