/**
 *
 */
package com.visfresh.reports;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.exception.DRException;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Language;
import com.visfresh.entities.User;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class JasperDrReportBuilder implements PdfReportBuilder {
    private static final int DEFAULT_FONT_SIZE = 10;

    /**
     * Default constructor.
     */
    public JasperDrReportBuilder() {
        super();
    }

    public JasperReportBuilder createPerformanceReport(final PerformanceReportBean bean, final User user) throws IOException {

        final JasperReportBuilder report = DynamicReports.report();
        report.setTitleSplitType(SplitType.IMMEDIATE);

        report.title(createTitle(bean, user));

        final VerticalListBuilder body = Components.verticalList();
        body.add(10, createSummary(bean, user));

        report.detail(body);
        report.setDataSource(Arrays.asList(bean));

        return report;
    }

    /**
     * @param bean
     * @param user
     * @return
     */
    private VerticalListBuilder createSummary(final PerformanceReportBean bean,
            final User user) {
        final ReportStyleBuilder defaultStyle = createStyleByFont(DEFAULT_FONT_SIZE, false);

        final VerticalListBuilder body = Components.verticalList();
        //add header
        body.add(Components.text("Summary").setStyle(createStyleByFont(DEFAULT_FONT_SIZE, true)));

        body.add(Components.text("Number of monitored shipments: "
                + bean.getNumberOfShipments()).setStyle(defaultStyle));
        body.add(Components.text("Number of trackers used: "
                + bean.getNumberOfTrackers()).setStyle(defaultStyle));
        body.add(Components.text("Average number of shipments per tracker: "
                + format(bean.getAvgShipmentsPerTracker())).setStyle(defaultStyle));
        body.add(Components.text("Average number of trackers per shipment: "
                + format(bean.getAvgTrackersPerShipment())).setStyle(defaultStyle));

        return body;
    }
    /**
     * @param bean
     * @param user
     * @return
     */
    private VerticalListBuilder createTitle(final PerformanceReportBean bean,
            final User user) {
        //title
        final VerticalListBuilder titles = Components.verticalList();

        TextFieldBuilder<String> f = Components.text("Performance Report");
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 3, true));

        titles.add(f);

        f = Components.text(createDatesSubtitle(bean.getStartDate(), bean.getEndDate(),
                user.getLanguage(), user.getTimeZone()));
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE, true));
        titles.add(f);
        return titles;
    }
    /**
     * @param size
     * @param isBold
     * @return
     */
    private ReportStyleBuilder createStyleByFont(final int size, final boolean isBold) {
        FontBuilder font = Styles.font().setFontSize(size);
        if (isBold) {
            font = font.bold();
        }

        final ReportStyleBuilder style = Styles.style().setFont(font);
        return style;
    }

    /**
     * @param startDate
     * @param endDate
     * @return
     */
    private String createDatesSubtitle(final Date startDate, final Date endDate, final Language lang, final TimeZone tz) {
//      From 1 June 2016 to 30 June 2016
        final DateFormat fmt = DateTimeUtils.createDateFormat("d MMMMMMMMMM yyyy", lang, tz);

        final StringBuilder sb = new StringBuilder();
        sb.append("From ");
        sb.append(fmt.format(startDate));
        sb.append(" to ");
        sb.append(fmt.format(endDate));
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see com.visfresh.reports.PdfReportBuilder#createPerformanceReport(java.io.OutputStream)
     */
    @Override
    public void createPerformanceReport(final PerformanceReportBean bean, final User user, final OutputStream out)
            throws IOException {
        final JasperReportBuilder report = createPerformanceReport(bean, user);
        try {
            report.toPdf(out);
        } catch (final DRException e) {
            throw new RuntimeException("Failed to build report", e);
        }
    }

    /**
     * @param value
     * @return
     */
    private String format(final double value) {
        //create US locale decimal format
        final DecimalFormat fmt = new DecimalFormat("#0.0");
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
        fmt.setDecimalFormatSymbols(decimalFormatSymbols);
        return fmt.format(value);
    }
}
