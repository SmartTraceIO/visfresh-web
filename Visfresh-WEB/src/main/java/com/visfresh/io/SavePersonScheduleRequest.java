/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.PersonSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SavePersonScheduleRequest {
    private Long notificationScheduleId;
    private PersonSchedule schedule;

    /**
     * Default constructor.
     */
    public SavePersonScheduleRequest() {
        super();
    }

    /**
     * @return the notificationScheduleId
     */
    public Long getNotificationScheduleId() {
        return notificationScheduleId;
    }
    /**
     * @param notificationScheduleId the notificationScheduleId to set
     */
    public void setNotificationScheduleId(final Long notificationScheduleId) {
        this.notificationScheduleId = notificationScheduleId;
    }
    /**
     * @return the schedule
     */
    public PersonSchedule getSchedule() {
        return schedule;
    }
    /**
     * @param schedule the schedule to set
     */
    public void setSchedule(final PersonSchedule schedule) {
        this.schedule = schedule;
    }
}
