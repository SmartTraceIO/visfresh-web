/**
 *
 */
package com.visfresh.dao.mock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.visfresh.dao.UserDao;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockUserDao extends MockDaoBase<User, String> implements UserDao {
    private Map<String, UserProfile> profiles = new ConcurrentHashMap<String, UserProfile>();

    /**
     * Default constructor.
     */
    public MockUserDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.UserDao#getProfile(com.visfresh.entities.User)
     */
    @Override
    public UserProfile getProfile(final User user) {
        return profiles.get(user.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends User> S save(final S entity) {
        entities.put(entity.getId(), entity);
        return entity;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.UserDao#saveProfile(com.visfresh.entities.User, com.visfresh.entities.UserProfile)
     */
    @Override
    public void saveProfile(final User user, final UserProfile profile) {
        if (profile == null) {
            profiles.remove(user.getId());
        } else {
            profiles.put(user.getId(), profile);
        }
    }
}
