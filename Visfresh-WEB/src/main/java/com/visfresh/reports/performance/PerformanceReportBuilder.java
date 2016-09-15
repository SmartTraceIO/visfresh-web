/**
 *
 */
package com.visfresh.reports.performance;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.exception.DRException;

import org.springframework.stereotype.Component;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class PerformanceReportBuilder {

    /**
     * Default constructor.
     */
    public PerformanceReportBuilder() {
        super();
    }

    public JasperReportBuilder createReport(final PerformanceReportBean bean, final User user) throws IOException {

        final JasperReportBuilder report = DynamicReports.report();
//        report.setTitleSplitType(SplitType.IMMEDIATE);
//
//        report.title(createTitle(bean, user));
//
//        final VerticalListBuilder body = Components.verticalList();
//        body.add(createSummary(bean, user));
//        body.add(Components.gap(1, 10));
//        for (final AlertProfileStats aps : bean.getAlertProfiles()) {
//            body.add(10, createAlertProfileStats(aps, user));
//        }
//
//        report.detail(body);
//        report.setDataSource(Arrays.asList(bean));

        return report;
    }

    public void createReport(final PerformanceReportBean bean,
            final User user, final OutputStream out) throws IOException {
        final JasperReportBuilder report = createReport(bean, user);
        try {
            report.toPdf(out);
        } catch (final DRException e) {
            throw new RuntimeException("Failed to build report", e);
        }
    }
}
