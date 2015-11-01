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

    //person to notify
    /**
     * First name.
     */
    private String firstName;
    /**
     * Last name.
     */
    private String lastName;
    /**
     * Company description.
     */
    private String company;
    /**
     * Position
     */
    private String position;

    //how to notify
    /**
     * Phone number for SMS notification.
     */
    private String smsNotification;
    /**
     * Email address for email notificaition
     */
    private String emailNotification;
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
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }
    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }
    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }
    /**
     * @param lastName the lastName to set
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }
    /**
     * @return the company
     */
    public String getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final String company) {
        this.company = company;
    }
    /**
     * @return the position
     */
    public String getPosition() {
        return position;
    }
    /**
     * @param position the position to set
     */
    public void setPosition(final String position) {
        this.position = position;
    }
    /**
     * @return the smsNotification
     */
    public String getSmsNotification() {
        return smsNotification;
    }
    /**
     * @param smsNotification the smsNotification to set
     */
    public void setSmsNotification(final String smsNotification) {
        this.smsNotification = smsNotification;
    }
    /**
     * @return the emailNotification
     */
    public String getEmailNotification() {
        return emailNotification;
    }
    /**
     * @param emailNotification the emailNotification to set
     */
    public void setEmailNotification(final String emailNotification) {
        this.emailNotification = emailNotification;
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
}