/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
    /**
     * Default constructor.
     */
    public WebConfig() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#configureMessageConverters(java.util.List)
     */
    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(createGsonHttpMessageConverter());
        super.configureMessageConverters(converters);
    }
    /**
     * @return GSON converter.
     */
    private GsonHttpMessageConverter createGsonHttpMessageConverter() {
        final Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();

        final GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter() {
            /* (non-Javadoc)
             * @see org.springframework.http.converter.json.GsonHttpMessageConverter#canRead(java.lang.Class, org.springframework.http.MediaType)
             */
            @Override
            public boolean canRead(final Class<?> clazz, final MediaType mediaType) {
                if (restContent(clazz, mediaType)) {
                    return true;
                }
                return super.canRead(clazz, mediaType);
            }
            /* (non-Javadoc)
             * @see org.springframework.http.converter.json.GsonHttpMessageConverter#canWrite(java.lang.Class, org.springframework.http.MediaType)
             */
            @Override
            public boolean canWrite(final Class<?> clazz, final MediaType mediaType) {
                if (restContent(clazz, mediaType)) {
                    return true;
                }
                return super.canWrite(clazz, mediaType);
            }
            /**
             * @param clazz
             * @param mediaType
             * @return
             */
            private boolean restContent(final Class<?> clazz, final MediaType mediaType) {
                return JsonElement.class.isAssignableFrom(clazz);
            }
        };
        gsonConverter.setGson(gson);
        return gsonConverter;
    }
}
