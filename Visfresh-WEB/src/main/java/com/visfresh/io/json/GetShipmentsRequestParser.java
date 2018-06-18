/**
 *
 */
package com.visfresh.io.json;

import java.util.List;
import java.util.TimeZone;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.visfresh.constants.ShipmentConstants;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.io.GetFilteredShipmentsRequest;
import com.visfresh.io.SortColumn;

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
        if (has(json, "sortBy")) {
            for (final JsonElement e : json.get("sortBy").getAsJsonArray()) {
                final JsonObject eson = e.getAsJsonObject();
                req.addSortColumn(
                        asString(eson.get("column")),
                        !"desc".equalsIgnoreCase(asString(eson.get("direction"))));
            }
        }
        req.setIncludeBeacons(!Boolean.FALSE.equals(asBoolean(json.get(ShipmentConstants.INCLUDE_BEACONS))));
        req.setIncludeTrackers(!Boolean.FALSE.equals(asBoolean(json.get(ShipmentConstants.INCLUDE_TRACKERS))));

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

        final JsonObject json = new JsonObject();
        json.addProperty("alertsOnly", r.isAlertsOnly());
        if (r.getDeviceImei() != null) {
            json.addProperty("deviceImei", r.getDeviceImei());
        }
        if (r.getLast2Days() != null) {
            json.addProperty("last2Days", r.getLast2Days());
        }
        if (r.getLastDay() != null) {
            json.addProperty("lastDay", r.getLastDay());
        }
        if (r.getLastMonth() != null) {
            json.addProperty("lastMonth", r.getLastMonth());
        }
        if (r.getLastWeek() != null) {
            json.addProperty("lastWeek", r.getLastWeek());
        }
        if (r.getShipmentDateFrom() != null) {
            json.addProperty("shipmentDateFrom", formatDate(r.getShipmentDateFrom()));
        }
        if (r.getShipmentDateTo() != null) {
            json.addProperty("shipmentDateTo", formatDate(r.getShipmentDateTo()));
        }
        if (r.getShipmentDescription() != null) {
            json.addProperty("shipmentDescription", r.getShipmentDescription());
        }
        if (r.getShippedFrom() != null && !r.getShippedFrom().isEmpty()) {
            json.add("shippedFrom", asJsonArray(r.getShippedFrom()));
        }
        if (r.getShippedTo() != null && !r.getShippedTo().isEmpty()) {
            json.add("shippedTo", asJsonArray(r.getShippedTo()));
        }
        if (r.getStatus() != null) {
            json.addProperty("status", r.getStatus().toString());
        }
        if (r.getGoods() != null) {
            json.addProperty("goods", r.getGoods());
        }
        if (r.getExcludePriorShipments() != null) {
            json.addProperty("excludePriorShipments", r.getExcludePriorShipments());
        }
        if (r.getDeviceSn() != null) {
            json.addProperty(ShipmentConstants.DEVICE_SN, r.getDeviceSn());
        }
        if (r.getPageIndex() != null) {
            json.addProperty("pageIndex", r.getPageIndex());
        }
        if (r.getPageSize() != null) {
            json.addProperty("pageSize", r.getPageSize());
        }
        if (r.getSortOrder() != null) {
            json.addProperty(JSON_SORT_ORDER, r.getSortOrder());
        }
        if (r.getSortColumn() != null) {
            json.addProperty(JSON_SORT_COLUMN, r.getSortColumn());
        }
        json.addProperty(ShipmentConstants.INCLUDE_BEACONS, r.getIncludeBeacons());
        json.addProperty(ShipmentConstants.INCLUDE_TRACKERS, r.getIncludeTrackers());

        if (r.getSortColumns().size() > 0) {
            final JsonArray sortBy = new JsonArray();
            for (final SortColumn sc : r.getSortColumns()) {
                final JsonObject scJson = new JsonObject();
                scJson.addProperty("column", sc.getName());
                scJson.addProperty("direction", sc.isAscent() ? "asc" : "desc");

                sortBy.add(scJson);
            }
            json.add("sortBy", sortBy);
        }

        return json;
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
