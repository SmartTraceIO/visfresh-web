/**
 *
 */
package au.smarttrace.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import au.smarttrace.Color;
import au.smarttrace.Device;
import au.smarttrace.DeviceModel;
import au.smarttrace.User;
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.dao.runner.DaoTestRunner;
import au.smarttrace.dao.runner.DbSupport;
import au.smarttrace.device.DevicesDao;
import au.smarttrace.device.GetDevicesRequest;
import au.smarttrace.junit.categories.DaoTest;
import au.smarttrace.user.UsersDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class DevicesDaoTest {
    @Autowired
    private DevicesDao dao;
    @Autowired
    private UsersDao usersDao;
    @Autowired
    private DbSupport dbSupport;
    private Long company;

    /**
     * Default constructor.
     */
    public DevicesDaoTest() {
        super();
    }

    @Test
    public void testGetByImei() {
        final boolean active = true;
        final Color color = Color.BlueViolet;
        final String description = "Device Description";
        final String imei = "23508209853798";
        final String name = "JUnit Device";
        final DeviceModel model = DeviceModel.TT18;

        Device d = new Device();
        d.setActive(active);
        d.setColor(color);
        d.setCompany(company);
        d.setDescription(description);
        d.setImei(imei);
        d.setName(name);
        d.setModel(model);

        dao.createDevice(d);
        d = dao.getByImei(imei);

        assertEquals(active, d.isActive());
        assertEquals(color, d.getColor());
        assertEquals(company, d.getCompany());
        assertEquals(description, d.getDescription());
        assertEquals(imei, d.getImei());
        assertEquals(name, d.getName());
        assertEquals(model, d.getModel());
    }
    @Test
    public void testDeleteDevice() {
        final Device d = new Device();
        d.setImei("2345-09234-0983");
        d.setName("JUnit");
        dao.createDevice(d);
        dao.deleteDevice(d.getImei());

        assertNull(dao.getByImei(d.getImei()));
    }
    @Test
    public void testUpdateDevice() {
        Device d = createDevice("235-892-4982-094809");

        final boolean active = true;
        final Color color = Color.BlueViolet;
        final String description = "Device Description";
        final String name = "JUnit Device";
        final DeviceModel model = DeviceModel.TT18;
        final Long company = dbSupport.createSimpleCompany("JUnit 1");

        d.setActive(active);
        d.setColor(color);
        d.setCompany(company);
        d.setDescription(description);
        d.setName(name);
        d.setModel(model);

        dao.updateDevice(d);
        d = dao.getByImei(d.getImei());

        assertEquals(active, d.isActive());
        assertEquals(color, d.getColor());
        assertEquals(company, d.getCompany());
        assertEquals(description, d.getDescription());
        assertEquals(name, d.getName());
        assertEquals(model, d.getModel());
    }

    @Test
    public void testGetSetTripCount() {
        final Device d = createDevice("235-892-4982-094809");

        final int tripCount = 77;
        dao.setTripCount(d, tripCount);
        assertEquals(tripCount, dao.getTripCount(d));
    }
    @Test
    public void testCreateDevice() {
        final Device d = new Device();
        d.setImei("2345-09234-0983");
        d.setName("JUnit");
        d.setCompany(company);
        dao.createDevice(d);

        assertNotNull(dao.getByImei(d.getImei()));
    }
    @Test
    public void testGetDevicesSorting() {
        final Device d1  = createDevice("JUnit-A", "234098-09398098-b");
        final Device d2  = createDevice("Junit-B", "234098-09398098-a");

        final GetDevicesRequest req = new GetDevicesRequest();

        //by name
        req.getOrders().add(new Order("name", true));
        assertEquals(d1.getImei(), dao.getDevices(req).getItems().get(0).getImei());

        //by imei
        req.getOrders().clear();
        req.getOrders().add(new Order("imei", true));
        assertEquals(d2.getImei(), dao.getDevices(req).getItems().get(0).getImei());
    }
    @Test
    public void testGetDevicesMultiFieldSorting() {
        final Device d1  = createDevice("JUnit-A", "234098-09398098-a");
        final Device d2  = createDevice("Junit-A", "234098-09398098-b");

        final Device d3  = createDevice("Junit-B", "234098-09398098-c");
        final Device d4  = createDevice("Junit-B", "234098-09398098-d");

        final GetDevicesRequest req = new GetDevicesRequest();

        //by name
        req.getOrders().add(new Order("name", false));
        req.getOrders().add(new Order("imei", true));

        final List<Device> devices = dao.getDevices(req).getItems();
        assertEquals(d3.getImei(), devices.get(0).getImei());
        assertEquals(d4.getImei(), devices.get(1).getImei());
        assertEquals(d1.getImei(), devices.get(2).getImei());
        assertEquals(d2.getImei(), devices.get(3).getImei());
    }
    @Test
    public void testGetDevicesLimits() {
        createDevice("JUnit-A", "234098-09398098-a");
        createDevice("Junit-B", "234098-09398098-b");

        final GetDevicesRequest req = new GetDevicesRequest();
        req.setPageSize(1);

        assertEquals(1, dao.getDevices(req).getItems().size());
        req.setPage(1);
        assertEquals(1, dao.getDevices(req).getItems().size());
        req.setPage(2);
        assertEquals(0, dao.getDevices(req).getItems().size());

        assertEquals(2, dao.getDevices(req).getTotalCount());
    }
    @Test
    public void testGetDevicesFilterByCompanies() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C2");

        createDevice("JUnit-A", "234098-09398098-a", c1);
        createDevice("Junit-B", "234098-09398098-b", c2);

        final GetDevicesRequest req = new GetDevicesRequest();
        req.getCompanyFilter().add(c2);

        assertEquals(1, dao.getDevices(req).getItems().size());
    }
    @Test
    public void testGetDevicesFilterByName() {
        createDevice("JUnit-A", "234098-09398098-a");
        createDevice("Junit-B", "234098-09398098-b");

        final GetDevicesRequest req = new GetDevicesRequest();
        req.setNameFilter("ABCD it-b efgh");

        assertEquals(1, dao.getDevices(req).getItems().size());
    }
    @Test
    public void testGetDevicesFilterByImei() {
        createDevice("JUnit-A", "2-358--w8weroj");
        createDevice("Junit-B", "qpio90834jlajkd-");

        final GetDevicesRequest req = new GetDevicesRequest();
        req.setImeiFilter("ABCD jkd- efgh");

        assertEquals(1, dao.getDevices(req).getItems().size());
    }

    @Test
    public void testMoveToNewCompany() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C2");

        final Device d = createDevice("D", "2304895703457", c1);
        final Device backup = createDevice("D2", "3485-93-49809809", c1);

        dao.moveToNewCompany(d, c2, backup);

        assertEquals(c2, dao.getByImei(d.getImei()).getCompany());
    }
    @Test
    public void testMoveToNewCompanyShipments() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C2");

        final Device d = createDevice("D", "2304895703457", c1);
        final Device backup = createDevice("D2", "3485-93-49809809", c1);

        //create shipment
        final Long shipment = createShipment(d);
        dao.moveToNewCompany(d, c2, backup);

        //check shipment device
        assertEquals(backup.getImei(), dbSupport.getJdbc().queryForList(
                "select device from shipments where id = " + shipment, new HashMap<>()).get(0).get("device"));
        assertEquals("Ended", dbSupport.getJdbc().queryForList(
                "select status from shipments where id = " + shipment, new HashMap<>()).get(0).get("status"));
    }
    @Test
    public void testMoveToNewCompanyAlerts() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C2");

        final Device d = createDevice("D", "2304895703457", c1);
        final Device backup = createDevice("D2", "3485-93-49809809", c1);

        //create shipment
        final Long shipment = createShipment(d);
        final Long alert = createAlert(shipment);

        dao.moveToNewCompany(d, c2, backup);

        //check shipment device
        assertEquals(backup.getImei(), dbSupport.getJdbc().queryForList(
                "select device from alerts where id = " + alert, new HashMap<>()).get(0).get("device"));
    }
    @Test
    public void testMoveToNewCompanyArrivals() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C2");

        final Device d = createDevice("D", "2304895703457", c1);
        final Device backup = createDevice("D2", "3485-93-49809809", c1);

        //create shipment
        final Long shipment = createShipment(d);
        final Long arrival = createArrival(shipment);

        dao.moveToNewCompany(d, c2, backup);

        //check shipment device
        assertEquals(backup.getImei(), dbSupport.getJdbc().queryForList(
                "select device from arrivals where id = " + arrival, new HashMap<>()).get(0).get("device"));
    }
    @Test
    public void testMoveToNewCompanyReadings() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C2");

        final Device d = createDevice("D", "2304895703457", c1);
        final Device backup = createDevice("D2", "3485-93-49809809", c1);

        //create shipment
        final Long shipment = createShipment(d);
        final Long reading = createReading(shipment);

        dao.moveToNewCompany(d, c2, backup);

        //check shipment device
        assertEquals(backup.getImei(), dbSupport.getJdbc().queryForList(
                "select device from trackerevents where id = " + reading, new HashMap<>()).get(0).get("device"));
    }
    @Test
    public void testMoveToNewCompanySimulators() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C2");

        final Device src = createDevice("D", "2304895703457", c1);
        final Device dst = createDevice("D2", "3485-93-49809809", c1);

        //create user
        final User user = new User();
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setCompany(company);
        user.setEmail("junit@developer.org");
        usersDao.createUser(user, "");

        //create simulator
        createSimulator(user, src, dst);

        dao.moveToNewCompany(src, c2, dst);

        //check shipment device
        final Map<String, Object> row = dbSupport.getJdbc().queryForList(
                "select count(*) as count from simulators", new HashMap<>()).get(0);
        assertEquals(0, ((Number) row.get("count")).intValue());
    }
    @Test
    public void testMoveToNewCompanyDeviceCommands() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C2");

        final Device device = createDevice("D", "2304895703457", c1);
        final Device backup = createDevice("D2", "3485-93-49809809", c1);

        //create device command
        createDeviceCommand(device, "STOP");

        dao.moveToNewCompany(device, c2, backup);

        //check shipment device
        final Map<String, Object> row = dbSupport.getJdbc().queryForList(
                "select count(*) as count from devicecommands", new HashMap<>()).get(0);
        assertEquals(0, ((Number) row.get("count")).intValue());
    }

    private Long createSimulator(final User user, final Device src, final Device dst) {
        final Map<String, Object> params = new HashMap<>();
        params.put("source", src.getImei());
        params.put("target", dst.getImei());
        params.put("user", user.getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        dbSupport.getJdbc().update("insert into simulators(source, target, user)"
                + " values (:source, :target, :user)",
                new MapSqlParameterSource(params), keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }
    private Long createDeviceCommand(final Device d, final String cmd) {
        final Map<String, Object> params = new HashMap<>();
        params.put("device", d.getImei());
        params.put("cmd", cmd);
        params.put("date", new Date());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        dbSupport.getJdbc().update("insert into devicecommands(device, command, `date`)"
                + " values (:device, :cmd, :date)",
                new MapSqlParameterSource(params), keyHolder);

        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }
    /**
     * @param shipment
     * @return
     */
    private Long createAlert(final Long shipment) {
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", shipment);
        params.put("date", new Date());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        dbSupport.getJdbc().update("insert into alerts"
                + "(`type`, temperature, minutes, `date`, device, shipment)"
                + " select 'LightOn', 5, 10, :date, device, id"
                + " from shipments where id = :shipment",
                new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }
    /**
     * @param shipment
     * @return
     */
    private Long createArrival(final Long shipment) {
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", shipment);
        params.put("date", new Date());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        dbSupport.getJdbc().update("insert into arrivals"
                + "(nummeters, `date`, device, shipment)"
                + " select 500, :date, device, id"
                + " from shipments where id = :shipment",
                new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }
    /**
     * @param shipment
     * @return
     */
    private Long createReading(final Long shipment) {
        final Map<String, Object> params = new HashMap<>();
        params.put("shipment", shipment);
        params.put("date", new Date());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        dbSupport.getJdbc().update("insert into trackerevents"
                + "(`type`, `time`, battery, temperature, createdon, device, shipment)"
                + " select 'AUT', :date, 5000, 2, :date, device, id"
                + " from shipments where id = :shipment",
                new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }
    /**
     * @param imei
     * @return
     */
    private Device createDevice(final String imei) {
        return createDevice("JUnit", imei);
    }
    /**
     * @param name
     * @param imei
     * @return
     */
    private Device createDevice(final String name, final String imei) {
        return createDevice(name, imei, company);
    }

    /**
     * @param name name.
     * @param imei IMEI code
     * @param companyId company
     * @return new device saved in DB.
     */
    protected Device createDevice(final String name, final String imei, final Long companyId) {
        final Device d = new Device();
        d.setImei(imei);
        d.setName(name);
        d.setCompany(companyId);
        dao.createDevice(d);
        return d;
    }
    /**
     * @param d device.
     * @return shipment ID.
     */
    private Long createShipment(final Device d) {
        final Map<String, Object> params = new HashMap<>();
        params.put("device", d.getImei());
        params.put("company", d.getCompany());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        dbSupport.getJdbc().update("insert into shipments"
                + "(device, company, istemplate, noalertsifcooldown, nonotifsifnoalerts, status)"
                + " values (:device, :company, false, false, false, 'InProgress')",
                new MapSqlParameterSource(params), keyHolder);
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        return null;
    }

    @Before
    public void setUp() {
        company = dbSupport.createSimpleCompany("JUnit");
    }
    @After
    public void tearDown() {
        dbSupport.deleteUsers();
        dbSupport.getJdbc().update("delete from devicecommands", new HashMap<>());
        dbSupport.getJdbc().update("delete from trackerevents", new HashMap<>());
        dbSupport.getJdbc().update("delete from arrivals", new HashMap<>());
        dbSupport.getJdbc().update("delete from alerts", new HashMap<>());
        dbSupport.deleteShipments();
        dbSupport.deleteDevices();
        dbSupport.deleteCompanies();
    }
}
