/**
 *
 */
package com.visfresh.io;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GetFilteredShipmentsRequest {
    private boolean alertsOnly;
    private Boolean lastDay;
    private Boolean last2Days;
    private Boolean lastWeek;
    private Boolean LastMonth;
    private Date shipmentDateFrom;
    private Date shipmentDateTo;
    private List<Long> shippedFrom;
    private List<Long> shippedTo;
    private String shipmentDescription;
    private String deviceImei;
    private ShipmentStatus status;
    private Integer pageIndex;
    private Integer pageSize;
    private String sortOrder;
    private String sortColumn;

    /**
     *
     */
    public GetFilteredShipmentsRequest() {
        super();
    }

    /**
     * @return the alertsOnly
     */
    public boolean isAlertsOnly() {
        return alertsOnly;
    }
    /**
     * @param alertsOnly the alertsOnly to set
     */
    public void setAlertsOnly(final boolean alertsOnly) {
        this.alertsOnly = alertsOnly;
    }
    /**
     * @return the lastDay
     */
    public Boolean getLastDay() {
        return lastDay;
    }
    /**
     * @param lastDay the lastDay to set
     */
    public void setLastDay(final Boolean lastDay) {
        this.lastDay = lastDay;
    }
    /**
     * @return the last2Days
     */
    public Boolean getLast2Days() {
        return last2Days;
    }
    /**
     * @param last2Days the last2Days to set
     */
    public void setLast2Days(final Boolean last2Days) {
        this.last2Days = last2Days;
    }
    /**
     * @return the lastWeek
     */
    public Boolean getLastWeek() {
        return lastWeek;
    }
    /**
     * @param lastWeek the lastWeek to set
     */
    public void setLastWeek(final Boolean lastWeek) {
        this.lastWeek = lastWeek;
    }
    /**
     * @return the lastMonth
     */
    public Boolean getLastMonth() {
        return LastMonth;
    }
    /**
     * @param lastMonth the lastMonth to set
     */
    public void setLastMonth(final Boolean lastMonth) {
        LastMonth = lastMonth;
    }
    /**
     * @return the shipmentDateFrom
     */
    public Date getShipmentDateFrom() {
        return shipmentDateFrom;
    }
    /**
     * @param shipmentDateFrom the shipmentDateFrom to set
     */
    public void setShipmentDateFrom(final Date shipmentDateFrom) {
        this.shipmentDateFrom = shipmentDateFrom;
    }
    /**
     * @return the shipmentDateTo
     */
    public Date getShipmentDateTo() {
        return shipmentDateTo;
    }
    /**
     * @param shipmentDateTo the shipmentDateTo to set
     */
    public void setShipmentDateTo(final Date shipmentDateTo) {
        this.shipmentDateTo = shipmentDateTo;
    }
    /**
     * @return the shippedFrom
     */
    public List<Long> getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final List<Long> shippedFrom) {
        this.shippedFrom = shippedFrom;
    }
    /**
     * @return the shippedTo
     */
    public List<Long> getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final List<Long> shippedTo) {
        this.shippedTo = shippedTo;
    }
    /**
     * @return the shipmentDescription
     */
    public String getShipmentDescription() {
        return shipmentDescription;
    }
    /**
     * @param shipmentDescription the shipmentDescription to set
     */
    public void setShipmentDescription(final String shipmentDescription) {
        this.shipmentDescription = shipmentDescription;
    }
    /**
     * @return the deviceImei
     */
    public String getDeviceImei() {
        return deviceImei;
    }
    /**
     * @param deviceImei the deviceImei to set
     */
    public void setDeviceImei(final String deviceImei) {
        this.deviceImei = deviceImei;
    }
    /**
     * @return the status
     */
    public ShipmentStatus getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(final ShipmentStatus status) {
        this.status = status;
    }
    /**
     * @return the pageIndex
     */
    public Integer getPageIndex() {
        return pageIndex;
    }
    /**
     * @param pageIndex the pageIndex to set
     */
    public void setPageIndex(final Integer pageIndex) {
        this.pageIndex = pageIndex;
    }
    /**
     * @return the pageSize
     */
    public Integer getPageSize() {
        return pageSize;
    }
    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(final Integer pageSize) {
        this.pageSize = pageSize;
    }
    /**
     * @return the sortOrder
     */
    public String getSortOrder() {
        return sortOrder;
    }
    /**
     * @param sortOrder the sortOrder to set
     */
    public void setSortOrder(final String sortOrder) {
        this.sortOrder = sortOrder;
    }
    /**
     * @return the sortColumn
     */
    public String getSortColumn() {
        return sortColumn;
    }
    /**
     * @param sortColumn the sortColumn to set
     */
    public void setSortColumn(final String sortColumn) {
        this.sortColumn = sortColumn;
    }
}
