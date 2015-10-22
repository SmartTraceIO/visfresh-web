/**
 *
 */
package com.visfresh.mpl.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.UserDao;
import com.visfresh.entities.User;
import com.visfresh.services.AbstractAuthService;
import com.visfresh.services.AuthToken;
import com.visfresh.services.AuthenticationException;
import com.visfresh.services.OpenJtsFacade;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultAuthService extends AbstractAuthService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private OpenJtsFacade openJts;

    /**
     * Default constructor.
     */
    public DefaultAuthService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#attachToExistingSession(javax.servlet.http.HttpSession)
     */
    @Override
    public AuthToken attachToExistingSession(final HttpSession session)
            throws AuthenticationException {
        return super.attachToExistingSession(session);
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
     * @see com.visfresh.services.AuthService#getUser(java.lang.String)
     */
    @Override
    public User getUser(final String username) {
        return userDao.findOne(username);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#createUser(com.visfresh.entities.User, java.lang.String)
     */
    @Override
    public void createUser(final User user, final String password) {
        user.setPassword(generateHash(password));
        userDao.save(user);
        openJts.addUser(user, password);
    }
}