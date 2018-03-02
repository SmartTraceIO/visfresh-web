/**
 *
 */
package com.visfresh.entities;

import java.util.Date;
import java.util.TimeZone;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Company implements EntityWithId<Long>, EntityWithCompany {
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

    private String address;
    private String contactPerson;
    private String email;
    private TimeZone timeZone;
    private Date startDate;
    private String trackersEmail;
    private PaymentMethod paymentMethod;
    private String billingPerson;
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
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithCompany#getCompany()
     */
    @Override
    public Long getCompanyId() {
        return getId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithCompany#setCompany(com.visfresh.entities.Company)
     */
    @Override
    public void setCompany(final Long c) {
    }
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }
    /**
     * @param address the address to set
     */
    public void setAddress(final String address) {
        this.address = address;
    }
    /**
     * @return the contactPerson
     */
    public String getContactPerson() {
        return contactPerson;
    }
    /**
     * @param contactPerson the contactPerson to set
     */
    public void setContactPerson(final String contactPerson) {
        this.contactPerson = contactPerson;
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
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }
    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }
    /**
     * @return the trackersEmail
     */
    public String getTrackersEmail() {
        return trackersEmail;
    }
    /**
     * @param trackersEmail the trackersEmail to set
     */
    public void setTrackersEmail(final String trackersEmail) {
        this.trackersEmail = trackersEmail;
    }
    /**
     * @return the paymentMethod
     */
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    /**
     * @param paymentMethod the paymentMethod to set
     */
    public void setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    /**
     * @return the billingPerson
     */
    public String getBillingPerson() {
        return billingPerson;
    }
    /**
     * @param billingPerson the billingPerson to set
     */
    public void setBillingPerson(final String billingPerson) {
        this.billingPerson = billingPerson;
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
}
