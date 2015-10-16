/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="notifications")
public class Notification implements EntityWithId {
    /**
     * Notification ID.
     */
    @Id
    @GeneratedValue
    private Long id;
    /**
     * Notification type.
     */
    private NotificationType type;
    /**
     * The user
     */
    @ManyToOne(targetEntity = User.class)
    private User user;
    /**
     * Notification issue.
     */
    private EntityWithId issue;

    /**
     * Default constructor.
     */
    public Notification() {
        super();
    }

    /**
     * @return the issue
     */
    public EntityWithId getIssue() {
        return issue;
    }
    /**
     * @param issue the issue to set
     */
    public void setIssue(final EntityWithId issue) {
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
