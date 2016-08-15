/**
 *
 */
package com.visfresh.reports.shipment;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import net.sf.dynamicreports.report.exception.DRException;

import com.visfresh.entities.AlertType;
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

        bean.setCompanyName("Test Company");
        bean.setAssetNum("Test Asset");
        bean.setComment("Test bean for development");
        bean.setDateArrived(new Date(System.currentTimeMillis() - 100000000));
        bean.setDateShipped(new Date(System.currentTimeMillis() - 1000000000));
        bean.setDescription("Autostarted by rule");
        bean.setDevice("1920387091287439");
        bean.setNumberOfSiblings(3);
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

        //generate events
        //Sidnay -33°52′10″ 151°12′30″
        final double lat0 = -33. + 52 / 60.;
        final double lon0 = 151. + 12 / 60.;

        //Carnarvon 24°52′02″ 113°39′40″
        final double lat1 = 24. + 52 / 60.;
        final double lon1 = 113. + 39 / 60.;

        final int numReadings = 150;
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
        bean.setMinimumTemperature(-2);
        bean.setLowerTemperatureLimit(0);
        bean.setTimeAboveUpperLimit(10 * 61 * 60 * 1000L);
        bean.setTimeBelowLowerLimit(2 * 61 * 60 * 1000L);
        bean.setMaximumTemperature(11);
        bean.setUpperTemperatureLimit(9);
        bean.setAvgTemperature(7.);
        bean.getWhoWasNotified().add("user1@smarttrace.com.au");
        bean.getWhoWasNotified().add("user2@smarttrace.com.au");

        bean.getAlertsFired().add(new AlertBean(AlertType.Cold, "<0.0°C for 60 min"));
        bean.getAlertsFired().add(new AlertBean(AlertType.Hot, ">0.0°C for 30 min"));

        return bean;
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
