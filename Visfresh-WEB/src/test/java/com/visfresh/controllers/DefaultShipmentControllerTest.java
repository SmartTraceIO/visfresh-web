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

import com.visfresh.controllers.restclient.DefaultShipmentRestClient;
import com.visfresh.dao.DefaultShipmentDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.DefaultShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.io.DefaultShipmentDto;
import com.visfresh.services.RestServiceException;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultShipmentControllerTest extends AbstractRestServiceTest {
    private DefaultShipmentRestClient client;
    private DefaultShipmentDao dao;

    /**
     * Default constructor.
     */
    public DefaultShipmentControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        //login to service.
        client = new DefaultShipmentRestClient(TimeZone.getDefault());
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());

        dao = context.getBean(DefaultShipmentDao.class);
    }

    @Test
    public void testGetDefaultShipment() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        createDefaultShipment(template, locFrom, locTo);
        final DefaultShipment ds = createDefaultShipment(template, locFrom, locTo);

        final DefaultShipmentDto dto = client.getDefaultShipment(ds.getId());

        assertEquals(ds.getId(), dto.getId());
        assertEquals(ds.getTemplate().getId(), dto.getTemplate());
        assertEquals(ds.getShippedFrom().get(0).getId(), locFrom.getId());
        assertEquals(ds.getShippedTo().get(0).getId(), locTo.getId());
    }
    @Test
    public void testDeleteDefaultShipment() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        final DefaultShipment ds1 = createDefaultShipment(template, locFrom, locTo);
        final DefaultShipment ds2 = createDefaultShipment(template, locFrom, locTo);

        client.deleteDefaultShipment(ds2.getId());

        assertEquals(1, dao.getEntityCount(null));
        assertEquals(ds1.getId(), dao.findAll(null, null, null).get(0).getId());
    }

    @Test
    public void testSaveDefaultShipment() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        final DefaultShipmentDto dto = new DefaultShipmentDto();
        dto.setTemplate(template.getId());
        dto.getStartLocations().add(locFrom.getId());
        dto.getEndLocations().add(locTo.getId());

        final Long id = client.saveDefaultShipment(dto);

        assertNotNull(id);

        final DefaultShipment ds = dao.findOne(id);
        assertEquals(id, ds.getId());
        assertEquals(dto.getTemplate(), ds.getTemplate().getId());
        assertEquals(locFrom.getId(), ds.getShippedFrom().get(0).getId());
        assertEquals(locTo.getId(), ds.getShippedTo().get(0).getId());
    }

    @Test
    public void testGetDefaultShipments() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        final DefaultShipment ds1 = createDefaultShipment(template, locFrom, locTo);
        final DefaultShipment ds2 = createDefaultShipment(template, locFrom, locTo);

        final List<DefaultShipmentDto> dss = client.getDefaultShipments(null, null);
        assertEquals(2, dss.size());
        assertEquals(ds1.getId(), dss.get(0).getId());
        assertEquals(ds2.getId(), dss.get(1).getId());
    }

    private DefaultShipment createDefaultShipment(final ShipmentTemplate template,
            final LocationProfile locFrom, final LocationProfile locTo) {
        final DefaultShipment cfg = new DefaultShipment();
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
