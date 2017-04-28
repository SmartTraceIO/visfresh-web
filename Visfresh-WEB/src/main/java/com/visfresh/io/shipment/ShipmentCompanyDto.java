/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentCompanyDto {
    private Long id;
    private String name;

    /**
     * Default constructor.
     */
    public ShipmentCompanyDto() {
        super();
    }
    /**
     * @param c company.
     */
    public ShipmentCompanyDto(final Company c) {
        super();
        setId(c.getId());
        setName(c.getName());
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
}
