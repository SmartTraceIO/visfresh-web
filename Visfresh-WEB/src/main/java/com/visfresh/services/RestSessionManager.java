/**
 *
 */
package com.visfresh.services;

import java.util.List;

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
     * @param authToken
     * @return
     */
    public RestSession getSession(final String authToken);
    public void closeAllSessions(final User u);
    /**
     * @param email user email
     * @return list of sessions for given user.
     */
    public List<RestSession> getAllUserSessions(final String email);
}
