/**
 *
 */
package au.smarttrace.utils;

import java.awt.Color;


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
     * @param color java color.
     * @return HTML color string.
     */
    public static String toHtmlColor(final Color color) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(color.getRGB() & 0xffffff));
        while (hex.length() < 6) {
            hex = hex.insert(0, '0');
        }
        hex.insert(0, '#');
        return hex.toString();
    }
}
