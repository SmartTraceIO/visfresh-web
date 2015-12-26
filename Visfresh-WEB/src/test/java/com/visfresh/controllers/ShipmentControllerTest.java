/**
 *
 */
package com.visfresh.controllers;

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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.controllers.restclient.RestIoListener;
import com.visfresh.controllers.restclient.ShipmentRestClient;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.UserResolver;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentControllerTest extends AbstractRestServiceTest {
    private ShipmentDao shipmentDao;
    private AlertDao alertDao;
    private ArrivalDao arrivalDao;
    private TrackerEventDao trackerEventDao;
    private ShipmentRestClient shipmentClient;
    private JsonObject currentJsonResponse;
    //response catcher
    final RestIoListener l = new RestIoListener() {
        @Override
        public void sendingRequest(final String url, final String body, final String methodName) {
        }
        @Override
        public void receivedResponse(final String responseBody) {
            currentJsonResponse = new JsonParser().parse(responseBody).getAsJsonObject();
        }
    };

    /**
     * Default constructor.
     */
    public ShipmentControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        shipmentDao = context.getBean(ShipmentDao.class);
        alertDao = context.getBean(AlertDao.class);
        arrivalDao = context.getBean(ArrivalDao.class);
        trackerEventDao = context.getBean(TrackerEventDao.class);

        final String token = login();
        final User user = context.getBean(AuthService.class).getUserForToken(token);
        shipmentClient = new ShipmentRestClient(user);

        shipmentClient.setServiceUrl(getServiceUrl());
        shipmentClient.setReferenceResolver(context.getBean(ReferenceResolver.class));
        shipmentClient.setUserResolver(context.getBean(UserResolver.class));
        shipmentClient.setAuthToken(token);
        shipmentClient.addRestIoListener(l);

        currentJsonResponse = null;
    }

    /**
     * The bug found where the count of item returned from server is not equals of real
     * number of items
     * @throws RestServiceException
     * @throws IOException
     */
    @Test
    public void testItemCount() throws RestServiceException, IOException {
        createShipmentTemplate(true);
        createShipmentTemplate(true);
        createShipment(true);

        assertEquals(1, shipmentClient.getShipments(1, 10000).size());
        assertEquals(1, currentJsonResponse.get("totalCount").getAsInt());
    }
    //@RequestMapping(value = "/saveShipment/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveShipment(@PathVariable final String authToken,
    //        final @RequestBody String shipment) {
    @Test
    public void testSaveShipment() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        s.setId(null);
        final SaveShipmentResponse resp = shipmentClient.saveShipment(s, "NewTemplate.tpl", true);
        assertNotNull(resp.getShipmentId());

        //check new template is saved
        final long id = resp.getTemplateId();
        final ShipmentTemplate tpl = context.getBean(ShipmentTemplateDao.class).findOne(id);

        assertNotNull(tpl);
        assertNotNull(tpl.getName());
    }
    @Test
    public void testGetShipments() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        s.getShippedTo().setAddress("Coles Perth DC");
        final Shipment s2 = createShipment(true);
        s2.getShippedTo().setAddress("Coles Perth DC");

        //add alert
        createAlert(s, AlertType.Battery);
        createAlert(s, AlertType.Battery);
        createAlert(s, AlertType.MovementStart);
        createAlert(s, AlertType.MovementStart);
        createAlert(s, AlertType.MovementStart);
        createAlert(s, AlertType.MovementStart);
//        createAlert(s, d, AlertType.MovementStop);
        createAlert(s, AlertType.LightOff);
        createAlert(s, AlertType.LightOn);

        createTemperatureAlert(s, AlertType.Hot);
        createTemperatureAlert(s, AlertType.Hot);
        createTemperatureAlert(s, AlertType.Cold);
        createTemperatureAlert(s, AlertType.CriticalCold);
        createTemperatureAlert(s, AlertType.CriticalCold);
        createTemperatureAlert(s, AlertType.CriticalCold);
        createTemperatureAlert(s, AlertType.CriticalHot);
        createArrival(s);

        assertEquals(2, shipmentClient.getShipments(null, null).size());
        assertEquals(1, shipmentClient.getShipments(1, 1).size());
        assertEquals(1, shipmentClient.getShipments(2, 1).size());
        assertEquals(0, shipmentClient.getShipments(3, 10000).size());
    }
    @Test
    public void testGetShipment() throws IOException, RestServiceException {
        final Shipment sp = createShipment(true);
        assertNotNull(shipmentClient.getShipment(sp.getId()));
    }
    @Test
    public void testDeleteShipment() throws IOException, RestServiceException {
        final Shipment sp = createShipment(true);
        shipmentClient.deleteShipment(sp.getId());
        assertNull(shipmentDao.findOne(sp.getId()));
    }
    //@RequestMapping(value = "/getShipmentData/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipmentData(@PathVariable final String authToken,
    //        @RequestParam final String fromDate,
    //        @RequestParam final String toDate,
    //        @RequestParam final String onlyWithAlerts
    @Test
    public void testGetSingleShipment() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);

        //add tracker event.
        createEvent(s, TrackerEventType.AUT);
        createEvent(s, TrackerEventType.AUT);

        //add alert
        createAlert(s, AlertType.Battery);
        createTemperatureAlert(s, AlertType.Hot);
        createArrival(s);

        final JsonObject sd = shipmentClient.getSingleShipment(s).getAsJsonObject();
        assertNotNull(sd);
    }
    @Test
    public void testGetTestSingleShipment() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);

        //correct location
        final LocationProfile shippedTo = s.getShippedTo();
        final Location loc = shippedTo.getLocation();
        loc.setLatitude(loc.getLatitude() + 10);
        loc.setLongitude(loc.getLongitude() + 10);
        context.getBean(LocationProfileDao.class).save(shippedTo);

        s.setShipmentDescription("JUnit test shipment");
        context.getBean(ShipmentDao.class).save(s);

        final JsonObject sd = shipmentClient.getSingleShipment(s).getAsJsonObject();
        assertNotNull(sd);
    }
    //@RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipments(@PathVariable final String authToken) {
    @Test
    public void testGetShipmentsFiltered() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);

        GetFilteredShipmentsRequest req = createFilter(s);
        assertEquals(1, shipmentClient.getShipments(req).size());

        //shipped from not matches
        req = createFilter(s);
        req.getShippedFrom().clear();
        req.getShippedFrom().add(-77L);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //shipped to not matches
        req = createFilter(s);
        req.getShippedTo().clear();
        req.getShippedTo().add(-77L);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //goods not matchers
        req = createFilter(s);
        req.setShipmentDescription("abrakadabra");
        assertEquals(0, shipmentClient.getShipments(req).size());

        //device not matches
        req = createFilter(s);
        req.setDeviceImei("0000000");
        assertEquals(0, shipmentClient.getShipments(req).size());

        //shipment status not matches
        req = createFilter(s);
        req.setStatus(ShipmentStatus.Complete);
        assertEquals(0, shipmentClient.getShipments(req).size());
    }

    @Test
    public void testFilteredByDateRanges() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);

        GetFilteredShipmentsRequest req = createFilter(s);
        assertEquals(1, shipmentClient.getShipments(req).size());

        //check default time ranges (2 weeks)
        final long oneDay = 24 * 60 * 60 * 1000L;
        s.setShipmentDate(new Date(System.currentTimeMillis() - 15 * oneDay));
        s.setLastEventDate(new Date(s.getShipmentDate().getTime() + 2 * 60 * 60 * 1000L));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //check one week date ranges
        s.setShipmentDate(new Date());
        s.setLastEventDate(new Date());
        saveShipmentDirectly(s);

        req = createFilter(s);
        req.setLastWeek(true);
        assertEquals(1, shipmentClient.getShipments(req).size());

        s.setShipmentDate(new Date(System.currentTimeMillis() - 8 * oneDay));
        s.setLastEventDate(new Date(s.getShipmentDate().getTime() + 2 * 60 * 60 * 1000L));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //check last day
        s.setShipmentDate(new Date(System.currentTimeMillis() - 3 * 60 * 60 * 1000L));
        s.setLastEventDate(new Date(s.getShipmentDate().getTime() + 2 * 60 * 60 * 1000L));
        saveShipmentDirectly(s);

        req = createFilter(s);
        req.setLastDay(true);
        assertEquals(1, shipmentClient.getShipments(req).size());

        s.setShipmentDate(new Date(System.currentTimeMillis() - 2 * oneDay));
        s.setLastEventDate(new Date(s.getShipmentDate().getTime() + 2 * 60 * 60 * 1000L));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //check last 2 days
        s.setShipmentDate(new Date());
        saveShipmentDirectly(s);

        req = createFilter(s);
        req.setLast2Days(true);
        assertEquals(1, shipmentClient.getShipments(req).size());

        s.setShipmentDate(new Date(System.currentTimeMillis() - 3 * oneDay));
        s.setLastEventDate(new Date(s.getShipmentDate().getTime() + 2 * 60 * 60 * 1000L));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //check last one month
        s.setShipmentDate(new Date(System.currentTimeMillis() - 3 * 60 * 60 * 1000L));
        s.setLastEventDate(new Date(s.getShipmentDate().getTime() + 2 * 60 * 60 * 1000L));
        saveShipmentDirectly(s);

        req = createFilter(s);
        req.setLastMonth(true);
        assertEquals(1, shipmentClient.getShipments(req).size());

        s.setShipmentDate(new Date(System.currentTimeMillis() - 32 * oneDay));
        s.setLastEventDate(new Date(s.getShipmentDate().getTime() + 2 * 60 * 60 * 1000L));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());
    }
    @Test
    public void testFilteredByOnlyWithAlerts() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);

        final GetFilteredShipmentsRequest req = createFilter(s);
        assertEquals(1, shipmentClient.getShipments(req).size());

        req.setAlertsOnly(true);
        assertEquals(0, shipmentClient.getShipments(req).size());

        final long oneDay = 24 * 60 * 60 * 1000L;

        //create alert
        final Alert alert = createTemperatureAlert(s, AlertType.Hot);
        alert.setDate(new Date(System.currentTimeMillis() - oneDay));
        alertDao.save(alert);

        assertEquals(1, shipmentClient.getShipments(req).size());

        //move alert to out of date ranges
        alert.setDate(new Date(System.currentTimeMillis() - 18 * oneDay));
        alertDao.save(alert);

        assertEquals(0, shipmentClient.getShipments(req).size());
    }
    @Test
    public void testSaveEmpty() throws RestServiceException, IOException {
        final Shipment shipment = new Shipment();
        shipment.setDevice(createDevice("123987230987", true));
        final Long id = shipmentClient.saveShipment(shipment, null, false).getShipmentId();
        assertNotNull(id);

        final Shipment s = shipmentDao.findOne(id);
        assertNull(s.getShutdownDeviceTimeOut());
        assertNull(s.getArrivalNotificationWithinKm());
    }
    /**
     * @param s
     * @return
     */
    protected GetFilteredShipmentsRequest createFilter(final Shipment s) {
        final GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();
        req.setPageIndex(1);
        req.setPageSize(10000);
        req.setShipmentDescription(s.getShipmentDescription());
        req.setStatus(ShipmentStatus.InProgress);

        List<Long> ids = new LinkedList<Long>();
        ids.add(s.getShippedFrom().getId());
        ids.add(-1l);
        ids.add(-2l);
        ids.add(-3l);
        ids.add(-4l);
        req.setShippedFrom(ids);

        ids = new LinkedList<Long>();
        ids.add(s.getShippedTo().getId());
        ids.add(-1l);
        ids.add(-2l);
        ids.add(-3l);
        ids.add(-4l);
        req.setShippedTo(ids);

        req.setDeviceImei(s.getDevice().getId());
        return req;
    }
    /**
     * @param s
     * @param type
     * @return
     */
    private TemperatureAlert createTemperatureAlert(final Shipment s, final AlertType type) {
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(new Date());
        alert.setType(type);
        alert.setTemperature(5);
        alert.setMinutes(55);
        alert.setDevice(s.getDevice());
        alert.setShipment(s);
        alertDao.save(alert);
        return alert;
    }
    /**
     * @param shipment shipment.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Shipment shipment, final TrackerEventType type) {
        final TrackerEvent e = new TrackerEvent();
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setBattery(1234);
        e.setTemperature(56);
        e.setTime(new Date());
        e.setType(type);
        e.setLatitude(50.50);
        e.setLongitude(51.51);

        trackerEventDao.save(e);
        return e;
    }
    /**
     * @param s shipment.
     * @return
     */
    private Arrival createArrival(final Shipment s) {
        final Arrival arrival = new Arrival();
        arrival.setDevice(s.getDevice());
        arrival.setShipment(s);
        arrival.setNumberOfMettersOfArrival(400);
        arrival.setDate(new Date(System.currentTimeMillis() - 50000));
        arrivalDao.save(arrival);
        return arrival;
    }
    /**
     * @param s shipment
     * @param type alert type.
     * @return alert.
     */
    private Alert createAlert(final Shipment s, final AlertType type) {
        final Alert alert = new Alert();
        alert.setShipment(s);
        alert.setDate(new Date(System.currentTimeMillis() - 100000000l));
        alert.setDevice(s.getDevice());
        alert.setType(type);
        alertDao.save(alert);
        return alert;
    }

    @After
    public void tearDown() {
        shipmentClient.removeRestIoListener(l);
    }
}
