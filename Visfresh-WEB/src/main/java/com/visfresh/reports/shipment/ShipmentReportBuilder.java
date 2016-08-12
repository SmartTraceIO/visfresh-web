/**
 *
 */
package com.visfresh.reports.shipment;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

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
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.BorderBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.ComponentPositionType;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.ImageScale;
import net.sf.dynamicreports.report.constant.SplitType;
import net.sf.dynamicreports.report.constant.StretchType;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Device;
import com.visfresh.entities.User;
import com.visfresh.reports.TableSupport;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentReportBuilder {
    private static final int DEFAULT_FONT_SIZE = 10;
    private static final int DEFAULT_PADDING = 4;
    private Color defaultGreen = Color.GREEN.brighter();

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
        report.setTitleSplitType(SplitType.IMMEDIATE);
        report.setShowColumnTitle(false);

        report.title(createTitle(bean, user));

        final VerticalListBuilder body = Components.verticalList();
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

        //add body to report
        report.detail(body);

        //add page footer
        report.addPageFooter(createPageFooter());

        //this data source is not used, but required for
        //show the content
        report.setDataSource(Arrays.asList(bean));
        return report;
    }
    /**
     * @param bean
     * @return
     */
    private ComponentBuilder<?, ?> createMap(final ShipmentReportBean bean) {
        return Components.text("There will be Map");
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
        imageBuilder.setStretchType(StretchType.RELATIVE_TO_BAND_HEIGHT);
        imageBuilder.setHorizontalImageAlignment(HorizontalImageAlignment.CENTER);
        imageBuilder.setStyle(Styles.style().setPadding(DEFAULT_PADDING));

        //add image wrapped to list
        final ComponentColumnBuilder imageColumnBuilder = Columns.componentColumn(images,
                Components.verticalList(imageBuilder));
        imageColumnBuilder.setWidth(24);
        imageColumnBuilder.setHeight(24);

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
        report.setDetailOddRowStyle(Styles.simpleStyle().setBackgroundColor(TableSupport.CELL_BG));

        //create subreport with border
        report.setShowColumnTitle(false);

        final VerticalListBuilder list = Components.verticalList(Components.subreport(report));
        final BorderBuilder border = Styles.border(Styles.pen1Point().setLineColor(defaultGreen));
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
        final VerticalListBuilder list = Components.verticalList();

        //add table border
        final PenBuilder pen = Styles.pen1Point().setLineColor(defaultGreen);
        final BorderBuilder border = Styles.border(pen);
        list.setStyle(Styles.style().setBorder(border));

        //add table title
        final StyleBuilder titleStyle = createStyleByFont(DEFAULT_FONT_SIZE, true);
        titleStyle.setBackgroundColor(defaultGreen);
        titleStyle.setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
        titleStyle.setPadding(Styles.padding(DEFAULT_PADDING));

        final BorderBuilder titleBorder = Styles.border(Styles.pen().setLineColor(defaultGreen));
        titleBorder.setBottomPen(Styles.pen1Point().setLineColor(Color.BLACK));
        titleStyle.setBorder(titleBorder);

        final TextFieldBuilder<String> title = Components.text("GOODS").setStyle(titleStyle);
        list.add(title);

        //add table
        final JasperReportBuilder report = new JasperReportBuilder();

        final ColumnBuilder<?, ?>[] cols = new ColumnBuilder<?, ?>[2];
        final StyleBuilder[] styles = new StyleBuilder[cols.length];

        //key column
        final StyleBuilder keyColumnStyle = Styles.style();
        keyColumnStyle.setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE).bold());
        keyColumnStyle.setPadding(DEFAULT_PADDING);
        styles[0] = keyColumnStyle;

        final String key = "key";
        TextColumnBuilder<String> columnBuilder = Columns.column(key, String.class);
        columnBuilder.setStretchWithOverflow(true);
        columnBuilder.setColumns(0);
        cols[0] = columnBuilder;

        //value column
        final StyleBuilder valueColumnStyle = Styles.style();
        valueColumnStyle.setFont(Styles.font().setFontSize(DEFAULT_FONT_SIZE));
        valueColumnStyle.setPadding(DEFAULT_PADDING);
        styles[1] = valueColumnStyle;

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
        final Map<String, Object> tracker = new HashMap<>();
        tracker.put(key, "Tracker (TripNum)");
        tracker.put(value, getShipmentNumber(bean));
        rows.add(tracker);

        //tracker
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

        report.setHighlightDetailOddRows(true);
        report.setDetailOddRowStyle(Styles.simpleStyle().setBackgroundColor(TableSupport.CELL_BG));
        report.setDataSource(new JRMapCollectionDataSource(rows));
        list.add(Components.subreport(report));
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
//            rows.add(createRow(t.getLabel() + ":", sb.toString()));
        }
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
     * @param bean
     * @param user TODO
     * @param user
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
                .setForegroundColor(TableSupport.CELL_BORDER));

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
}
