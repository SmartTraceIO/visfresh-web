/**
 *
 */
package com.visfresh.io.shipment;

import java.util.Date;

import com.visfresh.entities.Arrival;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalBean extends NotificationIssueBean {
    //Arrival
    //Notified when 50km away
    private Integer mettersForArrival;
    //Notified at 12:39 12 Jun 2016
    private Date notifiedAt;
    /**
     * Default constructor.
     */
    public ArrivalBean() {
        super();
    }
    /**
     * Default constructor.
     */
    public ArrivalBean(final Arrival arrival) {
        super(arrival);
        setNotifiedAt(arrival.getDate());
        setMettersForArrival(arrival.getNumberOfMettersOfArrival());
    }
    /**
     * @return the notifiedWhenKm
     */
    public Integer getMettersForArrival() {
        return mettersForArrival;
    }
    /**
     * @param notifiedWhenKm the notifiedWhenKm to set
     */
    public void setMettersForArrival(final Integer notifiedWhenKm) {
        this.mettersForArrival = notifiedWhenKm;
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
}
