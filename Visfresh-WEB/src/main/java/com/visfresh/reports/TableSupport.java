/**
 *
 */
package com.visfresh.reports;

import java.util.List;

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
    public static TableSupportCondition firstRowCondition = createFirstRowCondition();
    public static TableSupportCondition notFirstRowCondition = createNotFirstRowCondition();
    public static TableSupportCondition firstColumnCondition = createFirstColumnCondition();

    private static long lastConditionId = 1;

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
}
