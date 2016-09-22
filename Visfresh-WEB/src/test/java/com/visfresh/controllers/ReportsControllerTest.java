/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.RestClient;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.EmailShipmentReportRequest;
import com.visfresh.io.email.EmailMessage;
import com.visfresh.io.json.ReportsSerializer;
import com.visfresh.mock.MockEmailService;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.shipment.ShipmentReportBean;
import com.visfresh.rules.AbstractRuleEngine;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReportsControllerTest extends AbstractRestServiceTest {
    private RestClient client;
    private TrackerEventDao trackerEventDao;
    private AlertDao alertDao;
    private ArrivalDao arrivalDao;
    private ReportsSerializer serializer;

    /**
     * Default constructor.
     */
    public ReportsControllerTest() {
        super();
    }
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        context.getBean(MockEmailService.class).clear();

        serializer = new ReportsSerializer();

        trackerEventDao = context.getBean(TrackerEventDao.class);
        alertDao = context.getBean(AlertDao.class);
        arrivalDao = context.getBean(ArrivalDao.class);

        client = new RestClient();
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());

        context.getBean(ReportsController.class).setReportBuilder(new PdfReportBuilder() {
            @Override
            public void createShipmentReport(final ShipmentReportBean bean, final User user,
                    final OutputStream out) throws IOException {
                out.write("response pdf".getBytes());
            }

            @Override
            public void createPerformanceReport(final PerformanceReportBean bean, final User user,
                    final OutputStream out) throws IOException {
                out.write("response pdf".getBytes());
            }
        });
    }

    @Test
    public void testGetPerformanceReport() throws IOException, RestServiceException {
        createShipment();
        final Map<String, String> params = new HashMap<>();

        final String result = client.doSendGetRequest(client.getPathWithToken(
                ReportsController.GET_PERFORMANCE_REPORT), params);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testGetShipmentReportById() throws IOException, RestServiceException {
        final Shipment s = createShipment();

        final Map<String, String> params = new HashMap<>();
        params.put("shipmentId", s.getId().toString());

        final String result = client.doSendGetRequest(client.getPathWithToken(
                ReportsController.GET_SHIPMENT_REPORT), params);
        assertTrue(result.length() > 0);
    }
    @Test
    public void testGetShipmentReportBySnTripCount() throws IOException, RestServiceException {
        final Shipment s = createShipment();

        final Map<String, String> params = new HashMap<>();
        params.put("sn", s.getDevice().getSn());
        params.put("trip", Integer.toString(s.getTripCount()));

        final String result = client.doSendGetRequest(client.getPathWithToken(
                ReportsController.GET_SHIPMENT_REPORT), params);
        assertTrue(result.length() > 0);
    }
    @Test
    public void testEmailShipment() throws IOException, RestServiceException {
        final Shipment s = createShipment();

        final EmailShipmentReportRequest req = new EmailShipmentReportRequest();
        req.setSn(s.getDevice().getSn());
        req.setTrip(s.getTripCount());
        final String subject = "Shipment report from JUnit test";
        req.setSubject(subject);
        final String body = "Given report is sent from JUnit test";
        req.setMessageBody(body);
        req.getUsers().add(createUser1().getId());
        req.getEmails().add("junit@smarttrace.com.au");

        final String result = client.doSendPostRequest(client.getPathWithToken("emailShipmentReport"),
                serializer.toJson(req));
        assertTrue(result.length() > 0);

        //check email
        final MockEmailService emailer = context.getBean(MockEmailService.class);

        assertEquals(1, emailer.getAttachments().size());

        //delete attachment
        for (final File f : emailer.getAttachments().get(0)) {
            f.delete();
        }

        assertEquals(1, emailer.getMessages().size());

        //check message
        final EmailMessage m = emailer.getMessages().get(0);
        assertEquals(subject, m.getSubject());
        assertEquals(body, m.getMessage());
    }
    /**
     * @return
     */
    private Shipment createShipment() {
        final Shipment s = createShipment(true);

        //mark any rules as processed
        final ShipmentSession session = new ShipmentSession(s.getId());
        int count = 0;
        for(final TemperatureRule rule: s.getAlertProfile().getAlertRules()) {
            if (count > 2) {
                break;
            }
            AbstractRuleEngine.setProcessedTemperatureRule(session, rule);
            count++;
        }
        context.getBean(ShipmentSessionDao.class).saveSession(s, session);

        //add tracker event.
        final List<TrackerEvent> events = new LinkedList<>();
        final long dt = 100000l;
        final long startTime = 1000 * dt;

        for (int i = 0; i < 20; i++) {
            events.add(createEvent(s, TrackerEventType.AUT, startTime + dt * i));
        }

        //add alert
        createAlert(s, AlertType.Battery, events.get(18));
        createTemperatureAlert(s, AlertType.Hot, events.get(10));
        createTemperatureAlert(s, AlertType.Hot, events.get(11));
        createTemperatureAlert(s, AlertType.Hot, events.get(12));
        createTemperatureAlert(s, AlertType.Hot, events.get(13));
        createTemperatureAlert(s, AlertType.Hot, events.get(14));
        createArrival(s, events.get(events.size() - 1));

        return s;
    }
    /**
     * @param s shipment.
     * @return
     */
    private Arrival createArrival(final Shipment s, final TrackerEvent e) {
        final Arrival arrival = new Arrival();
        arrival.setDevice(s.getDevice());
        arrival.setShipment(s);
        arrival.setNumberOfMettersOfArrival(400);
        arrival.setTrackerEventId(e.getId());
        arrival.setDate(e.getTime());
        arrivalDao.save(arrival);
        return arrival;
    }
    /**
     * @param s
     * @param type
     * @return
     */
    private TemperatureAlert createTemperatureAlert(final Shipment s, final AlertType type,
            final TrackerEvent e) {
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setTrackerEventId(e.getId());
        alert.setDate(e.getTime());
        alert.setType(type);
        alert.setTemperature(5);
        alert.setMinutes(55);
        alert.setDevice(s.getDevice());
        alert.setShipment(s);
        alertDao.save(alert);
        return alert;
    }
    /**
     * @param s shipment
     * @param type alert type.
     * @return alert.
     */
    private Alert createAlert(final Shipment s, final AlertType type, final TrackerEvent e) {
        final Alert alert = new Alert();
        alert.setShipment(s);
        alert.setTrackerEventId(e.getId());
        alert.setDate(e.getTime());
        alert.setDevice(s.getDevice());
        alert.setType(type);
        alertDao.save(alert);
        return alert;
    }
    /**
     * @param shipment shipment.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Shipment shipment, final TrackerEventType type, final long time) {
        final TrackerEvent e = new TrackerEvent();
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setBattery(1234);
        e.setTemperature(56);
        e.setTime(new Date(time));
        e.setType(type);
        e.setLatitude(50.50);
        e.setLongitude(51.51);

        trackerEventDao.save(e);
        return e;
    }
    /**
     *
     */
    @After
    public void tearDown() {
        context.getBean(MockEmailService.class).clear();
    }
}
