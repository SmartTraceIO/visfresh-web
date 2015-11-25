/**
 *
 */
package com.visfresh.io.json;

import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.io.DeviceResolver;
import com.visfresh.io.ShipmentResolver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertSerializer extends AbstractJsonSerializer {
    private ShipmentResolver shipmentResolver;
    private DeviceResolver deviceResolver;

    /**
     * @param tz time zone.
     */
    public AlertSerializer(final TimeZone tz) {
        super(tz);
    }

    /**
     * @param json
     * @return
     */
    public Alert parseAlert(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();
        final AlertType type = AlertType.valueOf(json.get("type").getAsString());
        Alert alert;
        switch (type) {
            case CriticalHot:
            case CriticalCold:
            case Hot:
            case Cold:
                final TemperatureAlert ta = new TemperatureAlert();
                ta.setTemperature(asDouble(json.get("temperature")));
                ta.setMinutes(asInt(json.get("minutes")));
                ta.setCumulative(asBoolean(json.get("cumulative")));
                alert = ta;
                break;
            default:
                alert = new Alert();
        }

        alert.setDate(asDate(json.get("date")));
        alert.setDevice(getDeviceResolver().getDevice(asString(json.get("device"))));
        alert.setShipment(getShipmentResolver().getShipment(asLong(json.get("shipment"))));
        alert.setId(asLong(json.get("id")));
        alert.setType(type);

        return alert;
    }
    /**
     * @param alert
     * @return JSON object
     */
    public JsonObject toJson(final Alert alert) {
        final JsonObject json = new JsonObject();

        //add common alert properties
        json.addProperty("id", alert.getId());
        json.addProperty("date", formatDate(alert.getDate()));
        json.addProperty("device", alert.getDevice().getId());
        json.addProperty("shipment", alert.getShipment().getId());
        json.addProperty("type", alert.getType().name());

        switch (alert.getType()) {
            case CriticalHot:
            case CriticalCold:
            case Hot:
            case Cold:
                final TemperatureAlert ta = (TemperatureAlert) alert;
                json.addProperty("temperature", ta.getTemperature());
                json.addProperty("minutes", ta.getMinutes());
                json.addProperty("cumulative", ta.isCumulative());
                break;

                default:
                    //nothing
                    break;
        }

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
