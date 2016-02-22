/**
 *
 */
package com.visfresh.lists;

import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ListNotificationScheduleItem implements EntityWithId<Long> {
    private long notificationScheduleId;
    private String notificationScheduleName;
    private String notificationScheduleDescription;
    private String peopleToNotify;

    /**
     * Default constructor.
     */
    public ListNotificationScheduleItem() {
        super();
    }
    /**
     * @param s notification schedule.
     */
    public ListNotificationScheduleItem(final NotificationSchedule s) {
        super();
        setNotificationScheduleId(s.getId());
        setNotificationScheduleName(s.getName());
        setNotificationScheduleDescription(s.getDescription());

        final StringBuilder sb = new StringBuilder();
        for (final PersonSchedule ps : s.getSchedules()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(ps.getUser().getFirstName());
            if (ps.getUser().getLastName() != null) {
                sb.append(' ');
                sb.append(ps.getUser().getLastName());
            }
        }

        setPeopleToNotify(sb.toString());
    }

    /**
     * @return the notificationScheduleId
     */
    public long getNotificationScheduleId() {
        return notificationScheduleId;
    }
    /**
     * @param notificationScheduleId the notificationScheduleId to set
     */
    public void setNotificationScheduleId(final long notificationScheduleId) {
        this.notificationScheduleId = notificationScheduleId;
    }
    /**
     * @return the notificationScheduleName
     */
    public String getNotificationScheduleName() {
        return notificationScheduleName;
    }
    /**
     * @param notificationScheduleName the notificationScheduleName to set
     */
    public void setNotificationScheduleName(final String notificationScheduleName) {
        this.notificationScheduleName = notificationScheduleName;
    }
    /**
     * @return the notificationScheduleDescription
     */
    public String getNotificationScheduleDescription() {
        return notificationScheduleDescription;
    }
    /**
     * @param notificationScheduleDescription the notificationScheduleDescription to set
     */
    public void setNotificationScheduleDescription(
            final String notificationScheduleDescription) {
        this.notificationScheduleDescription = notificationScheduleDescription;
    }
    /**
     * @return the peopleToNotify
     */
    public String getPeopleToNotify() {
        return peopleToNotify;
    }
    /**
     * @param peopleToNotify the peopleToNotify to set
     */
    public void setPeopleToNotify(final String peopleToNotify) {
        this.peopleToNotify = peopleToNotify;
    }
    /**
     * @return
     */
    @Override
    public Long getId() {
        return getNotificationScheduleId();
    }
}
