/**
 *
 */
package com.visfresh.controllers;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.visfresh.controllers.audit.AuditInterceptor;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private ApplicationContext context;

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
        converters.add(new ResourceHttpMessageConverter());
    }
    /**
     * @return GSON converter.
     */
    private GsonHttpMessageConverter createGsonHttpMessageConverter() {
        final Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .setPrettyPrinting()
                .create();

        final GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter() {
            {
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
        };
        gsonConverter.setGson(gson);
        return gsonConverter;
    }
    /* (non-Javadoc)
     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry)
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        super.addInterceptors(registry);
        registry.addInterceptor(context.getBean(AuditInterceptor.class));
    }
}
