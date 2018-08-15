/**
 *
 */
package au.smarttrace.geolocation;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class MultiBeaconMessageGeoLocationFacadeTest {
    protected static final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private DbSupport support;
    @Autowired
    private RetryableEventDao dao;
    private MultiBeaconGeoLocationFacade facade;
    private String sender = "testSender";

    /**
     * Default constructor.
     */
    public MultiBeaconMessageGeoLocationFacadeTest() {
        super();
    }

    @Before
    public void setUp() {
        this.facade = MultiBeaconGeoLocationFacade.createFacade(jdbc, ServiceType.UnwiredLabs, sender);
    }
    @Test
    public void testProcessResolvedLocations() {
        final DeviceMessage msg = new DeviceMessage();
        msg.setImei("234098098098");
        msg.setTime(new Date());

        final MultiBeaconMessage mbm1 = new MultiBeaconMessage();
        mbm1.setGateway("0328970987987");
        mbm1.setGatewayMessage(msg);
        mbm1.getBeacons().add(msg);
        mbm1.getBeacons().add(msg);

        final MultiBeaconMessage mbm2 = new MultiBeaconMessage();
        mbm2.setGateway("0328970987987");
        mbm2.getBeacons().add(msg);

        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender, mbm1);
        createEvent(RequestStatus.error, ServiceType.UnwiredLabs, sender, mbm2);
        createEvent(null, ServiceType.UnwiredLabs, sender, mbm1);

        facade.processResolvedLocations();

        final List<Map<String, Object>> rows = jdbc.queryForList("select * from systemmessages",
                new HashMap<>());
        assertEquals(4, rows.size());
        assertEquals(1, support.getAllEvents().size());
    }
    @Test
    public void testSuccessToLockChannel() {
        final DeviceMessage gateway = new DeviceMessage();
        gateway.setImei("234098098098");
        gateway.setTime(new Date());

        final DeviceMessage beacon = new DeviceMessage();
        beacon.setImei("3240987987987");
        beacon.setTime(new Date());
        beacon.setGateway(gateway.getImei());

        final MultiBeaconMessage request = new MultiBeaconMessage();
        request.setGateway(gateway.getImei());
        request.setGatewayMessage(gateway);
        request.getBeacons().add(beacon);

        final Set<String> beacons = new HashSet<>();
        beacons.add(beacon.getImei());

        final Set<String> locked = facade.lockChannels(beacons, gateway.getImei());
        assertEquals(1, locked.size());

        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender, request);
        facade.processResolvedLocations();

        final List<Map<String, Object>> rows = jdbc.queryForList("select * from systemmessages",
                new HashMap<>());
        assertEquals(2, rows.size());
    }
    @Test
    public void testNotSuccessToLockChannel() {
        final DeviceMessage gateway = new DeviceMessage();
        gateway.setImei("234098098098");
        gateway.setTime(new Date());

        final DeviceMessage beacon = new DeviceMessage();
        beacon.setImei("3240987987987");
        beacon.setGateway(gateway.getImei());
        beacon.setTime(new Date());

        final MultiBeaconMessage request = new MultiBeaconMessage();
        request.setGateway(gateway.getImei());
        request.setGatewayMessage(gateway);
        request.getBeacons().add(beacon);

        final Set<String> beacons = new HashSet<>();
        beacons.add(beacon.getImei());

        //lock beacon channels by another gateway
        final Set<String> locked = facade.lockChannels(beacons, gateway.getImei() + "11");
        assertEquals(1, locked.size());

        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender, request);
        facade.processResolvedLocations();

        //only gateway message should be processed.
        final List<Map<String, Object>> rows = jdbc.queryForList("select * from systemmessages",
                new HashMap<>());
        assertEquals(1, rows.size());
    }
    /**
     * @param requestor
     */
    private RetryableEvent createEvent(final RequestStatus status, final ServiceType type,
            final String requestor, final MultiBeaconMessage message) {
        final GeoLocationRequest r = new GeoLocationRequest();
        r.setBuffer("{\"latitude\": 1.0, \"longitude\": 2.0}");
        r.setSender("");
        try {
            r.setUserData(json.writeValueAsString(message));
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
        support.clearBeaconLocks();
    }
}
