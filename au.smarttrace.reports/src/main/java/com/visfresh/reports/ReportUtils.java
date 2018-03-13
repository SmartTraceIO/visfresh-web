/**
 *
 */
package com.visfresh.reports;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.ImageBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.constant.ComponentPositionType;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.ImageScale;
import net.sf.dynamicreports.report.constant.StretchType;
import net.sf.dynamicreports.report.definition.ReportParameters;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@SuppressWarnings("serial")
public final class ReportUtils {
    public static TableSupportCondition firstRowCondition = createFirstRowCondition();
    public static TableSupportCondition notFirstRowCondition = createNotFirstRowCondition();
    public static TableSupportCondition firstColumnCondition = createFirstColumnCondition();

    private static long lastConditionId = 1;

    /**
     * Default constructor.
     */
    private ReportUtils() {
        super();
    }

    /**
     * @return
     */
    private static TableSupportCondition createNotFirstRowCondition() {
        return new TableSupportCondition("notFirstRowCondition") {
            @Override
            public Boolean evaluate(final List<?> values, final ReportParameters reportParameters) {
                final Integer row = reportParameters.getColumnRowNumber();
                return row != null && row > 1;
            }
        };
    }
    /**
     * @return
     */
    private static TableSupportCondition createFirstColumnCondition() {
        return new TableSupportCondition("firstColumnCondition") {
            @Override
            public Boolean evaluate(final List<?> values, final ReportParameters reportParameters) {
                final Integer column = reportParameters.getColumnNumber();
                return column != null && column == 1;
            }
        };
    }
    /**
     * @return
     */
    private static TableSupportCondition createFirstRowCondition() {
        return new TableSupportCondition("firstRowCondition") {
            @Override
            public Boolean evaluate(final List<?> values, final ReportParameters reportParameters) {
                final Integer row = reportParameters.getColumnRowNumber();
                return row != null && row < 2;
            }
        };
    }
    /**
     * @return
     */
    public static TableSupportCondition createRowEqualsCondition(final int row) {
        return new TableSupportCondition("rowCondition_" + (lastConditionId++)) {
            @Override
            public Boolean evaluate(final List<?> values, final ReportParameters reportParameters) {
                final Integer columnRow = reportParameters.getColumnRowNumber();
                return columnRow != null && columnRow == row;
            }
        };
    }

    public static void customizeTableStyles(final StyleBuilder[] columnStyles, final boolean disableFirstTopBorder) {
        for (int i = 0; i < columnStyles.length; i++) {
            final StyleBuilder style = columnStyles[i];

            final boolean isFirst = (i == 0);
            style.conditionalStyles(createConditionalStyle(notFirstRowCondition, false, isFirst));
            style.conditionalStyles(createConditionalStyle(firstRowCondition, disableFirstTopBorder, isFirst));
        }
    }

    /**
     * @param condition
     * @param disableFirstTopBorder
     * @return
     */
    private static ConditionalStyleBuilder createConditionalStyle(
            final TableSupportCondition condition, final boolean disableFirstTopBorder,
            final boolean isFirstColumn) {
        //cell background
        final PenBuilder cellBorder = Styles.pen1Point().setLineColor(Colors.CELL_BORDER);

        //first line border normally should not have border
        final ConditionalStyleBuilder style = Styles.conditionalStyle(condition);
        if (!isFirstColumn) {
            style.setLeftBorder(cellBorder);
        }
        if (!disableFirstTopBorder) {
            style.setTopBorder(cellBorder);
        }

        return style;
    }
    /**
     * @return
     */
    public static HorizontalListBuilder createPageFooter() {
        final HorizontalListBuilder list = Components.horizontalList();
        final TextFieldBuilder<String> text = Components.text(
                "For assistance, contact SmartTrace Pty Ltd P: 612 9939 3233 E: contact@smartTrace.com.au");
        text.setStretchWithOverflow(false);
        text.setStretchType(StretchType.NO_STRETCH);
        text.setPositionType(ComponentPositionType.FLOAT);

        text.setStyle(Styles.style().setPadding(Styles.padding().setTop(12))
                .setForegroundColor(Colors.CELL_BORDER));

        list.add(text);

        final ImageBuilder image = Components.image(ImagePaintingSupport.loadImageResource(
                "reports/images/shipment/logo.jpg"));
        image.setFixedWidth(110);
        image.setFixedHeight(40);
        image.setImageScale(ImageScale.RETAIN_SHAPE);
        list.add(image);
        return list;
    }
    /**
     * @return
     */
    public static ImageBuilder createDeviceRect(final Color c, final int padding) {
        //create background image.
        final BufferedImage bim = createDeviceRectImage(c, padding);

        final ImageBuilder image = Components.image(bim);
        image.setFixedDimension(bim.getWidth(), bim.getHeight());
        image.setImageScale(ImageScale.RETAIN_SHAPE);
        image.setHorizontalImageAlignment(HorizontalImageAlignment.LEFT);
        image.setStretchType(StretchType.CONTAINER_HEIGHT);
        image.setStyle(Styles.style().setPadding(padding));

        return image;
    }

    /**
     * @param c
     * @param padding
     * @return
     */
    public static BufferedImage createDeviceRectImage(final Color c,
            final int padding) {
        final int size = 10 + 2 * padding;

        final BufferedImage bim = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE);
        final Graphics2D g = bim.createGraphics();
        try {
            g.setColor(c);
            g.fillRect(0, 0, size, size);

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1f));
            g.drawRect(0, 0, size - 1, size - 1);
        } finally {
            g.dispose();
        }
        return bim;
    }
}
