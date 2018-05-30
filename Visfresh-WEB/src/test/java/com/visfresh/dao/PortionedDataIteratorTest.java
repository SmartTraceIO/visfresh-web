/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PortionedDataIteratorTest {
    /**
     * Default constructor.
     */
    public PortionedDataIteratorTest() {
        super();
    }

    @Test
    public void testIteration() {
        final List<Object> data = new LinkedList<>();
        final PartionedDataIterator<Object> iter = createIterator(data, 3);

        data.add(new Object());
        data.add(new Object());
        data.add(new Object());
        data.add(new Object());

        assertTrue(iter.hasNext());
        iter.next();
        assertTrue(iter.hasNext());
        iter.next();
        assertTrue(iter.hasNext());
        iter.next();
        assertTrue(iter.hasNext());
        iter.next();
        assertFalse(iter.hasNext());
    }
    @Test
    public void testNext() {
        final List<Object> data = new LinkedList<>();
        final PartionedDataIterator<Object> iter = createIterator(data, 3);

        data.add(new Object());

        assertNotNull(iter.next());
        assertFalse(iter.hasNext());

        try {
            iter.next();
            throw new AssertionFailedError("Exception should be thrown");
        } catch (final Exception e) {
            //normal
        }
    }
    @Test
    public void testEmptyStream() {
        final List<Object> data = new LinkedList<>();
        final PartionedDataIterator<Object> iter = createIterator(data, 3);

        try {
            iter.next();
            throw new AssertionFailedError("Exception should be thrown");
        } catch (final Exception e) {
            //normal
        }
        assertFalse(iter.hasNext());
    }
    /**
     * @param data
     * @return
     */
    private PartionedDataIterator<Object> createIterator(final List<Object> data, final int originLimit) {
        return new PartionedDataIterator<>(
            (page, limit) -> {
                final int offset = (page - 1)* limit;
                if (offset >= data.size()) {
                    return new LinkedList<>();
                }
                return data.subList(offset, Math.min(offset + limit, data.size()));
            }, originLimit);
    }
}
