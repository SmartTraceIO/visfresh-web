/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentTemplateDao extends
        DaoBase<ShipmentTemplate, Long> {
    /**
     * @param company
     * @return
     */
    List<ShipmentTemplate> findByCompany(Company company);
}
