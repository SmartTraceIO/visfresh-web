/**
 *
 */
package com.visfresh.dao;

import org.springframework.data.repository.CrudRepository;

import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface CompanyDao extends CrudRepository<Company, Long> {
}
