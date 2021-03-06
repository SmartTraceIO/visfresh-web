/**
 *
 */
package com.visfresh.io;

import java.util.TimeZone;

import com.visfresh.entities.Language;
import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.TemperatureUnits;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UpdateUserDetailsRequest {
    private Long user;
    private String firstName;
    private String lastName;
    private String position;
    private String email;
    private String phone;
    private TemperatureUnits temperatureUnits;
    private TimeZone timeZone;
    private String password;
    private MeasurementUnits measurementUnits;
    private Language language;
    private String scale;
    private String title;

    /**
     * Default constructor.
     */
    public UpdateUserDetailsRequest() {
        super();
    }

    /**
     * @param id
     */
    public void setUser(final Long id) {
        this.user = id;
    }
    /**
     * @return the user
     */
    public Long getUser() {
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
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }
    /**
     * @param phone the phone to set
     */
    public void setPhone(final String phone) {
        this.phone = phone;
    }
    /**
     * @return measurement units.
     */
    public MeasurementUnits getMeasurementUnits() {
        return measurementUnits;
    }
    /**
     * @param units the measurement units.
     */
    public void setMeasurementUnits(final MeasurementUnits units) {
        this.measurementUnits = units;
    }

    /**
     * @return language.
     */
    public Language getLanguage() {
        return language;
    }
    /**
     * @param language the language to set
     */
    public void setLanguage(final Language language) {
        this.language = language;
    }

    /**
     * @return scale.
     */
    public String getScale() {
        return scale;
    }
    /**
     * @param scale the scale to set
     */
    public void setScale(final String scale) {
        this.scale = scale;
    }
    /**
     * @return title.
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }
}
