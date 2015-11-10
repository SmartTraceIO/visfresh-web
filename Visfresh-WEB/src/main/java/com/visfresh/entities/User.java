/**
 *
 */
package com.visfresh.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class User implements EntityWithId<String>, EntityWithCompany {
    /**
     * User login.
     */
    private String login;
    /**
     * Company.
     */
    private Company company;
    /**
     * Full user name.
     */
    private String fullName;
    /**
     * Encrypted password.
     */
    private String password;
    /**
     * Time Zone
     */
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");
    /**
     * Set of roles.
     */
    private Set<Role> roles = new HashSet<Role>();
    /**
     * Temperature units
     */
    private TemperatureUnits temperatureUnits = TemperatureUnits.Celsius;

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
    @Override
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Company company) {
        this.company = company;
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
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }
    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    /**
     * @return the temperatureUnits
     */
    public TemperatureUnits getTemperatureUnits() {
        return temperatureUnits;
    }
    /**
     * @param temperatureUnits the temperatureUnits to set
     */
    public void setTemperatureUnits(final TemperatureUnits temperatureUnits) {
        this.temperatureUnits = temperatureUnits;
    }
}
