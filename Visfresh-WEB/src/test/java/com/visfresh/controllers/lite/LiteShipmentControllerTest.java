/**
 *
 */
package com.visfresh.controllers.lite;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.visfresh.controllers.AbstractRestServiceTest;
import com.visfresh.controllers.restclient.LiteShipmentRestClient;
import com.visfresh.controllers.restclient.RestIoListener;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.rules.EtaCalculationRule;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipmentControllerTest extends AbstractRestServiceTest {
    private ShipmentDao shipmentDao;
    private TrackerEventDao trackerEventDao;
    private LiteShipmentRestClient shipmentClient;
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
    public LiteShipmentControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        shipmentDao = context.getBean(ShipmentDao.class);
        trackerEventDao = context.getBean(TrackerEventDao.class);

        final String token = login();
        this.user = context.getBean(AuthService.class).getUserForToken(token);
        shipmentClient = new LiteShipmentRestClient(user);

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

        assertEquals(2, shipmentClient.getShipments(null, null).size());
        assertEquals(1, shipmentClient.getShipments(1, 1).size());
        assertEquals(1, shipmentClient.getShipments(2, 1).size());
        assertEquals(0, shipmentClient.getShipments(3, 10000).size());
    }
    @Test
    public void testGetShipmentsKeyLocations() throws RestServiceException, IOException {
        final Shipment s1 = createShipment(true);
        s1.getShippedTo().setAddress("Coles Perth DC");
        shipmentDao.save(s1);
        //create second shipment
        createShipment(true);

        final long t = System.currentTimeMillis() - 10000000l;
        createEvent(s1, t + 1000l, 2.0);
        createEvent(s1, t + 2000l, 2.0);
        createEvent(s1, t + 3000l, 6.0);
        createEvent(s1, t + 4000l, 6.0);
        createEvent(s1, t + 5000l, 5.0);
        createEvent(s1, t + 6000l, 4.0);
        createEvent(s1, t + 7000l, 2.0);
        createEvent(s1, t + 8000l, 2.0);
        createEvent(s1, t + 9000l, 2.0);

        final List<LiteShipment> shipments = shipmentClient.getShipments(null, null);
        assertEquals(9, shipments.get(0).getKeyLocations().size());
    }
    @Test
    public void testGetShipmentsNearBy() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        s.getShippedTo().setAddress("Coles Perth DC");
        shipmentDao.save(s);
        //create second shipment
        createShipment(true);

        final long t = System.currentTimeMillis() - 10000000l;
        createEvent(s, t + 1000l, 2.0);
        createEvent(s, t + 2000l, 2.0);
        createEvent(s, t + 3000l, 6.0);
        createEvent(s, t + 4000l, 6.0);
        createEvent(s, t + 5000l, 5.0);
        createEvent(s, t + 6000l, 4.0);
        createEvent(s, t + 7000l, 2.0);
        createEvent(s, t + 8000l, 2.0);

        //create last event.
        final TrackerEvent last = createEvent(s, t + 9000l, 2.0);
        //set coordinates
        last.setLatitude(last.getLatitude() + 10.);
        last.setLongitude(last.getLongitude() + 10.);
        trackerEventDao.save(last);

        final List<LiteShipment> shipments = shipmentClient.getShipmentsNearBy(
                last.getLatitude(), last.getLongitude(), 500, new Date(last.getTime().getTime() - 100000l));

        assertEquals(1, shipments.size());
        assertEquals(9, shipments.get(0).getKeyLocations().size());
    }
    @Test
    public void testGetShipmentsNearByNotDate() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        s.getShippedTo().setAddress("Coles Perth DC");
        shipmentDao.save(s);

        final long t = System.currentTimeMillis() - 10000000l;
        createEvent(s, t + 1000l, 2.0);
        createEvent(s, t + 2000l, 2.0);
        createEvent(s, t + 3000l, 6.0);
        createEvent(s, t + 4000l, 6.0);
        createEvent(s, t + 5000l, 5.0);
        createEvent(s, t + 6000l, 4.0);
        createEvent(s, t + 7000l, 2.0);
        createEvent(s, t + 8000l, 2.0);

        //create last event.
        final TrackerEvent last = createEvent(s, t + 9000l, 2.0);
        //set coordinates
        last.setLatitude(last.getLatitude() + 10.);
        last.setLongitude(last.getLongitude() + 10.);
        trackerEventDao.save(last);

        final List<LiteShipment> shipments = shipmentClient.getShipmentsNearBy(
                last.getLatitude(), last.getLongitude(), 500, null);

        assertEquals(1, shipments.size());
        assertEquals(9, shipments.get(0).getKeyLocations().size());
    }

    private TrackerEvent createEvent(final Shipment shipment,
            final long time, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setBattery(1234);
        e.setTemperature(temperature);
        e.setTime(new Date(time));
        e.setType(TrackerEventType.AUT);
        e.setLatitude(10.);
        e.setLongitude(10.);

        trackerEventDao.save(e);
        return e;
    }
}
