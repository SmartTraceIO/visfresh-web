/**
 *
 */
package com.visfresh.dao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Page {
    private int pageNumber;
    private int pageSize;

    /**
     * @param pageNumber page number.
     * @param page size;
     */
    public Page(final int pageNumber, final int pageSize) {
        super();
        if (pageNumber < 1) {
            throw new IllegalStateException("Page number should start from 1");
        }
        this.pageNumber = pageNumber;
        if (pageSize < 1) {
            throw new IllegalStateException("Page size should be more than 0");
        }
        this.pageSize = pageSize;
    }

    /**
     * @return the pageNumber
     */
    public int getPageNumber() {
        return pageNumber;
    }
    /**
     * @return the pageSize
     */
    public int getPageSize() {
        return pageSize;
    }
}
