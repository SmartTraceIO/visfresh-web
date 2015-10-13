/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Device {
    private String sn;
    private String imei;
    private String name;
    private String description;

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
}
