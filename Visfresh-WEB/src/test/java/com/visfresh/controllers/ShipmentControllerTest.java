/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
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
    }

    //@RequestMapping(value = "/saveShipmentTemplate/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveShipmentTemplate(@PathVariable final String authToken,
    //        final @RequestBody String tpl) {
    @Test
    public void testSaveShipmentTemplate() throws RestServiceException, IOException {
        final ShipmentTemplate t = createShipmentTemplate(true);
        t.setId(null);
        final Long id = facade.saveShipmentTemplate(t);
        assertNotNull(id);
    }
    //@RequestMapping(value = "/getShipmentTemplates/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipmentTemplates(@PathVariable final String authToken) {
    @Test
    public void testGetShipmentTemplates() throws RestServiceException, IOException {
        createShipmentTemplate(true);
        createShipmentTemplate(true);

        assertEquals(2, facade.getShipmentTemplates(1, 10000).size());
        assertEquals(1, facade.getShipmentTemplates(1, 1).size());
        assertEquals(1, facade.getShipmentTemplates(2, 1).size());
        assertEquals(0, facade.getShipmentTemplates(3, 10000).size());
    }
    @Test
    public void testGetShipmentTemplate() throws IOException, RestServiceException {
        final ShipmentTemplate sp = createShipmentTemplate(true);
        assertNotNull(facade.getShipmentTemplate(sp.getId()));
    }
    @Test
    public void testDeleteShipmentTemplate() throws IOException, RestServiceException {
        final ShipmentTemplate sp = createShipmentTemplate(true);
        facade.deleteShipmentTemplate(sp.getId());
        assertNull(shipmentTemplateDao.findOne(sp.getId()));
    }
    //@RequestMapping(value = "/saveShipment/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveShipment(@PathVariable final String authToken,
    //        final @RequestBody String shipment) {
    @Test
    public void testSaveShipment() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);
        s.setId(null);
        final Long id = facade.saveShipment(s, "NewTemplate.tpl", true);
        assertNotNull(id);
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

        assertEquals(2, facade.getShipments(null, null).size());
        assertEquals(1, facade.getShipments(1, 1).size());
        assertEquals(1, facade.getShipments(2, 1).size());
        assertEquals(0, facade.getShipments(3, 10000).size());
    }
    @Test
    public void testGetShipment() throws IOException, RestServiceException {
        final Shipment sp = createShipment(true);
        assertNotNull(facade.getShipment(sp.getId()));
    }
    @Test
    public void testDeleteShipment() throws IOException, RestServiceException {
        final Shipment sp = createShipment(true);
        facade.deleteShipment(sp.getId());
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
        final JsonObject sd = facade.getSingleShipment(s, fromTime, toTime).getAsJsonObject();
        assertNotNull(sd);
    }
    //@RequestMapping(value = "/getShipments/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getShipments(@PathVariable final String authToken) {
    @Test
    public void testGetShipmentsFiltered() throws RestServiceException, IOException {
        final Shipment s = createShipment(true);

        final Long shippedFrom = s.getShippedFrom().getId();
        final Long shippedTo = s.getShippedTo().getId();
        final String goods = s.getShipmentDescription();
        final ShipmentStatus status = ShipmentStatus.InProgress;
        final String device = s.getDevice().getId();

        assertEquals(1, facade.getShipments(1, 10000,
                shippedFrom, shippedTo, goods, device, status).size());
//        assertEquals(0, facade.getShipments(1, 10000,
//                shippedTo, shippedTo, goods, device, status).size());
        assertEquals(0, facade.getShipments(1, 10000,
                shippedFrom, shippedFrom, "abrakadabra", device, status).size());
        assertEquals(0, facade.getShipments(1, 10000,
                shippedFrom, shippedTo, goods, "11111111", status).size());
        assertEquals(0, facade.getShipments(1, 10000,
                shippedFrom, shippedTo, goods, device, ShipmentStatus.Complete).size());
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
