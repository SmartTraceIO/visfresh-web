/**
 *
 */
package com.visfresh.controllers.lite.dao;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.controllers.lite.LiteShipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipmentResult {
    /**
     * Result.
     */
    private final List<LiteShipment> result = new LinkedList<>();
    /**
     * Total items count.
     */
    private int totalCount;

    /**
     * Default constructor.
     */
    public LiteShipmentResult() {
        super();
    }

    /**
     * @return the result.
     */
    public List<LiteShipment> getResult() {
        return result;
    }

    /**
     * @return total count.
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
