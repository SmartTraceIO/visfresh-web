/**
 *
 */
package com.visfresh.io.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.dao.impl.ShipmentTemperatureStatsCollector;
import com.visfresh.dao.impl.TimeRanges;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsCollectorSerializer extends AbstractJsonSerializer {
    //tracker event keys
    private static final String TIME = "time";
    private static final String TEMPERATURE = "temperature";

    //time ranges keys
    private static final String END_TIME = "endTime";
    private static final String START_TIME = "startTime";

    //statistics keys
    private static final String TIME_RANGES = "timeRanges";
    private static final String LAST_EVENT= "lastEvent";
    private static final String N = "n";
    private static final String SUMMT2 = "summt2";
    private static final String SUMMT = "summt";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String HOT_TIME = "hotTime";
    private static final String COLD_TIME = "coldTime";

    /**
     * Default constructor.
     */
    public ShipmentStatisticsCollectorSerializer() {
        super(TimeZone.getTimeZone("UTC"));
    }

    public JsonObject toJson(final ShipmentTemperatureStatsCollector collector) {
        if (collector == null) {
            return null;
        }

        final JsonObject json = new JsonObject();

        json.add(TIME_RANGES, toJson(collector.getTimeRanges()));
        json.add(LAST_EVENT, toJson(collector.getLastEvent()));

        json.addProperty(N, collector.getN());
        json.addProperty(SUMMT2, collector.getSummt2());
        json.addProperty(SUMMT, collector.getSummt());
        json.addProperty(MIN, collector.getMin());
        json.addProperty(MAX, collector.getMax());
        json.addProperty(HOT_TIME, collector.getHotTime());
        json.addProperty(COLD_TIME, collector.getColdTime());
        return json;
    }
    public ShipmentTemperatureStatsCollector parseShipmentTemperatureStatsCollector(final JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }

        final JsonObject json = el.getAsJsonObject();
        final AccessibleShipmentStatsCollector collector = new AccessibleShipmentStatsCollector();

        collector.setTimeRanges(parseTimeRanges(json.get(TIME_RANGES)));
        collector.setLastEvent(parseLastEvent(json.get(LAST_EVENT)));

        collector.setN(asInt(json.get(N)));
        collector.setSummt2(asDouble(json.get(SUMMT2)));
        collector.setSummt(asDouble(json.get(SUMMT)));
        collector.setMin(asDouble(json.get(MIN)));
        collector.setMax(asDouble(json.get(MAX)));
        collector.setHotTime(asLong(json.get(HOT_TIME)));
        collector.setColdTime(asLong(json.get(COLD_TIME)));
        return collector;
    }
    /**
     * @param jsonElement
     * @return
     */
    private TrackerEvent parseLastEvent(final JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }
        final JsonObject json = jsonElement.getAsJsonObject();

        final TrackerEvent e = new TrackerEvent();
        e.setTemperature(asDouble(json.get(TEMPERATURE)));
        try {
            e.setTime(createDateFormat().parse(asString(json.get(TIME))));
        } catch (final ParseException exc) {
            throw new RuntimeException(exc);
        }

        return e;
    }
    /**
     * @param jsonElement
     * @return
     */
    private TimeRanges parseTimeRanges(final JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }
        final JsonObject json = jsonElement.getAsJsonObject();

        final TimeRanges r = new TimeRanges();
        r.setStartTime(asLong(json.get(START_TIME)));
        r.setEndTime(asLong(json.get(END_TIME)));

        return r;
    }
    /**
     * @param e tracker event.
     * @return
     */
    private JsonObject toJson(final TrackerEvent e) {
        if (e == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(TEMPERATURE, e.getTemperature());
        json.addProperty(TIME, createDateFormat().format(e.getTime()));
        return json;
    }
    /**
     * @param timeRanges
     * @return
     */
    private JsonObject toJson(final TimeRanges ranges) {
        if (ranges == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        json.addProperty(START_TIME, ranges.getStartTime());
        json.addProperty(END_TIME, ranges.getEndTime());
        return json;
    }

    /* (non-Javadoc)
     * @see com.visfresh.io.AbstractJsonSerializer#createDateFormat()
     */
    @Override
    protected SimpleDateFormat createDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }
}
