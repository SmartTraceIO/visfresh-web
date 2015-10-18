/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="notificationsschedules")
public class NotificationSchedule implements EntityWithId {
    /**
     * Notification schedule ID.
     */
    @Id
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    private Long id;
    /**
     * Company
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "company",
        foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT),
        columnDefinition = "bigint",
        referencedColumnName = "id")
    private Company company;
    /**
     * Notification schedule name.
     */
    @Column(nullable = false)
    private String name;
    /**
     * Description.
     */
    @Column
    private String description;
    /**
     * Personal schedules
     */
    @OneToMany(cascade = {CascadeType.ALL})
    private final List<SchedulePersonHowWhen> schedules = new LinkedList<SchedulePersonHowWhen>();

    /**
     * Default constructor.
     */
    public NotificationSchedule() {
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
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }
    /**
     * @return the schedules
     */
    public List<SchedulePersonHowWhen> getSchedules() {
        return schedules;
    }
    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Company company) {
        this.company = company;
    }
}
