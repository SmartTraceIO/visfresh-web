/**
 *
 */
package com.visfresh.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PushBackIterator<E> implements Iterator<E> {
    private final List<E> pushedBack = new LinkedList<E>();
    private final Iterator<E> iter;

    /**
     * @param iter iterator.
     */
    public PushBackIterator(final Iterator<E> iter) {
        this.iter = iter;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return !pushedBack.isEmpty() || iter.hasNext();
    }
    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public E next() {
        if (!pushedBack.isEmpty()) {
            return pushedBack.remove(0);
        }
        return iter.next();
    }
    /**
     * @param e element to push back.
     */
    public void pushBack(final E e) {
        pushedBack.add(e);
    }
}
