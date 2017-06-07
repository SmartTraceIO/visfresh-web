/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.ReferenceInfo;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface UserDao extends EntityWithCompanyDaoBase<User, User, Long> {
    /**
     * @param email email.
     * @return user by given email.
     */
    User findByEmail(String email);

    /**
     * @param userId user ID.
     * @return list of references to user.
     */
    List<ReferenceInfo> getDbReferences(Long userId);
}
