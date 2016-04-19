/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.AssertionFailedError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.restclient.SimulatorRestClient;
import com.visfresh.dao.AutoStartShipmentDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.SimulatorDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Device;
import com.visfresh.entities.Role;
import com.visfresh.entities.Simulator;
import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;
import com.visfresh.io.StartSimulatorRequest;
import com.visfresh.mock.MockSimulatorService;
import com.visfresh.services.RestServiceException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SimulatorControllerTest extends AbstractRestServiceTest {
    private MockSimulatorService service;
    private SimulatorRestClient client;
    private SimulatorDao dao;
    private User user;
    private Device device;
    private DateFormat serverDateFormat;

    /**
     * Default constructor.
     */
    public SimulatorControllerTest() {
        super();
    }

    @Before
    public void setUp() {
        service = context.getBean(MockSimulatorService.class);

        //create user
        user = context.getBean(UserDao.class).findAll(null, null, null).get(0);
        user.getRoles().add(Role.SmartTraceAdmin);
        context.getBean(UserDao.class).save(user);

        final String authToken = login();

        //create client
        final SimulatorRestClient c = new SimulatorRestClient(user);
        c.setAuthToken(authToken);
        c.setServiceUrl(getServiceUrl());
        this.client = c;

        //create DAO
        dao = context.getBean(SimulatorDao.class);

        //create device
        device = createDevice("098234790799284", true);

        //create GMT format
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:'00'", user.getLanguage().getLocale());
        fmt.setTimeZone(TimeZone.getDefault());
        serverDateFormat = fmt;
    }

    @Test
    public void testSaveSimulator() throws IOException, RestServiceException {
        final User u = createUser2();

        AutoStartShipment auto = new AutoStartShipment();
        auto.setTemplate(createShipmentTemplate(true));
        auto.setCompany(auto.getTemplate().getCompany());
        auto = context.getBean(AutoStartShipmentDao.class).save(auto);

        final SimulatorDto dto = new SimulatorDto();
        dto.setSourceDevice(device.getImei());
        dto.setTargetDevice(null);
        dto.setUser(u.getEmail());
        dto.setAutoStart(auto.getId());

        final String virtualDevice = client.saveSimulator(dto);
        assertNotNull(context.getBean(DeviceDao.class).findByImei(virtualDevice));

        final SimulatorDto sim = dao.findSimulatorDto(u);
        assertEquals(dto.getSourceDevice(), sim.getSourceDevice());
        assertEquals(u.getEmail(), sim.getUser());
        assertEquals(virtualDevice, sim.getTargetDevice());
        assertEquals(auto.getId(), sim.getAutoStart());
    }
    @Test
    public void testUpdateSimulator() throws IOException, RestServiceException {
        final User u = createUser2();

        final Simulator sim = createSimulator(u);
        final Device d = createDevice("187623887979876", true);

        AutoStartShipment auto = new AutoStartShipment();
        auto.setTemplate(createShipmentTemplate(true));
        auto.setCompany(auto.getTemplate().getCompany());
        auto = context.getBean(AutoStartShipmentDao.class).save(auto);

        //do update
        SimulatorDto dto = new SimulatorDto();
        dto.setSourceDevice(d.getImei());
        dto.setTargetDevice(sim.getTarget().getImei());
        dto.setUser(u.getEmail());
        dto.setAutoStart(auto.getId());

        final String virtualDevice = client.saveSimulator(dto);
        dto = dao.findSimulatorDto(u);

        //check virtual device not changed
        assertEquals(sim.getTarget().getImei(), virtualDevice);
        assertEquals(sim.getTarget().getImei(), dto.getTargetDevice());

        //check source device is changed
        assertEquals(dto.getSourceDevice(), d.getImei());
        assertEquals(auto.getId(),
                context.getBean(DeviceDao.class).findOne(dto.getTargetDevice()).getAutostartTemplateId());
    }
    @Test
    public void testDeleteSimulator() throws IOException, RestServiceException {
        final User u = createUser2();

        //do delete
        createSimulator(u);

        client.deleteSimulator(u);
        assertNull(dao.findSimulatorDto(u));
    }
    @Test
    public void testStartSimulator() throws IOException, RestServiceException {
        final User u = createUser2();

        createSimulator(u);
        final Date startDate = new Date(System.currentTimeMillis() - 10000000l);
        final Date endDate = new Date();

        client.startSimulator(u, startDate, endDate, 20);

        dao.setSimulatorStarted(u, true);
        try {
            client.startSimulator(u, startDate, endDate, 20);
            throw new AssertionFailedError("Exception should be thrown because already started");
        } catch (final Exception e) {
            //ok
        }

        assertEquals(1, service.getRequests().size());

        final StartSimulatorRequest r = service.getRequests().get(0);
        assertEquals(u.getEmail(), r.getUser());
        assertEquals(serverDateFormat.format(startDate), r.getStartDate());
        assertEquals(serverDateFormat.format(endDate), r.getEndDate());
    }
    @Test
    public void testStartSimulatorForCurrentUser() throws IOException, RestServiceException {
        createSimulator(user);
        client.startSimulator(null, new Date(), new Date(), 20);

        dao.setSimulatorStarted(user, true);
        try {
            client.startSimulator(null, new Date(), new Date(), 20);
            throw new AssertionFailedError("Exception should be thrown because already started");
        } catch (final Exception e) {
            //ok
        }

        assertEquals(1, service.getRequests().size());
    }
    @Test
    public void testStartSimulatorWithoutDates() throws IOException, RestServiceException {
        final User u = createUser2();

        createSimulator(u);
        client.startSimulator(u, null, null, 20);
        assertEquals(1, service.getRequests().size());
    }
    @Test
    public void testStopSimulator() throws IOException, RestServiceException {
        final User u = createUser2();

        createSimulator(u);
        dao.setSimulatorStarted(u, true);

        client.stopSimulator(u);
        assertEquals(0, service.getRequests().size());
    }
    @Test
    public void testGetSimulator() throws IOException, RestServiceException {
        final User u = createUser2();

        createSimulator(u);

        final SimulatorDto sim = client.getSimulator(u);
        assertNotNull(sim);
    }
    @Test
    public void testGetSimulatorDefaultUser() throws IOException, RestServiceException {
        createSimulator(user);

        final SimulatorDto sim = client.getSimulator(null);
        assertNotNull(sim);
    }
    @Test
    public void testStopSimulatorDefaultUser() throws IOException, RestServiceException {
        createSimulator(user);
        dao.setSimulatorStarted(user, true);

        client.stopSimulator(null);
        assertEquals(0, service.getRequests().size());
    }

    /**
     * @param u
     * @return
     */
    private Simulator createSimulator(final User u) {
        final Simulator sim = new Simulator();
        sim.setSource(device);
        sim.setUser(u);
        sim.setTarget(createDevice(SimulatorController.generateImei(u.getId()), true));
        dao.save(sim);
        return sim;
    }

    @After
    public void tearDown() {
        service.clear();
    }
}
