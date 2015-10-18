/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
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
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    private Long id;
    /**
     * Notification type.
     */
    @Column(nullable = false)
    @Enumerated
    private NotificationType type;
    /**
     * The user
     */
    @ManyToOne
    @JoinColumn(name = "user")
    private User user;
    /**
     * Notification issue.
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "issue",
        foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT),
        columnDefinition = "bigint",
        referencedColumnName = "id")
    private NotificationIssue issue;

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
}
