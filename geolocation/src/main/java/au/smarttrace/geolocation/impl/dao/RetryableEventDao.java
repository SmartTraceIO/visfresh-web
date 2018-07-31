/**
 *
 */
package au.smarttrace.geolocation.impl.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.RequestStatus;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.geolocation.impl.RetryableEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class RetryableEventDao {
    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    @Autowired
    public RetryableEventDao(final NamedParameterJdbcTemplate jdbc) {
        super();
        this.jdbc = jdbc;
    }

    /**
     * @param msg
     */
    public void updateRetryValues(final RetryableEvent msg) {
        final Map<String, Object> params = new HashMap<>();
        params.put("retryOn", msg.getRetryOn());
        params.put("numRetries", msg.getNumberOfRetry());
        jdbc.update("update locationrequests set retryon = :retryOn, numretry = :numRetries", params);
    }
    /**
     * @param msg
     * @param status
     */
    public void saveStatus(final RetryableEvent msg, final RequestStatus status) {
        final Map<String, Object> params = new HashMap<>();
        params.put("status", status.name());
        jdbc.update("update locationrequests set status = :status", params);
    }
    /**
     * @param e
     */
    public void save(final RetryableEvent e) {
        final Map<String, Object> params = new HashMap<>();
        //request fields
        final GeoLocationRequest req = e.getRequest();
        //-- geo location request fields
        //userdata longtext, -- data supplied by requestor.
        params.put("userdata", req.getUserData());
        //buffer longtext NOT NULL, -- request/response
        params.put("buffer", req.getBuffer());
        //`type` varchar(32) NOT NULL, -- request type (UnwiredLabs or other)
        params.put("type", req.getType().name());
        //sender varchar(32) NOT NULL, -- request sender. The sender identifies self by given field
        params.put("sender", req.getSender());
        //status varchar(16), -- error | success | NULL
        params.put("status", req.getStatus() == null ? null : req.getStatus().name());

        //retry fields
        //retryon timestamp NULL default NULL,
        params.put("retryon", e.getRetryOn());
        //numretry int not null default 0,
        params.put("numretry", e.getNumberOfRetry());

        final StringBuilder sql = new StringBuilder();
        if (e.getId() == null) {
            sql.append("insert into locationrequests (");
            sql.append(String.join(",", params.keySet()));
            sql.append(") values (");
            sql.append(':').append(String.join(",:", params.keySet()));
            sql.append(')');
        } else {
            //id bigint(20) NOT NULL AUTO_INCREMENT,
            params.put("id", e.getId());

            sql.append("update locationrequests set ");
            boolean first = true;
            for (final String param : params.keySet()) {
                if (!first) {
                    sql.append(',');
                } else {
                    first = false;
                }

                sql.append(param).append("=:").append(param);
            }
            sql.append(" where id = :id");
        }

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql.toString(), new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            e.setId(keyHolder.getKey().longValue());
        }
    }
    /**
     * @param date
     * @return
     */
    public List<RetryableEvent> getRetryableEventsForProcess(final Date date, final Set<ServiceType> types) {
        if (types.isEmpty()) {
            return new LinkedList<>();
        }

        final Map<String, Object> params = new HashMap<>();
        params.put("retryon", date);

        final Set<String> typesStr = new HashSet<>();
        for (final ServiceType t : types) {
            typesStr.add(t.name());
            params.put(t.name(), t.name());
        }

        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from locationrequests where type in (:"
                + String.join(",:", typesStr)
                + ") and status is NULL and retryon <= :retryon", params);
        return createEvents(rows);
    }
    /**
     * @param req
     */
    public Long saveNewRequest(final GeoLocationRequest req) {
        final RetryableEvent e = new RetryableEvent();
        e.setRequest(req);
        save(e);
        return e.getId();
    }
    /**
     * @param requestor geo location resolving requestor.
     * @return list of processed events with success or error status.
     */
    public List<RetryableEvent> getProcessedRequests(final String requestor) {
        final Map<String, Object> params = new HashMap<>();
        params.put("sender", requestor);
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select * from locationrequests where sender = :sender and not status is NULL", params);
        return createEvents(rows);
    }
    /**
     * @param rows
     * @return
     */
    private List<RetryableEvent> createEvents(final List<Map<String, Object>> rows) {
        final List<RetryableEvent> events = new LinkedList<>();
        for (final Map<String,Object> row : rows) {
            events.add(createEvent(row));
        }
        return events;
    }
    /**
     * @param row
     * @return
     */
    protected RetryableEvent createEvent(final Map<String, Object> row) {
        //request fields
        final GeoLocationRequest req = new GeoLocationRequest();
        //-- geo location request fields
        //userdata longtext, -- data supplied by requestor.
        req.setUserData((String) row.get("userdata"));
        //buffer longtext NOT NULL, -- request/response
        req.setBuffer((String) row.get("buffer"));
        //`type` varchar(32) NOT NULL, -- request type (UnwiredLabs or other)
        req.setType(ServiceType.valueOf((String) row.get("type")));
        //sender varchar(32) NOT NULL, -- request sender. The sender identifies self by given field
        req.setSender((String) row.get("sender"));
        //status varchar(16), -- error | success | NULL
        final String statusStr = (String) row.get("status");
        if (statusStr != null) {
            req.setStatus(RequestStatus.valueOf(statusStr));
        }

        //retry fields
        final RetryableEvent e = new RetryableEvent();
        //id bigint(20) NOT NULL AUTO_INCREMENT,
        e.setId(((Number) row.get("id")).longValue());
        //retryon timestamp NULL default NULL,
        e.setRetryOn((Date) row.get("retryon"));
        //numretry int not null default 0,
        e.setNumberOfRetry(((Number) row.get("numretry")).intValue());

        e.setRequest(req);
        return e;
    }

    /**
     * @param e
     */
    public void deleteRequest(final RetryableEvent e) {
        final Map<String, Object> params = new HashMap<>();
        params.put("id", e.getId());
        jdbc.update("delete from locationrequests where id = :id", params);
    }
}
