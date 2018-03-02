/**
 *
 */
package com.visfresh.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.controllers.SimulatorController;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.Simulator;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.io.SimulatorDto;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SimulatorDaoTest extends BaseDaoTest<SimulatorDao> {
    private User user;
    private Device device;

    /**
     * default constructor.
     */
    public SimulatorDaoTest() {
        super(SimulatorDao.class);
    }

    @Before
    public void setUp() {
        user = createUser("a@b.c");
        device = createDevice("09238479023873434");
    }

    @Test
    public void testFindSimulatorDto() {
        final User u1 = createUser("u1@b.c");
        final User u2 = createUser("u2@b.c");

        createSimulator(u1);
        createSimulator(u2);

        assertEquals(u1.getEmail(), dao.findSimulatorDto(u1).getUser());
        assertEquals(u2.getEmail(), dao.findSimulatorDto(u2).getUser());
    }
    @Test
    public void testSave() {
        final Long autostartTemplateId = 777l;

        final Simulator s = new Simulator();
        s.setSource(device);
        s.setUser(user);

        final Device d = createDevice(SimulatorController.generateImei(user.getId()));
        d.setAutostartTemplateId(autostartTemplateId);
        context.getBean(DeviceDao.class).save(d);

        s.setTarget(d);
        dao.save(s);

        final SimulatorDto dto = dao.findSimulatorDto(user);
        assertEquals(s.getSource().getImei(), dto.getSourceDevice());
        assertEquals(s.getTarget().getImei(), dto.getTargetDevice());
        assertEquals(s.getUser().getEmail(), dto.getUser());
        assertEquals(autostartTemplateId, dto.getAutoStart());
    }
    @Test
    public void testUpdate() {
        final User u1 = createUser("u1@b.c");
        final Device d = createDevice("2387982374098324");

        final Simulator s = createSimulator(u1);
        s.setSource(d);
        dao.save(s);

        final SimulatorDto dto = dao.findSimulatorDto(u1);
        assertEquals(d.getImei(), dto.getSourceDevice());
    }
    @Test
    public void testDelete() {
        final User u = createUser("u1@b.c");
        final Simulator s = createSimulator(u);

        //add shipment to simulator.
        final Shipment s1 = createShipment(s.getTarget());
        final Shipment s2 = createShipment(s.getTarget());

        //add alerts to simulator.
        createAlert(s1);
        createAlert(s1);
        createArrival(s2);
        createArrival(s2);
        createTrackerEvent(s2);
        createTrackerEvent(s2);

        dao.delete(u);

        assertEquals(0, context.getBean(AlertDao.class).findAll(null, null, null).size());
        assertEquals(0, context.getBean(ArrivalDao.class).findAll(null, null, null).size());
        assertEquals(0, context.getBean(TrackerEventDao.class).findAll(null, null, null).size());
        assertEquals(0, context.getBean(ShipmentDao.class).findAll(null, null, null).size());
        assertNull(context.getBean(DeviceDao.class).findByImei(s.getTarget().getImei()));
    }
    @Test
    public void testSimulatorStarted() {
        final User u = createUser("u1@b.c");
        createSimulator(u);

        //test start
        dao.setSimulatorStarted(u, true);

        SimulatorDto dto = dao.findSimulatorDto(u);
        assertTrue(dto.isStarted());

        //test stop
        dao.setSimulatorStarted(u, false);

        dto = dao.findSimulatorDto(u);
        assertFalse(dto.isStarted());
    }

    /**
     * @param s shipment.
     */
    private TrackerEvent createTrackerEvent(final Shipment s) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(3000);
        e.setDevice(s.getDevice());
        e.setTemperature(3.);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        return context.getBean(TrackerEventDao.class).save(e);
    }

    /**
     * @param s shipment.
     */
    private Alert createAlert(final Shipment s) {
        final Alert a = new Alert();
        a.setDate(new Date());
        a.setDevice(s.getDevice());
        a.setShipment(s);
        a.setType(AlertType.Battery);
        return context.getBean(AlertDao.class).save(a);
    }
    /**
     * @param s shipment.
     */
    private Arrival createArrival(final Shipment s) {
        final Arrival a = new Arrival();
        a.setDate(new Date());
        a.setDevice(s.getDevice());
        a.setShipment(s);
        a.setNumberOfMettersOfArrival(100);
        return context.getBean(ArrivalDao.class).save(a);
    }
    /**
     * @param target
     * @return
     */
    private Shipment createShipment(final Device target) {
        final Shipment s = new Shipment();
        s.setCompany(target.getCompanyId());
        s.setStatus(ShipmentStatus.Default);
        s.setDevice(target);
        s.setShipmentDescription("JUnit shipment");
        return context.getBean(ShipmentDao.class).save(s);
    }
    /**
     * @param imei device IMEI.
     * @return device.
     */
    private Device createDevice(final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setActive(true);
        d.setCompany(sharedCompany.getCompanyId());
        d.setDescription("JUnit Device");
        d.setName("JUnit");
        return context.getBean(DeviceDao.class).save(d);
    }
    /**
     * @param email user email
     * @return
     */
    private User createUser(final String email) {
        final User u = new User();
        u.setActive(true);
        u.setCompany(sharedCompany.getCompanyId());
        u.setEmail(email);
        u.setFirstName("JUnit");
        u.setLastName("User");
        return context.getBean(UserDao.class).save(u);
    }
    /**
     * @param user user.
     * @return simulator.
     */
    private Simulator createSimulator(final User user) {
        final Simulator s = new Simulator();
        s.setUser(user);
        s.setSource(device);
        s.setTarget(createDevice(SimulatorController.generateImei(user.getId())));
        context.getBean(SimulatorDao.class).save(s);
        return s;
    }
}
