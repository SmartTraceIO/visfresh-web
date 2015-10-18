/**
 *
 */
package com.visfresh.entities;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Converter(autoApply = false)
public class WeekDaysConverter implements AttributeConverter<boolean[], String> {
    /**
     * Default constructor.
     */
    public WeekDaysConverter() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn(final boolean[] attribute) {
        final StringBuilder sb = new StringBuilder();
        for (final boolean b : attribute) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(b);
        }

        return sb.toString();
    }
    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public boolean[] convertToEntityAttribute(final String dbData) {
        final boolean[] result = new boolean[7];
        final String[] split = dbData.split(", *");
        for (int i = 0; i < split.length; i++) {
            result[i] = Boolean.parseBoolean(split[i]);
        }
        return result;
    }
}
