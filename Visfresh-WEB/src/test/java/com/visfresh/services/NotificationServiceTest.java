/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.mock.MockEmailService;
import com.visfresh.mock.MockSmsService;
import com.visfresh.rules.RuleEngineTestRunner;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(RuleEngineTestRunner.class)
public class NotificationServiceTest {
    private NotificationService service;
    private AnnotationConfigApplicationContext context;
    private Company company;
    private Device device;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public NotificationServiceTest() {
        super();
    }

    @Before
    public void setUp() {
        this.context = RuleEngineTestRunner.getContext();
        service = context.getBean(NotificationService.class);

        //create company
        final Company c = new Company();
        c.setName("JUnit");
        company = context.getBean(CompanyDao.class).save(c);

        //create device
        this.device = createDevice("92387987987978");
        this.shipment = createShipment(ShipmentStatus.Default);
    }
    /**
     * @param status shipment status.
     * @return shipment.
     */
    private Shipment createShipment(final ShipmentStatus status) {
        final Shipment s = new Shipment();
        s.setCompany(company);
        s.setStatus(status);
        s.setDevice(device);
        return context.getBean(ShipmentDao.class).save(s);
    }
    /**
     * @param imei device imei.
     * @return
     */
    private Device createDevice(final String imei) {
        final Device d = new Device();
        d.setActive(true);
        d.setImei(imei);
        d.setName("JUnit-" + imei);
        d.setCompany(company);
        return context.getBean(DeviceDao.class).save(d);
    }

    @Test
    public void testSendEmailNotification() {
        //create alert and associated tracker event.
        final TrackerEvent e = createTrackerEvent();
        final Alert issue = createAlert(e);

        //create notification schedule
        final User u = createUser("junit@smarttrace.com.au");

        final NotificationSchedule sched = new NotificationSchedule();
        sched.setCompany(company);
        sched.setName("JUnit-" + u.getEmail());

        final PersonSchedule s = new PersonSchedule();
        s.setUser(u);
        s.setSendEmail(true);
        context.getBean(NotificationScheduleDao.class).save(sched);

        //send notification
        service.sendNotification(s, issue, e);

        //check notification created
        assertEquals(1, context.getBean(NotificationDao.class).findAll(null, null, null).size());

        //check email sent
        assertEquals(1, context.getBean(MockEmailService.class).getMessages().size());
    }
    @Test
    public void testSendSmsNotification() {
        //create alert and associated tracker event.
        final TrackerEvent e = createTrackerEvent();
        final Alert issue = createAlert(e);

        //create notification schedule
        final User u = createUser("junit@smarttrace.com.au");
        u.setPhone("+12345689123");
        context.getBean(UserDao.class).save(u);

        final NotificationSchedule sched = new NotificationSchedule();
        sched.setCompany(company);
        sched.setName("JUnit-" + u.getEmail());

        final PersonSchedule s = new PersonSchedule();
        s.setUser(u);
        s.setSendSms(true);
        context.getBean(NotificationScheduleDao.class).save(sched);

        //send notification
        service.sendNotification(s, issue, e);

        //check notification created
        assertEquals(1, context.getBean(NotificationDao.class).findAll(null, null, null).size());

        //check email sent
        assertEquals(1, context.getBean(MockSmsService.class).getMessages().size());
    }
    @Test
    public void testSendShipmentReport() {
        final TrackerEvent e = createTrackerEvent();

        //update shipment
        final Arrival arr = new Arrival();
        arr.setDevice(device);
        arr.setShipment(shipment);
        arr.setTrackerEventId(e.getId());
        arr.setDate(new Date());
        context.getBean(ArrivalDao.class).save(arr);

        shipment.setArrivalDate(arr.getDate());
        context.getBean(ShipmentDao.class).save(shipment);

        final User u = createUser("junit@smarttrace.com.au");

        //check not send for empty user list
        final List<User> users = new LinkedList<>();
        service.sendShipmentReport(shipment, users);

        assertFalse(service.isArrivalReportSent(shipment));
        //check send to not empty user list
        users.add(u);
        service.sendShipmentReport(shipment, users);

        assertTrue(service.isArrivalReportSent(shipment));
        assertEquals(1, context.getBean(MockEmailService.class).getMessages().size());

        //check not double sent
        service.sendShipmentReport(shipment, users);

        assertEquals(1, context.getBean(MockEmailService.class).getMessages().size());
    }
    /**
     * @return tracker event.
     */
    private TrackerEvent createTrackerEvent() {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(device);
        e.setShipment(shipment);
        e.setType(TrackerEventType.AUT);
        e.setTime(new Date());
        e.setCreatedOn(new Date());
        return context.getBean(TrackerEventDao.class).save(e);
    }
    /**
     * @param e tracker event.
     * @return
     */
    private Alert createAlert(final TrackerEvent e) {
        final Alert a = new Alert();
        a.setDevice(e.getDevice());
        a.setShipment(e.getShipment());
        a.setDate(new Date());
        a.setTrackerEventId(e.getId());
        a.setType(AlertType.Battery);
        return context.getBean(AlertDao.class).save(a);
    }
    /**
     * @param email user email.
     * @return the user.
     */
    private User createUser(final String email) {
        final User u = new User();
        u.setActive(true);
        u.setCompany(company);
        u.setEmail(email);
        u.setFirstName(email);
        u.setLastName("JUnit");
        return context.getBean(UserDao.class).save(u);
    }
}
