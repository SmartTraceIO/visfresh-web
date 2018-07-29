/**
 *
 */
package au.smarttrace.geolocation.impl;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GeoLocationRequest {
    /**
     * The message ID.
     */
    private Long id;
    /**
     * data supplied by user.
     */
    private String userData;
    /**
     * Request/response buffer
     */
    private String buffer;
    /**
     * Requested service type.
     */
    private ServiceType type;
    /**
     * request sender. The sender identifies self by given field
     */
    String sender;
    /**
     * Request status.
     */
    private RequestStatus status;

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
    public GeoLocationRequest() {
        super();
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
     * @return the userData
     */
    public String getUserData() {
        return userData;
    }
    /**
     * @param userData the userData to set
     */
    public void setUserData(final String userData) {
        this.userData = userData;
    }
    /**
     * @return the buffer
     */
    public String getBuffer() {
        return buffer;
    }
    /**
     * @param buffer the buffer to set
     */
    public void setBuffer(final String buffer) {
        this.buffer = buffer;
    }
    /**
     * @return the status
     */
    public RequestStatus getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(final RequestStatus status) {
        this.status = status;
    }
    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }
    /**
     * @param sender the sender to set
     */
    public void setSender(final String sender) {
        this.sender = sender;
    }
    /**
     * @return the type
     */
    public ServiceType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final ServiceType type) {
        this.type = type;
    }
}
