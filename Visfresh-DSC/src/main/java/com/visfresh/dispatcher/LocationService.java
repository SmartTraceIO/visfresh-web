/**
 *
 */
package com.visfresh.dispatcher;

import java.util.List;

import com.visfresh.Location;
import com.visfresh.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface LocationService {
    Location getLocation(String imei, String radio, List<StationSignal> stations) throws RetryableException;
}
