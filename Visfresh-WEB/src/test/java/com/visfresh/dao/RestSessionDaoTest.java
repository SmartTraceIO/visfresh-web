/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.RestSession;
import com.visfresh.entities.User;
import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestSessionDaoTest extends BaseCrudTest<RestSessionDao, RestSession, Long>{
    private User user;
    private long lastId = 1;

    /**
     * Default constructor.
     */
    public RestSessionDaoTest() {
        super(RestSessionDao.class);
    }

    @Before
    public void setUp() {
        user = createUser("junit@u.ser");
    }

    @Test
    public void testFindByToken() {
        dao.save(createTestEntity());
        dao.save(createTestEntity());
        final RestSession session = dao.save(createTestEntity());

        assertEquals(session.getId(), dao.findByToken(session.getToken().getToken()).getId());
    }
    @Test
    public void testProperties() {
        RestSession s1 = dao.save(createTestEntity());
        RestSession s2 = dao.save(createTestEntity());

        s1.getProperties().put("key1", "value1");
        s1.getProperties().put("key2", "value2");
        s1.getProperties().put("key3", "value3");

        dao.save(s1);

        s1 = dao.findOne(s1.getId());
        assertEquals("value1", s1.getProperties().get("key1"));
        assertEquals("value2", s1.getProperties().get("key2"));
        assertEquals("value3", s1.getProperties().get("key3"));

        s2 = dao.findOne(s2.getId());
        assertEquals(0, s2.getProperties().size());

        //test update
        s1.getProperties().remove("key1");
        s1.getProperties().put("key2", null);
        dao.save(s1);

        s1 = dao.findOne(s1.getId());

        assertEquals(1, s1.getProperties().size());
        assertEquals("value3", s1.getProperties().get("key3"));
    }

    /**
     * @param email user email.
     * @return
     */
    private User createUser(final String email) {
        final User u = new User();
        u.setActive(true);
        u.setCompany(sharedCompany);
        u.setFirstName("FirstName");
        u.setLastName("LastName");
        u.setEmail(email);
        return context.getBean(UserDao.class).save(u);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final RestSession session) {
        assertNotNull(session.getUser());
        assertEquals(user.getId(), session.getUser().getId());
        assertNotNull(session.getToken().getCreatedTime());
        assertNotNull(session.getToken().getToken());
        assertNotNull(session.getToken().getExpirationTime());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected RestSession createTestEntity() {
        final AuthToken token = new AuthToken("authToken_" + (lastId++));
        token.setExpirationTime(new Date(System.currentTimeMillis() + 100000000l));

        final RestSession s = new RestSession();
        s.setUser(user);
        s.setToken(token);
        return s;
    }
}
