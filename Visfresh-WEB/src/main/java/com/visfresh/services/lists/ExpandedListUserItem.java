/**
 *
 */
package com.visfresh.services.lists;

import java.util.HashSet;
import java.util.Set;

import com.visfresh.entities.Role;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ExpandedListUserItem {
    /**
     * User ID.
     */
    private Long id;
    /**
     * First user name.
     */
    private String firstName;
    /**
     * Last user name.
     */
    private String lastName;
    /**
     * Email address.
     */
    private String email;
    /**
     * User company name.
     */
    private String companyName;
    /**
     * User's position in company.
     */
    private String position;
    /**
     * External flag.
     */
    private boolean external;
    /**
     * Set of roles.
     */
    private final Set<Role> roles = new HashSet<Role>();
    /**
     * Active Flag.
     */
    private boolean active = true;

    /**
     * Default constructor.
     */
    public ExpandedListUserItem() {
        super();
    }
    /**
     * @param user user.
     */
    public ExpandedListUserItem(final User user) {
        super();
        setId(user.getId());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setEmail(user.getEmail());
        if (user.isExternal()) {
            setCompanyName(user.getExternalCompany());
        } else {
            setCompanyName(user.getCompany().getName());
        }
        setPosition(user.getPosition());
        setActive(user.getActive());
        getRoles().addAll(user.getRoles());
        setExternal(user.isExternal());
    }

    /**
     * @return the id
     */
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
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }
    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * @param lastName the lastName to set
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    /**
     * @param email the email to set
     */
    public void setEmail(final String email) {
        this.email = email;
    }
    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }
    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }
    /**
     * @return the position
     */
    public String getPosition() {
        return position;
    }
    /**
     * @param position the position to set
     */
    public void setPosition(final String position) {
        this.position = position;
    }
    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }
    /**
     * @param active the active to set
     */
    public void setActive(final boolean active) {
        this.active = active;
    }
    /**
     * @return the external
     */
    public boolean isExternal() {
        return external;
    }
    /**
     * @param external the external to set
     */
    public void setExternal(final boolean external) {
        this.external = external;
    }
    /**
     * @return the roles
     */
    public Set<Role> getRoles() {
        return roles;
    }
}
