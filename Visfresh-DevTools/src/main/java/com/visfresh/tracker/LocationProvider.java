/**
 *
 */
package com.visfresh.tracker;

import java.util.List;

import com.visfresh.model.Location;
import com.visfresh.model.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface LocationProvider {
    Location getLocation(String device, List<StationSignal> signals);
}
