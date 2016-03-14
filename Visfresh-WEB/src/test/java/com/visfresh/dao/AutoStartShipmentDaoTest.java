/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.AutoStartShipmentConstants;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentTemplate;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentDaoTest extends
        BaseCrudTest<AutoStartShipmentDao, AutoStartShipment, Long> {
    private LocationProfile locFrom;
    private LocationProfile locTo;

    /**
     * @param clazz the DAO class.
     */
    public AutoStartShipmentDaoTest() {
        super(AutoStartShipmentDao.class);
    }

    @Before
    public void setUp() {
        locFrom = createLocation("From");
        locTo = createLocation("To");
    }

    @Test
    public void testUpdateLocations() {
        AutoStartShipment cfg = createTestEntity();
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
    @Test
    public void testTemplateDeleted() {
        final AutoStartShipment cfg = createTestEntity();
        dao.save(cfg);

        final ShipmentTemplateDao templateDao = getContext().getBean(ShipmentTemplateDao.class);
        assertNotNull(templateDao.findOne(cfg.getTemplate().getId()));
        dao.delete(cfg);
        assertNull(templateDao.findOne(cfg.getTemplate().getId()));
    }
    @Test
    public void testSortById() {
        final AutoStartShipment a1 = dao.save(createTestEntity());
        final AutoStartShipment a2 = dao.save(createTestEntity());
        final AutoStartShipment a3 = dao.save(createTestEntity());

        List<AutoStartShipment> all = dao.findAll(null, new Sorting(true, AutoStartShipmentConstants.ID), null);

        assertEquals(a1.getId(), all.get(0).getId());
        assertEquals(a2.getId(), all.get(1).getId());
        assertEquals(a3.getId(), all.get(2).getId());

        //descent
        all = dao.findAll(null, new Sorting(false, AutoStartShipmentConstants.ID), null);

        assertEquals(a3.getId(), all.get(0).getId());
        assertEquals(a2.getId(), all.get(1).getId());
        assertEquals(a1.getId(), all.get(2).getId());
    }
    @Test
    public void testSortByTemplateName() {

//        ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME,
    }
    @Test
    public void testSortByTemplateDescription() {
//        ShipmentTemplateConstants.SHIPMENT_DESCRIPTION,
    }
    @Test
    public void testStartByStartLocations() {

    }
    @Test
    public void testStartByEndLocations() {

    }
    @Test
    public void testSortByAlertProfileName() {

    }

    /**
     * @return shipment template.
     */
    private ShipmentTemplate createTemplate() {
        return createTemplate("JUnit template", null);
    }
    /**
     * @param name template name.
     * @param description template description.
     * @return shipment template.
     */
    private ShipmentTemplate createTemplate(final String name, final String description) {
        final ShipmentTemplate tpl = new ShipmentTemplate();
        tpl.setCompany(sharedCompany);
        tpl.setName(name);
        tpl.setShipmentDescription(description);
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
    protected void assertCreateTestEntityOk(final AutoStartShipment cfg) {
        assertEquals(sharedCompany.getId(), cfg.getCompany().getId());
        assertNotNull(cfg.getTemplate());
        assertEquals(10, cfg.getPriority());
        assertEquals(1, cfg.getShippedFrom().size());
        assertEquals(locFrom.getId(), cfg.getShippedFrom().get(0).getId());
        assertEquals(1, cfg.getShippedTo().size());
        assertEquals(locTo.getId(), cfg.getShippedTo().get(0).getId());
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.BaseCrudTest#createTestEntity()
     */
    @Override
    protected AutoStartShipment createTestEntity() {
        final ShipmentTemplate template = createTemplate();
        return createAutoStart(template);
    }
    /**
     * @param template
     * @return
     */
    private AutoStartShipment createAutoStart(final ShipmentTemplate template) {
        final AutoStartShipment cfg = new AutoStartShipment();
        cfg.setCompany(sharedCompany);
        cfg.setPriority(10);
        cfg.setTemplate(template);
        cfg.getShippedFrom().add(locFrom);
        cfg.getShippedTo().add(locTo);
        return cfg;
    }
}
