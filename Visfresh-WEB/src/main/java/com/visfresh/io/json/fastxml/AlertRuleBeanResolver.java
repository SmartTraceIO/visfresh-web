/**
 *
 */
package com.visfresh.io.json.fastxml;

import java.io.IOException;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.visfresh.entities.AlertType;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.TemperatureRuleBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertRuleBeanResolver extends AbstractTypeIdResolver {
    /**
     * Default constructor.
     */
    public AlertRuleBeanResolver() {
        super(AlertRuleBean.class, "type");
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#typeFromId(com.fasterxml.jackson.databind.DatabindContext, java.lang.String)
     */
    @Override
    public JavaType typeFromId(final DatabindContext context, final String id) throws IOException {
        switch (AlertType.valueOf(id)) {
            case Cold:
            case Hot:
            case CriticalCold:
            case CriticalHot:
                return SimpleType.constructUnsafe(TemperatureRuleBean.class);
            default:
                return SimpleType.constructUnsafe(AlertRuleBean.class);
        }
    }
}
