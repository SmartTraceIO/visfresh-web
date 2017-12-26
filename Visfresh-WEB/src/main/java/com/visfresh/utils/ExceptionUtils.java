/**
 *
 */
package com.visfresh.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class ExceptionUtils {
    /**
     * Default constructor.
     */
    private ExceptionUtils() {
        super();
    }

    /**
     * @param e
     * @return
     */
    public static String getSteackTraceAsString(final Throwable e, final int maxLines) {
        final StringWriter s = new StringWriter();
        final PrintWriter wr = new PrintWriter(s);

        if (maxLines > 0) {
            // Print our stack trace
            wr.println(e);
            final StackTraceElement[] trace = e.getStackTrace();
            final int len = Math.min(maxLines, trace.length) - 1;
            for (int i = 0; i < len; i++) {
                wr.println("\tat " + trace[i]);
            }
        } else {
            e.printStackTrace(new PrintWriter(wr));
        }

        return s.toString();
    }
    /**
     * @param container container exception.
     * @param exception possible contained exception.
     * @return true if container exception contains given exception.
     */
    public static boolean containsException(final Throwable container, final Class<? extends Throwable> eclass) {
        return doRecursive(container, (current) ->
            {
                return eclass.isAssignableFrom(current.getClass());
            });
    }
    /**
     * @param container container exception.
     * @param exception possible contained exception.
     * @return true if container exception contains given exception.
     */
    public static boolean doRecursive(final Throwable container, final Function<Throwable, Boolean> fun) {
        final Set<Throwable> checked = new HashSet<>();
        Throwable current = container;
        while (current != null && !checked.contains(current)) {
            if (fun.apply(current)) {
                return true;
            }

            checked.add(current);
            current = current.getCause();
        }

        return false;
    }
    /**
     * @param e
     * @return
     */
    public static boolean isLockWaitTimeOut(final Throwable e) {
        return doRecursive(e, (current) ->
        {
            final String msg = e.getMessage();
            return msg != null && msg.contains("Lock wait timeout exceeded");
        });
    }
    /**
     * @param e
     * @return
     */
    public static boolean isMySqlDeadLock(final Throwable e) {
        return doRecursive(e, (current) ->
        {
            final String msg = e.getMessage();
            return msg != null && msg.contains("Deadlock found");
        });
    }
}
