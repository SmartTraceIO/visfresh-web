/**
 *
 */
package au.smarttrace.unwiredlabs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.geolocation.impl.RetryableEvent;
import au.smarttrace.geolocation.junit.db.DaoTestRunner;
import au.smarttrace.geolocation.junit.db.DbSupport;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
public class UnwiredLabsHelperTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private DbSupport support;
    private UnwiredLabsHelper helper;

    /**
     * Default constructor.
     */
    public UnwiredLabsHelperTest() {
        super();
    }

    @Before
    public void setUp() {
        this.helper = UnwiredLabsService.createHelper(jdbc);
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

    @After
    public void tearDown() {
        support.clearRequests();
    }
}
