/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PersonSchedule implements EntityWithId<Long> {
    /**
     * Personal schedule ID.
     */
    private Long id;

    /**
     * Whether or not should
     */
    private boolean pushToMobileApp;

    //when to notify
    /**
     * Week days.
     */
    private final boolean[] weekDays = new boolean[7];

    /**
     * Start minute from start of day.
     */
    private int fromTime;
    /**
     * End minute from start of day.
     */
    private int toTime;
    /**
     * Person to notify.
     */
    private User user;

    /**
     *
     */
    public PersonSchedule() {
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
     * @return the pushToMobileApp
     */
    public boolean isPushToMobileApp() {
        return pushToMobileApp;
    }
    /**
     * @param pushToMobileApp the pushToMobileApp to set
     */
    public void setPushToMobileApp(final boolean pushToMobileApp) {
        this.pushToMobileApp = pushToMobileApp;
    }
    /**
     * @return the fromMinute
     */
    public int getFromTime() {
        return fromTime;
    }
    /**
     * @param fromMinute the fromMinute to set
     */
    public void setFromTime(final int fromMinute) {
        this.fromTime = fromMinute;
    }
    /**
     * @return the forMinute
     */
    public int getToTime() {
        return toTime;
    }
    /**
     * @param forMinute the forMinute to set
     */
    public void setToTime(final int forMinute) {
        this.toTime = forMinute;
    }
    /**
     * @return the weekDays
     */
    public boolean[] getWeekDays() {
        return weekDays;
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
}
