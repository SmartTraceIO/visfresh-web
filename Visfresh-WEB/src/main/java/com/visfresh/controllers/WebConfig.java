/**
 *
 */
package com.visfresh.controllers;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(createGsonHttpMessageConverter());
        super.configureMessageConverters(converters);
    }

    private GsonHttpMessageConverter createGsonHttpMessageConverter() {
        final Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();

        final GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        gsonConverter.setGson(gson);
        return gsonConverter;
    }
}
