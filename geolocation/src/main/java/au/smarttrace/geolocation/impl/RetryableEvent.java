/**
 *
 */
package au.smarttrace.geolocation.impl;

import java.util.Date;

import au.smarttrace.geolocation.GeoLocationRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RetryableEvent {
    private GeoLocationRequest request;
    /**
     * The message ID.
     */
    private Long id;
    /**
     * The number of retry.
     */
    private int numberOfRetry;
    /**
     * The ready on date.
     */
    private Date retryOn = new Date();

    /**
     * Default constructor.
     */
    public RetryableEvent() {
        super();
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
    /**
     * @return the req
     */
    public GeoLocationRequest getRequest() {
        return request;
    }
    /**
     * @param request the request to set
     */
    public void setRequest(final GeoLocationRequest request) {
        this.request = request;
    }
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
}
