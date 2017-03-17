/**
 *
 */
package com.visfresh.controllers.lite;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.Language;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LiteShipmentSerializerTest {
    private LiteShipmentSerializer serializer;
    private DateFormat format;

    /**
     * Default constructor.
     */
    public LiteShipmentSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        final Language lang = Language.English;

        serializer = new LiteShipmentSerializer(tz, lang, TemperatureUnits.Fahrenheit);
        format = DateTimeUtils.createIsoFormat(lang, tz);
    }

    @Test
    public void testSerializeShipment() {
        final Date actualArrivalDate = new Date(System.currentTimeMillis() - 2937987l);
        final String deviceSN = "11";
        final Date estArrivalDate = new Date(System.currentTimeMillis() - 1298790879l);
        final double lowerTemperatureLimit = -12.7;
        final int percentageComplete = 77;
        final Date shipmentDate = new Date(actualArrivalDate.getTime() - 239879387l);
        final Long shipmentId = 77l;
        final String shippedFrom = "Shiped From";
        final String shippedTo = "Shipped To";
        final int siblingCount = 34;
        final ShipmentStatus status = ShipmentStatus.InProgress;
        final int tripCount = 78;
        final double upperTemperatureLimit = 23;
        final Integer coldNum = 5;
        final Integer hotNum = 6;

        LiteShipment s = new LiteShipment();

        s.setActualArrivalDate(actualArrivalDate);
        s.setDeviceSN(deviceSN);
        s.setEstArrivalDate(estArrivalDate);
        s.setLowerTemperatureLimit(lowerTemperatureLimit);
        s.setPercentageComplete(percentageComplete);
        s.setShipmentDate(shipmentDate);
        s.setShipmentId(shipmentId);
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        s.setSiblingCount(siblingCount);
        s.setStatus(status);
        s.setTripCount(tripCount);
        s.setUpperTemperatureLimit(upperTemperatureLimit);

        s.getAlertSummary().put(AlertType.Cold, coldNum);
        s.getAlertSummary().put(AlertType.Hot, hotNum);

        s.getKeyLocations().add(new LiteKeyLocation(11., new Date()));
        s.getKeyLocations().add(new LiteKeyLocation(12., new Date()));

        s = serializer.parseLiteShipment(serializer.toJson(s));

        //check result
        assertEquals(format.format(actualArrivalDate), format.format(s.getActualArrivalDate()));
        assertEquals(deviceSN, s.getDeviceSN());
        assertEquals(format.format(estArrivalDate), format.format(s.getEstArrivalDate()));
        assertEquals(lowerTemperatureLimit, s.getLowerTemperatureLimit(), 0.1);
        assertEquals(percentageComplete, s.getPercentageComplete());
        assertEquals(format.format(shipmentDate), format.format(s.getShipmentDate()));
        assertEquals(shipmentId, s.getShipmentId());
        assertEquals(shippedFrom, s.getShippedFrom());
        assertEquals(shippedTo, s.getShippedTo());
        assertEquals(siblingCount, s.getSiblingCount());
        assertEquals(status, s.getStatus());
        assertEquals(tripCount, s.getTripCount());
        assertEquals(upperTemperatureLimit, s.getUpperTemperatureLimit(), 1.);

        assertEquals(coldNum, s.getAlertSummary().get(AlertType.Cold));
        assertEquals(hotNum, s.getAlertSummary().get(AlertType.Hot));

        assertEquals(2, s.getKeyLocations().size());
    }
    @Test
    public void testSerializeKeyLocation() {
        final double temperature = 17.5;
        final Date time = new Date(System.currentTimeMillis() - 23498098l);

        LiteKeyLocation loc = new LiteKeyLocation();
        loc.setTemperature(temperature);
        loc.setTime(time);

        loc = serializer.parseLiteKeyLocation(serializer.toJson(loc));

        assertEquals(temperature, loc.getTemperature(), 1.);
        assertEquals(format.format(time), format.format(loc.getTime()));
    }
}
