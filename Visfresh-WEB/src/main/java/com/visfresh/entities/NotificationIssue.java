/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class NotificationIssue implements EntityWithId<Long>, Comparable<NotificationIssue> {
    /**
     * Entity ID.
     */
    private Long id;
    /**
     * Date of occurrence.
     */
    private Date date;

    /**
     * Default constructor.
     */
    public NotificationIssue() {
        super();
    }
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(final Date date) {
        this.date = date;
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
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final NotificationIssue o) {
        return getDate().compareTo(o.getDate());
    }
}
