/**
 *
 */
package com.visfresh.services;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthToken implements Serializable {
    private static final long DEFAULT_EXPIRATION_TIMEOUT = 30 * 60 * 1000L;
    private static final long serialVersionUID = 6619929326579791662L;

    /**
     * Expiration time.
     */
    private Date expirationTime;
    /**
     * Security token string.
     */
    private String token;
    /**
     * Created time.
     */
    private Date createdTime = new Date();

    /**
     * Default constructor.
     */
    public AuthToken(final String token) {
        super();
        setExpirationTime(new Date(System.currentTimeMillis() + DEFAULT_EXPIRATION_TIMEOUT));
        this.token = token;
    }

    /**
     * @param expirationTime the expirationTime to set
     */
    public void setExpirationTime(final Date expirationTime) {
        this.expirationTime = expirationTime;
    }
    /**
     * @return the expirationTime
     */
    public Date getExpirationTime() {
        return expirationTime;
    }
    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }
    /**
     * @return created time.
     */
    public Date getCreatedTime() {
        return createdTime;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getToken();
    }
}
