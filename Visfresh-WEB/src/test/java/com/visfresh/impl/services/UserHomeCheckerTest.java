/**
 *
 */
package com.visfresh.impl.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserHomeCheckerTest extends UserHomeChecker {
    private String alarm;

    private String savedUserHome;
    /**
     * Default constructor.
     */
    public UserHomeCheckerTest() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.services.UserHomeChecker#sendAlarm(java.lang.String)
     */
    @Override
    protected void sendAlarm(final String message) {
        this.alarm = message;
    }

    @Before
    public void setUp() {
        savedUserHome = System.getProperty("user.home");
    }
    @After
    public void tearDown() {
        System.setProperty("user.home", savedUserHome);
    }

    @Test
    public void testCorrectUserHome() {
        System.setProperty("user.home", "abracadabra");
        checkUserHome();

        assertNull(alarm);
    }
    @Test
    public void testIncorrectUserHome() {
        System.setProperty("user.home", "abra root cadabra");
        checkUserHome();

        assertNotNull(alarm);
    }
}
