/**
 *
 */
package com.visfresh.io.json.fastxml;

import java.io.IOException;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.visfresh.entities.AlertType;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.TemperatureAlertBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertBeanResolver extends AbstractTypeIdResolver {
    /**
     * Default constructor.
     */
    public AlertBeanResolver() {
        super(AlertBean.class, "type");
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
                return SimpleType.constructUnsafe(TemperatureAlertBean.class);
            default:
                return SimpleType.constructUnsafe(AlertBean.class);
        }
    }
}
