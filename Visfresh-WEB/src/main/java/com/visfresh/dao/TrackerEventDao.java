/**
 *
 */
package com.visfresh.dao;

import org.springframework.data.repository.CrudRepository;

import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface TrackerEventDao extends CrudRepository<TrackerEvent, Long> {

}
