/**
 *
 */
package com.visfresh.reports.shipment;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalBean {
    //Arrival
    //Notified when 50km away
    private Integer notifiedWhenKm;
    //Notified at 12:39 12 Jun 2016
    private Date notifiedAt;
//    //Who was notified Rob Arpas, Rob Arpas, Ashu Kafle, Ashu Kafle, Robert Annabel
//    private final List<String> whoIsNotified = new LinkedList<>();
//    //Schedule(s): High Temp en route COLES DC adelaide
//    private final List<String> schedules = new LinkedList<>();
    //Time of shutdown 14:53 12 Jun 2016
    private final List<String> whoIsNotified = new LinkedList<>();
    /**
     * Default constructor.
     */
    public ArrivalBean() {
        super();
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
    /**
     * @return
     */
    public List<String> getWhoIsNotified() {
        return whoIsNotified;
    }
}
