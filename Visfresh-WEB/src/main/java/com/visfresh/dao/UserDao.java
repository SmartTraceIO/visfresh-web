/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface UserDao extends EntityWithCompanyDaoBase<User, Long> {
    /**
     * @param email email.
     * @return user by given email.
     */
    User findByEmail(String email);
}
