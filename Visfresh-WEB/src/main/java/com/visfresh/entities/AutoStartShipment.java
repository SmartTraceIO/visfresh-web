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
    private Long company;
    private ShipmentTemplate template;
    private final List<LocationProfile> shippedFrom = new LinkedList<>();
    private final List<LocationProfile> shippedTo = new LinkedList<>();
    private final List<LocationProfile> interimStops = new LinkedList<>();
    private boolean startOnLeaveLocation;

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
    public Long getCompanyId() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Long company) {
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
    /**
     * @return the interimStops
     */
    public List<LocationProfile> getInterimStops() {
        return interimStops;
    }
    /**
     * @return the startOnLeaveLocation
     */
    public boolean isStartOnLeaveLocation() {
        return startOnLeaveLocation;
    }
    /**
     * @param startOnLeaveLocation the startOnLeaveLocation to set
     */
    public void setStartOnLeaveLocation(final boolean startOnLeaveLocation) {
        this.startOnLeaveLocation = startOnLeaveLocation;
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
