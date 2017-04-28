/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentUserDto {
    private Long id;
    private String email;

    /**
     * Default constructor.
     */
    public ShipmentUserDto() {
        super();
    }
    /**
     * Default constructor.
     */
    public ShipmentUserDto(final User u) {
        super();
        setId(u.getId());
        setEmail(u.getEmail());
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
}
