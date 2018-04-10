/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.visfresh.constants.PairedPhoneConstants;
import com.visfresh.entities.Company;
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
        return createPairedPhone("imei-" + (lastId)++);
    }

    /**
     * @param imei phone IMEI
     * @return
     */
    private PairedPhone createAndSavePairedPhone(final String imei, final String beacon) {
        final PairedPhone p = createPairedPhone(imei);
        p.setBeaconId(beacon);
        return dao.save(p);
    }
    /**
     * @param imei
     * @return
     */
    private PairedPhone createPairedPhone(final String imei) {
        final PairedPhone g = new PairedPhone();
        g.setActive(true);
        g.setImei(imei);
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

    @Test
    public void testGetEntityCount() {
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        final PairedPhone p1 = createPairedPhone("imei1");
        p1.setCompany(c1.getCompanyId());
        dao.save(p1);

        final PairedPhone p2 = createPairedPhone("imei2");
        p2.setCompany(c2.getCompanyId());
        dao.save(p2);

        final Filter filter = new Filter();
        assertEquals(1, dao.getEntityCount(c1.getCompanyId(), filter));
        assertEquals(1, dao.getEntityCount(c1.getCompanyId(), null));

        filter.addFilter(PairedPhoneConstants.IMEI, "abrakadabra");
        assertEquals(0, dao.getEntityCount(c1.getCompanyId(), filter));
    }
    @Test
    public void testFindByCompany() {
        final Company c1 = createCompany("C1");
        final Company c2 = createCompany("C2");

        final PairedPhone p1 = createPairedPhone("imei1");
        p1.setCompany(c1.getCompanyId());
        dao.save(p1);

        final PairedPhone p2 = createPairedPhone("imei2");
        p2.setCompany(c2.getCompanyId());
        dao.save(p2);

        final Filter filter = new Filter();
        assertEquals(1, dao.findByCompany(c1.getCompanyId(), null, null, filter).size());
        assertEquals(1, dao.findByCompany(c1.getCompanyId(), null, null, null).size());

        filter.addFilter(PairedPhoneConstants.IMEI, "abrakadabra");
        assertEquals(0, dao.findByCompany(c1.getCompanyId(), null, null, filter).size());
    }
    @Test
    public void testGetPairedBeacons() {
        createAndSavePairedPhone("imei1", "b1");
        createAndSavePairedPhone("imei1", "b2");
        createAndSavePairedPhone("imei2", "b3");

        assertEquals(2, dao.getPairedBeacons("imei1").size());
        assertEquals(1, dao.getPairedBeacons("imei2").size());
    }
}
