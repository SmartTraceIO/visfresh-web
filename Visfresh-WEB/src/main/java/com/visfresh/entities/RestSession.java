/**
 *
 */
package com.visfresh.entities;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.visfresh.controllers.session.RestSessionListener;
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
    private final Map<String, String> properties = new ConcurrentHashMap<>();
    /**
     * Listener list.
     */
    private final List<RestSessionListener> listeners = new LinkedList<>();

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
        getListeners().forEach(l -> l.tokenChanged(token));
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
     * @param key property key.
     * @param value property value. If null, property will removed.
     */
    public void putProperty(final String key, final String value) {
        final String oldValue = properties.get(key);

        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }

        if (!Objects.equals(oldValue, value)) {
            getListeners().forEach(l -> l.propertyChanged(key, oldValue, value));
        }
    }
    public String getProperty(final String key) {
        return properties.get(key);
    }
    /**
     * @return the set of property keys.
     */
    public Set<String> getPropertyKeys() {
        return new HashSet<>(properties.keySet());
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final RestSession o) {
        return getToken().getCreatedTime().compareTo(o.getToken().getCreatedTime());
    }

    //listeners
    public void addRestSessionListener(final RestSessionListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public void removeRestSessionListener(final RestSessionListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected List<RestSessionListener> getListeners() {
        synchronized (listeners) {
            return new LinkedList<>(listeners);
        }
    }
}
