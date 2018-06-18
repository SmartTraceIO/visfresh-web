/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.SortColumn;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GetShipmentsRequestParserTest extends AbstractSerializerTest {
    private GetShipmentsRequestParser serializer;

    /**
     * Default constructor.
     */
    public GetShipmentsRequestParserTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        final User user = new User();
        user.setTimeZone(UTC);

        serializer = new GetShipmentsRequestParser(user.getTimeZone());
    }
    @Test
    public void testGetFilteredShipmentsRequest() {
        //test all nulls
        GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();

        JsonObject json = serializer.toJson(req);
        req = serializer.parseGetFilteredShipmentsRequest(json);

        assertFalse(req.isAlertsOnly());
        assertNull(req.getDeviceImei());
        assertNull(req.getLast2Days());
        assertNull(req.getLastDay());
        assertNull(req.getLastMonth());
        assertNull(req.getLastWeek());
        assertNull(req.getShipmentDateFrom());
        assertNull(req.getShipmentDateTo());
        assertNull(req.getShipmentDescription());
        assertNull(req.getShippedFrom());
        assertNull(req.getShippedTo());
        assertNull(req.getStatus());
        assertNull(req.getGoods());
        assertNull(req.getExcludePriorShipments());
        assertTrue(req.getIncludeBeacons());
        assertTrue(req.getIncludeTrackers());

        //test not null values
        req = new GetFilteredShipmentsRequest();

        final String deviceImei = "283409237873234";
        final Boolean last2Days = true;
        final Boolean lastDay = true;
        final Boolean lastMonth = true;
        final Boolean lastWeek = true;
        final Date shipmentDateFrom = new Date(System.currentTimeMillis() - 1000000000l);
        final Date shipmentDateTo = new Date(System.currentTimeMillis() - 1000000l);
        final String shipmentDescription = "JUnit Shipment";
        final List<Long> shippedFrom = new LinkedList<Long>();
        shippedFrom.add(4l);
        shippedFrom.add(5l);
        final List<Long> shippedTo = new LinkedList<Long>();
        shippedTo.add(1l);
        shippedTo.add(2l);
        shippedTo.add(3l);
        final ShipmentStatus status = ShipmentStatus.InProgress;
        final String goods = "ABC";
        final Boolean excludePriorShipments = Boolean.TRUE;
        final String deviceSn = "device-serial-number";
        final Integer pageIndex = 10;
        final Integer pageSize = 200;
        final String sortColumn = "anyColumn";
        final String sortOrder = "asc";
        final String sortAscent = "sortAsc";
        final String sortDescent = "sortDesc";

        req.setAlertsOnly(true);
        req.setDeviceImei(deviceImei);
        req.setLast2Days(last2Days);
        req.setLastDay(lastDay);
        req.setLastMonth(lastMonth);
        req.setLastWeek(lastWeek);
        req.setShipmentDateFrom(shipmentDateFrom);
        req.setShipmentDateTo(shipmentDateTo);
        req.setShipmentDescription(shipmentDescription);
        req.setShippedFrom(shippedFrom);
        req.setShippedTo(shippedTo);
        req.setStatus(status);
        req.setGoods(goods);
        req.setExcludePriorShipments(excludePriorShipments);
        req.setDeviceSn(deviceSn);
        req.setPageIndex(pageIndex);
        req.setPageSize(pageSize);
        req.setSortColumn(sortColumn);
        req.setSortOrder(sortOrder);
        req.addSortColumn(sortAscent, true);
        req.addSortColumn(sortDescent, false);

        json = serializer.toJson(req);
        req = serializer.parseGetFilteredShipmentsRequest(json);

        assertTrue(req.isAlertsOnly());
        assertEquals(deviceImei, req.getDeviceImei());
        assertEquals(last2Days, req.getLast2Days());
        assertEquals(lastDay, req.getLastDay());
        assertEquals(lastMonth, req.getLastMonth());
        assertEquals(lastWeek, req.getLastWeek());
        assertEquals(formatDate(shipmentDateFrom), formatDate(req.getShipmentDateFrom()));
        assertEquals(formatDate(shipmentDateTo), formatDate(req.getShipmentDateTo()));
        assertEquals(shipmentDescription, req.getShipmentDescription());
        assertEquals(2, req.getShippedFrom().size());
        assertEquals(3, req.getShippedTo().size());
        assertEquals(status, req.getStatus());
        assertEquals(goods, req.getGoods());
        assertEquals(excludePriorShipments, req.getExcludePriorShipments());
        assertEquals(deviceSn, req.getDeviceSn());
        assertEquals(pageIndex, req.getPageIndex());
        assertEquals(pageSize, req.getPageSize());
        assertEquals(sortColumn, req.getSortColumn());
        assertEquals(sortOrder, req.getSortOrder());

        final List<SortColumn> sortColumns = req.getSortColumns();
        assertEquals(sortAscent, sortColumns.get(0).getName());
        assertTrue(sortColumns.get(0).isAscent());
        assertEquals(sortDescent, sortColumns.get(1).getName());
        assertFalse(sortColumns.get(1).isAscent());
    }
    @Test
    public void testIncludeBeaconsTrackers() {
        //test all nulls
        GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();

        req.setIncludeBeacons(true);
        req.setIncludeTrackers(true);

        JsonObject json = serializer.toJson(req);
        req = serializer.parseGetFilteredShipmentsRequest(json);

        assertTrue(req.getIncludeBeacons());
        assertTrue(req.getIncludeTrackers());

        //test false
        req.setIncludeBeacons(false);
        req.setIncludeTrackers(false);

        json = serializer.toJson(req);
        req = serializer.parseGetFilteredShipmentsRequest(json);

        assertFalse(req.getIncludeBeacons());
        assertFalse(req.getIncludeTrackers());
    }
    /**
     * @param date
     * @return
     */
    private String formatDate(final Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(date);
    }
}
