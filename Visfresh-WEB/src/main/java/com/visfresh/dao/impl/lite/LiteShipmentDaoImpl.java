/**
 *
 */
package com.visfresh.dao.impl.lite;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.controllers.lite.LiteKeyLocation;
import com.visfresh.controllers.lite.LiteShipment;
import com.visfresh.controllers.lite.LiteShipmentResult;
import com.visfresh.dao.Filter;
import com.visfresh.dao.LiteShipmentDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.impl.SelectAllSupport;
import com.visfresh.dao.impl.ShipmentDaoImpl;
import com.visfresh.entities.Company;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LiteShipmentDaoImpl implements LiteShipmentDao {
    @Autowired
    private ShipmentDao shipmentDao;
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
    public LiteShipmentDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.LiteShipmentDao#getShipmentsNear(com.visfresh.entities.Company, double, double, int, java.util.Date)
     */
    @Override
    public List<LiteShipment> getShipmentsNearby(final Company company, final double lat, final double lon, final int radius, final Date startDate) {
        final String sql = "select shipments.* ,"
                + " substring(shipments.device, -7, 6) as deviceSN,"
                + " sfrom.name as shippedFromLocationName,"
                + " sto.name as shippedToLocationName,"
                + " te.temperature as lastReadingTemperature,"
                + " te.latitude as lat,"
                + " te.longitude as lon,"
                + " (select count(*) from alerts al where al.shipment = shipments.id) as alertSummary,"
                + " ap.uppertemplimit as upperTemperatureLimit,"
                + " ap.lowertemplimit as lowerTemperatureLimit"
                + " from shipments"
                + " left outer join alertprofiles as ap on shipments.alert = ap.id"
                + " left outer join locationprofiles as sfrom on shipments.shippedfrom = sfrom.id"
                + " left outer join locationprofiles as sto on shipments.shippedto = sto.id"
                // select last event te
                + " left outer join (select t.temperature as temperature,"
                + "    t.shipment as shipment,"
                + "    t.latitude as latitude,"
                + "    t.longitude as longitude,"
                + "    t.time as time from trackerevents t"
                + "    join (select max(id) as id from trackerevents group by shipment) t1 on t1.id = t.id) te"
                + " on te.shipment = shipments.id"
                //end of last event
                + " where shipments.company= :company"
                + (startDate == null ? "" : " and  te.time >= :time ")
                + " and not shipments.istemplate";

        //create parameter map
        final Map<String, Object> params = new HashMap<>();
        params.put("company", company.getId());
        if (startDate != null) {
            params.put("time", startDate);
        }

        //execute query
        final List<Map<String,Object>> rows = jdbc.queryForList(sql, params);

        //convert to objects and filter not near
        final List<LiteShipment> shipments = new LinkedList<>();
        for (final Map<String,Object> map : rows) {
            final Number latObj = (Number) map.get("lat");
            final Number lonObj = (Number) map.get("lon");

            if (latObj != null && lonObj != null) {
                final double dinst = LocationUtils.getDistanceMeters(
                        lat, lon, latObj.doubleValue(), lonObj.doubleValue());
                if (dinst <= radius) {
                    shipments.add(createLiteShipment(map));
                }
            }
        }

        //add key locations
        addKeyLocations(shipments);

        return shipments;
    }
    /**
     * @param company company.
     * @param sorting sorting.
     * @param filter filter.
     * @param page page.
     * @return
     */
    @Override
    public LiteShipmentResult getShipments(final Company company, final Sorting sorting,
            final Filter filter, final Page page) {
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
    protected void addKeyLocationsToShipmentPart(final List<LiteShipment> shipments) {
        //create set of shipment IDs.
        final Map<Long, LiteShipment> shipmentsById = new HashMap<>();
        shipments.forEach(s -> shipmentsById.put(s.getShipmentId(), s));
        final Set<Long> ids = new HashSet<>(shipmentsById.keySet());

        final int max = getMaxReadingsToProcess();
        final AtomicInteger offset = new AtomicInteger();

        final KeyLocationsBuilder builder = new KeyLocationsBuilder();
        builder.setListener((oldId, newId) -> {
            ids.remove(oldId);
            offset.set(0);
        });

        //calculate key locations for shipment group
        while (shipmentsById.size() > 0) {
            final List<Map<String, Object>> rows = getKeyLocations(ids, offset.get(), max);
            for (final Map<String, Object> row : rows) {
                final Long shipmentId = ((Number) row.get("shipment")).longValue();
                final Long id = ((Number) row.get("id")).longValue();
                final Date time = (Date) row.get("time");
                final double temperature = ((Number) row.get("temperature")).doubleValue();
                final boolean hasAlerts = row.get("alertType") != null;

                builder.addNextReading(shipmentsById.get(shipmentId), id, time, temperature, hasAlerts);
                offset.incrementAndGet();
            }

            //break if 0 or <max selected.
            if (rows.size() < max) {
                break;
            }
        }

        //add key locations from builder to shipments
        for (final Map.Entry<Long, List<LiteKeyLocation>> e : builder.build().entrySet()) {
            shipmentsById.get(e.getKey()).getKeyLocations().addAll(e.getValue());
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
