/**
 *
 */
package com.visfresh.impl.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.visfresh.controllers.session.RestSessionListener;
import com.visfresh.controllers.session.SessionManagerListener;
import com.visfresh.dao.RestSessionDao;
import com.visfresh.entities.RestSession;
import com.visfresh.entities.User;
import com.visfresh.services.AuthToken;
import com.visfresh.services.RestSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultRestSessionManager implements RestSessionManager,
        ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private RestSessionDao sessionDao;

    private static final Logger log = LoggerFactory.getLogger(DefaultRestSessionManager.class);

    private static final long TIMEOUT = 60000L;

    private final AtomicBoolean isStopped = new AtomicBoolean();

    private final Map<String, RestSession> sessions = new HashMap<>();
    /**
     * The listener list.
     */
    private final List<SessionManagerListener> listeners = new LinkedList<>();

    /**
     * Default constructor.
     */
    public DefaultRestSessionManager() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        //removing of expired tokens and start check tokens thread should
        //be run after all context has initialized for be sure
        //that all listeners are installed.
        removeExpiredTokens();
        startCheckTokensThread();
    }

    @PostConstruct
    public void init() {
        final List<RestSession> sessions = loadSessions();

        synchronized (sessions) {
            for (final RestSession s : sessions) {
                this.sessions.put(s.getToken().getToken(), s);
            }
        }
    }
    /**
     * @param user
     * @param authToken
     */
    @Override
    public RestSession createSession(final User user, final AuthToken authToken) {
        final RestSession s = new RestSession();
        s.setUser(user);
        s.setToken(authToken);
        final List<RestSession> sessionsToClose;

        synchronized (sessions) {
            sessionsToClose = getSessionsToClose(user, authToken.getClientInstanceId());
            sessions.put(s.getToken().getToken(), s);
            saveSession(s);
        }

        log.debug("Rest session for user " + user.getEmail() + " has created. Token: "
                + s.getToken().getToken());

        //add session listener
        s.addRestSessionListener(new RestSessionListener() {
            @Override
            public void tokenChanged(final AuthToken token) {
                sessionTokenChanged(s, token);
            }
            @Override
            public void propertyChanged(final String name, final String oldValue, final String newValue) {
            }
        });

        //notify listeners
        for (final SessionManagerListener l : getListeners()) {
            l.sessionCreated(s);
        }

        //close redundant sessions.
        for (final RestSession sc : sessionsToClose) {
            closeSession(sc);
            final AuthToken t = sc.getToken();
            log.debug("Old session "
                + t.getToken()
                + " for user " + sc.getUser().getEmail()
                + " and client " + t.getClientInstanceId()
                + " has removed according new login");
        }

        return s;
    }
    /**
     * Warning!!! is not tread safe should be called from synchronized block.
     * @param user user.
     * @param clientId client instance ID.
     * @return list of session to close according of new session creation.
     */
    private List<RestSession> getSessionsToClose(final User user, final String clientId) {
        final List<RestSession> result = new LinkedList<>();

        for (final RestSession s : this.sessions.values()) {
            if (s.getUser().getId().equals(user.getId())
                    && Objects.equals(s.getToken().getClientInstanceId(), clientId)) {
                result.add(s);
            }
        }

        return result;
    }

    /**
     * @param session
     */
    @Override
    public void closeSession(final RestSession session) {
        synchronized (sessions) {
            sessions.remove(session.getToken().getToken());
            deleteSession(session);
        }

        for (final SessionManagerListener l : getListeners()) {
            l.sessionClosed(session);
        }
    }

    /**
     *
     */
    protected void startCheckTokensThread() {
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

        synchronized (sessions) {
            final Iterator<RestSession> iter = sessions.values().iterator();
            while (iter.hasNext()) {
                final RestSession session  = iter.next();
                if (session.getToken().getExpirationTime().getTime() < time) {
                    log.debug("Access token for user " + session.getUser().getEmail()
                            + " has expired and will removed");
                    iter.remove();
                    deleteSession(session);
                }
            }
        }
    }
    /**
     * @param authToken
     * @return
     */
    @Override
    public RestSession getSession(final String authToken) {
        synchronized (sessions) {
            return sessions.get(authToken);
        }
    }
    /**
     * Stops the service.
     */
    @PreDestroy
    public void stop() {
        synchronized (isStopped) {
            isStopped.set(true);
            isStopped.notifyAll();
        }
    }

    @Override
    public void closeAllSessions(final User u) {
        int count = 0;

        final Long id = u.getId();
        synchronized (sessions) {
            final Iterator<Entry<String, RestSession>> iter = sessions.entrySet().iterator();
            while(iter.hasNext()) {
                final Entry<String, RestSession> entry = iter.next();
                final RestSession info = entry.getValue();
                if (id.equals(info.getUser().getId())) {
                    iter.remove();
                    deleteSession(info);
                    count++;
                }
            }
        }

        log.debug(count + " user sessions have been closed for force logout of " + u.getEmail());
    }

    /**
     * @param s
     */
    protected void saveSession(final RestSession s) {
        sessionDao.save(s);
    }
    /**
     * @param s
     */
    protected void deleteSession(final RestSession s) {
        sessionDao.delete(s);
        log.debug("Rest session " + s.getToken().getToken()
                + " for user " + s.getUser().getEmail() + " has deleted");
    }
    /**
     * @param token
     * @return
     */
    protected RestSession findSessionByToken(final String token) {
        return sessionDao.findByToken(token);
    }
    /**
     * @return
     */
    protected List<RestSession> loadSessions() {
        return sessionDao.findAll(null, null, null);
    }

    /**
     * @param email user email
     * @return list of sessions for given user.
     */
    @Override
    public List<RestSession> getAllUserSessions(final String email) {
        final List<RestSession> list = new LinkedList<>();

        synchronized (sessions) {
            for (final RestSession ui : sessions.values()) {
                if (ui.getUser().getEmail().equals(email)) {
                    list.add(ui);
                }
            }
        }

        return list;
    }

    /**
     * @param s
     * @param token
     */
    protected void sessionTokenChanged(final RestSession s, final AuthToken token) {
        synchronized (sessions) {
            final Iterator<Map.Entry<String, RestSession>> iter = sessions.entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<String, RestSession> e = iter.next();
                if (e.getValue() == s) {
                    iter.remove();

                    //update session
                    sessions.put(token.getToken(), s);
                    saveSession(s);
                    break;
                }
            }
        }
    }
    /**
     * @param l listener to add.
     */
    @Override
    public void addSessionManagerListener(final SessionManagerListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    /**
     * @param l listener to remove.
     */
    @Override
    public void removeSessionManagerListener(final SessionManagerListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    /**
     * @return list of listeners.
     */
    protected List<SessionManagerListener> getListeners() {
        synchronized (listeners) {
            return new LinkedList<>(listeners);
        }
    }
}
