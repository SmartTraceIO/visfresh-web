/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.UserDao;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UserDaoImpl extends DaoImplBase<User, String> implements UserDao {
    /**
     * Default constructor.
     */
    public UserDaoImpl() {
        super(User.class);
    }
}
