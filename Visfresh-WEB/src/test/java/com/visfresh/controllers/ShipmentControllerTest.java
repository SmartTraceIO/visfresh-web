/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.constants.ShipmentConstants;
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
import com.visfresh.entities.Device;
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
        final String comments = "Some comments for receiver saved for shipment";

        final Shipment s = createShipment(true);
        s.setCommentsForReceiver(comments);
        s.setId(null);
        final SaveShipmentResponse resp = shipmentClient.saveShipment(s, "NewTemplate.tpl", true);
        assertNotNull(resp.getShipmentId());

        //check new template is saved
        final long id = resp.getTemplateId();
        final ShipmentTemplate tpl = context.getBean(ShipmentTemplateDao.class).findOne(id);

        assertNotNull(tpl);
        assertNotNull(tpl.getName());
        assertEquals(comments, tpl.getCommentsForReceiver());
    }
    @Test
    public void testSaveShipmentOverDefault() throws RestServiceException, IOException {
        final String comments = "Some comments for receiver saved for shipment";

        final Shipment s = createShipment(true);
        s.setStatus(ShipmentStatus.Default);
        getContext().getBean(ShipmentDao.class).save(s);
        final long oldId = s.getId();

        s.setCommentsForReceiver(comments);
        s.setId(null);

        final SaveShipmentResponse resp = shipmentClient.saveShipment(s, null, false);
        assertEquals(oldId, resp.getShipmentId().longValue());

        //check new template is saved
        final Shipment saved = context.getBean(ShipmentDao.class).findOne(oldId);

        assertEquals(comments, saved.getCommentsForReceiver());
    }
    @Test
    public void testSaveShipmentOverInProgress() throws RestServiceException, IOException {
        final String comments = "Some comments for receiver saved for shipment";

        final Shipment s = createShipment(true);
        s.setStatus(ShipmentStatus.InProgress);
        getContext().getBean(ShipmentDao.class).save(s);
        final long oldId = s.getId();

        s.setCommentsForReceiver(comments);
        s.setId(null);

        final SaveShipmentResponse resp = shipmentClient.saveShipment(s, null, false);
        assertNotSame(oldId, resp.getShipmentId().longValue());

        //check old shipment closed
        final Shipment old = context.getBean(ShipmentDao.class).findOne(oldId);
        assertEquals(ShipmentStatus.Ended, old.getStatus());
    }
    @Test
    public void testSaveShipmentOverDefaultWithExpiredEvent() throws RestServiceException, IOException {
        final String comments = "Some comments for receiver saved for shipment";

        final Shipment s = createShipment(true);
        s.setStatus(ShipmentStatus.Default);
        getContext().getBean(ShipmentDao.class).save(s);
        final long oldId = s.getId();

        //create tracker event
        final TrackerEvent event = new TrackerEvent();
        event.setDevice(s.getDevice());
        event.setShipment(s);
        event.setType(TrackerEventType.AUT);
        event.setTime(new Date(System.currentTimeMillis() - 3 * 60 * 60 * 1000l));
        getContext().getBean(TrackerEventDao.class).save(event);

        s.setCommentsForReceiver(comments);
        s.setId(null);

        final SaveShipmentResponse resp = shipmentClient.saveShipment(s, null, false);
        assertNotSame(oldId, resp.getShipmentId().longValue());

        //check old shipment closed
        final Shipment old = context.getBean(ShipmentDao.class).findOne(oldId);
        assertEquals(ShipmentStatus.Ended, old.getStatus());
    }
    @Test
    public void testGetShipments() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        s.getShippedTo().setAddress("Coles Perth DC");
        final Shipment s2 = createShipment(true);
        s2.getShippedTo().setAddress("Coles Perth DC");
        s2.setStatus(ShipmentStatus.Arrived);
        s2.setArrivalDate(new Date(System.currentTimeMillis() - 1000000));

        shipmentDao.save(s);
        shipmentDao.save(s2);

        //create last reading
        createEvent(s, TrackerEventType.AUT);

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
    public void testGetShipmentsSorted() throws RestServiceException, IOException {
        final Shipment s1 = createShipment(true);
        final Shipment s2 = createShipment(true);
        final Shipment s3 = createShipment(true);

        //CreationDate ascent
        List<Long> ids = getSortedShipmentId(ShipmentConstants.SHIPMENT_ID, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s3.getId(), ids.get(2));

        //CreationDate descent
        ids = getSortedShipmentId(ShipmentConstants.SHIPMENT_ID, false);
        assertEquals(s3.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));

        //Date Shipped (Oldest at top) not working
        final long currentTime = System.currentTimeMillis();
        s2.setShipmentDate(new Date(currentTime - 3 * 10000000L));
        s3.setShipmentDate(new Date(currentTime - 2 * 10000000L));
        s1.setShipmentDate(new Date(currentTime - 1 * 10000000L));

        shipmentDao.save(s1);
        shipmentDao.save(s2);
        shipmentDao.save(s3);

        ids = getSortedShipmentId(ShipmentConstants.SHIPMENT_DATE, true);
        assertEquals(s2.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.SHIPMENT_DATE, false);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s2.getId(), ids.get(2));

        //Date of Arrival (Oldest at top)
        s1.setArrivalDate(new Date(currentTime - 3 * 10000000L));
        s3.setArrivalDate(new Date(currentTime - 2 * 10000000L));
        s2.setArrivalDate(new Date(currentTime - 1 * 10000000L));

        shipmentDao.save(s1);
        shipmentDao.save(s2);
        shipmentDao.save(s3);

        ids = getSortedShipmentId(ShipmentConstants.ARRIVAL_DATE, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s2.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.ARRIVAL_DATE, false);
        assertEquals(s2.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));

        //Status not working
        s2.setStatus(ShipmentStatus.Ended);
        s3.setStatus(ShipmentStatus.Default);
        s1.setStatus(ShipmentStatus.Arrived);

        shipmentDao.save(s1);
        shipmentDao.save(s2);
        shipmentDao.save(s3);

        ids = getSortedShipmentId(ShipmentConstants.STATUS, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s2.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.STATUS, false);
        assertEquals(s2.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));

        //ShippedFrom not working (using location name)
        final LocationProfile la = createLocationProfile("A");
        final LocationProfile lb = createLocationProfile("B");
        final LocationProfile lc = createLocationProfile("C");

        s2.setShippedFrom(lc);
        s3.setShippedFrom(lb);
        s1.setShippedFrom(la);

        shipmentDao.save(s1);
        shipmentDao.save(s2);
        shipmentDao.save(s3);

        ids = getSortedShipmentId(ShipmentConstants.SHIPPED_FROM, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s2.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.SHIPPED_FROM, false);
        assertEquals(s2.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));

        //ShippedTo not working (using location name)
        s2.setShippedTo(lc);
        s3.setShippedTo(lb);
        s1.setShippedTo(la);

        shipmentDao.save(s1);
        shipmentDao.save(s2);
        shipmentDao.save(s3);

        ids = getSortedShipmentId(ShipmentConstants.SHIPPED_TO, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s2.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.SHIPPED_TO, false);
        assertEquals(s2.getId(), ids.get(0));
        assertEquals(s3.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));
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
    public void testGetSingleShipmentBySnTrip() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        final Device device = createDevice("1923087980000117", true);
        s.setDevice(device);
        saveShipmentDirectly(s);

        //add tracker event.
        createEvent(s, TrackerEventType.AUT);
        createEvent(s, TrackerEventType.AUT);

        //add alert
        createAlert(s, AlertType.Battery);
        createTemperatureAlert(s, AlertType.Hot);
        createArrival(s);

        final JsonObject sd = shipmentClient.getSingleShipment(
                "11", s.getTripCount()).getAsJsonObject();
        assertNotNull(sd);

        //check null params
        try {
            shipmentClient.getSingleShipment(null, s.getTripCount()).getAsJsonObject();
            throw new AssertionFailedError("Exception has trown associating of incorect parameters");
        } catch (final Exception e) {
            //ok
        }
    }
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
        req.setStatus(ShipmentStatus.Ended);
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
    }
    @Test
    public void testSaveEmpty() throws RestServiceException, IOException {
        final Shipment shipment = new Shipment();
        shipment.setDevice(createDevice("123987230987", true));
        final Long id = shipmentClient.saveShipment(shipment, null, false).getShipmentId();
        assertNotNull(id);

        final Shipment s = shipmentDao.findOne(id);
        assertNull(s.getShutdownDeviceAfterMinutes());
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
    /**
     * @param name location name.
     * @return location profile.
     */
    private LocationProfile createLocationProfile(final String name) {
        final LocationProfile loc = createLocationProfile(false);
        loc.setName(name);
        loc.setAddress("");
        return getContext().getBean(LocationProfileDao.class).save(loc);
    }
    /**
     * @param column
     * @param sortOrder
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    private List<Long> getSortedShipmentId(final String column, final boolean sortOrder) throws RestServiceException, IOException {
        final JsonArray items = shipmentClient.getShipmentsSorted(column, sortOrder);
        final List<Long> ids = new LinkedList<>();
        for (final JsonElement e : items) {
            final Long id = e.getAsJsonObject().get("shipmentId").getAsLong();
            ids.add(id);
        }
        return ids;
    }

    @After
    public void tearDown() {
        shipmentClient.removeRestIoListener(l);
    }
}
