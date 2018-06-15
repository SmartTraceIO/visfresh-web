/**
 *
 */
package com.visfresh.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class User extends ShortUserInfo implements EntityWithId<Long>,
        EntityWithCompany, Cloneable {
    /**
     * Company.
     */
    private Long company;
    /**
     * User's position in company.
     */
    private String position;
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
    private Set<Role> roles;
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
     * User language.
     */
    private Language language = Language.English;
    /**
     * User measurement units.
     */
    private MeasurementUnits measurementUnits = MeasurementUnits.Metric;
    /**
     * External company name.
     */
    private String externalCompany;
    /**
     * External flag.
     */
    private Boolean external;
    /**
     * User settings.
     */
    private final Map<String, String> settings = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public User() {
        super();
    }

    /**
     * @return the roles
     */
    public Set<Role> getRoles() {
        return roles;
    }
    /**
     * @param convertToEntityAttribute
     */
    public void setRoles(final Collection<Role> roles) {
        this.roles = new HashSet<Role>();
        this.roles.addAll(roles);
    }
    /**
     * @return the company
     */
    @Override
    public Long getCompanyId() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Long company) {
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
     * @param position the position to set
     */
    public void setPosition(final String position) {
        this.position = position;
    }
    /**
     * @return the position
     */
    public String getPosition() {
        return position;
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
     * @return the external company name
     */
    public String getExternalCompany() {
        return externalCompany;
    }
    /**
     * @param name the external company name.
     */
    public void setExternalCompany(final String name) {
        this.externalCompany = name;
    }
    /**
     * @return the external
     */
    public Boolean getExternal() {
        return external;
    }
    public boolean isExternal() {
        return Boolean.TRUE.equals(getExternal());
    }
    /**
     * @param external the external to set
     */
    public void setExternal(final Boolean external) {
        this.external = external;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public User clone() {
        User u;
        try {
            u = (User) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }

        final Set<Role> r = new HashSet<Role>(u.getRoles());
        u.roles = r;
        return u;
    }
    /**
     * @return the settings
     */
    public Map<String, String> getSettings() {
        return settings;
    }
}
