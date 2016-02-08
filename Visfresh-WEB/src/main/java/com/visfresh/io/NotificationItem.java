/**
 *
 */
package com.visfresh.io;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationItem {
    private Long notificationId;
    private boolean closed;
    private String date;
    private String type;
    private String alertType;
    private String title;
    private Long alertId;
    private Long shipmentId;
    private final List<String> lines = new LinkedList<>();

    /**
     * Default constructor.
     */
    public NotificationItem() {
        super();
    }

    /**
     * @return the notificationId
     */
    public Long getNotificationId() {
        return notificationId;
    }
    /**
     * @param notificationId the notificationId to set
     */
    public void setNotificationId(final Long notificationId) {
        this.notificationId = notificationId;
    }
    /**
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }
    /**
     * @param closed the closed to set
     */
    public void setClosed(final boolean closed) {
        this.closed = closed;
    }
    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(final String date) {
        this.date = date;
    }
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }
    /**
     * @return the alertType
     */
    public String getAlertType() {
        return alertType;
    }
    /**
     * @param alertType the alertType to set
     */
    public void setAlertType(final String alertType) {
        this.alertType = alertType;
    }
    /**
     * @return the alertId
     */
    public Long getAlertId() {
        return alertId;
    }
    /**
     * @param alertId the alertId to set
     */
    public void setAlertId(final Long alertId) {
        this.alertId = alertId;
    }
    /**
     * @return the shipmentId
     */
    public Long getShipmentId() {
        return shipmentId;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setShipmentId(final Long shipmentId) {
        this.shipmentId = shipmentId;
    }
    /**
     * @return the title.
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }
    /**
     * @return the lines
     */
    public List<String> getLines() {
        return lines;
    }
}
