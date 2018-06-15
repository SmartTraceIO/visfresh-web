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
    private ShortUserInfo user;
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
    /**
     * @param monday
     * @param tuesday
     * @param wednesday
     * @param thursday
     * @param friday
     * @param saturday
     * @param sunday
     */
    public void setWeekDays(final boolean monday, final boolean tuesday, final boolean wednesday,
            final boolean thursday, final boolean friday, final boolean saturday, final boolean sunday) {
        this.weekDays[0] = monday;
        this.weekDays[1] = tuesday;
        this.weekDays[2] = wednesday;
        this.weekDays[3] = thursday;
        this.weekDays[4] = friday;
        this.weekDays[5] = saturday;
        this.weekDays[6] = sunday;
    }
    /**
     * Sets all weeks days.
     */
    public void setAllWeek() {
        setWeekDays(true, true, true, true, true, true, true);
    }

    /**
     * @return user ID.
     */
    public ShortUserInfo getUser() {
        return user;
    }

    /**
     * @param u the userId to set
     */
    public void setUser(final ShortUserInfo u) {
        this.user = u;
    }
}
