/**
 *
 */
package au.smarttrace.security;

import javax.servlet.http.HttpServletResponse;

import au.smarttrace.ApplicationException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AccessException extends ApplicationException {
    private static final long serialVersionUID = -2163508215179014875L;

    /**
     * Default constructor.
     */
    public AccessException() {
        super(HttpServletResponse.SC_FORBIDDEN);
    }
    /**
     * @param message
     */
    public AccessException(final String message) {
        super(message, HttpServletResponse.SC_FORBIDDEN);
    }
    /**
     * @param cause
     */
    public AccessException(final Throwable cause) {
        super(cause, HttpServletResponse.SC_FORBIDDEN);
    }
    /**
     * @param message
     * @param cause
     */
    public AccessException(final String message, final Throwable cause) {
        super(message, cause, HttpServletResponse.SC_FORBIDDEN);
    }
}
