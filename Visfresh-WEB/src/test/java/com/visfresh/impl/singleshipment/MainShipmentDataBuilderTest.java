/**
 *
 */
package com.visfresh.impl.singleshipment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.impl.services.NotificationServiceImpl;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MainShipmentDataBuilderTest extends BaseBuilderTest {
    /**
     * Default constructor.
     */
    public MainShipmentDataBuilderTest() {
        super();
    }

    @Test
    public void testSiblings() {
        final Shipment s1 = shipmentDao.save(createDefaultNotSavedShipment(device));
        final Shipment s2 = shipmentDao.save(createDefaultNotSavedShipment(device));
        final Shipment s3 = shipmentDao.save(createDefaultNotSavedShipment(device));

        setAsSiblings(s1, s2, s3);

        final SingleShipmentBuildContext c = new SingleShipmentBuildContext();

        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s1.getId());
        b.fetchData();
        b.build(c);

        //test result
        assertEquals(s1.getId().longValue(), c.getData().getBean().getShipmentId());
        assertEquals(2, c.getData().getSiblings().size());
        assertEquals(2, c.getData().getBean().getSiblings().size());
        assertEquals(2, c.getData().getSiblings().get(0).getSiblings().size());
        assertEquals(2, c.getData().getSiblings().get(1).getSiblings().size());
    }
    @Test
    public void testBuild() {
        //shipment session fields
        final boolean alertsSuppressed = true;
        final Date alertsSuppressedTime = new Date(System.currentTimeMillis() - 1000000000l);
        final boolean arrivalReportSent = true;

        //shipment fields
        final Integer alertSuppressionMinutes = 15;
        final Integer arrivalNotificationWithinKm = 35;
        final Date arrivalTime = new Date(System.currentTimeMillis() - 1098098l);
        final String assetNum = "AssertNumber";
        final String assetType = "AssertType";
        final String commentsForReceiver = "Comments for receiver";
        final boolean excludeNotificationsIfNoAlerts = true;
        final ShipmentStatus status = ShipmentStatus.InProgress;
        final Integer noAlertsAfterArrivalMinutes = 450;
        final Integer noAlertsAfterStartMinutes = 320;
        final String palletId = "PaletId";
        final boolean sendArrivalReport = true;
        final boolean sendArrivalReportOnlyIfAlerts = true;
        final String shipmentDescription = "Shipment description";
        final Integer shutDownAfterStartMinutes = 23;
        final Integer shutdownDeviceAfterMinutes = 2345;
        final Date startTime = new Date(System.currentTimeMillis() - 29380470934l);
        final Date eta = new Date(System.currentTimeMillis() - 1000000l);

        //set fields to shipment
        final Shipment s = createDefaultNotSavedShipment(device);
        s.setAlertSuppressionMinutes(alertSuppressionMinutes);
        s.setArrivalNotificationWithinKm(arrivalNotificationWithinKm);
        s.setArrivalDate(arrivalTime);
        s.setAssetNum(assetNum);
        s.setAssetType(assetType);
        s.setCommentsForReceiver(commentsForReceiver);
        s.setExcludeNotificationsIfNoAlerts(excludeNotificationsIfNoAlerts);
        s.setStatus(status);
        s.setNoAlertsAfterArrivalMinutes(noAlertsAfterArrivalMinutes);
        s.setNoAlertsAfterStartMinutes(noAlertsAfterStartMinutes);
        s.setPalletId(palletId);
        s.setSendArrivalReport(sendArrivalReport);
        s.setSendArrivalReportOnlyIfAlerts(sendArrivalReportOnlyIfAlerts);
        s.setShipmentDescription(shipmentDescription);
        s.setShutDownAfterStartMinutes(shutDownAfterStartMinutes);
        s.setShutdownDeviceAfterMinutes(shutdownDeviceAfterMinutes);
        s.setShipmentDate(startTime);
        s.setEta(eta);
        shipmentDao.save(s);

        //set up shipment session
        final ShipmentSession ss = new ShipmentSession(s.getId());
        ss.setAlertsSuppressed(alertsSuppressed);
        ss.setAlertsSuppressionDate(alertsSuppressedTime);
        ss.setArrivalProcessed(arrivalReportSent);
        NotificationServiceImpl.setArrivalReportSent(ss, new Date());
        context.getBean(ShipmentSessionDao.class).saveSession(ss);

        final SingleShipmentBuildContext c = new SingleShipmentBuildContext();

        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId());
        b.fetchData();
        b.build(c);
        final SingleShipmentBean bean = c.getData().getBean();

        assertEquals(alertsSuppressed, bean.isAlertsSuppressed());
        assertTrue(Math.abs(alertsSuppressedTime.getTime() - bean.getAlertsSuppressionTime().getTime()) < 1000l);
        assertEquals(arrivalReportSent, bean.isArrivalReportSent());
        assertEquals(alertSuppressionMinutes, bean.getAlertSuppressionMinutes());
        assertEquals(arrivalNotificationWithinKm, bean.getArrivalNotificationWithinKm());
        assertTrue(Math.abs(arrivalTime.getTime() - bean.getArrivalTime().getTime()) < 1000l);
        assertEquals(assetNum, bean.getAssetNum());
        assertEquals(assetType, bean.getAssetType());
        assertEquals(commentsForReceiver, bean.getCommentsForReceiver());
        assertEquals(device.getCompany().getId(), bean.getCompanyId());
        assertEquals(device.getImei(), bean.getDevice());
        assertEquals(device.getColor().name(), bean.getDeviceColor());
        assertEquals(device.getName(), bean.getDeviceName());
        assertEquals(excludeNotificationsIfNoAlerts, bean.isExcludeNotificationsIfNoAlerts());
        assertEquals(status, bean.getStatus());
        assertEquals(s.getTripCount(), bean.getTripCount());
        assertTrue(bean.isLatestShipment());
        assertEquals(noAlertsAfterArrivalMinutes, bean.getNoAlertsAfterArrivalMinutes());
        assertEquals(noAlertsAfterStartMinutes, bean.getNoAlertsAfterStartMinutes());
        assertEquals(palletId, bean.getPalletId());
        assertEquals(sendArrivalReport, bean.isSendArrivalReport());
        assertEquals(sendArrivalReportOnlyIfAlerts, bean.isSendArrivalReportOnlyIfAlerts());
        assertEquals(shipmentDescription, bean.getShipmentDescription());
        assertEquals(s.getId().longValue(), bean.getShipmentId());
        assertEquals("Manual", bean.getShipmentType());
        assertEquals(shutDownAfterStartMinutes, bean.getShutDownAfterStartMinutes());
        assertEquals(shutdownDeviceAfterMinutes, bean.getShutdownDeviceAfterMinutes());
        assertTrue(Math.abs(startTime.getTime() - bean.getStartTime().getTime()) < 1000l);
        assertTrue(Math.abs(eta.getTime() - bean.getEta().getTime()) < 1000l);
        assertEquals(100, bean.getPercentageComplete());
    }
}
