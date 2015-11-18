/**
 *
 */
package com.visfresh.services.lists;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ListAlertProfileItem {
//    "alertProfileId": 1,
    private long alertProfileId;
//    "alertProfileName": "AnyAlert",
    private String alertProfileName;
//    "alertProfileDescription": "Any description",
    private String alertProfileDescription;
//    "alertRuleList": " >5 for 60min, >5 for 60min in total, <0 for 120min, light, lightOff, shock"
    private final List<String> alertRuleList = new LinkedList<String>();

    /**
     * Default constructor.
     */
    public ListAlertProfileItem() {
        super();
    }

    /**
     * @return the alertProfileId
     */
    public long getAlertProfileId() {
        return alertProfileId;
    }
    /**
     * @param alertProfileId the alertProfileId to set
     */
    public void setAlertProfileId(final long alertProfileId) {
        this.alertProfileId = alertProfileId;
    }
    /**
     * @return the alertProfileName
     */
    public String getAlertProfileName() {
        return alertProfileName;
    }
    /**
     * @param alertProfileName the alertProfileName to set
     */
    public void setAlertProfileName(final String alertProfileName) {
        this.alertProfileName = alertProfileName;
    }
    /**
     * @return the alertProfileDescription
     */
    public String getAlertProfileDescription() {
        return alertProfileDescription;
    }
    /**
     * @param alertProfileDescription the alertProfileDescription to set
     */
    public void setAlertProfileDescription(final String alertProfileDescription) {
        this.alertProfileDescription = alertProfileDescription;
    }
    /**
     * @return the alertRuleList
     */
    public List<String> getAlertRuleList() {
        return alertRuleList;
    }
}
