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
    /**
     * @param bytes
     */
    public static void revertBytes(final byte[] bytes) {
        final int len = bytes.length;
        final int middle = len / 2;

        for (int pos1 = 0; pos1 < middle; pos1++) {
            final int pos2 = len - 1 - pos1;
            //change values by place
            final byte b = bytes[pos1];
            bytes[pos1] = bytes[pos2];
            bytes[pos2] = b;
        }
    }
}
