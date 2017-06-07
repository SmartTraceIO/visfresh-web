/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.ShipmentAuditItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentAuditDao extends DaoBase<ShipmentAuditItem, ShipmentAuditItem, Long> {
    /**
     * @param company company.
     * @param filter additional filter.
     * @return total items count.
     */
    int getEntityCount(Company company, Filter filter);
    /**
     * @param company company.
     * @param filter additional filter.
     * @param createSorting sorting order.
     * @param page page.
     * @return selected items by given criterias.
     */
    List<ShipmentAuditItem> findAll(Company company, Filter filter, Sorting createSorting, Page page);
}
