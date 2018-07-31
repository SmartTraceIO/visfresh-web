/**
 *
 */
package au.smarttrace.geolocation.impl.dao;

import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.GeoLocationRequestDao;
import au.smarttrace.geolocation.impl.RetryableEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GeoLocationRequestDaoImpl implements GeoLocationRequestDao {
    private RetryableEventDao dao;

    /**
     * @param jdbc JDBC template.
     */
    public GeoLocationRequestDaoImpl(final NamedParameterJdbcTemplate jdbc) {
        this.dao = new RetryableEventDao(jdbc);
    }

    /* (non-Javadoc)
     * @see au.smarttrace.geolocation.GeoLocationRequestDao#saveRequest(au.smarttrace.geolocation.GeoLocationRequest)
     */
    @Override
    public void saveRequest(final GeoLocationRequest req) {
        dao.saveNewRequest(req);
    }

    /* (non-Javadoc)
     * @see au.smarttrace.geolocation.GeoLocationRequestDao#getProcessedRequests()
     */
    @Override
    public List<RetryableEvent> getProcessedRequests(final String requestor) {
        return dao.getProcessedRequests(requestor);
    }
    @Override
    public void deleteRequest(final RetryableEvent e) {
        dao.deleteRequest(e);
    }
}
