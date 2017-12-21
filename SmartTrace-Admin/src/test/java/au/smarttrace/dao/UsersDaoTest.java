/**
 *
 */
package au.smarttrace.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import au.smarttrace.Language;
import au.smarttrace.MeasurementUnits;
import au.smarttrace.Roles;
import au.smarttrace.TemperatureUnits;
import au.smarttrace.User;
import au.smarttrace.ctrl.req.Order;
import au.smarttrace.dao.runner.DaoTestRunner;
import au.smarttrace.dao.runner.DbSupport;
import au.smarttrace.junit.categories.DaoTest;
import au.smarttrace.user.GetUsersRequest;
import au.smarttrace.user.UsersDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class UsersDaoTest {
    @Autowired
    private UsersDao dao;
    @Autowired
    private DbSupport dbSupport;
    private Long company;

    /**
     * Default constructor.
     */
    public UsersDaoTest() {
        super();
    }

    @Test
    public void testFindUserByEmailAndPassword() {
        final boolean active = true;
        final String deviceGroup = "Device group";
        final String email = "dev@junit.org";
        final boolean external = true;
        final String externalCompany = "External Company";
        final String firstName = "FirstName";
        final Language language = Language.German;
        final String lastName = "LastName";
        final MeasurementUnits measurementUnits = MeasurementUnits.English;
        final String phone = "1111111117";
        final String position = "developer";
        final TemperatureUnits temperatureUnits = TemperatureUnits.Fahrenheit;
        final TimeZone timeZone = TimeZone.getTimeZone("PCT");
        final String title = "Title";
        final String admin = Roles.Admin;
        final String smartTraceAdmin = Roles.SmartTraceAdmin;

        User user = new User();
        user.setActive(active);
        user.setCompany(company);
        user.setDeviceGroup(deviceGroup);
        user.setEmail(email);
        user.setExternal(external);
        user.setExternalCompany(externalCompany);
        user.setFirstName(firstName);
        user.setLanguage(language);
        user.setLastName(lastName);
        user.setMeasurementUnits(measurementUnits);
        user.setPhone(phone);
        user.setPosition(position);
        user.setTemperatureUnits(temperatureUnits);
        user.setTimeZone(timeZone);
        user.setTitle(title);
        user.getRoles().add(admin);
        user.getRoles().add(smartTraceAdmin);

        final String passwordHash = "02385470294387";
        dao.createUser(user, passwordHash);

        user = dao.findUserByEmailAndPassword(email, passwordHash);

        assertNotNull(user);
        assertEquals(active, user.isActive());
        assertEquals(company, user.getCompany());
        assertEquals(deviceGroup, user.getDeviceGroup());
        assertEquals(email, user.getEmail());
        assertEquals(external, user.isExternal());
        assertEquals(externalCompany, user.getExternalCompany());
        assertEquals(firstName, user.getFirstName());
        assertEquals(language, user.getLanguage());
        assertEquals(lastName, user.getLastName());
        assertEquals(measurementUnits, user.getMeasurementUnits());
        assertEquals(phone, user.getPhone());
        assertEquals(position, user.getPosition());
        assertEquals(temperatureUnits, user.getTemperatureUnits());
        assertEquals(timeZone, user.getTimeZone());
        assertEquals(title, user.getTitle());
        assertTrue(user.getRoles().contains(admin));
        assertTrue(user.getRoles().contains(smartTraceAdmin));

        //test with left password
        assertNull(dao.findUserByEmailAndPassword(email, ""));
    }
    @Test
    public void testSorting() {
        final User u1  = createUser("FirstName-A", "LirstName-B", "b@junit.org");
        final User u2  = createUser("FirstName-B", "LirstName-A", "a@junit.org");

        final GetUsersRequest req = new GetUsersRequest();

        //by first name
        req.getOrders().add(new Order("firstName", true));
        assertEquals(u1.getId(), dao.getUsers(req).getItems().get(0).getId());

        //by last name
        req.getOrders().clear();
        req.getOrders().add(new Order("lastName", true));
        assertEquals(u2.getId(), dao.getUsers(req).getItems().get(0).getId());

        //by email
        req.getOrders().clear();
        req.getOrders().add(new Order("email", true));
        assertEquals(u2.getId(), dao.getUsers(req).getItems().get(0).getId());
    }
    @Test
    public void testSortingDesc() {
        createUser("FirstName-A", "LirstName-B", "b@junit.org");
        final User u2  = createUser("FirstName-B", "LirstName-A", "a@junit.org");

        final GetUsersRequest req = new GetUsersRequest();

        //by first name
        req.getOrders().add(new Order("firstName", false));
        assertEquals(u2.getId(), dao.getUsers(req).getItems().get(0).getId());

        //by last name
        req.getOrders().clear();
        req.getOrders().add(new Order("lastName", true));
        assertEquals(u2.getId(), dao.getUsers(req).getItems().get(0).getId());
    }
    @Test
    public void testMultiFieldSorting() {
        final User u1  = createUser("FirstName-A", "LirstName", "a1@junit.org");
        final User u2  = createUser("FirstName-B", "LirstName", "a2@junit.org");

        final User u3  = createUser("FirstName-A", "LirstName", "b1@junit.org");
        final User u4  = createUser("FirstName-B", "LirstName", "b2@junit.org");

        final GetUsersRequest req = new GetUsersRequest();
        req.getOrders().add(new Order("firstName", false));
        req.getOrders().add(new Order("email", true));

        final List<User> users = dao.getUsers(req).getItems();

        assertEquals(u2.getId(), users.get(0).getId());
        assertEquals(u4.getId(), users.get(1).getId());
        assertEquals(u1.getId(), users.get(2).getId());
        assertEquals(u3.getId(), users.get(3).getId());
    }
    @Test
    public void testLimits() {
        createUser("FirstName-A", "LirstName", "a1@junit.org");
        createUser("FirstName-B", "LirstName", "a2@junit.org");

        final GetUsersRequest req = new GetUsersRequest();
        req.setPageSize(1);

        assertEquals(1, dao.getUsers(req).getItems().size());
        req.setPage(1);
        assertEquals(1, dao.getUsers(req).getItems().size());
        req.setPage(2);
        assertEquals(0, dao.getUsers(req).getItems().size());

        assertEquals(2, dao.getUsers(req).getTotalCount());
    }
    @Test
    public void testFilterByCompanies() {
        final Long c1 = dbSupport.createSimpleCompany("C1");
        final Long c2 = dbSupport.createSimpleCompany("C1");
        final Long c3 = dbSupport.createSimpleCompany("C1");

        createUser("u1", c1);
        createUser("u2", c2);

        final GetUsersRequest req = new GetUsersRequest();

        assertEquals(2, dao.getUsers(req).getItems().size());

        req.getCompanyFilter().add(c3);
        assertEquals(0, dao.getUsers(req).getItems().size());

        req.getCompanyFilter().add(c1);
        assertEquals(1, dao.getUsers(req).getItems().size());

        req.getCompanyFilter().add(c2);
        assertEquals(2, dao.getUsers(req).getItems().size());
    }

    @Test
    public void testFilterByName() {
        createUser("FirstName-A", "LastName", "a@junit.org");

        final GetUsersRequest req = new GetUsersRequest();
        req.setNameFilter("A B First");
        assertEquals(1, dao.getUsers(req).getItems().size());

        req.setNameFilter("C D Last");
        assertEquals(1, dao.getUsers(req).getItems().size());

        req.setNameFilter("Third ED");
        assertEquals(0, dao.getUsers(req).getItems().size());
    }
    @Test
    public void testFilterByEmail() {
        createUser("FirstName-A", "LirstName", "a@junit.org");

        final GetUsersRequest req = new GetUsersRequest();
        req.setEmailFilter("a");
        assertEquals(1, dao.getUsers(req).getItems().size());

        req.setEmailFilter("@junit.org");
        assertEquals(1, dao.getUsers(req).getItems().size());

        req.setEmailFilter("b@junit.org");
        assertEquals(0, dao.getUsers(req).getItems().size());
    }
    @Test
    public void testCreateUser() {
        final String email = "user@junit.org";
        final String passwordHash = "230870987349";

        User user = new User();
        user.setEmail(email);
        user.setCompany(company);
        user.setFirstName("Java Developer");
        user.getRoles().add(Roles.Admin);
        user.getRoles().add(Roles.SmartTraceAdmin);

        dao.createUser(user, passwordHash);

        user = dao.findUserByEmailAndPassword(email, passwordHash);
        assertNotNull(user);
    }
    @Test
    public void testUpdatePassword() {
        final User user = createUser("user@junit.org");

        final String password = "2350uhwuy018290";
        dao.updatePassword(user.getId(), password);
        assertNotNull(dao.findUserByEmailAndPassword(user.getEmail(), password));
    }
    @Test
    public void testUpdateUser() {
        final boolean active = true;
        final String deviceGroup = "Device group";
        final String email = "dev@junit.org";
        final boolean external = true;
        final String externalCompany = "External Company";
        final String firstName = "FirstName";
        final Language language = Language.German;
        final String lastName = "LastName";
        final MeasurementUnits measurementUnits = MeasurementUnits.English;
        final String phone = "1111111117";
        final String position = "developer";
        final TemperatureUnits temperatureUnits = TemperatureUnits.Fahrenheit;
        final TimeZone timeZone = TimeZone.getTimeZone("PCT");
        final String title = "Title";
        final String admin = Roles.Admin;
        final String smartTraceAdmin = Roles.SmartTraceAdmin;

        User user = createUser(email);

        user.setActive(active);
        user.setCompany(company);
        user.setDeviceGroup(deviceGroup);
        user.setEmail(email);
        user.setExternal(external);
        user.setExternalCompany(externalCompany);
        user.setFirstName(firstName);
        user.setLanguage(language);
        user.setLastName(lastName);
        user.setMeasurementUnits(measurementUnits);
        user.setPhone(phone);
        user.setPosition(position);
        user.setTemperatureUnits(temperatureUnits);
        user.setTimeZone(timeZone);
        user.setTitle(title);
        user.getRoles().add(admin);
        user.getRoles().add(smartTraceAdmin);

        dao.saveUser(user);

        user = dao.findUserByEmail(email);

        assertNotNull(user);
        assertEquals(active, user.isActive());
        assertEquals(company, user.getCompany());
        assertEquals(deviceGroup, user.getDeviceGroup());
        assertEquals(email, user.getEmail());
        assertEquals(external, user.isExternal());
        assertEquals(externalCompany, user.getExternalCompany());
        assertEquals(firstName, user.getFirstName());
        assertEquals(language, user.getLanguage());
        assertEquals(lastName, user.getLastName());
        assertEquals(measurementUnits, user.getMeasurementUnits());
        assertEquals(phone, user.getPhone());
        assertEquals(position, user.getPosition());
        assertEquals(temperatureUnits, user.getTemperatureUnits());
        assertEquals(timeZone, user.getTimeZone());
        assertEquals(title, user.getTitle());
        assertTrue(user.getRoles().contains(admin));
        assertTrue(user.getRoles().contains(smartTraceAdmin));
    }
    @Test
    public void testDeleteUser() {
        final User user = createUser("user@junit.org");
        dao.deleteUser(user.getId());

        assertNull(dao.findUserByEmail(user.getEmail()));
    }
    /**
     * @param email
     * @return
     */
    private User createUser(final String email) {
        final User user = new User();
        user.setEmail(email);
        user.setFirstName(email);
        user.setCompany(company);

        dao.createUser(user, "1234567890");
        return user;
    }
    /**
     * @param firstName
     * @param lastName
     * @param email
     * @return
     */
    private User createUser(final String firstName, final String lastName, final String email) {
        final User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCompany(company);
        dao.createUser(user, "1234567890");
        return user;
    }
    /**
     * @param name
     * @param company
     * @return
     */
    private User createUser(final String name, final Long company) {
        final User user = new User();
        user.setEmail(name + "@junit.org");
        user.setFirstName("First_" + name);
        user.setLastName("Last_" + name);
        user.setCompany(company);
        dao.createUser(user, "1234567890");
        return user;
    }
    @Before
    public void setUp() {
        company = dbSupport.createSimpleCompany("JUnit");
    }
    @After
    public void tearDown() {
        dbSupport.deleteUsers();
        dbSupport.deleteCompanies();
    }
}
