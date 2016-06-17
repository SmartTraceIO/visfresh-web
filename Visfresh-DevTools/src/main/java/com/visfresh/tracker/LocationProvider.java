/**
 *
 */
package com.visfresh.tracker;

import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface LocationProvider {
    Location getLocation(List<StationSignal> signals);
}