/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.NotificationRestClient;
import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.User;
import com.visfresh.io.DeviceResolver;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationControllerTest extends AbstractRestServiceTest {
    private User user;
    private DeviceDao deviceDao;
    private AlertDao alertDao;
    private ArrivalDao arrivalDao;
    private NotificationDao notificationDao;
    private NotificationRestClient client = new NotificationRestClient(UTC);

    /**
     * Default constructor.
     */
    public NotificationControllerTest() {
        super();
    }


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        deviceDao = context.getBean(DeviceDao.class);
        alertDao = context.getBean(AlertDao.class);
        arrivalDao = context.getBean(ArrivalDao.class);
        notificationDao = context.getBean(NotificationDao.class);

        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
        client.setDeviceResolver(context.getBean(DeviceResolver.class));
        client.setShipmentResolver(context.getBean(ShipmentResolver.class));
    }
    //@RequestMapping(value = "/getNotifications/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getNotifications(@PathVariable final String authToken,
    //        @RequestParam final Long shipment) {
    @Test
    public void testGetNotifications() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        //get server device
        final Device d = deviceDao.findOne(s.getDevice().getId());

        //create temperature alert notification
        final TemperatureAlert tempAlert = new TemperatureAlert();
        tempAlert.setDate(new Date());
        tempAlert.setType(AlertType.Hot);
        tempAlert.setTemperature(5);
        tempAlert.setMinutes(55);
        tempAlert.setDevice(d);
        tempAlert.setShipment(s);
        alertDao.save(tempAlert);

        Notification n = new Notification();
        n.setIssue(tempAlert);
        n.setType(NotificationType.Alert);
        n.setUser(user);
        notificationDao.save(n);

        //create ordinar alert
        final Alert batteryAlert = new Alert();
        batteryAlert.setDate(new Date());
        batteryAlert.setType(AlertType.Battery);
        batteryAlert.setDevice(d);
        batteryAlert.setShipment(s);
        alertDao.save(batteryAlert);

        n = new Notification();
        n.setIssue(batteryAlert);
        n.setType(NotificationType.Alert);
        n.setUser(user);
        notificationDao.save(n);

        //arrival notification
        final Arrival a = new Arrival();
        a.setDevice(d);
        a.setDate(new Date());
        a.setNumberOfMettersOfArrival(1500);
        a.setShipment(s);
        arrivalDao.save(a);

        n = new Notification();
        n.setIssue(a);
        n.setType(NotificationType.Arrival);
        n.setUser(user);
        notificationDao.save(n);

        //get notifications
        assertEquals(3, client.getNotifications(false, null, null).size());
        assertEquals(1, client.getNotifications(false, 1, 1).size());
        assertEquals(1, client.getNotifications(false, 2, 1).size());
        assertEquals(0, client.getNotifications(false, 3, 10000).size());
    }
    @Test
    public void testIncludeRead() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        //get server device
        final Device d = deviceDao.findOne(s.getDevice().getId());

        //create temperature alert notification
        final TemperatureAlert tempAlert = new TemperatureAlert();
        tempAlert.setDate(new Date());
        tempAlert.setType(AlertType.Hot);
        tempAlert.setTemperature(5);
        tempAlert.setMinutes(55);
        tempAlert.setDevice(d);
        tempAlert.setShipment(s);
        alertDao.save(tempAlert);

        final Notification n = new Notification();
        n.setIssue(tempAlert);
        n.setType(NotificationType.Alert);
        n.setUser(user);
        n.setRead(true);
        notificationDao.save(n);

        //get notifications
        assertEquals(0, client.getNotifications(false, null, null).size());
        assertEquals(1, client.getNotifications(true, null, null).size());
    }
    //@RequestMapping(value = "/markNotificationsAsRead/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String markNotificationsAsRead(@PathVariable final String authToken,
    //        @RequestBody final String notificationIds) {
    @Test
    public void testMarkNotificationsAsRead() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        //get server device
        final Device d = deviceDao.findOne(s.getDevice().getId());

        //create temperature alert notification
        final TemperatureAlert tempAlert = new TemperatureAlert();
        tempAlert.setDate(new Date());
        tempAlert.setType(AlertType.Hot);
        tempAlert.setTemperature(5);
        tempAlert.setMinutes(55);
        tempAlert.setShipment(s);
        tempAlert.setDevice(d);
        alertDao.save(tempAlert);

        final Notification n = new Notification();
        n.setUser(user);
        n.setIssue(tempAlert);
        n.setType(NotificationType.Alert);
        notificationDao.save(n);

        //create ordinar alert
        final Alert batteryAlert = new Alert();
        batteryAlert.setDate(new Date());
        batteryAlert.setType(AlertType.Battery);
        batteryAlert.setDevice(d);
        batteryAlert.setShipment(s);
        alertDao.save(batteryAlert);

        final Notification n1 = new Notification();
        n1.setIssue(batteryAlert);
        n1.setType(NotificationType.Alert);
        n1.setUser(user);
        notificationDao.save(n1);

        //arrival notification
        final Arrival a = new Arrival();
        a.setDevice(d);
        a.setDate(new Date());
        a.setNumberOfMettersOfArrival(1500);
        a.setShipment(s);
        arrivalDao.save(a);

        final Notification n2 = new Notification();
        n2.setIssue(a);
        n2.setType(NotificationType.Arrival);
        n2.setUser(user);
        notificationDao.save(n2);

        //get notifications
        final List<Notification> toReaden = new LinkedList<Notification>();
        toReaden.add(n1);
        toReaden.add(n2);

        client.markNotificationsAsRead(toReaden);

        assertEquals(1, client.getNotifications(false, null, null).size());
    }
}
