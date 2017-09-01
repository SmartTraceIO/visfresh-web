/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.SingleShipmentBeanDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.services.RuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentDataRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Shipment shipment;
    private SingleShipmentBeanDao dao;

    /**
     * Default constructor.
     */
    public SingleShipmentDataRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        rule = context.getBean(RuleEngine.class).getRule(SingleShipmentDataRule.NAME);
        shipment = createDefaultShipment(ShipmentStatus.InProgress, createDevice("9283470987"));
        dao = context.getBean(SingleShipmentBeanDao.class);
    }
    @Test
    public void testAccept() {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);

        final RuleContext req = new RuleContext(e, new SessionHolder());
        //shipment not set
        assertFalse(rule.accept(req));

        //shipment has set
        e.setShipment(shipment);
        assertTrue(rule.accept(req));
    }
    @Test
    public void testHandle() {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(shipment.getDevice());
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        e.setShipment(shipment);

        final RuleContext context = new RuleContext(e, new SessionHolder());
        //always false
        assertFalse(rule.handle(context));

        //test single shipment data has created.
        assertNotNull(dao.getShipmentBeanIncludeSiblings(shipment.getId()));
        assertEquals(1, dao.getShipmentBeanIncludeSiblings(shipment.getId()).size());

        //test double handle
        assertTrue(rule.accept(context));
    }
}
