/**
 *
 */
package com.visfresh.reports;

import java.io.IOException;
import java.io.OutputStream;

import com.visfresh.entities.User;
import com.visfresh.reports.performance.PerformanceReportBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface PdfReportBuilder {
    public void createPerformanceReport(PerformanceReportBean bean, User user, final OutputStream out)
            throws IOException;
}
