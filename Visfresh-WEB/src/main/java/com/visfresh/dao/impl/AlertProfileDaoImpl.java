/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.entities.AlertProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertProfileDaoImpl extends DaoImplBase<AlertProfile, Long> implements AlertProfileDao {
    /**
     * Default constructor.
     */
    public AlertProfileDaoImpl() {
        super(AlertProfile.class);
    }
}
