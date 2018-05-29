/**
 *
 */
package com.visfresh.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PushBackIteratorTest {
    /**
     * Default constructor.
     */
    public PushBackIteratorTest() {
        super();
    }

    @Test
    public void testEmptyIterator() {
        final Iterator<Object> iter = new PushBackIterator<>(new LinkedList<Object>().iterator());
        assertFalse(iter.hasNext());

        try {
            iter.next();
            throw new AssertionFailedError("Exception should be thown");
        } catch (final RuntimeException e) {
            //normal
        }
    }
    @Test
    public void testOnlyPushedBack() {
        final LinkedList<Object> list = new LinkedList<Object>();
        final PushBackIterator<Object> iter = new PushBackIterator<>(list.iterator());

        iter.pushBack(new Object());
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());

        try {
            iter.next();
            throw new AssertionFailedError("Exception should be thown");
        } catch (final RuntimeException e) {
            //normal
        }
    }
    @Test
    public void testOnlyOriginIterator() {
        final LinkedList<Object> list = new LinkedList<Object>();
        list.add(new Object());
        final PushBackIterator<Object> iter = new PushBackIterator<>(list.iterator());

        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());

        try {
            iter.next();
            throw new AssertionFailedError("Exception should be thown");
        } catch (final RuntimeException e) {
            //normal
        }
    }
    @Test
    public void testCombinedIterator() {
        final LinkedList<Object> list = new LinkedList<Object>();
        list.add(new Object());
        final PushBackIterator<Object> iter = new PushBackIterator<>(list.iterator());
        iter.pushBack(new Object());

        assertTrue(iter.hasNext());
        iter.next();
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }
}
