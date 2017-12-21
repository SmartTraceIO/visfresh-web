/**
 *
 */
package au.smarttrace.ctrl.res;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ListResponse<E> {
    private final List<E> items = new LinkedList<>();
    private int totalCount;

    /**
     * Default constructor.
     */
    public ListResponse() {
        super();
    }

    /**
     * @return the items
     */
    public List<E> getItems() {
        return items;
    }
    /**
     * @return the totalCount
     */
    public int getTotalCount() {
        return totalCount;
    }
    /**
     * @param totalCount the totalCount to set
     */
    public void setTotalCount(final int totalCount) {
        this.totalCount = totalCount;
    }
}
