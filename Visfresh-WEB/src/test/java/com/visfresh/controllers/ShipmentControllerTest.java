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

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.controllers.restclient.ShipmentRestClient;
import com.visfresh.controllers.restclient.ShipmentTemplateRestClient;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
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
    private ShipmentTemplateDao shipmentTemplateDao;
    private AlertDao alertDao;
    private ArrivalDao arrivalDao;
    private TrackerEventDao trackerEventDao;
    private ShipmentTemplateRestClient shipmentTemplateClient = new ShipmentTemplateRestClient(UTC);
    private ShipmentRestClient shipmentClient;

    /**
     * Default constructor.
     */
    public ShipmentControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        shipmentDao = context.getBean(ShipmentDao.class);
        shipmentTemplateDao = context.getBean(ShipmentTemplateDao.class);
        alertDao = context.getBean(AlertDao.class);
        arrivalDao = context.getBean(ArrivalDao.class);
        trackerEventDao = context.getBean(TrackerEventDao.class);

        final String token = login();
        final User user = context.getBean(AuthService.class).getUserForToken(token);
        shipmentClient = new ShipmentRestClient(user);

        shipmentClient.setServiceUrl(getServiceUrl());
        shipmentTemplateClient.setServiceUrl(getServiceUrl());

        final ReferenceResolver r = context.getBean(ReferenceResolver.class);

        shipmentTemplateClient.setReferenceResolver(r);
        shipmentClient.setReferenceResolver(r);
        shipmentClient.setUserResolver(context.getBean(UserResolver.class));

        shipmentTemplateClient.setAuthToken(token);
        shipmentClient.setAuthToken(token);
    }

    //@RequestMapping(value = "/saveShipmentTemplate/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveShipmentTemplate(@PathVariable final String authToken,
    //        final @RequestBody String tpl) {
    @Test
    public void testSaveShipmentTemplate() throws RestServiceException, IOException {
        final ShipmentTemplate t = createShipmentTemplate(true);
        t.setId(null);
        final Long id = shipmentTemplateClient.saveShipmentTemplate(t);
        assertNotNull(id);
    }
    //@RequestMapping(value = "/getShipmentTemplates/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipmentTemplates(@PathVariable final String authToken) {
    @Test
    public void testGetShipmentTemplates() throws RestServiceException, IOException {
        createShipmentTemplate(true);
        createShipmentTemplate(true);

        assertEquals(2, shipmentTemplateClient.getShipmentTemplates(1, 10000).size());
        assertEquals(1, shipmentTemplateClient.getShipmentTemplates(1, 1).size());
        assertEquals(1, shipmentTemplateClient.getShipmentTemplates(2, 1).size());
        assertEquals(0, shipmentTemplateClient.getShipmentTemplates(3, 10000).size());
    }
    @Test
    public void testGetShipmentTemplate() throws IOException, RestServiceException {
        final ShipmentTemplate sp = createShipmentTemplate(true);
        assertNotNull(shipmentTemplateClient.getShipmentTemplate(sp.getId()));
    }
    @Test
    public void testDeleteShipmentTemplate() throws IOException, RestServiceException {
        final ShipmentTemplate sp = createShipmentTemplate(true);
        shipmentTemplateClient.deleteShipmentTemplate(sp.getId());
        assertNull(shipmentTemplateDao.findOne(sp.getId()));
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
        final ShipmentTemplate tpl = shipmentTemplateDao.findOne(id);

        assertNotNull(tpl);
        assertNotNull(tpl.getName());
    }
    //@RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipments(@PathVariable final String authToken) {
    @Test
    public void testGetShipments() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        s.getShippedTo().setAddress("Coles Perth DC");
        final Shipment s2 = createShipment(true);
        s2.getShippedTo().setAddress("Coles Perth DC");

        //add alert
        final Device d = s.getDevice();

        createAlert(s, d, AlertType.Battery);
        createAlert(s, d, AlertType.Battery);
        createAlert(s, d, AlertType.MovementStart);
        createAlert(s, d, AlertType.MovementStart);
        createAlert(s, d, AlertType.MovementStart);
        createAlert(s, d, AlertType.MovementStart);
//        createAlert(s, d, AlertType.MovementStop);
        createAlert(s, d, AlertType.LightOff);
        createAlert(s, d, AlertType.LightOn);

        createTemperatureAlert(s, d, AlertType.Hot);
        createTemperatureAlert(s, d, AlertType.Hot);
        createTemperatureAlert(s, d, AlertType.Cold);
        createTemperatureAlert(s, d, AlertType.CriticalCold);
        createTemperatureAlert(s, d, AlertType.CriticalCold);
        createTemperatureAlert(s, d, AlertType.CriticalCold);
        createTemperatureAlert(s, d, AlertType.CriticalHot);
        createArrival(s, d);

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
        //get server device
        final Device d = s.getDevice();

        //add tracker event.
        createEvent(s, "AUT", d);
        createEvent(s, "AUT", d);

        //add alert
        createAlert(s, d, AlertType.Battery);
        createTemperatureAlert(s, d, AlertType.Hot);
        createArrival(s, d);

        final Date fromTime = new Date(System.currentTimeMillis() - 100000000L);
        final Date toTime = new Date(System.currentTimeMillis() + 10000000l);
        final JsonObject sd = shipmentClient.getSingleShipment(s, fromTime, toTime).getAsJsonObject();
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
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //check one week date ranges
        s.setShipmentDate(new Date());
        saveShipmentDirectly(s);

        req = createFilter(s);
        req.setLastWeek(true);
        assertEquals(1, shipmentClient.getShipments(req).size());

        s.setShipmentDate(new Date(System.currentTimeMillis() - 8 * oneDay));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //check last day
        s.setShipmentDate(new Date());
        saveShipmentDirectly(s);

        req = createFilter(s);
        req.setLastDay(true);
        assertEquals(1, shipmentClient.getShipments(req).size());

        s.setShipmentDate(new Date(System.currentTimeMillis() - 2 * oneDay));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //check last 2 days
        s.setShipmentDate(new Date());
        saveShipmentDirectly(s);

        req = createFilter(s);
        req.setLast2Days(true);
        assertEquals(1, shipmentClient.getShipments(req).size());

        s.setShipmentDate(new Date(System.currentTimeMillis() - 3 * oneDay));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());

        //check last one month
        s.setShipmentDate(new Date());
        saveShipmentDirectly(s);

        req = createFilter(s);
        req.setLastMonth(true);
        assertEquals(1, shipmentClient.getShipments(req).size());

        s.setShipmentDate(new Date(System.currentTimeMillis() - 32 * oneDay));
        saveShipmentDirectly(s);
        assertEquals(0, shipmentClient.getShipments(req).size());
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
     * @param d
     * @param type
     * @return
     */
    private TemperatureAlert createTemperatureAlert(final Shipment s, final Device d,
            final AlertType type) {
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(new Date());
        alert.setType(type);
        alert.setTemperature(5);
        alert.setMinutes(55);
        alert.setDevice(d);
        alert.setShipment(s);
        alertDao.save(alert);
        return alert;
    }
    /**
     * @param shipment shipment.
     * @param device device.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final Shipment shipment, final String type, final Device device) {
        final TrackerEvent e = new TrackerEvent();
        e.setShipment(shipment);
        e.setDevice(device);
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
     * @param d device.
     * @return
     */
    private Arrival createArrival(final Shipment s, final Device d) {
        final Arrival arrival = new Arrival();
        arrival.setDevice(d);
        arrival.setShipment(s);
        arrival.setNumberOfMettersOfArrival(400);
        arrival.setDate(new Date(System.currentTimeMillis() - 50000));
        arrivalDao.save(arrival);
        return arrival;
    }
    /**
     * @param s shipment
     * @param device device
     * @param type alert type.
     * @return alert.
     */
    private Alert createAlert(final Shipment s, final Device device, final AlertType type) {
        final Alert alert = new Alert();
        alert.setShipment(s);
        alert.setDate(new Date(System.currentTimeMillis() - 100000000l));
        alert.setDevice(device);
        alert.setType(type);
        alertDao.save(alert);
        return alert;
    }
}
