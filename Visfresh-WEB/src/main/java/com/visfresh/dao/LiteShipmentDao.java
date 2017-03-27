/**
 *
 */
package com.visfresh.dao;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.lite.LiteShipmentResult;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public interface LiteShipmentDao {
    /**
     * @param company company.
     * @param sorting sorting.
     * @param filter filter.
     * @param page page.
     * @return
     */
    public LiteShipmentResult getShipments(final Company company, final Sorting sorting,
            final Filter filter, final Page page);
}
