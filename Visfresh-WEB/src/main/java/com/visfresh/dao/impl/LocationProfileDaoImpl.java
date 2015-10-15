/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LocationProfileDaoImpl extends DaoImplBase<LocationProfile, Long>
    implements LocationProfileDao {
    /**
     * Default constructor.
     */
    public LocationProfileDaoImpl() {
        super(LocationProfile.class);
    }
}
