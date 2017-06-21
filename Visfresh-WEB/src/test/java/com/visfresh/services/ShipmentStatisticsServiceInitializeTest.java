/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.BaseDaoTest;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentStatisticsDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.impl.services.ShipmentStatisticsServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsServiceInitializeTest extends BaseDaoTest<ShipmentStatisticsDao> {
    private ShipmentStatisticsService service;
    private Device device;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsServiceInitializeTest() {
        super(ShipmentStatisticsDao.class);
    }

    @Before
    public void setUp() {
        service = context.getBean(ShipmentStatisticsServiceImpl.class);

        //create shared device
        final Device d = new Device();
        d.setActive(true);
        d.setCompany(sharedCompany);
        d.setImei("3487098374353452");
        d.setName("JUnit");

        this.device = context.getBean(DeviceDao.class).save(d);
    }

    @Test
    public void testCreateStatisticsIfNotYetCreated() {
        //create shipments
        final Shipment s1 = createShipment();
        final Shipment s2 = createShipment();
        final Shipment s3 = createShipment();

        //create statistics if not yet created
        final int result = createStatisticsIfNotYetCreated();
        assertEquals(3, result);

        //check statistics exists
        assertNotNull(dao.getStatistics(s1));
        assertNotNull(dao.getStatistics(s2));
        assertNotNull(dao.getStatistics(s3));
    }
    @Test
    public void testNotCreateIfCreated() {
        //create shipments
        final Shipment s = createShipment();

        //create statistics
        dao.saveStatistics(service.calculate(s));

        //run and check result
        final int result = createStatisticsIfNotYetCreated();
        assertEquals(0, result);
    }
    /**
     * @return the shipment.
     */
    private Shipment createShipment() {
        final Shipment s = new Shipment();
        s.setStatus(ShipmentStatus.Default);
        s.setCompany(sharedCompany);
        s.setDevice(device);
        return context.getBean(ShipmentDao.class).save(s);
    }
    /**
     * @return
     */
    private int createStatisticsIfNotYetCreated() {
        try {
            final Method m = ShipmentStatisticsServiceImpl.class.getDeclaredMethod(
                    "createStatisticsIfNotYetCreated");
            m.setAccessible(true);
            final Integer result = (Integer) m.invoke(service);
            return result.intValue();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
