/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.AutoStartShipmentRestClient;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.io.AutoStartShipmentDto;
import com.visfresh.services.RestServiceException;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentControllerTest extends AbstractRestServiceTest {
    private AutoStartShipmentRestClient client;
    private AutoStartShipmentDao dao;

    /**
     * Default constructor.
     */
    public AutoStartShipmentControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        //login to service.
        client = new AutoStartShipmentRestClient(TimeZone.getDefault());
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());

        dao = context.getBean(AutoStartShipmentDao.class);
    }

    @Test
    public void testGetAutoStartShipment() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        createAutoStartShipment(template, locFrom, locTo);
        final AutoStartShipment ds = createAutoStartShipment(template, locFrom, locTo);

        final AutoStartShipmentDto dto = client.getAutoStartShipment(ds.getId());

        assertEquals(ds.getId(), dto.getId());
        assertEquals(ds.getTemplate().getId(), dto.getTemplate());
        assertEquals(ds.getShippedFrom().get(0).getId(), locFrom.getId());
        assertEquals(ds.getShippedTo().get(0).getId(), locTo.getId());
    }
    @Test
    public void testDeleteAutoStartShipment() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        final AutoStartShipment ds1 = createAutoStartShipment(template, locFrom, locTo);
        final AutoStartShipment ds2 = createAutoStartShipment(template, locFrom, locTo);

        client.deleteAutoStartShipment(ds2.getId());

        assertEquals(1, dao.getEntityCount(null));
        assertEquals(ds1.getId(), dao.findAll(null, null, null).get(0).getId());
    }

    @Test
    public void testSaveAutoStartShipment() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        final AutoStartShipmentDto dto = new AutoStartShipmentDto();
        dto.setTemplate(template.getId());
        dto.getStartLocations().add(locFrom.getId());
        dto.getEndLocations().add(locTo.getId());

        final Long id = client.saveAutoStartShipment(dto);

        assertNotNull(id);

        final AutoStartShipment ds = dao.findOne(id);
        assertEquals(id, ds.getId());
        assertEquals(dto.getTemplate(), ds.getTemplate().getId());
        assertEquals(locFrom.getId(), ds.getShippedFrom().get(0).getId());
        assertEquals(locTo.getId(), ds.getShippedTo().get(0).getId());
    }

    @Test
    public void testGetAutoStartShipments() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        final AutoStartShipment ds1 = createAutoStartShipment(template, locFrom, locTo);
        final AutoStartShipment ds2 = createAutoStartShipment(template, locFrom, locTo);

        final List<AutoStartShipmentDto> dss = client.getAutoStartShipments(null, null);
        assertEquals(2, dss.size());
        assertEquals(ds1.getId(), dss.get(0).getId());
        assertEquals(ds2.getId(), dss.get(1).getId());
    }

    private AutoStartShipment createAutoStartShipment(final ShipmentTemplate template,
            final LocationProfile locFrom, final LocationProfile locTo) {
        final AutoStartShipment cfg = new AutoStartShipment();
        cfg.setCompany(getCompany());
        cfg.setTemplate(template);
        cfg.getShippedFrom().add(locFrom);
        cfg.getShippedTo().add(locTo);
        return dao.save(cfg);
    }
    /**
     * @return shipment template.
     */
    private ShipmentTemplate createTemplate() {
        final ShipmentTemplate tpl = new ShipmentTemplate();
        tpl.setCompany(getCompany());
        tpl.setName("JUnit template");
        return context.getBean(ShipmentTemplateDao.class).save(tpl);
    }
    /**
     * @param name location name.
     * @return location.
     */
    private LocationProfile createLocation(final String name) {
        final LocationProfile l = new LocationProfile();
        l.setName(name);
        l.setCompany(getCompany());
        l.setRadius(300);
        l.setAddress("adderss of " + name);
        return context.getBean(LocationProfileDao.class).save(l);
    }
}
