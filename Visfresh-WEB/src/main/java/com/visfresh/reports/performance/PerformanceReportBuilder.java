/**
 *
 */
package com.visfresh.reports.performance;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Language;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class PerformanceReportBuilder {
    private static final int DEFAULT_FONT_SIZE = 10;
    private final ReportStyleBuilder defaultStyle = createStyleByFont(DEFAULT_FONT_SIZE, false);
    private final ReportStyleBuilder defaultStyleBold = createStyleByFont(DEFAULT_FONT_SIZE, true);

    /**
     * Default constructor.
     */
    public PerformanceReportBuilder() {
        super();
    }

    public JasperReportBuilder createPerformanceReport(final PerformanceReportBean bean, final User user) throws IOException {

        final JasperReportBuilder report = DynamicReports.report();
        report.setTitleSplitType(SplitType.IMMEDIATE);

        report.title(createTitle(bean, user));

        final VerticalListBuilder body = Components.verticalList();
        body.add(createSummary(bean, user));
        body.add(Components.gap(1, 10));
        for (final AlertProfileStats aps : bean.getAlertProfiles()) {
            body.add(10, createAlertProfileStats(aps, user));
        }

        report.detail(body);
        report.setDataSource(Arrays.asList(bean));

        return report;
    }

    /**
     * @param aps
     * @param user
     * @return
     */
    private VerticalListBuilder createAlertProfileStats(
            final AlertProfileStats aps, final User user) {
        final VerticalListBuilder alertProfile = Components.verticalList();

        //add header
        alertProfile.add(Components.text("Profile: " + aps.getName()).setStyle(defaultStyleBold));

        //add summary as subreport
        final JasperReportBuilder summary = DynamicReports.report();
        summary.columns(Columns.column("name", String.class));
        summary.columns(Columns.column("value", String.class));

        //create summary data source
        final List<Map<String, ?>> rows = new LinkedList<>();
        Map<String, String> row = new HashMap<>();
        row.put("name", "Total time of monitoring:");
        row.put("value", Integer.toString(getNumberOfHours(aps.getTotalMonitoringTime())));
        rows.add(row);
//         4.5°C
        row = new HashMap<>();
        row.put("name", "Average temperature:");
        row.put("value", LocalizationUtils.getTemperatureString(aps.getAvgTemperature(), user.getTemperatureUnits()));
        rows.add(row);
//         0.4
        row = new HashMap<>();
        row.put("name", "Standard deviation:");
        row.put("value", format(aps.getStandardDeviation()));
        rows.add(row);

        summary.setDataSource(new JRMapCollectionDataSource(rows));

        alertProfile.add(Components.subreport(summary));
        final int gap = 15;
        alertProfile.add(Components.gap(1, gap));

        //add temperature rules
        for (final TemperatureRuleStats rule : aps.getTemperatureRules()) {
            alertProfile.add(gap, createRuleStats(rule, user));
        }

        return alertProfile;
    }

    /**
     * @param rule
     * @param user
     * @return
     */
    private SubreportBuilder createRuleStats(final TemperatureRuleStats rule,
            final User user) {
        final JasperReportBuilder report = new JasperReportBuilder();
        report.columns(Columns.column("name", String.class));
        report.columns(Columns.column("value", String.class));

        final List<Map<String, ?>> rows = new LinkedList<>();
        Map<String, String> row = new HashMap<>();
        //create rule description row
        row.put("name", getRuleDescription(rule.getRule(), user));
        row.put("value", createTimeString(rule.getTotalTime()));
        rows.add(row);

        // biggest exception rows
        row = new HashMap<>();
        row.put("name", "Biggest exceptions:");

        sortExceptions(rule.getBiggestExceptions());
        final List<String> bidgestExceptions = new LinkedList<>();
        for (final BiggestTemperatureException exc : rule.getBiggestExceptions()) {
            if (exc.getTime() > 0) {
                bidgestExceptions.add(createBidgestExceptionPart(exc, user));
            }
        }

        row.put("value", StringUtils.combine(bidgestExceptions, ",\n"));
        rows.add(row);

        report.setDataSource(new JRMapCollectionDataSource(rows));
        return Components.subreport(report);
    }

    /**
     * @param rule
     * @return
     */
    private String getRuleDescription(final TemperatureRule rule, final User user) {
        final StringBuilder sb = new StringBuilder();

        //5°C): 2hrs 52min
        //8°C): 1hrs 12min
        //0°C): 22min
        //-2C):
        switch (rule.getType()) {
            case Cold:
                sb.append("Total time below low temp (");
                break;
            case CriticalCold:
                sb.append("Total time below critical low temp (");
                break;
            case CriticalHot:
                sb.append("Total time above critical high temp (");
                break;
            case Hot:
                sb.append("Total time above high temp (");
                break;
            default:
                throw new RuntimeException("Unexpected rule type: " + rule.getType());
        }
        sb.append(LocalizationUtils.getTemperatureString(rule.getTemperature(), user.getTemperatureUnits()));
        sb.append("): ");
        sb.append(createTimeString(rule.getTimeOutMinutes() * 60 * 1000l));
        return sb.toString();
    }

    /**
     * @param biggestExceptions
     */
    private void sortExceptions(final List<BiggestTemperatureException> biggestExceptions) {
        Collections.sort(biggestExceptions, new Comparator<BiggestTemperatureException>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final BiggestTemperatureException o1,
                    final BiggestTemperatureException o2) {
                return new Long(o2.getTime()).compareTo(new Long(o1.getTime()));
            }
        });
    }

    /**
     * @param exc exception.
     * @param user user.
     * @return
     */
    private String createBidgestExceptionPart(final BiggestTemperatureException exc,
            final User user) {
        final StringBuilder sb = new StringBuilder();

        //122(4) for 1hr 1min
        sb.append(exc.getSerialNumber());
        sb.append('(');
        sb.append(exc.getTripCount());
        sb.append(") for ");
        sb.append(createTimeString(exc.getTime()));

        return sb.toString();
    }

    /**
     * @param time
     * @return
     */
    private String createTimeString(final long time) {
        final StringBuilder sb = new StringBuilder();
        //1hr 1min
        int minutes = (int) (time / (1000L * 60));
        final int hours = minutes / 60;
        minutes = minutes - hours * 60;

        if (hours > 0) {
            sb.append(hours);
            sb.append("hr");
        }
        if (minutes > 0) {
            if (hours > 0) {
                sb.append(' ');
            }
            sb.append(minutes);
            sb.append("min");
        }

        return sb.toString();
    }

    /**
     * @param totalMonitoringTime
     * @return
     */
    private int getNumberOfHours(final long totalMonitoringTime) {
        return (int) (totalMonitoringTime / (1000L * 60 * 60));
    }

    /**
     * @param bean
     * @param user
     * @return
     */
    private VerticalListBuilder createSummary(final PerformanceReportBean bean,
            final User user) {

        final VerticalListBuilder body = Components.verticalList();
        //add header
        body.add(Components.text("Summary").setStyle(defaultStyleBold));

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
        titles.add(Components.gap(0, 20));
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

    public void createReport(final PerformanceReportBean bean, final User user, final OutputStream out)
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
