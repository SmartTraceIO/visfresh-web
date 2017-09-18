/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.dao.BaseDbTest;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Color;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class BaseBuilderTest extends BaseDbTest {
    protected NamedParameterJdbcTemplate jdbc;
    protected Device device;
    protected ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public BaseBuilderTest() {
        super();
    }

    @Before
    public void setUp() {
        jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        shipmentDao = context.getBean(ShipmentDao.class);
        device = createDevice("234897029345798");
    }

    /**
     * @param device device.
     * @return shipment.
     */
    protected Shipment createDefaultNotSavedShipment(final Device device) {
        final Shipment s = new Shipment();
        s.setDevice(device);
        s.setCompany(sharedCompany);
        s.setStatus(ShipmentStatus.InProgress);
        s.setShipmentDescription("JUnit shipment");
        return s;
    }
    /**
     * @param device device IMEI.
     * @return
     */
    protected Device createDevice(final String device) {
        final Device d = new Device();
        d.setImei(device);
        d.setName("JUnit-" + device);
        d.setCompany(sharedCompany);
        d.setDescription("JUnit device");
        d.setColor(Color.Brown);
        return context.getBean(DeviceDao.class).save(d);
    }
    /**
     * @param siblings
     */
    protected void setAsSiblings(final Shipment... siblings) {
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
