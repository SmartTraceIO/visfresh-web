/**
 *
 */
package com.visfresh.controllers.io;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visfresh.controllers.ApplicationException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
//@ControllerAdvice
public class GlobalDefaultExceptionHandler {
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
        final ServiceResponse<Object> r = new ServiceResponse<>();
        r.setStatus(new Status(getExceptionCode(e), e.getMessage()));
        //write JSON error response.
        new ObjectMapper().writeValue(res.getOutputStream(), r);
    }

    /**
     * @param e exception.
     * @return associated exception code.
     */
    private static int getExceptionCode(final Throwable e) {
        if (e instanceof AuthenticationException) {
            return HttpServletResponse.SC_FORBIDDEN;
        } else if (e instanceof ApplicationException) {
            return ((ApplicationException) e).getStatusCode();
        }

        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
}
