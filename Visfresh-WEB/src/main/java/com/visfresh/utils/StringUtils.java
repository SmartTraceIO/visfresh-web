/**
 *
 */
package com.visfresh.utils;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class StringUtils {
    /**
     * Default constructor.
     */
    private StringUtils() {
        super();
    }

    /**
     * @param objects
     * @param delim
     * @return
     */
    public static <M> String combine(final Iterable<M> objects, final String delim) {
        final StringBuilder sb = new StringBuilder();
        for (final M i : objects) {
            sb.append(String.valueOf(i));
        }
        return sb.toString();
    }
}
