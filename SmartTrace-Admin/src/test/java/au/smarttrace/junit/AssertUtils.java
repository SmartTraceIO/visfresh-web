/**
 *
 */
package au.smarttrace.junit;

import java.util.Date;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class AssertUtils {
    /**
     * Default constructor.
     */
    private AssertUtils() {
        super();
    }
    /**
     * @param d1 first date.
     * @param d2 second date.
     * @param delta delta.
     */
    public static void assertEqualDates(final Date d1, final Date d2, final long delta) {
        if (Math.abs(d1.getTime() - d2.getTime()) > delta) {
            throw new AssertionFailedError(d1 + " not equals " + d2);
        }
    }
}
