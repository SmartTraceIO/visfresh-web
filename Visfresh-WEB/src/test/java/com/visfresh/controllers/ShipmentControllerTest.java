/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
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
import com.visfresh.constants.LocationConstants;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.controllers.restclient.RestIoListener;
import com.visfresh.controllers.restclient.ShipmentRestClient;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.DeviceGroupDao;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NoteDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Note;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.KeyLocation;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentDto;
import com.visfresh.io.json.ShipmentSerializer;
import com.visfresh.rules.AbstractRuleEngine;
import com.visfresh.rules.EtaCalculationRule;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;
import com.visfresh.services.RuleEngine;

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
    final RestIoListener restIoListener = new RestIoListener() {
        @Override
        public void sendingRequest(final String url, final String body, final String methodName) {
        }
        @Override
        public void receivedResponse(final String responseBody) {
            currentJsonResponse = new JsonParser().parse(responseBody).getAsJsonObject();
        }
    };
    private User user;

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
        this.user = context.getBean(AuthService.class).getUserForToken(token);
        shipmentClient = new ShipmentRestClient(user);

        shipmentClient.setServiceUrl(getServiceUrl());
        shipmentClient.setAuthToken(token);
        shipmentClient.addRestIoListener(restIoListener);

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

        final Shipment shp = createShipment(true);
        final ShipmentDto s = new ShipmentDto(shp);
        s.setCommentsForReceiver(comments);

        //add interim locations
        final List<Long> locs = new LinkedList<Long>();
        locs.add(createLocationProfile(true).getId());
        locs.add(createLocationProfile(true).getId());
        s.setInterimLocations(locs);

        s.setId(null);

        final SaveShipmentResponse resp = shipmentClient.saveShipment(s, "NewTemplate.tpl", true);
        assertNotNull(resp.getShipmentId());
        shp.setId(resp.getShipmentId());

        //check new template is saved
        final long id = resp.getTemplateId();
        final ShipmentTemplate tpl = context.getBean(ShipmentTemplateDao.class).findOne(id);

        assertNotNull(tpl);
        assertNotNull(tpl.getName());
        assertEquals(comments, tpl.getCommentsForReceiver());
        assertEquals(2, context.getBean(AlternativeLocationsDao.class).getBy(shp).getInterim().size());
    }
    @Test
    public void testSaveShipmentAddDateShipped() throws RestServiceException, IOException {
        final ShipmentDto s = new ShipmentDto(createShipment(true));
        s.setId(null);
        final SaveShipmentResponse resp = shipmentClient.saveShipment(s, null, false);
        assertNotNull(resp.getShipmentId());

        //check new template is saved
        final long id = resp.getShipmentId();
        final Shipment shipment = context.getBean(ShipmentDao.class).findOne(id);

        assertNotEquals(s.getShipmentDescription(), shipment.getShipmentDescription());
    }
    @Test
    public void testCreatedBy() throws RestServiceException, IOException {
        final ShipmentDto s = new ShipmentDto(createShipment(true));
        s.setId(null);
        final SaveShipmentResponse resp = shipmentClient.saveShipment(s, null, false);
        assertNotNull(resp.getShipmentId());

        //check new template is saved
        final long id = resp.getShipmentId();
        final Shipment shipment = context.getBean(ShipmentDao.class).findOne(id);

        assertNotNull(shipment.getCreatedBy());
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

        final SaveShipmentResponse resp = shipmentClient.saveShipment(new ShipmentDto(s), null, false);
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

        final SaveShipmentResponse resp = shipmentClient.saveShipment(new ShipmentDto(s), null, false);
        assertNotSame(oldId, resp.getShipmentId().longValue());

        //check old shipment closed
        final Shipment old = context.getBean(ShipmentDao.class).findOne(oldId);
        assertEquals(ShipmentStatus.Ended, old.getStatus());
    }
    @Test
    public void testSaveInterimLocations() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);

        final LocationProfile l1 = createLocationProfile(true);
        final LocationProfile l2 = createLocationProfile(true);

        final List<Long> locs = new LinkedList<>();
        locs.add(l1.getId());
        locs.add(l2.getId());

        final SaveShipmentRequest req = new SaveShipmentRequest();
        final ShipmentDto dto = new ShipmentDto(s);
        req.setShipment(dto);
        dto.setInterimLocations(locs);

        shipmentClient.saveShipment(req);

        //check alternative locations saved
        final AlternativeLocationsDao alDao = context.getBean(AlternativeLocationsDao.class);
        assertEquals(2, alDao.getBy(s).getInterim().size());

        //check interim locations configured.
        final RuleEngine eng = context.getBean(RuleEngine.class);
        assertEquals(2, eng.getInterimLocations(s).size());

        //check interim locations
        locs.remove(locs.size() - 1);
        shipmentClient.saveShipment(req);

        //check locations
        assertEquals(1, alDao.getBy(s).getInterim().size());
        assertEquals(1, eng.getInterimLocations(s).size());

        //check if interims not present in request, that not then changed
        dto.setInterimLocations(null);
        shipmentClient.saveShipment(req);

        //check locations
        assertEquals(1, alDao.getBy(s).getInterim().size());
        assertEquals(1, eng.getInterimLocations(s).size());
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

        final SaveShipmentResponse resp = shipmentClient.saveShipment(new ShipmentDto(s), null, false);
        assertNotSame(oldId, resp.getShipmentId().longValue());

        //check old shipment closed
        final Shipment old = context.getBean(ShipmentDao.class).findOne(oldId);
        assertEquals(ShipmentStatus.Ended, old.getStatus());
    }
    @Test
    public void testGetShipments() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        s.getShippedTo().setAddress("Coles Perth DC");
        final Date eta = context.getBean(EtaCalculationRule.class).estimateArrivalDate(s,
                s.getShippedFrom().getLocation(),
                s.getShipmentDate(),
                new Date(s.getShipmentDate().getTime() + 100000l));
        s.setEta(eta);

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
    public void testGetShipmentsWithoutTrackerEvents() throws RestServiceException, IOException {
        createShipment(true);
        createShipment(true);

        assertEquals(2, shipmentClient.getShipments(null, null).size());
    }
    @Test
    public void testGetShipmentsFilterLightOnOffAlertSummary() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        s.getShippedTo().setAddress("Coles Perth DC");
        final Date eta = context.getBean(EtaCalculationRule.class).estimateArrivalDate(s,
                s.getShippedFrom().getLocation(),
                s.getShipmentDate(),
                new Date(s.getShipmentDate().getTime() + 100000l));
        s.setEta(eta);

        shipmentDao.save(s);

        //create last reading
        createEvent(s, TrackerEventType.AUT);

        //add alert
        createAlert(s, AlertType.Battery);
        createAlert(s, AlertType.LightOff);
        createAlert(s, AlertType.LightOn);

        final JsonObject json = shipmentClient.getShipments(null, null).get(0).getAsJsonObject();
        //check alert summary
        //"alertSummary": {
        //    "MovementStart": "4",
        //    "LightOff": "1",
        //    "Cold": "1",
        //    "CriticalCold": "3",
        //    "LightOn": "1",
        //    "CriticalHot": "1",
        //    "Battery": "2",
        //    "Hot": "2"
        //},
        final JsonObject alertSummary = json.get("alertSummary").getAsJsonObject();
        assertEquals(1, alertSummary.get("Battery").getAsInt());
        assertNull(alertSummary.get("LightOn"));
        assertNull(alertSummary.get("LightOff"));
    }
    @Test
    public void testGetShipmentsKeyLocations() throws RestServiceException, IOException {
        //first shipment.
        final Shipment s1 = createShipment(true);
        s1.getShippedTo().setAddress("Coles Perth DC");
        shipmentDao.save(s1);

        //second shipment.
        final Shipment s2 = createShipment(true);
        s2.getShippedTo().setAddress("Coles Perth DC");
        s2.setStatus(ShipmentStatus.Arrived);
        s2.setArrivalDate(new Date(System.currentTimeMillis() - 1000000));
        shipmentDao.save(s2);

        //correct locations
        final Location l1 = new Location(3., 3.);
        final Location l2 = new Location(5., 5.);

        //correct shipped from and shipped to for second location.
        s2.getShippedFrom().getLocation().setLatitude(l1.getLatitude());
        s2.getShippedFrom().getLocation().setLongitude(l1.getLongitude());

        s2.getShippedTo().getLocation().setLatitude(l2.getLatitude());
        s2.getShippedTo().getLocation().setLongitude(l2.getLongitude());

        context.getBean(LocationProfileDao.class).save(s2.getShippedFrom());
        context.getBean(LocationProfileDao.class).save(s2.getShippedTo());

        long time = System.currentTimeMillis() - 1000000000l;

        final List<TrackerEvent> events = new LinkedList<TrackerEvent>();
        //create events
        for (double x = l1.getLatitude(), y = l1.getLongitude();
                x < l2.getLatitude() + 0.2 && y < l2.getLongitude() + 0.2;
                x += 0.1, y += 0.1) {
            events.add(createEvent(s2, x, y, time += 120 * 1000l));
        }

        //create one interim stop
        final TrackerEvent e = events.get(events.size() / 2);

        //create interim stop location
        final LocationProfile loc = new LocationProfile();
        loc.setAddress("address");
        loc.setCompany(getCompany());
        loc.setInterim(true);
        loc.setName("Unexpected stop");
        loc.setRadius(10);
        context.getBean(LocationProfileDao.class).save(loc);

        //create interim stop
        final InterimStop stp = new InterimStop();
        stp.setDate(e.getTime());
        stp.setLatitude(e.getLatitude());
        stp.setLongitude(e.getLongitude());
        stp.setLocation(loc);
        context.getBean(InterimStopDao.class).add(s2, stp);

        //add one alert
        final Alert alert = new TemperatureAlert();
        alert.setTrackerEventId(e.getId());
        alert.setType(AlertType.Hot);
        alert.setShipment(e.getShipment());
        alert.setDate(e.getTime());
        alert.setDevice(e.getShipment().getDevice());
        alertDao.save(alert);

        assertEquals(2, shipmentClient.getShipments(null, null).size());
    }
    @Test
    public void testKeyLocationsIgnoreLightOff() throws RestServiceException, IOException {
        //second shipment.
        final Shipment s2 = createShipment(true);
        s2.setShippedTo(null);
        s2.setShippedFrom(null);
        shipmentDao.save(s2);

        //correct locations
        final Location l1 = new Location(3., 3.);
        final Location l2 = new Location(5., 5.);

        long time = System.currentTimeMillis() - 1000000000l;

        final List<TrackerEvent> events = new LinkedList<TrackerEvent>();
        //create events
        for (double x = l1.getLatitude(), y = l1.getLongitude();
                x < l2.getLatitude() + 0.2 && y < l2.getLongitude() + 0.2;
                x += 0.1, y += 0.1) {
            events.add(createEvent(s2, x, y, time += 120 * 1000l));
        }

        //create one interim stop
        final TrackerEvent e = events.get(events.size() / 2);

        //add one alert
        final Alert alert = new Alert();
        alert.setTrackerEventId(e.getId());
        alert.setType(AlertType.LightOff);
        alert.setShipment(e.getShipment());
        alert.setDate(e.getTime());
        alert.setDevice(e.getShipment().getDevice());
        alertDao.save(alert);

        final JsonArray response = shipmentClient.getShipments(null, null);
        assertEquals(1, response.size());

        final List<KeyLocation> locs = getKeyLocations(response.get(0).getAsJsonObject());
        final List<KeyLocation> offs = getAlerts(locs, AlertType.LightOff);
        assertEquals(0, offs.size());
    }
    @Test
    public void testKeyLocationsLightOn() throws RestServiceException, IOException {
        //second shipment.
        final Shipment s2 = createShipment(true);
        s2.setShippedTo(null);
        s2.setShippedFrom(null);
        shipmentDao.save(s2);

        //correct locations
        final Location l1 = new Location(3., 3.);
        final Location l2 = new Location(5., 5.);

        long time = System.currentTimeMillis() - 1000000000l;

        final List<TrackerEvent> events = new LinkedList<TrackerEvent>();
        //create events
        for (double x = l1.getLatitude(), y = l1.getLongitude();
                x < l2.getLatitude() + 0.2 && y < l2.getLongitude() + 0.2;
                x += 0.1, y += 0.1) {
            events.add(createEvent(s2, x, y, time += 120 * 1000l));
        }

        //create one interim stop
        final TrackerEvent e = events.get(events.size() / 2);

        //add one alert
        final Alert alert = new Alert();
        alert.setTrackerEventId(e.getId());
        alert.setType(AlertType.LightOn);
        alert.setShipment(e.getShipment());
        alert.setDate(e.getTime());
        alert.setDevice(e.getShipment().getDevice());

        alertDao.save(alert);
        //save again as new alert
        alert.setId(null);
        alertDao.save(alert);

        final JsonArray response = shipmentClient.getShipments(null, null);
        assertEquals(1, response.size());

        final List<KeyLocation> locs = getKeyLocations(response.get(0).getAsJsonObject());
        final List<KeyLocation> offs = getAlerts(locs, AlertType.LightOn);
        assertEquals(1, offs.size());
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
    public void testSortingByEta() throws RestServiceException, IOException {
        final long currentTime = System.currentTimeMillis();

        final Shipment s3 = createShipment(true);
        s3.setEta(new Date(currentTime - 100000l));
        final Shipment s1 = createShipment(true);
        s1.setEta(new Date(currentTime - 3 * 100000l));
        final Shipment s2 = createShipment(true);
        s2.setEta(new Date(currentTime - 2 * 100000l));

        saveShipmentDirectly(s1);
        saveShipmentDirectly(s2);
        saveShipmentDirectly(s3);

        List<Long> ids;

        ids = getSortedShipmentId(ShipmentConstants.ETA, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s3.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.ETA, false);
        assertEquals(s3.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));
    }
    @Test
    public void testSortingBySiblingCount() throws RestServiceException, IOException {
        final Shipment s3 = createShipment(true);
        s3.setSiblingCount(3);
        final Shipment s1 = createShipment(true);
        s1.setSiblingCount(1);
        final Shipment s2 = createShipment(true);
        s2.setSiblingCount(2);

        saveShipmentDirectly(s1);
        saveShipmentDirectly(s2);
        saveShipmentDirectly(s3);

        List<Long> ids;

        ids = getSortedShipmentId(ShipmentConstants.SIBLING_COUNT, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s3.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.SIBLING_COUNT, false);
        assertEquals(s3.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));
    }
    @Test
    public void testSortingByLastReadingDate() throws RestServiceException, IOException {
        final long currentTime = System.currentTimeMillis();

        final Shipment s3 = createShipment(true);
        s3.setLastEventDate(new Date(currentTime - 100000l));
        final Shipment s1 = createShipment(true);
        s1.setLastEventDate(new Date(currentTime - 3 * 100000l));
        final Shipment s2 = createShipment(true);
        s2.setLastEventDate(new Date(currentTime - 2 * 100000l));

        saveShipmentDirectly(s1);
        saveShipmentDirectly(s2);
        saveShipmentDirectly(s3);

        List<Long> ids;

        ids = getSortedShipmentId(ShipmentConstants.LAST_READING_TIME, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s3.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.LAST_READING_TIME_ISO, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s3.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.LAST_READING_TIME, false);
        assertEquals(s3.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.LAST_READING_TIME_ISO, false);
        assertEquals(s3.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));
    }
    @Test
    public void testSortingByLastReadingTemperature() throws RestServiceException, IOException {
        final Shipment s3 = createShipment(true);
        createTrackerEvent(s3, 3.);
        final Shipment s1 = createShipment(true);
        createTrackerEvent(s1, 1.);
        final Shipment s2 = createShipment(true);
        createTrackerEvent(s2, 2.);

        List<Long> ids;

        ids = getSortedShipmentId(ShipmentConstants.LAST_READING_TEMPERATURE, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s3.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.LAST_READING_TEMPERATURE, false);
        assertEquals(s3.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));
    }
    @Test
    public void testSortingByLastAlertSummary() throws RestServiceException, IOException {
        final Shipment s3 = createShipment(true);
        createAlert(s3, AlertType.Cold);
        createAlert(s3, AlertType.Cold);
        createAlert(s3, AlertType.Cold);
        final Shipment s1 = createShipment(true);
        createAlert(s1, AlertType.Cold);
        final Shipment s2 = createShipment(true);
        createAlert(s2, AlertType.Cold);
        createAlert(s2, AlertType.Cold);

        List<Long> ids;

        ids = getSortedShipmentId(ShipmentConstants.ALERT_SUMMARY, true);
        assertEquals(s1.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s3.getId(), ids.get(2));

        ids = getSortedShipmentId(ShipmentConstants.ALERT_SUMMARY, false);
        assertEquals(s3.getId(), ids.get(0));
        assertEquals(s2.getId(), ids.get(1));
        assertEquals(s1.getId(), ids.get(2));
    }

    @Test
    public void testGetShipment() throws IOException, RestServiceException {
        final Shipment sp = createShipment(true);
        sp.setDeviceShutdownTime(new Date(System.currentTimeMillis() - 10000000l));
        saveShipmentDirectly(sp);

        final AlternativeLocations loc = new AlternativeLocations();
        loc.getInterim().add(createLocationProfile(true));
        loc.getInterim().add(createLocationProfile(true));

        final AlternativeLocationsDao altDao = context.getBean(AlternativeLocationsDao.class);
        altDao.save(sp, loc);

        final ShipmentDto dto = shipmentClient.getShipment(sp.getId());
        assertNotNull(dto);
        assertEquals(2, dto.getInterimLocations().size());
    }
    @Test
    public void testDeleteShipment() throws IOException, RestServiceException {
        final Shipment sp = createShipment(true);
        shipmentClient.deleteShipment(sp.getId());
        assertNull(shipmentDao.findOne(sp.getId()));
    }
    @Test
    public void testSuppressAlerts() throws IOException, RestServiceException {
        final Shipment sp = createShipment(true);
        shipmentClient.suppressAlerts(sp.getId());

        final ShipmentSession session = context.getBean(ShipmentSessionDao.class).getSession(sp);
        assertTrue(session.isAlertsSuppressed());
        assertNotNull(session.getAlertsSuppressionDate());
    }
    @Test
    public void testGetSingleShipmentWithSuppressedAlerts() throws IOException, RestServiceException {
        final Shipment sp = createShipment(true);
        context.getBean(RuleEngine.class).suppressNextAlerts(sp);

        //run client only for obtain the HTTP dump
        shipmentClient.getSingleShipment(sp).getAsJsonObject();
    }
    //@RequestMapping(value = "/getShipmentData/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipmentData(@PathVariable final String authToken,
    //        @RequestParam final String fromDate,
    //        @RequestParam final String toDate,
    //        @RequestParam final String onlyWithAlerts
    @Test
    public void testGetSingleShipment() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);

        //mark any rules as processed
        final ShipmentSession session = new ShipmentSession();
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
        createEvent(s, TrackerEventType.AUT);
        createEvent(s, TrackerEventType.AUT);

        //add alert
        createAlert(s, AlertType.Battery);
        createTemperatureAlert(s, AlertType.Hot);
        createArrival(s);

        createNote(s, "Note 1");
        createNote(s, "Note 2");

        final JsonObject sd = shipmentClient.getSingleShipment(s).getAsJsonObject();
        assertNotNull(sd);
    }
    @Test
    public void testGetSingleShipmentAlternatives() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        final LocationProfile l1 = createLocationProfile("L1");
        final LocationProfile l2 = createLocationProfile("L2");
        final LocationProfile l3 = createLocationProfile("L3");
        final LocationProfile l4 = createLocationProfile("L4");
        final LocationProfile l5 = createLocationProfile("L5");
        final LocationProfile l6 = createLocationProfile("L6");

        final AlternativeLocations alts = new AlternativeLocations();
        alts.getFrom().add(l1);
        alts.getFrom().add(l2);

        alts.getTo().add(l3);
        alts.getTo().add(l4);

        alts.getInterim().add(l5);
        alts.getInterim().add(l6);

        context.getBean(AlternativeLocationsDao.class).save(s, alts);

        final JsonObject sd = shipmentClient.getSingleShipment(s).getAsJsonObject();

        JsonArray array;
        array = sd.get("startLocationAlternatives").getAsJsonArray();
        assertEquals(2, array.size());
        assertEquals("L1", array.get(0).getAsJsonObject().get(LocationConstants.PROPERTY_LOCATION_NAME).getAsString());
        array = sd.get("endLocationAlternatives").getAsJsonArray();
        assertEquals(2, array.size());
        assertEquals("L3", array.get(0).getAsJsonObject().get(LocationConstants.PROPERTY_LOCATION_NAME).getAsString());
        array = sd.get("interimLocations").getAsJsonArray();
        assertEquals(2, array.size());
        assertEquals("L5", array.get(0).getAsJsonObject().get(LocationConstants.PROPERTY_LOCATION_NAME).getAsString());

        assertNotNull(sd);
    }
    @Test
    public void testInterimStops() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        createInterimStop(s);
        createInterimStop(s);
        createInterimStop(s);

        final JsonObject sd = shipmentClient.getSingleShipment(s).getAsJsonObject();
        final JsonArray array = sd.get("interimStops").getAsJsonArray();

        assertEquals(3, array.size());
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
    public void testGetSingleShipmentByDeviceGroups() throws RestServiceException, IOException {
        final Device d1 = createDevice("1923087980000117", true);
        final Shipment s1 = createShipment(d1, true);
        s1.getSiblings().add(11l);
        s1.getSiblings().add(12l);
        s1.getSiblings().add(13l);
        shipmentDao.save(s1);

        final Device d2 = createDevice("2304870870987087", true);
        final Shipment s2 = createShipment(d2, true);
        s1.getSiblings().add(11l);
        s1.getSiblings().add(12l);
        s1.getSiblings().add(13l);
        shipmentDao.save(s2);

        //add to device groups
        final DeviceGroupDao dgd = context.getBean(DeviceGroupDao.class);
        final DeviceGroup dg1 = createDeviceGroup("GR1");
        final DeviceGroup dg2 = createDeviceGroup("GR2");
        final DeviceGroup dg3 = createDeviceGroup("GR2");
        final DeviceGroup dg4 = createDeviceGroup("GR3");

        dgd.addDevice(dg1, d1);
        dgd.addDevice(dg2, d1);
        dgd.addDevice(dg3, d2);
        dgd.addDevice(dg4, d2);

        final JsonObject sd = shipmentClient.getSingleShipment(
                "11", s1.getTripCount()).getAsJsonObject();
        assertNotNull(sd);
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

        //create light on alert
        Alert alert = createAlert(s, AlertType.LightOn);
        alert.setDate(new Date(System.currentTimeMillis() - oneDay));
        alertDao.save(alert);

        alert = createAlert(s, AlertType.LightOff);
        alert.setDate(new Date(System.currentTimeMillis() - oneDay));
        alertDao.save(alert);

        assertEquals(0, shipmentClient.getShipments(req).size());

        //create alert
        alert = createTemperatureAlert(s, AlertType.Hot);
        alert.setDate(new Date(System.currentTimeMillis() - oneDay));
        alertDao.save(alert);

        assertEquals(1, shipmentClient.getShipments(req).size());
    }
    @Test
    public void testFilteredByGoods() throws RestServiceException, IOException {
        final String s1Goods = "s1Goods";
        final String s2Goods = "s2Goods";
        final String s3Goods = "s3Goods";

        final Shipment s1 = createShipment(true);
        s1.setShipmentDescription(s1Goods);
        final Shipment s2 = createShipment(true);
        s2.setPalletId(s2Goods);
        final Shipment s3 = createShipment(true);
        s3.setAssetNum(s3Goods);

        shipmentDao.save(Arrays.asList(s1, s2, s3));

        final GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();
        JsonArray shipments;

        req.setGoods(s1Goods);
        shipments = shipmentClient.getShipments(req);
        assertEquals(1, shipments.size());
        assertEquals(s1.getId().longValue(),
                shipments.get(0).getAsJsonObject().get(ShipmentConstants.SHIPMENT_ID).getAsLong());

        req.setGoods(s2Goods);
        shipments = shipmentClient.getShipments(req);
        assertEquals(1, shipments.size());
        assertEquals(s2.getId().longValue(),
                shipments.get(0).getAsJsonObject().get(ShipmentConstants.SHIPMENT_ID).getAsLong());

        req.setGoods(s3Goods);
        shipments = shipmentClient.getShipments(req);
        assertEquals(1, shipments.size());
        assertEquals(s3.getId().longValue(),
                shipments.get(0).getAsJsonObject().get(ShipmentConstants.SHIPMENT_ID).getAsLong());
        assertEquals(1, currentJsonResponse.get("totalCount").getAsInt());
   }
    @Test
    public void testExcludePriorShipments() throws RestServiceException, IOException {
        final Device d1 = createDevice("329487092384570", true);
        final Device d2 = createDevice("392879384787838", true);
        createShipment(d1, true);
        final Shipment s2 = createShipment(d1, true);
        createShipment(d2, true);
        final Shipment s4 = createShipment(d2, true);

        final GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();
        JsonArray shipments;
        shipments = shipmentClient.getShipments(req);
        assertEquals(4, shipments.size());
        assertEquals(4, currentJsonResponse.get("totalCount").getAsInt());

        req.setExcludePriorShipments(true);
        shipments = shipmentClient.getShipments(req);
        assertEquals(2, shipments.size());
        assertEquals(s2.getId().longValue(),
                shipments.get(0).getAsJsonObject().get(ShipmentConstants.SHIPMENT_ID).getAsLong());
        assertEquals(s4.getId().longValue(),
                shipments.get(1).getAsJsonObject().get(ShipmentConstants.SHIPMENT_ID).getAsLong());
        assertEquals(2, currentJsonResponse.get("totalCount").getAsInt());
    }
    @Test
    public void testSaveEmpty() throws RestServiceException, IOException {
        final Shipment shipment = new Shipment();
        shipment.setDevice(createDevice("123987230987", true));
        final Long id = shipmentClient.saveShipment(new ShipmentDto(shipment), null, false).getShipmentId();
        assertNotNull(id);

        final Shipment s = shipmentDao.findOne(id);
        assertNull(s.getShutdownDeviceAfterMinutes());
        assertNull(s.getArrivalNotificationWithinKm());
    }
    @Test
    public void autoStartShipment() throws IOException, RestServiceException {
        final Device d = createDevice("123987230987", true);

        //test not device found
        try {
            shipmentClient.autoStartShipment("1111111111111111");
            throw new AssertionFailedError("Not device found exception should be thrown");
        } catch (final Exception e) {
            //ok
        }

        //test not last reading found
        try {
            shipmentClient.autoStartShipment(d.getImei());
            throw new AssertionFailedError("Not last reading found exception should be thrown");
        } catch (final Exception e) {
            //ok
        }

        //create last event for device
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(d);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        e.setLatitude(50.50);
        e.setLongitude(51.51);

        trackerEventDao.save(e);

        final Long id = shipmentClient.autoStartShipment(d.getImei());
        assertNotNull(id);
        final Shipment s = shipmentDao.findOne(id);
        assertNotNull(s);
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
        return createEvent(shipment, 50.50, 51.51, type, System.currentTimeMillis());
    }
    /**
     * @param shipment shipment.
     * @param lat latitude.
     * @param lon longitude.
     * @param event time.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Shipment shipment, final double lat, final double lon, final long time) {
        return createEvent(shipment, lat, lon, TrackerEventType.AUT, time);
    }

    /**
     * @param shipment
     * @param lat
     * @param lon
     * @param type
     * @return
     */
    private TrackerEvent createEvent(final Shipment shipment, final double lat,
            final double lon, final TrackerEventType type, final long time) {
        final TrackerEvent e = new TrackerEvent();
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setBattery(1234);
        e.setTemperature(56);
        e.setTime(new Date(time));
        e.setType(type);
        e.setLatitude(lat);
        e.setLongitude(lon);

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
     * @param shipment
     * @param temperature
     */
    private TrackerEvent createTrackerEvent(final Shipment shipment, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setTime(new Date());
        e.setTemperature(temperature);
        e.setLatitude(0.);
        e.setLongitude(0.);
        return context.getBean(TrackerEventDao.class).save(e);
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
    /**
     * @param locationName
     * @return
     */
    private InterimStop createInterimStop(final Shipment s) {
        final LocationProfile loc = createLocationProfile(true);

        final InterimStop stop = new InterimStop();
        stop.setLocation(loc);
        stop.setDate(new Date());
        stop.setLatitude(1.0);
        stop.setLongitude(2.0);
        stop.setTime(15);
        context.getBean(InterimStopDao.class).add(s, stop);
        return stop;
    }

    /**
     * @param text note text.
     */
    private Note createNote(final Shipment shipment, final String text) {
        final Note n = new Note();
        n.setCreatedBy(user.getEmail());
        n.setCreationDate(new Date());
        n.setNoteText(text);
        n.setNoteType("Green");
        n.setTimeOnChart(new Date());
        return context.getBean(NoteDao.class).save(shipment, n);
    }
    /**
     * @param name
     * @return
     */
    private DeviceGroup createDeviceGroup(final String name) {
        final DeviceGroup dg = new DeviceGroup();
        dg.setCompany(getCompany());
        dg.setName(name);
        dg.setDescription("Description of group " + name);
        return context.getBean(DeviceGroupDao.class).save(dg);
    }
    /**
     * @param shipmentJson
     * @return
     */
    private List<KeyLocation> getKeyLocations(final JsonObject shipmentJson) {
        final ShipmentSerializer ser = new ShipmentSerializer(user);

        final JsonElement el = shipmentJson.get("keyLocations");
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final List<KeyLocation> locs = new LinkedList<>();
        for (final JsonElement jsl : el.getAsJsonArray()) {
            locs.add(ser.parseKeyLocation(jsl));
        }
        return locs;
    }
    /**
     * @param locs list of locations.
     * @param type alert type.
     * @return
     */
    private List<KeyLocation> getAlerts(final List<KeyLocation> locs,
            final AlertType type) {

        final List<KeyLocation> list = new LinkedList<>();
        for (final KeyLocation kl : locs) {
            final String key = kl.getKey();
            if (key != null && key.startsWith(type.name())) {
                list.add(kl);
            }
        }
        return list;
    }
    @After
    public void tearDown() {
        shipmentClient.removeRestIoListener(restIoListener);
    }
}
