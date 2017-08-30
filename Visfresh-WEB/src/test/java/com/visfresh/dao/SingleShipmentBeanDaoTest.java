/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.io.shipment.SingleShipmentBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentBeanDaoTest extends BaseDaoTest<SingleShipmentBeanDao> {
    private Device d1;
    private Device d2;
    private Device d3;
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public SingleShipmentBeanDaoTest() {
        super(SingleShipmentBeanDao.class);
    }

    @Before
    public void setUp() {
        shipmentDao = context.getBean(ShipmentDao.class);

        //create devices
        d1 = createDevice("211827019870983");
        d2 = createDevice("203870989893844");
        d3 = createDevice("292309480980344");
    }
    @Test
    public void testShipmentSiblingsNotBeans() {
        final Shipment s1 = createShipment(d1);
        final Shipment s2 = createShipment(d2);
        final Shipment s3 = createShipment(d3);
        createShipment(d3);//s4

        setAsSiblings(s1, s2, s3);

        assertEquals(0, dao.getShipmentBeanIncludeSiblings(s1.getId()).size());
        assertEquals(0, dao.getShipmentBeanIncludeSiblings(
                Device.getSerialNumber(s1.getDevice().getImei()), s1.getTripCount()).size());
    }

    @Test
    public void testShipmentNotSiblingsBeans() {
        final Shipment s1 = createShipment(d1);
        final Shipment s2 = createShipment(d2);
        final Shipment s3 = createShipment(d3);
        createShipment(d3);//s4

        createBean(s1);
        createBean(s2);
        createBean(s3);

        assertEquals(1, dao.getShipmentBeanIncludeSiblings(s1.getId()).size());
        assertEquals(1, dao.getShipmentBeanIncludeSiblings(
                Device.getSerialNumber(s1.getDevice().getImei()), s1.getTripCount()).size());
    }
    @Test
    public void testShipmentSiblingsBeans() {
        final Shipment s1 = createShipment(d1);
        final Shipment s2 = createShipment(d2);
        final Shipment s3 = createShipment(d3);
        createShipment(d3);//s4

        createBean(s1);
        createBean(s2);
        createBean(s3);

        setAsSiblings(s1, s2, s3);

        assertEquals(3, dao.getShipmentBeanIncludeSiblings(s1.getId()).size());
        assertEquals(3, dao.getShipmentBeanIncludeSiblings(
                Device.getSerialNumber(s1.getDevice().getImei()), s1.getTripCount()).size());
    }

    @Test
    public void testMainSiblingNotBean() {
        final Shipment s1 = createShipment(d1);
        final Shipment s2 = createShipment(d2);
        final Shipment s3 = createShipment(d3);
        createShipment(d3);//s4

        createBean(s2);
        createBean(s3);

        setAsSiblings(s1, s2, s3);

        assertEquals(2, dao.getShipmentBeanIncludeSiblings(s1.getId()).size());
        assertEquals(2, dao.getShipmentBeanIncludeSiblings(
                Device.getSerialNumber(s1.getDevice().getImei()), s1.getTripCount()).size());
    }
    /**
     * @param device device.
     * @return shipment.
     */
    private Shipment createShipment(final Device device) {
        final Shipment s = new Shipment();
        s.setDevice(device);
        s.setCompany(sharedCompany);
        s.setStatus(ShipmentStatus.InProgress);
        s.setShipmentDescription("JUnit shipment");
        return shipmentDao.save(s);
    }
    /**
     * @param s shipment.
     * @return single shipment bean.
     */
    private SingleShipmentBean createBean(final Shipment s) {
        final SingleShipmentBean bean = new SingleShipmentBean();
        bean.setShipmentId(s.getId());
        bean.setDevice(s.getDevice().getImei());
        bean.setStatus(s.getStatus());
        bean.setCompanyId(s.getCompany().getId());
        bean.setShipmentDescription(s.getShipmentDescription());
        dao.saveShipmentBean(bean);
        return bean;
    }
    /**
     * @param device device IMEI.
     * @return
     */
    private Device createDevice(final String device) {
        final Device d = new Device();
        d.setImei(device);
        d.setName("JUnit-" + device);
        d.setCompany(sharedCompany);
        d.setDescription("JUnit device");
        return context.getBean(DeviceDao.class).save(d);
    }
    /**
     * @param siblings
     */
    private void setAsSiblings(final Shipment... siblings) {
        //create ID set
        final Set<Long> allIds = new HashSet<>();
        for (final Shipment shipment : siblings) {
            allIds.add(shipment.getId());
        }

        //set new sibling list to siblings
        for (final Shipment shipment : siblings) {
            final Set<Long> ids = new HashSet<>(allIds);
            ids.remove(shipment.getId());

            shipment.getSiblings().clear();
            shipment.getSiblings().addAll(ids);
            shipment.setSiblingCount(shipment.getSiblings().size());
            shipmentDao.save(shipment);
        }
    }
}
