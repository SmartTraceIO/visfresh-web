/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.Arrival;
import com.visfresh.io.DeviceResolver;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ArrivalSerializer extends AbstractJsonSerializer {
    private ShipmentResolver shipmentResolver;
    private DeviceResolver deviceResolver;

    /**
     * @param tz time zone.
     */
    public ArrivalSerializer(final TimeZone tz) {
        super(tz);
    }
    /**
     * @param asJsonObject
     * @return
     */
    public Arrival parseArrival(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }
        final JsonObject obj = e.getAsJsonObject();

        final Arrival a = new Arrival();
        a.setDate(asDate(obj.get("date")));
        a.setDevice(getDeviceResolver().getDevice(asString(obj.get("device"))));
        a.setShipment(getShipmentResolver().getShipment(asLong(obj.get("shipment"))));
        a.setId(asLong(obj.get("id")));
        a.setNumberOfMettersOfArrival(asInt(obj.get("numberOfMetersOfArrival")));
        return a;
    }
    /**
     * @param arrival
     * @return JSON object.
     */
    public JsonObject toJson(final Arrival arrival) {
        final JsonObject json = new JsonObject();
        json.addProperty("id", arrival.getId());
        json.addProperty("numberOfMetersOfArrival", arrival.getNumberOfMettersOfArrival());
        json.addProperty("date", formatDate(arrival.getDate()));
        json.addProperty("dateTimestamp", DateTimeUtils.toTimestamp(arrival.getDate()));
        json.addProperty("device", arrival.getDevice().getId());
        json.addProperty("shipment", arrival.getShipment().getId());
        return json;
    }
    /**
     * @return the shipmentResolver
     */
    public ShipmentResolver getShipmentResolver() {
        return shipmentResolver;
    }
    /**
     * @param shipmentResolver the shipmentResolver to set
     */
    public void setShipmentResolver(final ShipmentResolver shipmentResolver) {
        this.shipmentResolver = shipmentResolver;
    }
    /**
     * @return the deviceResolver
     */
    public DeviceResolver getDeviceResolver() {
        return deviceResolver;
    }
    /**
     * @param deviceResolver the deviceResolver to set
     */
    public void setDeviceResolver(final DeviceResolver deviceResolver) {
        this.deviceResolver = deviceResolver;
    }
}
