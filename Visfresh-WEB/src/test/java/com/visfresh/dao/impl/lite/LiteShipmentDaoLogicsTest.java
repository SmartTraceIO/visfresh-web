/**
 *
 */
package com.visfresh.dao.impl.lite;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.controllers.lite.LiteKeyLocation;
import com.visfresh.controllers.lite.LiteShipment;
import com.visfresh.controllers.lite.LiteShipmentResult;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.impl.shipment.ShipmentDaoImpl;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipmentDaoLogicsTest extends LiteShipmentDaoImpl {
    private List<LiteShipment> shipments = new LinkedList<>();
    private Map<Long, List<LiteKeyLocation>> keyLocations = new HashMap<>();
    private long lastId = 1000l;
    private final List<Integer> shipmentListSizes = new LinkedList<>();
    /**
     * The ranges of readings read by getKeyLocations method
     * in this case the Page object is used not as page but as offset and length holder.
     */
    private final List<Page> readingsRanges = new LinkedList<>();

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
    protected List<Map<String, Object>> getShipmentsDbData(final Long company, final Sorting sorting, final Filter filter, final Page page) {
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
        // in this case the Page object is used not as page but as offset and length holder.
        readingsRanges.add(new Page(offset + 1, max));

        //get all sorted key locations.
        final List<Map<String, Object>> allSortedKeyLocations = new LinkedList<>();
        final List<LiteShipment> shipments = getSortedShipments();

        for (final LiteShipment s : shipments) {
            if (ids.contains(s.getShipmentId())) {
                final List<LiteKeyLocation> locs = this.keyLocations.get(s.getShipmentId());
                if (locs != null) {
                    final List<LiteKeyLocation> sorted = new LinkedList<LiteKeyLocation>(locs);
                    sorted.sort((l1, l2) -> {return sortKeyLocations(l1, l2);});

                    for (final LiteKeyLocation l : sorted) {
                        allSortedKeyLocations.add(toDbRow(s, l));
                    }
                }
            }
        }

        //select readings only from offset and using max as limit
        final List<Map<String, Object>> rows = new LinkedList<>();

        if (offset < allSortedKeyLocations.size()) {
            int count = 0;
            final Iterator<Map<String, Object>> iter = allSortedKeyLocations.listIterator(offset);
            while (iter.hasNext() && count < max) {
                rows.add(iter.next());
                count++;
            }
        }

        return rows;
    }

    /**
     * @param loc
     * @return
     */
    private Map<String, Object> toDbRow(final LiteShipment shipment, final LiteKeyLocation loc) {
        final Map<String, Object> row = new HashMap<>();
        row.put("id", loc.getId());
        row.put("temperature", loc.getTemperature());
        row.put("time", loc.getTime());
        row.put("shipment", shipment.getShipmentId());
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
     * @return compare result.
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
    protected int getTotalCount(final Long company, final Filter filter) {
        int totalCount = 0;
        for (final LiteShipment s : shipments) {
            final List<LiteKeyLocation> locs = this.keyLocations.get(s.getShipmentId());
            if (locs != null) {
                totalCount += locs.size();
            }
        }

        return totalCount;
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.lite.dao.LiteShipmentDao#addKeyLocationsToShipmentPart(java.util.List)
     */
    @Override
    protected void addKeyLocationsToShipmentPart(final List<LiteShipment> shipments) {
        shipmentListSizes.add(shipments.size());
        super.addKeyLocationsToShipmentPart(shipments);
    }
    private long nextId() {
        return ++lastId;
    }
    private LiteShipment createShipment() {
        final long t = System.currentTimeMillis() - 1000000l;

        final LiteShipment s = new LiteShipment();
        s.setActualArrivalDate(new Date(t));
        s.setDeviceSN("12345");
        s.setEstArrivalDate(s.getActualArrivalDate());
        s.setShipmentDate(new Date(t - 1000000000l));
        s.setShipmentId(nextId());
        s.setShippedFrom("ShippedFrom");
        s.setShippedTo("ShippedTo");
        s.setSiblingCount(7);
        s.setStatus(ShipmentStatus.Default);
        s.setTripCount(1);

        return s;
    }
    /**
     * @param t temperature.
     * @return key location.
     */
    private LiteKeyLocation createLocation(final long time, final double t) {
        final LiteKeyLocation loc = new LiteKeyLocation();
        loc.setId(nextId());
        loc.setTemperature(t);
        loc.setTime(new Date(time));
        return loc;
    }

    //tests
    @Test
    public void testGetShipments() {
        final LiteShipment s = createShipment();
        shipments.add(s);

        final long t = System.currentTimeMillis() - 1000000l;

        final List<LiteKeyLocation> locs = new LinkedList<>();
        keyLocations.put(s.getShipmentId(), locs);

        locs.add(createLocation(t + 10000l, 2.));
        locs.add(createLocation(t + 20000l, 2.));
        locs.add(createLocation(t + 60000l, 2.));

        final LiteShipmentResult result = getShipments(null, null, null, null);

        assertEquals(1, result.getResult().size());
        assertEquals(3, result.getResult().get(0).getKeyLocations().size());

    }
    @Test
    public void testGetShipmentsSeveralShipments() {
        final LiteShipment s1 = createShipment();
        final LiteShipment s2 = createShipment();
        shipments.add(s1);
        shipments.add(s2);

        final long t = System.currentTimeMillis() - 1000000l;

        final List<LiteKeyLocation> locs1 = new LinkedList<>();
        final List<LiteKeyLocation> locs2 = new LinkedList<>();
        keyLocations.put(s1.getShipmentId(), locs1);
        keyLocations.put(s2.getShipmentId(), locs2);

        locs1.add(createLocation(t + 10000l, 2.));
        locs2.add(createLocation(t + 15000l, 2.));
        locs1.add(createLocation(t + 20000l, 2.));
        locs2.add(createLocation(t + 25000l, 2.));
        locs1.add(createLocation(t + 60000l, 2.));
        locs2.add(createLocation(t + 65000l, 2.));

        final LiteShipmentResult result = getShipments(null, null, null, null);

        assertEquals(2, result.getResult().size());
        assertEquals(3, result.getResult().get(0).getKeyLocations().size());
        assertEquals(3, result.getResult().get(1).getKeyLocations().size());
    }

    @Test
    public void testGetShipmentsPagesSupport() {
        setMaxShipmentsToProcess(100);

        final LiteShipment s1 = createShipment();
        final LiteShipment s2 = createShipment();
        shipments.add(s1);
        shipments.add(s2);

        final long t = System.currentTimeMillis() - 1000000l;

        final List<LiteKeyLocation> locs1 = new LinkedList<>();
        final List<LiteKeyLocation> locs2 = new LinkedList<>();
        keyLocations.put(s1.getShipmentId(), locs1);
        keyLocations.put(s2.getShipmentId(), locs2);

        locs1.add(createLocation(t + 10000l, 2.));
        locs2.add(createLocation(t + 15000l, 2.));

        getShipments(null, null, null, null);

        //check number or readings
        assertEquals(new Integer(2), shipmentListSizes.get(0));
    }
    @Test
    public void testGetShipmentsReadingPagesSupport() {
        setMaxReadingsToProcess(1);

        final LiteShipment s1 = createShipment();
        final LiteShipment s2 = createShipment();
        shipments.add(s1);
        shipments.add(s2);

        final long t = System.currentTimeMillis() - 1000000l;

        final List<LiteKeyLocation> locs1 = new LinkedList<>();
        final List<LiteKeyLocation> locs2 = new LinkedList<>();
        keyLocations.put(s1.getShipmentId(), locs1);
        keyLocations.put(s2.getShipmentId(), locs2);

        locs1.add(createLocation(t + 10000l, 2.));
        locs2.add(createLocation(t + 15000l, 2.));
        locs1.add(createLocation(t + 20000l, 2.));
        locs2.add(createLocation(t + 25000l, 2.));

        getShipments(null, null, null, null);

        //check number or readings
        assertEquals(5, readingsRanges.size());
        //Attention, in fact the pageNumber is used as holder of position of end reading
        //(not as real page number)
        assertEquals(1, readingsRanges.get(0).getPageNumber());
        assertEquals(2, readingsRanges.get(1).getPageNumber());
        assertEquals(3, readingsRanges.get(2).getPageNumber());
        assertEquals(2, readingsRanges.get(3).getPageNumber());
    }
    @Test
    public void testGetShipmentsPagesLocationsOrdering() {
        setMaxReadingsToProcess(1);

        final LiteShipment s1 = createShipment();
        final LiteShipment s2 = createShipment();
        shipments.add(s1);
        shipments.add(s2);

        final long t = System.currentTimeMillis() - 1000000l;

        final List<LiteKeyLocation> locs1 = new LinkedList<>();
        final List<LiteKeyLocation> locs2 = new LinkedList<>();
        keyLocations.put(s1.getShipmentId(), locs1);
        keyLocations.put(s2.getShipmentId(), locs2);

        final LiteKeyLocation l1 = createLocation(t + 10000l, 2.);
        locs1.add(l1);
        final LiteKeyLocation l2 = createLocation(t + 15000l, 2.);
        locs2.add(l2);
        final LiteKeyLocation l3 = createLocation(t + 20000l, 2.);
        locs1.add(l3);
        final LiteKeyLocation l4 = createLocation(t + 25000l, 2.);
        locs2.add(l4);

        final LiteShipmentResult result = getShipments(null, null, null, null);

        //check shipments ordering
        assertEquals(s1.getShipmentId(), result.getResult().get(0).getShipmentId());
        assertEquals(s2.getShipmentId(), result.getResult().get(1).getShipmentId());

        //check locations ordering
        assertEquals(l1.getId(), result.getResult().get(0).getKeyLocations().get(0).getId());
        assertEquals(l3.getId(), result.getResult().get(0).getKeyLocations().get(1).getId());

        assertEquals(l2.getId(), result.getResult().get(1).getKeyLocations().get(0).getId());
        assertEquals(l4.getId(), result.getResult().get(1).getKeyLocations().get(1).getId());
    }
}
