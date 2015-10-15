/**
 *
 */
package com.visfresh.dao;

import org.springframework.data.repository.CrudRepository;

import com.visfresh.entities.Arrival;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ArrivalDao extends CrudRepository<Arrival, Long> {

}
