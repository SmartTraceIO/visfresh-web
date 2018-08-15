/**
 *
 */
package au.smarttrace.geolocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;
import au.smarttrace.json.ObjectMapperFactory;
import au.smarttrace.unwiredlabs.UnwiredLabsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
public class FastXmlGeoLocationFacadeTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private DbSupport support;
    @Autowired
    private RetryableEventDao dao;
    private FastXmlGeoLocationFacade<TestMessage> facade;
    private String sender = "testSender";
    protected static final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();

    /**
     * Default constructor.
     */
    public FastXmlGeoLocationFacadeTest() {
        super();
    }

    @Before
    public void setUp() {
        this.facade = FastXmlGeoLocationFacade.createFacade(jdbc, ServiceType.UnwiredLabs,
                TestMessage.class, sender);
    }
    @Test
    public void testProcessResolvedLocations() {
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);
        createEvent(RequestStatus.error, ServiceType.UnwiredLabs, sender);
        createEvent(null, ServiceType.UnwiredLabs, sender);

        final List<TestMessage> success = new LinkedList<>();
        final List<TestMessage> error = new LinkedList<>();

        facade.processResolvedLocations(new ResolvedLocationHandler<TestMessage>() {
            @Override
            public void handle(final TestMessage userData, final Location loc, final RequestStatus status) {
                userData.setLocation(loc);
                if (status == RequestStatus.success) {
                    success.add(userData);
                } else if (status == RequestStatus.error) {
                    error.add(userData);
                }
            }
        });

        assertEquals(1, success.size());
        assertEquals(1, error.size());
        assertEquals(1, support.getAllEvents().size());
    }
    @Test
    public void testSendRequest() throws IOException {
        final String field = "UserData";

        final TestMessage userData = new TestMessage();
        userData.setField(field);

        final String imei = "12093847987234";
        final String radio = "lte";

        final int ci = 1234567;
        final int lac = 54321;
        final int level = 15;
        final int mcc = 501;
        final int mnc = 1;

        StationSignal sig = new StationSignal();
        sig.setCi(ci);
        sig.setLac(lac);
        sig.setLevel(level);
        sig.setMcc(mcc);
        sig.setMnc(mnc);

        GsmLocationResolvingRequest gsm = new GsmLocationResolvingRequest();
        gsm.setImei(imei);
        gsm.setRadio(radio);
        gsm.getStations().add(sig);
        gsm.getStations().add(sig);

        final DataWithGsmInfo<TestMessage> data = new DataWithGsmInfo<>();
        data.setGsmInfo(gsm);
        data.setUserData(userData);
        facade.saveRequest(facade.createRequest(data));

        final List<RetryableEvent> events = support.getAllEvents();
        assertEquals(1, events.size());

        final GeoLocationRequest req = events.get(0).getRequest();
        assertEquals(sender, req.getSender());

        final TestMessage tm = json.readValue(req.getUserData(), TestMessage.class);
        assertEquals(field, tm.getField());

        assertNull(req.getStatus());
        assertEquals(ServiceType.UnwiredLabs, req.getType());

        //test correct serialized service request
        gsm = UnwiredLabsService.parseRequest(req.getBuffer());
        assertEquals(imei, gsm.getImei());
        assertEquals(radio, gsm.getRadio());
        assertEquals(2, gsm.getStations().size());

        //test station
        sig = gsm.getStations().get(0);
        assertEquals(ci, sig.getCi());
        assertEquals(lac, sig.getLac());
        assertEquals(level, sig.getLevel());
        assertEquals(mcc, sig.getMcc());
        assertEquals(mnc, sig.getMnc());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsFilterByServiceType() {
        final RetryableEvent e1 = createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);
        createEvent(RequestStatus.success, ServiceType.TestType, sender);

        final List<GeoLocationResponse> responses = facade.getAndRemoveProcessedResponses(1000);
        assertEquals(1, responses.size());
        assertEquals(e1.getRequest().getType(), responses.get(0).getType());

    }
    @Test
    public void testGetAndRemoveProcessedRequestsLimit() {
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);

        assertEquals(2, facade.getAndRemoveProcessedResponses(1000).size());

        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);

        assertEquals(1, facade.getAndRemoveProcessedResponses(1).size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsFilterBySender() {
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, "other");

        final List<GeoLocationResponse> responses = facade.getAndRemoveProcessedResponses(1000);
        assertEquals(1, responses.size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsFilterByStatus() {
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);
        createEvent(null, ServiceType.UnwiredLabs, sender);

        final List<GeoLocationResponse> responses = facade.getAndRemoveProcessedResponses(1000);
        assertEquals(1, responses.size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsRemoveReturned() {
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);
        createEvent(RequestStatus.success, ServiceType.UnwiredLabs, sender);
        createEvent(null, ServiceType.UnwiredLabs, sender);

        facade.getAndRemoveProcessedResponses(1000);
        assertEquals(1, support.getAllEvents().size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsSuccess() throws JsonProcessingException {
        final TestMessage userData = new TestMessage();
        createSuccessEvent(sender, RequestStatus.success, ServiceType.UnwiredLabs, userData);

        final GeoLocationResponse resp = facade.getAndRemoveProcessedResponses(1000).get(0);
        assertEquals(RequestStatus.success, resp.getStatus());
        assertNotNull(resp.getLocation());
        assertEquals(ServiceType.UnwiredLabs, resp.getType());
        assertEquals(json.writeValueAsString(userData), resp.getUserData());
        assertEquals(0, support.getAllEvents().size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsError() throws JsonProcessingException {
        final TestMessage userData = new TestMessage();
        createSuccessEvent(sender, RequestStatus.error, ServiceType.UnwiredLabs, userData);

        final GeoLocationResponse resp = facade.getAndRemoveProcessedResponses(1000).get(0);
        assertEquals(RequestStatus.error, resp.getStatus());
        assertNull(resp.getLocation());
        assertEquals(ServiceType.UnwiredLabs, resp.getType());
        assertEquals(json.writeValueAsString(userData), resp.getUserData());
        assertEquals(0, support.getAllEvents().size());
    }
    /**
     * @param requestor TODO
     */
    private RetryableEvent createEvent(final RequestStatus status,
            final ServiceType type, final String requestor) {
        return createSuccessEvent(requestor, status, type, new TestMessage());
    }
    /**
     * @param requestor
     */
    private RetryableEvent createSuccessEvent(final String requestor,
            final RequestStatus status, final ServiceType type, final TestMessage userData) {
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
