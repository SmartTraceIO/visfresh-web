/**
 *
 */
package com.visfresh.reports.shipment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import com.visfresh.dao.impl.ShipmentReportDaoImpl;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Color;
import com.visfresh.entities.CorrectiveActionList;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.shipment.AlertProfileBean;
import com.visfresh.io.shipment.ArrivalBean;
import com.visfresh.io.shipment.CorrectiveActionListBean;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.reports.PdfReportBuilder;
import com.visfresh.reports.PdfReportBuilderFactory;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.tools.SingleShipmentUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class ShipmentReportBuilderTool {

    /**
     * Default constructor.
     */
    public ShipmentReportBuilderTool() {
        super();
    }

    /**
     * @return the user.
     */
    private static User createUser() {
        final User user = new User();
        user.setId(7l);
        user.setEmail("dev@smarttrace.com.au");
//        user.setTemperatureUnits(TemperatureUnits.Fahrenheit);
        user.setTimeZone(TimeZone.getDefault());
        return user;
    }
    /**
     * @param bean
     * @param user
     * @throws DRException
     * @throws IOException
     * @throws JRException
     */
    public static void showShipmentReport(final ShipmentReportBean bean, final User user)
            throws IOException {
        final PdfReportBuilder builder = PdfReportBuilderFactory.createReportBuilder();
        final OutputStream out = createPdfOut();
        try {
            builder.createShipmentReport(bean, user, out);
        } finally {
            out.close();
        }
    }

    /**
     * @return
     * @throws FileNotFoundException
     */
    private static OutputStream createPdfOut() throws FileNotFoundException {
        int i = 0;
        while (true) {
            i++;
            final File f = new File("shrep-" + i + ".pdf");
            System.out.println(f.getAbsolutePath());
            if (!f.exists()) {
                return new FileOutputStream(f);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final File f = new File(args[0]);
        if (!f.exists()) {
            System.err.println("Shipment data file should exist");
            return;
        }

        final User user = createUser();
        showShipmentReport(createShipmentReportBean(f), user);
    }

    /**
     * @param f single shipment data file
     * @return shipment report bean.
     * @throws IOException
     */
    private static ShipmentReportBean createShipmentReportBean(final File f) throws IOException {
        final SingleShipmentData data = loadSingleShipmentData(f);
        final SingleShipmentBean s = data.getBean();

        final ShipmentReportBean bean = new ShipmentReportBean();
        //populate bean
        bean.setArrival(createBean(s.getArrival()));
        bean.setDateArrived(s.getArrivalTime());

        bean.setCompanyName("Report Tool studio");
        bean.setAssetNum(s.getAssetNum());
        bean.setComment(s.getCommentsForReceiver());
        bean.setDateShipped(s.getShutdownTime());
        bean.setDescription(s.getShipmentDescription());
        bean.setDevice(s.getDevice());
        bean.setNumberOfSiblings(data.getSiblings().size());
        bean.setPalletId(s.getPalletId());
        bean.setShutdownTime(s.getShutdownTime());
        bean.setAlertSuppressionMinutes(s.getAlertSuppressionMinutes());

        if (s.getDeviceColor() != null) {
            bean.setDeviceColor(java.awt.Color.decode(Color.valueOf(s.getDeviceColor()).getHtmlValue()));
        }
        bean.setShippedFrom(createBean(s.getStartLocation()));
        bean.getInterimStops().addAll(toStops(s.getInterimStops()));

        if (s.getEndLocation() != null) {
            bean.setShippedTo(createBean(s.getEndLocation()));
        } else if (!s.getEndLocationAlternatives().isEmpty()){
            final List<String> altNames = new LinkedList<>();
            for (final LocationProfileBean altLoc : s.getEndLocationAlternatives()) {
                altNames.add(altLoc.getName());
            }

            bean.setPossibleShippedTo(altNames);
        }
        bean.setStatus(s.getStatus());
        bean.setTripCount(s.getTripCount());

        //add readings
        final List<SingleShipmentLocationBean> events = data.getLocations();
        for (final SingleShipmentLocationBean e : events) {
            bean.getReadings().add(createEvent(s, e));
        }

        //add temperature history
        if (s.getAlertProfile() != null) {
            final AlertProfileBean ap = s.getAlertProfile();
            bean.setAlertProfile(ap.getName());
        }

        //add fired alerts
        bean.getFiredAlertRules().addAll(s.getAlertFired());

        //add notified persons
        bean.getAlerts().addAll(s.getSentAlerts());

        //get notifications
        //notified by alert
        bean.getWhoWasNotifiedByAlert().add("Not implemented for test tool");

        if (s.getStatus() == ShipmentStatus.Arrived) {
            if (s.getArrival() != null) {
                //notified users
                bean.getWhoReceivedReport().add("Not implemented for test tool");
            }
        } else if (!s.getStatus().isFinal()){
            //user to be notified map. Map - because need to avoid of duplicates.
            //get not potentially notified users without filtering by time frames
            for (final ListNotificationScheduleItem schedule : s.getArrivalNotificationSchedules()) {
                bean.getWhoReceivedReport().add(schedule.getPeopleToNotify());
            }
        }

        bean.setTemperatureStats(createTemperatureStats(s, events));
        return bean;
    }

    /**
     * @param ap
     * @param events
     * @return
     */
    private static TemperatureStats createTemperatureStats(final SingleShipmentBean s,
            final List<SingleShipmentLocationBean> events) {
        //create mock shipment
        final Shipment shipment = new Shipment();
        shipment.setStatus(s.getStatus());
        shipment.setId(s.getShipmentId());

        final List<TrackerEvent> trackerEvents = new LinkedList<>();
        for (final SingleShipmentLocationBean e : events) {
            final TrackerEvent event = createTrackerEvent(e);
            event.setShipment(shipment);
            trackerEvents.add(event);
        }

        return ShipmentReportDaoImpl.createTemperatureStats(createAlertProfile(s.getAlertProfile()), trackerEvents);
    }

    /**
     * @param p
     * @return
     */
    private static AlertProfile createAlertProfile(final AlertProfileBean p) {
        if (p == null) {
            return null;
        }

        final AlertProfile ap = new AlertProfile();
        ap.setBatteryLowCorrectiveActions(createCorrectiveActions(p.getBatteryLowCorrectiveActions()));
        ap.setDescription(p.getDescription());
        ap.setId(p.getId());
        ap.setLightOnCorrectiveActions(createCorrectiveActions(p.getLightOnCorrectiveActions()));
        ap.setLowerTemperatureLimit(p.getLowerTemperatureLimit());
        ap.setName(p.getName());
        ap.setUpperTemperatureLimit(p.getUpperTemperatureLimit());
        ap.setWatchBatteryLow(p.isWatchBatteryLow());
        ap.setWatchEnterBrightEnvironment(p.isWatchEnterBrightEnvironment());
        ap.setWatchEnterDarkEnvironment(p.isWatchEnterDarkEnvironment());
        ap.setWatchMovementStart(p.isWatchMovementStart());
        ap.setWatchMovementStop(p.isWatchMovementStop());
        return ap;
    }

    /**
     * @param bean
     * @return
     */
    private static TrackerEvent createTrackerEvent(final SingleShipmentLocationBean bean) {
        final TrackerEvent e = new TrackerEvent();
        e.setCreatedOn(bean.getTime());
        e.setId(bean.getId());
        e.setLatitude(bean.getLatitude());
        e.setLongitude(bean.getLongitude());
        e.setTemperature(bean.getTemperature());
        e.setTime(bean.getTime());
        e.setType(bean.getType());
        return e;
    }
    /**
     * @param bean
     * @return
     */
    private static CorrectiveActionList createCorrectiveActions(final CorrectiveActionListBean bean) {
        final CorrectiveActionList list = new CorrectiveActionList();
        list.setDescription(bean.getDescription());
        list.setId(bean.getId());
        list.setName(bean.getName());

        list.getActions().addAll(bean.getActions());
        return list;
    }
    /**
     * @param bean
     * @return
     */
    private static ShortTrackerEvent createEvent(final SingleShipmentBean s, final SingleShipmentLocationBean bean) {
        final ShortTrackerEvent e = new ShortTrackerEvent();
        e.setCreatedOn(bean.getTime());
        e.setDeviceModel(s.getDeviceModel());
        e.setDeviceImei(s.getDevice());
        e.setId(bean.getId());
        e.setLatitude(bean.getLatitude());
        e.setLongitude(bean.getLongitude());
        e.setShipmentId(s.getShipmentId());
        e.setTemperature(bean.getTemperature());
        e.setHumidity(bean.getHumidity());
        e.setTime(bean.getTime());
        e.setType(bean.getType());
        return e;
    }

    /**
     * @param interimStops
     * @return
     */
    private static List<InterimStop> toStops(final List<InterimStopBean> interimStops) {
        final List<InterimStop> stops = new LinkedList<>();
        for (final InterimStopBean s : interimStops) {
            final InterimStop stp = new InterimStop();
            stp.setDate(s.getStopDate());
            stp.setId(s.getId());
            stp.setLocation(createBean(s.getLocation()));
            stp.setTime(s.getTime());
            stops.add(stp);
        }
        return stops;
    }

    /**
     * @param startLocation
     * @return
     */
    private static LocationProfile createBean(final LocationProfileBean loc) {
        if (loc == null) {
            return null;
        }
        final LocationProfile p = new LocationProfile();
        p.setAddress(loc.getAddress());
        p.setId(loc.getId());
        p.setInterim(loc.isInterim());
        p.setName(loc.getName());
        p.setNotes(loc.getNotes());
        p.setRadius(loc.getRadius());
        p.setStart(loc.isStart());
        p.setStop(loc.isStop());
        return p;
    }

    /**
     * @param arrival
     * @return
     */
    private static com.visfresh.reports.shipment.ArrivalBean createBean(final ArrivalBean arrival) {
        if (arrival == null) {
            return null;
        }

        final com.visfresh.reports.shipment.ArrivalBean bean = new com.visfresh.reports.shipment.ArrivalBean();
        bean.setNotifiedAt(arrival.getNotifiedAt());
        bean.setNotifiedWhenKm(arrival.getMettersForArrival());
        return bean;
    }

    /**
     * @param f shipment data file.
     * @return
     * @throws IOException
     */
    private static SingleShipmentData loadSingleShipmentData(final File f) throws IOException {
        final InputStream in = new FileInputStream(f);
        try {
            return SingleShipmentUtils.parseSingleShipmentDataJson(in);
        } finally {
            in.close();
        }
    }
}
