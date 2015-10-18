/**
 *
 */
package com.visfresh.entities;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="users")
public class User implements EntityWithId {
    /**
     * User login.
     */
    @Id
    private String login;
    /**
     * Company.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "company",
        foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT),
        columnDefinition = "bigint",
        referencedColumnName = "id")
    private Company company;
    /**
     * Full user name.
     */
    private String fullName;
    /**
     * Set of roles.
     */
    @Column(nullable = false)
    @Convert(converter = RoleConverter.class)
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
