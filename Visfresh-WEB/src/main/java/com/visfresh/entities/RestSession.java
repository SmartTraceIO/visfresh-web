/**
 *
 */
package com.visfresh.entities;

import java.util.HashMap;
import java.util.Map;

import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestSession implements EntityWithId<Long>, Comparable<RestSession> {
    /**
     * Session ID.
     */
    private Long id;
    /**
     * User.
     */
    private User user;
    /**
     * Authentication token.
     */
    private AuthToken token;
    /**
     * Session properties.
     */
    private final Map<String, String> properties = new HashMap<>();

    /**
     * Default constructor.
     */
    public RestSession() {
        super();
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
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final RestSession o) {
        return getToken().getCreatedTime().compareTo(o.getToken().getCreatedTime());
    }
}
