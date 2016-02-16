/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipment implements EntityWithCompany,
        EntityWithId<Long>, Comparable<AutoStartShipment> {
    private Long id;
    private int priority = 0;
    private Company company;
    private ShipmentTemplate template;
    private final List<LocationProfile> shippedFrom = new LinkedList<>();
    private final List<LocationProfile> shippedTo = new LinkedList<>();

    /**
     * Default constructor.
     */
    public AutoStartShipment() {
        super();
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
     * @return the company
     */
    @Override
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Company company) {
        this.company = company;
    }
    /**
     * @return the template
     */
    public ShipmentTemplate getTemplate() {
        return template;
    }
    /**
     * @param template the template to set
     */
    public void setTemplate(final ShipmentTemplate template) {
        this.template = template;
    }
    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }
    /**
     * @param priority the priority to set
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }
    /**
     * @return the shippedFrom
     */
    public List<LocationProfile> getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @return the shippedTo
     */
    public List<LocationProfile> getShippedTo() {
        return shippedTo;
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final AutoStartShipment o) {
        if (getPriority() > o.getPriority()) {
            return -1;
        }
        if (o.getPriority() > getPriority()) {
            return 1;
        }
        return getId().compareTo(o.getId());
    }
}
