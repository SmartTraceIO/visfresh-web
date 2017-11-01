/**
 *
 */
package com.visfresh.impl.singleshipment;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.PreliminarySingleShipmentData;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.TemperatureAlertBean;
import com.visfresh.services.LocationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReadingsDataBuilderTest extends BaseBuilderTest {
    /**
     * Default constructor.
     */
    public ReadingsDataBuilderTest() {
        super();
    }
    @Test
    public void testReadings() {
        final Shipment s = createShipment(device);
        final Shipment sib1 = createShipment(device);
        final Shipment sib2 = createShipment(device);

        final Date d1 = new Date(System.currentTimeMillis() - 3 * 1000000l);
        final Date d2 = new Date(System.currentTimeMillis() - 2 * 1000000l);
        final Date d3 = new Date(System.currentTimeMillis() - 1 * 1000000l);

        final int b1 = 300;
        final int b2 = 400;
        final int b3 = 500;

        createTrackerEvent(s, 10., d1, b1);
        createTrackerEvent(s, 11., d2, b2);
        createTrackerEvent(s, 12., d3, b3);

        createTrackerEvent(sib1, 12., d1, b1);
        createTrackerEvent(sib1, 13., d2, b2);

        createTrackerEvent(sib2, 14., d1, b1);

        setAsSiblings(s, sib1, sib2);

        final PreliminarySingleShipmentData pd = getPreliminaryData(s);
        final SingleShipmentBuildContext ctxt = createContext(pd);
        final ReadingsDataBuilder b = createBuilder(pd);
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(3, data.getLocations().size());

        //check main bean
        final SingleShipmentBean mainBean = data.getBean();
        assertEquals(10.0, mainBean.getMinTemp(), 0.0001);
        assertEquals(12.0, mainBean.getMaxTemp(), 0.0001);
        assertEquals(12.0, mainBean.getLastReadingTemperature(), 0.0001);
        assertEquals(b3, mainBean.getBatteryLevel().intValue());
        assertEqualsDates(d3, mainBean.getLastReadingTime());

        //first sibling
        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);
        assertEquals(12.0, sibBean1.getMinTemp(), 0.0001);
        assertEquals(13.0, sibBean1.getMaxTemp(), 0.0001);
        assertEquals(13.0, sibBean1.getLastReadingTemperature(), 0.0001);
        assertEquals(b2, sibBean1.getBatteryLevel().intValue());
        assertEqualsDates(d2, sibBean1.getLastReadingTime());

        //second sibling
        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertEquals(14.0, sibBean2.getMinTemp(), 0.0001);
        assertEquals(14.0, sibBean2.getMaxTemp(), 0.0001);
        assertEquals(14.0, sibBean2.getLastReadingTemperature(), 0.0001);
        assertEquals(b1, sibBean2.getBatteryLevel().intValue());
        assertEqualsDates(d1, sibBean2.getLastReadingTime());
    }
    @Test
    public void testReadingsAlertWithTrackerEventId() {
        final Shipment s = createShipment(device);

        final Date d1 = new Date(System.currentTimeMillis() - 3 * 1000000l);
        final Date d2 = new Date(System.currentTimeMillis() - 2 * 1000000l);
        final Date d3 = new Date(System.currentTimeMillis() - 1 * 1000000l);

        final TrackerEvent e1 = createTrackerEvent(s, 10., d1, 400);
        createTrackerEvent(s, 10., d2, 400);
        final TrackerEvent e2 = createTrackerEvent(s, 10., d3, 400);

        createAlert(e1);
        createAlert(e1);
        createAlert(e2);

        final PreliminarySingleShipmentData pd = getPreliminaryData(s);
        final ReadingsDataBuilder b = createBuilder(pd);

        //check with tracker event ID set
        SingleShipmentBuildContext ctxt = createContext(pd);
        b.fetchData();
        b.build(ctxt);

        SingleShipmentData data = ctxt.getData();
        assertEquals(3, data.getLocations().size());
        assertEquals(2, data.getLocations().get(0).getAlerts().size());
        assertEquals(1, data.getLocations().get(2).getAlerts().size());

        //clear tracker event ID
        final AlertDao alertDao = context.getBean(AlertDao.class);
        for (final Alert a : alertDao.findAll(null, null, null)) {
            a.setTrackerEventId(null);
            alertDao.save(a);
        }

        //check with tracker event ID set
        ctxt = createContext(pd);
        b.fetchData();
        b.build(ctxt);

        data = ctxt.getData();
        assertEquals(2, data.getLocations().get(0).getAlerts().size());
        assertEquals(0, data.getLocations().get(1).getAlerts().size());
        assertEquals(1, data.getLocations().get(2).getAlerts().size());
    }

    /**
     * @param s
     * @param t
     * @param time
     * @param battery
     * @return
     */
    protected TrackerEvent createTrackerEvent(final Shipment s, final double t, final Date time, final int battery) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(battery);
        e.setCreatedOn(new Date());
        e.setDevice(s.getDevice());
        e.setLatitude(30.);
        e.setLongitude(20.);
        e.setShipment(s);
        e.setTemperature(t);
        e.setTime(time);
        e.setType(TrackerEventType.AUT);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    /**
     * @param pd
     * @return
     */
    private SingleShipmentBuildContext createContext(final PreliminarySingleShipmentData pd) {
        final SingleShipmentBuildContext c = new SingleShipmentBuildContext(new LocationService() {
            @Override
            public String getLocationDescription(final Location loc) {
                return "Undetermined";
            }
        });

        //create result data
        final SingleShipmentData data = new SingleShipmentData();
        c.setData(data);
        final List<SingleShipmentBean> allBeans = new LinkedList<>();

        final SingleShipmentBean mainBean = new SingleShipmentBean();
        mainBean.setShipmentId(pd.getShipment());
        mainBean.setCompanyId(pd.getCompany());
        allBeans.add(mainBean);

        data.setBean(mainBean);

        //add sibling beans to data.
        for (final Long sibling : pd.getSiblings()) {
            final SingleShipmentBean bean = new SingleShipmentBean();
            bean.setShipmentId(sibling);
            bean.setCompanyId(pd.getCompany());

            data.getSiblings().add(bean);
        }

        //add siblings to each bean and alerts.

        allBeans.addAll(data.getSiblings());
        for (final SingleShipmentBean b : allBeans) {
            //add alerts
            final Map<Long, List<Alert>> map = context.getBean(AlertDao.class).getAlertsForShipmentIds(
                    Collections.singleton(b.getShipmentId()));
            final List<Alert> alerts = map.get(b.getShipmentId());
            if (alerts != null) {
                for (final Alert a : alerts) {
                    b.getSentAlerts().add((a instanceof TemperatureAlert)
                        ? new TemperatureAlertBean((TemperatureAlert) a)
                        : new AlertBean(a));
                }
            }

            //add siblings.
            for (final SingleShipmentBean b1 : allBeans) {
                if (b1.getShipmentId() != b.getShipmentId()) {
                    b.getSiblings().add(b1.getShipmentId());
                }
            }
        }


        return c;
    }
    /**
     * @param pd
     * @return
     */
    private ReadingsDataBuilder createBuilder(final PreliminarySingleShipmentData pd) {
        return new ReadingsDataBuilder(context.getBean(TrackerEventDao.class),
                pd.getShipment(), pd.getSiblings());
    }
    /**
     * @param s
     * @return
     */
    private PreliminarySingleShipmentData getPreliminaryData(final Shipment s) {
        final PreliminarySingleShipmentData pd = shipmentDao.getPreliminarySingleShipmentData(
                s.getId(), null, null);
        return pd;
    }
}
