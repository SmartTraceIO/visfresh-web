/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserInfo implements Comparable<UserInfo> {
    private User user;
    private AuthToken token;

    /**
     * Default constructor.
     */
    public UserInfo() {
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
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final UserInfo o) {
        return getToken().getCreatedTime().compareTo(o.getToken().getCreatedTime());
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getUser().getEmail() + ": " + getToken();
    }
}
