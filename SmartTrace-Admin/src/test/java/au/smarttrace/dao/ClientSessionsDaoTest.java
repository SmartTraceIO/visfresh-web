/**
 *
 */
package au.smarttrace.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import au.smarttrace.Roles;
import au.smarttrace.User;
import au.smarttrace.dao.runner.DaoTestRunner;
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

        final String selectSessions = "select user from sessions";
        assertEquals(1, jdbc.queryForList(selectSessions, new HashMap<>()).size());

        dao.deleteSession(t.getToken());
        assertEquals(0, jdbc.queryForList(selectSessions, new HashMap<>()).size());
    }
    @Test
    public void getAuthInfo() {
        //create user
        final String email = "user@junit.org";
        final String firstName = "Java Developer";
        final String passwordHash = "230870987349";

        final User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.getRoles().add(Roles.Admin);
        user.getRoles().add(Roles.SmartTraceAdmin);

        context.getBean(UsersDao.class).createUser(user, passwordHash);

        //create token
        final String tokenStr = "2398579070987098";
        final Date createdTime = new Date(System.currentTimeMillis() - 3 * 60 * 60 * 1000l);
        final Date expirationTime = new Date(createdTime.getTime() + 2 * 60 * 60 * 1000l);

        final AccessToken token = new AccessToken(tokenStr);
        token.setExpirationTime(expirationTime);

        dao.createSession(user.getId(), token);

        //check result
        final AuthInfo info = dao.getAuthInfo(tokenStr);
        assertNotNull(info);

        //check user
        assertEquals(email, info.getUser().getEmail());
        assertEquals(firstName, info.getUser().getFirstName());
        assertEquals(2, info.getUser().getRoles().size());
        assertTrue(info.getUser().getRoles().contains(Roles.Admin));
        assertTrue(info.getUser().getRoles().contains(Roles.SmartTraceAdmin));

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

        final String selectSessions = "select user from sessions";
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

        context.getBean(UsersDao.class).createUser(user, "1234567890");
        return user;
    }
    @After
    public void tearDown() {
        final Map<String, Object> params = new HashMap<>();
        //delete all users, should automatically delete sessions.
        jdbc.update("delete from users", params);
    }
}
