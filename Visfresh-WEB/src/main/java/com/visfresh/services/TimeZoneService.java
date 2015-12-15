/**
 *
 */
package com.visfresh.services;

import java.util.List;
import java.util.TimeZone;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface TimeZoneService {
    List<TimeZone> getSupportedTimeZones();
}
