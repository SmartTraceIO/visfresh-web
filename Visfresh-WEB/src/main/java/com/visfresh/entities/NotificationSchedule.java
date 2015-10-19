/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationSchedule implements EntityWithId<Long> {
    /**
     * Notification schedule ID.
     */
    private Long id;
    /**
     * Company
     */
    private Company company;
    /**
     * Notification schedule name.
     */
    private String name;
    /**
     * Description.
     */
    private String description;
    /**
     * Personal schedules
     */
    private final List<PersonalSchedule> schedules = new LinkedList<PersonalSchedule>();

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
    public List<PersonalSchedule> getSchedules() {
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
