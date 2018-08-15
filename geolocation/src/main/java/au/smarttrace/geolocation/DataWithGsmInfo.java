/**
 *
 */
package au.smarttrace.geolocation;

import au.smarttrace.gsm.GsmLocationResolvingRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DataWithGsmInfo<T> {
    private T userData;
    private GsmLocationResolvingRequest gsmInfo;

    /**
     * Default constructor.
     */
    public DataWithGsmInfo() {
        super();
    }

    /**
     * @return the userData
     */
    public T getUserData() {
        return userData;
    }
    /**
     * @param userData the userData to set
     */
    public void setUserData(final T userData) {
        this.userData = userData;
    }
    /**
     * @return the req
     */
    public GsmLocationResolvingRequest getGsmInfo() {
        return gsmInfo;
    }
    /**
     * @param req the req to set
     */
    public void setGsmInfo(final GsmLocationResolvingRequest req) {
        this.gsmInfo = req;
    }
}
