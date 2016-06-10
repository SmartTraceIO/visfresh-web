/**
 *
 */
package com.visfresh.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class StringUtilsTest {
    /**
     * Default constructor.
     */
    public StringUtilsTest() {
        super();
    }

    @Test
    public void testGetSteackTrace() {
        final String trace = StringUtils.getSteackTrace(new Exception());
        assertTrue(trace.contains("java.lang.Exception"));
        assertTrue(trace.contains(StringUtilsTest.class.getName()));
    }
}
