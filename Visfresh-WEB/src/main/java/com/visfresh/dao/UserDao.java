/**
 * 
 */
package com.visfresh.dao;

import org.springframework.data.repository.CrudRepository;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface UserDao extends CrudRepository<User, String> {

}
