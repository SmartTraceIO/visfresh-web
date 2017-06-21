/**
 *
 */
package com.visfresh.mock;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visfresh.entities.RestSession;
import com.visfresh.impl.services.DefaultRestSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockRestSessionManager extends DefaultRestSessionManager {
    private static final Logger log = LoggerFactory.getLogger(MockRestSessionManager.class);

    private List<RestSession> savedSessions = new LinkedList<RestSession>();

    /**
     * Default constructor.
     */
    public MockRestSessionManager() {
        super();
    }
    /**
     * @param s rest session
     */
    @Override
    protected void saveSession(final RestSession s) {
        savedSessions.add(s);
    }
    /**
     * @param s rest session
     */
    @Override
    protected void deleteSession(final RestSession s) {
        savedSessions.remove(s);
        log.debug("Rest session " + s.getToken().getToken()
                + " for user " + s.getUser().getEmail() + " has deleted");
    }
    /**
     * @param token
     * @return
     */
    @Override
    protected RestSession findSessionByToken(final String token) {
        for (final RestSession s : savedSessions) {
            if (s.getToken().getToken().equals(token)) {
                return s;
            }
        }
        return null;
    }
    /**
     * @return
     */
    @Override
    protected List<RestSession> loadSessions() {
        return new LinkedList<>(savedSessions);
    }
    /**
     * @return the savedSessions
     */
    public List<RestSession> getSavedSessions() {
        return savedSessions;
    }
    public void clear() {
        savedSessions.clear();
    }
}
