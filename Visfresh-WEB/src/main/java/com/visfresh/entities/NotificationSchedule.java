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
public class NotificationSchedule implements EntityWithId<Long>, EntityWithCompany {
    /**
     * Notification schedule ID.
     */
    private Long id;
    /**
     * Company
     */
    private Long company;
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
    private final List<PersonSchedule> schedules = new LinkedList<PersonSchedule>();

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
    public List<PersonSchedule> getSchedules() {
        return schedules;
    }
    /**
     * @return the company
     */
    @Override
    public Long getCompanyId() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Long company) {
        this.company = company;
    }
}
