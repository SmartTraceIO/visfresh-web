/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="notificationsschedules")
public class NotificationSchedule implements EntityWithId {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String description;
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
}
