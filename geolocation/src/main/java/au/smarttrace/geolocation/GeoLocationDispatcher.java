/**
 *
 */
package au.smarttrace.geolocation;

import au.smarttrace.geolocation.impl.ServiceType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface GeoLocationDispatcher {
    void setGeoLocationService(ServiceType type, GeoLocationService s);
}
