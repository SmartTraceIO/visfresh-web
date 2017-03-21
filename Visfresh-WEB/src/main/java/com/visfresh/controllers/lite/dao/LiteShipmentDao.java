/**
 *
 */
package com.visfresh.controllers.lite.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.controllers.lite.LiteKeyLocation;
import com.visfresh.controllers.lite.LiteShipment;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.impl.SelectAllSupport;
import com.visfresh.dao.impl.ShipmentDaoImpl;
import com.visfresh.entities.Company;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LiteShipmentDao {
    @Autowired
    private ShipmentDaoImpl shipmentDao;
    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;
    private int maxShipmentsToProcess = 40;
    private int maxReadingsToProcess = 10000;

    /**
     * Default constructor.
     */
    public LiteShipmentDao() {
        super();
    }

    /**
     * @param company company.
     * @param sorting sorting.
     * @param filter filter.
     * @param page page.
     * @param user
     * @return
     */
    public LiteShipmentResult getShipments(final Company company, final Sorting sorting,
            final Filter filter, final Page page,
            final User user) {
        final List<Map<String, Object>> list = getShipmentsDbData(company, sorting, filter, page);

        //parse result to lite shipment items.
        final LiteShipmentResult result = new LiteShipmentResult();

        for (final Map<String,Object> map : list) {
            final LiteShipment t = createLiteShipment(map);
            result.getResult().add(t);
        }

        //add total entity count
        result.setTotalCount(getTotalCount(company, filter));

        //add key locations
        addKeyLocations(result.getResult());

        return result;
    }

    /**
     * @param company
     * @param filter
     * @return
     */
    protected int getTotalCount(final Company company, final Filter filter) {
        //select shipments using standard query builder
        final SelectAllSupport support = shipmentDao.getSelectAllSupport();
        //add company to filter
        final Filter f = new Filter(filter);
        if (company != null) {
            f.addFilter("company", company.getId());
        }
        support.buildGetCount(f);

        final List<Map<String, Object>> list = jdbc.queryForList(support.getQuery(), support.getParameters());
        return ((Number) list.get(0).get("count")).intValue();
    }

    /**
     * @param company
     * @param sorting
     * @param filter
     * @param page
     * @return
     */
    protected List<Map<String, Object>> getShipmentsDbData(final Company company, final Sorting sorting, final Filter filter, final Page page) {
        //select shipments using standard query builder
        final SelectAllSupport support = shipmentDao.getSelectAllSupport();
        //add company to filter
        final Filter f = new Filter(filter);
        if (company != null) {
            f.addFilter("company", company.getId());
        }

        //build query
        support.buildSelectAll(f, sorting, page);

        final String sql = support.getQuery();
        return jdbc.queryForList(sql, support.getParameters());
    }

    /**
     * @param originShipments
     */
    private void addKeyLocations(final List<LiteShipment> originShipments) {
        final int limit = getMaxShipmentsToProcess();
        int index = 0;
        final int size = originShipments.size();

        while (index < size) {
            //process shipment part
            final List<LiteShipment> part = originShipments.subList(index, Math.min(index + limit, size));
            addKeyLocationsToShipmentPart(part);
            index += limit;
        }
    }
    /**
     * @return max number of shipment to process key locations.
     */
    public int getMaxShipmentsToProcess() {
        return maxShipmentsToProcess;
    }
    /**
     * @param maxShipmentsToProcess the maxShipmentsToProcess to set
     */
    public void setMaxShipmentsToProcess(final int maxShipmentsToProcess) {
        this.maxShipmentsToProcess = maxShipmentsToProcess;
    }

    /**
     * @param shipments
     */
    private void addKeyLocationsToShipmentPart(final List<LiteShipment> shipments) {

        //create set of shipment IDs.
        final Map<Long, LiteShipment> shipmentsById = new HashMap<>();
        shipments.forEach(s -> shipmentsById.put(s.getShipmentId(), s));

        final int max = getMaxReadingsToProcess();

        LiteShipment currentShipment = null;
        final Set<Long> currentIds = new HashSet<>();
        final List<LiteKeyLocation> currentEvents = new LinkedList<>();
        final List<LiteKeyLocation> currentKeyLocations = new LinkedList<>();
        int offset = 0;

        while (shipmentsById.size() > 0) {
            final List<Map<String, Object>> rows = getKeyLocations(shipmentsById.keySet(), offset, max);
            for (final Map<String, Object> row : rows) {
                final Long shipmentId = ((Number) row.get("shipment")).longValue();

                if (currentShipment == null || !currentShipment.getShipmentId().equals(shipmentId)) {
                    if (currentShipment != null) {
                        //add key locations to latest processed shipment using
                        //collected key location info
                        addKeyLocations(currentShipment, currentEvents, currentKeyLocations);

                        //remove the shipment for list of ID
                        shipmentsById.remove(currentShipment.getShipmentId());
                        currentIds.clear();
                        currentEvents.clear();
                        currentKeyLocations.clear();

                        //set next shipment as current shipment
                        offset = 0;
                        currentShipment = shipmentsById.get(shipmentId);
                    }
                }

                //process next event
                final Long id = ((Number) row.get("id")).longValue();
                if (!currentIds.contains(id)) {
                    final LiteKeyLocation loc = createKeyLocation(id, row);
                    currentEvents.add(loc);

                    //if has alerts, should be added to key locations immediately
                    if (row.get("alertType") != null) {
                        currentKeyLocations.add(loc);
                    }
                }
                offset++;
            }

            //break if 0 or <max selected.
            if (rows.size() < max) {
                break;
            }
        }

        if (currentShipment != null) {
            //add key locations to latest processed shipment using
            //collected key location info
            addKeyLocations(currentShipment, currentEvents, currentKeyLocations);
        }
    }
    /**
     * @param ids
     * @param offset
     * @param max
     * @return
     */
    protected List<Map<String, Object>> getKeyLocations(final Set<Long> ids, final int offset, final int max) {
        final String in = "(" + StringUtils.combine(ids, ",") + ")";
        final String query = "select te.id as id, te.temperature as temperature, te.time as time,"
                + " a.type as alertType, te.shipment as shipment from trackerevents te"
                + " left outer join alerts a on a.event = te.id and a.shipment"
                + " in "
                + in
                + " where te.shipment in "
                + in
                + " order by te.shipment, te.time, te.id limit " + offset + "," + max;
        return jdbc.queryForList(query, new HashMap<>());
    }

    /**
     * @return max number of readings to process key locations.
     */
    public int getMaxReadingsToProcess() {
        return maxReadingsToProcess;
    }
    /**
     * @param maxReadingsToProcess the maxReadingsToProcess to set
     */
    public void setMaxReadingsToProcess(final int maxReadingsToProcess) {
        this.maxReadingsToProcess = maxReadingsToProcess;
    }
    /**
     * @param id key location ID.
     * @param row the DB data row.
     * @return key location from row data.
     */
    private LiteKeyLocation createKeyLocation(final Long id, final Map<String, Object> row) {
        final LiteKeyLocation loc = new LiteKeyLocation();
        loc.setId(id);
        loc.setTemperature(((Number) row.get("temperature")).doubleValue());
        loc.setTime((Date) row.get("time"));
        return loc;
    }

    /**
     * @param shipment
     * @param currentEvents
     * @param currentKeyLocations
     */
    private void addKeyLocations(final LiteShipment shipment, final List<LiteKeyLocation> currentEvents,
            final List<LiteKeyLocation> currentKeyLocations) {
        final List<List<LiteKeyLocation>> groups = new LinkedList<>();
        final int maxGroupSize = Math.max(1, currentEvents.size() / currentKeyLocations.size());
        List<LiteKeyLocation> currentGroup = null;

        for (final LiteKeyLocation e : currentEvents) {
            if (currentKeyLocations.remove(e)) {
                //if is already selected as key location, should create separate group for it
                //this group will contain only one element.
                currentGroup = new LinkedList<>();
                groups.add(currentGroup);
                currentGroup.add(e);

                currentGroup = null;
            } else {
                if (currentGroup == null) {
                    currentGroup = new LinkedList<>();
                    groups.add(currentGroup);
                }

                currentGroup.add(e);
                if (currentGroup.size() >= maxGroupSize) {
                    //stop populate current group if group size is equals by
                    //max group size.
                    currentGroup = null;
                }
            }
        }

        //convert groups to key locations
        for (final List<LiteKeyLocation> group : groups) {
            shipment.getKeyLocations().add(getBestKeyLocation(group));
        }
    }

    /**
     * @param group key location group.
     * @return best key location.
     */
    private LiteKeyLocation getBestKeyLocation(final List<LiteKeyLocation> group) {
        if (group.size() == 1) {
            return group.get(0);
        }

        //calculate avg temperature
        double avg = 0;
        for (final LiteKeyLocation e : group) {
            avg += e.getTemperature();
        }

        avg /= group.size();

        //select location with nearest by avg temperature
        final Iterator<LiteKeyLocation> iter = group.iterator();
        LiteKeyLocation loc = iter.next();
        double min = Math.abs(loc.getTemperature() - avg);

        while (iter.hasNext()) {
            final LiteKeyLocation next = iter.next();
            final double currentMin = Math.abs(next.getTemperature() - avg);
            if (currentMin < min) {
                min = currentMin;
                loc = next;
            }
        }

        return loc;
    }

    /**
     * @param map map of DB values.
     * @return lite shipment entity.
     */
    private LiteShipment createLiteShipment(final Map<String, Object> map) {
        final LiteShipment s = new LiteShipment();
        s.setActualArrivalDate((Date) map.get(ShipmentDaoImpl.ARRIVALDATE_FIELD));
        s.setDeviceSN((String) map.get(ShipmentConstants.DEVICE_SN));
        s.setEstArrivalDate((Date) map.get(ShipmentDaoImpl.ETA_FIELD));
        s.setLowerTemperatureLimit(getDouble(map, "lowerTemperatureLimit", 0.));
        s.setShipmentDate((Date) map.get(ShipmentDaoImpl.SHIPMENTDATE_FIELD));
        s.setShipmentId(((Number) map.get(ShipmentDaoImpl.ID_FIELD)).longValue());
        s.setShippedFrom((String) map.get("sfrom"));
        s.setShippedTo((String) map.get("sto"));
        s.setSiblingCount(((Number) map.get(ShipmentDaoImpl.SIBLINGCOUNT_FIELD)).intValue());
        s.setStatus(ShipmentStatus.valueOf((String) map.get(ShipmentDaoImpl.STATUS_FIELD)));
        s.setTripCount(((Number) map.get(ShipmentDaoImpl.SIBLINGCOUNT_FIELD)).intValue());
        s.setUpperTemperatureLimit(getDouble(map, "upperTemperatureLimit", 0.));
        if (s.getEstArrivalDate() != null) {
            s.setPercentageComplete(getPercentageCompleted(s.getShipmentDate(),
                    new Date(), s.getEstArrivalDate()));
        }

        return s;
    }

    private int getPercentageCompleted(final Date shipmentDate,
            final Date currentTime, final Date eta) {
        int percentage;
        if (eta.before(currentTime)) {
            percentage = 100;
        } else {
            double d = currentTime.getTime() - shipmentDate.getTime();
            d = Math.max(0., d / (eta.getTime() - shipmentDate.getTime()));
            percentage = (int) Math.round(d);
        }
        return percentage;
    }

    /**
     * @param map map of DB values.
     * @param name field name.
     * @param defaultValue
     * @return value.
     */
    private double getDouble(final Map<String, Object> map, final String name, final double defaultValue) {
        final Number num = (Number) map.get(name);
        return num == null ? defaultValue : num.doubleValue();
    }
}
