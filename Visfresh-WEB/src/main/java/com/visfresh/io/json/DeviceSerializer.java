/**
 *
 */
package com.visfresh.io.json;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.visfresh.constants.DeviceConstants;
import com.visfresh.entities.Color;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.DeviceModel;
import com.visfresh.entities.Language;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.DeviceResolver;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceSerializer extends AbstractJsonSerializer {
    private DeviceResolver deviceResolver;
    final DateFormat isoFormat;
    final DateFormat prettyFormat;
    final TemperatureUnits temperatureUnits;

    /**
     * @param tz time zone.
     */
    public DeviceSerializer(final TimeZone tz, final Language lang, final TemperatureUnits tu) {
        super(tz);
        isoFormat = DateTimeUtils.createIsoFormat(lang, tz);
        prettyFormat = DateTimeUtils.createPrettyFormat(lang, tz);
        temperatureUnits = tu;
    }
    /**
     * @param tz time zone.
     */
    public DeviceSerializer(final User user) {
        this(user.getTimeZone(), user.getLanguage(), user.getTemperatureUnits());
    }
    /**
     * @param json JSON object.
     * @return device.
     */
    public Device parseDevice(final JsonObject json) {
        final Device tr = new Device();
        tr.setImei(asString(json.get(DeviceConstants.PROPERTY_IMEI)));
        if (has(json, DeviceConstants.PROPERTY_MODEL)) {
            tr.setModel(DeviceModel.valueOf(asString(json.get(DeviceConstants.PROPERTY_MODEL))));
        }
        tr.setName(asString(json.get(DeviceConstants.PROPERTY_NAME)));
        tr.setColor(parseColor(asString(json.get(DeviceConstants.PROPERTY_COLOR))));
        tr.setDescription(asString(json.get(DeviceConstants.PROPERTY_DESCRIPTION)));
        tr.setActive(!Boolean.FALSE.equals(asBoolean(json.get(DeviceConstants.PROPERTY_ACTIVE))));
        tr.setAutostartTemplateId(asLong(json.get(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID)));
        return tr;
    }
    /**
     * @param d device.
     * @return device serialized to JSON format.
     */
    public JsonObject toJson(final Device d) {
        if (d == null) {
            return null;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty(DeviceConstants.PROPERTY_DESCRIPTION, d.getDescription());
        obj.addProperty(DeviceConstants.PROPERTY_IMEI, d.getImei());
        obj.addProperty(DeviceConstants.PROPERTY_MODEL, d.getModel().name());
        obj.addProperty(DeviceConstants.PROPERTY_NAME, d.getName());
        obj.addProperty(DeviceConstants.PROPERTY_ACTIVE, d.isActive());
        obj.addProperty(DeviceConstants.PROPERTY_SN, d.getSn());
        obj.addProperty(DeviceConstants.PROPERTY_COLOR, d.getColor() != null ? d.getColor().name() : null);
        obj.addProperty(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID, d.getAutostartTemplateId());
        return obj;
    }
    public ListDeviceItem parseListDeviceItem(final JsonElement e) {
        if (e == null || e.isJsonNull()) {
            return null;
        }

        final JsonObject json = e.getAsJsonObject();

        final ListDeviceItem d = new ListDeviceItem();
        d.setImei(asString(json.get(DeviceConstants.PROPERTY_IMEI)));
        if (has(json, DeviceConstants.PROPERTY_MODEL)) {
            d.setModel(DeviceModel.valueOf(asString(json.get(DeviceConstants.PROPERTY_MODEL))));
        }
        d.setName(asString(json.get(DeviceConstants.PROPERTY_NAME)));
        d.setDescription(asString(json.get(DeviceConstants.PROPERTY_DESCRIPTION)));
        d.setActive(!Boolean.FALSE.equals(asBoolean(json.get(DeviceConstants.PROPERTY_ACTIVE))));
        d.setAutostartTemplateId(asLong(json.get(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID)));
        d.setAutostartTemplateName(asString(json.get(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_NAME)));
        if (has(json, DeviceConstants.PROPERTY_COLOR)) {
            d.setColor(Color.valueOf(asString(json.get(DeviceConstants.PROPERTY_COLOR))));
        }

        d.setShipmentId(asLong(json.get(DeviceConstants.PROPERTY_LAST_SHIPMENT)));
        final String status = asString(json.get(DeviceConstants.PROPERTY_SHIPMENT_STATUS));
        if (status != null) {
            d.setShipmentStatus(ShipmentStatus.valueOf(status));
        }
        final String lastReadingTimeStr = asString(json.get(DeviceConstants.PROPERTY_LAST_READING_TIME_ISO));
        if (lastReadingTimeStr != null) {
            try {
                d.setLastReadingTime(isoFormat.parse(lastReadingTimeStr));
            } catch (final ParseException exc) {
                throw new RuntimeException(exc);
            }
        }
        d.setTemperature(parseTemperature(asString(json.get(DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE))));
        d.setBattery(asInteger(json.get(DeviceConstants.PROPERTY_LAST_READING_BATTERY)));
        d.setLatitude(asDouble(json.get(DeviceConstants.PROPERTY_LAST_READING_LAT)));
        d.setLongitude(asDouble(json.get(DeviceConstants.PROPERTY_LAST_READING_LONG)));

        return d;
    }
    /**
     * @param str
     * @return
     */
    private Double parseTemperature(final String str) {
        if (str == null) {
            return null;
        }

        final String suffix = LocalizationUtils.getDegreeSymbol(this.temperatureUnits);
        final String degreeStr = str.substring(0, str.length() - suffix.length());

        //create US locale decimal format
        final DecimalFormat fmt = new DecimalFormat("#0.0");
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
        fmt.setDecimalFormatSymbols(decimalFormatSymbols);

        //format temperature string
        double degree;
        try {
            degree = fmt.parse(degreeStr).doubleValue();
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
        return LocalizationUtils.convertFromUnits(degree, this.temperatureUnits);
    }
    /**
     * @param json JSON object.
     * @return device command.
     */
    public DeviceCommand parseDeviceCommand(final JsonObject json) {
        final DeviceCommand dc = new DeviceCommand();
        dc.setDevice(deviceResolver.getDevice(asString(json.get("device"))));
        dc.setCommand(asString(json.get("command")));
        return dc;
    }
    public JsonElement toJson(final DeviceCommand cmd) {
        if (cmd == null) {
            return JsonNull.INSTANCE;
        }

        final JsonObject obj = new JsonObject();
        obj.addProperty("device", cmd.getDevice().getId());
        obj.addProperty("command", cmd.getCommand());
        return obj;
    }
    /**
     * @param deviceResolver the deviceResolver to set
     */
    public void setDeviceResolver(final DeviceResolver deviceResolver) {
        this.deviceResolver = deviceResolver;
    }
    /**
     * @return the deviceResolver
     */
    public DeviceResolver getDeviceResolver() {
        return deviceResolver;
    }
    /**
     * @param name color name.
     * @return
     */
    private Color parseColor(final String name) {
        if (name == null) {
            return null;
        }
        return Color.valueOf(name);
    }
    /**
     * @param item
     * @return
     */
    public JsonObject exportToView(final ListDeviceItem item) {
        if (item == null) {
            return null;
        }
        final JsonObject obj = new JsonObject();
        obj.addProperty(DeviceConstants.PROPERTY_DESCRIPTION, item.getDescription());
        obj.addProperty(DeviceConstants.PROPERTY_IMEI, item.getImei());
        obj.addProperty(DeviceConstants.PROPERTY_MODEL, item.getModel().name());
        obj.addProperty(DeviceConstants.PROPERTY_NAME, item.getName());
        obj.addProperty(DeviceConstants.PROPERTY_SN, Device.getSerialNumber(item.getImei()));
        obj.addProperty(DeviceConstants.PROPERTY_COLOR,
                item.getColor() == null ? null : item.getColor().name());
        obj.addProperty(DeviceConstants.PROPERTY_ACTIVE, item.isActive());
        obj.addProperty(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_ID, item.getAutostartTemplateId());
        obj.addProperty(DeviceConstants.PROPERTY_AUTOSTART_TEMPLATE_NAME, item.getAutostartTemplateName());

        obj.addProperty(DeviceConstants.PROPERTY_LAST_SHIPMENT, item.getShipmentId());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_TIME_ISO, formatIso(item.getLastReadingTime()));
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_TIME, formatPretty(item.getLastReadingTime()));
        obj.addProperty("lastReadingTimeTimestamp", DateTimeUtils.toTimestamp(item.getLastReadingTime()));
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_TEMPERATURE,
                item.getTemperature() == null ? null : LocalizationUtils.getTemperatureString(
                        item.getTemperature(), temperatureUnits));
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_BATTERY, item.getBattery());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_LAT, item.getLatitude());
        obj.addProperty(DeviceConstants.PROPERTY_LAST_READING_LONG, item.getLongitude());
        obj.addProperty(DeviceConstants.PROPERTY_SHIPMENT_NUMBER,
                item.getShipmentId() == null ? null
                        : (Device.getSerialNumber(item.getImei()) + "(" + item.getTripCount() + ")"));
        obj.addProperty(DeviceConstants.PROPERTY_SHIPMENT_STATUS,
                item.getShipmentStatus() == null ? null: item.getShipmentStatus().name());

        return obj;
    }
    /**
     * @param date
     * @return
     */
    private String formatPretty(final Date date) {
        if (date == null) {
            return null;
        }
        return prettyFormat.format(date);
    }
    /**
     * @param date
     * @return
     */
    private String formatIso(final Date date) {
        if (date == null) {
            return null;
        }
        return isoFormat.format(date);
    }
}
