/**
 *
 */
package au.smarttrace.eel;

import java.util.TimeZone;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class Utils {
    public static final String MARK = "6767";

    /**
     * Default constructor.
     */
    private Utils() {
        super();
    }

    public static long timeToUtc(final long t) {
        //convert to UTC
        return t - TimeZone.getDefault().getOffset(t);
    }
    public static long timeFromUtc(final long t) {
        //convert from UTC
        return t + TimeZone.getDefault().getOffset(t);
    }
}
