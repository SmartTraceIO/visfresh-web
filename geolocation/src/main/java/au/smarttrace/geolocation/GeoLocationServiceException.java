/**
 *
 */
package au.smarttrace.geolocation;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GeoLocationServiceException extends Exception {
    private static final long serialVersionUID = -6333136803281304890L;
    private int numberOfRetry = -1;

    /**
     * Can retry message.
     */
    private boolean canRetry = true;

    /**
     * Default constructor.
     */
    public GeoLocationServiceException() {
        super();
    }
    /**
     * @param message message.
     */
    public GeoLocationServiceException(final String message) {
        super(message);
    }
    /**
     * @param cause cause.
     */
    public GeoLocationServiceException(final Throwable cause) {
        super(cause);
    }
    /**
     * @param message message.
     * @param cause cause exception.
     */
    public GeoLocationServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
    /**
     * @param message message.
     * @param cause cause exception.
     * @param enableSuppression
     * @param writableStackTrace
     */
    public GeoLocationServiceException(final String message, final Throwable cause,
            final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    /**
     * @return the canRetry
     */
    public boolean canRetry() {
        return canRetry;
    }
    /**
     * @param canRetry the canRetry to set
     */
    public void setCanRetry(final boolean canRetry) {
        this.canRetry = canRetry;
    }
    /**
     * @return the numberOfRetry
     */
    public int getNumberOfRetry() {
        return numberOfRetry;
    }
    /**
     * @param numberOfRetry the numberOfRetry to set
     */
    public void setNumberOfRetry(final int numberOfRetry) {
        this.numberOfRetry = numberOfRetry;
    }
}
