/**
 *
 */
package com.visfresh.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="users")
public class User implements EntityWithId {
    @Id
    private String login;
    @ManyToOne
    private Company company;
    private String fullName;
    @Transient
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
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public String getId() {
        return getLogin();
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
    @Column(name="roles")
    protected String getRolesImpl() {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return StringUtils.combine(roles, ",");
    }
    @Column(name="roles")
    protected void setRoles(final String roles) {
        this.roles = new HashSet<Role>();

        if (roles != null && roles.length() > 0) {
            final String[] split = roles.split(", *");
            for (final String string : split) {
                this.roles.add(Role.valueOf(string));
            }
        }
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
