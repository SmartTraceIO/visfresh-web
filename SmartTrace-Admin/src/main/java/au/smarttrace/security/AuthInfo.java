/**
 *
 */
package au.smarttrace.security;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import au.smarttrace.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthInfo {
    private User user;
    private AccessToken token;
    /**
     * Default constructor.
     */
    public AuthInfo() {
        super();
    }

    /**
     * @return the user
     */
    @JsonGetter("user")
    public User getUser() {
        return user;
    }
    /**
     * @param user the user to set
     */
    @JsonSetter("user")
    public void setUser(final User user) {
        this.user = user;
    }
    /**
     * @return the token
     */
    @JsonGetter("token")
    public AccessToken getToken() {
        return token;
    }
    /**
     * @param token the token to set
     */
    @JsonSetter("token")
    public void setToken(final AccessToken token) {
        this.token = token;
    }
}
