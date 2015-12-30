/**
 *
 */
package com.visfresh.services;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalEstimation {
    private Date arrivalDate;
    private int percentageComplete;

    /**
     * Default constructor.
     */
    public ArrivalEstimation() {
        super();
    }
    /**
     * @param arrivalDate arrival date.
     * @param percentage percentage.
     */
    public ArrivalEstimation(final Date arrivalDate, final int percentage) {
        super();
        setArrivalDate(arrivalDate);
        setPercentageComplete(percentage);
    }

    /**
     * @return the arrivalDate
     */
    public Date getArrivalDate() {
        return arrivalDate;
    }
    /**
     * @param arrivalDate the arrivalDate to set
     */
    public void setArrivalDate(final Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }
    /**
     * @return the percentageComplete
     */
    public int getPercentageComplete() {
        return percentageComplete;
    }
    /**
     * @param percentageComplete the percentageComplete to set
     */
    public void setPercentageComplete(final int percentageComplete) {
        this.percentageComplete = percentageComplete;
    }
}
