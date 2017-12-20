/**
 *
 */
package au.smarttrace.security;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AccessToken implements Serializable {
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
     * Default constructor.
     */
    public AccessToken() {
        super();
    }
    /**
     * Default constructor.
     */
    public AccessToken(final String token) {
        super();
        setToken(token);
    }

    /**
     * @param expirationTime the expirationTime to set
     */
    @JsonSetter("expired")
    public void setExpirationTime(final Date expirationTime) {
        this.expirationTime = expirationTime;
    }
    /**
     * @return the expirationTime
     */
    @JsonGetter("expired")
    public Date getExpirationTime() {
        return expirationTime;
    }
    /**
     * @return the token
     */
    @JsonGetter("token")
    public String getToken() {
        return token;
    }
    /**
     * @param token the token to set
     */
    @JsonSetter("token")
    public void setToken(final String token) {
        this.token = token;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getToken() + " expired: " + getExpirationTime();
    }
}
