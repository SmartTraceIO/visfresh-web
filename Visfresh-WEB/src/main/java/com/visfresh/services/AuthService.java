/**
 *
 */
package com.visfresh.services;

import javax.servlet.http.HttpSession;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AuthService {
    /**
     * @param login login.
     * @param password password.
     * @return authentication token.
     * @throws AuthenticationException
     */
    public AuthToken login(final String login, final String password) throws AuthenticationException;
    /**
     * @param session HTTP session.
     * @return authentication token for already logged in user.
     */
    public AuthToken attachToExistingSession(final HttpSession session) throws AuthenticationException;
    /**
     * @param authToken
     */
    public void logout(final String authToken);
    /**
     * @param user TODO
     * @return refreshed authentication token.
     * @exception AuthenticationException
     */
    public AuthToken refreshToken(User user) throws AuthenticationException;
    /**
     * @param authToken authentication token.
     * @return user.
     */
    public User getUserForToken(String authToken);
    /**
     * @param username the user name.
     * @return user info or null if not found.
     */
    User getUser(String username);
}
