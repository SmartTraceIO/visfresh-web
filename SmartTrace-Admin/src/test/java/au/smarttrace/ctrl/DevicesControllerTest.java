/**
 *
 */
package au.smarttrace.ctrl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.Color;
import au.smarttrace.Company;
import au.smarttrace.Device;
import au.smarttrace.Roles;
import au.smarttrace.User;
import au.smarttrace.company.CompaniesDao;
import au.smarttrace.company.CompaniesService;
import au.smarttrace.ctrl.client.AuthClient;
import au.smarttrace.ctrl.client.DevicesClient;
import au.smarttrace.ctrl.client.ServiceException;
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.ctrl.res.ColorDto;
import au.smarttrace.ctrl.runner.ControllerTestRunner;
import au.smarttrace.ctrl.runner.ServiceUrlHolder;
import au.smarttrace.device.DevicesService;
import au.smarttrace.device.GetDevicesRequest;
import au.smarttrace.junit.categories.ControllerTest;
import au.smarttrace.user.UsersService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Category(ControllerTest.class)
@RunWith(ControllerTestRunner.class)
public class DevicesControllerTest {
    private AuthClient authClient = new AuthClient();
    private DevicesClient client = new DevicesClient();

    @Autowired
    private ApplicationContext context;
    @Autowired
    private DevicesService devicesService;
    private Company company;

    /**
     * Default constructor.
     */
    public DevicesControllerTest() {
        super();
    }

    @Before
    public void setUp() throws ServiceException, IOException {
        final String serviceUrl = context.getBean(ServiceUrlHolder.class).getServiceUrl();
        authClient.setServiceUrl(serviceUrl);
        client.setServiceUrl(serviceUrl);

        company  = new Company();
        company.setName("JUnit Company");
        context.getBean(CompaniesDao.class).createCompany(company);

        //create user
        final User u = new User();
        u.setEmail("junit@developer.com");
        u.setFirstName("Java");
        u.setLastName("Developer");
        u.setCompany(company.getId());
        u.getRoles().add(Roles.SmartTraceAdmin);
        final String password = "password";
        context.getBean(UsersService.class).createUser(u, password);

        //login
        final String token = authClient.login(u.getEmail(), password).getToken().getToken();
        authClient.setAccessToken(token);
        client.setAccessToken(token);
    }

    @Test
    public void testGetByImei() throws IOException, ServiceException {
        final boolean active = true;
        final Color color = Color.BlueViolet;
        final String description = "Device Description";
        final String imei = "23508209853798";
        final String name = "JUnit Device";

        Device d = new Device();
        d.setActive(active);
        d.setColor(color);
        d.setCompany(company.getId());
        d.setDescription(description);
        d.setImei(imei);
        d.setName(name);

        devicesService.createDevice(d);
        d = client.getDevice(imei);

        assertEquals(active, d.isActive());
        assertEquals(color, d.getColor());
        assertEquals(company.getId(), d.getCompany());
        assertEquals(description, d.getDescription());
        assertEquals(imei, d.getImei());
        assertEquals(name, d.getName());
    }
    @Test
    public void testDeleteDevice() throws IOException, ServiceException {
        final Device d = new Device();
        d.setImei("2345-09234-0983");
        d.setName("JUnit");
        devicesService.createDevice(d);

        client.deleteDevice(d.getImei());

        assertNull(devicesService.getDevice(d.getImei()));
    }
    @Test
    public void testUpdateDevice() throws IOException, ServiceException {
        Device d = createDevice("235-892-4982-094809");

        final boolean active = true;
        final Color color = Color.BlueViolet;
        final String description = "Device Description";
        final String name = "JUnit Device";
        final Long company = createCompany("JUnit 1").getId();

        d.setActive(active);
        d.setColor(color);
        d.setCompany(company);
        d.setDescription(description);
        d.setName(name);

        client.updateDevice(d);
        d = devicesService.getDevice(d.getImei());

        assertEquals(active, d.isActive());
        assertEquals(color, d.getColor());
        assertEquals(company, d.getCompany());
        assertEquals(description, d.getDescription());
        assertEquals(name, d.getName());
    }

    @Test
    public void testCreateDevice() throws IOException, ServiceException {
        final boolean active = true;
        final Color color = Color.BlueViolet;
        final String description = "Device Description";
        final String imei = "23508209853798";
        final String name = "JUnit Device";

        Device d = new Device();
        d.setActive(active);
        d.setColor(color);
        d.setCompany(company.getId());
        d.setDescription(description);
        d.setImei(imei);
        d.setName(name);

        client.createDevice(d);
        d = devicesService.getDevice(imei);

        assertEquals(active, d.isActive());
        assertEquals(color, d.getColor());
        assertEquals(company.getId(), d.getCompany());
        assertEquals(description, d.getDescription());
        assertEquals(imei, d.getImei());
        assertEquals(name, d.getName());
    }
    @Test
    public void testGetDevicesSorting() throws IOException, ServiceException {
        final Device d1  = createDevice("JUnit-A", "234098-09398098-b");
        final Device d2  = createDevice("Junit-B", "234098-09398098-a");

        final GetDevicesRequest req = new GetDevicesRequest();

        //by name
        req.getOrders().add(new Order("name", true));
        assertEquals(d1.getImei(), client.getDevices(req).getItems().get(0).getImei());

        //by imei
        req.getOrders().clear();
        req.getOrders().add(new Order("imei", true));
        assertEquals(d2.getImei(), client.getDevices(req).getItems().get(0).getImei());
    }
    @Test
    public void testGetDevicesMultiFieldSorting() throws IOException, ServiceException {
        final Device d1  = createDevice("JUnit-A", "234098-09398098-a");
        final Device d2  = createDevice("Junit-A", "234098-09398098-b");

        final Device d3  = createDevice("Junit-B", "234098-09398098-c");
        final Device d4  = createDevice("Junit-B", "234098-09398098-d");

        final GetDevicesRequest req = new GetDevicesRequest();

        //by name
        req.getOrders().add(new Order("name", false));
        req.getOrders().add(new Order("imei", true));

        final List<Device> devices = client.getDevices(req).getItems();
        assertEquals(d3.getImei(), devices.get(0).getImei());
        assertEquals(d4.getImei(), devices.get(1).getImei());
        assertEquals(d1.getImei(), devices.get(2).getImei());
        assertEquals(d2.getImei(), devices.get(3).getImei());
    }
    @Test
    public void testGetDevicesLimits() throws IOException, ServiceException {
        createDevice("JUnit-A", "234098-09398098-a");
        createDevice("Junit-B", "234098-09398098-b");

        final GetDevicesRequest req = new GetDevicesRequest();
        req.setPageSize(1);

        assertEquals(1, client.getDevices(req).getItems().size());
        req.setPage(1);
        assertEquals(1, client.getDevices(req).getItems().size());
        req.setPage(2);
        assertEquals(0, client.getDevices(req).getItems().size());

        assertEquals(2, client.getDevices(req).getTotalCount());
    }
    @Test
    public void testGetDevicesFilterByCompanies() throws IOException, ServiceException {
        final Long c1 = createCompany("C1").getId();
        final Long c2 = createCompany("C2").getId();

        createDevice("JUnit-A", "234098-09398098-a", c1);
        createDevice("Junit-B", "234098-09398098-b", c2);

        final GetDevicesRequest req = new GetDevicesRequest();
        req.getCompanyFilter().add(c2);

        assertEquals(1, client.getDevices(req).getItems().size());
    }
    @Test
    public void testGetDevicesFilterByName() throws IOException, ServiceException {
        createDevice("JUnit-A", "234098-09398098-a");
        createDevice("Junit-B", "234098-09398098-b");

        final GetDevicesRequest req = new GetDevicesRequest();
        req.setNameFilter("ABCD it-b efgh");

        assertEquals(1, client.getDevices(req).getItems().size());
    }
    @Test
    public void testGetDevicesFilterByImei() throws IOException, ServiceException {
        createDevice("JUnit-A", "2-358--w8weroj");
        createDevice("Junit-B", "qpio90834jlajkd-");

        final GetDevicesRequest req = new GetDevicesRequest();
        req.setImeiFilter("ABCD jkd- efgh");

        assertEquals(1, client.getDevices(req).getItems().size());
    }

    @Test
    public void testMoveToNewCompany() throws IOException, ServiceException {
        final Long c1 = createCompany("C1").getId();
        final Long c2 = createCompany("C2").getId();

        final Device d = createDevice("D", "2304895703457", c1);

        final Device response = client.moveDevice(d, c2);

        assertEquals(c2, devicesService.getDevice(d.getImei()).getCompany());

        //check backup have old company
        final Device backup = devicesService.getDevice(response.getImei());
        assertEquals(c1, backup.getCompany());
    }
    /**
     * @return list device colors.
     * @throws ServiceException
     * @throws IOException
     */
    @Test
    public void getDeviceColors() throws IOException, ServiceException {
        final List<ColorDto> colors = client.getDeviceColors();
        assertTrue(colors.size() > 0);
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
        return createDevice(name, imei, company.getId());
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
        devicesService.createDevice(d);
        return d;
    }
    /**
     * @param name company name.
     * @param description company description.
     * @return
     */
    private Company createCompany(final String name) {
        final Company company = new Company();
        company.setName(name);
        context.getBean(CompaniesService.class).createCompany(company);
        return company;
    }
    @After
    public void tearDown() {
        final NamedParameterJdbcTemplate jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        jdbc.update("delete from devices", new HashMap<>());
        jdbc.update("delete from users", new HashMap<>());
        jdbc.update("delete from companies", new HashMap<>());
    }
}
