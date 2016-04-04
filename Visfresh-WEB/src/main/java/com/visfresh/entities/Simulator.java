/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Simulator implements EntityWithId<Long> {
    private User user;
    private Device source;
    private Device target;
    private Date startDate;
    private Date endDate;
    private int velosity;

    /**
     * Default constructor.
     */
    public Simulator() {
        super();
    }

    /**
     * @param u user.
     */
    public void setUser(final User u) {
        this.user = u;
    }
    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }
    /**
     * @param d source device.
     */
    public void setSource(final Device d) {
        this.source = d;
    }
    /**
     * @return the source
     */
    public Device getSource() {
        return source;
    }
    /**
     * @param d target device.
     */
    public void setTarget(final Device d) {
        this.target = d;
    }
    /**
     * @return the target
     */
    public Device getTarget() {
        return target;
    }
    /* (non-Javadoc)
     * @see com.visfresh.entities.EntityWithId#getId()
     */
    @Override
    public Long getId() {
        return getUser() == null ? null : getUser().getId();
    }
    /**
     * @param date start date.
     */
    public void setStartDate(final Date date) {
        this.startDate = date;
    }
    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }
    /**
     * @param date start date.
     */
    public void setEndDate(final Date date) {
        this.endDate = date;
    }
    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }
    public void setVelosity(final int v) {
        this.velosity = v;
    }
    /**
     * @return the velosity
     */
    public int getVelosity() {
        return velosity;
    }
}
