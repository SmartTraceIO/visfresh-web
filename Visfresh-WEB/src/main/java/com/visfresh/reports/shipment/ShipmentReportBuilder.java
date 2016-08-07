/**
 *
 */
package com.visfresh.reports.shipment;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
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

import com.visfresh.entities.Device;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentReportBuilder {
    private static final int DEFAULT_FONT_SIZE = 10;
    private final ReportStyleBuilder defaultStyle = createStyleByFont(DEFAULT_FONT_SIZE, false);
    private final ReportStyleBuilder defaultStyleBold = createStyleByFont(DEFAULT_FONT_SIZE, true);
    final List<String> twoColumns = new ArrayList<>(2);

    /**
     * Default constructor.
     */
    public ShipmentReportBuilder() {
        super();
        twoColumns.add("name");
        twoColumns.add("value");
    }


    public void createReport(final ShipmentReportBean bean,
            final User user, final OutputStream out)
            throws IOException {
        final JasperReportBuilder report = createReport(bean, user);
        try {
            report.toPdf(out);
        } catch (final DRException e) {
            throw new RuntimeException("Failed to build report", e);
        }
    }
    public JasperReportBuilder createReport(
            final ShipmentReportBean bean, final User user) throws IOException {

        final JasperReportBuilder report = DynamicReports.report();
        report.setTitleSplitType(SplitType.IMMEDIATE);

        report.title(createTitle(bean));

        final VerticalListBuilder body = Components.verticalList();
        final int gap = 15;

        //add shipment details
        List<Map<String, ?>> rows = shipmentDetailsToRows(bean, user);
        body.add(createTableWithTitle("Shipment Details", rows));
        body.add(Components.gap(1, gap));

        //create goods
        rows = goodsAsRows(bean, user);
        body.add(createTableWithTitle("Goods", rows));
        body.add(Components.gap(1, gap));

        //temperature history
        body.add(Components.text("Temperature History").setStyle(defaultStyleBold));
        if (bean.getAlertProfile() == null) {
            body.add(Components.gap(1, gap));
        } else {
            body.add(createTemperatureHistory(bean, user));
            body.add(Components.gap(1, gap));
        }

        //arrival
        if (bean.getArrival() != null) {
            rows = arrivalToRows(bean.getArrival(), user);
            body.add(createTableWithTitle("Arrival", rows));
            body.add(Components.gap(1, gap));
        }

        //last reading
        final int numReadings = bean.getReadings().size();
        if (numReadings > 0) {
            rows = lastReadingToRows(
                    bean.getReadings().get(numReadings - 1),
                    bean.isSuppressFurtherAlerts(),
                    user);
            body.add(createTableWithTitle("Last Reading", rows));
            body.add(Components.gap(1, gap));

            //TODO add maps
        }

        //add body to report
        report.detail(body);
        //this data source is not used, but required for
        //show the content
        report.setDataSource(Arrays.asList(bean));
        return report;
    }

    /**
     * @param t
     * @param user
     * @return
     */
    private VerticalListBuilder createTemperatureHistory(
            final ShipmentReportBean t, final User user) {
        final int gap = 5;
        final VerticalListBuilder list = Components.verticalList();

        list.add(createTable(twoColumns, alertProfileSummaryToMap(t, user)));
        list.add(Components.gap(1, gap));

        list.add(createTable(twoColumns, temperaturesToMap(t, user)));
        list.add(Components.gap(1, gap));

        list.add(createTable(twoColumns, alertsToMap(t, user)));

        return list;
    }

    /**
     * @param bean
     * @param user
     * @return
     */
    private List<Map<String, ?>> alertsToMap(final ShipmentReportBean bean, final User user) {
        //Total time above high temp (5°C): 2hrs 12min
        //Total time above critical high temp (8°C): 1hrs 12min
        //Total time below low temp (0°C): 22min
        //Total time below critical low temp (-2C): nil
        final List<Map<String, ?>> rows = new LinkedList<>();
        for (final TimeWithLabel t : bean.getAlerts()) {
            final StringBuilder sb = new StringBuilder();
            if (t.getTotalTime() > 0) {
                final int mins = (int) (t.getTotalTime() / 60000l);
                final int hours =  (int) (t.getTotalTime() / 3600000l);

                if (hours > 0) {
                    sb.append(hours);
                    sb.append("hrs");
                }
                if (mins > 0) {
                    if (sb.length() > 0) {
                        sb.append(' ');
                    }
                    sb.append(mins);
                    sb.append("min");
                }
            }
            rows.add(createRow(t.getLabel() + ":", sb.toString()));
        }
        return rows;
    }
    /**
     * @param t
     * @param user
     * @return
     */
    private List<Map<String, ?>> temperaturesToMap(final ShipmentReportBean t,
            final User user) {
        final List<Map<String, ?>> rows = new LinkedList<>();
        rows.add(createRow("Total time of monitoring:",
                (t.getTotalTime() / (1000l * 60 * 60)) + "hrs"));
        rows.add(createRow("Average temperature:",
                LocalizationUtils.getTemperatureString(
                        t.getAvgTemperature(), user.getTemperatureUnits())));

        final DecimalFormat fmt = new DecimalFormat("#0.0");
        fmt.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        rows.add(createRow("Standard deviation:",
                fmt.format(t.getStandardDevitation())));
        return rows;
    }
    /**
     * @param t
     * @param user
     * @return
     */
    private List<Map<String, ?>> alertProfileSummaryToMap(
            final ShipmentReportBean t, final User user) {
        final List<Map<String, ?>> rows = new LinkedList<>();
        rows.add(createRow("Alert Profile:", t.getAlertProfile()));
        rows.add(createRow("Alerts fired:",
                StringUtils.combine(t.getAlertsFired(), ", ")));
        rows.add(createRow("Who was notified:",
                StringUtils.combine(t.getWhoWasNotified(), ", ")));
        rows.add(createRow("Schedule(s):",
                StringUtils.combine(t.getSchedules(), ", ")));
        return rows;
    }
    /**
     * @param e
     * @param user
     * @return
     */
    private List<Map<String, ?>> lastReadingToRows(
            final ShortTrackerEvent e, final boolean suppressFurtherAlerts, final User user) {
        final List<Map<String, ?>> rows = new LinkedList<>();
        rows.add(createRow("Time of last reading:", format(user, e.getTime())));
        rows.add(createRow("Temperature:", LocalizationUtils.getTemperatureString(
                e.getTemperature(), user.getTemperatureUnits())));

        final StringBuilder sb = new StringBuilder();
        final int persents = (int) Math.round(Math.min(4150, e.getBattery()) * 100 / 4150.);
        sb.append(persents);

        sb.append("% (");
        sb.append(toFormattedVolts(e.getBattery()));
        sb.append("V)");

        if (suppressFurtherAlerts) {
            sb.append(". Suppress further alerts");
        }

        rows.add(createRow("Battery Level:", sb.toString()));
        return rows;
    }
    /**
     * @param battery
     * @return
     */
    private String toFormattedVolts(final int battery) {
        final double d = battery / 1000.;
        //create US locale decimal format
        final DecimalFormat fmt = new DecimalFormat("#0.0");
        fmt.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        return fmt.format(d);
    }

    /**
     * @param a arrival.
     * @return arrival properties as map.
     */
    private List<Map<String, ?>> arrivalToRows(final ArrivalBean a, final User user) {
        final List<Map<String, ?>> rows = new LinkedList<>();
        rows.add(createRow("Time of arrival:", format(user, a.getTime())));
        rows.add(createRow("Notified when", a.getNotifiedWhenKm() == null
                ? "" : a.getNotifiedWhenKm() + " km away"));
        rows.add(createRow("Notified at", format(user, a.getNotifiedAt())));
        rows.add(createRow("Who was notified", StringUtils.combine(
                a.getWhoIsNotified(), ", ")));
        rows.add(createRow("Schedule(s):",
                StringUtils.combine(a.getSchedules(), ", ")));
        rows.add(createRow("Time of shutdown", format(user, a.getShutdownTime())));
        return rows;
    }
    /**
     * @param bean
     * @param user
     * @return
     */
    private List<Map<String, ?>> goodsAsRows(final ShipmentReportBean bean, final User user) {
        final List<Map<String, ?>> rows = new LinkedList<>();
        //Goods
        //Description: some desc
        rows.add(createRow("Description:", bean.getDescription()));
        //Pallet ID: nil
        rows.add(createRow("Pallet ID:", bean.getPalletId()));
        //Asset Num: nil
        rows.add(createRow("Asset Num:", bean.getAssetNum()));
        //Comment:
        rows.add(createRow("Comment:", bean.getComment()));
        //Number of siblings
        rows.add(createRow("Number of siblings", Integer.toString(
                bean.getNumberOfSiblings())));
        return rows;
    }

    /**
     * @param bean
     * @return
     */
    private List<Map<String, ?>> shipmentDetailsToRows(
            final ShipmentReportBean bean, final User user) {
        final List<Map<String, ?>> rows = new LinkedList<>();
        //Shipment Num: 122(5)
        rows.add(createRow("Shipment Num:", getShipmentNumber(bean)));
        //Shipped From: Primo Head Office
        rows.add(createRow("Shipped From:", bean.getShippedFrom()));
        //Date shipped: 12 Jun 2016 13:45
        rows.add(createRow("Date shipped:", format(user, bean.getDateShipped())));
        //Shipped To: Coles DC Perth
        rows.add(createRow("Shipped To:", bean.getShippedTo()));
        //Date Arrived: 13 Jun 2016 22:44
        rows.add(createRow("Date Arrived:", format(user, bean.getDateArrived())));
        //Shipment Status: Arrived
        rows.add(createRow("Shipment Status:", bean.getStatus().toString()));
        return rows;
    }

    /**
     * @param bean
     * @param user
     * @return
     */
    private VerticalListBuilder createTitle(final ShipmentReportBean bean) {
        //title
        final VerticalListBuilder titles = Components.verticalList();

        TextFieldBuilder<String> f = Components.text("Shipment Report");
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 3, true));

        titles.add(f);

        f = Components.text(getShipmentNumber(bean));
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE, true));
        titles.add(f);
        titles.add(Components.gap(0, 20));
        return titles;
    }

    /**
     * @param bean
     * @return
     */
    private String getShipmentNumber(final ShipmentReportBean bean) {
        return Device.getSerialNumber(bean.getDevice())
        + "(" + bean.getTripCount() + ")";
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
     * @param title
     * @param rows
     * @return
     */
    private VerticalListBuilder createTableWithTitle(final String title,
            final List<Map<String, ?>> rows) {
        final VerticalListBuilder list = Components.verticalList();
        list.add(Components.text(title).setStyle(defaultStyleBold));
        list.add(createTable(twoColumns, rows));
        return list;
    }
    /**
     * @param columns
     * @param rows
     * @return
     */
    private SubreportBuilder createTable(final List<String> columns,
            final List<Map<String, ?>> rows) {
        final JasperReportBuilder report = new JasperReportBuilder();

        final ColumnBuilder<?, ?>[] cols = new ColumnBuilder<?, ?>[columns.size()];
        int index = 0;

        for (final String column : columns) {
            final TextColumnBuilder<String> columnBuilder = Columns.column
                    (column, String.class);
            columnBuilder.setStyle(defaultStyle);
            columnBuilder.setStretchWithOverflow(true);
            if (index == 0) {
                columnBuilder.setColumns(1);
            } else {
                columnBuilder.setColumns(2);
            }
            cols[index] = columnBuilder;
            index++;
        }

        report.columns(cols);
        report.setDataSource(new JRMapCollectionDataSource(rows));
        return Components.subreport(report);
    }
    /**
     * @param key
     * @param value
     * @return
     */
    private Map<String, ?> createRow(final String... values) {
        final Map<String, String> row = new HashMap<String, String>();
        int i = 0;
        for (final String value : values) {
            row.put(twoColumns.get(i), value == null ? "" : value);
            i++;
        }
        return row;
    }
    /**
     * @param date
     * @return
     */
    private String format(final User user, final Date date) {
        if (date == null) {
            return "";
        }
        return DateTimeUtils.createPrettyFormat(
                user.getLanguage(), user.getTimeZone()).format(date);
    }
}
