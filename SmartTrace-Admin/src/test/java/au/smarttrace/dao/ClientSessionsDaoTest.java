/**
 *
 */
package au.smarttrace.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.Language;
import au.smarttrace.MeasurementUnits;
import au.smarttrace.Roles;
import au.smarttrace.TemperatureUnits;
import au.smarttrace.User;
import au.smarttrace.dao.runner.DaoTestRunner;
import au.smarttrace.dao.runner.DbSupport;
import au.smarttrace.junit.AssertUtils;
import au.smarttrace.junit.categories.DaoTest;
import au.smarttrace.security.AccessToken;
import au.smarttrace.security.AuthInfo;
import au.smarttrace.security.ClientSessionsDao;
import au.smarttrace.user.UsersDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RunWith(DaoTestRunner.class)
@Category(DaoTest.class)
public class ClientSessionsDaoTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private ClientSessionsDao dao;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private DbSupport dbSupport;

    private Long company;

    /**
     * Default constructor.
     */
    public ClientSessionsDaoTest() {
        super();
    }

    @Test
    public void deleteSession() {
        final User user = createUser("user@junit.org");

        //create user session
        final AccessToken t = createToken("2304587230957987", new Date());
        dao.createSession(user.getId(), t);

        final String selectSessions = "select user from restsessions";
        assertEquals(1, jdbc.queryForList(selectSessions, new HashMap<>()).size());

        dao.deleteSession(t.getToken());
        assertEquals(0, jdbc.queryForList(selectSessions, new HashMap<>()).size());
    }
    @Test
    public void getAuthInfo() {
        //create user
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

        final User user = new User();
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

        context.getBean(UsersDao.class).createUser(user, "02385470294387");

        //create token
        final String tokenStr = "2398579070987098";
        final Date expirationTime = new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000l);

        final AccessToken token = new AccessToken(tokenStr);
        token.setExpirationTime(expirationTime);

        dao.createSession(user.getId(), token);

        //check result
        final AuthInfo info = dao.getAuthInfo(tokenStr);
        assertNotNull(info);

        //check user
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

        //check token
        assertEquals(tokenStr, info.getToken().getToken());
        AssertUtils.assertEqualDates(expirationTime, info.getToken().getExpirationTime(), 1000l);
    }
    @Test
    public void createSession() {
        final User user = createUser("user@junit.org");

        //create user session
        final AccessToken t = createToken("2304587230957987", new Date());
        dao.createSession(user.getId(), t);

        final String selectSessions = "select user from restsessions";
        assertEquals(1, jdbc.queryForList(selectSessions, new HashMap<>()).size());
    }
    /**
     * @param token access token string.
     * @param expiredOn expiration date.
     * @return access token.
     */
    private AccessToken createToken(final String token, final Date expiredOn) {
        final AccessToken t = new AccessToken(token);
        t.setExpirationTime(expiredOn);
        return t;
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

        context.getBean(UsersDao.class).createUser(user, "1234567890");
        return user;
    }
    @Before
    public void setUp() {
        this.company = dbSupport.createSimpleCompany("JUnit");
    }
    @After
    public void tearDown() {
        dbSupport.deleteUsers();
        dbSupport.deleteCompanies();
    }
}
