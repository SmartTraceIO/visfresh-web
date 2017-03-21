/**
 *
 */
package com.visfresh.controllers.lite.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.controllers.lite.LiteKeyLocation;
import com.visfresh.controllers.lite.LiteShipment;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.impl.ShipmentDaoImpl;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipmentDaoLogicsTest extends LiteShipmentDao {
    private List<LiteShipment> shipments = new LinkedList<>();
    private Map<Long, List<LiteKeyLocation>> keyLocations = new HashMap<>();
    private long lastId = 1000l;

    /**
     * Default constructor.
     */
    public LiteShipmentDaoLogicsTest() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.lite.dao.LiteShipmentDao#getShipmentsDbData(com.visfresh.entities.Company, com.visfresh.dao.Sorting, com.visfresh.dao.Filter, com.visfresh.dao.Page)
     */
    @Override
    protected List<Map<String, Object>> getShipmentsDbData(final Company company, final Sorting sorting, final Filter filter, final Page page) {
        final List<LiteShipment> shipments = getSortedShipments();

        final List<Map<String, Object>> rows = new LinkedList<>();
        for (final LiteShipment s : shipments) {
            rows.add(toDbRow(s));
        }
        return rows;
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.lite.dao.LiteShipmentDao#getKeyLocations(java.util.Set, int, int)
     */
    @Override
    protected List<Map<String, Object>> getKeyLocations(final Set<Long> ids, final int offset, final int max) {
        final List<LiteKeyLocation> allSortedKeyLocations = new LinkedList<>();

        final List<LiteShipment> shipments = getSortedShipments();
        for (final LiteShipment s : shipments) {
            final List<LiteKeyLocation> locs = this.keyLocations.get(s.getShipmentId());
            if (locs != null) {
                final List<LiteKeyLocation> sorted = new LinkedList<LiteKeyLocation>(locs);
                sorted.sort((l1, l2) -> {return sortKeyLocations(l1, l2);});

                allSortedKeyLocations.addAll(sorted);
            }
        }

        final List<Map<String, Object>> rows = new LinkedList<>();

        if (offset + 1 < allSortedKeyLocations.size()) {
            int count = 0;
            final Iterator<LiteKeyLocation> iter = allSortedKeyLocations.listIterator(offset);
            while (iter.hasNext() && count < max) {
                rows.add(toDbRow(iter.next()));
                count++;
            }
        }

        return rows;
    }

    /**
     * @param loc
     * @return
     */
    private Map<String, Object> toDbRow(final LiteKeyLocation loc) {
        final Map<String, Object> row = new HashMap<>();
        row.put("id", loc.getId());
        row.put("temperature", loc.getTemperature());
        row.put("time", loc.getTime());
        return row;
    }
    /**
     * @param s shipment.
     * @return DB row
     */
    private Map<String, Object> toDbRow(final LiteShipment s) {
        final Map<String, Object> row = new HashMap<>();

        row.put(ShipmentDaoImpl.ARRIVALDATE_FIELD, s.getActualArrivalDate());
        row.put(ShipmentConstants.DEVICE_SN, s.getDeviceSN());
        row.put(ShipmentDaoImpl.ETA_FIELD, s.getEstArrivalDate());
        row.put("lowerTemperatureLimit", s.getLowerTemperatureLimit());
        row.put(ShipmentDaoImpl.SHIPMENTDATE_FIELD, s.getShipmentDate());
        row.put(ShipmentDaoImpl.ID_FIELD, s.getShipmentId());
        row.put("sfrom", s.getShippedFrom());
        row.put("sto", s.getShippedTo());
        row.put(ShipmentDaoImpl.SIBLINGCOUNT_FIELD, s.getSiblingCount());
        row.put(ShipmentDaoImpl.STATUS_FIELD, s.getStatus().toString());
        row.put(ShipmentDaoImpl.SIBLINGCOUNT_FIELD, s.getTripCount());
        row.put("upperTemperatureLimit", s.getUpperTemperatureLimit());

        return row;
    }

    /**
     * @param l1 first location.
     * @param l2 second location.
     * @return comparation result.
     */
    private int sortKeyLocations(final LiteKeyLocation l1, final LiteKeyLocation l2) {
        int result = l1.getTime().compareTo(l2.getTime());
        if (result == 0) {
            result = ((Long) l1.getId()).compareTo(l2.getId());
        }
        return result;
    }

    /**
     * @return
     */
    private List<LiteShipment> getSortedShipments() {
        final List<LiteShipment> sorted = new LinkedList<>(shipments);
        sorted.sort((s1, s2) -> {
            return s1.getShipmentId().compareTo(s2.getShipmentId());
        });
        return sorted;
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.lite.dao.LiteShipmentDao#getTotalCount(com.visfresh.entities.Company, com.visfresh.dao.Filter)
     */
    @Override
    protected int getTotalCount(final Company company, final Filter filter) {
        int totalCount = 0;
        for (final LiteShipment s : shipments) {
            final List<LiteKeyLocation> locs = this.keyLocations.get(s.getShipmentId());
            if (locs != null) {
                totalCount += locs.size();
            }
        }

        return totalCount;
    }
    private long nextId() {
        return ++lastId;
    }
}
