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
