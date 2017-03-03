/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.dao.impl.ShipmentTemperatureStatsCollector;
import com.visfresh.dao.impl.TimeRanges;
import com.visfresh.entities.Language;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentStatisticsCollectorSerializerTest {
    private ShipmentStatisticsCollectorSerializer serializer;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsCollectorSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new ShipmentStatisticsCollectorSerializer();
    }

    @Test
    public void testSerizlie() {
        //time ranges
        final TimeRanges ranges = new TimeRanges();
        ranges.setEndTime(System.currentTimeMillis());
        ranges.setStartTime(System.currentTimeMillis() - 100000000);

        final TrackerEvent e = new TrackerEvent();
        e.setTime(new Date());
        e.setTemperature(12.34);

        //statistics
        final int count = 77;
        final double summ2 = 0.002;
        final double summ = 123.67;
        final double minTemp = -2.7;
        final double maxTemp = 23.8;
        final long hot = 298298792;
        final long cold = 234589798;

        ShipmentTemperatureStatsCollector collector = new ShipmentTemperatureStatsCollector() {
            {
                this.timeRanges = ranges;
                this.lastEvent = e;
                this.n = count;
                this.summt2 = summ2;
                this.summt = summ;
                this.min = minTemp;
                this.max = maxTemp;
                this.hotTime = hot;
                this.coldTime = cold;
            }
        };

        final JsonObject json = serializer.toJson(collector);
        System.out.println(json);
        System.out.println(json.toString().length());
        collector = serializer.parseShipmentTemperatureStatsCollector(json);

        // time ranges
        assertEquals(ranges.getStartTime(), collector.getTimeRanges().getStartTime(), 0.001);
        assertEquals(ranges.getEndTime(), collector.getTimeRanges().getEndTime(), 0.001);

        // last event
        assertEquals(format(e.getTime()), format(collector.getLastEvent().getTime()));
        assertEquals(e.getTemperature(), collector.getLastEvent().getTemperature(), 0.001);

        // number of readings;
        assertEquals(count, collector.getN());
        // get square of temperatures summ
        assertEquals(summ2, collector.getSummt2(), 0.001);
        // get summ of temperatures
        assertEquals(summ, collector.getSummt(), 0.001);
        // get minimal temperature
        assertEquals(minTemp, collector.getMin(), 0.001);
        // get maximal temperature
        assertEquals(maxTemp, collector.getMax(), 0.001);
        // hot time
        assertEquals(hot, collector.getHotTime());
        // cold time
        assertEquals(cold, collector.getColdTime());
    }
    private String format(final Date date) {
        return DateTimeUtils.createIsoFormat(Language.English, TimeZone.getDefault()).format(date);
    }
}
