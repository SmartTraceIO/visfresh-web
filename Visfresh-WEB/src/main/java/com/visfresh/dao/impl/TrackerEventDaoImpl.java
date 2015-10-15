/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TrackerEventDaoImpl extends DaoImplBase<TrackerEvent, Long>
    implements TrackerEventDao {
    /**
     * Default constructor.
     */
    public TrackerEventDaoImpl() {
        super(TrackerEvent.class);
    }
}
