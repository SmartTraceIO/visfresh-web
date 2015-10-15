/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentTemplateDaoImpl extends DaoImplBase<ShipmentTemplate, Long>
    implements ShipmentTemplateDao {
    /**
     * Default constructor.
     */
    public ShipmentTemplateDaoImpl() {
        super(ShipmentTemplate.class);
    }
}
