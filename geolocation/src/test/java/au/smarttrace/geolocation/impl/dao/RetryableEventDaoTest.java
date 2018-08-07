/**
 *
 */
package au.smarttrace.geolocation.impl.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.RequestStatus;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.geolocation.impl.RetryableEvent;
import au.smarttrace.geolocation.junit.db.DaoTestRunner;
import au.smarttrace.geolocation.junit.db.DbSupport;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
public class RetryableEventDaoTest {
    @Autowired
    private RetryableEventDao dao;
    @Autowired
    private DbSupport support;

    /**
     * Default constructor.
     */
    public RetryableEventDaoTest() {
        super();
    }

    @Test
    public void testUpdateRetryValues() {
        final GeoLocationRequest r = new GeoLocationRequest();
        final String buffer = "buffer";
        final String sender = "sender";
        final ServiceType type = ServiceType.UnwiredLabs;
        final String userData = "userData";

        r.setBuffer(buffer);
        r.setSender(sender);
        r.setType(type);
        r.setUserData(userData);

        final Long id = dao.saveNewRequest(r);

        final int numOfRetry = 101;
        final Date retryOn = new Date(System.currentTimeMillis() + 1000000l);

        RetryableEvent e = support.getEvent(id);
        e.setNumberOfRetry(numOfRetry);
        e.setRetryOn(retryOn);
        dao.updateRetryValues(e);

        e = support.getEvent(e.getId());

        //event
        assertEquals(numOfRetry, e.getNumberOfRetry());
        assertTrue(Math.abs(retryOn.getTime() - e.getRetryOn().getTime()) < 1000l);

        //request
        assertEquals(buffer, e.getRequest().getBuffer());
        assertEquals(sender, e.getRequest().getSender());
        assertEquals(type, e.getRequest().getType());
        assertEquals(userData, e.getRequest().getUserData());
        assertNull(e.getRequest().getStatus());
    }
    @Test
    public void testSaveStatus() {
        final GeoLocationRequest r = new GeoLocationRequest();
        final String buffer = "buffer";
        final String sender = "sender";
        final ServiceType type = ServiceType.UnwiredLabs;
        final String userData = "userData";

        r.setBuffer(buffer);
        r.setSender(sender);
        r.setType(type);
        r.setUserData(userData);

        final Long id = dao.saveNewRequest(r);

        RetryableEvent e = support.getEvent(id);
        dao.saveStatus(e, RequestStatus.error);

        e = support.getEvent(e.getId());

        //request
        assertEquals(buffer, e.getRequest().getBuffer());
        assertEquals(sender, e.getRequest().getSender());
        assertEquals(type, e.getRequest().getType());
        assertEquals(userData, e.getRequest().getUserData());
        assertEquals(RequestStatus.error, e.getRequest().getStatus());
    }
    @Test
    public void testSave() {
        final GeoLocationRequest r = new GeoLocationRequest();
        final String buffer = "buffer";
        final String sender = "sender";
        final ServiceType type = ServiceType.UnwiredLabs;
        final String userData = "userData";
        final int numOfRetry = 101;
        final Date retryOn = new Date(System.currentTimeMillis() + 1000000l);

        r.setBuffer(buffer);
        r.setSender(sender);
        r.setType(type);
        r.setUserData(userData);
        r.setStatus(RequestStatus.error);

        RetryableEvent e = new RetryableEvent();
        e.setNumberOfRetry(numOfRetry);
        e.setRetryOn(retryOn);
        e.setRequest(r);

        dao.save(e);

        e = support.getEvent(e.getId());

        //event
        assertEquals(numOfRetry, e.getNumberOfRetry());
        assertTrue(Math.abs(retryOn.getTime() - e.getRetryOn().getTime()) < 1000l);

        //request
        assertEquals(buffer, e.getRequest().getBuffer());
        assertEquals(sender, e.getRequest().getSender());
        assertEquals(type, e.getRequest().getType());
        assertEquals(userData, e.getRequest().getUserData());
        assertEquals(RequestStatus.error, e.getRequest().getStatus());
    }
    @Test
    public void testUpdate() {
        final RetryableEvent event = createEvent();

        final GeoLocationRequest r = event.getRequest();
        final String buffer = "buffer";
        final String sender = "sender";
        final ServiceType type = ServiceType.UnwiredLabs;
        final String userData = "userData";
        final int numOfRetry = 101;
        final Date retryOn = new Date(System.currentTimeMillis() + 1000000l);

        r.setBuffer(buffer);
        r.setSender(sender);
        r.setType(type);
        r.setUserData(userData);
        r.setStatus(RequestStatus.error);

        RetryableEvent e = new RetryableEvent();
        e.setNumberOfRetry(numOfRetry);
        e.setRetryOn(retryOn);
        e.setRequest(r);

        dao.save(e);

        e = support.getEvent(e.getId());

        //event
        assertEquals(numOfRetry, e.getNumberOfRetry());
        assertTrue(Math.abs(retryOn.getTime() - e.getRetryOn().getTime()) < 1000l);

        //request
        assertEquals(buffer, e.getRequest().getBuffer());
        assertEquals(sender, e.getRequest().getSender());
        assertEquals(type, e.getRequest().getType());
        assertEquals(userData, e.getRequest().getUserData());
        assertEquals(RequestStatus.error, e.getRequest().getStatus());
    }
    @Test
    public void testGetRetryableEventsForProcess() {
        final Date date = new Date(System.currentTimeMillis() - 10000000l);
        final Date corectDate = new Date(date.getTime() - 10000000l);

        //event with success status should be ignored
        final RetryableEvent e1 = createEvent();
        e1.getRequest().setStatus(RequestStatus.success);
        e1.setRetryOn(corectDate);
        dao.save(e1);

        //event with error status should be ignored.
        final RetryableEvent e2 = createEvent();
        e2.getRequest().setStatus(RequestStatus.error);
        e2.setRetryOn(corectDate);
        dao.save(e2);

        //retry on is after current date
        final RetryableEvent e3 = createEvent();
        e3.setRetryOn(new Date(date.getTime() + 10000000l));
        dao.save(e3);

        final RetryableEvent e4 = createEvent();
        e4.getRequest().setStatus(null);
        e4.setRetryOn(corectDate);
        dao.save(e4);

        final Set<ServiceType> types = new HashSet<>();
        assertEquals(0, dao.getRetryableEventsForProcess(date, types).size());

        types.add(ServiceType.UnwiredLabs);
        final List<RetryableEvent> events = dao.getRetryableEventsForProcess(date, types);
        assertEquals(1, events.size());
        assertEquals(e4.getId(), events.get(0).getId());
    }
    @Test
    public void testSaveNewRequest() {
        final GeoLocationRequest r = new GeoLocationRequest();
        final String buffer = "buffer";
        final String sender = "sender";
        final ServiceType type = ServiceType.UnwiredLabs;
        final String userData = "userData";

        r.setBuffer(buffer);
        r.setSender(sender);
        r.setType(type);
        r.setUserData(userData);

        final Long id = dao.saveNewRequest(r);

        final RetryableEvent e = support.getEvent(id);

        //request
        assertEquals(buffer, e.getRequest().getBuffer());
        assertEquals(sender, e.getRequest().getSender());
        assertEquals(type, e.getRequest().getType());
        assertEquals(userData, e.getRequest().getUserData());
        assertNull(e.getRequest().getStatus());
    }
    @Test
    public void testGetProcessedRequests() {
        final String requestor = "req12345estor";

        final RetryableEvent e1 = createSuccessEvent(requestor);

        //event with error status should be ignored.
        final RetryableEvent e2 = createEvent();
        e2.getRequest().setStatus(RequestStatus.error);
        e2.getRequest().setSender(requestor);
        dao.save(e2);

        //in not final status
        final RetryableEvent e3 = createEvent();
        e3.getRequest().setSender(requestor);
        dao.save(e3);

        //left requestor
        final RetryableEvent e4 = createEvent();
        e4.getRequest().setSender("abcdef");
        dao.save(e4);

        final List<RetryableEvent> events = dao.getProcessedRequests(requestor, 1000, ServiceType.UnwiredLabs);
        assertEquals(2, events.size());

        final Set<Long> ids = getIds(events);
        assertTrue(ids.contains(e1.getId()));
        assertTrue(ids.contains(e2.getId()));
    }
    @Test
    public void testGetProcessedRequestsFilterByType() {
        final String requestor = "req12345estor";

        //event with success status should be ignored
        final RetryableEvent e1 = createEvent();
        e1.getRequest().setStatus(RequestStatus.success);
        e1.getRequest().setType(ServiceType.UnwiredLabs);
        e1.getRequest().setSender(requestor);
        dao.save(e1);

        assertEquals(1, dao.getProcessedRequests(requestor, 1000, ServiceType.UnwiredLabs).size());
        assertEquals(0, dao.getProcessedRequests(requestor, 1000, ServiceType.TestType).size());
    }
    @Test
    public void testGetProcessedRequestsLimit() {
        final String requestor = "req12345estor";

        //event with success status should be ignored
        createSuccessEvent(requestor);
        createSuccessEvent(requestor);

        assertEquals(2, dao.getProcessedRequests(requestor, 1000, ServiceType.UnwiredLabs).size());
        assertEquals(1, dao.getProcessedRequests(requestor, 1, ServiceType.UnwiredLabs).size());
    }

    /**
     * @param requestor
     */
    private RetryableEvent createSuccessEvent(final String requestor) {
        final RetryableEvent e = createEvent();
        e.getRequest().setStatus(RequestStatus.success);
        e.getRequest().setSender(requestor);
        dao.save(e);
        return e;
    }

    /**
     * @param events
     * @return
     */
    private Set<Long> getIds(final List<RetryableEvent> events) {
        final Set<Long> ids = new HashSet<>();
        for (final RetryableEvent e : events) {
            ids.add(e.getId());
        }
        return ids;
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
