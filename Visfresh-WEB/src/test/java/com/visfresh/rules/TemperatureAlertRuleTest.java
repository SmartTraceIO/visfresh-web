/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TemperatureAlertRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Shipment shipment;
    private AlertProfile alertProfile;

    private AlertProfileDao alertProfileDao;
    private AlertDao alertDao;

    /**
     * Default constructor.
     */
    public TemperatureAlertRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        //create rule
        alertProfileDao = context.getBean(AlertProfileDao.class);
        alertDao = context.getBean(AlertDao.class);
        rule = engine.getRule(TemperatureAlertRule.NAME);

        //create shipment.
        final Device device = createDevice("90324870987");
        shipment = createDefaultShipment(ShipmentStatus.InProgress, device);

        //create alert profile.
        final AlertProfile p = new AlertProfile();
        p.setCompany(shipment.getCompanyId());
        p.setName("JUnit");
        p.setDescription("JUnit alerts");

        alertProfile = alertProfileDao.save(p);
        shipment.setAlertProfile(p);
        context.getBean(ShipmentDao.class).save(shipment);
    }

    @Test
    public void testHotTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.Hot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();
        mgr = new SessionHolder();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //normal temperature occurence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature - 3);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));

        assertEquals(0, alertDao.findAll(null, null, null).size());
    }
    @Test
    public void testEventIdSet() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.Hot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        final Alert alert = alertDao.findAll(null, null, null).get(0);
        assertEquals(e.getId(), alert.getTrackerEventId());
    }
    @Test
    public void testRuleIdSet() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.Hot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        final TemperatureAlert alert = (TemperatureAlert) alertDao.findAll(null, null, null).get(0);
        assertEquals(r.getId(), alert.getRuleId());
    }
    @Test
    public void testCumulativeHotTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.Hot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 5);
        rule.handle(new RuleContext(e, mgr));

        //normal temperature occurrence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature - 3);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 5);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
    }

    @Test
    public void testCriticalHotTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.CriticalHot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();
        mgr = new SessionHolder();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //normal temperature occurence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature - 3);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));

        assertEquals(0, alertDao.findAll(null, null, null).size());
    }
    @Test
    public void testCumulativeCriticalHotTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.CriticalHot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 5);
        rule.handle(new RuleContext(e, mgr));

        //normal temperature occurrence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature - 3);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 5);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, mgr));
    }
    @Test
    public void testColdTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.Cold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        alertDao.deleteAll();

        //normal temperature occurence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature + 3);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));

        assertEquals(0, alertDao.findAll(null, null, null).size());
    }

    @Test
    public void testCumulativeColdTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.Cold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, mgr));

        //normal temperature occurrence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature + 3);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
    }

    @Test
    public void testCriticalColdTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.CriticalCold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();
        mgr = new SessionHolder();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //normal temperature occurence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature + 3);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));

        assertEquals(0, alertDao.findAll(null, null, null).size());
    }
    @Test
    public void testCumulativeCriticalColdTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.CriticalCold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, mgr));

        //normal temperature occurrence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature + 3);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
    }
    @Test
    public void testNotCumulativeFlag() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.CriticalCold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, mgr));
        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));

        final TemperatureAlert a = (TemperatureAlert) alertDao.findAll(null, null, null).get(0);
        assertFalse(a.isCumulative());
    }
    @Test
    public void testCumulativeFlag() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.CriticalCold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, mgr));
        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));

        final TemperatureAlert a = (TemperatureAlert) alertDao.findAll(null, null, null).get(0);
        assertTrue(a.isCumulative());
    }
    @Test
    public void testRepeatTimeOutCummulative() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.CriticalCold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);
        r.setMaxRateMinutes(1);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - 10 * timeOutMinutes * minute - 6;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, mgr));
        e = createEvent(startTime + (timeOutMinutes + 1) * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));

        assertEquals(1, alertDao.findAll(null, null, null).size());

        //repeat
        e = createEvent(startTime + (timeOutMinutes + 1) * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //repeat
        e = createEvent(startTime + 3 * timeOutMinutes * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(2, alertDao.findAll(null, null, null).size());
    }
    @Test
    public void testRepeatTimeOut() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final TemperatureRule r = new TemperatureRule(AlertType.CriticalCold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setMaxRateMinutes(1);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - 2 * timeOutMinutes * minute - 6;
        final SessionHolder mgr = new SessionHolder();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, mgr));
        e = createEvent(startTime + (timeOutMinutes + 1) * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));

        assertEquals(1, alertDao.findAll(null, null, null).size());

        //repeat
        e = createEvent(startTime + (timeOutMinutes + 1) * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //repeat
        e = createEvent(startTime + (2 * timeOutMinutes + 1) * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, mgr));
        assertEquals(2, alertDao.findAll(null, null, null).size());
    }
    /**
     * @param date date.
     * @param type type.
     * @param temperature temperature.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final long date, final TrackerEventType type, final double temperature) {
        final TrackerEvent e = createEventNotSave(date, type, temperature);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    /**
     * @param date date.
     * @param type type.
     * @param temperature temperature.
     * @return tracker event.
     */
    private TrackerEvent createEventNotSave(final long date,
            final TrackerEventType type, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(10.);
        e.setLongitude(10.);
        e.setTemperature(20.4);
        e.setType(type);
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setTime(new Date(date));
        e.setTemperature(temperature);
        return e;
    }
}
