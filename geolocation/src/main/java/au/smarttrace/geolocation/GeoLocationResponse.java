/**
 *
 */
package au.smarttrace.geolocation;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GeoLocationResponse {
    /**
     * data supplied by user.
     */
    private String userData;
    /**
     * location if resolved.
     */
    private Location location;
    /**
     * Requested service type.
     */
    private ServiceType type;
    /**
     * Request status.
     */
    private RequestStatus status;

    /**
     * Default constructor.
     */
    public GeoLocationResponse() {
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
     * @return the location
     */
    public Location getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(final Location location) {
        this.location = location;
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
}
