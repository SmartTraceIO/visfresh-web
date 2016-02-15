/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.DefaultShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentTemplate;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultShipmentDaoTest extends
        BaseCrudTest<DefaultShipmentDao, DefaultShipment, Long> {
    private LocationProfile locFrom;
    private LocationProfile locTo;
    private ShipmentTemplate template;

    /**
     * @param clazz the DAO class.
     */
    public DefaultShipmentDaoTest() {
        super(DefaultShipmentDao.class);
    }

    @Before
    public void setUp() {
        locFrom = createLocation("From");
        locTo = createLocation("To");
        template = createTemplate();
    }

    @Test
    public void testUpdateLocations() {
        DefaultShipment cfg = createTestEntity();
        cfg.getShippedFrom().clear();
        cfg.getShippedTo().clear();

        final LocationProfile l1 = createLocation("l1");
        final LocationProfile l2 = createLocation("l2");

        cfg.getShippedFrom().add(l1);
        cfg.getShippedTo().add(l2);

        dao.save(cfg);

        cfg.getShippedFrom().clear();
        cfg.getShippedFrom().add(l2);
        cfg.getShippedTo().clear();
        cfg.getShippedTo().add(l1);

        dao.save(cfg);

        cfg = dao.findOne(cfg.getId());

        assertEquals(1, cfg.getShippedFrom().size());
        assertEquals(1, cfg.getShippedTo().size());

        assertEquals(l2.getId(), cfg.getShippedFrom().get(0).getId());
        assertEquals(l1.getId(), cfg.getShippedTo().get(0).getId());
    }

    /**
     * @return shipment template.
     */
    private ShipmentTemplate createTemplate() {
        final ShipmentTemplate tpl = new ShipmentTemplate();
        tpl.setCompany(sharedCompany);
        tpl.setName("JUnit template");
        return getContext().getBean(ShipmentTemplateDao.class).save(tpl);
    }
    /**
     * @param name location name.
     * @return location.
     */
    private LocationProfile createLocation(final String name) {
        final LocationProfile l = new LocationProfile();
        l.setName(name);
        l.setCompany(sharedCompany);
        l.setRadius(300);
        l.setAddress("adderss of " + name);
        return getContext().getBean(LocationProfileDao.class).save(l);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#assertCreateTestEntityOk(com.visfresh.entities.EntityWithId)
     */
    @Override
    protected void assertCreateTestEntityOk(final DefaultShipment cfg) {
        assertEquals(sharedCompany.getId(), cfg.getCompany().getId());
        assertEquals(template.getId(), cfg.getTemplate().getId());
        assertEquals(1, cfg.getShippedFrom().size());
        assertEquals(locFrom.getId(), cfg.getShippedFrom().get(0).getId());
        assertEquals(1, cfg.getShippedTo().size());
        assertEquals(locTo.getId(), cfg.getShippedTo().get(0).getId());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected DefaultShipment createTestEntity() {
        final DefaultShipment cfg = new DefaultShipment();
        cfg.setCompany(sharedCompany);
        cfg.setTemplate(template);
        cfg.getShippedFrom().add(locFrom);
        cfg.getShippedTo().add(locTo);
        return cfg;
    }
}
