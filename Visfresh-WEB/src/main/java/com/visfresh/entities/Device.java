/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="devices")
public class Device {
    private String sn;
    @Id
    private String id;
    private String imei;
    private String name;
    private String description;
    @ManyToOne
    private Company company;

    /**
     * Default constructor.
     */
    public Device() {
        super();
    }

    /**
     * @return the sn
     */
    public String getSn() {
        return sn;
    }
    /**
     * @param sn the sn to set
     */
    public void setSn(final String sn) {
        this.sn = sn;
    }
    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
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
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }
    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Company company) {
        this.company = company;
    }
}
