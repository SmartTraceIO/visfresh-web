/**
 *
 */
package com.visfresh.utils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
}
