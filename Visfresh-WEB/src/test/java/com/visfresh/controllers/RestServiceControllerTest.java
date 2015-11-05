/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.User;
import com.visfresh.mock.MockAuthService;
import com.visfresh.mock.MockRestService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceControllerTest extends AbstractRestServiceTest {

    private MockRestService service;
    private MockAuthService authService;
    private User user;

    /**
     * Default constructor.
     */
    public RestServiceControllerTest() {
        super();
    }


    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        service = context.getBean(MockRestService.class);
        authService = context.getBean(MockAuthService.class);
        user = authService.users.values().iterator().next();
    }

    //@RequestMapping(value = "/login", method = RequestMethod.POST)
    //public @ResponseBody String login(@RequestBody final String loginRequest) {
    @Test
    public void testLogin() throws RestServiceException, IOException {
        final User user = new User();
        user.setLogin("aldsklksadf");
        final String password = "lkasdlfkj";

        authService.createUser(user, password);
        final String token = facade.login(user.getLogin(), password);
        assertNotNull(token);
    }
    //@RequestMapping(value = "/getToken", method = RequestMethod.GET)
    //public @ResponseBody String getAuthToken(final HttpSession session) {
    public void _testGetToken() throws IOException, RestServiceException {
        final String token = facade.getToken();
        assertNotNull(token);
    }
    //@RequestMapping(value = "/logout/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String logout(@PathVariable final String authToken) {
    @Test
    public void testLogout() throws RestServiceException, IOException {
        facade.logout(facade.getAuthToken());
    }
    //@RequestMapping(value = "/refreshToken/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String refreshToken(@PathVariable final String authToken) {
    @Test
    public void testRefreshToken() throws IOException, RestServiceException {
        final String token = facade.refreshToken();
        facade.setAuthToken(token);
        assertNotNull(token);
    }
    //@RequestMapping(value = "/saveDevice/{authToken}", method = RequestMethod.POST)
    //public @ResponseBody String saveDevice(@PathVariable final String authToken,
    //        final @RequestBody String device) {
    @Test
    public void testSaveDevice() throws RestServiceException, IOException {
        final Device d = createDevice("1209898347987", false);
        facade.saveDevice(d);
    }
    //@RequestMapping(value = "/getDevices/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getDevices(@PathVariable final String authToken) {
    @Test
    public void testGetDevices() throws RestServiceException, IOException {
        createDevice("1209898347987", true);
        createDevice("1209898347988", true);

        assertEquals(2, facade.getDevices(1, 10000).size());
        assertEquals(1, facade.getDevices(1, 1).size());
        assertEquals(1, facade.getDevices(2, 1).size());
        assertEquals(0, facade.getDevices(3, 10000).size());
    }
    //@RequestMapping(value = "/getNotifications/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String getNotifications(@PathVariable final String authToken,
    //        @RequestParam final Long shipment) {
    @Test
    public void testGetNotifications() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        //get server device
        final Device d = service.devices.get(s.getDevice().getId());

        //create temperature alert notification
        final TemperatureAlert tempAlert = new TemperatureAlert();
        tempAlert.setDate(new Date());
        tempAlert.setId(service.ids.incrementAndGet());
        tempAlert.setType(AlertType.Hot);
        tempAlert.setTemperature(5);
        tempAlert.setMinutes(55);
        tempAlert.setDevice(d);
        tempAlert.setShipment(s);

        Notification n = new Notification();
        n.setId(service.ids.incrementAndGet());
        n.setIssue(tempAlert);
        n.setType(NotificationType.Alert);

        service.alerts.put(tempAlert.getId(), tempAlert);
        final List<Notification> nofications = new LinkedList<Notification>();
        nofications.add(n);

        service.notifications.put(user.getLogin(), nofications);

        //create ordinar alert
        final Alert batteryAlert = new Alert();
        batteryAlert.setDate(new Date());
        batteryAlert.setId(service.ids.incrementAndGet());
        batteryAlert.setType(AlertType.Battery);
        batteryAlert.setDevice(d);
        batteryAlert.setShipment(s);

        n = new Notification();
        n.setId(service.ids.incrementAndGet());
        n.setIssue(batteryAlert);
        n.setType(NotificationType.Alert);

        service.alerts.put(batteryAlert.getId(), batteryAlert);
        nofications.add(n);

        //arrival notification
        final Arrival a = new Arrival();
        a.setDevice(d);
        a.setId(service.ids.incrementAndGet());
        a.setDate(new Date());
        a.setNumberOfMettersOfArrival(1500);
        a.setShipment(s);


        n = new Notification();
        n.setId(service.ids.incrementAndGet());
        n.setIssue(a);
        n.setType(NotificationType.Arrival);

        service.arrivals.put(a.getId(), a);
        nofications.add(n);

        //get notifications
        assertEquals(3, facade.getNotifications(1, 10000).size());
        assertEquals(1, facade.getNotifications(1, 1).size());
        assertEquals(1, facade.getNotifications(2, 1).size());
        assertEquals(0, facade.getNotifications(3, 10000).size());
    }
    //@RequestMapping(value = "/markNotificationsAsRead/{authToken}", method = RequestMethod.GET)
    //public @ResponseBody String markNotificationsAsRead(@PathVariable final String authToken,
    //        @RequestBody final String notificationIds) {
    @Test
    public void testMarkNotificationsAsRead() throws IOException, RestServiceException {
        final Shipment s = createShipment(true);
        //get server device
        final Device d = service.devices.get(s.getDevice().getId());

        //create temperature alert notification
        final TemperatureAlert tempAlert = new TemperatureAlert();
        tempAlert.setDate(new Date());
        tempAlert.setId(service.ids.incrementAndGet());
        tempAlert.setType(AlertType.Hot);
        tempAlert.setTemperature(5);
        tempAlert.setMinutes(55);
        tempAlert.setShipment(s);
        tempAlert.setDevice(d);

        final Notification n = new Notification();
        n.setId(service.ids.incrementAndGet());
        n.setIssue(tempAlert);
        n.setType(NotificationType.Alert);

        service.alerts.put(tempAlert.getId(), tempAlert);
        final List<Notification> notifications = new LinkedList<Notification>();
        notifications.add(n);

        service.notifications.put(user.getLogin(), notifications);

        //create ordinar alert
        final Alert batteryAlert = new Alert();
        batteryAlert.setDate(new Date());
        batteryAlert.setId(service.ids.incrementAndGet());
        batteryAlert.setType(AlertType.Battery);
        batteryAlert.setDevice(d);
        batteryAlert.setShipment(s);

        final Notification n1 = new Notification();
        n1.setId(service.ids.incrementAndGet());
        n1.setIssue(batteryAlert);
        n1.setType(NotificationType.Alert);

        service.alerts.put(batteryAlert.getId(), batteryAlert);
        notifications.add(n1);

        //arrival notification
        final Arrival a = new Arrival();
        a.setDevice(d);
        a.setId(service.ids.incrementAndGet());
        a.setDate(new Date());
        a.setNumberOfMettersOfArrival(1500);
        a.setShipment(s);

        final Notification n2 = new Notification();
        n2.setId(service.ids.incrementAndGet());
        n2.setIssue(a);
        n2.setType(NotificationType.Arrival);

        service.arrivals.put(a.getId(), a);
        notifications.add(n2);

        //get notifications
        final List<Notification> toReaden = new LinkedList<Notification>();
        toReaden.add(n1);
        toReaden.add(n2);

        facade.markNotificationsAsRead(toReaden);

        assertEquals(1, facade.getNotifications(1, 10000).size());

    }

    @Test
    public void testSendCommandToDevice() throws RestServiceException, IOException {
        final Device device = createDevice("089723409857032498", true);
        facade.saveDevice(device);

        facade.sendCommandToDevice(device, "shutdown");
    }
    @Test
    public void testGetDevice() throws IOException, RestServiceException {
        final Device d = createDevice("923487509328", true);
        service.devices.put(d.getId(), d);

        assertNotNull(facade.getDevice(d.getId()));
    }
    @Test
    public void testGetCompany() throws IOException, RestServiceException {
        final String description = "JUnit test company";
        final Long id = 77777l;
        final String name = "Test Company";

        Company c = new Company();
        c.setDescription(description);
        c.setId(id);
        c.setName(name);

        service.companies.put(c.getId(), c);

        c = facade.getCompany(c.getId());

        assertEquals(description, c.getDescription());
        assertEquals(id, c.getId());
        assertEquals(name, c.getName());
    }
    @Test
    public void testGetCompanies() throws IOException, RestServiceException {
        //create company
        Company c = new Company();
        c.setDescription("JUnit test company");
        c.setId(7777l);
        c.setName("JUnit-C-1");
        service.companies.put(c.getId(), c);

        c = new Company();
        c.setDescription("JUnit test company");
        c.setId(7778l);
        c.setName("JUnit-C-2");
        service.companies.put(c.getId(), c);

        //+ one default company existing on server
        assertEquals(3, facade.getCompanies(1, 10000).size());
        assertEquals(1, facade.getCompanies(1, 1).size());
        assertEquals(1, facade.getCompanies(2, 1).size());
        assertEquals(0, facade.getCompanies(3, 10000).size());
    }
}
