/**
 *
 */
package com.visfresh.impl.singleshipment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.ArrivalDao;
import com.visfresh.dao.InterimStopDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NoteDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Note;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.impl.services.NotificationServiceImpl;
import com.visfresh.io.shipment.ArrivalBean;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.NoteBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MainShipmentDataBuilderTest extends BaseBuilderTest {
    private InterimStopDao inStopDao;
    private LocationProfileDao dao;
    private AlternativeLocationsDao altLocDao;
    private NoteDao noteDao;

    /**
     * Default constructor.
     */
    public MainShipmentDataBuilderTest() {
        super();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        dao = context.getBean(LocationProfileDao.class);
        altLocDao = context.getBean(AlternativeLocationsDao.class);
        inStopDao = context.getBean(InterimStopDao.class);
        noteDao = context.getBean(NoteDao.class);
    }

    @Test
    public void testSiblings() {
        final Shipment s1 = shipmentDao.save(createDefaultNotSavedShipment(device));
        final Shipment s2 = shipmentDao.save(createDefaultNotSavedShipment(device));
        final Shipment s3 = shipmentDao.save(createDefaultNotSavedShipment(device));

        setAsSiblings(s1, s2, s3);

        final SingleShipmentBuildContext c = new SingleShipmentBuildContext();

        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s1.getId(), s1.getCompany().getId());
        b.fetchData();
        b.build(c);

        //test result
        assertEquals(s1.getId().longValue(), c.getData().getBean().getShipmentId());
        assertEquals(2, c.getData().getSiblings().size());
        assertEquals(2, c.getData().getBean().getSiblings().size());
        assertEquals(2, c.getData().getSiblings().get(0).getSiblings().size());
        assertEquals(2, c.getData().getSiblings().get(1).getSiblings().size());
    }
    @Test
    public void testBuild() {
        //shipment session fields
        final boolean alertsSuppressed = true;
        final Date alertsSuppressedTime = new Date(System.currentTimeMillis() - 1000000000l);
        final boolean arrivalReportSent = true;

        //shipment fields
        final Integer alertSuppressionMinutes = 15;
        final Integer arrivalNotificationWithinKm = 35;
        final Date arrivalTime = new Date(System.currentTimeMillis() - 1098098l);
        final String assetNum = "AssertNumber";
        final String assetType = "AssertType";
        final String commentsForReceiver = "Comments for receiver";
        final boolean excludeNotificationsIfNoAlerts = true;
        final ShipmentStatus status = ShipmentStatus.InProgress;
        final Integer noAlertsAfterArrivalMinutes = 450;
        final Integer noAlertsAfterStartMinutes = 320;
        final String palletId = "PaletId";
        final boolean sendArrivalReport = true;
        final boolean sendArrivalReportOnlyIfAlerts = true;
        final String shipmentDescription = "Shipment description";
        final Integer shutDownAfterStartMinutes = 23;
        final Integer shutdownDeviceAfterMinutes = 2345;
        final Date startTime = new Date(System.currentTimeMillis() - 29380470934l);
        final Date eta = new Date(System.currentTimeMillis() - 1000000l);

        //set fields to shipment
        final Shipment s = createDefaultNotSavedShipment(device);
        s.setAlertSuppressionMinutes(alertSuppressionMinutes);
        s.setArrivalNotificationWithinKm(arrivalNotificationWithinKm);
        s.setArrivalDate(arrivalTime);
        s.setAssetNum(assetNum);
        s.setAssetType(assetType);
        s.setCommentsForReceiver(commentsForReceiver);
        s.setExcludeNotificationsIfNoAlerts(excludeNotificationsIfNoAlerts);
        s.setStatus(status);
        s.setNoAlertsAfterArrivalMinutes(noAlertsAfterArrivalMinutes);
        s.setNoAlertsAfterStartMinutes(noAlertsAfterStartMinutes);
        s.setPalletId(palletId);
        s.setSendArrivalReport(sendArrivalReport);
        s.setSendArrivalReportOnlyIfAlerts(sendArrivalReportOnlyIfAlerts);
        s.setShipmentDescription(shipmentDescription);
        s.setShutDownAfterStartMinutes(shutDownAfterStartMinutes);
        s.setShutdownDeviceAfterMinutes(shutdownDeviceAfterMinutes);
        s.setShipmentDate(startTime);
        s.setEta(eta);
        shipmentDao.save(s);

        //set up shipment session
        final ShipmentSession ss = new ShipmentSession(s.getId());
        ss.setAlertsSuppressed(alertsSuppressed);
        ss.setAlertsSuppressionDate(alertsSuppressedTime);
        ss.setArrivalProcessed(arrivalReportSent);
        NotificationServiceImpl.setArrivalReportSent(ss, new Date());
        context.getBean(ShipmentSessionDao.class).saveSession(ss);

        final SingleShipmentBuildContext c = new SingleShipmentBuildContext();

        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(c);
        final SingleShipmentBean bean = c.getData().getBean();

        assertEquals(alertsSuppressed, bean.isAlertsSuppressed());
        assertTrue(Math.abs(alertsSuppressedTime.getTime() - bean.getAlertsSuppressionTime().getTime()) < 1000l);
        assertEquals(arrivalReportSent, bean.isArrivalReportSent());
        assertEquals(alertSuppressionMinutes, bean.getAlertSuppressionMinutes());
        assertEquals(arrivalNotificationWithinKm, bean.getArrivalNotificationWithinKm());
        assertTrue(Math.abs(arrivalTime.getTime() - bean.getArrivalTime().getTime()) < 1000l);
        assertEquals(assetNum, bean.getAssetNum());
        assertEquals(assetType, bean.getAssetType());
        assertEquals(commentsForReceiver, bean.getCommentsForReceiver());
        assertEquals(device.getCompany().getId(), bean.getCompanyId());
        assertEquals(device.getImei(), bean.getDevice());
        assertEquals(device.getColor().name(), bean.getDeviceColor());
        assertEquals(device.getName(), bean.getDeviceName());
        assertEquals(excludeNotificationsIfNoAlerts, bean.isExcludeNotificationsIfNoAlerts());
        assertEquals(status, bean.getStatus());
        assertEquals(s.getTripCount(), bean.getTripCount());
        assertTrue(bean.isLatestShipment());
        assertEquals(noAlertsAfterArrivalMinutes, bean.getNoAlertsAfterArrivalMinutes());
        assertEquals(noAlertsAfterStartMinutes, bean.getNoAlertsAfterStartMinutes());
        assertEquals(palletId, bean.getPalletId());
        assertEquals(sendArrivalReport, bean.isSendArrivalReport());
        assertEquals(sendArrivalReportOnlyIfAlerts, bean.isSendArrivalReportOnlyIfAlerts());
        assertEquals(shipmentDescription, bean.getShipmentDescription());
        assertEquals(s.getId().longValue(), bean.getShipmentId());
        assertEquals("Manual", bean.getShipmentType());
        assertEquals(shutDownAfterStartMinutes, bean.getShutDownAfterStartMinutes());
        assertEquals(shutdownDeviceAfterMinutes, bean.getShutdownDeviceAfterMinutes());
        assertTrue(Math.abs(startTime.getTime() - bean.getStartTime().getTime()) < 1000l);
        assertTrue(Math.abs(eta.getTime() - bean.getEta().getTime()) < 1000l);
        assertEquals(100, bean.getPercentageComplete());
    }
    @Test
    public void testInterimStops() {
        final Shipment s = createShipment();
        final Shipment sib1 = createShipment();
        final Shipment sib2 = createShipment();

        setAsSiblings(s, sib1, sib2);

        final LocationProfile loc1 = createLocation("Loc1");
        final LocationProfile loc2 = createLocation("Loc1");

        inStopDao.save(s, createInterimStop(loc1));
        inStopDao.save(s, createInterimStop(loc2));
        inStopDao.save(sib1, createInterimStop(loc2));

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(2, data.getBean().getInterimStops().size());

        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);
        assertEquals(1, sibBean1.getInterimStops().size());
        assertEquals(loc2.getId(), sibBean1.getInterimStops().get(0).getLocation().getId());

        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertEquals(0, sibBean2.getInterimStops().size());
    }
    @Test
    public void testInterimStopsStop() {
        final Shipment s = createShipment();

        setAsSiblings(s);

        final LocationProfile loc = createLocation("Loc");
        final InterimStop stp = createInterimStop(loc);
        inStopDao.save(s, stp);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(1, data.getBean().getInterimStops().size());

        //check equals stops
        final InterimStopBean stpBean = data.getBean().getInterimStops().get(0);
        assertEquals(stp.getId(), stpBean.getId());
        assertEquals(stp.getTime(), stpBean.getTime());
        assertTrue(Math.abs(stpBean.getStopDate().getTime() - stp.getDate().getTime()) < 1000l);
        assertEqualsLocations(loc, stpBean.getLocation());
    }
    @Test
    public void testLocationFrom() {
        final String address = "Any address";
        final String companyName = "Any company";
        final boolean interim = true;
        final boolean start = false;
        final boolean stop = true;
        final String name = "Location name";
        final String notes = "Location notes";
        final int radius = 1500;

        final Shipment s = createShipment();

        final LocationProfile loc = new LocationProfile();
        loc.setAddress(address);
        loc.setCompany(s.getCompany());
        loc.setCompanyName(companyName);
        loc.setInterim(interim);
        loc.setStart(start);
        loc.setStop(stop);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        dao.save(loc);

        s.setShippedFrom(loc);
        shipmentDao.save(s);

        //create siblings with locations
        final Shipment sib1 = createShipment();
        sib1.setShippedFrom(loc);
        shipmentDao.save(sib1);

        final Shipment sib2 = createShipment();
        sib2.setShippedFrom(createLocation("Sibling location"));
        shipmentDao.save(sib2);

        setAsSiblings(s, sib1, sib2);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getStartLocation());
        assertEqualsLocations(sib1.getShippedFrom(), SingleShipmentTestUtils.getSibling(
                sib1.getId(), data).getStartLocation());
        assertEqualsLocations(sib2.getShippedFrom(), SingleShipmentTestUtils.getSibling(
                sib2.getId(), data).getStartLocation());
    }

    @Test
    public void testLeftLocationFromTo() {
        final Shipment s = createShipment();

        final LocationProfile loc = new LocationProfile();
        loc.setAddress("Odessa. Deribasovskaya 1, 1");
        loc.setCompany(s.getCompany());
        loc.setCompanyName("Company name");
        loc.setInterim(true);
        loc.setStart(true);
        loc.setStop(true);
        loc.setName("Location Name");
        loc.setNotes("Location notes");
        loc.setRadius(1500);
        dao.save(loc);

        shipmentDao.save(s);

        //create siblings with locations
        final Shipment sib1 = createShipment();
        shipmentDao.save(sib1);

        setAsSiblings(s, sib1);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();

        assertNull(data.getBean().getStartLocation());
    }
    @Test
    public void testLocationTo() {
        final String address = "Any address";
        final String companyName = "Any company";
        final boolean interim = true;
        final boolean start = false;
        final boolean stop = true;
        final String name = "Location name";
        final String notes = "Location notes";
        final int radius = 1500;

        final Shipment s = createShipment();

        final LocationProfile loc = new LocationProfile();
        loc.setAddress(address);
        loc.setCompany(s.getCompany());
        loc.setCompanyName(companyName);
        loc.setInterim(interim);
        loc.setStart(start);
        loc.setStop(stop);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        dao.save(loc);

        s.setShippedTo(loc);
        shipmentDao.save(s);

        //create siblings with locations
        final Shipment sib1 = createShipment();
        sib1.setShippedTo(loc);
        shipmentDao.save(sib1);

        final Shipment sib2 = createShipment();
        sib2.setShippedTo(createLocation("Sibling location"));
        shipmentDao.save(sib2);

        setAsSiblings(s, sib1, sib2);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getEndLocation());
        assertEqualsLocations(sib1.getShippedTo(), SingleShipmentTestUtils.getSibling(
                sib1.getId(), data).getEndLocation());
        assertEqualsLocations(sib2.getShippedTo(), SingleShipmentTestUtils.getSibling(
                sib2.getId(), data).getEndLocation());
    }
    @Test
    public void testAlternativeLocations() {
        final Shipment s = createShipment();
        final Shipment sib1 = createShipment();
        final Shipment sib2 = createShipment();

        setAsSiblings(s, sib1, sib2);

        //create alternative locations for main shipment
        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getFrom().add(createLocation("From1"));
        sLocs.getFrom().add(createLocation("From2"));
        sLocs.getTo().add(createLocation("To1"));
        sLocs.getTo().add(createLocation("To2"));
        sLocs.getInterim().add(createLocation("Stp1"));
        sLocs.getInterim().add(createLocation("Stp2"));

        altLocDao.save(s, sLocs);

        //create alternative locations for sibling.
        final AlternativeLocations sib1Locs = new AlternativeLocations();
        sib1Locs.getFrom().add(createLocation("SibFrom1"));
        sib1Locs.getTo().add(createLocation("SibTo1"));
        sib1Locs.getInterim().add(createLocation("SibStp1"));

        altLocDao.save(sib1, sib1Locs);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(2, data.getBean().getStartLocationAlternatives().size());
        assertEquals(2, data.getBean().getEndLocationAlternatives().size());
        assertEquals(2, data.getBean().getInterimLocationAlternatives().size());

        //first sibling
        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);

        assertEquals(1, sibBean1.getStartLocationAlternatives().size());
        assertEquals(sib1Locs.getFrom().get(0).getId(), sibBean1.getStartLocationAlternatives().get(0).getId());

        assertEquals(1, sibBean1.getEndLocationAlternatives().size());
        assertEquals(sib1Locs.getTo().get(0).getId(), sibBean1.getEndLocationAlternatives().get(0).getId());

        assertEquals(1, sibBean1.getInterimLocationAlternatives().size());
        assertEquals(sib1Locs.getInterim().get(0).getId(), sibBean1.getInterimLocationAlternatives().get(0).getId());

        //test second sibling
        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertEquals(0, sibBean2.getStartLocationAlternatives().size());
        assertEquals(0, sibBean2.getEndLocationAlternatives().size());
        assertEquals(0, sibBean2.getInterimLocationAlternatives().size());
    }
    @Test
    public void testAlternativeLocationStart() {
        final String address = "Any address";
        final String companyName = "Any company";
        final boolean interim = true;
        final boolean start = false;
        final boolean stop = true;
        final String name = "Location name";
        final String notes = "Location notes";
        final int radius = 1500;

        final Shipment s = createShipment();

        final LocationProfile loc = new LocationProfile();
        loc.setAddress(address);
        loc.setCompany(s.getCompany());
        loc.setCompanyName(companyName);
        loc.setInterim(interim);
        loc.setStart(start);
        loc.setStop(stop);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        dao.save(loc);

        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getFrom().add(loc);

        altLocDao.save(s, sLocs);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getStartLocationAlternatives().get(0));
    }
    @Test
    public void testAlternativeLocationEnd() {
        final String address = "Any address";
        final String companyName = "Any company";
        final boolean interim = true;
        final boolean start = false;
        final boolean stop = true;
        final String name = "Location name";
        final String notes = "Location notes";
        final int radius = 1500;

        final Shipment s = createShipment();

        final LocationProfile loc = new LocationProfile();
        loc.setAddress(address);
        loc.setCompany(s.getCompany());
        loc.setCompanyName(companyName);
        loc.setInterim(interim);
        loc.setStart(start);
        loc.setStop(stop);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        dao.save(loc);

        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getTo().add(loc);

        altLocDao.save(s, sLocs);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getEndLocationAlternatives().get(0));
    }
    @Test
    public void testAlternativeLocationInterim() {
        final String address = "Any address";
        final String companyName = "Any company";
        final boolean interim = true;
        final boolean start = false;
        final boolean stop = true;
        final String name = "Location name";
        final String notes = "Location notes";
        final int radius = 1500;

        final Shipment s = createShipment();

        final LocationProfile loc = new LocationProfile();
        loc.setAddress(address);
        loc.setCompany(s.getCompany());
        loc.setCompanyName(companyName);
        loc.setInterim(interim);
        loc.setStart(start);
        loc.setStop(stop);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        dao.save(loc);

        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getInterim().add(loc);

        altLocDao.save(s, sLocs);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();

        assertEqualsLocations(loc, data.getBean().getInterimLocationAlternatives().get(0));
    }
    @Test
    public void testOneLocationForAllAlternatives() {
        final Shipment s = createShipment();
        final Shipment sib = createShipment();

        setAsSiblings(s, sib);

        final LocationProfile loc = createLocation("From1");

        //create alternative locations for main shipment
        final AlternativeLocations sLocs = new AlternativeLocations();
        sLocs.getFrom().add(loc);
        sLocs.getTo().add(loc);
        sLocs.getInterim().add(loc);

        altLocDao.save(s, sLocs);

        //create alternative locations for sibling.
        final AlternativeLocations sibLocs = new AlternativeLocations();
        sibLocs.getFrom().add(loc);
        sibLocs.getTo().add(loc);
        sibLocs.getInterim().add(loc);

        altLocDao.save(sib, sibLocs);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(1, data.getBean().getStartLocationAlternatives().size());
        assertEquals(loc.getId(), data.getBean().getStartLocationAlternatives().get(0).getId());

        assertEquals(1, data.getBean().getEndLocationAlternatives().size());
        assertEquals(loc.getId(), data.getBean().getEndLocationAlternatives().get(0).getId());

        assertEquals(1, data.getBean().getInterimLocationAlternatives().size());
        assertEquals(loc.getId(), data.getBean().getInterimLocationAlternatives().get(0).getId());

        //first sibling
        final SingleShipmentBean sibBean = SingleShipmentTestUtils.getSibling(sib.getId(), data);

        assertEquals(1, sibBean.getStartLocationAlternatives().size());
        assertEquals(loc.getId(), sibBean.getStartLocationAlternatives().get(0).getId());

        assertEquals(1, sibBean.getEndLocationAlternatives().size());
        assertEquals(loc.getId(), sibBean.getEndLocationAlternatives().get(0).getId());

        assertEquals(1, sibBean.getInterimLocationAlternatives().size());
        assertEquals(loc.getId(), sibBean.getInterimLocationAlternatives().get(0).getId());
    }
    @Test
    public void testNote() {
        final User u = createUser("junit");

        final Shipment s = createShipment();
        final Shipment sib1 = createShipment();
        final Shipment sib2 = createShipment();

        setAsSiblings(s, sib1, sib2);

        createNote(s, u, "NoteMain1");
        createNote(s, u, "NoteMain2");
        final Note nSib = createNote(sib1, u, "NoteSib");

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(2, data.getBean().getNotes().size());

        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);
        assertEquals(1, sibBean1.getNotes().size());
        assertEquals(nSib.getNoteNum(), sibBean1.getNotes().get(0).getNoteNum());

        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertEquals(0, sibBean2.getNotes().size());
    }
    @Test
    public void testNoteNote() {
        final User u = createUser("junit");
        final Shipment s = createShipment();

        final boolean active = true;
        final Date creationDate = new Date(System.currentTimeMillis() - 100500l);
        final String notetext = "Any text of note";
        final String notetype = "Green";
        final Date timeOnChart = new Date(System.currentTimeMillis() - 100000l);

        final Note note = new Note();
        note.setActive(active);
        note.setCreatedBy(u.getEmail());
        note.setCreationDate(creationDate);
        note.setNoteText(notetext);
        note.setNoteType(notetype);
        note.setTimeOnChart(timeOnChart);

        noteDao.save(s, note);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(1, data.getBean().getNotes().size());

        //check equals stops
        final NoteBean n = data.getBean().getNotes().get(0);

        assertEquals(active, n.isActive());
        assertEquals(u.getEmail(), n.getCreatedBy());
        assertEqualsDates(creationDate, n.getCreationDate());
        assertNotNull(n.getNoteNum());
        assertEquals(notetext, n.getNoteText());
        assertEquals(StringUtils.createFullUserName(u.getFirstName(), u.getLastName()), n.getCreatedByName());
        assertEquals(notetype, n.getNoteType());
        assertEqualsDates(timeOnChart, n.getTimeOnChart());
    }
    @Test
    public void testArrival() {
        final Shipment s = createShipment();
        final Shipment sib1 = createShipment();
        final Shipment sib2 = createShipment();

        setAsSiblings(s, sib1, sib2);

        final Arrival sArr = createArrival(s);
        final Arrival sibArr = createArrival(sib1);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(sArr.getId(), data.getBean().getArrival().getId());

        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);
        assertEquals(sibArr.getId(), sibBean1.getArrival().getId());

        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertNull(sibBean2.getArrival());
    }
    @Test
    public void testArrivalArrival() {
        final Shipment s = createShipment();
        final TrackerEvent e = createTrackerEvent(s);

        final Arrival arrival = new Arrival();
        final Integer numberOfMetersOfArrival = 1500;
        final Date date = new Date(System.currentTimeMillis() - 1230000L);

        arrival.setDate(date);
        arrival.setDevice(s.getDevice());
        arrival.setNumberOfMettersOfArrival(numberOfMetersOfArrival);
        arrival.setShipment(s);
        arrival.setTrackerEventId(e.getId());

        context.getBean(ArrivalDao.class).save(arrival);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        final ArrivalBean a = data.getBean().getArrival();

        assertEqualsDates(date, a.getDate());
        assertEquals(arrival.getId(), a.getId());
        assertEquals(numberOfMetersOfArrival, a.getMettersForArrival());
        assertEqualsDates(date, a.getNotifiedAt());
        assertEquals(e.getId(), a.getTrackerEventId());
    }
    @Test
    public void testArrivalNotificationSchedule() {
        final User u = createUser("junit");

        final Shipment s = createShipment();
        final Shipment sib1 = createShipment();
        final Shipment sib2 = createShipment();

        setAsSiblings(s, sib1, sib2);

        final NotificationSchedule sched1 = createNotificationSchedule(u, "NoteMain1");
        final NotificationSchedule sched2 = createNotificationSchedule(u, "NoteMain2");
        s.getArrivalNotificationSchedules().add(sched1);
        s.getArrivalNotificationSchedules().add(sched2);
        shipmentDao.save(s);

        final NotificationSchedule schedSib = createNotificationSchedule(u, "NoteSib");
        sib1.getArrivalNotificationSchedules().add(schedSib);
        shipmentDao.save(sib1);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(2, data.getBean().getArrivalNotificationSchedules().size());

        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);
        assertEquals(1, sibBean1.getArrivalNotificationSchedules().size());
        assertEquals(schedSib.getId(), sibBean1.getArrivalNotificationSchedules().get(0).getId());

        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertEquals(0, sibBean2.getArrivalNotificationSchedules().size());
    }
    @Test
    public void testArrivalNotificationScheduleSchedule() {
        final User u1 = createUser("junit1");
        final User u2 = createUser("junit2");
        final Shipment s = createShipment();

        final NotificationSchedule sched = new NotificationSchedule();
        sched.setCompany(sharedCompany);
        final String description = "Schedule description";
        sched.setDescription(description);
        final String name = "Schedule name";
        sched.setName(name);

        final PersonSchedule ps1 = new PersonSchedule();
        ps1.setWeekDays(false, true, false, true, false, true, false);
        ps1.setFromTime(16);
        ps1.setSendApp(true);
        ps1.setSendEmail(true);
        ps1.setSendSms(true);
        ps1.setToTime(4578);
        ps1.setUser(u1);

        final PersonSchedule ps2 = new PersonSchedule();
        ps2.setUser(u2);

        sched.getSchedules().add(ps1);
        sched.getSchedules().add(ps2);

        context.getBean(NotificationScheduleDao.class).save(sched);

        s.getArrivalNotificationSchedules().add(sched);
        shipmentDao.save(s);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(1, data.getBean().getArrivalNotificationSchedules().size());

        //check equals stops
        final ListNotificationScheduleItem n = data.getBean().getArrivalNotificationSchedules().get(0);

        assertEquals(sched.getId(), n.getId());
        assertEquals(sched.getDescription(), n.getNotificationScheduleDescription());
        assertEquals(sched.getId().longValue(), n.getNotificationScheduleId());
        assertEquals(sched.getName(), n.getNotificationScheduleName());
        assertTrue(n.getPeopleToNotify().contains(getFullName(u1)));
        assertTrue(n.getPeopleToNotify().contains(getFullName(u1)));
    }
    @Test
    public void testAlertsNotificationSchedule() {
        final User u = createUser("junit");

        final Shipment s = createShipment();
        final Shipment sib1 = createShipment();
        final Shipment sib2 = createShipment();

        setAsSiblings(s, sib1, sib2);

        final NotificationSchedule sched1 = createNotificationSchedule(u, "NoteMain1");
        final NotificationSchedule sched2 = createNotificationSchedule(u, "NoteMain2");
        s.getAlertsNotificationSchedules().add(sched1);
        s.getAlertsNotificationSchedules().add(sched2);
        shipmentDao.save(s);

        final NotificationSchedule schedSib = createNotificationSchedule(u, "NoteSib");
        sib1.getAlertsNotificationSchedules().add(schedSib);
        shipmentDao.save(sib1);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(2, data.getBean().getAlertsNotificationSchedules().size());

        final SingleShipmentBean sibBean1 = SingleShipmentTestUtils.getSibling(sib1.getId(), data);
        assertEquals(1, sibBean1.getAlertsNotificationSchedules().size());
        assertEquals(schedSib.getId(), sibBean1.getAlertsNotificationSchedules().get(0).getId());

        final SingleShipmentBean sibBean2 = SingleShipmentTestUtils.getSibling(sib2.getId(), data);
        assertEquals(0, sibBean2.getAlertsNotificationSchedules().size());
    }
    @Test
    public void testAlertsNotificationScheduleSchedule() {
        final User u1 = createUser("junit1");
        final User u2 = createUser("junit2");
        final Shipment s = createShipment();

        final NotificationSchedule sched = new NotificationSchedule();
        sched.setCompany(sharedCompany);
        final String description = "Schedule description";
        sched.setDescription(description);
        final String name = "Schedule name";
        sched.setName(name);

        final PersonSchedule ps1 = new PersonSchedule();
        ps1.setWeekDays(false, true, false, true, false, true, false);
        ps1.setFromTime(16);
        ps1.setSendApp(true);
        ps1.setSendEmail(true);
        ps1.setSendSms(true);
        ps1.setToTime(4578);
        ps1.setUser(u1);

        final PersonSchedule ps2 = new PersonSchedule();
        ps2.setUser(u2);

        sched.getSchedules().add(ps1);
        sched.getSchedules().add(ps2);

        context.getBean(NotificationScheduleDao.class).save(sched);

        s.getAlertsNotificationSchedules().add(sched);
        shipmentDao.save(s);

        final SingleShipmentBuildContext ctxt = new SingleShipmentBuildContext();
        final MainShipmentDataBuilder b = new MainShipmentDataBuilder(jdbc, s.getId(), s.getCompany().getId());
        b.fetchData();
        b.build(ctxt);

        final SingleShipmentData data = ctxt.getData();
        assertEquals(1, data.getBean().getAlertsNotificationSchedules().size());

        //check equals stops
        final ListNotificationScheduleItem n = data.getBean().getAlertsNotificationSchedules().get(0);

        assertEquals(sched.getId(), n.getId());
        assertEquals(sched.getDescription(), n.getNotificationScheduleDescription());
        assertEquals(sched.getId().longValue(), n.getNotificationScheduleId());
        assertEquals(sched.getName(), n.getNotificationScheduleName());
        assertTrue(n.getPeopleToNotify().contains(getFullName(u1)));
        assertTrue(n.getPeopleToNotify().contains(getFullName(u1)));
    }
    /**
     * @param s shipment.
     * @param u user.
     * @param name schedule name.
     */
    private NotificationSchedule createNotificationSchedule(final User u, final String name) {
        //create person schedule.
        final PersonSchedule ps = new PersonSchedule();
        ps.setAllWeek();
        ps.setSendApp(true);
        ps.setSendEmail(true);
        ps.setSendSms(true);
        ps.setToTime(23 * 60);
        ps.setFromTime(1);
        ps.setUser(u);

        //create notification schedule.
        final NotificationSchedule sched = new NotificationSchedule();
        sched.setCompany(sharedCompany);
        sched.setDescription(name);
        sched.setName(name);
        sched.getSchedules().add(ps);
        context.getBean(NotificationScheduleDao.class).save(sched);

        return sched;
    }
    /**
     * @param s shipment.
     * @return tracker event.
     */
    private TrackerEvent createTrackerEvent(final Shipment s) {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(700);
        e.setCreatedOn(new Date());
        e.setDevice(s.getDevice());
        e.setLatitude(30.);
        e.setLongitude(20.);
        e.setShipment(s);
        e.setTemperature(12.);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        return context.getBean(TrackerEventDao.class).save(e);
    }
    /**
     * @param s shipment.
     * @return arrival
     */
    private Arrival createArrival(final Shipment s) {
        final Arrival a = new Arrival();
        a.setDate(new Date());
        a.setDevice(s.getDevice());
        a.setNumberOfMettersOfArrival(500);
        a.setShipment(s);
        return context.getBean(ArrivalDao.class).save(a);
    }
    /**
     * @param symbolycId
     * @return
     */
    private User createUser(final String symbolycId) {
        final User u = new User();
        u.setCompany(sharedCompany);
        u.setActive(true);
        u.setEmail(symbolycId + "@smarttrace.com.au");
        u.setFirstName(symbolycId);
        //add roles
        final List<Role> roles = new LinkedList<>();
        roles.add(Role.BasicUser);
        u.setRoles(roles);
        //save user
        return context.getBean(UserDao.class).save(u);
    }
    /**
     * @param s shipment.
     * @param u user.
     * @param text note text.
     * @return note
     */
    private Note createNote(final Shipment s, final User u, final String text) {
        final Note n = new Note();
        n.setActive(true);
        n.setCreatedBy(u.getEmail());
        n.setCreationDate(new Date());
        n.setNoteText(text);
        n.setNoteType("Green");
        n.setTimeOnChart(new Date());
        return noteDao.save(s, n);
    }
    /**
     * @param loc location.
     * @return unsaved interim stop.
     */
    private InterimStop createInterimStop(final LocationProfile loc) {
        final InterimStop stp = new InterimStop();
        stp.setDate(new Date());
        stp.setLocation(loc);
        stp.setTime(12);
        return stp;
    }
    /**
     * @param loc
     * @param b
     */
    private void assertEqualsLocations(final LocationProfile loc, final LocationProfileBean b) {
        assertEquals("ID", loc.getId(), b.getId());
        assertEquals("Address", loc.getAddress(), b.getAddress());
        assertEquals("Company name", loc.getCompanyName(), b.getCompanyName());
        assertEquals("Latitude", loc.getLocation().getLatitude(), b.getLocation().getLatitude(), 0.001);
        assertEquals("Longitude", loc.getLocation().getLongitude(), b.getLocation().getLongitude(), 0.001);
        assertEquals("Name", loc.getName(), b.getName());
        assertEquals("Notes", loc.getNotes(), b.getNotes());
        assertEquals("Radius", loc.getRadius(), b.getRadius());
    }
    /**
     * @param d1 first date.
     * @param d2 second date.
     */
    private void assertEqualsDates(final Date d1, final Date d2) {
        assertTrue(Math.abs(d1.getTime() - d2.getTime()) < 1000l);
    }
    private LocationProfile createLocation(final String name) {
        final LocationProfile p = new LocationProfile();
        p.setAddress("Odessa city, Deribasovskaya st. 1, apt. 1");
        p.setCompany(sharedCompany);
        p.setInterim(true);
        p.setName("Test location");
        p.setNotes("Any notes");
        p.setRadius(700);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.200);
        p.getLocation().setLongitude(300.400);
        return dao.save(p);
    }
    /**
     * @param user user.
     * @return full user name.
     */
    private String getFullName(final User user) {
        return StringUtils.createFullUserName(user.getFirstName(), user.getLastName());
    }
    /**
     * @return shipment.
     */
    private Shipment createShipment() {
        return shipmentDao.save(createDefaultNotSavedShipment(device));
    }
}
