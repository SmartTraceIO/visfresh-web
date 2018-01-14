/**
 *
 */
package com.visfresh.controllers.io;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
//@ControllerAdvice
public class ResponseToJsonConverter implements ResponseBodyAdvice<Object> {
    /**
     * Default constructor.
     */
    public ResponseToJsonConverter() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice#supports(org.springframework.core.MethodParameter, java.lang.Class)
     */
    @Override
    public boolean supports(final MethodParameter returnType, final Class<? extends HttpMessageConverter<?>> converterType) {
        //supports all.
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice#beforeBodyWrite(java.lang.Object, org.springframework.core.MethodParameter, org.springframework.http.MediaType, java.lang.Class, org.springframework.http.server.ServerHttpRequest, org.springframework.http.server.ServerHttpResponse)
     */
    @Override
    public Object beforeBodyWrite(final Object body, final MethodParameter returnType, final MediaType selectedContentType,
            final Class<? extends HttpMessageConverter<?>> selectedConverterType, final ServerHttpRequest request,
            final ServerHttpResponse response) {
        final ServiceResponse<Object> resp = new ServiceResponse<Object>();
        resp.setStatus(new Status(HttpServletResponse.SC_OK, "OK"));
        resp.setResponseObject(body);
        return resp;
    }
}
