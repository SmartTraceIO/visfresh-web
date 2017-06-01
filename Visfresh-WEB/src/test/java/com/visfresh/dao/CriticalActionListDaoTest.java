/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.CriticalActionList;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CriticalActionListDaoTest extends BaseCrudTest<CriticalActionListDao, CriticalActionList, Long> {
    /**
     * Default constructor.
     */
    public CriticalActionListDaoTest() {
        super(CriticalActionListDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected CriticalActionList createTestEntity() {
        return createList(sharedCompany);
    }

    /**
     * @param c
     * @return
     */
    protected CriticalActionList createList(final Company c) {
        final CriticalActionList list = new CriticalActionList();
        list.setCompany(c);
        list.setName("JUnit action list");
        list.getActions().add("Run JUnit and check result");
        list.getActions().add("Run JUnit and check result again");
        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final CriticalActionList list) {
        assertEquals(sharedCompany.getId(), list.getCompany().getId());
        assertEquals("JUnit action list", list.getName());
        assertEquals("Run JUnit and check result", list.getActions().get(0));
        assertEquals("Run JUnit and check result again", list.getActions().get(1));
    }
    @Test
    public void testFindByCompany() {
        createAndSaveAlertProfile(sharedCompany);
        createAndSaveAlertProfile(sharedCompany);

        assertEquals(2, dao.findByCompany(sharedCompany, null, null, null).size());

        //test left company
        Company left = new Company();
        left.setName("name");
        left.setDescription("description");
        left = companyDao.save(left);

        assertEquals(0, dao.findByCompany(left, null, null, null).size());
    }
    /**
     * @param c
     */
    private CriticalActionList createAndSaveAlertProfile(final Company c) {
        return dao.save(createList(c));
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<CriticalActionList> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);
        assertCreateTestEntityOk(all.get(0));
    }
    @Override
    @Test
    public void testUpdate() {
        CriticalActionList list = new CriticalActionList();
        list.setCompany(sharedCompany);
        list.setName("JUnit-CriticalActionList");

        dao.save(list);

        list = dao.findOne(list.getId());
        list.setName("New name");
        list.getActions().add("A1");
        list.getActions().add("A2");

        dao.save(list);
        final CriticalActionList actual = dao.findOne(list.getId());

        //check updated
        assertEquals(list.getName(), actual.getName());
        assertEquals(list.getActions().size(), actual.getActions().size());
    }
}
