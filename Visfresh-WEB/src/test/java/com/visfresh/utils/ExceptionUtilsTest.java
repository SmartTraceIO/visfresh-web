/**
 *
 */
package com.visfresh.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ExceptionUtilsTest {

    /**
     * Default constructor.
     */
    public ExceptionUtilsTest() {
        super();
    }

    @Test
    public void testContainsException() {
        final Exception e = new Exception("DEF");
        e.initCause(new SQLException("ABC"));

        assertFalse(ExceptionUtils.containsException(e, RuntimeException.class));
        assertTrue(ExceptionUtils.containsException(e, SQLException.class));
    }
    @Test
    public void testContainsExceptionNullParent() {
        final Exception e = new Exception("DEF");
        e.initCause(null);

        assertFalse(ExceptionUtils.containsException(e, RuntimeException.class));
        assertTrue(ExceptionUtils.containsException(e, Exception.class));
    }
    @Test
    public void testContainsExceptionRecursiveParent() {
        final Exception e = new Exception("DEF");

        assertFalse(ExceptionUtils.containsException(e, RuntimeException.class));
        assertTrue(ExceptionUtils.containsException(e, Exception.class));
    }

    @Test
    public void testGetSteackTrace() {
        final String trace = ExceptionUtils.getSteackTraceAsString(new Exception(), 100000);
        assertTrue(trace.contains("java.lang.Exception"));
        assertTrue(trace.contains(ExceptionUtilsTest.class.getName()));
    }
    @Test
    public void testGetSteackTraceUnlimitedLines() {
        final String trace = ExceptionUtils.getSteackTraceAsString(new Exception(), -1);
        assertTrue(trace.contains("java.lang.Exception"));
        assertTrue(trace.contains(ExceptionUtilsTest.class.getName()));
    }
    @Test
    public void testMaxLines() {
        final String trace = ExceptionUtils.getSteackTraceAsString(new Exception(), 3).trim();
        assertEquals(3, trace.split("\n").length);
    }
}
