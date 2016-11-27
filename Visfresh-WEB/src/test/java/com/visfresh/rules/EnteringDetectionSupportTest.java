/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.rules.state.ShipmentSession;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EnteringDetectionSupportTest {
    private ShipmentSession session;

    /**
     * Default constructor.
     */
    public EnteringDetectionSupportTest() {
        super();
    }

    @Before
    public void setUp() {
        session = new ShipmentSession(7l);
    }

    @Test
    public void testHandleEntered() {
        final int maxNum = 3;

        final EnteringDetectionSupport support = new EnteringDetectionSupport(maxNum, "JUnit");
        assertFalse(support.handleEntered(session));
        assertFalse(support.handleEntered(session));
        assertTrue(support.handleEntered(session));
    }
    @Test
    public void testInControl() {
        final EnteringDetectionSupport support = new EnteringDetectionSupport("JUnit");
        assertFalse(support.isInControl(session));

        support.handleEntered(session);
        assertTrue(support.isInControl(session));
    }
    @Test
    public void testClearInControl() {
        final EnteringDetectionSupport support = new EnteringDetectionSupport("JUnit");

        support.handleEntered(session);
        assertTrue(support.isInControl(session));

        support.clearInControl(session);
        assertFalse(support.isInControl(session));
    }
}
