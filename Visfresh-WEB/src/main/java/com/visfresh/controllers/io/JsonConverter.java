/**
 *
 */
package com.visfresh.controllers.io;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JsonConverter extends MappingJackson2HttpMessageConverter {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public JsonConverter() {
        super();
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));

        //add support of text/plain meda type.
        final List<MediaType> media = new LinkedList<>(getSupportedMediaTypes());
        media.add(MediaType.TEXT_PLAIN);
        setSupportedMediaTypes(media);
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
        return super.canRead(clazz, mediaType);
    }
}
