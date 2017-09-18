/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.Shipment;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class SingleShipmentTestUtils {
    /**
     * Default constructor.
     */
    private SingleShipmentTestUtils() {
        super();
    }

    /**
     * @param beans sibling beans.
     */
    private static void addSiblingIds(final List<SingleShipmentBean> beans) {
        for (final SingleShipmentBean b1 : beans) {
            for (final SingleShipmentBean b2 : beans) {
                if (b1.getShipmentId() != b2.getShipmentId()) {
                    b1.getSiblings().add(b2.getShipmentId());
                }
            }
        }
    }
    /**
     * @param s main shipment.
     * @param siblings siblings.
     * @return
     */
    public static SingleShipmentBuildContext createContextWithMainData(final Shipment s, final Shipment... siblings) {
        final SingleShipmentBuildContext c = new SingleShipmentBuildContext();
        final SingleShipmentData data = new SingleShipmentData();
        c.setData(data);

        final List<SingleShipmentBean> beans = new LinkedList<>();
        final SingleShipmentBean main = createBean(s);
        data.setBean(main);
        beans.add(main);

        //add siblings to data
        for (final Shipment sp : siblings) {
            final SingleShipmentBean bean = createBean(sp);
            beans.add(bean);
            data.getSiblings().add(bean);
        }

        //set cross sibling IDs
        addSiblingIds(beans);

        return c;
    }

    /**
     * @param s
     * @return
     */
    private static SingleShipmentBean createBean(final Shipment s) {
        final SingleShipmentBean bean = new SingleShipmentBean();
        bean.setShipmentId(s.getId());
        bean.setStatus(s.getStatus());
        bean.setArrivalNotificationWithinKm(s.getArrivalNotificationWithinKm());
        bean.setArrivalTime(s.getArrivalDate());
        bean.setAssetNum(s.getAssetNum());
        bean.setAssetType(s.getAssetType());
        bean.setCommentsForReceiver(s.getCommentsForReceiver());
        bean.setCompanyId(s.getCompany().getId());
        bean.setDevice(s.getDevice().getImei());
        if (s.getDevice().getColor() != null) {
            bean.setDeviceColor(s.getDevice().getColor().name());
        }
        bean.setDeviceName(s.getDevice().getName());
        bean.setEta(s.getEta());
        bean.setExcludeNotificationsIfNoAlerts(s.isExcludeNotificationsIfNoAlerts());
        bean.setNoAlertsAfterArrivalMinutes(s.getNoAlertsAfterArrivalMinutes());
        bean.setNoAlertsAfterStartMinutes(s.getNoAlertsAfterStartMinutes());
        bean.setPalletId(s.getPalletId());
        bean.setSendArrivalReport(s.isSendArrivalReport());
        bean.setSendArrivalReportOnlyIfAlerts(s.isSendArrivalReportOnlyIfAlerts());
        bean.setShipmentDescription(s.getShipmentDescription());
        bean.setShipmentType(s.isAutostart() ? "Autostart": "Manual");
        bean.setShutDownAfterStartMinutes(s.getShutDownAfterStartMinutes());
        bean.setShutdownDeviceAfterMinutes(s.getShutdownDeviceAfterMinutes());
        bean.setShutdownTime(s.getDeviceShutdownTime());
        bean.setStartTime(s.getShipmentDate());
        bean.setTripCount(s.getTripCount());
        return bean;
    }
    /**
     * @param id
     * @param data
     * @return
     */
    public static SingleShipmentBean getSibling(final long id, final SingleShipmentData data) {
        for (final SingleShipmentBean bean : data.getSiblings()) {
            if (id == bean.getShipmentId()) {
                return bean;
            }
        }
        return null;
    }
}
