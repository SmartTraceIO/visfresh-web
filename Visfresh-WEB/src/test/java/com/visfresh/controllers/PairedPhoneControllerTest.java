/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.constants.PairedPhoneConstants;
import com.visfresh.controllers.restclient.PairedPhoneRestClient;
import com.visfresh.dao.PairedPhoneDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.PairedPhone;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.SerializerUtils;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class PairedPhoneControllerTest extends AbstractRestServiceTest {

    private PairedPhoneDao dao;
    private PairedPhoneRestClient client;

    /**
     * Default constructor.
     */
    public PairedPhoneControllerTest() {
        super();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        dao = context.getBean(PairedPhoneDao.class);
        client = new PairedPhoneRestClient(SerializerUtils.UTС);
        client.setServiceUrl(getServiceUrl());
        client.setAuthToken(login());
    }

    @Test
    public void testGetPairedPhone() throws IOException, RestServiceException {
        final boolean active = true;
        final String beacon = "beacon";
        final String description = "description";
        final String imei = "imei";

        PairedPhone p = new PairedPhone();
        p.setActive(active);
        p.setBeaconId(beacon);
        p.setCompany(getCompanyId());
        p.setDescription(description);
        p.setImei(imei);

        dao.save(p);

        p = client.getPairedPhone(p.getId());

        assertNotNull(p.getId());
        assertEquals(active, p.isActive());
        assertEquals(beacon, p.getBeaconId());
        assertEquals(getCompanyId(), p.getCompany());
        assertEquals(description, p.getDescription());
        assertEquals(imei, p.getImei());
    }
    @Test
    public void testGetPairedPhones() throws IOException, RestServiceException {
        createPairedPhone("imei1", "b1");
        createPairedPhone("imei1", "b2");

        final PairedPhone p2 = createPairedPhone("imei2", "b2");
        final Company c2 = createCompany("C2");
        p2.setCompany(c2.getCompanyId());
        dao.save(p2);

        assertEquals(2, client.getPairedPhones(null, true, 1, 100).size());
        assertEquals(0, client.getPairedPhones(null, true, 2, 100).size());
        assertEquals(1, client.getPairedPhones(null, true, 1, 1).size());
    }
    @Test
    public void testGetPairedPhonesSortedByImei() throws IOException, RestServiceException {
        final PairedPhone p1 = createPairedPhone("imei2", "b1");
        final PairedPhone p2 = createPairedPhone("imei1", "b2");

        assertEquals(p2.getImei(), client.getPairedPhones(
                PairedPhoneConstants.IMEI, true, 1, 100).get(0).getImei());
        assertEquals(p1.getImei(), client.getPairedPhones(
                PairedPhoneConstants.IMEI, false, 1, 100).get(0).getImei());
    }
    @Test
    public void testGetPairedPhonesSortedByBeacon() throws IOException, RestServiceException {
        final PairedPhone p1 = createPairedPhone("imei1", "b2");
        final PairedPhone p2 = createPairedPhone("imei2", "b1");

        assertEquals(p2.getBeaconId(), client.getPairedPhones(
                PairedPhoneConstants.BEACON_ID, true, 1, 100).get(0).getBeaconId());
        assertEquals(p1.getBeaconId(), client.getPairedPhones(
                PairedPhoneConstants.BEACON_ID, false, 1, 100).get(0).getBeaconId());
    }
    @Test
    public void testGetPairedPhonesSortedDefault() throws IOException, RestServiceException {
        final PairedPhone p1 = createPairedPhone("imei2", "b2");
        final PairedPhone p2 = createPairedPhone("imei1", "b1");

        assertEquals(p1.getId(), client.getPairedPhones(
                null, true, 1, 100).get(0).getId());
        assertEquals(p2.getId(), client.getPairedPhones(
                PairedPhoneConstants.ID, false, 1, 100).get(0).getId());
    }
    @Test
    public void testGetPairedBeacons() throws IOException, RestServiceException {
        createPairedPhone("imei1", "b1");
        createPairedPhone("imei1", "b2");
        final PairedPhone p3 = createPairedPhone("imei1", "b3");
        p3.setActive(false);
        dao.save(p3);

        createPairedPhone("imei2", "b4");

        assertEquals(2, client.getPairedBeacons("imei1").size());
    }
    @Test
    public void testFindOneByPhoneAndBeacon() throws IOException, RestServiceException {
        createPairedPhone("imei1", "b1");
        final PairedPhone p = createPairedPhone("imei2", "b2");

        assertEquals(p.getId(), client.getPairedPhone("imei2", "b2").getId());
    }
    @Test
    public void testGetPairedPhoneWithoutParameters() {
        try {
            client.sendGetRequest(client.getPathWithToken("getPairedPhone"),
                    new HashMap<>()).getAsJsonObject();
            throw new AssertionFailedError("Exception should be thrown");
        } catch (IOException | RestServiceException e) {
            //correct behavior
        }
    }
    @Test
    public void testSavePairedPhone() throws IOException, RestServiceException {
        final boolean active = true;
        final String beacon = "beacon";
        final String description = "description";
        final String imei = "imei";

        PairedPhone p = new PairedPhone();
        p.setActive(active);
        p.setBeaconId(beacon);
        p.setCompany(getCompanyId());
        p.setDescription(description);
        p.setImei(imei);

        final Long id = client.savePairedPhone(p);
        p = dao.findOne(id);

        assertNotNull(p.getId());
        assertEquals(active, p.isActive());
        assertEquals(beacon, p.getBeaconId());
        assertEquals(getCompanyId(), p.getCompany());
        assertEquals(description, p.getDescription());
        assertEquals(imei, p.getImei());
    }
    @Test
    public void testUpdatePairedPhone() throws IOException, RestServiceException {
        final boolean active = true;
        final String beacon = "beacon";
        final String description = "description";
        final String imei = "imei";

        PairedPhone p = createPairedPhone("leftimei", "leftbeacon");

        p.setActive(active);
        p.setBeaconId(beacon);
        p.setCompany(getCompanyId());
        p.setDescription(description);
        p.setImei(imei);

        final Long id = client.savePairedPhone(p);
        p = dao.findOne(id);

        assertNotNull(p.getId());
        assertEquals(active, p.isActive());
        assertEquals(beacon, p.getBeaconId());
        assertEquals(getCompanyId(), p.getCompany());
        assertEquals(description, p.getDescription());
        assertEquals(imei, p.getImei());
    }
    @Test
    public void testDeletePairedPhone() throws IOException, RestServiceException {
        final PairedPhone p = createPairedPhone("leftimei", "leftbeacon");
        client.deletePairedPhone(p.getId());

        assertNull(dao.findOne(p.getId()));
    }
    /**
     * @param imei
     * @param beacon
     * @return
     */
    private PairedPhone createPairedPhone(final String imei, final String beacon) {
        final PairedPhone p = new PairedPhone();
        p.setActive(true);
        p.setBeaconId(beacon);
        p.setCompany(getCompanyId());
        p.setImei(imei);
        return dao.save(p);
    }
}
