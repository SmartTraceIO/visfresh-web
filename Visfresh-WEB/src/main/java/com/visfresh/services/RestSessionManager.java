/**
 *
 */
package com.visfresh.services;

import java.util.List;

import com.visfresh.controllers.session.SessionManagerListener;
import com.visfresh.entities.RestSession;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RestSessionManager {
    /**
     * @param user
     * @param authToken
     */
    public RestSession createSession(final User user, final AuthToken authToken);
    /**
     * @param session
     */
    public void closeSession(final RestSession session);
    /**
     * @param authToken auth token.
     * @return rest session for given token.
     */
    public RestSession getSession(final String authToken);
    /**
     * Closes all opened sessions for given user.
     * @param u the user.
     */
    public void closeAllSessions(final User u);
    /**
     * @param email user email
     * @return list of sessions for given user.
     */
    public List<RestSession> getAllUserSessions(final String email);
    /**
     * @param l session manager listener.
     */
    void addSessionManagerListener(SessionManagerListener l);
    /**
     * @param l session manager listener to remove.
     */
    void removeSessionManagerListener(SessionManagerListener l);
}
