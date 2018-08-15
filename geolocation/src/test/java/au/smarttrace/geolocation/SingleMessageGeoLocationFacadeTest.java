/**
 *
 */
package au.smarttrace.geolocation;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.smarttrace.geolocation.impl.RetryableEvent;
import au.smarttrace.geolocation.impl.dao.RetryableEventDao;
import au.smarttrace.geolocation.junit.db.DaoTestRunner;
import au.smarttrace.geolocation.junit.db.DbSupport;
import au.smarttrace.json.ObjectMapperFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
public class SingleMessageGeoLocationFacadeTest {
    protected static final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private DbSupport support;
    @Autowired
    private RetryableEventDao dao;
    private SingleMessageGeoLocationFacade facade;
    private String sender = "testSender";

    /**
     * Default constructor.
     */
    public SingleMessageGeoLocationFacadeTest() {
        super();
    }

    @Before
    public void setUp() {
        this.facade = SingleMessageGeoLocationFacade.createFacade(jdbc, ServiceType.UnwiredLabs, sender);
    }
    @Test
    public void testProcessResolvedLocations() {
        final DeviceMessage msg = new DeviceMessage();
        msg.setImei("234098098098");
        msg.setTime(new Date());

        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender, msg);
        createEvent(RequestStatus.error, ServiceType.UnwiredLabs, sender, msg);
        createEvent(null, ServiceType.UnwiredLabs, sender, msg);

        facade.processResolvedLocations();

        final List<Map<String, Object>> rows = jdbc.queryForList("select * from systemmessages",
                new HashMap<>());
        assertEquals(2, rows.size());
        assertEquals(1, support.getAllEvents().size());
    }
    /**
     * @param requestor
     */
    private RetryableEvent createEvent(final RequestStatus status, final ServiceType type,
            final String requestor, final DeviceMessage userData) {
        final GeoLocationRequest r = new GeoLocationRequest();
        r.setBuffer("{\"latitude\": 1.0, \"longitude\": 2.0}");
        r.setSender("");
        try {
            r.setUserData(json.writeValueAsString(userData));
        } catch (final JsonProcessingException exc) {
            throw new RuntimeException(exc);
        }
        r.setType(type);
        r.setStatus(status);
        r.setSender(requestor);

        final RetryableEvent e = new RetryableEvent();
        e.setRetryOn(new Date(System.currentTimeMillis() - 10000000l));
        e.setRequest(r);
        dao.save(e);
        return e;
    }

    @After
    public void tearDown() {
        support.clearRequests();
        support.clearSystemMessages();
    }
}
