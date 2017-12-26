/**
 *
 */
package au.smarttrace.ctrl.req;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractGetListRequest {
    private int page;
    private int pageSize = 1000;
    private final List<Order> orders = new LinkedList<>();

    /**
     *
     */
    public AbstractGetListRequest() {
        super();
    }

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(final int page) {
        this.page = page;
    }

    /**
     * @return the limit
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param limit the limit to set
     */
    public void setPageSize(final int limit) {
        this.pageSize = limit;
    }
    /**
     * @return the orders
     */
    public List<Order> getOrders() {
        return orders;
    }
}
