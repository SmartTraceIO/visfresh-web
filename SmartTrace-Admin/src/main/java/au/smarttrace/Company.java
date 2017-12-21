/**
 *
 */
package au.smarttrace;

import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Company {
    /**
     * Company ID.
     */
    private Long id;

    /**
     * Company name.
     */
    private String name;
    /**
     * Company description
     */
    private String description;
    /**
     * Address.
     */
    private String address;
    /**
     * Contact person.
     */
    private String contactPerson;
    /**
     * Email.
     */
    private String email;
    /**
     * Time Zone
     */
    private TimeZone timeZone;
    /**
     * Start date.
     */
    private Date startDate;
    /**
     * Tracker's email
     */
    private String trackersEmail;
    /**
     * Payment method.
     */
    private PaymentMethod paymentMethod;
    /**
     * Billing person.
     */
    private String billingPerson;
    /**
     * Language.
     */
    private Language language;

    /**
     * Default constructor.
     */
    public Company() {
        super();
    }

    /**
     * @param l
     */
    public Company(final long l) {
        super();
        setId(l);
    }

    /**
     * @return the id
     */
    @JsonGetter("id")
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    @JsonSetter("id")
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the name
     */
    @JsonGetter("name")
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    @JsonSetter("name")
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    @JsonGetter("description")
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    @JsonSetter("description")
    public void setDescription(final String description) {
        this.description = description;
    }
    /**
     * @return the address
     */
    @JsonGetter("address")
    public String getAddress() {
        return address;
    }
    /**
     * @param address the address to set
     */
    @JsonSetter("address")
    public void setAddress(final String address) {
        this.address = address;
    }
    /**
     * @return the contactPerson
     */
    @JsonGetter("contactPerson")
    public String getContactPerson() {
        return contactPerson;
    }
    /**
     * @param contactPerson the contactPerson to set
     */
    @JsonSetter("contactPerson")
    public void setContactPerson(final String contactPerson) {
        this.contactPerson = contactPerson;
    }
    /**
     * @return the email
     */
    @JsonGetter("email")
    public String getEmail() {
        return email;
    }
    /**
     * @param email the email to set
     */
    @JsonSetter("email")
    public void setEmail(final String email) {
        this.email = email;
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
     * @return the startDate
     */
    @JsonGetter("startDate")
    public Date getStartDate() {
        return startDate;
    }
    /**
     * @param startDate the startDate to set
     */
    @JsonSetter("startDate")
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }
    /**
     * @return the trackersEmail
     */
    @JsonGetter("trackersEmail")
    public String getTrackersEmail() {
        return trackersEmail;
    }
    /**
     * @param trackersEmail the trackersEmail to set
     */
    @JsonSetter("trackersEmail")
    public void setTrackersEmail(final String trackersEmail) {
        this.trackersEmail = trackersEmail;
    }
    /**
     * @return the paymentMethod
     */
    @JsonGetter("paymentMethod")
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    /**
     * @param paymentMethod the paymentMethod to set
     */
    @JsonSetter("paymentMethod")
    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    /**
     * @return the billingPerson
     */
    @JsonGetter("billingPerson")
    public String getBillingPerson() {
        return billingPerson;
    }
    /**
     * @param billingPerson the billingPerson to set
     */
    @JsonSetter("billingPerson")
    public void setBillingPerson(final String billingPerson) {
        this.billingPerson = billingPerson;
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
}
