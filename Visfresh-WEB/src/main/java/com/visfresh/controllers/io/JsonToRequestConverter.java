/**
 *
 */
package com.visfresh.controllers.io;

import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
//@ControllerAdvice
public class JsonToRequestConverter extends RequestBodyAdviceAdapter {

    /**
     *
     */
    public JsonToRequestConverter() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice#supports(org.springframework.core.MethodParameter, java.lang.reflect.Type, java.lang.Class)
     */
    @Override
    public boolean supports(final MethodParameter methodParameter, final Type targetType,
            final Class<? extends HttpMessageConverter<?>> converterType) {
        // TODO Auto-generated method stub
        return false;
    }

}
