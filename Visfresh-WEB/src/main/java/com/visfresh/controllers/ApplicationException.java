/**
 *
 */
package com.visfresh.controllers;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ApplicationException extends Exception {
    private static final long serialVersionUID = 6496204120668291770L;
    protected int statusCode;

    /**
     * @param code error code.
     */
    public ApplicationException(final int code) {
        this(null, null, code);
    }

    /**
     * @param code error code.
     * @param message
     * @param cause
     */
    public ApplicationException(final String message, final Throwable cause, final int code) {
        super(message, cause);
        this.statusCode = code;
    }

    /**
     * @param code error code.
     * @param message
     */
    public ApplicationException(final String message, final int code) {
        this(message, null, code);
    }

    /**
     * @param code error code.
     * @param cause
     */
    public ApplicationException(final Throwable cause, final int code) {
        this(null, cause, code);
    }
    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }
}
