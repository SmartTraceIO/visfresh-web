/**
 *
 */
package com.visfresh.services;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PasswordResetRequest {
    private static final long CHECK_PASSWORD_EXPIRATION_TIMEOUT = 15 * 60 * 1000l; //15 min
    private final String secureString;
    private final Date creationDate = new Date(System.currentTimeMillis() + CHECK_PASSWORD_EXPIRATION_TIMEOUT);

    /**
     * @param secureString security string.
     */
    public PasswordResetRequest(final String secureString) {
        super();
        this.secureString = secureString;
    }

    /**
     * @return the creationDate
     */
    public Date getExpirationTime() {
        return creationDate;
    }
    /**
     * @return the secureString
     */
    public String getSecureString() {
        return secureString;
    }
}
