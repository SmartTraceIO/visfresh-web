/**
 *
 */
package com.visfresh.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.PerformanceReportDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.reports.performance.AlertProfileStats;
import com.visfresh.reports.performance.BiggestTemperatureException;
import com.visfresh.reports.performance.MonthlyTemperatureStats;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.performance.ReportsWithAlertStats;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class PerformanceReportDaoImpl implements PerformanceReportDao {
    private static final String HAS_HOT = "hasHot";
    private static final String HAS_COLD = "hasCold";

    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private AlertDao alertDao;
    @Autowired
    private AlertProfileDao alertProfileDao;

    /**
     * Default constructor.
     */
    public PerformanceReportDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.PerformanceReportDao#createReport(com.visfresh.entities.Company, java.util.Date, java.util.Date)
     */
    @Override
    public PerformanceReportBean createReport(final Company c, final Date startDate, final Date endDate) {
        final PerformanceReportBean bean = new PerformanceReportBean();
        bean.setDate(new Date());

        final Map<Long, AlertProfileStats> statsMap = createInitialStatsMap(c, startDate, endDate);
        final Map<Long, List<TemperatureAlert>> shipmentAlerts = new HashMap<>();

        addShipmentStats(c, startDate, endDate, statsMap, shipmentAlerts);

        //add collector to each alert profile
        final Map<Long, Map<Integer, TemperatureStatsCollector>> profileByMonthCollectors = new HashMap<>();
        final Map<Long, TemperatureStatsCollector> shipmentCollectors = new HashMap<>();
        final Map<Long, Shipment> shipmentMap = new HashMap<>();

        final Calendar calendar = new GregorianCalendar();

        final Filter filter = createFilter(c, startDate, endDate);
        final Sorting sorting = new Sorting("time", "id");

        int page = 1;
        List<TrackerEvent> events;

        //should calculate two statistics temperature statistics for alert profile
        //and also for shipment for detect
        while(!(events = trackerEventDao.findAll(filter, sorting, new Page(page, 1000))).isEmpty()) {
            for (final TrackerEvent e : events) {
                if (e.getShipment() != null && e.getShipment().getAlertProfile() != null) {
                    if (!shipmentMap.containsKey(e.getShipment().getId())) {
                        shipmentMap.put(e.getShipment().getId(), e.getShipment());
                    }

                    final Long alertProfileId = e.getShipment().getAlertProfile().getId();

                    Map<Integer, TemperatureStatsCollector> monthlyCollectors = profileByMonthCollectors.get(alertProfileId);
                    if (monthlyCollectors == null) {
                        //add collectors map to profile/months map
                        monthlyCollectors = new HashMap<>();
                        profileByMonthCollectors.put(alertProfileId, new HashMap<Integer, TemperatureStatsCollector>());
                    }

                    //get month of date
                    calendar.setTime(e.getTime());
                    final int monthKey = calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH);

                    TemperatureStatsCollector collector = monthlyCollectors.get(monthKey);
                    if (collector == null) {
                        collector = new TemperatureStatsCollector();
                        monthlyCollectors.put(monthKey, collector);
                    }

                    //process event by monthly collector
                    collector.processEvent(e);

                    //shipment stats
                    TemperatureStatsCollector sc = shipmentCollectors.get(e.getShipment().getId());
                    if (sc == null) {
                        sc = new TemperatureStatsCollector();
                        shipmentCollectors.put(e.getShipment().getId(), sc);
                    }

                    sc.processEvent(e);
                }
            }

            page++;
        }

        //merge alert profile stats
        for (final Map.Entry<Long, AlertProfileStats> e : statsMap.entrySet()) {
            //merge monthly data
            final Map<Integer, TemperatureStatsCollector> monthlyCollectors = profileByMonthCollectors.get(e.getKey());
            for (final MonthlyTemperatureStats monthlyStats : e.getValue().getMonthlyData()) {
                //get monthly key
                calendar.setTime(monthlyStats.getMonth());
                final int monthKey = calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH);

                final TemperatureStatsCollector collector = monthlyCollectors.get(monthKey);
                if (collector != null) {
                    monthlyStats.setTemperatureStats(collector.applyStatistics());
                }
            }

            //merge shipment data
            final Map<Long, TemperatureStats> shipmentStats = getShipmentStatsForProfile(
                    e.getKey(), shipmentMap, shipmentCollectors);
            final List<Long> orderedShipmentIds = getOrderByMaxIncedents(shipmentStats);

            for (int i = 0; i < 3 && !shipmentStats.isEmpty(); i++) {
                final Shipment shipment = shipmentMap.get(orderedShipmentIds.remove(0));

                final BiggestTemperatureException exc = new BiggestTemperatureException();
                e.getValue().getTemperatureExceptions().add(exc);

                exc.setDateShipped(shipment.getShipmentDate());
                exc.setSerialNumber(shipment.getDevice().getSn());
                exc.setTripCount(shipment.getTripCount());
                if (shipment.getShippedTo() != null) {
                    exc.setShippedTo(shipment.getShippedTo().getName());
                }

                final TemperatureStats stats = shipmentStats.get(shipment.getId());
                if (stats != null) {
                    exc.setTemperatureStats(stats);
                }

                final List<TemperatureAlert> listShipmentAlerts = shipmentAlerts.get(shipment.getId());
                if (listShipmentAlerts != null) {
                    exc.getAlertsFired().addAll(createTemperatureRules(listShipmentAlerts));
                }
            }
        }

        bean.getAlertProfiles().addAll(statsMap.values());
        return bean;
    }

    /**
     * @param shipmentStats
     * @return
     */
    private List<Long> getOrderByMaxIncedents(
            final Map<Long, TemperatureStats> shipmentStats) {
        final List<Long> result = new LinkedList<Long>(shipmentStats.keySet());
        Collections.sort(result, new Comparator<Long>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final Long id1, final Long id2) {
                final TemperatureStats st1 = shipmentStats.get(id1);
                final TemperatureStats st2 = shipmentStats.get(id2);
                return new Long(st2.getTimeBelowLowerLimit() + st2.getTimeAboveUpperLimit()).compareTo(
                        new Long(st1.getTimeBelowLowerLimit() + st1.getTimeAboveUpperLimit()));
            }
        });

        return result;
    }

    /**
     * @param alertProfileId
     * @param shipmentMap
     * @param shipmentCollectors
     * @return
     */
    private Map<Long, TemperatureStats> getShipmentStatsForProfile(final Long alertProfileId,
            final Map<Long, Shipment> shipmentMap,
            final Map<Long, TemperatureStatsCollector> shipmentCollectors) {
        final Map<Long, TemperatureStats> result = new HashMap<>();
        for (final Long id : shipmentCollectors.keySet()) {
            final Shipment s = shipmentMap.get(id);
            if (s.getAlertProfile().getId().equals(alertProfileId)) {
                result.put(s.getId(), shipmentCollectors.get(id).applyStatistics());
            }
        }

        return result;
    }

    /**
     * @param shipment
     * @param listShipmentAlerts
     * @return
     */
    private Set<TemperatureRule> createTemperatureRules(final List<TemperatureAlert> listShipmentAlerts) {
        final Set<TemperatureRule> rules = new HashSet<>();
        for (final TemperatureAlert a : listShipmentAlerts) {
            final Shipment shipment = a.getShipment();
            TemperatureRule rule = null;

            for (final TemperatureRule r : shipment.getAlertProfile().getAlertRules()) {
                if (r.getId().equals(a.getRuleId())) {
                    rule = r;
                    break;
                }
            }

            rules.add(rule);
        }
        return rules;
    }

    /**
     * @param c
     * @param startDate
     * @param endDate
     * @return
     */
    protected Map<Long, AlertProfileStats> createInitialStatsMap(
            final Company c, final Date startDate, final Date endDate) {
        final List<Date> monthDates = createMonthDateList(startDate, endDate);

        //Warning!!! the number of shipments per alert profile and month
        //should be added after
        final Map<Long, AlertProfileStats> statsMap = new HashMap<>();
        for (final AlertProfile ap: alertProfileDao.findByCompany(c, null, null, null)) {
            final AlertProfileStats s = new AlertProfileStats();
            s.setName(ap.getName());

            //add monthly data
            for (final Date date : monthDates) {
                final MonthlyTemperatureStats ms = new MonthlyTemperatureStats(date);
                s.getMonthlyData().add(ms);
            }

            statsMap.put(ap.getId(), s);
        }
        return statsMap;
    }

    /**
     * @param startDate
     * @param endDate
     * @return
     */
    private List<Date> createMonthDateList(final Date startDate, final Date endDate) {
        final List<Date> months = new LinkedList<>();

        Date month = getMiddleOfMonth(startDate);
        final Date endMonth = getMiddleOfMonth(endDate);

        while (!month.after(endMonth)) {
            months.add(month);
            month = getMiddleOfMonth(new Date(month.getTime() + 25 * 24 * 60 * 60 * 1000l));
        }

        return months;
    }

    /**
     * @param date
     * @return
     */
    private Date getMiddleOfMonth(final Date date) {
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        final int day = (calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                - calendar.getActualMinimum(Calendar.DAY_OF_MONTH)) / 2;

        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * @param bean bean to populate
     * @param c company
     * @param startDate start date.
     * @param endDate end date.
     * @param shipmentAlerts
     */
    public void addShipmentStats(final Company c,
            final Date startDate, final Date endDate, final Map<Long, AlertProfileStats> result,
            final Map<Long, List<TemperatureAlert>> shipmentAlerts) {
        //create year+month formatter.
        final DateFormat fmt = new SimpleDateFormat("yyyyMM");

        //alertProfileId
        //   year+month
        //      shipment
        //          hotAlerts
        //          coldAlerts
        final Map<Long, Map<String, Map<Long, Map<String, Boolean>>>> data = new HashMap<>();
        for (final Map.Entry<Long, AlertProfileStats> e : result.entrySet()) {
            data.put(e.getKey(), new HashMap<String, Map<Long, Map<String, Boolean>>>());
        }

        final Filter f = createFilterByDateRanges(c, startDate, endDate, "alerts", "date");
        final List<Alert> alerts = alertDao.findAll(f, null, null);

        for (final Alert alert : alerts) {
            if (alert.getShipment() != null && alert instanceof TemperatureAlert) {
                //alert profile scope
                final Long alertProfileId = alert.getShipment().getAlertProfile().getId();

                final Map<String, Map<Long, Map<String, Boolean>>> alertProfileData = data.get(alertProfileId);
                //monthly scope
                final String yearMonth = fmt.format(alert.getDate());

                if (!alertProfileData.containsKey(yearMonth)) {
                    alertProfileData.put(yearMonth, new HashMap<Long, Map<String, Boolean>>());
                }

                final Map<Long, Map<String, Boolean>> monthlyData = alertProfileData.get(yearMonth);

                //shipment scope
                final Long shipmentId = alert.getShipment().getId();
                if (!monthlyData.containsKey(shipmentId)) {
                    monthlyData.put(shipmentId, new HashMap<String, Boolean>());
                }

                final Map<String, Boolean> shipmentData = monthlyData.get(shipmentId);

                switch (alert.getType()) {
                    case Cold:
                    case CriticalCold:
                        shipmentData.put(HAS_COLD, Boolean.TRUE);
                        break;
                    case Hot:
                    case CriticalHot:
                        shipmentData.put(HAS_HOT, Boolean.TRUE);
                        break;
                        default:
                            //nothing
                }

                //add alert to shipment data
                List<TemperatureAlert> sa = shipmentAlerts.get(shipmentId);
                if (sa == null) {
                    sa = new LinkedList<>();
                    shipmentAlerts.put(shipmentId, sa);
                }

                sa.add((TemperatureAlert) alert);
            }
        }

        //convert accepted data to stats
        for (final Map.Entry<Long, Map<String, Map<Long, Map<String, Boolean>>>> e: data.entrySet()) {
            final AlertProfileStats s = result.get(e.getKey());
            for (final MonthlyTemperatureStats monthlyStats : s.getMonthlyData()) {
                final String monthKey = fmt.format(monthlyStats.getMonth());

                final Map<Long, Map<String, Boolean>> shipments = e.getValue().get(monthKey);
                //if found any alerts.
                if (shipments != null) {
                    //process shipments
                    for (final Map<String, Boolean> shipment : shipments.values()) {
                        final boolean hasCold = Boolean.TRUE == shipment.get(HAS_COLD);
                        final boolean hasHot = Boolean.TRUE == shipment.get(HAS_HOT);

                        final ReportsWithAlertStats as = monthlyStats.getAlertStats();

                        if (hasCold && hasHot) {
                            as.setHotAndColdAlerts(as.getHotAndColdAlerts() + 1);
                        } else if (hasCold) {
                            as.setColdAlerts(as.getColdAlerts() + 1);
                        } else if (hasHot) {
                            as.setHotAlerts(as.getHotAlerts() + 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param c company.
     * @param startDate start date.
     * @param endDate end date.
     * @return filter.
     */
    private Filter createFilter(final Company c, final Date startDate, final Date endDate) {
        return createFilterByDateRanges(c, startDate, endDate, "trackerevents", "time");
    }
    /**
     * @param c
     * @param startDate
     * @param endDate
     * @param tableFielName
     * @param dateFieldName
     * @return
     */
    protected Filter createFilterByDateRanges(final Company c,
            final Date startDate, final Date endDate,
            final String tableFielName, final String dateFieldName) {
        final Filter f = new Filter();
        f.addFilter("company", c);

        //start date.
        final String endDateProp = "enDate";
        f.addFilter(endDateProp, new SynteticFilter() {
            @Override
            public Object[] getValues() {
                return new Object[] {endDate};
            }

            @Override
            public String[] getKeys() {
                return new String[] {endDateProp};
            }

            @Override
            public String getFilter() {
                return tableFielName + "." + dateFieldName + " >= :" + endDateProp;
            }
        });

        //end date.
        final String startDateProp = "startDate";
        f.addFilter(startDateProp, new SynteticFilter() {
            @Override
            public Object[] getValues() {
                return new Object[] {startDate};
            }

            @Override
            public String[] getKeys() {
                return new String[] {startDateProp};
            }

            @Override
            public String getFilter() {
                return tableFielName + "." + dateFieldName + " >= :" + startDateProp;
            }
        });

        return f;
    }
}
