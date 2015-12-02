/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SaveUserRequest {
    private User user;
    private String password;
    private Boolean resetOnLogin;

    /**
     * Default constructor.
     */
    public SaveUserRequest() {
        super();
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    /**
     * @param password the password to set
     */
    public void setPassword(final String password) {
        this.password = password;
    }
    /**
     * @return the usr
     */
    public User getUser() {
        return user;
    }
    /**
     * @param usr the usr to set
     */
    public void setUser(final User usr) {
        this.user = usr;
    }
    /**
     * @param resetOnLogin
     */
    public void setResetOnLogin(final Boolean resetOnLogin) {
        this.resetOnLogin = resetOnLogin;
    }
    /**
     * @return the resetOnLogin
     */
    public Boolean getResetOnLogin() {
        return resetOnLogin;
    }
}
