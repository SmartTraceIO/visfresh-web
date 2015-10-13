/**
 *
 */
package com.visfresh.mock;

import com.visfresh.entities.User;
import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MockUserInfo {
    private User user;
    private AuthToken token;

    /**
     * Default constructor.
     */
    public MockUserInfo() {
        super();
    }

    /**
     * @return the token
     */
    public AuthToken getToken() {
        return token;
    }
    /**
     * @param token the token to set
     */
    public void setToken(final AuthToken token) {
        this.token = token;
    }
    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }
    /**
     * @param user the user to set
     */
    public void setUser(final User user) {
        this.user = user;
    }
}
