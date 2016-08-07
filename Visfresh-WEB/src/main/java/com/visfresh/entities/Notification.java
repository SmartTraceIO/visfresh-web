/**
 *
 */
package com.visfresh.entities;



/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Notification implements EntityWithId<Long> {
    /**
     * Notification ID.
     */
    private Long id;
    /**
     * Notification type.
     */
    private NotificationType type;
    /**
     * The user
     */
    private User user;
    /**
     * Is notification read flag.
     */
    private boolean isRead;
    /**
     * Notification issue.
     */
    private NotificationIssue issue;
    /**
     * Whether or not notification should be visible for user.
     */
    private boolean isHidden;

    /**
     * Default constructor.
     */
    public Notification() {
        super();
    }

    /**
     * @return the issue
     */
    public NotificationIssue getIssue() {
        return issue;
    }
    /**
     * @param issue the issue to set
     */
    public void setIssue(final NotificationIssue issue) {
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
    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }
    /**
     * @param user the user to set
     */
    public void setUser(final User user) {
        this.user = user;
    }
    /**
     * @return the isRead
     */
    public boolean isRead() {
        return isRead;
    }
    /**
     * @param isRead the isRead to set
     */
    public void setRead(final boolean isRead) {
        this.isRead = isRead;
    }
    /**
     * @return the isHidden
     */
    public boolean isHidden() {
        return isHidden;
    }
    /**
     * @param isHidden the isHidden to set
     */
    public void setHidden(final boolean isHidden) {
        this.isHidden = isHidden;
    }
}
