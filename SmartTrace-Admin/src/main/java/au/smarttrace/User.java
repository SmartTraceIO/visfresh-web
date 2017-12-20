/**
 *
 */
package au.smarttrace;

import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class User {
    /**
     * User ID.
     */
    private Long id;
    /**
     * Company.
     */
    private Long company;
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
     * Time Zone
     */
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");
    /**
     * Set of roles.
     */
    private final Set<String> roles = new HashSet<>();
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
    private String title = "Mr";
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
    private boolean active;
    /**
     * External company name.
     */
    private String externalCompany;
    /**
     * External flag.
     */
    private boolean external;

    /**
     * Default constructor.
     */
    public User() {
        super();
    }

    /**
     * @param id the id to set
     */
    @JsonSetter("id")
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the id
     */
    @JsonGetter("id")
    public Long getId() {
        return id;
    }
    /**
     * @return the title
     */
    @JsonGetter("title")
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    @JsonSetter("title")
    public void setTitle(final String title) {
        this.title = title;
    }
    /**
     * @return the firstName
     */
    @JsonGetter("firstName")
    public String getFirstName() {
        return firstName;
    }
    /**
     * @param firstName the firstName to set
     */
    @JsonSetter("firstName")
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }
    /**
     * @return the lastName
     */
    @JsonGetter("lastName")
    public String getLastName() {
        return lastName;
    }
    /**
     * @param lastName the lastName to set
     */
    @JsonSetter("lastName")
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the external
     */
    @JsonGetter("external")
    public boolean isExternal() {
        return external;
    }
    /**
     * @param external the external to set
     */
    @JsonSetter("external")
    public void setExternal(final boolean external) {
        this.external = external;
    }
    /**
     * @param externalCompany the externalCompany to set
     */
    @JsonSetter("externalCompany")
    public void setExternalCompany(final String externalCompany) {
        this.externalCompany = externalCompany;
    }
    /**
     * @return the externalCompany
     */
    @JsonGetter("externalCompany")
    public String getExternalCompany() {
        return externalCompany;
    }
    /**
     * @param position the position to set
     */
    @JsonSetter("position")
    public void setPosition(final String position) {
        this.position = position;
    }
    /**
     * @return the position
     */
    @JsonGetter("position")
    public String getPosition() {
        return position;
    }
    /**
     * @return the email address.
     */
    @JsonGetter("email")
    public String getEmail() {
        return email;
    }
    /**
     * @param email the email address.
     */
    @JsonSetter("email")
    public void setEmail(final String email) {
        this.email = email;
    }
    /**
     * @return the phone
     */
    @JsonGetter("phone")
    public String getPhone() {
        return phone;
    }
    /**
     * @param phone the phone to set
     */
    @JsonSetter("phone")
    public void setPhone(final String phone) {
        this.phone = phone;
    }
    /**
     * @return the timeZone
     */
    @JsonGetter("timeZone")
    public TimeZone getTimeZone() {
        return timeZone;
    }
    /**
     * @param timeZone the timeZone to set
     */
    @JsonSetter("timeZone")
    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    /**
     * @return the temperatureUnits
     */
    @JsonGetter("temperatureUnits")
    public TemperatureUnits getTemperatureUnits() {
        return temperatureUnits;
    }
    /**
     * @param temperatureUnits the temperatureUnits to set
     */
    @JsonSetter("temperatureUnits")
    public void setTemperatureUnits(final TemperatureUnits temperatureUnits) {
        this.temperatureUnits = temperatureUnits;
    }
    /**
     * @return the deviceGroup
     */
    @JsonGetter("deviceGroup")
    public String getDeviceGroup() {
        return deviceGroup;
    }
    /**
     * @param deviceGroup the deviceGroup to set
     */
    @JsonSetter("deviceGroup")
    public void setDeviceGroup(final String deviceGroup) {
        this.deviceGroup = deviceGroup;
    }
    /**
     * @return the language
     */
    @JsonGetter("language")
    public Language getLanguage() {
        return language;
    }
    /**
     * @param language the language to set
     */
    @JsonSetter("language")
    public void setLanguage(final Language language) {
        this.language = language;
    }
    /**
     * @return the measurementUnits
     */
    @JsonGetter("measurementUnits")
    public MeasurementUnits getMeasurementUnits() {
        return measurementUnits;
    }
    /**
     * @param measurementUnits the measurementUnits to set
     */
    @JsonSetter("measurementUnits")
    public void setMeasurementUnits(final MeasurementUnits measurementUnits) {
        this.measurementUnits = measurementUnits;
    }
    /**
     * @return the active
     */
    @JsonGetter("active")
    public boolean isActive() {
        return active;
    }
    /**
     * @param active the active to set
     */
    @JsonSetter("active")
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * @return roles
     */
    @JsonGetter("roles")
    public Set<String> getRoles() {
        return roles;
    }
    /**
     * @return the company
     */
    @JsonGetter("internalCompanyId")
    public Long getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @JsonSetter("internalCompanyId")
    public void setCompany(final Long company) {
        this.company = company;
    }
}
