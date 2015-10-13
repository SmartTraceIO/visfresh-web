/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Notification implements EntityWithId {
    /**
     * Notification ID.
     */
    private Long id;
    /**
     * Notification type.
     */
    private NotificationType type;
    /**
     * Notification issue.
     */
    private Object issue;

    /**
     * Default constructor.
     */
    public Notification() {
        super();
    }

    /**
     * @return the issue
     */
    public Object getIssue() {
        return issue;
    }
    /**
     * @param issue the issue to set
     */
    public void setIssue(final Object issue) {
        this.issue = issue;
    }
    /**
     * @return the type
     */
    public NotificationType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final NotificationType type) {
        this.type = type;
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
}
