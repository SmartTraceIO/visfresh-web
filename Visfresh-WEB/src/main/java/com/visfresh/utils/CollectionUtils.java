/**
 *
 */
package com.visfresh.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
