/**
 *
 */
package com.visfresh.reports.shipment;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import net.sf.dynamicreports.report.exception.DRException;

import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEventWithAlerts;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;

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
        return user;
    }
    /**
     * @return the bean to visualize.
     */
    private static ShipmentReportBean createPerformanceBean() {
        final ShipmentReportBean bean = new ShipmentReportBean();

        bean.setAssetNum("Test Asset");
        bean.setComment("Test bean for development");
        bean.setDateArrived(new Date(System.currentTimeMillis() - 100000000));
        bean.setDateShipped(new Date(System.currentTimeMillis() - 1000000000));
        bean.setDescription("Autostarted by rule");
        bean.setDevice("1920387091287439");
        bean.setNumberOfSiblings(3);
        bean.setSuppressFurtherAlerts(true);
        bean.setPalletId("pallet-007");
        bean.setShippedFrom("Sidney");
        bean.setShippedTo("Canberra");
        bean.setStatus(ShipmentStatus.Arrived);
        bean.setTripCount(14);

        //set arrival
        final ArrivalBean arrival = new ArrivalBean();
        arrival.setNotifiedAt(bean.getDateArrived());
        arrival.setShutdownTime(new Date(System.currentTimeMillis() - 10000000));
        arrival.setTime(bean.getDateArrived());
        arrival.setNotifiedWhenKm(40);
        bean.setArrival(arrival);

        //notified by arrival
        arrival.getSchedules().add("High Temp en route COLES DC adelaide");
        arrival.getSchedules().add("Low Temp en route COLES DC adelaide");

        arrival.getWhoIsNotified().add("James");
        arrival.getWhoIsNotified().add("Vu");
        arrival.getWhoIsNotified().add("Vyacheslav");

        //generate events
        //Sidnay -33°52′10″ 151°12′30″
        final double lat0 = -33. + 52 / 60.;
        final double lon0 = 151. + 12 / 60.;

        //Carnarvon 24°52′02″ 113°39′40″
        final double lat1 = 24. + 52 / 60.;
        final double lon1 = 113. + 39 / 60.;

        final int numReadings = 15;
        final double dlat = (lat1 - lat0) / numReadings;
        final double dlon = (lon1 - lon0) / numReadings;

        //time
        final long t0 = bean.getDateShipped().getTime();
        final long dt = (bean.getDateArrived().getTime() - t0) / numReadings;

        final Random random = new Random();
        for (int i = 0; i < numReadings; i++) {
            final ShortTrackerEventWithAlerts e = new ShortTrackerEventWithAlerts();
            e.setLatitude(lat0 + i * dlat);
            e.setLongitude(lon0 + i * dlon);
            e.setTemperature(11. + random.nextDouble() * 3.);
            e.setBattery(3000 + random.nextInt(15));
            e.setType(TrackerEventType.AUT);
            e.setTime(new Date(t0 + i * dt));
            bean.getReadings().add(e);
        }

        final long oneHour = 60 * 60 * 1000l;

        bean.setAlertProfile("Chilled Beef");
        bean.setStandardDevitation(0.001 + random.nextDouble() / 0.5);
        bean.setTotalTime((1 + random.nextInt(3 * 30 * 24)) * oneHour);
        bean.setAvgTemperature(7.);
        bean.getWhoWasNotified().add("user1@smarttrace.com.au");
        bean.getWhoWasNotified().add("user2@smarttrace.com.au");
        bean.getSchedules().add("Schedule 1");
        bean.getSchedules().add("Schedule 2");

        bean.getAlertsFired().add("<0.0°C for 60 min");
        bean.getAlertsFired().add(">0.0°C for 30 min");

        bean.getAlerts().add(createTimeWithLabel(
                "Total time above high temp (5°C)", 72 * 60 * 1000l));
        //Total time above high temp (5°C): 2hrs 12min
        bean.getAlerts().add(createTimeWithLabel(
                "Total time above critical high temp (8°C)", 42 * 60 * 1000l));
        //Total time above critical high temp (8°C): 1hrs 12min
        bean.getAlerts().add(createTimeWithLabel(
                "Total time below low temp (0°C)", 22 * 60 * 1000l));
        //Total time below low temp (0°C): 22min
        bean.getAlerts().add(createTimeWithLabel(
                "Total time below low temp (0°C)", 72 * 60 * 1000l));
        //Total time below critical low temp (-2C): nil
        bean.getAlerts().add(createTimeWithLabel(
                "Total time below critical low temp (-2C)", 0l));

        return bean;
    }

    /**
     * @param label
     * @param time
     * @return
     */
    private static TimeWithLabel createTimeWithLabel(final String label, final long time) {
        final TimeWithLabel tl = new TimeWithLabel();
        tl.setLabel(label);
        tl.setTotalTime(time);
        return tl;
    }

    /**
     * @param bean
     * @param user
     * @throws DRException
     * @throws IOException
     */
    public static void showShipmentReport(final ShipmentReportBean bean, final User user)
            throws DRException, IOException {
        final ShipmentReportBuilder builder = new ShipmentReportBuilder();
        builder.createReport(bean, user).show();
    }

    public static void main(final String[] args) throws Exception {
        showShipmentReport(createPerformanceBean(), createUser());
    }
}
