/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.ActionTaken;
import com.visfresh.entities.ActionTakenView;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ActionTakenDaoTest extends BaseCrudTest<ActionTakenDao, ActionTakenView, ActionTaken, Long> {
    private Shipment shipment;
    private TemperatureAlert alert;
    private User confirmedBy;
    private User verifiedBy;

    /**
     * Default constructor.
     */
    public ActionTakenDaoTest() {
        super(ActionTakenDao.class);
    }

    @Before
    public void beforeTest() {
        final Device d = new Device();
        d.setCompany(sharedCompany);
        final String imei = "932487032487";
        d.setImei(imei);
        d.setDescription("JUnit device");
        d.setName("Test device");
        context.getBean(DeviceDao.class).save(d);

        final AlertProfile ap = new AlertProfile();
        ap.setName("ActionTakenTestProfile");
        ap.setCompany(sharedCompany);

        final TemperatureRule rule = new TemperatureRule();
        rule.setCumulativeFlag(true);
        rule.setMaxRateMinutes(77);
        rule.setTemperature(-16.);
        rule.setMaxRateMinutes(11);
        rule.setTimeOutMinutes(14);
        rule.setType(AlertType.Cold);
        ap.getAlertRules().add(rule);
        context.getBean(AlertProfileDao.class).save(ap);

        shipment = createShipment(d, ap);

        //create alert
        alert = createAlert(AlertType.Cold, new Date(System.currentTimeMillis() - 10000000l));
        alert.setRuleId(rule.getId());
        context.getBean(AlertDao.class).save(alert);

        //create users
        confirmedBy = createUser("User1");
        verifiedBy = createUser("User2");
    }

    @Test
    public void testNullVerifiedBy() {
        final ActionTaken e = createTestEntity();
        e.setVerifiedBy(null);
        dao.save(e);

        final ActionTakenView v = dao.findOne(e.getId());
        assertNull(v.getVerifiedBy());
        assertNull(v.getVerifiedByEmail());
        assertEquals(0, v.getVerifiedByName().length());
    }
    @Test
    public void testVerifiedTime() {
        final Date time = new Date(System.currentTimeMillis() - 10000000000l);
        final ActionTaken e = createTestEntity();
        e.setVerifiedTime(time);
        dao.save(e);

        final ActionTakenView v = dao.findOne(e.getId());
        assertTrue(Math.abs(time.getTime() - v.getVerifiedTime().getTime()) < 1000l);
    }
    @Test
    public void testFindByIdAndCompany() {
        final ActionTaken e = createTestEntity();
        dao.save(e);

        assertNotNull(dao.findOne(e.getId(), sharedCompany));
        assertNull(dao.findOne(e.getId(), createCompany("Other")));
    }
    @Test
    public void testFindByShipment() {
        final Shipment s1 = createShipment(shipment.getDevice(), shipment.getAlertProfile());
        final Shipment s2 = createShipment(shipment.getDevice(), shipment.getAlertProfile());

        final TemperatureAlert alert = createAlert(AlertType.Hot, new Date(), s1);

        //create action takes for first shipment
        final ActionTaken e1 = createTestEntity();
        e1.setAlert(alert.getId());
        dao.save(e1);

        final ActionTaken e2 = createTestEntity();
        e2.setAlert(alert.getId());
        dao.save(e2);

        //check result
        assertEquals(2, dao.findByShipment(s1).size());
        assertEquals(0, dao.findByShipment(s2).size());
    }

    /**
     * @param name
     * @return
     */
    private User createUser(final String name) {
        final User u = new User();
        u.setActive(true);
        u.setCompany(sharedCompany);
        u.setEmail(name + "@junit.ru");
        u.setFirstName(name);
        return context.getBean(UserDao.class).save(u);
    }

    /**
     * @param d
     * @param ap TODO
     * @return
     */
    private Shipment createShipment(final Device d, final AlertProfile ap) {
        Shipment s = new Shipment();
        s.setCompany(sharedCompany);
        s.setDevice(d);
        s.setAlertProfile(ap);
        s = getContext().getBean(ShipmentDao.class).save(s);
        return s;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected ActionTaken createTestEntity() {
        final ActionTaken at = new ActionTaken();
        at.setAction(new CorrectiveAction("Do anything"));
        at.setAlert(alert.getId());
        at.setComments("Any comments");
        at.setConfirmedBy(confirmedBy.getId());
        at.setVerifiedBy(verifiedBy.getId());
        at.setTime(new Date());
        return at;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final ActionTaken at) {
        assertNotNull(at.getId());
        assertEquals("Do anything", at.getAction().getAction());
        assertFalse(at.getAction().isRequestVerification());

        assertEquals(alert.getId(), at.getAlert());
        assertEquals("Any comments", at.getComments());
        assertEquals(confirmedBy.getId(), at.getConfirmedBy());
        assertEquals(verifiedBy.getId(), at.getVerifiedBy());
        assertNotNull(at.getTime());

        assertTrue(at instanceof ActionTakenView);

        //view fields:
        final ActionTakenView v = (ActionTakenView) at;
        assertTrue(Math.abs(v.getAlertTime().getTime() - alert.getDate().getTime()) < 2000l);
        assertEquals(confirmedBy.getFirstName(), v.getConfirmedByName());
        assertEquals(confirmedBy.getEmail(), v.getConfirmedByEmail());
        assertEquals(verifiedBy.getFirstName(), v.getVerifiedByName());
        assertEquals(verifiedBy.getEmail(), v.getVerifiedByEmail());
        assertEquals(shipment.getDevice().getSn(), v.getShipmentSn());
        assertEquals(shipment.getTripCount(), v.getShipmentTripCount());

        //rule fields
        final TemperatureRule rule = shipment.getAlertProfile().getAlertRules().get(0);
        assertEquals(rule.getTemperature(), ((TemperatureRule) v.getAlertRule()).getTemperature(), 0.001);
        assertEquals(rule.getTimeOutMinutes(), ((TemperatureRule) v.getAlertRule()).getTimeOutMinutes());
        assertEquals(rule.getType(), ((TemperatureRule) v.getAlertRule()).getType());
    }

    /**
     * @param type alert type.
     * @param date date.
     * @return
     */
    protected TemperatureAlert createAlert(final AlertType type, final Date date) {
        return createAlert(type, date, shipment);
    }

    /**
     * @param type
     * @param date
     * @param s
     * @return
     */
    protected TemperatureAlert createAlert(final AlertType type, final Date date, final Shipment s) {
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(date);
        alert.setDevice(s.getDevice());
        alert.setShipment(s);
        alert.setType(type);
        alert.setTemperature(100);
        alert.setCumulative(true);
        alert.setMinutes(15);
        alert.setRuleId(77l);
        return context.getBean(AlertDao.class).save(alert);
    }
    /**
     * @param company
     * @param imei
     * @return
     */
    protected Device createDevice(final Company company, final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setActive(true);
        d.setName("JUnit-" + imei);
        d.setCompany(company);
        return context.getBean(DeviceDao.class).save(d);
    }
}
