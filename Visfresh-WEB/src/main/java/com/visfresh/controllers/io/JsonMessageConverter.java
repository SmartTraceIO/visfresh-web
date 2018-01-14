/**
 *
 */
package com.visfresh.controllers.io;

import java.util.LinkedList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class JsonMessageConverter extends GsonHttpMessageConverter {
    /**
     * Default constructor.
     */
    public JsonMessageConverter() {
        final List<MediaType> media = new LinkedList<>(getSupportedMediaTypes());
        media.add(MediaType.TEXT_PLAIN);
        setSupportedMediaTypes(media);

        final Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
        setGson(gson);
    }

    /* (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#canWrite(java.lang.Class, org.springframework.http.MediaType)
     */
    @Override
    public boolean canWrite(final Class<?> clazz, final MediaType mediaType) {
        return canWrite(mediaType);
    }

    /* (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#canRead(java.lang.Class, org.springframework.http.MediaType)
     */
    @Override
    public boolean canRead(final Class<?> clazz, final MediaType mediaType) {
        return canRead(mediaType);
    }
}
