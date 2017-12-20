/**
 *
 */
package au.smarttrace.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.Language;
import au.smarttrace.MeasurementUnits;
import au.smarttrace.Roles;
import au.smarttrace.TemperatureUnits;
import au.smarttrace.User;
import au.smarttrace.dao.runner.DaoTestRunner;
import au.smarttrace.junit.TestUtils;
import au.smarttrace.junit.categories.DaoTest;
import au.smarttrace.user.UsersDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class UsersDaoTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private UsersDao dao;
    private Long company;

    /**
     * Default constructor.
     */
    public UsersDaoTest() {
        super();
    }

    @Test
    public void findUserByEmailAndPassword() {
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
    public void createUser() {
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
    public void updatePassword() {
        final User user = createUser("user@junit.org");

        final String password = "2350uhwuy018290";
        dao.updatePassword(user.getId(), password);
        assertNotNull(dao.findUserByEmailAndPassword(user.getEmail(), password));
    }
    @Test
    public void updateUser() {
        final String email = "user@junit.org";
        final String newName = "New User Name";

        User user = createUser(email);
        user.setFirstName(newName);

        dao.saveUser(user);

        user = dao.findUserByEmail(user.getEmail());
        assertEquals(newName, user.getFirstName());
    }
    @Test
    public void deleteUser() {
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
    @Before
    public void setUp() {
        company = TestUtils.createSimpleCompany(jdbc, "JUnit");
    }
    @After
    public void tearDown() {
        final Map<String, Object> params = new HashMap<>();
        //delete all users, should automatically delete sessions.
        jdbc.update("delete from users", params);
        TestUtils.deleteCompanies(jdbc);
    }
}
