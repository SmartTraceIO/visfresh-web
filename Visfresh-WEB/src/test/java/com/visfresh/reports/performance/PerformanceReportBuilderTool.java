/**
 *
 */
package com.visfresh.reports.performance;

import java.io.IOException;
import java.util.Date;
import java.util.Random;

import net.sf.dynamicreports.report.exception.DRException;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class PerformanceReportBuilderTool {

    /**
     * Default constructor.
     */
    private PerformanceReportBuilderTool() {
        super();
    }

    /**
     * @param bean
     * @param user
     * @throws DRException
     * @throws IOException
     */
    public static void showPerformanceReport(final PerformanceReportBean bean, final User user)
            throws DRException, IOException {
        final PerformanceReportBuilder builder = new PerformanceReportBuilder();
        builder.createPerformanceReport(bean, user).show();
    }

    public static void main(final String[] args) throws Exception {
        showPerformanceReport(createPerformanceBean(), createUser());
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
    private static PerformanceReportBean createPerformanceBean() {
        final PerformanceReportBean bean = new PerformanceReportBean();

        bean.setStartDate(new Date(System.currentTimeMillis() - 10000000000l));
        bean.setEndDate(new Date());
        bean.setNumberOfShipments(212);
        bean.setNumberOfTrackers(122);
        bean.setAvgShipmentsPerTracker(2.2);
        bean.setAvgTrackersPerShipment(1.4);

        bean.getAlertProfiles().add(createAlertProfile("Chilled Beef"));
        bean.getAlertProfiles().add(createAlertProfile("Chilled Wine"));
        return bean;
    }

    /**
     * @param name alert profile name.
     * @return random generated alert profile stats.
     */
    private static AlertProfileStats createAlertProfile(final String name) {
        final AlertType[] types = {AlertType.Hot, AlertType.CriticalHot, AlertType.Cold, AlertType.CriticalCold};
        final String[] serialNums = {"123", "324", "673", "257"};

        final Random random = new Random();
        final long oneHour = 60 * 60 * 1000l;

        final AlertProfileStats ap = new AlertProfileStats();
        ap.setAvgTemperature((random.nextDouble() - 0.5) * 20.);
        ap.setName(name);
        ap.setStandardDeviation(0.001 + random.nextDouble() / 0.5);
        ap.setTotalMonitoringTime((1 + random.nextInt(3 * 30 * 24)) * oneHour);

        final int numRules = 3 + random.nextInt(7);
        for (int i = 0; i < numRules; i++) {
            final TemperatureRuleStats rule = new TemperatureRuleStats();
            rule.setTotalTime((3 + random.nextInt(15)) * oneHour);

            //create temperature rule
            final TemperatureRule tr = new TemperatureRule();
            tr.setType(types[random.nextInt(types.length)]);
            tr.setTemperature((random.nextDouble() - 0.5) * 20.);
            tr.setCumulativeFlag(random.nextBoolean());
            tr.setTimeOutMinutes((3 + random.nextInt(15)) * 60);
            rule.setRule(tr);

            //add biggest exceptions
            final int numBidgest = random.nextInt(5);
            for (int j = 0; j < numBidgest; j++) {
                final BiggestTemperatureException b = new BiggestTemperatureException();
                b.setSerialNumber(serialNums[random.nextInt(serialNums.length)]);
                b.setTripCount(1 + random.nextInt(4));
                b.setTime((3 + random.nextInt(5)) * oneHour);
                rule.getBiggestExceptions().add(b);
            }

            ap.getTemperatureRules().add(rule);
        }

        return ap;
    }
}
