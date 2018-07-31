/**
 *
 */
package au.smarttrace.geolocation;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GeoLocationRequest {
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
     * Default constructor.
     */
    public GeoLocationRequest() {
        super();
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
