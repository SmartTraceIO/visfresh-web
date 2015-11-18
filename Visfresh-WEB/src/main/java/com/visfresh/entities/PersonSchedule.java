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
     * Whether or not should create notification object for user.
     */
    private boolean sendApp;
    /**
     * Whether or not should send Email for user.
     */
    private boolean sendEmail;
    /**
     * Whether or not should send SMS to user.
     */
    private boolean sendSms;

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
    public boolean isSendApp() {
        return sendApp;
    }
    /**
     * @param pushToMobileApp the pushToMobileApp to set
     */
    public void setSendApp(final boolean pushToMobileApp) {
        this.sendApp = pushToMobileApp;
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
    /**
     * @return the true if should send email to user.
     */
    public boolean isSendEmail() {
        return sendEmail;
    }
    /**
     * @return true if should send SMS to user, false otherwise.
     */
    public boolean isSendSms() {
        return sendSms;
    }
    /**
     * @param sendEmail the sendEmail to set
     */
    public void setSendEmail(final boolean sendEmail) {
        this.sendEmail = sendEmail;
    }
    /**
     * @param sendSms the sendSms to set
     */
    public void setSendSms(final boolean sendSms) {
        this.sendSms = sendSms;
    }
}
