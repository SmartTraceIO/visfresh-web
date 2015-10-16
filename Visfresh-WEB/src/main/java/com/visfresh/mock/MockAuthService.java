/**
 *
 */
package com.visfresh.mock;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;
import com.visfresh.services.AuthToken;
import com.visfresh.services.AuthenticationException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockAuthService implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(MockAuthService.class);

    private static final long DEFAULT_TOKEN_ACTIVE_TIMEOUT = 10 * 60 * 1000l;//10 min
    private static final long TIMEOUT = 15000L;
    private final AtomicLong tokens = new AtomicLong(100000l);

    private final Map<String, MockUserInfo> users = new HashMap<String, MockUserInfo>();
    private final AtomicBoolean isStopped = new AtomicBoolean();

    /**
     * Default constructor.
     */
    public MockAuthService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#login(java.lang.String, java.lang.String)
     */
    @Override
    public AuthToken login(final String login, final String password)
            throws AuthenticationException {
        synchronized (users) {
            MockUserInfo u = users.get(login);
            if (u == null) {
                u = new MockUserInfo();
                final User user = new User();
                user.setLogin(login);
                user.setFullName(login);
                user.getRoles().add(Role.GlobalAdmin);
                user.getRoles().add(Role.ReportViewer);

                u.setUser(user);
                users.put(login, u);
            }

            u.setToken(generateNewToken());

            return u.getToken();
        }
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

    /* (non-Javadoc)
     * @see com.visfresh.services.AuthService#refreshToken(java.lang.String)
     */
    @Override
    public AuthToken refreshToken(final User user)
            throws AuthenticationException {
        final MockUserInfo info = getUserInfo(user.getLogin());
        if (info == null) {
            throw new AuthenticationException("Not authorized or token expired");
        }

        info.setToken(generateNewToken());
        return info.getToken();
    }
    /**
     * @param authToken
     * @return
     */
    private MockUserInfo getUserInfoForToken(final String authToken) {
        synchronized (users) {
            for (final Map.Entry<String, MockUserInfo> e : users.entrySet()) {
                final MockUserInfo ui = e.getValue();
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
        final MockUserInfo info = getUserInfoForToken(authToken);
        return info == null ? null : info.getUser();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getUser(java.lang.String)
     */
    @Override
    public User getUser(final String username) {
        synchronized (users) {
            final MockUserInfo ui = getUserInfo(username);
            if (ui != null) {
                return ui.getUser();
            }
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RestService#getUser(java.lang.String)
     */
    private MockUserInfo getUserInfo(final String username) {
        synchronized (users) {
            for (final Map.Entry<String, MockUserInfo> e : users.entrySet()) {
                final MockUserInfo ui = e.getValue();
                if (ui.getUser().getLogin().equals(username)) {
                    return ui;
                }
            }
        }

        return null;
    }

    /**
     * @return
     */
    private AuthToken generateNewToken() {
        final String token = "token_" + tokens.incrementAndGet();
        final AuthToken t = new AuthToken(token);
        t.setExpirationTime(new Date(System.currentTimeMillis() + DEFAULT_TOKEN_ACTIVE_TIMEOUT));
        return t;
    }

    @PostConstruct
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
            final Iterator<Map.Entry<String, MockUserInfo>> iter = users.entrySet().iterator();
            while (iter.hasNext()) {
                final MockUserInfo ui = iter.next().getValue();
                if (ui.getToken().getExpirationTime().getTime() < time) {
                    log.debug("Access token for user " + ui.getUser().getLogin() + " has expired and will removed");
                    iter.remove();
                }
            }
        }
    }

    @PreDestroy
    public void stop() {
        synchronized (isStopped) {
            isStopped.set(true);
            isStopped.notify();
        }
    }
}
