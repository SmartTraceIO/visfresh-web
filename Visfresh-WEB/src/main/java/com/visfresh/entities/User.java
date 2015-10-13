/**
 *
 */
package com.visfresh.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class User {
    private String login;
    private String fullName;
    private Set<Role> roles = new HashSet<Role>();

    /**
     * Default constructor.
     */
    public User() {
        super();
    }

    /**
     * @return the login
     */
    public String getLogin() {
        return login;
    }
    /**
     * @param login the login to set
     */
    public void setLogin(final String login) {
        this.login = login;
    }
    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }
    /**
     * @param fullName the fullName to set
     */
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }
    /**
     * @return the roles
     */
    public Set<Role> getRoles() {
        return roles;
    }
    /**
     * @param roles the roles to set
     */
    public void setRoles(final Set<Role> roles) {
        this.roles = roles;
    }
}
