/**
 *
 */
package com.visfresh.io.sms;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SmsMessagingException extends Exception {
    private static final long serialVersionUID = 5455449724231179252L;

    /**
     * Default constructor.
     */
    public SmsMessagingException() {
        super();
    }
    /**
     * @param message the message.
     */
    public SmsMessagingException(final String message) {
        super(message);
    }
    /**
     * @param cause cause exception.
     */
    public SmsMessagingException(final Throwable cause) {
        super(cause);
    }
    /**
     * @param message the message.
     * @param cause cause exception.
     */
    public SmsMessagingException(final String message, final Throwable cause) {
        super(message, cause);
    }
    /**
     * @param message the message.
     * @param cause cause exception.
     * @param enableSuppression
     * @param writableStackTrace
     */
    public SmsMessagingException(final String message, final Throwable cause,
            final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
