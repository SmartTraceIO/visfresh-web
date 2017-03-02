/**
 *
 */
package com.visfresh.dao.impl;

import static com.visfresh.utils.DateTimeUtils.getTimeRanges;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.PerformanceReportDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.reports.TemperatureStats;
import com.visfresh.reports.performance.AlertProfileStats;
import com.visfresh.reports.performance.BiggestTemperatureException;
import com.visfresh.reports.performance.PerformanceReportBean;
import com.visfresh.reports.performance.ReportsWithAlertStats;
import com.visfresh.reports.performance.TimeRangesTemperatureStats;
import com.visfresh.utils.EntityUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class PerformanceReportDaoImpl implements PerformanceReportDao {
    private static final String HAS_HOT = "hasHot";
    private static final String HAS_COLD = "hasCold";

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
    public PerformanceReportBean createReport(final Company c, final Date startDate, final Date endDate,
            final TimeAtom timeAtom, final LocationProfile location) {
        final List<TimeRanges> timeRanges = divideToTimeRanges(startDate, endDate, timeAtom);
        final PerformanceReportBean bean = new PerformanceReportBean();
        bean.setDate(new Date());
        bean.setCompanyName(c.getName());
        bean.setTimeAtom(timeAtom);

        final Map<Long, AlertProfileStats> statsMap = createInitialStatsMap(c, startDate, endDate, timeRanges);
        final Map<Long, List<TemperatureAlert>> shipmentAlerts = new HashMap<>();

        addShipmentStats(c, startDate, endDate, statsMap, shipmentAlerts, timeAtom, location);

        //add collector to each alert profile
        final Map<Long, Map<TimeRanges, AlertProfileTemperatureStatsCollector>> profileByTimeRangesCollectors = new HashMap<>();
        final Map<Long, AlertProfileTemperatureStatsCollector> shipmentCollectors = new HashMap<>();
        final Map<Long, Shipment> shipmentMap = new HashMap<>();

        int page = 1;
        List<TrackerEvent> events;

        //should calculate two statistics temperature statistics for alert profile
        //and also for shipment for detect
        while(!(events = trackerEventDao.findForArrivedShipmentsInDateRanges(
                c, startDate, endDate, location, new Page(page, 1000))).isEmpty()) {
            for (final TrackerEvent e : events) {
                if (e.getShipment() != null && e.getShipment().getAlertProfile() != null) {
                    if (!shipmentMap.containsKey(e.getShipment().getId())) {
                        shipmentMap.put(e.getShipment().getId(), e.getShipment());
                    }

                    final Long alertProfileId = e.getShipment().getAlertProfile().getId();

                    Map<TimeRanges, AlertProfileTemperatureStatsCollector> rimeRangesCollectors = profileByTimeRangesCollectors.get(alertProfileId);
                    if (rimeRangesCollectors == null) {
                        //add collectors map to profile/time ranges stats map
                        rimeRangesCollectors = new HashMap<>();
                        profileByTimeRangesCollectors.put(alertProfileId, rimeRangesCollectors);
                    }

                    final TimeRanges key = getRangesForDate(e.getTime(), timeRanges);

                    AlertProfileTemperatureStatsCollector collector = rimeRangesCollectors.get(key);
                    if (collector == null) {
                        collector = new AlertProfileTemperatureStatsCollector();
                        rimeRangesCollectors.put(key, collector);
                    }

                    //process event by time ranges collector
                    collector.processEvent(e);

                    //shipment stats
                    AlertProfileTemperatureStatsCollector sc = shipmentCollectors.get(e.getShipment().getId());
                    if (sc == null) {
                        sc = new AlertProfileTemperatureStatsCollector();
                        shipmentCollectors.put(e.getShipment().getId(), sc);
                    }

                    sc.processEvent(e);
                }
            }

            page++;
        }

        //merge alert profile stats
        for (final Map.Entry<Long, AlertProfileStats> e : statsMap.entrySet()) {
            //merge time ranges data
            final Map<TimeRanges, AlertProfileTemperatureStatsCollector> timeRangesCollectors = profileByTimeRangesCollectors.get(e.getKey());
            for (final TimeRangesTemperatureStats timeRangesStats : e.getValue().getTimedData()) {
                if (timeRangesCollectors == null) {
                    continue;
                }

                final AlertProfileTemperatureStatsCollector collector = timeRangesCollectors.get(timeRangesStats.getTimeRanges());
                if (collector != null) {
                    timeRangesStats.setTemperatureStats(collector.getStatistics());
                    final int numShipments = collector.getDetectedShipments().size();
                    timeRangesStats.setNumShipments(numShipments);

                    //set shipments without alerts
                    final ReportsWithAlertStats alertStats = timeRangesStats.getAlertStats();
                    alertStats.setNotAlerts(numShipments - alertStats.getColdAlerts()
                            - alertStats.getHotAlerts() - alertStats.getHotAndColdAlerts());
                }
            }

            //merge shipment data
            final Map<Long, TemperatureStats> shipmentStats = getShipmentStatsForProfile(
                    e.getKey(), shipmentMap, shipmentCollectors);
            final List<Long> orderedShipmentIds = getOrderByMaxIncedents(shipmentStats);

            while(e.getValue().getTemperatureExceptions().size() < 3 && !orderedShipmentIds.isEmpty()) {
                final Shipment shipment = shipmentMap.get(orderedShipmentIds.remove(0));
                final TemperatureStats stats = shipmentStats.get(shipment.getId());

                if (stats != null && (stats.getTimeAboveUpperLimit() > 0 || stats.getTimeBelowLowerLimit() > 0)) {
                    final BiggestTemperatureException exc = new BiggestTemperatureException();
                    exc.setTemperatureStats(stats);
                    e.getValue().getTemperatureExceptions().add(exc);

                    exc.setDateShipped(shipment.getShipmentDate());
                    exc.setSerialNumber(shipment.getDevice().getSn());
                    exc.setTripCount(shipment.getTripCount());
                    if (shipment.getShippedTo() != null) {
                        exc.setShippedTo(shipment.getShippedTo().getName());
                    }

                    final List<TemperatureAlert> listShipmentAlerts = shipmentAlerts.get(shipment.getId());
                    if (listShipmentAlerts != null) {
                        exc.getAlertsFired().addAll(EntityUtils.getTemperatureRules(listShipmentAlerts));
                    }
                }
            }
        }

        bean.getAlertProfiles().addAll(statsMap.values());
        return bean;
    }

    /**
     * @param startDate
     * @param endDate
     * @param timeAtom
     * @return
     */
    private List<TimeRanges> divideToTimeRanges(final Date startDate, final Date endDate, final TimeAtom timeAtom) {
        final List<TimeRanges> ranges = new LinkedList<>();

        TimeRanges r = getTimeRanges(startDate.getTime(), timeAtom);
        final TimeRanges endRanges = getTimeRanges(endDate.getTime(), timeAtom);

        while (!r.after(endRanges)) {
            ranges.add(r);

            r = getTimeRanges(r.getEndTime() +  (r.getEndTime() - r.getStartTime()) / 2, timeAtom);
        }

        return ranges;
    }
    /**
     * @param time
     * @param timeRanges
     * @return
     */
    private TimeRanges getRangesForDate(final Date time, final List<TimeRanges> timeRanges) {
        final long t = time.getTime();
        for (final TimeRanges r : timeRanges) {
            if (r.contains(t)) {
                return r;
            }
        }
        return null;
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
            final Map<Long, AlertProfileTemperatureStatsCollector> shipmentCollectors) {
        final Map<Long, TemperatureStats> result = new HashMap<>();
        for (final Long id : shipmentCollectors.keySet()) {
            final Shipment s = shipmentMap.get(id);
            if (s.getAlertProfile().getId().equals(alertProfileId)) {
                result.put(s.getId(), shipmentCollectors.get(id).getStatistics());
            }
        }

        return result;
    }

    /**
     * @param c
     * @param startDate
     * @param endDate
     * @return
     */
    protected Map<Long, AlertProfileStats> createInitialStatsMap(
            final Company c, final Date startDate, final Date endDate, final List<TimeRanges> timeRanges) {
        //Warning!!! the number of shipments per alert profile and time ranges
        //should be added after
        final Map<Long, AlertProfileStats> statsMap = new HashMap<>();
        for (final AlertProfile ap: alertProfileDao.findByCompany(c, null, null, null)) {
            final AlertProfileStats s = new AlertProfileStats();
            s.setName(ap.getName());
            s.setLowerTemperatureLimit(ap.getLowerTemperatureLimit());
            s.setUpperTemperatureLimit(ap.getUpperTemperatureLimit());

            //add time ranges data
            for (final TimeRanges r : timeRanges) {
                final TimeRangesTemperatureStats ms = new TimeRangesTemperatureStats(r);
                s.getTimedData().add(ms);
            }

            statsMap.put(ap.getId(), s);
        }
        return statsMap;
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
            final Map<Long, List<TemperatureAlert>> shipmentAlerts, final TimeAtom atom, final LocationProfile loc) {
        //alertProfileId
        //   timeRanges
        //      shipment
        //          hotAlerts
        //          coldAlerts
        final Map<Long, Map<TimeRanges, Map<Long, Map<String, Boolean>>>> data = new HashMap<>();
        for (final Map.Entry<Long, AlertProfileStats> e : result.entrySet()) {
            data.put(e.getKey(), new HashMap<TimeRanges, Map<Long, Map<String, Boolean>>>());
        }

        final List<Alert> alerts = alertDao.getAlerts(c, startDate, endDate);

        for (final Alert alert : alerts) {
            final Shipment shipment = alert.getShipment();
            if (shipment != null
                    && shipment.getStatus() == ShipmentStatus.Arrived
                    && (loc == null || (shipment.getShippedTo() != null && shipment.getShippedTo().getId().equals(loc.getId())))
                    && alert instanceof TemperatureAlert) {
                //alert profile scope
                final Long alertProfileId = shipment.getAlertProfile().getId();

                final Map<TimeRanges, Map<Long, Map<String, Boolean>>> alertProfileData = data.get(alertProfileId);
                if (alertProfileData == null) {
                    //Please not remove this block. The alert profile can be moved to another company
                    //manually, therefore shipment can have alert profile from left company
                    continue;
                }

                //time ranges scope
                final TimeRanges ranges = getTimeRanges(alert.getDate().getTime(), atom);
                if (!alertProfileData.containsKey(ranges)) {
                    alertProfileData.put(ranges, new HashMap<Long, Map<String, Boolean>>());
                }

                final Map<Long, Map<String, Boolean>> timeRangesData = alertProfileData.get(ranges);

                //shipment scope
                final Long shipmentId = shipment.getId();
                if (!timeRangesData.containsKey(shipmentId)) {
                    timeRangesData.put(shipmentId, new HashMap<String, Boolean>());
                }

                final Map<String, Boolean> shipmentData = timeRangesData.get(shipmentId);

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
        for (final Map.Entry<Long, Map<TimeRanges, Map<Long, Map<String, Boolean>>>> e: data.entrySet()) {
            final AlertProfileStats s = result.get(e.getKey());
            for (final TimeRangesTemperatureStats timeRangesStats : s.getTimedData()) {
                final TimeRanges key = timeRangesStats.getTimeRanges();

                final Map<Long, Map<String, Boolean>> shipments = e.getValue().get(key);
                //if found any alerts.
                if (shipments != null) {
                    //process shipments
                    for (final Map<String, Boolean> shipment : shipments.values()) {
                        final boolean hasCold = Boolean.TRUE == shipment.get(HAS_COLD);
                        final boolean hasHot = Boolean.TRUE == shipment.get(HAS_HOT);

                        final ReportsWithAlertStats as = timeRangesStats.getAlertStats();

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
}
