/**
 *
 */
package au.smarttrace.eel.service;

import au.smarttrace.gsm.GsmLocationResolvingRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UnwiredLabsRequest {
    private String sender;
    private String userData;
    private GsmLocationResolvingRequest request;

    /**
     * Default constructor.
     */
    public UnwiredLabsRequest() {
        super();
    }

    /**
     * @param sender
     */
    public void setSender(final String sender) {
        this.sender = sender;
    }
    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @param userData
     */
    public void setUserData(final String userData) {
        this.userData = userData;
    }
    /**
     * @return the userData
     */
    public String getUserData() {
        return userData;
    }

    /**
     * @param r
     */
    public void setRequest(final GsmLocationResolvingRequest r) {
        this.request = r;
    }
    /**
     * @return the request
     */
    public GsmLocationResolvingRequest getRequest() {
        return request;
    }
}
