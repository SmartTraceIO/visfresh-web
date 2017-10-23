/**
 *
 */
package com.visfresh.impl.singleshipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SingleShipmentPartBuilder {
    int MAX_PRIORITY = 1000;
    int MIN_PRIORITY = 0;

    /**
     * @return merge priority.
     */
    int getPriority();
    /**
     * @param context build context.
     */
    void build(SingleShipmentBuildContext context);
    /**
     * Fetch the data (from DB).
     */
    void fetchData();
}
