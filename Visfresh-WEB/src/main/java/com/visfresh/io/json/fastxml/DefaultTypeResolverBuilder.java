/**
 *
 */
package com.visfresh.io.json.fastxml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultTypeResolverBuilder implements TypeResolverBuilder<DefaultTypeResolverBuilder> {
    private final List<AbstractTypeIdResolver> resolvers = new LinkedList<>();

    /**
     * Default constructor.
     */
    public DefaultTypeResolverBuilder() {
        super();
    }

    @Override
    public TypeSerializer buildTypeSerializer(final SerializationConfig config, final JavaType baseType,
            final Collection<NamedType> subtypes) {
        return null;
    }

    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder#init(com.fasterxml.jackson.annotation.JsonTypeInfo.Id, com.fasterxml.jackson.databind.jsontype.TypeIdResolver)
     */
    @Override
    public DefaultTypeResolverBuilder init(final Id idType, final TypeIdResolver res) {
        return null;
    }
    @Override
    public TypeDeserializer buildTypeDeserializer(final DeserializationConfig config, final JavaType baseType,
            final Collection<NamedType> subtypes) {


        final AbstractTypeIdResolver idRes = idResolver(baseType);
        if (idRes != null) {
            return new AsPropertyTypeDeserializer(baseType, idRes, idRes.getPropertyName(), true, null, null);
        }

        return null;
    }

    /**
     * @param baseType base java type.
     * @return
     */
    private AbstractTypeIdResolver idResolver(final JavaType baseType) {
        for (final AbstractTypeIdResolver r : resolvers) {
            if (r.getBaseClass().isAssignableFrom(baseType.getRawClass())) {
                return r;
            }
        }
        return null;
    }

    @Override
    public DefaultTypeResolverBuilder inclusion(final JsonTypeInfo.As includeAs) {
        return this;
    }

    @Override
    public DefaultTypeResolverBuilder typeProperty(final String typeIdPropName) {
        return this;
    }

    @Override
    public DefaultTypeResolverBuilder defaultImpl(final Class<?> defaultImpl) {
        return this;
    }

    @Override
    public DefaultTypeResolverBuilder typeIdVisibility(final boolean isVisible) {
        return this;
    }

    @Override
    public Class<?> getDefaultImpl() {
        return null;
    }
    public void addResolver(final AbstractTypeIdResolver r) {
        resolvers.add(r);
    }
}
