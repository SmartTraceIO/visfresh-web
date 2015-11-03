/**
 *
 */
package com.visfresh.utils;

import java.util.Map;
import java.util.regex.Pattern;


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
            if (sb.length() > 0) {
                sb.append(delim);
            }
            sb.append(String.valueOf(i));
        }
        return sb.toString();
    }
    /**
     * @param objects
     * @param delim
     * @return
     */
    public static <S> String combine(final S[] objects, final String delim) {
        final StringBuilder sb= new StringBuilder();
        for (final S s : objects) {
            if (sb.length() > 0) {
                sb.append(delim);
            }
            sb.append(s);
        }
        return sb.toString();
    }
    /**
     * @param template template string.
     * @param replacements map of replacements.
     * @return
     */
    public static String getMessage(final String template, final Map<String, String> replacements) {
        String result = template;
        for (final Map.Entry<String, String> e : replacements.entrySet()) {
            result = result.replaceAll(Pattern.quote("${" + e.getKey() + "}"), e.getValue());
        }
        return result;
    }
}
