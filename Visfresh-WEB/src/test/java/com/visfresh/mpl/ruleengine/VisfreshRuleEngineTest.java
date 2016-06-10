/**
 *
 */
package com.visfresh.mpl.ruleengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.visfresh.rules.RuleContext;
import com.visfresh.rules.TrackerEventRule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class VisfreshRuleEngineTest extends VisfreshRuleEngine {
    private RuleContext context = new RuleContext(null, null);
    /**
     * Default constructor.
     */
    public VisfreshRuleEngineTest() {
        super();
    }


    @Test
    public void testLoadPriorities() {
        assertEquals(150, getPriority("VeryOldEvent").intValue());
        assertEquals(150, getPriority("CorrectMovingControll").intValue());
        assertEquals(100, getPriority("AssignShipment").intValue());
        assertEquals(50, getPriority("AutoStartShipment").intValue());
        assertEquals(49, getPriority("AutoDetectEndLocation").intValue());
    }
    @Test
    public void testNotAcceptNotRun() {
        final AtomicBoolean isRun = new AtomicBoolean();

        setRule("1", new TrackerEventRule() {
            @Override
            public boolean handle(final RuleContext context) {
                isRun.set(true);
                return false;
            }
            @Override
            public boolean accept(final RuleContext context) {
                return false;
            }
        });

        invokeRules(context);
        assertFalse(isRun.get());
    }

    @Test
    public void testRunIfAccept() {
        final AtomicBoolean isRun = new AtomicBoolean();

        setRule("1", new TrackerEventRule() {
            @Override
            public boolean handle(final RuleContext context) {
                isRun.set(true);
                return false;
            }
            @Override
            public boolean accept(final RuleContext context) {
                return true;
            }
        });

        invokeRules(context);
        assertTrue(isRun.get());
    }

    @Test
    public void testRerunAll() {
        final AtomicInteger numRuns = new AtomicInteger();

        final TrackerEventRule rule = new TrackerEventRule() {
            @Override
            public boolean handle(final RuleContext context) {
                numRuns.incrementAndGet();
                return numRuns.get() == 2;
            }
            @Override
            public boolean accept(final RuleContext context) {
                return true;
            }
        };

        setRule("1", rule);
        setRule("2", rule);

        invokeRules(context);
        assertEquals(4, numRuns.get());
    }
    @Test
    public void testPriority() {
        final List<TrackerEventRule> results = new LinkedList<>();

        final TrackerEventRule r1 = new TrackerEventRule() {
            @Override
            public boolean handle(final RuleContext context) {
                results.add(this);
                return false;
            }
            @Override
            public boolean accept(final RuleContext context) {
                return true;
            }
            /* (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString() {
                return "r1";
            }
        };

        final TrackerEventRule r2 = new TrackerEventRule() {
            @Override
            public boolean handle(final RuleContext context) {
                results.add(this);
                return false;
            }
            @Override
            public boolean accept(final RuleContext context) {
                return true;
            }
            /* (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString() {
                return "r2";
            }
        };

        setRule("1", r1);
        setRule("2", r2);

        setPriority("1", 100);
        setPriority("2", 0);

        invokeRules(context);
        assertEquals(0, results.indexOf(r1));
        assertEquals(1, results.indexOf(r2));

        //change priorities
        results.clear();

        setPriority("1", 0);
        setPriority("2", 100);

        invokeRules(context);
        assertEquals(1, results.indexOf(r1));
        assertEquals(0, results.indexOf(r2));
    }
}
