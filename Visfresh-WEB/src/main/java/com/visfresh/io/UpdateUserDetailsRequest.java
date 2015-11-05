/**
 *
 */
package com.visfresh.io;

import java.util.TimeZone;

import com.visfresh.entities.TemperatureUnits;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UpdateUserDetailsRequest {
    private String user;
    private TimeZone timeZone;
    private String fullName;
    private TemperatureUnits temperatureUnits;
    private String password;

    /**
     * Default constructor.
     */
    public UpdateUserDetailsRequest() {
        super();
    }

    /**
     * @param login
     */
    public void setUser(final String login) {
        this.user = login;
    }
    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }
    /**
     * @param tz tile zone.
     */
    public void setTimeZone(final TimeZone tz) {
        this.timeZone = tz;
    }
    /**
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }
    /**
     * @param fullName full user name.
     */
    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }
    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }
    /**
     * @param tu
     */
    public void setTemperatureUnits(final TemperatureUnits tu) {
        this.temperatureUnits = tu;
    }
    /**
     * @return the temperatureUnits
     */
    public TemperatureUnits getTemperatureUnits() {
        return temperatureUnits;
    }
    /**
     * @param password password.
     */
    public void setPassword(final String password) {
        this.password = password;
    }
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
}
