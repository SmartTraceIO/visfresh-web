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

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {PerformanceReportBuilder.class})
public class JasperDrReportBuilder implements PdfReportBuilder {
    @Autowired
    private PerformanceReportBuilder builder;

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
        builder.createReport(bean, user, out);
    }
}
