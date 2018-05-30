/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PartionedDataIterator<E> implements Iterator<E> {
    public interface DataProvider<EE> {
        /**
         * @param page data page.
         * @param limit the data limit.
         * @return data collection. Attention!!! should return full possible data by
         * given limitation, because if the returned data size will less then limit
         * value the iteration will stopped and next step will not call.
         */
        Collection<EE> getNextPart(int page, int limit);
    }

    private final DataProvider<E> provider;
    private final int limit;
    private int page = 0;
    private boolean stopFetchFromDb;
    private Iterator<E> nextPart;

    /**
     * @param provider data provider.
     * @param limit fetch limit.
     */
    public PartionedDataIterator(final DataProvider<E> provider, final int limit) {
        super();
        this.provider = provider;
        this.limit = limit;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        if (shouldFetchNextPart()) {
            fetchNextPart();
        }
        return nextPart.hasNext();
    }
    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public E next() {
        if (shouldFetchNextPart()) {
            fetchNextPart();
        }
        return nextPart.next();
    }
    private boolean shouldFetchNextPart() {
        return nextPart == null || (!nextPart.hasNext() && !stopFetchFromDb);
    }
    private void fetchNextPart() {
        final Collection<E> part = provider.getNextPart(page + 1, limit);
        if (part.size() < limit) {
            stopFetchFromDb = true;
        }

        nextPart = part.iterator();
        page++;
    }
}
