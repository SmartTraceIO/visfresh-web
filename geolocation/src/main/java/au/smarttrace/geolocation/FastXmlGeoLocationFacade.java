/**
 *
 */
package au.smarttrace.geolocation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.smarttrace.geolocation.impl.RetryableEvent;
import au.smarttrace.geolocation.impl.dao.RetryableEventDao;
import au.smarttrace.geolocation.impl.dao.SystemMessageDao;
import au.smarttrace.json.ObjectMapperFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class FastXmlGeoLocationFacade<T> {
    private static final Logger log = LoggerFactory.getLogger(FastXmlGeoLocationFacade.class);
    protected static final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();

    private final RetryableEventDao eventDao;
    private final SystemMessageDao systemMessageDao;

    private final ServiceType type;
    private final Class<T> userDataClass;
    private final String sender;

    protected FastXmlGeoLocationFacade(final NamedParameterJdbcTemplate jdbc,
            final ServiceType type, final Class<T> userDataClass, final String sender) {
        super();
        eventDao = new RetryableEventDao(jdbc);
        systemMessageDao = new SystemMessageDao(jdbc);
        this.userDataClass = userDataClass;
        this.type = type;
        this.sender = sender;
    }

    public void processResolvedLocations(final ResolvedLocationHandler<T> h) {
        for (int i = 0; i < 10; i++) {
            final List<GeoLocationResponse> responses = getAndRemoveProcessedResponses(20);
            if (responses.isEmpty()) {
                break;
            }

            for (final GeoLocationResponse resp : responses) {
                final Location loc = resp.getLocation();
                try {
                    final T r = json.readValue(resp.getUserData(), userDataClass);
                    h.handle(r, loc, resp.getStatus());
                } catch (final IOException exc) {
                    log.error("Failed to send resolved message to system", exc);
                }
            }
        }
    }
    /**
     * @param sm system message.
     */
    protected void sendResolvedMessages(final SystemMessage sm) {
        systemMessageDao.save(sm);
    }
    /**
     * @param sm system message.
     */
    protected void sendResolvedMessagesFor(final DeviceMessage msg) {
        systemMessageDao.sendSystemMessageFor(msg);
    }
    /**
     * @param req
     */
    public void saveRequest(final GeoLocationRequest req) {
        eventDao.saveNewRequest(req);
    }
    /**
     * @param data TODO
     * @return
     */
    public GeoLocationRequest createRequest(final DataWithGsmInfo<T> data) {
        final GeoLocationRequest req = new GeoLocationRequest();
        try {
            req.setBuffer(json.writeValueAsString(data.getGsmInfo()));
            req.setSender(sender);
            req.setType(type);
            req.setUserData(json.writeValueAsString(data.getUserData()));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return req;
    }
    /**
     * @param limit max number of fetch events.
     * @return list of GEO location responses.
     */
    public List<GeoLocationResponse> getAndRemoveProcessedResponses(
            final int limit) {
        final List<RetryableEvent> events = eventDao.getProcessedRequests(sender, limit, type);

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
                eventDao.deleteRequest(e);
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
    public static <T> FastXmlGeoLocationFacade<T> createFacade(
            final NamedParameterJdbcTemplate jdbc, final ServiceType type,
            final Class<T> clazz, final String sender) {
        return new FastXmlGeoLocationFacade<T>(jdbc, type, clazz, sender);
    }
}
