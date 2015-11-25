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
public class User implements EntityWithId<Long>, EntityWithCompany {
    /**
     * User ID.
     */
    private Long id;
    /**
     * Company.
     */
    private Company company;
    /**
     * First user name.
     */
    private String firstName;
    /**
     * Last user name.
     */
    private String lastName;
    /**
     * User's position in company.
     */
    private String position;
    /**
     * Email address.
     */
    private String email;
    /**
     * Phone number
     */
    private String phone;
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
     * Authorized device group.
     */
    private String deviceGroup;
    /**
     * User title Mr/Mrs
     */
    private String title;
    /**
     * User scale
     */
    private String scale;
    /**
     * User language.
     */
    private Language language = Language.English;
    /**
     * User measurement units.
     */
    private MeasurementUnits measurementUnits = MeasurementUnits.Metric;
    /**
     * Active Flag.
     */
    private boolean active = true;

    /**
     * Default constructor.
     */
    public User() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
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
     * @return authorized device group.
     */
    public String getAuthorizedDeviceGroup() {
        return deviceGroup;
    }
    /**
     * @param group the authorizedDeviceGroup to set
     */
    public void setAuthorizedDeviceGroup(final String group) {
        this.deviceGroup = group;
    }
    /**
     * @return the deviceGroup
     */
    public String getDeviceGroup() {
        return deviceGroup;
    }
    /**
     * @param deviceGroup the deviceGroup to set
     */
    public void setDeviceGroup(final String deviceGroup) {
        this.deviceGroup = deviceGroup;
    }
    /**
     * @return the title
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
    /**
     * @return the scale
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
     * @return the language
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
     * @return the measurementUnits
     */
    public MeasurementUnits getMeasurementUnits() {
        return measurementUnits;
    }
    /**
     * @param measurementUnits the measurementUnits to set
     */
    public void setMeasurementUnits(final MeasurementUnits measurementUnits) {
        this.measurementUnits = measurementUnits;
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
}
