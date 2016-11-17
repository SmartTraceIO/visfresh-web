/**
 *
 */
package com.visfresh.reports.performance;

import static com.visfresh.utils.DateTimeUtils.getTimeRanges;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.visfresh.dao.impl.TimeAtom;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.User;
import com.visfresh.reports.TemperatureStats;

import net.sf.dynamicreports.report.exception.DRException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class PerformanceReportBuilderTool {
    private static final Random random = new Random();
    private static final AlertType[] alertTypes = {
        AlertType.Hot,
        AlertType.CriticalHot,
        AlertType.Cold,
        AlertType.CriticalCold
    };
    private static final String[] serialNums = {"123", "324", "673", "257"};
    private static int count;

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
        builder.createReport(bean, user).show();
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
        final TimeAtom atom = TimeAtom.Month;

        final PerformanceReportBean bean = new PerformanceReportBean();
        bean.setCompanyName("SmartTrace");
        bean.setDate(new Date());
        bean.setTimeAtom(atom);

        bean.getAlertProfiles().add(createAlertProfile("Chilled Beef", atom));
        bean.getAlertProfiles().add(createAlertProfile("Chilled Wine", atom));
        return bean;
    }

    /**
     * @param name alert profile name.
     * @return random generated alert profile stats.
     */
    private static AlertProfileStats createAlertProfile(final String name, final TimeAtom atom) {
        final AlertProfileStats ap = new AlertProfileStats();
        ap.setName(name);
        final long time = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            if (i < 2) {
                ap.getTimedData().add(0, generateTimePeriodData(new Date(time - i * 28 * 24 * 60 * 60 * 1000l), atom));
                ap.getTemperatureExceptions().add(generateException(serialNums[i]));
            } else {
                final TimeRangesTemperatureStats stats = new TimeRangesTemperatureStats(
                        getTimeRanges(time - i * 28 * 24 * 60 * 60 * 1000l, atom));
                stats.getTemperatureStats().setTotalTime(10 * 60 * 1000);
                stats.getTemperatureStats().setTimeAboveUpperLimit(10 * 60 * 1000);
                ap.getTimedData().add(0, stats);
            }
        }

        return ap;
    }

    /**
     * @return
     */
    protected static BiggestTemperatureException generateException(final String sn) {
        final BiggestTemperatureException exc = new BiggestTemperatureException();
        exc.setSerialNumber(sn);
        exc.setTripCount(random.nextInt(17));
        exc.setShippedTo("Moscow");
        exc.setDateShipped(new Date());

        final List<AlertType> rules = new LinkedList<AlertType>(Arrays.asList(alertTypes));
        for (int i = 0; i < 3; i++) {
            final AlertType t = rules.remove(random.nextInt(rules.size()));
            final TemperatureRule rule = new TemperatureRule(t);
            rule.setCumulativeFlag(random.nextBoolean());
            rule.setTemperature(random.nextInt(10) - 3);
            rule.setTimeOutMinutes(1 + random.nextInt(25));
        }

        exc.getAlertsFired();
        return exc;
    }

    /**
     * @param date
     * @return
     */
    private static TimeRangesTemperatureStats generateTimePeriodData(final Date date, final TimeAtom atom) {
        final long oneHour = 60 * 60 * 1000l;

        final TimeRangesTemperatureStats ms = new TimeRangesTemperatureStats(getTimeRanges(date.getTime(), atom));
        ms.setNumShipments(29);
        ms.setNumExcludedHours(23);

        final TemperatureStats stats = new TemperatureStats();
        ms.setTemperatureStats(stats);

        stats.setAvgTemperature(3.);
        stats.setMaximumTemperature(5.);
        stats.setMinimumTemperature(-1.);
        stats.setStandardDevitation(2.1);
        stats.setTotalTime(345 * oneHour);
        stats.setTimeAboveUpperLimit(11 * oneHour + oneHour / 2);
        stats.setTimeBelowLowerLimit(12 * oneHour);

        final int n = count % 4;
        if (n != 0) {
            ms.getAlertStats().setColdAlerts(5);
        }
        if (n != 1) {
            ms.getAlertStats().setHotAlerts(7);
        }
        if (n != 2) {
            ms.getAlertStats().setHotAndColdAlerts(2);
        }
        if (n != 3) {
            ms.getAlertStats().setNotAlerts(15);
        }
        count++;

        return ms;
    }
}
