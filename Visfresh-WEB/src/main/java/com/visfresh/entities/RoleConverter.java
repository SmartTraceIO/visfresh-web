/**
 *
 */
package com.visfresh.entities;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * The type parameter Object is used instead of Role collection only for
 * fix the bug of hibernate implementation. Should be replace when the bug will fixed.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Converter(autoApply = false)
public class RoleConverter implements AttributeConverter<Object/*Collection<Role>*/, String> {
    /**
     * Default constructor.
     */
    public RoleConverter() {
        super();
    }
    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String convertToDatabaseColumn(final Object/*final Collection<Role>*/ obj) {
        final Collection<Role> attribute = (Collection<Role>) obj;
        final StringBuilder sb = new StringBuilder();

        for (final Role role : attribute) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(role);
        }

        return sb.toString();
    }
    /* (non-Javadoc)
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public Collection<Role> convertToEntityAttribute(final String dbData) {
        final List<Role> list = new LinkedList<Role>();

        if (dbData != null && dbData.length() > 0) {
            final String[] split = dbData.split(", *");
            for (int i = 0; i < split.length; i++) {
                list.add(Role.valueOf(split[i]));
            }
        }

        return list;
    }
}
