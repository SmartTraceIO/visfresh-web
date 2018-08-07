/**
 *
 */
package au.smarttrace.unwiredlabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.geolocation.GeoLocationHelper;
import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.GeoLocationResponse;
import au.smarttrace.geolocation.RequestStatus;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.geolocation.impl.RetryableEvent;
import au.smarttrace.geolocation.impl.dao.RetryableEventDao;
import au.smarttrace.geolocation.junit.db.DaoTestRunner;
import au.smarttrace.geolocation.junit.db.DbSupport;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
public class GeoLocationHelperTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private DbSupport support;
    @Autowired
    private RetryableEventDao dao;
    private GeoLocationHelper helper;

    /**
     * Default constructor.
     */
    public GeoLocationHelperTest() {
        super();
    }

    @Before
    public void setUp() {
        this.helper = GeoLocationHelper.createHelper(jdbc, ServiceType.UnwiredLabs);
    }
    @Test
    public void testSendRequest() throws IOException {
        final String sender = "sender";
        final String userData = "userData";
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

        helper.saveRequest(sender, userData, gsm);

        final List<RetryableEvent> events = support.getAllEvents();
        assertEquals(1, events.size());

        final GeoLocationRequest req = events.get(0).getRequest();
        assertEquals(sender, req.getSender());
        assertEquals(userData, req.getUserData());
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
        final RetryableEvent e1 = createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.TestType);

        final List<GeoLocationResponse> responses = helper.getAndRemoveProcessedResponses("abcd", 1000);
        assertEquals(1, responses.size());
        assertEquals(e1.getRequest().getType(), responses.get(0).getType());

    }
    @Test
    public void testGetAndRemoveProcessedRequestsLimit() {
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);

        assertEquals(2, helper.getAndRemoveProcessedResponses("abcd", 1000).size());

        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);

        assertEquals(1, helper.getAndRemoveProcessedResponses("abcd", 1).size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsFilterBySender() {
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);
        createSuccessEvent("dcba", RequestStatus.success, ServiceType.UnwiredLabs);

        final List<GeoLocationResponse> responses = helper.getAndRemoveProcessedResponses("abcd", 1000);
        assertEquals(1, responses.size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsFilterByStatus() {
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);
        createSuccessEvent("abcd", null, ServiceType.UnwiredLabs);

        final List<GeoLocationResponse> responses = helper.getAndRemoveProcessedResponses("abcd", 1000);
        assertEquals(1, responses.size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsRemoveReturned() {
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs);
        createSuccessEvent("abcd", null, ServiceType.UnwiredLabs);

        helper.getAndRemoveProcessedResponses("abcd", 1000);
        assertEquals(1, support.getAllEvents().size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsSuccess() {
        final String userData = "userData";
        createSuccessEvent("abcd", RequestStatus.success, ServiceType.UnwiredLabs, userData);

        final GeoLocationResponse resp = helper.getAndRemoveProcessedResponses("abcd", 1000).get(0);
        assertEquals(RequestStatus.success, resp.getStatus());
        assertNotNull(resp.getLocation());
        assertEquals(ServiceType.UnwiredLabs, resp.getType());
        assertEquals(userData, resp.getUserData());
        assertEquals(0, support.getAllEvents().size());
    }
    @Test
    public void testGetAndRemoveProcessedRequestsError() {
        final String userData = "userData";
        createSuccessEvent("abcd", RequestStatus.error, ServiceType.UnwiredLabs, userData);

        final GeoLocationResponse resp = helper.getAndRemoveProcessedResponses("abcd", 1000).get(0);
        assertEquals(RequestStatus.error, resp.getStatus());
        assertNull(resp.getLocation());
        assertEquals(ServiceType.UnwiredLabs, resp.getType());
        assertEquals(userData, resp.getUserData());
        assertEquals(0, support.getAllEvents().size());
    }
    /**
     * @param requestor
     */
    private RetryableEvent createSuccessEvent(final String requestor,
            final RequestStatus status, final ServiceType type) {
        return createSuccessEvent(requestor, status, type, "");
    }
    /**
     * @param requestor
     */
    private RetryableEvent createSuccessEvent(final String requestor,
            final RequestStatus status, final ServiceType type, final String userData) {
        final GeoLocationRequest r = new GeoLocationRequest();
        r.setBuffer("{\"latitude\": 1.0, \"longitude\": 2.0}");
        r.setSender("");
        r.setUserData(userData);
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
    }
}
