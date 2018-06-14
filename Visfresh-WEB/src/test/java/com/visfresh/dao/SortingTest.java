/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SortingTest {

    /**
     * Default constructor.
     */
    public SortingTest() {
        super();
    }

    @Test
    public void testAddPropertiesByConstructor() {
        final String prop1 = "a";
        final String prop2 = "b";

        final Sorting s = new Sorting(prop1, prop2, prop1);
        assertEquals(3, s.getSortProperties().length);
        assertTrue(s.isAscentDirection(prop1));
        assertTrue(s.isAscentDirection(prop2));
    }
    @Test
    public void testAddPropertiesByConstructorWithAscent() {
        final String prop1 = "a";
        final String prop2 = "b";

        final Sorting s = new Sorting(false, prop1, prop2, prop1);
        assertEquals(3, s.getSortProperties().length);
        assertFalse(s.isAscentDirection(prop1));
        assertFalse(s.isAscentDirection(prop2));
    }
    @Test
    public void testAddPropertiy() {
        final String prop1 = "a";
        final String prop2 = "b";

        final Sorting s = new Sorting();
        s.addSortProperty(prop1);
        s.addSortProperty(prop2);
        s.addSortProperty(prop1);

        assertEquals(3, s.getSortProperties().length);
        assertTrue(s.isAscentDirection(prop1));
        assertTrue(s.isAscentDirection(prop2));
    }
    @Test
    public void testAddPropertiyWithAscent() {
        final String prop1 = "a";
        final String prop2 = "b";

        final Sorting s = new Sorting();
        s.addSortProperty(prop1, true);
        s.addSortProperty(prop2, true);
        s.addSortProperty(prop1, false);

        assertEquals(3, s.getSortProperties().length);
        assertFalse(s.isAscentDirection(prop1));
        assertTrue(s.isAscentDirection(prop2));
    }
}
