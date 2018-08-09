/**
 *
 */
package au.smarttrace.geolocation;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.smarttrace.geolocation.impl.RetryableEvent;
import au.smarttrace.geolocation.impl.dao.RetryableEventDao;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.json.ObjectMapperFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GeoLocationHelper {
    private static final Logger log = LoggerFactory.getLogger(GeoLocationHelper.class);
    private static final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();

    private RetryableEventDao dao;
    private final ServiceType type;

    protected GeoLocationHelper(final NamedParameterJdbcTemplate jdbc, final ServiceType type) {
        super();
        dao = new RetryableEventDao(jdbc);
        this.type = type;
    }
    /**
     * @param req
     */
    public void saveRequest(final GeoLocationRequest req) {
        dao.saveNewRequest(req);
    }
    /**
     * @param sender
     * @param userData
     * @param gsm
     * @return
     */
    public GeoLocationRequest createRequest(final String sender, final String userData,
            final GsmLocationResolvingRequest gsm) {
        return createRequest(sender, userData, gsm, type);
    }
    /**
     * @param sender
     * @param userData
     * @param gsm
     * @param type
     * @return
     */
    public static GeoLocationRequest createRequest(
            final String sender, final String userData, final GsmLocationResolvingRequest gsm,
            final ServiceType type) {
        //create request
        final GeoLocationRequest req = new GeoLocationRequest();
        try {
            req.setBuffer(json.writeValueAsString(gsm));
            req.setSender(sender);
            req.setType(type);
            req.setUserData(userData);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return req;
    }
    /**
     * @param sender request sender.
     * @param limit max number of fetch events.
     * @return list of GEO location responses.
     */
    public List<GeoLocationResponse> getAndRemoveProcessedResponses(
            final String sender, final int limit) {
        final List<RetryableEvent> events = dao.getProcessedRequests(sender, limit, type);

        //create responses.
        final List<GeoLocationResponse> responses = new LinkedList<>();
        final List<RetryableEvent> toDelete = new LinkedList<>();

        for (final RetryableEvent e : events) {
            final GeoLocationRequest req = e.getRequest();
            final GeoLocationResponse resp = new GeoLocationResponse();

            resp.setStatus(req.getStatus());
            resp.setType(req.getType());
            resp.setUserData(req.getUserData());

            try {
                if (req.getStatus() == RequestStatus.success) {
                    final Location loc = json.readValue(req.getBuffer(), Location.class);
                    resp.setLocation(loc);
                }

                responses.add(resp);
                toDelete.add(e);
            } catch (final Exception exc) {
                log.error("Failed to create Geo Location response", exc);
            }

        }

        //delete
        for (final RetryableEvent e : toDelete) {
            try {
                dao.deleteRequest(e);
            } catch (final Exception exc) {
                log.error("Failed to delete Geo Location request", exc);
            }
        }

        return responses;
    }
    /**
     * @param jdbc JDBC support.
     * @param type service type.
     * @return helper instance.
     */
    public static GeoLocationHelper createHelper(
            final NamedParameterJdbcTemplate jdbc, final ServiceType type) {
        return new GeoLocationHelper(jdbc, type);
    }
}
