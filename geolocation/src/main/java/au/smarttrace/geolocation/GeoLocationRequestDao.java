/**
 *
 */
package au.smarttrace.geolocation;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.geolocation.impl.dao.GeoLocationRequestDaoImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface GeoLocationRequestDao {
    public class Factory {
        /**
         *
         */
        public static GeoLocationRequestDao create(final NamedParameterJdbcTemplate jdbc) {
            return new GeoLocationRequestDaoImpl(jdbc);
        }
    }

    void saveRequest(GeoLocationRequest req);
    List<GeoLocationRequest> getProcessedRequests(String requestor);
}
