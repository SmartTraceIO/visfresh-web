/**
 *
 */
package com.visfresh.services;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceException extends Exception {
    private static final long serialVersionUID = 1818103287080317513L;
    private final int errorCode;

    /**
     * @param code error code.
     * @param message error message.
     */
    public RestServiceException(final int code, final String message) {
        super(message);
        errorCode = code;
    }
    /**
     * @param code error code.
     * @param message error message.
     * @param cause origin exception.
     */
    public RestServiceException(final int code, final String message, final Throwable cause) {
        super(message, cause);
        errorCode = code;
    }
    /**
     * @return error code.
     */
    public int getErrorCode() {
        return errorCode;
    }
}
