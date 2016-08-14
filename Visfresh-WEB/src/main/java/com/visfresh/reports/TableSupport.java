/**
 *
 */
package com.visfresh.reports;

import java.awt.Color;
import java.util.List;

import net.sf.dynamicreports.report.builder.style.BorderBuilder;
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder;
import net.sf.dynamicreports.report.builder.style.PenBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.style.Styles;
import net.sf.dynamicreports.report.definition.ReportParameters;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@SuppressWarnings("serial")
public final class TableSupport {
    public static Color CELL_BORDER = parseColor("#7D959D");
    public static Color CELL_BG = parseColor("#B6FCFB");

    public static TableSupportCondition firstRowCondition = createFirstRowCondition();
    public static TableSupportCondition notFirstRowCondition = createNotFirstRowCondition();
    public static TableSupportCondition firstColumnCondition = createFirstColumnCondition();

    /**
     * Default constructor.
     */
    private TableSupport() {
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
     * @param hex
     * @return
     */
    private static Color parseColor(final String hex) {
        // #7D959D
        final int r = Integer.parseInt(hex.substring(1, 3), 16);
        final int g = Integer.parseInt(hex.substring(3, 5), 16);
        final int b = Integer.parseInt(hex.substring(5, 7), 16);
        return new Color(r, g, b, 255);
    }

    public static void customizeTableStyles(final StyleBuilder[] columnStyles) {
        for (int i = 0; i < columnStyles.length; i++) {
            final StyleBuilder style = columnStyles[i];

            //cell background
            final PenBuilder emptyPen = Styles.pen().setLineWidth(0f);

            //create normal border
            final ConditionalStyleBuilder normalCellStyle = Styles.conditionalStyle(notFirstRowCondition);
            final BorderBuilder normalBorder = Styles.border(Styles.pen1Point().setLineColor(CELL_BORDER));
            normalBorder.setRightPen(emptyPen);
            normalBorder.setBottomPen(emptyPen);
            if (i == 0) {
                normalBorder.setLeftPen(emptyPen);
            }

            normalCellStyle.setBorder(normalBorder);

            //first line border
            final ConditionalStyleBuilder firstLineCellStyle = Styles.conditionalStyle(firstRowCondition);

            final BorderBuilder firstLineBorder = Styles.border(Styles.pen1Point().setLineColor(CELL_BORDER));
            firstLineBorder.setRightPen(emptyPen);
            firstLineBorder.setBottomPen(emptyPen);
            if (i == 0) {
                firstLineBorder.setLeftPen(emptyPen);
            }
            firstLineBorder.setTopPen(emptyPen);

            firstLineCellStyle.setBorder(firstLineBorder);

            style.conditionalStyles(normalCellStyle);
            style.conditionalStyles(firstLineCellStyle);
        }
    }
}
