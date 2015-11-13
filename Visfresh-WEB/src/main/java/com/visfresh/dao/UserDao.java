/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface UserDao extends EntityWithCompanyDaoBase<User, String> {
    /**
     * @param user user.
     * @return user profile.
     */
    UserProfile getProfile(User user);
    /**
     * @param user user
     * @param profile user profile to save.
     */
    void saveProfile(User user, UserProfile profile);
}
