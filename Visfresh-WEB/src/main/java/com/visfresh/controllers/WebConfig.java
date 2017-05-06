/**
 *
 */
package com.visfresh.controllers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.visfresh.controllers.audit.AuditInterceptor;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);
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
            @Override
            public Object read(final Type type, final Class<?> contextClass, final HttpInputMessage inputMessage)
                    throws IOException, HttpMessageNotReadableException {

                final TypeToken<?> token = getTypeToken(type);
                final Reader r = new InputStreamReader(inputMessage.getBody(), "UTF-8");
                try {
                    final String json = StringUtils.getContent(r);
                    log.debug("JSON request received:\n" + json);

                    return gson.fromJson(json, token.getType());
                }
                catch (final JsonParseException ex) {
                    throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
                }
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
