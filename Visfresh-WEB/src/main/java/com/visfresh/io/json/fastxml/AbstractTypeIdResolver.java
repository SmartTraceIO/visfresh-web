/**
 *
 */
package com.visfresh.io.json.fastxml;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractTypeIdResolver implements TypeIdResolver {
    private final Class<?> baseClass;
    private String propertyName;

    /**
     * Default constructor.
     */
    public AbstractTypeIdResolver(final Class<?> baseClass, final String propertyName) {
        super();
        this.baseClass = baseClass;
        this.propertyName = propertyName;
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#init(com.fasterxml.jackson.databind.JavaType)
     */
    @Override
    public void init(final JavaType baseType) {
    }
    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#idFromValue(java.lang.Object)
     */
    @Override
    public String idFromValue(final Object value) {
        return null;
    }
    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#idFromValueAndType(java.lang.Object, java.lang.Class)
     */
    @Override
    public String idFromValueAndType(final Object value, final Class<?> suggestedType) {
        return null;
    }
    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#idFromBaseType()
     */
    @Override
    public String idFromBaseType() {
        return null;
    }
    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#getDescForKnownTypeIds()
     */
    @Override
    public String getDescForKnownTypeIds() {
        return null;
    }
    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#getMechanism()
     */
    @Override
    public Id getMechanism() {
        return null;
    }
    /**
     * @return the baseClass
     */
    public Class<?> getBaseClass() {
        return baseClass;
    }
    /**
     * @return property name.
     */
    public String getPropertyName() {
        return propertyName;
    }
}
