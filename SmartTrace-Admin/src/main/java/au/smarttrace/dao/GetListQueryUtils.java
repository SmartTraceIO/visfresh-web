/**
 *
 */
package au.smarttrace.dao;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class GetListQueryUtils {
    public static final int MAX_ITEMS = 1000;

    /**
     * Default constructor.
     */
    private GetListQueryUtils() {
        super();
    }

    /**
     * @param str origin string.
     * @return tokenized string.
     */
    public static String[] tokenize(final String str) {
        final List<String> result = new LinkedList<>();
        for (final String t : str.split("\\s+")) {
            if (t.length() > 0) {
                result.add(t);
            }
        }
        return result.toArray(new String[result.size()]);
    }
    /**
     * @param col collection of objects.
     * @return collection of to string representations or nulls if object is null.
     */
    public static <M> List<String> toStringList(final Collection<M> col) {
        final List<String> result = new LinkedList<>();
        for (final M m : col) {
            result.add(m == null ? null : m.toString());
        }
        return result;
    }
}
