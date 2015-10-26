/**
 *
 */
package com.visfresh.mock;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.AbstractAuthService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockAuthService extends AbstractAuthService {
    /**
     * The user map.
     */
    public Map<String, User> users = new HashMap<String, User>();

    /**
     * Default constructor.
     */
    public MockAuthService() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getUser(java.lang.String)
     */
    @Override
    public User getUser(final String username) {
        synchronized (users) {
            User u = users.get(username);
            if (u == null) {
                //create new
                u = new User();
                u.setLogin(username);
                u.getRoles().add(Role.CompanyAdmin);
                createUser(u, "");
            }
            return u;
        }
    }

    @Override
    @PostConstruct
    public void start() {
        super.start();
    }
    @Override
    @PreDestroy
    public void stop() {
        super.stop();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#createUser(com.visfresh.entities.User)
     */
    @Override
    public void createUser(final User user, final String password) {
        user.setPassword(generateHash(password));
        synchronized (users) {
            users.put(user.getLogin(), user);
        }
    }
}
