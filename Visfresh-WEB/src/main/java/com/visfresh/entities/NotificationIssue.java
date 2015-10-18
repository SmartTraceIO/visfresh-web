/**
 *
 */
package com.visfresh.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "notificationissues")
public abstract class NotificationIssue implements EntityWithId {
    /**
     * Entity ID.
     */
    @Id
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    /**
     * Date of occurrence.
     */
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
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
}
