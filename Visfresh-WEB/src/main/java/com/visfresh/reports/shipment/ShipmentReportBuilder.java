/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.chart.Charts;
import net.sf.dynamicreports.report.builder.chart.TimeSeriesChartBuilder;
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
import net.sf.dynamicreports.report.constant.TimePeriod;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.controllers.UtilitiesController;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.reports.Colors;
import com.visfresh.reports.TableSupport;
import com.visfresh.reports.geomap.MapImageBuilder;
import com.visfresh.reports.geomap.RenderedMap;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentReportBuilder {
    private static final Logger log = LoggerFactory.getLogger(ShipmentReportBuilder.class);

    private static final int DEFAULT_FONT_SIZE = 10;
    private static final int DEFAULT_PADDING = 6;

    @Autowired
    protected RuleBundle ruleBundle;
    private MapImageBuilder mapBuilder = new MapImageBuilder();

    /**
     * Default constructor.
     */
    public ShipmentReportBuilder() {
        super();
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
        report.setDetailSplitType(SplitType.IMMEDIATE);
        report.setShowColumnTitle(false);

        report.title(createTitle(bean, user));

        final VerticalListBuilder body = Components.verticalList();
        report.detail(body);

        final int gap = 15;

        //first gap
        body.add(Components.gap(1, gap));

        //Goods
        body.add(createGoodsTable(bean));
        body.add(Components.gap(1, gap));

        //Shipment description and map
        final HorizontalListBuilder shipmentDescAndMap = Components.horizontalFlowList();
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
        body.add(createTemperatureChart(bean, user));

        //add page footer
        report.addPageFooter(createPageFooter());

        //this data source is not used, but required for
        //show the content
        report.setDataSource(Arrays.asList(bean));
        return report;
    }
    /**
     * @param bean
     * @param user
     * @return
     */
    @SuppressWarnings("serial")
    private ComponentBuilder<?, ?> createTemperatureChart(
            final ShipmentReportBean bean, final User user) {
        final DateFormat fmt = DateTimeUtils.createPrettyFormat(
                user.getLanguage(), user.getTimeZone());

        final String time = "time";
        final String temperature = "temperature";

        final TimeSeriesChartBuilder chart = Charts.timeSeriesChart();
        chart
            .setTimePeriod(Columns.column(time, Date.class))
            .setTimeAxisFormat(Charts.axisFormat().setLabel(
                    "Time ("
                    + user.getTimeZone().getID()
                    + " "
                    + UtilitiesController.createOffsetString(user.getTimeZone().getRawOffset())
                    + ")"))
            .setTimePeriodType(TimePeriod.MINUTE)
            .series(
                Charts.serie(Columns.column(temperature, java.lang.Double.class))
                .setLabel("Temperature "
                        + LocalizationUtils.getDegreeSymbol(user.getTemperatureUnits())))
            .seriesColors(Colors.DEFAULT_GREEN);

        //add data
        final DRDataSource ds = new DRDataSource(new String[]{time, temperature});
        for (final ShortTrackerEvent e : bean.getReadings()) {
            ds.add(
                e.getTime(),
                LocalizationUtils.convertToUnits(
                        e.getTemperature(), user.getTemperatureUnits()));
        }

        chart.setDataSource(ds);
        chart.addCustomizer(new DRIChartCustomizer() {
            @Override
            public void customize(final JFreeChart chart, final ReportParameters reportParameters) {
                final XYPlot plot = chart.getXYPlot();
                final DateAxis axis = (DateAxis) plot.getDomainAxis();
                axis.setDateFormatOverride(fmt);

                final TemperatureChartRenderer renderer = new TemperatureChartRenderer();
                addFiredAlerts(renderer, bean);
                plot.setRenderer(0, renderer);
                renderer.setSeriesShape(0, new Rectangle(0, 0));
                renderer.setLegendShape(0, new Rectangle(-1, -1, 2, 2));
            }
        });

        return chart;
    }

    /**
     * @param renderer
     * @param bean
     */
    protected void addFiredAlerts(final TemperatureChartRenderer renderer,
            final ShipmentReportBean bean) {
        for (final Alert a : bean.getAlerts()) {
            final int index = indexOf(bean.getReadings(), a.getTrackerEventId());
            if (index > -1) {
                renderer.addFiredAlerts(index, a.getType());
            }
        }
    }

    /**
     * @param readings
     * @param trackerEventId
     * @return
     */
    private int indexOf(final List<ShortTrackerEvent> readings, final Long trackerEventId) {
        int index = 0;
        for (final ShortTrackerEvent e : readings) {
            if (e.getId().equals(trackerEventId)) {
                return index;
            }
            index++;
        }
        return -1;
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
        ds.add("xxxxxxxxxxx", bean.getAlertProfile(), StringUtils.combine(bean.getWhoWasNotified(), ", "));

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
            final Map<AlertType, Image> alertImageMap = loadAlertImages();

            for (final AlertRule alert: bean.getFiredAlertRules()) {
                //create alert component
                final HorizontalListBuilder alertView = Components.horizontalList();
                alertView.setStyle(Styles.style().setLeftPadding(DEFAULT_PADDING));

                //image
                final ImageBuilder image = Components.image(alertImageMap.get(alert.getType()));
                image.setFixedDimension(16, 16);
                image.setImageScale(ImageScale.RETAIN_SHAPE);
                image.setStretchType(StretchType.CONTAINER_HEIGHT);

                alertView.add(Components.hListCell(image).heightFixedOnMiddle());

                //text
                alertView.add(Components.text(ruleBundle.buildDescription(alert, units))
                        .setStyle(createStyleByFont(DEFAULT_FONT_SIZE, true)
                            .setPadding(DEFAULT_PADDING)
                            .setVerticalTextAlignment(VerticalTextAlignment.TOP)));

                list.add(alertView);
            }
        }

        return list;
    }

    /**
     * @return
     */
    public static Map<AlertType, Image> loadAlertImages() {
        final Map<AlertType, Image> map = new HashMap<>();
        for (final AlertType type : AlertType.values()) {
            map.put(type, TemperatureChartRenderer.loadAlertImage(type));
        }
        return map;
    }


    /**
     * @param bean
     * @param user
     * @return
     */
    private ComponentBuilder<?, ?> createTemperatureTable(
            final ShipmentReportBean bean, final User user) {
        String titleText = "TEMPERATURE";
        if (bean.getAlertSuppressionMinutes() > 0) {
            titleText += " (excluding "
                    + LocalizationUtils.formatByOneDecimal(bean.getAlertSuppressionMinutes() / 60l) + "hrs"
                    + " cooldown period)";
        }

        final VerticalListBuilder list = createStyledVerticalListWithTitle(titleText);

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
        final ColumnBuilder<?, ?>[] cols = new ColumnBuilder<?, ?>[styles.length];

        final ConditionalStyleBuilder font = Styles.conditionalStyle(TableSupport.firstColumnCondition)
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
        ds.add("Avg Temp",
                "SD",
                "Min Temp",
                "Max Temp",
                "Time below " + LocalizationUtils.getTemperatureString(
                        bean.getLowerTemperatureLimit(), units),
                "Time above " + LocalizationUtils.getTemperatureString(
                        bean.getUpperTemperatureLimit(), units),
                "Time monitored");
        ds.add(
                getTemperatureString(bean.getAvgTemperature(), user),
                LocalizationUtils.formatByOneDecimal(
                        LocalizationUtils.convertSdToUnits(bean.getStandardDevitation(), units)),
                LocalizationUtils.formatByOneDecimal(
                        LocalizationUtils.convertToUnits(bean.getMinimumTemperature(), units)),
                LocalizationUtils.formatByOneDecimal(
                        LocalizationUtils.convertToUnits(bean.getMaximumTemperature(), units)),
                LocalizationUtils.formatByOneDecimal(
                        bean.getTimeBelowLowerLimit() / (60 * 60 * 1000l)) + "hrs",
                LocalizationUtils.formatByOneDecimal(
                        bean.getTimeAboveUpperLimit() / (60 * 60 * 1000l)) + "hrs",
                LocalizationUtils.formatByOneDecimal(
                        bean.getTotalTime() / (60 * 60 * 1000l)) + "hrs"
                );

        //apply styles
        TableSupport.customizeTableStyles(styles);
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
        if (bean.getReadings().size() != 0) {
            final int w = 300;
            final int h = 119;

            final List<Point2D> coords = new LinkedList<>();

            for (final ShortTrackerEvent e : bean.getReadings()) {
                coords.add(new Point2D.Double(e.getLongitude(), e.getLatitude()));
            }

            try {
                final Dimension size = new Dimension(w, h);
                final RenderedMap rim = mapBuilder.createMapImage(coords, size);

//                final RenderedMap rim = new RenderedMap();
//                final BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
//                final Graphics g2 = bim.getGraphics();
//                g2.setColor(Color.BLUE);
//                g2.fillRect(0, 0, w, h);
//                g2.dispose();
//                rim.setMap(bim);
//                rim.setMapLocation(new Point());
//                rim.setZoom(14);

                final BufferedImage image = scaleMapAndAddPath(rim, coords, size, bean.getDeviceColor());

                final ImageBuilder ib = Components.image(image);
                ib.setHorizontalImageAlignment(HorizontalImageAlignment.CENTER);
                ib.setImageScale(ImageScale.RETAIN_SHAPE);
                ib.setStretchType(StretchType.CONTAINER_HEIGHT);

                final VerticalListBuilder centerVertical = Components.centerVertical(ib);
                centerVertical.setFixedDimension(w, h);
                return centerVertical;
            } catch (final Exception exc) {
                log.error("Faile to load tiles from openstreet map", exc);
            }
        }

        return Components.text("openstreetmap service is unavailable now");
    }


    /**
     * @param rim
     * @param coords
     */
    private BufferedImage scaleMapAndAddPath(final RenderedMap rim,
            final List<Point2D> coords, final Dimension size, final Color color) {
        double scale = 1.;
        final boolean scaleChanged = rim.getMap().getWidth() != size.width || rim.getMap().getHeight() != size.height;
        if (scaleChanged) {
            scale = (double) size.width / rim.getMap().getWidth();
            //scale image
        }

        final BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            if (scaleChanged) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.drawImage(rim.getMap(), AffineTransform.getScaleInstance(scale, scale), null);
            } else {
                g.drawImage(rim.getMap(), 0, 0, null);
            }

            //create path shape
            final Point loc = rim.getMapLocation();
            final GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            final int zoom = rim.getZoom();

            for (final Point2D p : coords) {
                final int x = (int) Math.round(scale * (MapImageBuilder.lon2position(p.getX(), zoom) - loc.x));
                final int y = (int) Math.round(scale * (MapImageBuilder.lat2position(p.getY(), zoom) - loc.y));
                if (path.getCurrentPoint() == null) {
                    path.moveTo(x, y);
                } else {
                    path.lineTo(x, y);
                }
            }

            g.setStroke(new BasicStroke(2.f));
            g.setColor(color);
            g.draw(path);
        } finally {
            g.dispose();
        }

        return image;
    }

    /**
     * @param bean
     * @param user
     * @return
     */
    private ComponentBuilder<?, ?> createShipmentDescription(
            final ShipmentReportBean bean, final User user) {
        final JasperReportBuilder report = new JasperReportBuilder();

        //column names
        final String images = "images";
        final String key = "key";
        final String value = "value";

        //creat rows
        final List<Map<String, ?>> rows = new LinkedList<>();

        //shipped from
        final Map<String, Object> shippedFrom = new HashMap<>();
        shippedFrom.put(images, createImage("reports/images/shipment/shippedFrom.png"));
        shippedFrom.put(key, "Shipped From");
        shippedFrom.put(value, bean.getShippedFrom() == null ? "" : bean.getShippedFrom());
        rows.add(shippedFrom);

        //date shipped
        final Map<String, Object> dateShipped = new HashMap<>();
        dateShipped.put(key, "Date Shipped");
        dateShipped.put(value, bean.getDateShipped() == null ? "" : format(user, bean.getDateShipped()));
        rows.add(dateShipped);

        //shipped to
        final Map<String, Object> shippedTo = new HashMap<>();
        shippedTo.put(images, createImage("reports/images/shipment/shippedTo.png"));
        shippedTo.put(key, "Shipped To");
        shippedTo.put(value, bean.getShippedTo() == null ? "" : bean.getShippedTo());
        rows.add(shippedTo);

        //arrival date
        final Map<String, Object> arrivalDate = new HashMap<>();
        arrivalDate.put(key, "Arrival Date");
        arrivalDate.put(value, bean.getDateArrived() == null ? "" : format(user, bean.getDateArrived()));
        rows.add(arrivalDate);

        //shipment status
        final Map<String, Object> status = new HashMap<>();
        status.put(key, "Shipment Status");
        status.put(value, bean.getStatus().name());
        rows.add(status);

        final ColumnBuilder<?, ?>[] cols = new ColumnBuilder<?, ?>[3];
        final StyleBuilder[] styles = new StyleBuilder[cols.length];

        //image column
        styles[0] = Styles.style();

        @SuppressWarnings("serial")
        final
        ImageBuilder imageBuilder = Components.image(new AbstractSimpleExpression<Image>() {
            /* (non-Javadoc)
             * @see net.sf.dynamicreports.report.definition.expression.DRISimpleExpression#evaluate(net.sf.dynamicreports.report.definition.ReportParameters)
             */
            @Override
            public Image evaluate(final ReportParameters reportParameters) {
                final int row = reportParameters.getColumnRowNumber();
                return (Image) rows.get(row - 1).get(images);
            }
        });
        imageBuilder.setStretchType(StretchType.CONTAINER_HEIGHT);
        imageBuilder.setHorizontalImageAlignment(HorizontalImageAlignment.CENTER);
        imageBuilder.setStyle(Styles.style().setPadding(DEFAULT_PADDING));
        imageBuilder.setImageScale(ImageScale.RETAIN_SHAPE);

        //add image wrapped to list
        final ComponentColumnBuilder imageColumnBuilder = Columns.componentColumn(images,
                Components.verticalList(imageBuilder));
        imageColumnBuilder.setWidth(20);
        imageColumnBuilder.setHeight(20);

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
        TableSupport.customizeTableStyles(styles);
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
     * @param resource
     * @return
     */
    private Image createImage(final String resource) {
        final URL url = ShipmentReportBuilder.class.getClassLoader().getResource(resource);
        if (url == null) {
            return null;
        }

        try {
            return ImageIO.read(url);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load image", e);
        }
    }


    /**
     * @param bean
     * @return
     */
    private ComponentBuilder<?, ?> createGoodsTable(final ShipmentReportBean bean) {
        final VerticalListBuilder list = createStyledVerticalListWithTitle("GOODS");

        //add table
        final JasperReportBuilder report = new JasperReportBuilder();

        final ColumnBuilder<?, ?>[] cols = new ColumnBuilder<?, ?>[2];
        final StyleBuilder[] styles = new StyleBuilder[cols.length];

        //key column
        styles[0] = Styles.style()
            .setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE).bold())
            .setPadding(DEFAULT_PADDING);

        final String key = "key";
        TextColumnBuilder<String> columnBuilder = Columns.column(key, String.class);
        columnBuilder.setStretchWithOverflow(true);
        columnBuilder.setColumns(0);
        cols[0] = columnBuilder;

        //value column
        styles[1] = Styles.style()
            .setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE))
            .setPadding(DEFAULT_PADDING);

        final String value = "value";
        columnBuilder = Columns.column(value, String.class);
        columnBuilder.setStretchWithOverflow(true);
        columnBuilder.setColumns(1);
        cols[1] = columnBuilder;

        //apply styles
        TableSupport.customizeTableStyles(styles);
        for (int i = 0; i < styles.length; i++) {
            cols[i].setStyle(styles[i]);
        }

        report.columns(cols);

        //add data
        final List<Map<String, ?>> rows = new LinkedList<>();

        //tracker
        final Map<String, String> tracker = new HashMap<>();
        tracker.put(key, "Tracker (TripNum)");
        tracker.put(value, getShipmentNumber(bean));

//        final TextFieldBuilder<String> trackerField = Components.text("There will be tracker num");
//        report.addField(FieldBuilder<.>);
//        tracker.put(value, trackerField);
        rows.add(tracker);

        //description
        final Map<String, Object> description = new HashMap<>();
        description.put(key, "Description");
        description.put(value, bean.getDescription() == null ? "" : bean.getDescription());
        rows.add(description);

        //pallet ID
        final Map<String, Object> palletId = new HashMap<>();
        palletId.put(key, "Pallet ID");
        palletId.put(value, bean.getPalletId() == null ? "" : bean.getPalletId());
        rows.add(palletId);

        //comments
        final Map<String, Object> comments = new HashMap<>();
        comments.put(key, "Comments");
        comments.put(value, bean.getComment() == null ? "" : bean.getComment());
        rows.add(comments);

        //number of siblings
        final Map<String, Object> siblings = new HashMap<>();
        siblings.put(key, "Number of siblings");
        siblings.put(value, Integer.toString(bean.getNumberOfSiblings()));
        rows.add(siblings);

        report.setHighlightDetailOddRows(true);
        report.setDetailOddRowStyle(Styles.simpleStyle().setBackgroundColor(Colors.CELL_BG));
        report.setDataSource(new JRMapCollectionDataSource(rows));
        list.add(Components.subreport(report));
        return list;
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
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 5, true));

        titles.add(f);
//      Primo Moraitis Fresh
        f = Components.text(bean.getCompanyName());
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 4, true));

        titles.add(f);
//      Tracker 122(4) - as of 6:45 13 Aug 2016 (EST)
        final DateFormat fmt = DateTimeUtils.createPrettyFormat(user.getLanguage(), user.getTimeZone());
        f = Components.text("Tracker " + getShipmentNumber(bean) + " - as of "
                + fmt.format(new Date()) + " (" + user.getTimeZone().getID() + ")");
        f.setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        f.setStyle(createStyleByFont(DEFAULT_FONT_SIZE + 3, true));
        titles.add(f);
        return titles;
    }
    /**
     * @return
     */
    private ComponentBuilder<?, ?> createPageFooter() {
        final HorizontalListBuilder list = Components.horizontalList();
        final TextFieldBuilder<String> text = Components.text("For assistance, contact SmartTrace Pty Ltd P: 612 9939 3233 E: contact@smartTrace.com.au");
        text.setStretchWithOverflow(false);
        text.setStretchType(StretchType.NO_STRETCH);
        text.setPositionType(ComponentPositionType.FLOAT);

        text.setStyle(Styles.style().setPadding(Styles.padding().setTop(12))
                .setForegroundColor(Colors.CELL_BORDER));

        list.add(text);

        final ImageBuilder image = Components.image(createImage("reports/images/shipment/logo.jpg"));
        image.setFixedWidth(110);
        image.setFixedHeight(40);
        image.setImageScale(ImageScale.RETAIN_SHAPE);
        list.add(image);
        return list;
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
    /**
     * @param t
     * @param user
     * @return
     */
    private String getTemperatureString(final double t, final User user) {
        return LocalizationUtils.getTemperatureString(t, user.getTemperatureUnits());
    }
}
