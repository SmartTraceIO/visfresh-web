/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.ArrivalDao;
import com.visfresh.entities.Arrival;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ArrivalDaoImpl extends DaoImplBase<Arrival, Long> implements ArrivalDao {
    /**
     * Default constructor.
     */
    public ArrivalDaoImpl() {
        super(Arrival.class);
    }
}
