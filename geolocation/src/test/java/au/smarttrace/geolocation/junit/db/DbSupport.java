/**
 *
 */
package au.smarttrace.geolocation.junit.db;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import au.smarttrace.geolocation.impl.RetryableEvent;
import au.smarttrace.geolocation.impl.dao.RetryableEventDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DbSupport {
    private class AccessibleRetryableEventDao extends RetryableEventDao {
        /**
         * @param jdbc
         */
        public AccessibleRetryableEventDao(final NamedParameterJdbcTemplate jdbc) {
            super(jdbc);
        }
        /* (non-Javadoc)
         * @see au.smarttrace.geolocation.impl.dao.RetryableEventDao#createEvent(java.util.Map)
         */
        @Override
        public RetryableEvent createEvent(final Map<String, Object> row) {
            return super.createEvent(row);
        }
    }

    /**
     * JDBC template
     */
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    private AccessibleRetryableEventDao eventDao;

    /**
     * Default constructor.
     */
    public DbSupport() {
        super();
    }

    @PostConstruct
    public void createEventDao() {
        eventDao = new AccessibleRetryableEventDao(jdbc);
    }

    /**
     * Deletes devices.
     */
    public void clearRequests() {
        jdbc.update("delete from locationrequests", new HashMap<>());
    }
    /**
     * @param id event ID.
     * @return
     */
    public RetryableEvent getEvent(final Long id) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from locationrequests where id = :id", params);
        if (rows.size() > 0) {
            return eventDao.createEvent(rows.get(0));
        }
        return null;
    }
    public List<RetryableEvent> getAllEvents() {
        final List<RetryableEvent> events = new LinkedList<>();
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from locationrequests", new HashMap<>());
        for (final Map<String, Object> row : rows) {
            events.add(eventDao.createEvent(row));
        }
        return events;
    }
    /**
     * @return the jdbc
     */
    public NamedParameterJdbcTemplate getJdbc() {
        return jdbc;
    }
}
