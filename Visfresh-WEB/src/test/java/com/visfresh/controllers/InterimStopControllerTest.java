/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.InterimStopRestClient;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.InterimStopDto;
import com.visfresh.services.AuthService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopControllerTest extends AbstractRestServiceTest {
    private InterimStopRestClient client;
    private User user;

    /**
     * Default constructor.
     */
    public InterimStopControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        final String token = login();
        this.user = context.getBean(AuthService.class).getUserForToken(token);
        client = new InterimStopRestClient(user);

        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(token);
    }

    @Test
    public void testAddInterimStop() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        final LocationProfile loc = createLocationProfile(true);

        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final int time = 10;

        final InterimStopDto req = new InterimStopDto();
        req.setDate(date);
        req.setShipmentId(s.getId());
        req.setLocationId(loc.getId());
        req.setTime(time);

        final Long id = client.addInterimStop(req);
        assertNotNull(id);

        final InterimStop stop = context.getBean(InterimStopDao.class).getByShipment(s).get(0);
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

        assertEquals(id, stop.getId());
        assertEquals(fmt.format(date), fmt.format(stop.getDate()));
        assertEquals(loc.getId(), stop.getLocation().getId());
        assertEquals(time, stop.getTime());
    }
    @Test
    public void testSaveInterimStop() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        final LocationProfile loc = createLocationProfile(true);

        InterimStop stop = createStop(s, loc);
        final InterimStopDto req = new InterimStopDto(s, stop);

        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final int time = 10;

        req.setDate(date);
        req.setShipmentId(s.getId());
        req.setLocationId(loc.getId());
        req.setTime(time);

        final Long id = client.saveInterimStop(req);
        assertNotNull(id);

        stop = context.getBean(InterimStopDao.class).getByShipment(s).get(0);
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

        assertEquals(id, stop.getId());
        assertEquals(fmt.format(date), fmt.format(stop.getDate()));
        assertEquals(loc.getId(), stop.getLocation().getId());
        assertEquals(time, stop.getTime());
    }
    @Test
    public void testDeleteInterimStop() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        final LocationProfile loc = createLocationProfile(true);

        final InterimStop stop = createStop(s, loc);

        client.deleteInterimStop(s, stop);

        assertEquals(0, context.getBean(InterimStopDao.class).getByShipment(s).size());
    }
    @Test
    public void testGetInterimStops() throws IOException, RestServiceException {
        final Shipment s1 = createShipment(true);
        final Shipment s2 = createShipment(true);
        final LocationProfile loc = createLocationProfile(true);

        createStop(s1, loc);
        createStop(s1, loc);
        createStop(s2, loc);

        final List<InterimStopDto> stops = client.getInterimStops(s1);

        assertEquals(2, stops.size());
    }
    @Test
    public void testAddInterimStopNullValues() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        final LocationProfile loc = createLocationProfile(true);

        final int time = 10;

        final InterimStopDto req = new InterimStopDto();
        req.setShipmentId(s.getId());
        req.setLocationId(loc.getId());
        req.setTime(time);

        final Long id = client.addInterimStop(req);
        assertNotNull(id);

        final InterimStop stop = context.getBean(InterimStopDao.class).getByShipment(s).get(0);

        assertEquals(id, stop.getId());
        assertNotNull(stop.getDate());
        assertEquals(loc.getId(), stop.getLocation().getId());
        assertEquals(time, stop.getTime());
    }
    /**
     * @param locationName
     * @return
     */
    private InterimStop createStop(final Shipment s, final LocationProfile loc) {
        final InterimStop stop = new InterimStop();
        stop.setLocation(loc);
        stop.setDate(new Date());
        stop.setTime(15);

        context.getBean(InterimStopDao.class).save(s, stop);
        return stop;
    }
}
