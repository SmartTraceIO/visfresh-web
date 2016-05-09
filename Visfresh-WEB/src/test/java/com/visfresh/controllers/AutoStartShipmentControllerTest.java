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
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.User;
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
        final AlertProfile alerts = createAlertProfile(true);
        template.setAlertProfile(alerts);
        saveShipmentTemplateDirectly(template);

        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        createAutoStartShipment(template, locFrom, locTo);
        final AutoStartShipment ds = createAutoStartShipment(template, locFrom, locTo);

        final AutoStartShipmentDto dto = client.getAutoStartShipment(ds.getId());

        assertEquals(ds.getId(), dto.getId());
        assertEquals(ds.getPriority(), dto.getPriority());
        assertEquals(ds.getShippedFrom().get(0).getId(), locFrom.getId());
        assertEquals(ds.getShippedTo().get(0).getId(), locTo.getId());

        //assert equals template fields
        final ShipmentTemplate tpl = ds.getTemplate();
        assertEquals(tpl.getAlertProfile().getId(), dto.getAlertProfile());
        assertEquals(tpl.getAlertsNotificationSchedules().size(), dto.getAlertsNotificationSchedules().size());
        assertEquals(tpl.getAlertSuppressionMinutes(), dto.getAlertSuppressionMinutes());
        assertEquals(tpl.getArrivalNotificationSchedules().size(), dto.getArrivalNotificationSchedules().size());
        assertEquals(tpl.getArrivalNotificationWithinKm(), dto.getArrivalNotificationWithinKm());
        assertEquals(tpl.getCommentsForReceiver(), dto.getCommentsForReceiver());
        assertEquals(tpl.getName(), dto.getName());
        assertEquals(tpl.getNoAlertsAfterArrivalMinutes(), dto.getNoAlertsAfterArrivalMinutes());
        assertEquals(tpl.getShipmentDescription(), dto.getShipmentDescription());
        assertEquals(tpl.getShutDownAfterStartMinutes(), dto.getShutDownAfterStartMinutes());
        assertEquals(tpl.getShutdownDeviceAfterMinutes(), dto.getShutdownDeviceAfterMinutes());
    }
    @Test
    public void testDeleteAutoStartShipment() throws IOException, RestServiceException {
        final ShipmentTemplate t1 = createTemplate();
        final ShipmentTemplate t2 = createTemplate();
        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");

        final AutoStartShipment ds1 = createAutoStartShipment(t1, locFrom, locTo);
        final AutoStartShipment ds2 = createAutoStartShipment(t2, locFrom, locTo);

        client.deleteAutoStartShipment(ds2.getId());

        assertEquals(1, dao.getEntityCount(null));
        assertEquals(ds1.getId(), dao.findAll(null, null, null).get(0).getId());
    }

    @Test
    public void testSaveAutoStartShipment() throws IOException, RestServiceException {
        final User user = createUser2();
        final NotificationSchedule n1 = createNotificationSchedule(user, true);
        final NotificationSchedule n2 = createNotificationSchedule(user, true);

        final LocationProfile locTo = createLocation("From");
        final LocationProfile locFrom = createLocation("TO");
        final LocationProfile locInterim = createLocation("Interim");
        final int priority = 99;

        final AutoStartShipmentDto dto = new AutoStartShipmentDto();
        dto.setPriority(priority);
        dto.getStartLocations().add(locFrom.getId());
        dto.getEndLocations().add(locTo.getId());
        dto.getInterimStops().add(locInterim.getId());

        //add shipment template fields
        final AlertProfile alertProfile = createAlertProfile(true);
        final int alertSuppressionMinutes = 25;
        final Integer arrivalNotificationWithinKm = 15;
        final boolean excludeNotificationsIfNoAlerts = true;
        final Integer shutdownDeviceAfterMinutes = 99;
        final Integer noAlertsAfterArrivalMinutes = 43;
        final Integer shutDownAfterStartMinutes = 47;
        final String commentsForReceiver = "Any comments for receiver";
        final String name = "JUnit name";
        final String shipmentDescription = "JUnit shipment";
        final boolean addDateShipped = true;

        dto.setAlertSuppressionMinutes(alertSuppressionMinutes);
        dto.setAlertProfile(alertProfile.getId());
        dto.getAlertsNotificationSchedules().add(n1.getId());
        dto.setArrivalNotificationWithinKm(arrivalNotificationWithinKm);
        dto.getArrivalNotificationSchedules().add(n2.getId());
        dto.setExcludeNotificationsIfNoAlerts(excludeNotificationsIfNoAlerts);
        dto.setShutdownDeviceAfterMinutes(shutdownDeviceAfterMinutes);
        dto.setNoAlertsAfterArrivalMinutes(noAlertsAfterArrivalMinutes);
        dto.setShutDownAfterStartMinutes(shutDownAfterStartMinutes);
        dto.setCommentsForReceiver(commentsForReceiver);
        dto.setName(name);
        dto.setShipmentDescription(shipmentDescription);
        dto.setAddDateShipped(addDateShipped);

        final Long id = client.saveAutoStartShipment(dto);

        assertNotNull(id);

        final AutoStartShipment ds = dao.findOne(id);
        assertEquals(id, ds.getId());
        assertEquals(priority, ds.getPriority());
        assertEquals(locFrom.getId(), ds.getShippedFrom().get(0).getId());
        assertEquals(locTo.getId(), ds.getShippedTo().get(0).getId());
        assertEquals(locInterim.getId(), ds.getInterimStops().get(0).getId());

        //check shipment template fields.
        final ShipmentTemplate tpl = ds.getTemplate();

        assertEquals(alertSuppressionMinutes, tpl.getAlertSuppressionMinutes());
        assertEquals(alertProfile.getId(), tpl.getAlertProfile().getId());
        assertEquals(n1.getId(), tpl.getAlertsNotificationSchedules().get(0).getId());
        assertEquals(arrivalNotificationWithinKm, tpl.getArrivalNotificationWithinKm());
        assertEquals(n2.getId(), tpl.getArrivalNotificationSchedules().get(0).getId());
        assertEquals(excludeNotificationsIfNoAlerts, tpl.isExcludeNotificationsIfNoAlerts());
        assertEquals(shutdownDeviceAfterMinutes, tpl.getShutdownDeviceAfterMinutes());
        assertEquals(noAlertsAfterArrivalMinutes, tpl.getNoAlertsAfterArrivalMinutes());
        assertEquals(shutDownAfterStartMinutes, tpl.getShutDownAfterStartMinutes());
        assertEquals(commentsForReceiver, tpl.getCommentsForReceiver());
        assertEquals(name, tpl.getName());
        assertEquals(shipmentDescription, tpl.getShipmentDescription());
        assertEquals(addDateShipped, tpl.isAddDateShipped());
        assertEquals(true, tpl.isAutostart());
    }

    @Test
    public void testSaveNotificationScheudles() throws IOException, RestServiceException {
        final User user = createUser2();
        final NotificationSchedule n1 = createNotificationSchedule(user, true);
        final NotificationSchedule n2 = createNotificationSchedule(user, true);

        final AutoStartShipmentDto dto = new AutoStartShipmentDto();
        dto.setPriority(7);

        //add shipment template fields
        final AlertProfile alertProfile = createAlertProfile(true);
        final String name = "JUnit name";
        final String shipmentDescription = "JUnit shipment";
        final boolean addDateShipped = true;

        dto.setAlertProfile(alertProfile.getId());
        dto.getAlertsNotificationSchedules().add(n1.getId());
        dto.getAlertsNotificationSchedules().add(n2.getId());
        dto.getArrivalNotificationSchedules().add(n1.getId());
        dto.getArrivalNotificationSchedules().add(n2.getId());
        dto.setName(name);
        dto.setShipmentDescription(shipmentDescription);
        dto.setAddDateShipped(addDateShipped);

        final Long id = client.saveAutoStartShipment(dto);

        assertNotNull(id);

        final ShipmentTemplate tpl = dao.findOne(id).getTemplate();
        assertEquals(2, tpl.getAlertsNotificationSchedules().size());
        assertEquals(2, tpl.getArrivalNotificationSchedules().size());
    }

    @Test
    public void testUpdateAutoStartShipment() throws IOException, RestServiceException {
        final ShipmentTemplate template = createTemplate();

        final LocationProfile locFrom1 = createLocation("From1");
        final LocationProfile locTo1 = createLocation("TO1");
        final LocationProfile locFrom2 = createLocation("From2");
        final LocationProfile locTo2 = createLocation("TO2");

        final User user = createUser2();
        final NotificationSchedule n1 = createNotificationSchedule(user, true);
        final NotificationSchedule n2 = createNotificationSchedule(user, true);

        createAutoStartShipment(template, locTo1, locFrom1);
        AutoStartShipment ds = createAutoStartShipment(template, locTo1, locFrom1);

        final AutoStartShipmentDto dto = client.getAutoStartShipment(ds.getId());

        //change any fields in autostart
        dto.getStartLocations().clear();
        dto.getStartLocations().add(locFrom2.getId());

        dto.getEndLocations().clear();
        dto.getEndLocations().add(locTo2.getId());

        //change notification schedules
        dto.getAlertsNotificationSchedules().clear();
        dto.getAlertsNotificationSchedules().add(n1.getId());

        dto.getArrivalNotificationSchedules().clear();
        dto.getArrivalNotificationSchedules().add(n2.getId());
        //change one field in template
        final String commentsForReceiver = "Changed comments for receiver";
        dto.setCommentsForReceiver(commentsForReceiver);

        client.saveAutoStartShipment(dto);

        ds = dao.findOne(ds.getId());
        //check locations
        assertEquals(1, ds.getShippedFrom().size());
        assertEquals(locFrom2.getId(), ds.getShippedFrom().get(0).getId());
        assertEquals(1, ds.getShippedTo().size());
        assertEquals(locTo2.getId(), ds.getShippedTo().get(0).getId());

        //check template
        assertEquals(template.getId(), ds.getTemplate().getId());
        assertEquals(commentsForReceiver, ds.getTemplate().getCommentsForReceiver());

        //check notification schedules
        assertEquals(1, ds.getTemplate().getAlertsNotificationSchedules().size());
        assertEquals(n1.getId(), ds.getTemplate().getAlertsNotificationSchedules().get(0).getId());

        assertEquals(1, ds.getTemplate().getArrivalNotificationSchedules().size());
        assertEquals(n2.getId(), ds.getTemplate().getArrivalNotificationSchedules().get(0).getId());
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
        cfg.setPriority(77);
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
