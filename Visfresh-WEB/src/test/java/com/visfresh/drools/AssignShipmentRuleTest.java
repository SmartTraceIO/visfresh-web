/**
 *
 */
package com.visfresh.drools;

import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.mock.MockDaoConfig;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.mock.MockSmsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AssignShipmentRuleTest {
    private static AnnotationConfigApplicationContext context;

    private DroolsRuleEngine ruleEngine;
    private DeviceDao deviceDao;
    private ShipmentDao shipmentDao;
    private AlertDao alertDao;
    private ArrivalDao arrivalDao;
    private NotificationDao notificationDao;
    private TrackerEventDao trackerEventDao;

    private CompanyDao companyDao;

    private Company company;

    /**
     *
     */
    public AssignShipmentRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        ruleEngine = context.getBean(DroolsRuleEngine.class);
        //disable OpenJTS rule.
        final OpenJtsRule rule = (OpenJtsRule) ruleEngine.getRule(OpenJtsRule.NAME);
        rule.setEnabled(false);

        deviceDao = context.getBean(DeviceDao.class);
        shipmentDao = context.getBean(ShipmentDao.class);
        alertDao = context.getBean(AlertDao.class);
        arrivalDao = context.getBean(ArrivalDao.class);
        notificationDao = context.getBean(NotificationDao.class);
        trackerEventDao = context.getBean(TrackerEventDao.class);
        companyDao = context.getBean(CompanyDao.class);

        //create shared company
        final Company c = new Company();
        c.setName("JUnit company");
        c.setDescription("Any Description");
        company = companyDao.save(c);
    }

    @Test
    public void testAllRulesWorks() {
        final Device device = createDevice("90324870987");

        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(13.14);
        e.setLongitude(15.16);
        e.setTemperature(20.4);
        e.setTime(new Date());
        e.setType("INIT");
        e.setDevice(device);

        ruleEngine.processTrackerEvent(e);
    }

    /**
     * @param imei
     * @return
     */
    protected Device createDevice(final String imei) {
        final Device d = new Device();
        d.setName("Test Device");
        d.setImei(imei);
        d.setId(d.getImei() + ".1234");
        d.setSn("456");
        d.setCompany(company);
        d.setDescription("Test device");
        return deviceDao.save(d);
    }
    @After
    public void tearDown() {
        notificationDao.deleteAll();
        trackerEventDao.deleteAll();
        arrivalDao.deleteAll();
        alertDao.deleteAll();
        shipmentDao.deleteAll();
        deviceDao.deleteAll();
        companyDao.deleteAll();
    }
    @BeforeClass
    public static void beforeClass() {
        final AnnotationConfigApplicationContext ctxt = new AnnotationConfigApplicationContext();
        ctxt.scan(MockDaoConfig.class.getPackage().getName(),
                DroolsRuleEngine.class.getPackage().getName(),
                MockSmsService.class.getPackage().getName());
        ctxt.refresh();
        context = ctxt;
    }
    @AfterClass
    public static void afterClass() {
        if (context != null) {
            context.destroy();
        }
    }
}
