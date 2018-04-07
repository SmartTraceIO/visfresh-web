/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.visfresh.entities.BeaconGateway;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BeaconGatewayTest extends BaseCrudTest<BeaconGatewayDao, BeaconGateway, BeaconGateway, Long> {
    /**
     * Default constructor.
     */
    public BeaconGatewayTest() {
        super(BeaconGatewayDao.class);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected BeaconGateway createTestEntity() {
        final BeaconGateway g = new BeaconGateway();
        g.setActive(true);
        g.setBeacon("beacon");
        g.setCompany(sharedCompany.getCompanyId());
        g.setDescription("description");
        g.setGateway("gateway");
        return g;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final BeaconGateway g) {
        assertNotNull(g.getId());
        assertEquals(true, g.isActive());
        assertEquals("beacon", g.getBeacon());
        assertEquals(sharedCompany.getCompanyId(), g.getCompany());
        assertEquals("description", g.getDescription());
        assertEquals("gateway", g.getGateway());
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertTestGetAllOk(int, java.util.List)
     */
    @Override
    protected void assertTestGetAllOk(final int numberOfCreatedEntities,
            final List<BeaconGateway> all) {
        super.assertTestGetAllOk(numberOfCreatedEntities, all);
        assertCreateTestEntityOk(all.get(0));
    }
}
