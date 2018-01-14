/**
 *
 */
package com.visfresh.controllers.io;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visfresh.constants.ErrorCodes;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@ControllerAdvice
public class GlobalDefaultExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

    public static final String DEFAULT_ERROR_VIEW = "error";

    @ExceptionHandler(value = Throwable.class)
    public void defaultErrorHandler(final HttpServletResponse res, final Throwable e) throws Exception {
        handleError(res, e);
    }
    /**
     * @param res HTTP servlet response.
     * @param e error.
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public static void handleError(final HttpServletResponse res, final Throwable e)
            throws JsonGenerationException, JsonMappingException, IOException {
        log.error("Exception occured during to process request", e);

        final ServiceResponse<Object> r = new ServiceResponse<>();
        //status code
        int code = -1;
        if (e instanceof AuthenticationException) {
            code = ErrorCodes.AUTHENTICATION_ERROR;
        } else if (e instanceof RestServiceException) {
            code = ((RestServiceException) e).getErrorCode();
        }

        //status message
        final String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        r.setStatus(new Status(code, msg));

        //write JSON error response.
        new ObjectMapper().writeValue(res.getOutputStream(), r);
    }
}
