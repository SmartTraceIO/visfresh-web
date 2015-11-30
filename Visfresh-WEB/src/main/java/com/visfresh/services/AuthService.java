/**
 *
 */
package com.visfresh.services;

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
     * @param authToken
     */
    public void logout(final String authToken);
    /**
     * @param user user
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
     * @param user user to create.
     * @param password user password.
     * @param resetOnLogin reset password on login flag.
     */
    void saveUser(User user, String password, boolean resetOnLogin);
}
