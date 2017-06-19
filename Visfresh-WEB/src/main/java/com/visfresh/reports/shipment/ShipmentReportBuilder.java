/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.reports.AbstractGraphics2DRenderer;
import com.visfresh.reports.Colors;
import com.visfresh.reports.ImagePaintingSupport;
import com.visfresh.reports.ReportUtils;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.services.EventsNullCoordinatesCorrector;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.column.ComponentColumnBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.ImageBuilder;
import net.sf.dynamicreports.report.builder.component.MultiPageListBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.BorderBuilder;
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.ComponentPositionType;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.ImageScale;
import net.sf.dynamicreports.report.constant.LineStyle;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.constant.StretchType;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignField;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentReportBuilder {
    private static final Logger log = LoggerFactory.getLogger(ShipmentReportBuilder.class);

    public static final int LOCATION_IMAGE_SIZE = 14;
    private static final int DEFAULT_FONT_SIZE = 8;
    static final int DEFAULT_PADDING = 3;

    @Autowired
    protected RuleBundle ruleBundle;
    private EventsNullCoordinatesCorrector nullCoordinatesCorrector = new EventsNullCoordinatesCorrector();
    /**
     * Default constructor.
     */
    public ShipmentReportBuilder() {
        super();
    }

    public void createReport(final ShipmentReportBean bean,
            final User user, final OutputStream out)
            throws IOException, JRException {
        final JasperReportBuilder report = createReport(bean, user);
        try {
            report.toPdf(out);
        } catch (final DRException e) {
            throw new RuntimeException("Failed to build report", e);
        }
    }
    public JasperReportBuilder createReport(
            final ShipmentReportBean bean, final User user) throws IOException, JRException {
        final JasperReportBuilder report = DynamicReports.report();
        report.setDetailSplitType(SplitType.IMMEDIATE);
        report.setShowColumnTitle(false);
        report.setPageMargin(DynamicReports.margin(10).setBottom(0));

        report.title(createTitle(bean, user));

        final VerticalListBuilder body = Components.verticalList();
        report.detail(body);

        final int gap = 8;

        //first gap
        body.add(Components.gap(1, gap));

        //Goods
        body.add(createGoodsTable(bean));
        body.add(Components.gap(1, gap));

        //Shipment description and map
        final HorizontalListBuilder shipmentDescAndMap = Components.horizontalList();
        shipmentDescAndMap.add(createShipmentDescription(bean, user));
        shipmentDescAndMap.add(Components.gap(gap, 1));
        shipmentDescAndMap.add(createMap(bean));

        body.add(shipmentDescAndMap);
        body.add(Components.gap(1, gap));

        //add temperature table
        body.add(createTemperatureTable(bean, user));
        body.add(Components.gap(1, gap));

        //add alerts table
        body.add(createAlertsTable(bean, user));
        body.add(Components.gap(1, gap));

        //add temperature chart
        body.add(TemperatureChartRenderer.createCompatibleChart(bean, user));

        //add page footer
        report.addPageFooter(ReportUtils.createPageFooter());

        //this data source is not used, but required for
        //show the content
        report.setDataSource(Arrays.asList(bean));
        report.setHighlightDetailOddRows(true);
        report.setDetailOddRowStyle(Styles.simpleStyle().setBackgroundColor(Colors.CELL_BG));
        report.setShowColumnTitle(false);

        return report;
    }

    /**
     * @param alerts
     * @return
     */
    public static List<Alert> filterAlerts(final List<Alert> alerts) {
        final List<Alert> result = new LinkedList<Alert>();
        for (final Alert a : alerts) {
            if (a.getType() != AlertType.LightOff && a.getType() != AlertType.LightOn) {
                result.add(a);
            }
        }
        return result;
    }
    /**
     * @param bean
     * @return
     */
    private ComponentBuilder<?, ?> createAlertsTable(final ShipmentReportBean bean, final User user) {

        final JasperReportBuilder report = new JasperReportBuilder();

        final String[] columns = {"key", "ap", "value"};
        final PenBuilder borderPen = Styles.pen1Point().setLineColor(Colors.CELL_BORDER);

        //first column
        final ComponentBuilder<?, ?> alertListView = buildAlertViewList(bean, user.getTemperatureUnits());
        alertListView.setStyle(Styles.style()
                .setRightBorder(borderPen));

        final ComponentColumnBuilder alertsColumn = Columns.componentColumn(columns[0], alertListView);
        alertsColumn.setStyle(Styles.style()
                .setRightBorder(borderPen));
        alertsColumn.setTitle("Alert Fired");
        alertsColumn.setTitleStyle(Styles.style(createStyleByFont(DEFAULT_FONT_SIZE, true)
                .setPadding(DEFAULT_PADDING)
                .setBottomBorder(borderPen)));
        alertsColumn.setFixedWidth(200);

        //second column
        final TextColumnBuilder<String> alertProfile = Columns.column(columns[1], String.class);
        alertProfile.setStretchWithOverflow(true);
        alertProfile.setStyle(Styles.style(createStyleByFont(DEFAULT_FONT_SIZE, false)
                .setPadding(DEFAULT_PADDING)
                .setLeftBorder(borderPen)));
        alertProfile.setTitle("Alert Profile");
        alertProfile.setTitleStyle(Styles.style(createStyleByFont(DEFAULT_FONT_SIZE, true)
                .setPadding(DEFAULT_PADDING))
                .setBottomBorder(borderPen)
                .setLeftBorder(borderPen));

        //second column
        final TextColumnBuilder<String> whoNotifiedView = Columns.column(columns[2], String.class);
        whoNotifiedView.setStretchWithOverflow(true);
        whoNotifiedView.setStyle(Styles.style(createStyleByFont(DEFAULT_FONT_SIZE, false)
                .setPadding(DEFAULT_PADDING)
                .setLeftBorder(borderPen)));
        whoNotifiedView.setTitle("Sent To");
        whoNotifiedView.setTitleStyle(Styles.style(createStyleByFont(DEFAULT_FONT_SIZE, true)
                .setPadding(DEFAULT_PADDING))
                .setBottomBorder(borderPen)
                .setLeftBorder(borderPen));

        report.columns(alertsColumn, alertProfile, whoNotifiedView);

        //add data
        final DRDataSource ds = new DRDataSource(columns);
        ds.add("xxxxxxxxxxx", bean.getAlertProfile(), namesAsString(bean.getWhoWasNotifiedByAlert()));

        report.setDataSource(ds);
        report.setShowColumnTitle(true);
        report.setDetailStyle(Styles.style().setBackgroundColor(Colors.CELL_BG));

        //create subreport with border
        final VerticalListBuilder sub = Components.verticalList(Components.subreport(report));
        final BorderBuilder border = Styles.border(Styles.pen1Point().setLineColor(Colors.DEFAULT_GREEN));
        sub.setStyle(Styles.style().setBorder(border));
        return sub;
    }

    /**
     * @param names list of names.
     * @return
     */
    private String namesAsString(final List<String> names) {
//        //remove duplicates
//        final Set<String> set = new HashSet<>(names);
//        final List<String> list = new LinkedList<>(set);
        final List<String> list = new LinkedList<>(names);
        //sort names.
        Collections.sort(list);
        return StringUtils.combine(list, ", ");
    }

    /**
     * @param bean
     * @return
     */
    private ComponentBuilder<?, ?> buildAlertViewList(final ShipmentReportBean bean, final TemperatureUnits units) {
        final MultiPageListBuilder list = Components.multiPageList();
        list.setStretchType(StretchType.CONTAINER_HEIGHT);
        list.setPositionType(ComponentPositionType.FIX_RELATIVE_TO_TOP);
        list.setWidth(150);

        if (bean.getFiredAlertRules().isEmpty()) {
            //build one component for avoid of confuse the cell
            list.add(Components.text("        "));
        } else {
            final Map<AlertType, BufferedImage> alertImageMap = loadAlertImages();

            list.add(Components.gap(1, DEFAULT_PADDING));
            for (final AlertRule alert: bean.getFiredAlertRules()) {
                //create alert component
                final HorizontalListBuilder alertView = Components.horizontalList();
                alertView.setStyle(Styles.style().setLeftPadding(DEFAULT_PADDING));

                //image
                final BufferedImage im = alertImageMap.get(alert.getType());
                final ImageBuilder image = Components.image(im);
                image.setFixedDimension(14, 14);
                image.setImageScale(ImageScale.RETAIN_SHAPE);
                image.setStretchType(StretchType.CONTAINER_HEIGHT);

                alertView.add(Components.hListCell(image).heightFixedOnMiddle());
                alertView.add(Components.gap(2, 1));

                //text
                final TextFieldBuilder<String> text = Components.text(ruleBundle.buildDescription(alert, units))
                        .setStyle(createStyleByFont(DEFAULT_FONT_SIZE, false)
                            .setVerticalTextAlignment(VerticalTextAlignment.MIDDLE));
                alertView.add(text);

                list.add(alertView);
            }
            list.add(Components.gap(1, DEFAULT_PADDING));
        }

        return list;
    }
    /**
     * @param useTinyImages TODO
     * @return
     */
    public static Map<AlertType, BufferedImage> loadAlertImages() {
        final Map<AlertType, BufferedImage> map = new HashMap<>();
        for (final AlertType type : AlertType.values()) {
            map.put(type, ImagePaintingSupport.loadAlertImage(type));
        }
        return map;
    }
    /**
     * @param bean
     * @param user
     * @return
     * @throws JRException
     */
    private ComponentBuilder<?, ?> createTemperatureTable(
            final ShipmentReportBean bean, final User user) throws JRException {
        String titleText = "TEMPERATURE";
        if (bean.getAlertSuppressionMinutes() > 0) {
            titleText += " (excluding "
                    + LocalizationUtils.formatByOneDecimal(bean.getAlertSuppressionMinutes() / 60.) + "hrs"
                    + " cooldown and post-arrival)";
        }

        final VerticalListBuilder list = createStyledVerticalListWithTitle(titleText);
        list.add(Components.gap(1, 1));

        final JasperReportBuilder report = new JasperReportBuilder();

        final String[] columns = {
                "avg",
                "sd",
                "min",
                "max",
                "timeBelow",
                "timeAbove",
                "totalTime"
        };
        final StyleBuilder[] styles = new StyleBuilder[columns.length];
        @SuppressWarnings("unchecked")
        final TextColumnBuilder<String>[] cols = new TextColumnBuilder[styles.length];

        final ConditionalStyleBuilder font = Styles.conditionalStyle(ReportUtils.firstRowCondition)
                .setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE).bold());

        for (int i = 0; i < columns.length; i++) {
            final BorderBuilder headerBorder = Styles.border(Styles.pen(0f, LineStyle.SOLID));
            headerBorder.setBottomPen(Styles.pen1Point());
            final StyleBuilder style = Styles.style(createStyleByFont(DEFAULT_FONT_SIZE, false)
                    .setPadding(DEFAULT_PADDING))
                    .setBorder(headerBorder);
            style.addConditionalStyle(font);
            styles[i] = style;

            final TextColumnBuilder<String> columnBuilder = Columns.column(columns[i], String.class);
            columnBuilder.setStretchWithOverflow(true);
            cols[i] = columnBuilder;
        }

//      Avg Temp SD Min Temp Max Temp Time below 0C Time above 5C Time monitored
//      3.3°C 0.6 0.2°C 5.3°C 1.1hrs 0.2hrs 23.3hrs
        final DRDataSource ds = new DRDataSource(columns);
        final TemperatureUnits units = user.getTemperatureUnits();
        final TemperatureStats temp = bean.getTemperatureStats();
        ds.add("Avg Temp",
                "SD",
                "Min Temp",
                "Max Temp",
                "Time below " + getTemperatureString(temp.getLowerTemperatureLimit(), units, ""),
                "Time above " + getTemperatureString(temp.getUpperTemperatureLimit(), units, ""),
                "Time monitored");
        ds.add(
                getTemperatureString(temp.getAvgTemperature(), units, "No Readings"),
                getSdString(temp.getStandardDevitation(), units, "No Readings"),
                getTemperatureString(temp.getMinimumTemperature(), units, "No Readings"),
                getTemperatureString(temp.getMaximumTemperature(), units, "No Readings"),
                LocalizationUtils.formatByOneDecimal(
                        temp.getTimeBelowLowerLimit() / (60 * 60 * 1000.)) + "hrs",
                LocalizationUtils.formatByOneDecimal(
                        temp.getTimeAboveUpperLimit() / (60 * 60 * 1000.)) + "hrs",
                LocalizationUtils.formatByOneDecimal(
                        temp.getTotalTime() / (60 * 60 * 1000.)) + "hrs"
                );

        //init column sizes from first row labels
        //first row is in fact the table header
        ds.next();
        final JRDesignField field = new JRDesignField();
        field.setValueClass(String.class);
        field.setDescription("Temporaty field for access the data source");

        for (int i = 0; i < cols.length; i++) {
            final TextColumnBuilder<String> c = cols[i];
            field.setName(columns[i]);
            final String value = (String) ds.getFieldValue(field);
            c.setColumns(Math.max(8, value.length() + 2));
        }

        ds.moveFirst();

        //apply styles
        ReportUtils.customizeTableStyles(styles, true);
        for (int i = 0; i < styles.length; i++) {
            cols[i].setStyle(styles[i]);
        }

        report.columns(cols);
        report.setDataSource(ds);
        report.setHighlightDetailOddRows(true);
        report.setDetailOddRowStyle(Styles.simpleStyle().setBackgroundColor(Colors.CELL_BG));
        report.setShowColumnTitle(false);

        //create subreport with border
        final VerticalListBuilder sub = Components.verticalList(Components.subreport(report));
        final BorderBuilder border = Styles.border(Styles.pen1Point().setLineColor(Colors.DEFAULT_GREEN));
        list.setStyle(Styles.style().setBorder(border));
        list.add(sub);

        return list;
    }

    /**
     * @param titleText
     */
    private VerticalListBuilder createStyledVerticalListWithTitle(final String titleText) {
        final VerticalListBuilder list = Components.verticalList();

        //add table border
        final PenBuilder pen = Styles.pen1Point().setLineColor(Colors.DEFAULT_GREEN);
        final BorderBuilder border = Styles.border(pen);
        list.setStyle(Styles.style().setBorder(border));

        //add table title
        final StyleBuilder titleStyle = createStyleByFont(DEFAULT_FONT_SIZE, true);
        titleStyle.setBackgroundColor(Colors.DEFAULT_GREEN);
        titleStyle.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        titleStyle.setPadding(Styles.padding(DEFAULT_PADDING));

        final BorderBuilder titleBorder = Styles.border(Styles.pen().setLineColor(Colors.DEFAULT_GREEN));
        titleBorder.setBottomPen(Styles.pen1Point().setLineColor(Color.BLACK).setLineWidth(2f)
                .setLineStyle(LineStyle.SOLID));
        titleStyle.setBorder(titleBorder);

        final TextFieldBuilder<String> title = Components.text(titleText).setStyle(titleStyle);
        list.add(title);

        return list;
    }
    /**
     * @param bean
     * @return
     */
    private ComponentBuilder<?, ?> createMap(final ShipmentReportBean bean) {
        final List<ShortTrackerEvent> readings = bean.getReadings();
        nullCoordinatesCorrector.correct(readings);

        if (hasReadingsWithLocation(readings)) {
            try {
                final ImageBuilder ib = Components.image(new MapRendererImpl(bean));
                ib.setImageScale(ImageScale.FILL_FRAME);
                ib.setStretchType(StretchType.CONTAINER_HEIGHT);

                final VerticalListBuilder centerVertical = Components.centerVertical(ib);
                centerVertical.setFixedWidth(300);
                centerVertical.setHeight(10);
                return centerVertical;
            } catch (final Exception exc) {
                log.error("Faile to load tiles from openstreet map", exc);
            }
        }

        return Components.text("openstreetmap service is unavailable now");
    }
    /**
     * @param readings
     * @return
     */
    private boolean hasReadingsWithLocation(final List<ShortTrackerEvent> readings) {
        for (final ShortTrackerEvent e : readings) {
            if (e.getLatitude() != null && e.getLongitude() != null) {
                return true;
            }
        }
        return false;
    }
    /**
     * @param bean
     * @param user
     * @return
     */
    private ComponentBuilder<?, ?> createShipmentDescription(
            final ShipmentReportBean bean, final User user) {
        final JasperReportBuilder report = new JasperReportBuilder();
        final DateFormat prettyFormat = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());

        //column names
        final String images = "images";
        final String key = "key";
        final String value = "value";

        //creat rows
        final List<Map<String, ?>> rows = new LinkedList<>();

        //shipped from
        final Map<String, Object> shippedFrom = new HashMap<>();
        shippedFrom.put(images, new ImageRenderingInfo("shippedFrom"));
        shippedFrom.put(key, "Shipped From");

        final StringBuilder shfBuilder = new StringBuilder();
        if (bean.getShippedFrom() != null) {
            shfBuilder.append(bean.getShippedFrom().getName());
        } else {
            shfBuilder.append("Undetermined");
        }
        //add data shipped to shipped from
        if (bean.getDateShipped() != null) {
            shfBuilder.append('\n');
            shfBuilder.append(prettyFormat.format(bean.getDateShipped()));
        }

        shippedFrom.put(value, shfBuilder.toString());
        rows.add(shippedFrom);

        //add interim stops
        int j = 0;
        final Iterator<InterimStop> iter = bean.getInterimStops().iterator();
        while (iter.hasNext()) {
            final int num = j + 1;
            final Map<String, Object> row = new HashMap<>();
            final InterimStop stop = iter.next();

            row.put(images, num);
            row.put(key, "Interim Stop " + num);
            row.put(value, stop.getLocation().getName()
                    + "\n" + prettyFormat.format(stop.getDate()));
            rows.add(row);
            j++;
        }

        //add shipped to
        final Map<String, Object> shippedTo = new HashMap<>();
        final List<String> possibleShippedTo = bean.getPossibleShippedTo();

        //shipped to image
        final ImageRenderingInfo im;
        if (Shipment.isFinalStatus(bean.getStatus())) {
            im = new ImageRenderingInfo("shippedTo", true);
        } else {
            im = new ImageRenderingInfo("shippedToToBeDetermined");
        }
        shippedTo.put(images, im);

        //shipped to text
        if (bean.getShippedTo() == null
                && possibleShippedTo != null && !possibleShippedTo.isEmpty()) {
            //cut two locations
            final List<String> otherLocs = new LinkedList<>(possibleShippedTo);
            final List<String> locs = new LinkedList<>();
            while (!otherLocs.isEmpty()) {
                locs.add(otherLocs.remove(0));
                if (locs.size() == 2) {
                    break;
                }
            }

            //show two alternative locations.
            final StringBuilder locations = new StringBuilder(StringUtils.combine(locs, ", "));

            //add number of other alternative locations.
            if (otherLocs.size() > 0) {
                locations.append(" and ");
                locations.append(Integer.toString(otherLocs.size()));
                locations.append(" others");
            }

            //wrap by braces
            locations.insert(0, '(').append(')');

            if (!Shipment.isFinalStatus(bean.getStatus())) {
                locations.insert(0, "To be determined ");
            } else {
                locations.insert(0, "Not determined ");
            }

            shippedTo.put(value, locations.toString());
        } else {
            shippedTo.put(value, bean.getShippedTo() == null ? "Undetermined" : bean.getShippedTo().getName());
        }

        shippedTo.put(key, "Shipped To");
        rows.add(shippedTo);

        //arrival date
        final Map<String, Object> arrivalDate = new HashMap<>();
        arrivalDate.put(key, "Arrival Date");
        arrivalDate.put(value, bean.getDateArrived() == null ? "" : prettyFormat.format(bean.getDateArrived()));
        rows.add(arrivalDate);

        //shipment status
        final Map<String, Object> status = new HashMap<>();
        status.put(key, "Shipment Status");
        status.put(value, bean.getStatus().name());
        rows.add(status);

        //arrival
        if (bean.getArrival() != null) {
            //Arrival Notification at: 19:18 4 Sep 2016
            final Map<String, Object> arrival = new HashMap<>();
            arrival.put(key, "Arrival Notification at");
            arrival.put(value, prettyFormat.format(
                    bean.getArrival().getNotifiedAt()));
            rows.add(arrival);
        }

        //Who was notified:     Rob Arpas, Rob Arpas
        if (!Shipment.isFinalStatus(bean.getStatus()) || bean.getStatus() == ShipmentStatus.Arrived) {
            final Map<String, Object> whoNotified = new HashMap<>();
//          If Arrived, can we label it "Arrival Report sent to"
//          if Default, can we label it "Arrival Report to be sent to"
//          if Ended, no need to show row
            if (bean.getStatus() == ShipmentStatus.Arrived) {
                whoNotified.put(key, "Arrival Report sent to");
            } else {
                whoNotified.put(key, "Arrival Report to be sent to");
            }
            whoNotified.put(value, namesAsString(bean.getWhoReceivedReport()));
            rows.add(whoNotified);
        }

        //last reading data
        if (bean.getReadings().size() > 0) {
            final ShortTrackerEvent lastReading = bean.getReadings().get(bean.getReadings().size() - 1);

            // Time of last reading:   11:43 2 Aug 2016
            final Map<String, Object> lastReadingTime = new HashMap<>();
            lastReadingTime.put(key, "Time of last reading");
            lastReadingTime.put(value, prettyFormat.format(lastReading.getTime()));
            rows.add(lastReadingTime);

            //Temperature:    23.6℃
            final Map<String, Object> lastReadingTemperature = new HashMap<>();
            lastReadingTemperature.put(key, "Temperature");
            lastReadingTemperature.put(value, getTemperatureString(lastReading.getTemperature(),
                    user.getTemperatureUnits(), null));
            rows.add(lastReadingTemperature);

            //Battery Level:  90% (4.1V)
            final Map<String, Object> batteryLevel = new HashMap<>();
            batteryLevel.put(key, "Battery Level");
            batteryLevel.put(value, (int) Device.batteryLevelToPersents(lastReading.getBattery())
                    + "% (" + LocalizationUtils.formatByOneDecimal(lastReading.getBattery() / 1000.) + "V)");
            rows.add(batteryLevel);
        }

        if (bean.getShutdownTime() != null) {
            //Time of shutdown: 21:46 4 Sep 2016
            final Map<String, Object> shutdownTime = new HashMap<>();
            shutdownTime.put(key, "Time of shutdown");
            shutdownTime.put(value, prettyFormat.format(bean.getShutdownTime()));
            rows.add(shutdownTime);
        }

        //define columns
        final ColumnBuilder<?, ?>[] cols = new ColumnBuilder<?, ?>[3];
        final StyleBuilder[] styles = new StyleBuilder[cols.length];

        //image column
        styles[0] = Styles.style();

        @SuppressWarnings("serial")
        final
        ImageBuilder imageBuilder = Components.image(
                new AbstractSimpleExpression<AbstractGraphics2DRenderer>() {
            /* (non-Javadoc)
             * @see net.sf.dynamicreports.report.definition.expression.DRISimpleExpression#evaluate(net.sf.dynamicreports.report.definition.ReportParameters)
             */
            @Override
            public AbstractGraphics2DRenderer evaluate(final ReportParameters reportParameters) {
                final int row = reportParameters.getColumnRowNumber();
                final Object data = rows.get(row - 1).get(images);

                if (data instanceof ImageRenderingInfo) {
                    final ImageRenderingInfo info = (ImageRenderingInfo) data;
                    final Color bg = (row % 2 != 0) ? Color.WHITE : Colors.CELL_BG;
                    return new FirstRowIconRenderer(info, bg);
                } else if (data instanceof Integer) {
                    return new InterimStopIconRenderer((Integer) data);
                }

                return null;
            }
        }
        );
        imageBuilder.setStyle(Styles.style().setPadding(DEFAULT_PADDING));

        //add image wrapped to list
        final ComponentBuilder<?, ?> imageWrapper = imageBuilder;

        final ComponentColumnBuilder imageColumnBuilder = Columns.componentColumn(images,
                imageWrapper);
        imageColumnBuilder.setWidth(LOCATION_IMAGE_SIZE);
        imageColumnBuilder.setHeight(LOCATION_IMAGE_SIZE);

        cols[0] = imageColumnBuilder;

        //key column
        final StyleBuilder keyColumnStyle = Styles.style();
        keyColumnStyle.setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE).bold());
        keyColumnStyle.setPadding(DEFAULT_PADDING);
        styles[1] = keyColumnStyle;

        TextColumnBuilder<String> columnBuilder = Columns.column(key, String.class);
        columnBuilder.setStretchWithOverflow(true);
        cols[1] = columnBuilder;

        //value column
        final StyleBuilder valueColumnStyle = Styles.style();
        valueColumnStyle.setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE));
        valueColumnStyle.setPadding(DEFAULT_PADDING);
        styles[2] = valueColumnStyle;

        columnBuilder = Columns.column(value, String.class);
        columnBuilder.setStretchWithOverflow(true);
        cols[2] = columnBuilder;

        //apply styles
        ReportUtils.customizeTableStyles(styles, true);
        for (int i = 0; i < styles.length; i++) {
            cols[i].setStyle(styles[i]);
        }

        report.columns(cols);
        report.setDataSource(new JRMapCollectionDataSource(rows));
        report.setHighlightDetailOddRows(true);
        report.setDetailOddRowStyle(Styles.simpleStyle().setBackgroundColor(Colors.CELL_BG));
        report.setShowColumnTitle(false);

        //create subreport with border
        final VerticalListBuilder list = Components.verticalList(Components.subreport(report));
        final BorderBuilder border = Styles.border(Styles.pen1Point().setLineColor(Colors.DEFAULT_GREEN));
        list.setStyle(Styles.style().setBorder(border));
        return list;
    }

    /**
     * @param bean
     * @return
     */
    private ComponentBuilder<?, ?> createGoodsTable(final ShipmentReportBean bean) {
        final VerticalListBuilder list = createStyledVerticalListWithTitle("GOODS");

        //data model
        final List<Map<String, ?>> rows = new LinkedList<>();
        //shipment
        addGoodsRow(rows, "Tracker (TripNum)", getShipmentNumber(bean));
        //description
        addGoodsRow(rows, "Description", bean.getDescription() == null ? "" : bean.getDescription());
        //pallet ID
        if (bean.getPalletId() != null) {
            addGoodsRow(rows, "Pallet ID", bean.getPalletId());
        }
        //assert num
        if (bean.getAssetNum() != null) {
            addGoodsRow(rows, "Asset Num", bean.getAssetNum());
        }
        //comments
        if (bean.getComment() != null) {
            addGoodsRow(rows, "Comments", bean.getComment() == null ? "" : bean.getComment());
        }
        //number of siblings
        if (bean.getNumberOfSiblings() > 0) {
            addGoodsRow(rows, "Number of siblings", Integer.toString(bean.getNumberOfSiblings()));
        }

        //calculate fixed first row width
        int firstRowWidth = 0;
        for (final Map<String, ?> map : rows) {
            final String key = (String) map.get("key");
            firstRowWidth = Math.max(firstRowWidth, key.length());
        }
        firstRowWidth += 2;

        //first row as separate table
        final ColumnBuilder<?, ?>[] firstTableCols = new ColumnBuilder<?, ?>[2];
        final StyleBuilder[] firstTableStyles = new StyleBuilder[firstTableCols.length];

        //add text column
        firstTableStyles[0] = createDefaultStyle(true);
        firstTableCols[0] = Columns.column("key", String.class).setFixedColumns(firstRowWidth);

        //add compound column with images
        firstTableStyles[1] = Styles.style();
        final HorizontalListBuilder deviceWithAlerts = Components.horizontalList();
        firstTableCols[1] = Columns.componentColumn("value", deviceWithAlerts);

        //add device
        final ImageBuilder deviceRect = ReportUtils.createDeviceRect(bean.getDeviceColor(), DEFAULT_PADDING);
        deviceWithAlerts.add(deviceRect);

        //add shipment number
        final String shipmentNumber = (String) rows.get(0).get("value");
        deviceWithAlerts.add(Components.text(shipmentNumber)
                .setStyle(createDefaultStyle(false))
                .setFixedColumns(shipmentNumber.length()));

        //apply styles
        for (int i = 0; i < firstTableStyles.length; i++) {
            firstTableCols[i].setStyle(firstTableStyles[i]);
        }
        ReportUtils.customizeTableStyles(firstTableStyles, true);

        final JasperReportBuilder firstTable = new JasperReportBuilder();
        firstTable.columns(firstTableCols);
        firstTable.setShowColumnTitle(false);

        //move first row from data to first table.
        final List<Map<String, ?>> tmp = new LinkedList<>();
        tmp.add(rows.remove(0));
        firstTable.setDataSource(new JRMapCollectionDataSource(tmp));

        //add report to result list.
        final HorizontalListBuilder firstTableWrapper = Components.horizontalList(Components.subreport(firstTable));
        firstTableWrapper.setStyle(Styles.style()
                .setBottomBorder(Styles.pen2Point().setLineColor(Colors.CELL_BORDER)));
        list.add(Components.gap(2, 2));
        list.add(firstTableWrapper);

        //second table
        final JasperReportBuilder secondTable = new JasperReportBuilder();

        final ColumnBuilder<?, ?>[] cols = new ColumnBuilder<?, ?>[2];
        final StyleBuilder[] styles = new StyleBuilder[cols.length];

        //key column
        styles[0] = createDefaultStyle(true);

        final TextColumnBuilder<String> columnBuilder = Columns.column("key", String.class);
        columnBuilder.setStretchWithOverflow(true);
        columnBuilder.setFixedColumns(firstRowWidth);
        cols[0] = columnBuilder;

        //value column
        styles[1] = createDefaultStyle(false);
        cols[1] = Columns.column("value", String.class);

        //apply styles
        for (int i = 0; i < styles.length; i++) {
            cols[i].setStyle(styles[i]);
        }
        ReportUtils.customizeTableStyles(styles, true);

        secondTable.columns(cols);

        secondTable.setShowColumnTitle(false);
        secondTable.setDataSource(new JRMapCollectionDataSource(rows));
        secondTable.setHighlightDetailEvenRows(true);
        secondTable.setDetailEvenRowStyle(Styles.simpleStyle().setBackgroundColor(Colors.CELL_BG));

        list.add(Components.subreport(secondTable));
        return list;
    }
    /**
     * @param bean
     * @return
     */
    private ImageBuilder createImageWithAlerts(final ShipmentReportBean bean) {
        final List<AlertType> types = new LinkedList<>();
        for (final Alert alert: bean.getAlerts()) {
            final AlertType type = alert.getType();
            switch (type) {
                case Hot:
                case CriticalHot:
                case Cold:
                case CriticalCold:
                    types.add(type);
                break;
                default:
            }
        }

        final List<BufferedImage> images = new LinkedList<>();
        int h = 0;
        int w = 0;

        final Map<AlertType, BufferedImage> cache = new HashMap<>();
        for (final AlertType type: types) {
            BufferedImage im = cache.get(type);
            if (im == null) {
                im = ImagePaintingSupport.loadAlertImage(type);
                cache.put(type, im);
            }

            images.add(im);

            h = Math.max(h, im.getHeight());
            w += im.getWidth();
        }

        //create image.
        final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);

            int offset = 0;
            for (final BufferedImage im : images) {
                g.drawImage(im, offset, (h - im.getHeight()) / 2, null);
                offset += im.getWidth();
            }
        } finally {
            g.dispose();
        }

        //create background image.
        final int size = 14;

        final ImageBuilder imb = Components.image(image);
        imb.setFixedHeight(size);
        imb.setImageScale(ImageScale.RETAIN_SHAPE);
        imb.setHorizontalImageAlignment(HorizontalImageAlignment.CENTER);
        imb.setStretchType(StretchType.CONTAINER_HEIGHT);

        return imb;
    }

    /**
     * @param rows
     * @param key
     * @param value
     */
    private void addGoodsRow(final List<Map<String, ?>> rows, final String key, final String value) {
        final Map<String, Object> row = new HashMap<>();
        row.put("key", key);
        row.put("value", value);
        rows.add(row);
    }


    /**
     * @param bean report bean.
     * @param user user.
     * @return
     */
    private VerticalListBuilder createTitle(final ShipmentReportBean bean, final User user) {
        //title
        final VerticalListBuilder titles = Components.verticalList();

//      SHIPMENT REPORT
        TextFieldBuilder<String> f = Components.text("SHIPMENT REPORT");
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 3, true));

        titles.add(f);
//      Primo Moraitis Fresh
        f = Components.text(bean.getCompanyName());
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 2, true));

        titles.add(f);
//      Tracker 122(4) - as of 6:45 13 Aug 2016 (EST)
        final DateFormat fmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());
        f = Components.text("Tracker " + getShipmentNumber(bean) + " - as of "
                + fmt.format(new Date()) + " (" + user.getTimeZone().getID() + ")");
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 1, true));
        titles.add(f);

        //add image with list of alerts
        if (!bean.getFiredAlertRules().isEmpty()) {
            titles.add(createImageWithAlerts(bean));
        }

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
    private StyleBuilder createStyleByFont(final int size, final boolean isBold) {
        FontBuilder font = Styles.font().setFontSize(size);
        if (isBold) {
            font = font.bold();
        }

        return Styles.style().setFont(font);
    }
    /**
     * @param t
     * @param units
     * @return
     */
    private String getTemperatureString(final Double t, final TemperatureUnits units, final String defValue) {
        if (t == null) {
            return defValue;
        }
        return LocalizationUtils.getTemperatureString(t, units);
    }
    /**
     * @param t
     * @param units
     * @param defValue
     * @return
     */
    private String getSdString(final Double t, final TemperatureUnits units, final String defValue) {
        if (t == null) {
            return defValue;
        }
        return LocalizationUtils.getTemperatureString(t, units);
    }
    /**
     * @param bold
     * @return
     */
    private StyleBuilder createDefaultStyle(final boolean bold) {
        final FontBuilder fond = Styles.font().setFontSize(DEFAULT_FONT_SIZE);
        if (bold) {
            fond.bold();
        }

        final StyleBuilder style = Styles.style()
            .setFont(fond)
            .setPadding(DEFAULT_PADDING);
        return style;
    }
    /**
     * @param u user.
     * @return user name.
     */
    public static String createUserName(final User u) {
        final StringBuilder sb = new StringBuilder();
        if (u.getFirstName() != null) {
            sb.append(u.getFirstName());
        }
        if (u.getLastName() != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(u.getLastName());
        }

        //add email instead name if empty
        if (sb.length() < 1) {
            sb.append(u.getEmail());
        }
        return sb.toString();
    }
}
