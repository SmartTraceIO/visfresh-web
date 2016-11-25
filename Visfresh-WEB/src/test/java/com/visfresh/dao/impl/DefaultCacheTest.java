/**
 *
 */
package com.visfresh.dao.impl;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultCacheTest {
    private DefaultCache<Object, String> cache;

    /**
     * Default constructor.
     */
    public DefaultCacheTest() {
        super();
    }

    @Before
    public void setUp() {
        cache = new DefaultCache<>("JUnit", 1, 1000, 1000);
        cache.initialize();
    }
    @After
    public void tearDown() {
        cache.destroy();
    }

    @Test
    public void testCacheNonSerializable() {
        final Object obj = new Object();

        cache.put("7", obj);

        assertEquals(obj, cache.get("7"));
    }
}
