/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.entities.AbstractAlert;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertDaoImpl extends DaoImplBase<AbstractAlert, Long> implements AlertDao {
    /**
     * Default constructor.
     */
    public AlertDaoImpl() {
        super(AbstractAlert.class);
    }
}
