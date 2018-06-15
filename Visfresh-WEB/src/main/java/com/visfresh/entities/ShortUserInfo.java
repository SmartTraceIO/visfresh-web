/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortUserInfo {
    /**
     * User ID.
     */
    private Long id;
    /**
     * First user name.
     */
    private String firstName;
    /**
     * Last user name.
     */
    private String lastName;
    /**
     * Email address.
     */
    private String email;
    /**
     * Phone number
     */
    private String phone;
    /**
     * Active Flag.
     */
    private Boolean active;

    /**
     * Default constructor.
     */
    public ShortUserInfo() {
        super();
    }

    /**
     * @return the id
     */
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
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }
    public boolean isActive() {
        return Boolean.TRUE.equals(getActive());
    }
    /**
     * @param active the active to set
     */
    public void setActive(final Boolean active) {
        this.active = active;
    }
}
