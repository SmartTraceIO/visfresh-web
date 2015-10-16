/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.User;



/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserDaoTest extends BaseCrudTest<UserDao, User, String> {
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
        u.setLogin("asuvorov");
        return u;
    }
}
