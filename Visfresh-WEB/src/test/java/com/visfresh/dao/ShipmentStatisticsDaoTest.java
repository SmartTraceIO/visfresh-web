/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.impl.ShipmentTemperatureStatsCollector;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.rules.state.ShipmentStatistics;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsDaoTest extends BaseDaoTest<ShipmentStatisticsDao> {
    private Shipment shipment;
    /**
     * Default constructor.
     */
    public ShipmentStatisticsDaoTest() {
        super(ShipmentStatisticsDao.class);
    }
    @Before
    public void setUp() {
        Device d = new Device();
        d.setImei("9238470983274987");
        d.setName("Test Device");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        d.setTripCount(5);
        d = context.getBean(DeviceDao.class).save(d);

        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(ShipmentStatus.Arrived);
        this.shipment = getContext().getBean(ShipmentDao.class).save(s);
    }
    @Test
    public void testOneStatistics() {
        //check not yet saved
        assertNull(dao.getStatistics(shipment));

        //test save empty
        ShipmentStatistics s = new ShipmentStatistics(shipment.getId());
        dao.saveStatistics(s);
        s = dao.getStatistics(shipment);
        dao.clearCache();

        assertNotNull(s);

        //test update
        final Double avgTemperature = 5.5;
        final ShipmentTemperatureStatsCollector collector = new ShipmentTemperatureStatsCollector();
        final Double maximumTemperature = 14.78;
        final Double minimumTemperature = -2.45;
        final Double standardDevitation = 0.02;
        final long timeAboveUpperLimit = 92348709238l;
        final long timeBelowLowerLimit = 23947987324l;
        final long totalTime = 2390870293879l;

        s.setAvgTemperature(avgTemperature);
        s.setCollector(collector);
        s.setMaximumTemperature(maximumTemperature);
        s.setMinimumTemperature(minimumTemperature);
        s.setStandardDevitation(standardDevitation);
        s.setTimeAboveUpperLimit(timeAboveUpperLimit);
        s.setTimeBelowLowerLimit(timeBelowLowerLimit);
        s.setTotalTime(totalTime);

        dao.saveStatistics(s);
        dao.clearCache();
        s = dao.getStatistics(shipment);

        assertEquals(avgTemperature, s.getAvgTemperature());
        assertNotNull(s.getCollector());
        assertEquals(maximumTemperature, s.getMaximumTemperature());
        assertEquals(minimumTemperature, s.getMinimumTemperature());
        assertEquals(standardDevitation, s.getStandardDevitation());
        assertEquals(timeAboveUpperLimit, s.getTimeAboveUpperLimit());
        assertEquals(timeBelowLowerLimit, s.getTimeBelowLowerLimit());
        assertEquals(totalTime, s.getTotalTime());
    }
}
