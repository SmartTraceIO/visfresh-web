/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.reports.JasperDrReportBuilder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockPdfReportBuilder extends JasperDrReportBuilder {
    /**
     * Default constructor.
     */
    public MockPdfReportBuilder() {
        super();
    }
}
