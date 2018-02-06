/**
 *
 */
package au.smarttrace.sms;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SmsMessagingException extends Exception {
    private static final long serialVersionUID = 9121264663072673723L;

    /**
     * Default constructor.
     */
    public SmsMessagingException() {
        super();
    }
    /**
     * @param message message.
     */
    public SmsMessagingException(final String message) {
        super(message);
    }
    /**
     * @param cause origin exception.
     */
    public SmsMessagingException(final Throwable cause) {
        super(cause);
    }
    /**
     * @param message message.
     * @param cause origin exception.
     */
    public SmsMessagingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
