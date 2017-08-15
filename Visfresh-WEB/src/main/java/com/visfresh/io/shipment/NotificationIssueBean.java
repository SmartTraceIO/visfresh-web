/**
 *
 */
package com.visfresh.io.shipment;

import java.util.Date;

import com.visfresh.entities.NotificationIssue;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationIssueBean {
    /**
     * Entity ID.
     */
    private Long id;
    /**
     * Date of occurrence.
     */
    private Date date;
    /**
     * The trackere event ID.
     */
    private Long trackerEventId;

    /**
     * Default constructor.
     */
    public NotificationIssueBean() {
        super();
    }
    public NotificationIssueBean(final NotificationIssue issue) {
        super();
        setDate(issue.getDate());
        setId(issue.getId());
        setTrackerEventId(issue.getTrackerEventId());
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
     * @return the trackerEventId
     */
    public Long getTrackerEventId() {
        return trackerEventId;
    }
    /**
     * @param trackerEventId the trackerEventId to set
     */
    public void setTrackerEventId(final Long trackerEventId) {
        this.trackerEventId = trackerEventId;
    }
}
