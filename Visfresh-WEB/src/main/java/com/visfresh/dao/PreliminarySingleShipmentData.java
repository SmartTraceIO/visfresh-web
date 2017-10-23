/**
 *
 */
package com.visfresh.dao;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PreliminarySingleShipmentData {
    private Long shipment;
    private Long company;
    private Set<Long> siblings = new HashSet<>();

    /**
     * Default constructor.
     */
    public PreliminarySingleShipmentData() {
        super();
    }

    /**
     * @return the shipment
     */
    public Long getShipment() {
        return shipment;
    }

    /**
     * @param shipment the shipment to set
     */
    public void setShipment(final Long shipment) {
        this.shipment = shipment;
    }
    /**
     * @return the company
     */
    public Long getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Long company) {
        this.company = company;
    }
    /**
     * @return the siblings
     */
    public Set<Long> getSiblings() {
        return siblings;
    }
    /**
     * @param siblings the siblings to set
     */
    public void setSiblings(final Set<Long> siblings) {
        this.siblings = siblings;
    }
}
