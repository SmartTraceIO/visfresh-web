/**
 *
 */
package com.visfresh.services;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthenticationException extends Exception {
    private static final long serialVersionUID = -4847939304366661895L;

    /**
     * Default constructor.
     */
    public AuthenticationException() {
        super();
    }
    /**
     * @param message exception message.
     */
    public AuthenticationException(final String message) {
        super(message);
    }
    /**
     * @param cause origin exception.
     */
    public AuthenticationException(final Throwable cause) {
        super(cause);
    }
    /**
     * @param message exception message.
     * @param cause origin exception.
     */
    public AuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
