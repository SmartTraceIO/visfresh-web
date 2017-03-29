/**
 *
 */
package com.visfresh.utils;

import static org.junit.Assert.assertEquals;
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
        final String trace = StringUtils.getSteackTrace(new Exception(), 100000);
        assertTrue(trace.contains("java.lang.Exception"));
        assertTrue(trace.contains(StringUtilsTest.class.getName()));
    }
    @Test
    public void testGetSteackTraceUnlimitedLines() {
        final String trace = StringUtils.getSteackTrace(new Exception(), -1);
        assertTrue(trace.contains("java.lang.Exception"));
        assertTrue(trace.contains(StringUtilsTest.class.getName()));
    }
    @Test
    public void testMaxLines() {
        final String trace = StringUtils.getSteackTrace(new Exception(), 3).trim();
        assertEquals(3, trace.split("\n").length);
    }
}
