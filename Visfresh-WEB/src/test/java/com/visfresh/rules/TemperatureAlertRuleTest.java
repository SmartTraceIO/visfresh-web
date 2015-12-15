/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;
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
        p.setCompany(shipment.getCompany());
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
        final AlertRule r = new AlertRule(AlertType.Hot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        DeviceState state = new DeviceState();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();
        state = new DeviceState();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //normal temperature occurence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature - 3);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));

        assertEquals(0, alertDao.findAll(null, null, null).size());
    }
    @Test
    public void testCumulativeHotTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final AlertRule r = new AlertRule(AlertType.Hot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final DeviceState state = new DeviceState();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 5);
        rule.handle(new RuleContext(e, state));

        //normal temperature occurrence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature - 3);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 5);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
    }

    @Test
    public void testCriticalHotTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final AlertRule r = new AlertRule(AlertType.CriticalHot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        DeviceState state = new DeviceState();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();
        state = new DeviceState();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //normal temperature occurence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature - 3);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));

        assertEquals(0, alertDao.findAll(null, null, null).size());
    }
    @Test
    public void testCumulativeCriticalHotTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final AlertRule r = new AlertRule(AlertType.CriticalHot);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final DeviceState state = new DeviceState();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 5);
        rule.handle(new RuleContext(e, state));

        //normal temperature occurrence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature - 3);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature + 5);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature + 1);
        rule.handle(new RuleContext(e, state));
    }
    @Test
    public void testColdTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final AlertRule r = new AlertRule(AlertType.Cold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        DeviceState state = new DeviceState();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();
        state = new DeviceState();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //normal temperature occurence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature + 3);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));

        assertEquals(0, alertDao.findAll(null, null, null).size());
    }
    @Test
    public void testCumulativeColdTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final AlertRule r = new AlertRule(AlertType.Cold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final DeviceState state = new DeviceState();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, state));

        //normal temperature occurrence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature + 3);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
    }

    @Test
    public void testCriticalColdTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final AlertRule r = new AlertRule(AlertType.CriticalCold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(false);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        DeviceState state = new DeviceState();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();
        state = new DeviceState();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        //normal temperature occurence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature + 3);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));

        assertEquals(0, alertDao.findAll(null, null, null).size());
    }
    @Test
    public void testCumulativeCriticalColdTemperatureAlert() {
        final long minute = 60000l;
        final double temperature = 10.;
        final int timeOutMinutes = 30;

        //create alert rule
        final AlertRule r = new AlertRule(AlertType.CriticalCold);
        r.setTemperature(temperature);
        r.setTimeOutMinutes(timeOutMinutes);
        r.setCumulativeFlag(true);

        alertProfile.getAlertRules().add(r);
        alertProfileDao.save(alertProfile);

        //check first iteration
        final long startTime = System.currentTimeMillis() - timeOutMinutes * minute - 3;
        final DeviceState state = new DeviceState();

        TrackerEvent e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, state));

        //normal temperature occurrence should flush state
        e = createEvent(startTime + 12 * minute, TrackerEventType.AUT, temperature + 3);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 11 * minute, TrackerEventType.AUT, temperature - 5);
        rule.handle(new RuleContext(e, state));
        assertEquals(0, alertDao.findAll(null, null, null).size());

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
        assertEquals(1, alertDao.findAll(null, null, null).size());

        //check not handles already handled
        alertDao.deleteAll();

        e = createEvent(startTime, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));

        e = createEvent(startTime + 31 * minute, TrackerEventType.AUT, temperature - 1);
        rule.handle(new RuleContext(e, state));
    }
    /**
     * @param date date.
     * @param type type.
     * @param temperature temperature.
     * @return tracker event.
     */
    private TrackerEvent createEvent(final long date, final TrackerEventType type, final double temperature) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(100);
        e.setLatitude(10);
        e.setLongitude(10);
        e.setTemperature(20.4);
        e.setType(type);
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setTime(new Date(date));
        e.setTemperature(temperature);
        return context.getBean(TrackerEventDao.class).save(e);
    }
}