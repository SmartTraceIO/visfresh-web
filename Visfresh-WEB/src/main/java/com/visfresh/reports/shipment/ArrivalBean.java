/**
 *
 */
package com.visfresh.reports.shipment;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalBean {
    //Arrival
    //Time of arrival 12:53 12 Jun 2016
    private Date time;
    //Notified when 50km away
    private Integer notifiedWhenKm;
    //Notified at 12:39 12 Jun 2016
    private Date notifiedAt;
//    //Who was notified Rob Arpas, Rob Arpas, Ashu Kafle, Ashu Kafle, Robert Annabel
//    private final List<String> whoIsNotified = new LinkedList<>();
//    //Schedule(s): High Temp en route COLES DC adelaide
//    private final List<String> schedules = new LinkedList<>();
    //Time of shutdown 14:53 12 Jun 2016
    private Date shutdownTime;
    /**
     * Default constructor.
     */
    public ArrivalBean() {
        super();
    }
    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final Date time) {
        this.time = time;
    }
    /**
     * @return the notifiedWhenKm
     */
    public Integer getNotifiedWhenKm() {
        return notifiedWhenKm;
    }
    /**
     * @param notifiedWhenKm the notifiedWhenKm to set
     */
    public void setNotifiedWhenKm(final Integer notifiedWhenKm) {
        this.notifiedWhenKm = notifiedWhenKm;
    }
    /**
     * @return the notifiedAt
     */
    public Date getNotifiedAt() {
        return notifiedAt;
    }
    /**
     * @param notifiedAt the notifiedAt to set
     */
    public void setNotifiedAt(final Date notifiedAt) {
        this.notifiedAt = notifiedAt;
    }
    /**
     * @return the shutdownTime
     */
    public Date getShutdownTime() {
        return shutdownTime;
    }
    /**
     * @param shutdownTime the shutdownTime to set
     */
    public void setShutdownTime(final Date shutdownTime) {
        this.shutdownTime = shutdownTime;
    }
//    /**
//     * @return the whoIsNotified
//     */
//    public List<String> getWhoIsNotified() {
//        return whoIsNotified;
//    }
//    /**
//     * @return the schedules
//     */
//    public List<String> getSchedules() {
//        return schedules;
//    }
}
