/**
 *
 */
package com.visfresh.io.json;

import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.io.GetFilteredShipmentsRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GetShipmentsRequestParser extends AbstractJsonSerializer {
    private static final String JSON_SORT_COLUMN = "sc";
    private static final String JSON_SORT_ORDER = "so";

    /**
     * @param tz
     */
    public GetShipmentsRequestParser(final TimeZone tz) {
        super(tz);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param json
     * @param tz
     * @return
     */
    public GetFilteredShipmentsRequest parseGetFilteredShipmentsRequest(final JsonObject json) {
        final GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();
        req.setAlertsOnly(asBoolean(json.get("alertsOnly")));
        req.setDeviceImei(asString(json.get("deviceImei")));
        if (json.has("last2Days")) {
            req.setLast2Days(asBoolean(json.get("last2Days")));
        }
        if (json.has("lastDay")) {
            req.setLastDay(asBoolean(json.get("lastDay")));
        }
        if (json.has("lastMonth")) {
            req.setLastMonth(asBoolean(json.get("lastMonth")));
        }
        if (json.has("lastWeek")) {
            req.setLastWeek(asBoolean(json.get("lastWeek")));
        }
        if (json.has("shipmentDateFrom")) {
            req.setShipmentDateFrom(parseDate(asString(json.get("shipmentDateFrom"))));
        }
        if (json.has("shipmentDateTo")) {
            req.setShipmentDateTo(parseDate(asString(json.get("shipmentDateTo"))));
        }
        req.setShipmentDescription(asString(json.get("shipmentDescription")));
        if (json.has("shippedFrom")) {
            req.setShippedFrom(asLongList(json.get("shippedFrom")));
        }
        if (json.has("shippedTo")) {
            req.setShippedTo(asLongList(json.get("shippedTo")));
        }
        if (json.has("status")) {
            final String statusString = asString(json.get("status"));
            req.setStatus(statusString == null ? null : ShipmentStatus.valueOf(statusString));
        }
        if (json.has("goods")) {
            req.setGoods(asString(json.get("goods")));
        }
        if (json.has("excludePriorShipments")) {
            req.setExcludePriorShipments(asBoolean(json.get("excludePriorShipments")));
        }
        req.setDeviceSn(asString(json.get(ShipmentConstants.DEVICE_SN)));

        if (json.has("pageIndex")) {
            req.setPageIndex(asInt(json.get("pageIndex")));
        }
        if (json.has("pageSize")) {
            req.setPageSize(asInt(json.get("pageSize")));
        }
        if (json.has(JSON_SORT_ORDER)) {
            req.setSortOrder(asString(json.get(JSON_SORT_ORDER)));
        }
        if (json.has(JSON_SORT_COLUMN)) {
            req.setSortColumn(asString(json.get(JSON_SORT_COLUMN)));
        }

        return req;
    }
    /**
     * @param r filtered shipments request.
     * @return request as JSON object.
     */
    public JsonObject toJson(final GetFilteredShipmentsRequest r) {
        if (r == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty("alertsOnly", r.isAlertsOnly());
        if (r.getDeviceImei() != null) {
            obj.addProperty("deviceImei", r.getDeviceImei());
        }
        if (r.getLast2Days() != null) {
            obj.addProperty("last2Days", r.getLast2Days());
        }
        if (r.getLastDay() != null) {
            obj.addProperty("lastDay", r.getLastDay());
        }
        if (r.getLastMonth() != null) {
            obj.addProperty("lastMonth", r.getLastMonth());
        }
        if (r.getLastWeek() != null) {
            obj.addProperty("lastWeek", r.getLastWeek());
        }
        if (r.getShipmentDateFrom() != null) {
            obj.addProperty("shipmentDateFrom", formatDate(r.getShipmentDateFrom()));
        }
        if (r.getShipmentDateTo() != null) {
            obj.addProperty("shipmentDateTo", formatDate(r.getShipmentDateTo()));
        }
        if (r.getShipmentDescription() != null) {
            obj.addProperty("shipmentDescription", r.getShipmentDescription());
        }
        if (r.getShippedFrom() != null && !r.getShippedFrom().isEmpty()) {
            obj.add("shippedFrom", asJsonArray(r.getShippedFrom()));
        }
        if (r.getShippedTo() != null && !r.getShippedTo().isEmpty()) {
            obj.add("shippedTo", asJsonArray(r.getShippedTo()));
        }
        if (r.getStatus() != null) {
            obj.addProperty("status", r.getStatus().toString());
        }
        if (r.getGoods() != null) {
            obj.addProperty("goods", r.getGoods());
        }
        if (r.getExcludePriorShipments() != null) {
            obj.addProperty("excludePriorShipments", r.getExcludePriorShipments());
        }
        if (r.getDeviceSn() != null) {
            obj.addProperty(ShipmentConstants.DEVICE_SN, r.getDeviceSn());
        }
        if (r.getPageIndex() != null) {
            obj.addProperty("pageIndex", r.getPageIndex());
        }
        if (r.getPageSize() != null) {
            obj.addProperty("pageSize", r.getPageSize());
        }
        if (r.getSortOrder() != null) {
            obj.addProperty(JSON_SORT_ORDER, r.getSortOrder());
        }
        if (r.getSortColumn() != null) {
            obj.addProperty(JSON_SORT_COLUMN, r.getSortColumn());
        }

        return obj;
    }
    /**
     * @param lsit
     * @return
     */
    public JsonArray asJsonArray(final List<Long> lsit) {
        final JsonArray array = new JsonArray();
        for (final Long item : lsit) {
            array.add(new JsonPrimitive(item));
        }
        return array;
    }
}
