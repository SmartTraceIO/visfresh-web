/**
 *
 */
package com.visfresh.utils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class CollectionUtils {
    /**
     * Default constructor.
     */
    private CollectionUtils() {
        super();
    }

    /**
     * Groups the list values by groups. This method not changes the origin values ordering.
     * @param <T> the type of objects.
     * @param list the list of objects.
     * @param grouper the grouper.
     * @return the map of separated objects.
     */
    public static <T> Map<String, List<T>> group(final List<T> list, final Grouper<T> grouper) {
        final Map<String, List<T>> map = new LinkedHashMap<>();
        for (final T t : list) {
            final String key = grouper.getGroup(t);

            List<T> group = map.get(key);
            if (group == null) {
                group = new LinkedList<>();
                map.put(key, group);
            }
            group.add(t);
        }
        return map;
    }
    /**
     * @param list
     */
    public static <ID extends Serializable & Comparable<ID>, E extends EntityWithId<ID>> void sortById(
            final List<E> list) {
        Collections.sort(list, new Comparator<E>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final E o1, final E o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
    }
    /**
     * @param l1 first list.
     * @param l2 second list.
     * @return true if lists are equals.
     */
    public static boolean equals(final List<?> l1, final List<?> l2) {
        if (l1 == l2) {
            return true;
        }
        if (l1 == null || l2 == null || l1.size() != l2.size()) {
            return false;
        }

        final Iterator<?> iter1 = l1.iterator();
        final Iterator<?> iter2 = l2.iterator();
        while (iter1.hasNext()) {
            if (!Objects.equals(iter1.next(), iter2.next())) {
                return false;
            }
        }

        return true;
    }
}
