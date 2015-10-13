/**
 *
 */
package com.visfresh.services;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthToken {
    private static final long DEFAULT_EXPIRATION_TIMEOUT = 30 * 60 * 1000L;

    /**
     * Expiration time.
     */
    private Date expirationTime;
    /**
     * Security token string.
     */
    private final String token;

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
}
