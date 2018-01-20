/**
 *
 */
package com.visfresh.io.json.fastxml;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class JsonSerializerFactory {
    public static final JsonSerializerFactory FACTORY = new JsonSerializerFactory();

    /**
     * Default constructor.
     */
    private JsonSerializerFactory() {
        super();
    }

    public ObjectMapper createSingleShipmentDataParser() {
        final ObjectMapper parser = createDefaultMapper();

        final DefaultTypeResolverBuilder builder = new DefaultTypeResolverBuilder();
        builder.addResolver(new AlertBeanResolver());
        builder.addResolver(new AlertRuleBeanResolver());

        parser.setDefaultTyping(builder);
        return parser;
    }
    /**
     * @return
     */
    public ObjectMapper createDefaultMapper() {
        final ObjectMapper m = new ObjectMapper();
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        m.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        m.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        m.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        return m;
    }
}
