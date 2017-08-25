/**
 *
 */
package com.visfresh.io.shipment;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentData {
    private SingleShipmentBean bean;
    private List<SingleShipmentBean> siblings = new LinkedList<>();
    private final List<SingleShipmentLocationBean> locations = new LinkedList<>();

    /**
     * Default constructor.
     */
    public SingleShipmentData() {
        super();
    }

    /**
     * @return the bean
     */
    public SingleShipmentBean getBean() {
        return bean;
    }
    /**
     * @param bean the bean to set
     */
    public void setBean(final SingleShipmentBean bean) {
        this.bean = bean;
    }
    /**
     * @return the siblings
     */
    public List<SingleShipmentBean> getSiblings() {
        return siblings;
    }
    /**
     * @return the locations
     */
    public List<SingleShipmentLocationBean> getLocations() {
        return locations;
    }
}
