/**
 *
 */
package au.smarttrace.geolocation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.GeoLocationService;
import au.smarttrace.geolocation.GeoLocationServiceException;
import au.smarttrace.geolocation.RequestStatus;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.geolocation.impl.dao.RetryableEventDao;
import au.smarttrace.geolocation.junit.db.DaoTestRunner;
import au.smarttrace.geolocation.junit.db.DbSupport;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
public class GeoLocationDispatcherTest extends GeoLocationDispatcherImpl {
    @Autowired
    private RetryableEventDao dao;
    @Autowired
    private DbSupport support;

    /**
     * Default constructor.
     */
    public GeoLocationDispatcherTest() {
        super();
    }

    @Before
    public void setUp() {
        start();
        setRetryTimeOut(100000l);
    }

    @After
    public void tearDown() {
        support.clearRequests();
        stop();
    }

    @Test
    public void testNotHandlers() {
        createEvent();
        assertEquals(0, processMessages());
    }
    @Test
    public void testSuccess() {
        final RetryableEvent e1 = createEvent();
        final RetryableEvent e2 = createEvent();
        final RetryableEvent e3 = createEvent();

        setGeoLocationService(ServiceType.UnwiredLabs, new GeoLocationService() {
            @Override
            public String requestLocation(final String request) throws GeoLocationServiceException {
                return "ok";
            }
        });

        assertEquals(3, processMessages());

        final Map<Long, RequestStatus> statuses = getRequestStatuses();
        assertEquals(RequestStatus.success, statuses.get(e1.getId()));
        assertEquals(RequestStatus.success, statuses.get(e2.getId()));
        assertEquals(RequestStatus.success, statuses.get(e3.getId()));

        assertEquals("ok", support.getEvent(e1.getId()).getRequest().getBuffer());
    }
    @Test
    public void testRetryableError() {
        setRetryLimit(2);
        RetryableEvent e = createEvent();

        setGeoLocationService(ServiceType.UnwiredLabs, new GeoLocationService() {
            @Override
            public String requestLocation(final String request) throws GeoLocationServiceException {
                final GeoLocationServiceException exc = new GeoLocationServiceException();
                exc.setCanRetry(true);
                throw exc;
            }
        });

        assertEquals(1, processMessages());
        //test not just next processed because retry on shifted
        assertEquals(0, processMessages());

        e = support.getEvent(e.getId());
        assertNull(e.getRequest().getStatus());
        assertTrue(e.getRetryOn().after(new Date()));

        //shift and retry
        e.setRetryOn(new Date(System.currentTimeMillis() - 1000));
        dao.save(e);
        assertEquals(1, processMessages());
        e = support.getEvent(e.getId());
        assertNull(e.getRequest().getStatus());

        //shift and retry
        e.setRetryOn(new Date(System.currentTimeMillis() - 1000));
        dao.save(e);
        assertEquals(1, processMessages());
        e = support.getEvent(e.getId());
        assertEquals(RequestStatus.error, e.getRequest().getStatus());
    }
    @Test
    public void testNotRetryableError() {
        setRetryLimit(2);
        RetryableEvent e = createEvent();

        setGeoLocationService(ServiceType.UnwiredLabs, new GeoLocationService() {
            @Override
            public String requestLocation(final String request) throws GeoLocationServiceException {
                final GeoLocationServiceException exc = new GeoLocationServiceException();
                exc.setCanRetry(false);
                throw exc;
            }
        });

        assertEquals(1, processMessages());
        e = support.getEvent(e.getId());
        assertEquals(RequestStatus.error, e.getRequest().getStatus());
    }
    /* (non-Javadoc)
     * @see au.smarttrace.geolocation.impl.GeoLocationDispatcherImpl#startDispatcherThread()
     */
    @Override
    protected void startDispatcherThread() {
        // not start in fact.
    }
    /**
     * @return
     */
    private RetryableEvent createEvent() {
        final GeoLocationRequest r = new GeoLocationRequest();
        r.setBuffer("");
        r.setSender("");
        r.setType(ServiceType.UnwiredLabs);
        r.setUserData("");

        final RetryableEvent e = new RetryableEvent();
        e.setRetryOn(new Date(System.currentTimeMillis() - 1000l));
        e.setRequest(r);
        dao.save(e);
        return e;
    }
    private Map<Long, RequestStatus> getRequestStatuses() {
        final List<Map<String, Object>> rows = support.getJdbc().queryForList(
                "select id, status from locationrequests", new HashMap<>());

        final Map<Long, RequestStatus> result = new HashMap<>();
        for (final Map<String, Object> row : rows) {
            final String statusStr = (String) row.get("status");
            result.put(((Number) row.get("id")).longValue(),
                    statusStr == null ? null : RequestStatus.valueOf(statusStr));
        }
        return result;
    }
}
