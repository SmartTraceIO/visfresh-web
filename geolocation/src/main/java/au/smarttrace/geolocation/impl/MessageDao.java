/**
 *
 */
package au.smarttrace.geolocation.impl;

import java.util.Date;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageDao {

    /**
     * @param msg
     */
    public void saveForRetry(final GeoLocationRequest msg) {
        // TODO Auto-generated method stub

    }
    /**
     * @param msg
     * @param error
     */
    public void saveStatus(final GeoLocationRequest msg, final RequestStatus error) {
        // TODO Auto-generated method stub

    }
    /**
     * @param r
     */
    public void save(final GeoLocationRequest r) {
        // TODO Auto-generated method stub

    }
    /**
     * @param date
     * @return
     */
    public List<GeoLocationRequest> getGeoLocationRequestsForProcess(final Date date) {
        // TODO Auto-generated method stub
        return null;
    }

}
