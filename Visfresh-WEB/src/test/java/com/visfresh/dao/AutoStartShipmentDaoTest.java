/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.AutoStartShipmentConstants;
import com.visfresh.constants.ShipmentTemplateConstants;
import com.visfresh.entities.AlertProfile;
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
    private LocationProfile locInterim;

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
        locInterim = createLocation("Interim");
    }

    @Test
    public void testUpdateLocations() {
        AutoStartShipment cfg = createTestEntity();
        cfg.getShippedFrom().clear();
        cfg.getShippedTo().clear();
        cfg.getInterimStops().clear();

        final LocationProfile l1 = createLocation("l1");
        final LocationProfile l2 = createLocation("l2");
        final LocationProfile l3 = createLocation("l3");

        cfg.getShippedFrom().add(l1);
        cfg.getShippedTo().add(l2);
        cfg.getInterimStops().add(l3);

        dao.save(cfg);

        cfg.getShippedFrom().clear();
        cfg.getShippedFrom().add(l2);
        cfg.getShippedTo().clear();
        cfg.getShippedTo().add(l1);
        cfg.getInterimStops().clear();
        cfg.getInterimStops().add(l1);

        dao.save(cfg);

        cfg = dao.findOne(cfg.getId());

        assertEquals(1, cfg.getShippedFrom().size());
        assertEquals(1, cfg.getShippedTo().size());
        assertEquals(1, cfg.getInterimStops().size());

        assertEquals(l2.getId(), cfg.getShippedFrom().get(0).getId());
        assertEquals(l1.getId(), cfg.getShippedTo().get(0).getId());
        assertEquals(l1.getId(), cfg.getInterimStops().get(0).getId());
    }
    @Test
    public void testLocationsOrder() {
        AutoStartShipment cfg = createTestEntity();
        cfg.getShippedFrom().clear();
        cfg.getShippedTo().clear();
        cfg.getInterimStops().clear();

        final LocationProfile l3 = createLocation("l3");
        final LocationProfile l1 = createLocation("l1");
        final LocationProfile l2 = createLocation("l2");

        cfg.getShippedFrom().add(l1);
        cfg.getShippedFrom().add(l2);
        cfg.getShippedFrom().add(l3);

        cfg.getShippedTo().add(l2);
        cfg.getShippedTo().add(l1);
        cfg.getShippedTo().add(l3);

        cfg.getInterimStops().add(l3);
        cfg.getInterimStops().add(l2);
        cfg.getInterimStops().add(l1);

        dao.save(cfg);

        cfg = dao.findOne(cfg.getId());

        assertEquals(l1.getId(), cfg.getShippedFrom().get(0).getId());
        assertEquals(l2.getId(), cfg.getShippedTo().get(0).getId());
        assertEquals(l3.getId(), cfg.getInterimStops().get(0).getId());
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
        final ShipmentTemplate tpl1 = createTemplate("A", null);
        final ShipmentTemplate tpl2 = createTemplate("B", null);

        final AutoStartShipment a1 = dao.save(createAutoStart(tpl2));
        final AutoStartShipment a2 = dao.save(createAutoStart(tpl1));

        List<AutoStartShipment> all = dao.findAll(null,
                new Sorting(true, ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME), null);

        assertEquals(a2.getId(), all.get(0).getId());
        assertEquals(a1.getId(), all.get(1).getId());

        //descent
        all = dao.findAll(null,
                new Sorting(false, ShipmentTemplateConstants.SHIPMENT_TEMPLATE_NAME), null);

        assertEquals(a1.getId(), all.get(0).getId());
        assertEquals(a2.getId(), all.get(1).getId());
    }
    @Test
    public void testSortByTemplateDescription() {
        final ShipmentTemplate tpl1 = createTemplate(null, "A");
        final ShipmentTemplate tpl2 = createTemplate(null, "B");

        final AutoStartShipment a1 = dao.save(createAutoStart(tpl2));
        final AutoStartShipment a2 = dao.save(createAutoStart(tpl1));

        List<AutoStartShipment> all = dao.findAll(null,
                new Sorting(true, ShipmentTemplateConstants.SHIPMENT_DESCRIPTION), null);

        assertEquals(a2.getId(), all.get(0).getId());
        assertEquals(a1.getId(), all.get(1).getId());

        //descent
        all = dao.findAll(null,
                new Sorting(false, ShipmentTemplateConstants.SHIPMENT_DESCRIPTION), null);

        assertEquals(a1.getId(), all.get(0).getId());
        assertEquals(a2.getId(), all.get(1).getId());
    }
    @Test
    public void testStartByEndLocations() {
        final AutoStartShipment a1 = dao.save(createAutoStart(createTemplate()));
        final AutoStartShipment a2 = dao.save(createAutoStart(createTemplate()));

        //a1
        a1.getShippedFrom().add(createLocation("A"));
        a1.getShippedFrom().add(createLocation("A"));

        a1.getShippedTo().add(createLocation("E"));
        a1.getShippedTo().add(createLocation("B"));

        dao.save(a1);

        //a2
        a2.getShippedFrom().add(createLocation("A"));
        a2.getShippedFrom().add(createLocation("A"));

        a2.getShippedTo().add(createLocation("C"));
        a2.getShippedTo().add(createLocation("A"));

        dao.save(a2);

        //run test
        List<AutoStartShipment> all = dao.findAll(null,
                new Sorting(true, AutoStartShipmentConstants.END_LOCATIONS), null);

        assertEquals(a2.getId(), all.get(0).getId());
        assertEquals(a1.getId(), all.get(1).getId());

        //descent
        all = dao.findAll(null,
                new Sorting(false, AutoStartShipmentConstants.END_LOCATIONS), null);

        assertEquals(a1.getId(), all.get(0).getId());
        assertEquals(a2.getId(), all.get(1).getId());
    }
    @Test
    public void testSortByStartLocations() {
        final AutoStartShipment a1 = dao.save(createAutoStart(createTemplate()));
        final AutoStartShipment a2 = dao.save(createAutoStart(createTemplate()));

        //a1
        a1.getShippedFrom().add(createLocation("E"));
        a1.getShippedFrom().add(createLocation("B"));

        dao.save(a1);

        //a2
        a2.getShippedFrom().add(createLocation("C"));
        a2.getShippedFrom().add(createLocation("A"));

        dao.save(a2);

        //run test
        List<AutoStartShipment> all = dao.findAll(null,
                new Sorting(true, AutoStartShipmentConstants.START_LOCATIONS), null);

        assertEquals(a2.getId(), all.get(0).getId());
        assertEquals(a1.getId(), all.get(1).getId());

        //descent
        all = dao.findAll(null,
                new Sorting(false, AutoStartShipmentConstants.START_LOCATIONS), null);

        assertEquals(a1.getId(), all.get(0).getId());
        assertEquals(a2.getId(), all.get(1).getId());
    }
    @Test
    public void testLocationDuplicates() {
        final ShipmentTemplate tpl = createTemplate();
        final AutoStartShipment auto1 = dao.save(createAutoStart(tpl));
        final AutoStartShipment auto2 = dao.save(createAutoStart(tpl));

        final LocationProfile a = createLocation("A");
        final LocationProfile b = createLocation("B");
        final LocationProfile c = createLocation("C");
        final LocationProfile e = createLocation("E");

        //a1
        auto1.getShippedFrom().clear();
        auto1.getShippedTo().clear();
        auto1.getShippedFrom().add(c);
        auto1.getShippedTo().add(a);
        auto1.getShippedTo().add(b);

        dao.save(auto1);

        //a2
        auto2.getShippedFrom().clear();
        auto2.getShippedTo().clear();
        auto2.getShippedFrom().add(e);
        auto2.getShippedTo().add(a);
        auto2.getShippedTo().add(b);

        dao.save(auto2);

        //run test
        final List<AutoStartShipment> all = dao.findAll(null,
                new Sorting(true, AutoStartShipmentConstants.START_LOCATIONS), null);

        //just check correct loaded
        assertEquals(auto1.getId(), all.get(0).getId());
        assertEquals(auto2.getId(), all.get(1).getId());
    }
    @Test
    public void testSortByAlertProfileName() {
        final AlertProfile ap1 = createAlertProfile("A");
        final AlertProfile ap2 = createAlertProfile("B");

        final ShipmentTemplate tpl1 = createTemplate();
        tpl1.setAlertProfile(ap1);
        final ShipmentTemplate tpl2 = createTemplate();
        tpl2.setAlertProfile(ap2);

        getContext().getBean(ShipmentTemplateDao.class).save(tpl1);
        getContext().getBean(ShipmentTemplateDao.class).save(tpl2);

        final AutoStartShipment a1 = dao.save(createAutoStart(tpl2));
        final AutoStartShipment a2 = dao.save(createAutoStart(tpl1));

        List<AutoStartShipment> all = dao.findAll(null,
                new Sorting(true, AutoStartShipmentConstants.ALERT_PROFILE_NAME), null);

        assertEquals(a2.getId(), all.get(0).getId());
        assertEquals(a1.getId(), all.get(1).getId());

        //descent
        all = dao.findAll(null,
                new Sorting(false, AutoStartShipmentConstants.ALERT_PROFILE_NAME), null);

        assertEquals(a1.getId(), all.get(0).getId());
        assertEquals(a2.getId(), all.get(1).getId());
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
    /**
     * @param name alert profile name.
     * @return alert profile.
     */
    private AlertProfile createAlertProfile(final String name) {
        final AlertProfile a = new AlertProfile();
        a.setCompany(sharedCompany);
        a.setName(name);
        return getContext().getBean(AlertProfileDao.class).save(a);
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
        assertEquals(1, cfg.getInterimStops().size());
        assertEquals(locInterim.getId(), cfg.getInterimStops().get(0).getId());
        assertTrue(cfg.isStartOnLeaveLocation());
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
        cfg.setStartOnLeaveLocation(true);
        cfg.setPriority(10);
        cfg.setTemplate(template);
        cfg.getShippedFrom().add(locFrom);
        cfg.getShippedTo().add(locTo);
        cfg.getInterimStops().add(locInterim);
        return cfg;
    }
}
