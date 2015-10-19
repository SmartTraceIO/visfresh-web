/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserDaoTest extends BaseCrudTest<UserDao, User, String> {
    private int ids;

    /**
     * Default constructor.
     */
    public UserDaoTest() {
        super(UserDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected User createTestEntity() {
        final User u = new User();
        u.setFullName("Alexander Suvorov");
        u.setCompany(sharedCompany);
        u.setLogin("asuvorov-" + (++ids));
        u.setPassword("abrakadabra");
        return u;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final User user) {
        assertEquals("asuvorov-1", user.getLogin());
        assertEquals("Alexander Suvorov", user.getFullName());
        assertEquals("abrakadabra", user.getPassword());

        //test company
        final Company c = user.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<User> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);

        //check first entity
        final User user = all.get(0);

        assertNotNull(user.getLogin());
        assertEquals("Alexander Suvorov", user.getFullName());
        assertEquals("abrakadabra", user.getPassword());

        //test company
        final Company c = user.getCompany();
        assertEquals(sharedCompany.getId(), c.getId());
        assertEquals(sharedCompany.getName(), c.getName());
        assertEquals(sharedCompany.getDescription(), c.getDescription());
    }
}
