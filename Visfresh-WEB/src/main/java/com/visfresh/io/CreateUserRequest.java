/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.Company;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CreateUserRequest {
    private User user;
    private Company company;
    private String password;

    /**
     * Default constructor.
     */
    public CreateUserRequest() {
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
     * @return the company
     */
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Company company) {
        this.company = company;
    }
}
