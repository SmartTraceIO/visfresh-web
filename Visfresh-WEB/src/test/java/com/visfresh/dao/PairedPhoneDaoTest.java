/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.visfresh.entities.PairedPhone;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PairedPhoneDaoTest extends BaseCrudTest<PairedPhoneDao, PairedPhone, PairedPhone, Long> {
    int lastId = 1;
    /**
     * Default constructor.
     */
    public PairedPhoneDaoTest() {
        super(PairedPhoneDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected PairedPhone createTestEntity() {
        final PairedPhone g = new PairedPhone();
        g.setActive(true);
        g.setImei("imei-" + (lastId)++);
        g.setCompany(sharedCompany.getCompanyId());
        g.setDescription("description");
        g.setBeaconId("beacon");
        return g;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final PairedPhone g) {
        assertNotNull(g.getId());
        assertEquals(true, g.isActive());
        assertTrue(g.getImei().startsWith("imei-"));
        assertEquals(sharedCompany.getCompanyId(), g.getCompany());
        assertEquals("description", g.getDescription());
        assertEquals("beacon", g.getBeaconId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<PairedPhone> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);
        assertCreateTestEntityOk(all.get(0));
    }
}
