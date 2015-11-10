/**
 *
 */
package com.visfresh.dao.mock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.UserConstants;
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
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property, final User u) {
        if (property.equals(UserConstants.PROPERTY_ROLES)) {
            return u.getRoles();
        }
        if (property.equals(UserConstants.PROPERTY_TEMPERATURE_UNITS)) {
            return u.getTemperatureUnits();
        }
        if (property.equals(UserConstants.PROPERTY_TIME_ZONE)) {
            return u.getTimeZone();
        }
        if (property.equals(UserConstants.PROPERTY_FULL_NAME)) {
            return u.getFullName();
        }
        if (property.equals(UserConstants.PROPERTY_LOGIN)) {
            return u.getLogin();
        }
        throw new IllegalArgumentException("Undefined property: " + property);
    }
}
