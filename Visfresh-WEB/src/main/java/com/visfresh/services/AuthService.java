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
     * @param clientInstance client instance ID.
     * @return authentication token.
     * @throws AuthenticationException
     */
    public AuthToken login(final String login, final String password, final String clientInstance) throws AuthenticationException;
    /**
     * @param authToken
     */
    public void logout(final String authToken);
    /**
     * @param oldToken old authentication token.
     * @return refreshed authentication token.
     * @exception AuthenticationException
     */
    public AuthToken refreshToken(String oldToken) throws AuthenticationException;
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
    /**
     * @param email email.
     * @param baseUrl part of URL for send to user.
     * @throws AuthenticationException
     */
    public void startResetPassword(String email, String baseUrl) throws AuthenticationException;
    /**
     * @param email email.
     * @param password password.
     * @param token security token.
     * @throws AuthenticationException
     */
    public void resetPassword(String email, String password, String token) throws AuthenticationException;
    /**
     * Closes all user sessions.
     * @param u user.
     */
    public void forceLogout(User u);
}
