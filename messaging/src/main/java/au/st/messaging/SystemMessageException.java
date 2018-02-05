/**
 *
 */
package au.st.messaging;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageException extends Exception {
    private static final long serialVersionUID = 1549213078369774493L;
    private Date retryOn;

    /**
     * Default constructor.
     */
    public SystemMessageException() {
        super();
    }
    /**
     * @param message message.
     * @param cause origin exception.
     */
    public SystemMessageException(final String message, final Throwable cause) {
        super(message, cause);
    }
    /**
     * @param message message.
     */
    public SystemMessageException(final String message) {
        super(message);
    }
    /**
     * @param cause origin exception.
     */
    public SystemMessageException(final Throwable cause) {
        super(cause);
    }

    /**
     * @return the retryOn
     */
    public Date getRetryOn() {
        return retryOn;
    }
    /**
     * @param retryOn the retryOn to set
     */
    public void setRetryOn(final Date retryOn) {
        this.retryOn = retryOn;
    }
}
