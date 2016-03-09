/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.DeviceConstants;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDaoTest extends BaseCrudTest<DeviceDao, Device, String> {
    private int num;
    private AutoStartShipment autoStart;
    /**
     * Default constructor.
     */
    public DeviceDaoTest() {
        super(DeviceDao.class);
    }

    @Before
    public void setUp() {
        final ShipmentTemplate tpl = createShipmentTemplate("Tpl1");
        this.autoStart = createAutoStartTemplate(tpl);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected Device createTestEntity() {
        return createDevice("3984709300000" + (++num));
    }

    /**
     * @param imei device IMEI..
     * @return
     */
    private Device createDevice(final String imei) {
        return createDevice(imei, autoStart);
    }
    /**
     * @param imei
     * @param au
     * @return
     */
    private Device createDevice(final String imei, final AutoStartShipment au) {
        final Device d = new Device();
        d.setImei(imei);
        d.setName("Test Device");
        d.setCompany(sharedCompany);
        d.setDescription("Test device");
        d.setTripCount(5);
        if (au != null) {
            d.setAutostartTemplateId(au.getId());
        }
        return d;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCorrectSaved(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final Device d) {
        assertNotNull(d.getImei());
        assertEquals("Test Device", d.getName());
        assertEquals("Test device", d.getDescription());
        assertEquals(5, d.getTripCount());
        assertEquals(autoStart.getId(), d.getAutostartTemplateId());

        //test company
        final Company c = d.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<Device> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        //check first entity
        final Device d = all.get(0);

        assertNotNull(d.getImei());
        assertEquals("Test Device", d.getName());
        assertEquals("Test device", d.getDescription());
        assertEquals(5, d.getTripCount());

        //test company
        final Company c = d.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
        assertEquals(autoStart.getId(), d.getAutostartTemplateId());
    }
    @Test
    public void testFindByCompany() {
        createAndSaveDevice(sharedCompany, "293487032784");
        createAndSaveDevice(sharedCompany, "834270983474");

        assertEquals(2, dao.findByCompany(sharedCompany, null, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left, null, null, null).size());
    }
    @Test
    public void testEnabledField() {
        final Device d = createAndSaveDevice(sharedCompany, "293487032784");
        final boolean active = !d.isActive();
        d.setActive(active);
        dao.save(d);
        assertEquals(active, d.isActive());
    }
    @Test
    public void testGetDevicesSortingByDeviceFiels() {
        final String imei1 = "133333333333";
        final String imei2 = "322222222222";
        final boolean active1 = false;
        final boolean active2 = true;
        final String name1 = "Name1";
        final String name2 = "Name2";
        final String desc1 = "Desc1";
        final String desc2 = "Desc1";
        final int tripCount1 = 1;
        final int tripCount2 = 2;

        final ShipmentTemplate tpl1 = createShipmentTemplate(name1);
        final ShipmentTemplate tpl2 = createShipmentTemplate(name2);

        final AutoStartShipment aut2 = createAutoStartTemplate(tpl2);
        final AutoStartShipment aut1 = createAutoStartTemplate(tpl1);

        final Device d1 = createDevice(imei1, aut1);
        d1.setActive(active2);
        d1.setDescription(desc1);
        d1.setName(name2);
        d1.setTripCount(tripCount1);

        final Device d2 = createDevice(imei2, aut2);
        d2.setActive(active1);
        d2.setDescription(desc2);
        d2.setName(name1);
        d2.setTripCount(tripCount2);

        dao.save(d1);
        dao.save(d2);

        List<ListDeviceItem> devices;
        //test sort by imei
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_IMEI), null);
        assertEquals(imei1, devices.get(0).getImei());
        assertEquals(imei2, devices.get(1).getImei());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_IMEI), null);
        assertEquals(imei2, devices.get(0).getImei());
        assertEquals(imei1, devices.get(1).getImei());

        //test sort by name
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_NAME), null);
        assertEquals(name1, devices.get(0).getName());
        assertEquals(name2, devices.get(1).getName());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_NAME), null);
        assertEquals(name2, devices.get(0).getName());
        assertEquals(name1, devices.get(1).getName());

        //sorting by description
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_DESCRIPTION), null);
        assertEquals(desc1, devices.get(0).getDescription());
        assertEquals(desc2, devices.get(1).getDescription());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_DESCRIPTION), null);
        assertEquals(desc2, devices.get(0).getDescription());
        assertEquals(desc1, devices.get(1).getDescription());

        //test by active status
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_ACTIVE), null);
        assertEquals(active1, devices.get(0).isActive());
        assertEquals(active2, devices.get(1).isActive());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_ACTIVE), null);
        assertEquals(active2, devices.get(0).isActive());
        assertEquals(active1, devices.get(1).isActive());

        //test by serial number property
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_SN), null);
        assertEquals(imei2, devices.get(0).getImei());
        assertEquals(imei1, devices.get(1).getImei());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_SN), null);
        assertEquals(imei1, devices.get(0).getImei());
        assertEquals(imei2, devices.get(1).getImei());

        //test by shipment number
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_SHIPMENT_NUMBER), null);
        assertEquals(imei2, devices.get(0).getImei());
        assertEquals(imei1, devices.get(1).getImei());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_SHIPMENT_NUMBER), null);
        assertEquals(imei1, devices.get(0).getImei());
        assertEquals(imei2, devices.get(1).getImei());

        //sort by autostart template ID
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID), null);
        assertEquals(d2.getImei(), devices.get(0).getImei());
        assertEquals(d1.getId(), devices.get(1).getImei());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID), null);
        assertEquals(d1.getImei(), devices.get(0).getImei());
        assertEquals(d2.getId(), devices.get(1).getImei());

        //sort by autostart template name
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_NAME), null);
        assertEquals(d1.getImei(), devices.get(0).getImei());
        assertEquals(d2.getId(), devices.get(1).getImei());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_NAME), null);
        assertEquals(d2.getImei(), devices.get(0).getImei());
        assertEquals(d1.getId(), devices.get(1).getImei());
    }
    @Test
    public void testGetDevicesSortingByLastReadingFields() {
        final Device d1 = createAndSaveDevice(sharedCompany, "111111111111");
        final Device d2 = createAndSaveDevice(sharedCompany, "222222222222");

        final ShipmentStatus status1 = ShipmentStatus.Default;
        final ShipmentStatus status2 = ShipmentStatus.Ended;

        createShipment(d1, ShipmentStatus.Arrived);
        createShipment(d2, ShipmentStatus.Arrived);
        final Shipment s11 = createShipment(d1, status1);
        final Shipment s22 = createShipment(d2, status2);

        final double lat1 = 11.11;
        final double lat2 = 22.22;
        final double lon1 = 11.11;
        final double lon2 = 22.22;
        final int bat1 = 1111;
        final int bat2 = 2222;
        final double t1 = 11.11;
        final double t2 = 22.22;
        final Date time2 = new Date();
        final Date time1 = new Date(time2.getTime() - 11111111l);

        createTrackerEvent(s11, lat1, lon2, bat1, t2, time1);
        createTrackerEvent(s11, lat2, lon1, bat2, t1, time2);

        createTrackerEvent(s22, lat2, lon1, bat2, t1, time2);
        createTrackerEvent(s22, lat1, lon2, bat1, t2, time1);

        List<ListDeviceItem> devices;
        //test by shipment
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_LAST_SHIPMENT), null);
        assertEquals(s11.getId(), devices.get(0).getShipmentId());
        assertEquals(s22.getId(), devices.get(1).getShipmentId());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_LAST_SHIPMENT), null);
        assertEquals(s22.getId(), devices.get(0).getShipmentId());
        assertEquals(s11.getId(), devices.get(1).getShipmentId());

        //test sort by latitude
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_LAST_READING_LAT), null);
        assertEquals(s22.getId(), devices.get(0).getShipmentId());
        assertEquals(s11.getId(), devices.get(1).getShipmentId());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_LAST_READING_LAT), null);
        assertEquals(s11.getId(), devices.get(0).getShipmentId());
        assertEquals(s22.getId(), devices.get(1).getShipmentId());

        //sort by last reading longitude
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_LAST_READING_LONG), null);
        assertEquals(s11.getId(), devices.get(0).getShipmentId());
        assertEquals(s22.getId(), devices.get(1).getShipmentId());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_LAST_READING_LONG), null);
        assertEquals(s22.getId(), devices.get(0).getShipmentId());
        assertEquals(s11.getId(), devices.get(1).getShipmentId());

        //sort by battery
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_LAST_READING_BATTERY), null);
        assertEquals(s22.getId(), devices.get(0).getShipmentId());
        assertEquals(s11.getId(), devices.get(1).getShipmentId());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_LAST_READING_BATTERY), null);
        assertEquals(s11.getId(), devices.get(0).getShipmentId());
        assertEquals(s22.getId(), devices.get(1).getShipmentId());

        //sort by temperature
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE), null);
        assertEquals(s11.getId(), devices.get(0).getShipmentId());
        assertEquals(s22.getId(), devices.get(1).getShipmentId());

        devices = dao.getDevices(sharedCompany,
                new Sorting(false, DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE), null);
        assertEquals(s22.getId(), devices.get(0).getShipmentId());
        assertEquals(s11.getId(), devices.get(1).getShipmentId());

        //sort by last reading time
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_LAST_READING_TIME_ISO), null);
        assertEquals(s22.getId(), devices.get(0).getShipmentId());
        assertEquals(s11.getId(), devices.get(1).getShipmentId());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_LAST_READING_TIME_ISO), null);
        assertEquals(s11.getId(), devices.get(0).getShipmentId());
        assertEquals(s22.getId(), devices.get(1).getShipmentId());

        //sort by shipment status
        devices = dao.getDevices(sharedCompany, new Sorting(DeviceConstants.PROPERTY_SHIPMENT_STATUS), null);
        assertEquals(s11.getId(), devices.get(0).getShipmentId());
        assertEquals(s22.getId(), devices.get(1).getShipmentId());

        devices = dao.getDevices(sharedCompany, new Sorting(false, DeviceConstants.PROPERTY_SHIPMENT_STATUS), null);
        assertEquals(s22.getId(), devices.get(0).getShipmentId());
        assertEquals(s11.getId(), devices.get(1).getShipmentId());
    }
    @Test
    public void testGetDevicesLeftCompany() {
        createAndSaveDevice(sharedCompany, "293487032784");
        createAndSaveDevice(sharedCompany, "834270983474");

        assertEquals(2, dao.getDevices(sharedCompany, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.getDevices(left, null, null).size());
    }
    @Test
    public void testGetDevicesPagination() {
        createAndSaveDevice(sharedCompany, "111111111111");
        createAndSaveDevice(sharedCompany, "222222222222");
        createAndSaveDevice(sharedCompany, "333333333333");
        createAndSaveDevice(sharedCompany, "444444444444");

        assertEquals(4, dao.getDevices(sharedCompany,
                new Sorting(DeviceConstants.PROPERTY_IMEI), null).size());
        assertEquals(3, dao.getDevices(sharedCompany,
                new Sorting(DeviceConstants.PROPERTY_IMEI), new Page(1, 3)).size());
        assertEquals(1, dao.getDevices(sharedCompany,
                new Sorting(DeviceConstants.PROPERTY_IMEI), new Page(2, 3)).size());
    }
    @Test
    public void testGetDevicesCorrectData() {
        final Device d = createAndSaveDevice(sharedCompany, "111111111111");
        d.setTripCount(999);
        dao.save(d);

        final Shipment s = createShipment(d, ShipmentStatus.Arrived);

        final double lat1 = 11.11;
        final double lat2 = 22.22;
        final double lon1 = 11.11;
        final double lon2 = 22.22;
        final int bat1 = 1111;
        final int bat2 = 2222;
        final double t1 = 11.11;
        final double t2 = 22.22;
        final Date time2 = new Date();
        final Date time1 = new Date(time2.getTime() - 11111111l);

        createTrackerEvent(s, lat1, lon1, bat1, t1, time1);
        createTrackerEvent(s, lat2, lon2, bat2, t2, time2);

        final ListDeviceItem item = dao.getDevices(sharedCompany, null, null).get(0);
        assertEquals(bat2, item.getBattery().doubleValue(), 0.0001);
        assertEquals(d.getDescription(), item.getDescription());
        assertEquals(d.getImei(), item.getImei());
        assertEquals(time2.getTime(), item.getLastReadingTime().getTime(), 2000);
        assertEquals(lat2, item.getLatitude().doubleValue(), 0.0001);
        assertEquals(lon2, item.getLongitude().doubleValue(), 0.0001);
        assertEquals(d.getName(), item.getName());
        assertEquals(s.getId(), item.getShipmentId());
        assertEquals(s.getStatus(), item.getShipmentStatus());
        assertEquals(t2, item.getTemperature().doubleValue(), 0.0001);
        assertEquals(d.getTripCount(), item.getTripCount());
    }
    @Test
    public void testGetByImei() {
        final String imei = "3984709382475";
        final Device d1 = createDevice(imei);
        final Device d3 = createDevice("234870432987");

        dao.save(d1);
        dao.save(d3);

        final Device d = dao.findByImei(imei);
        assertNotNull(d);

        //test one from found
        assertEquals("Test Device", d.getName());
        assertEquals("Test device", d.getDescription());

        //test company
        final Company c = d.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
    @Test
    public void testDeviceState() {
        final Device d = createDevice("3984709382475");
        dao.save(d);

        final DeviceState s = new DeviceState();

        dao.saveState(d.getImei(), s);
        assertNotNull(dao.getState(d.getImei()));

        //test update state
        dao.saveState(d.getImei(), s);
        assertNotNull(dao.getState(d.getImei()));

        //test on delete trigger
        dao.delete(d);
        assertNull(dao.getState(d.getImei()));
    }
    /**
     * @param d device.
     * @return shipment.
     */
    private Shipment createShipment(final Device d, final ShipmentStatus status) {
        final Shipment s = new Shipment();
        s.setDevice(d);
        s.setCompany(d.getCompany());
        s.setStatus(status);
        return getContext().getBean(ShipmentDao.class).save(s);
    }
    /**
     * @param c
     * @param imei
     */
    private Device createAndSaveDevice(final Company c, final String imei) {
        final Device d = createDevice(imei);
        d.setCompany(c);
        return dao.save(d);
    }
    /**
     * @param s shipment.
     * @param lat latitude.
     * @param lon longitude.
     * @param battery battery.
     * @param t temperature.
     * @param date date.
     */
    private TrackerEvent createTrackerEvent(final Shipment s, final double lat, final double lon,
            final int battery, final double t, final Date date) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(battery);
        e.setDevice(s.getDevice());
        e.setShipment(s);
        e.setLatitude(lat);
        e.setLongitude(lon);
        e.setTemperature(t);
        e.setTime(date);
        e.setType(TrackerEventType.AUT);
        return getContext().getBean(TrackerEventDao.class).save(e);
    }
    /**
     * @param name name.
     * @return shipment template
     */
    private ShipmentTemplate createShipmentTemplate(final String name) {
        final ShipmentTemplate s = new ShipmentTemplate();
        s.setCompany(sharedCompany);
        s.setName(name);
        return getContext().getBean(ShipmentTemplateDao.class).save(s);
    }
    /**
     * @param tpl
     * @return
     */
    private AutoStartShipment createAutoStartTemplate(final ShipmentTemplate tpl) {
        final AutoStartShipment aut = new AutoStartShipment();
        aut.setCompany(sharedCompany);
        aut.setTemplate(tpl);
        aut.setPriority(10);
        return getContext().getBean(AutoStartShipmentDao.class).save(aut);
    }
}
