/**
 *
 */
package com.visfresh.reports.shipment;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import com.visfresh.dao.impl.AlertProfileTemperatureStatsCollector;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;
import com.visfresh.l12n.RuleBundle;
import com.visfresh.reports.ShortTrackerEventsImporter;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.utils.StringUtils;

import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class ShipmentReportBuilderTool {
    private static Device device = createDevice();
    private static Random random = new Random();

    /**
     * Default constructor.
     */
    public ShipmentReportBuilderTool() {
        super();
    }

    /**
     * @return
     */
    private static Device createDevice() {
        final Device d = new Device();
        d.setImei("1920387091287439");
        return d;
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
     * @param user TODO
     * @return the bean to visualize.
     * @throws ParseException
     * @throws IOException
     */
    private static ShipmentReportBean createShipmentReportBean(final User user) throws IOException, ParseException {
        final ShipmentReportBean bean = new ShipmentReportBean();

        bean.setCompanyName("Test Company");
        bean.setAssetNum("Test Asset");
        bean.setComment("Test bean for development");
        bean.setDateArrived(new Date(System.currentTimeMillis() - 100000000));
        bean.setDateShipped(new Date(System.currentTimeMillis() - 1000000000));
        bean.setDescription("Autostarted by rule");
        bean.setDevice(device.getImei());
        bean.setNumberOfSiblings(7);
        bean.setPalletId("12345");
        final List<String> altLocs = new LinkedList<>();
        altLocs.add("De Costi Office Lidcombe");
        altLocs.add("Odessa deribasovskaya");
        altLocs.add("WW DC Sydney");
        altLocs.add("WW DC Brisbane");

        bean.setPossibleShippedTo(altLocs);

        bean.setStatus(ShipmentStatus.Default);
        bean.setStatus(ShipmentStatus.Arrived);
//        bean.setStatus(ShipmentStatus.Ended);
        bean.setTripCount(14);
        bean.setAlertSuppressionMinutes(110);

        //add receivers
        bean.getWhoReceivedReport().add("Max Plank");
        bean.getWhoReceivedReport().add("Alexander Makedonsky");

        addReadings(bean);
        final List<ShortTrackerEvent> readings = bean.getReadings();

        if (readings.size() > 0) {
            final ShortTrackerEvent first = readings.get(0);
            final LocationProfile from = new LocationProfile();
            from.setName("De Costi Office Lidcombe");
            from.getLocation().setLatitude(first.getLatitude());
            from.getLocation().setLongitude(first.getLongitude());
            bean.setShippedFrom(from);

            bean.setDateShipped(first.getTime());

            final ShortTrackerEvent arrived = readings.get(readings.size() - Math.min(readings.size(), 10));
            bean.setDateArrived(arrived.getTime());

            final LocationProfile to = new LocationProfile();
            to.setName("De Costi Office Lidcombe");
            to.getLocation().setLatitude(arrived.getLatitude());
            to.getLocation().setLongitude(arrived.getLongitude());

            bean.setShippedTo(to);
            //set arrival
//            final ArrivalBean arrival = new ArrivalBean();
//            arrival.setNotifiedAt(bean.getDateArrived());
//            arrival.setNotifiedWhenKm(40);
//            bean.setArrival(arrival);
//
//            bean.setShutdownTime(new Date(arrival.getNotifiedAt().getTime() + 10000000));
        }

        //add interim stops
        final int num = readings.size() / 5;
        int count = num;
        while (count < readings.size()) {
            final ShortTrackerEvent e = readings.get(count);

            //create location of interim stop
            final LocationProfile loc = new LocationProfile();
            loc.setName("De Costi Office Lidcombe " + bean.getInterimStops().size());
            loc.getLocation().setLatitude(e.getLatitude());
            loc.getLocation().setLongitude(e.getLongitude());

            final InterimStop stop = new InterimStop();
            stop.setLocation(loc);
            stop.setDate(e.getTime());

            bean.getInterimStops().add(stop);

            count += num;
        }

        bean.setAlertProfile("Chilled Beef");

        //calculate stats
        final Shipment s = new Shipment();
        s.setId(7l);

        final Device device = new Device();
        device.setImei(bean.getDevice());
        s.setDevice(device);

        final AlertProfile ap = new AlertProfile();
        ap.setName(bean.getAlertProfile());
        ap.setUpperTemperatureLimit(5);
        ap.setLowerTemperatureLimit(2);
        s.setAlertProfile(ap);

        final AlertProfileTemperatureStatsCollector collector = new AlertProfileTemperatureStatsCollector();
        for (final ShortTrackerEvent r : readings) {
            final TrackerEvent e = new TrackerEvent();
            e.setShipment(s);
            e.setDevice(s.getDevice());
            e.setTime(r.getTime());
            e.setCreatedOn(r.getCreatedOn());
            e.setTemperature(r.getTemperature());
            e.setBattery(r.getBattery());
            e.setLatitude(r.getLatitude());
            e.setLongitude(r.getLongitude());
            e.setType(r.getType());

            collector.processEvent(e);
        }

        final TemperatureStats stats = collector.getStatistics();
        stats.setLowerTemperatureLimit(ap.getLowerTemperatureLimit());
        stats.setUpperTemperatureLimit(ap.getUpperTemperatureLimit());
        bean.setTemperatureStats(stats);

        bean.getWhoWasNotifiedByAlert().add("user1@smarttrace.com.au");
        bean.getWhoWasNotifiedByAlert().add("user2@smarttrace.com.au");

        addFiredAlert(bean, new AlertRule(AlertType.MovementStart));
        addFiredAlert(bean, AlertType.Cold, 0., 60 * 1000l);
        addFiredAlert(bean, AlertType.Hot, 10., 30 * 1000l);
        addFiredAlert(bean, AlertType.CriticalHot, 40., 10 * 1000l);
        addFiredAlert(bean, AlertType.CriticalHot, 50., 30 * 1000l);
        addFiredAlert(bean, new AlertRule(AlertType.LightOn));
        addFiredAlert(bean, new AlertRule(AlertType.LightOff));
        addFiredAlert(bean, AlertType.Cold, 0., 60 * 1000l);
        addFiredAlert(bean, AlertType.CriticalCold, -5., 30 * 1000l);
        addFiredAlert(bean, AlertType.CriticalCold, -10.5, 10 * 1000l);
        addFiredAlert(bean, AlertType.CriticalHot, 60., 30 * 1000l);
        addFiredAlert(bean, AlertType.CriticalHot, 70., 30 * 1000l);
        addFiredAlert(bean, AlertType.CriticalHot, 50., 30 * 1000l);
        addFiredAlert(bean, new AlertRule(AlertType.LightOn));
        addFiredAlert(bean, new AlertRule(AlertType.LightOff));
        addFiredAlert(bean, AlertType.Cold, 0., 60 * 1000l);
        addFiredAlert(bean, AlertType.CriticalCold, -5., 30 * 1000l);
        addFiredAlert(bean, AlertType.CriticalCold, -10.5, 10 * 1000l);
        addFiredAlert(bean, AlertType.CriticalHot, 60., 30 * 1000l);
        addFiredAlert(bean, AlertType.CriticalHot, 70., 30 * 1000l);

        return bean;
    }
    /**
     * @param bean
     * @throws ParseException
     * @throws IOException
     */
    private static void addReadings(final ShipmentReportBean bean) throws IOException, ParseException {
        final String readings = StringUtils.getContent(ShipmentReportBuilderTool.class.getResource("readings.csv"),
                "UTF-8");
        final List<ShortTrackerEvent> events = new ShortTrackerEventsImporter(1l) {
            @Override
            protected void handleEventImported(final ShortTrackerEvent e) {
                e.setDeviceImei(bean.getDevice());
            };
        }.importEvents(new StringReader(readings));
        bean.getReadings().addAll(events);
    }

    /**
     * @param bean report bean.
     * @param type alert type.
     * @param temperature temperature.
     * @param time time.
     */
    private static void addFiredAlert(final ShipmentReportBean bean, final AlertType type,
            final double temperature, final long time) {
        final TemperatureRule rule = new TemperatureRule();
        rule.setType(type);
        rule.setTemperature(temperature);
        rule.setTimeOutMinutes((int) (time / (60 * 1000l)));
        addFiredAlert(bean, rule);
    }
    /**
     * @param bean
     * @param rule
     */
    private static void addFiredAlert(final ShipmentReportBean bean,
            final AlertRule rule) {
        final ShortTrackerEvent e = bean.getReadings().get(random.nextInt(bean.getReadings().size()));
        Alert alert;
        if (rule instanceof TemperatureRule) {
            final TemperatureRule tr = (TemperatureRule) rule;

            final TemperatureAlert ta = new TemperatureAlert();
            ta.setType(rule.getType());
            ta.setTemperature(e.getTemperature());
            ta.setCumulative(tr.isCumulativeFlag());
            ta.setMinutes(tr.getTimeOutMinutes());

            alert = ta;
        } else {
            alert = new Alert(rule.getType());
            if (rule.getType() == AlertType.LightOn) {
                e.setType(TrackerEventType.BRT);
            } else if (rule.getType() == AlertType.LightOff) {
                e.setType(TrackerEventType.DRK);
            }
        }

        alert.setDate(e.getTime());
        alert.setDevice(device);
        alert.setTrackerEventId(e.getId());

        bean.getAlerts().add(alert);
        bean.getFiredAlertRules().add(rule);
    }
    /**
     * @param bean
     * @param user
     * @throws DRException
     * @throws IOException
     * @throws JRException
     */
    public static void showShipmentReport(final ShipmentReportBean bean, final User user)
            throws DRException, IOException, JRException {
        final ShipmentReportBuilder builder = new ShipmentReportBuilder() {
            {
                this.ruleBundle = new RuleBundle();
            }
        };
        builder.createReport(bean, user).show();
    }

    public static void main(final String[] args) throws Exception {
        final User user = createUser();
        showShipmentReport(createShipmentReportBean(user), user);
    }
}
