/**
 *
 */
package com.visfresh.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.visfresh.entities.User;
import com.visfresh.utils.HashGenerator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractAuthService implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AbstractAuthService.class);

    private static final long DEFAULT_TOKEN_ACTIVE_TIMEOUT = 10 * 60 * 1000l;//10 min
    private static final long TIMEOUT = 15000L;

    private final AtomicBoolean isStopped = new AtomicBoolean();

    private final Map<String, UserInfo> users = new HashMap<String, UserInfo>();

    /**
     * Default constructor.
     */
    public AbstractAuthService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#login(java.lang.String, java.lang.String)
     */
    @Override
    public AuthToken login(final String login, final String password)
            throws AuthenticationException {
        final User user = getUser(login);
        if (user == null) {
            throw new AuthenticationException("Unknown user " + login);
        }

        if (user.getPassword().equals(generateHash(password))) {
            synchronized (users) {
                UserInfo u = users.get(login);
                if (u == null) {
                    u = new UserInfo();
                    u.setUser(user);
                    users.put(login, u);
                }

                u.setToken(generateNewToken(user));
                return u.getToken();
            }
        }

        throw new AuthenticationException("Authentication failed");
    }

    /**
     * @param user user.
     * @return
     */
    protected AuthToken generateNewToken(final User user) {
        final String token = user.hashCode() + "-" + generateHash(Long.toString(System.currentTimeMillis()));
        final AuthToken t = new AuthToken(token);
        t.setExpirationTime(new Date(System.currentTimeMillis() + DEFAULT_TOKEN_ACTIVE_TIMEOUT));
        return t;
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    removeExpiredTokens();

                    synchronized (isStopped) {
                        try {
                            isStopped.wait(TIMEOUT);
                        } catch (final InterruptedException e) {
                            log.error("Check token expiration thread has interrupted", e);
                            return;
                        }
                        if (isStopped.get()) {
                            log.debug("Check token expiration thread has stopped");
                            return;
                        }
                    }
                }
            }
        }.start();
    }

    /**
     *
     */
    protected void removeExpiredTokens() {
        final long time = System.currentTimeMillis();

        synchronized (users) {
            final Iterator<Map.Entry<String, UserInfo>> iter = users.entrySet().iterator();
            while (iter.hasNext()) {
                final UserInfo ui = iter.next().getValue();
                if (ui.getToken().getExpirationTime().getTime() < time) {
                    log.debug("Access token for user " + ui.getUser().getLogin()
                            + " has expired and will removed");
                    iter.remove();
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#refreshToken(java.lang.String)
     */
    @Override
    public AuthToken refreshToken(final User user)
            throws AuthenticationException {
        final UserInfo info = getUserInfo(user.getLogin());
        if (info == null) {
            throw new AuthenticationException("Not authorized or token expired");
        }

        info.setToken(generateNewToken(user));
        return info.getToken();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getUser(java.lang.String)
     */
    private UserInfo getUserInfo(final String username) {
        synchronized (users) {
            for (final Map.Entry<String, UserInfo> e : users.entrySet()) {
                final UserInfo ui = e.getValue();
                if (ui.getUser().getLogin().equals(username)) {
                    return ui;
                }
            }
        }

        return null;
    }
    /**
     * @param authToken
     * @return
     */
    private UserInfo getUserInfoForToken(final String authToken) {
        synchronized (users) {
            for (final Map.Entry<String, UserInfo> e : users.entrySet()) {
                final UserInfo ui = e.getValue();
                if (ui.getToken().getToken().equals(authToken)) {
                    return ui;
                }
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#getUserForToken(java.lang.String)
     */
    @Override
    public User getUserForToken(final String authToken) {
        final UserInfo info = getUserInfoForToken(authToken);
        return info == null ? null : info.getUser();
    }

    public void stop() {
        synchronized (isStopped) {
            isStopped.set(true);
            isStopped.notify();
        }
    }
    /**
     * @param password password.
     * @return password hash.
     */
    protected String generateHash(final String password) {
        return HashGenerator.createMd5Hash(password);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#attachToExistingSession(javax.servlet.http.HttpSession)
     */
    @Override
    public AuthToken attachToExistingSession(final HttpSession session)
            throws AuthenticationException {
        throw new AuthenticationException("Not implemented for mock service");
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#logout(java.lang.String)
     */
    @Override
    public void logout(final String authToken) {
        final User user = getUserForToken(authToken);
        if (user != null) {
            synchronized (users) {
                users.remove(user.getLogin());
            }
        }
    }
}
