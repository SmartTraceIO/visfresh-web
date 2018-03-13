/**
 *
 */
package com.visfresh.reports;

import net.sf.dynamicreports.report.builder.expression.AbstractComplexExpression;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class TableSupportCondition extends AbstractComplexExpression<Boolean> {
    private static final long serialVersionUID = 1188006896426549653L;
    private String name;

    /**
     * @param acceptEven
     */
    protected TableSupportCondition(final String name) {
        super();
        this.name = name;
    }
    /* (non-Javadoc)
     * @see net.sf.dynamicreports.report.builder.expression.AbstractComplexExpression#getName()
     */
    @Override
    public String getName() {
        return name;
    }
    /* (non-Javadoc)
     * @see net.sf.dynamicreports.report.definition.expression.DRIExpression#getValueClass()
     */
    @Override
    public Class<? super Boolean> getValueClass() {
        return Boolean.class;
    }
}
