/**
 *
 */
package au.smarttrace.geolocation;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface GeoLocationService {
    String requestLocation(String request) throws GeoLocationServiceException;
}
