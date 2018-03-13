/**
 *
 */
package com.visfresh.reports.shipment;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReadingsParserTest extends ReadingsParser implements
        ReadingsHandler {
    private final List<ShortTrackerEvent> events = new LinkedList<>();
    private final Map<Long, AlertType[]> alerts = new HashMap<Long, AlertType[]>();

    /**
     * Default constructor.
     */
    public ReadingsParserTest() {
        super();
        setHandler(this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.reports.shipment.ReadingsHandler#handleEvent(com.visfresh.entities.ShortTrackerEvent, com.visfresh.entities.AlertType[])
     */
    @Override
    public void handleEvent(final ShortTrackerEvent e, final AlertType[] alerts) {
        events.add(e);
        this.alerts.put(e.getId(), alerts);
    }

    @Test
    public void testParse() throws IOException, ParseException {
        final String content = StringUtils.getContent(
                ReadingsParserTest.class.getResource("readingsParserTest.csv"), "UTF-8");

        parse(new StringReader(content));

        //check events
        assertEquals(3, events.size());
        //369020,769(10),2016-08-19 10:43,-2.5,3954,-33.901055,151.194647,"354430070007696",2016-08-19 10:43,LightOn,"LightOn,Cold"
        final ShortTrackerEvent e = events.get(1);
        //369020,
        assertEquals(369020l, (long) e.getId());
        //769(10),
        assertEquals(76910l, (long) e.getShipmentId());
        //2016-08-19 10:43,
        assertEquals("2016-08-19 10:43", dateParser.format(e.getTime()));
        //-2.5,
        assertEquals(-2.5, e.getTemperature(), 0.01);
        //3954,
        assertEquals(3954, e.getBattery());
        //-33.901055,
        assertEquals(-33.9, e.getLatitude(), 0.01);
        //151.194647,
        assertEquals(151.19, e.getLongitude(), 0.01);
        //"354430070007696",
        assertEquals("354430070007696", e.getDeviceImei());
        //2016-08-19 10:43,
        assertEquals("2016-08-19 10:43", dateParser.format(e.getCreatedOn()));
        //LightOn,
        assertEquals(TrackerEventType.BRT, e.getType());

        //check alerts
        final AlertType[] alerts = this.alerts.get(369020l);
        assertEquals(2, alerts.length);
        assertEquals(AlertType.LightOn, alerts[0]);
        assertEquals(AlertType.Cold, alerts[1]);
    }
    /* (non-Javadoc)
     * @see com.visfresh.reports.shipment.ReadingsHandler#getShipmentId(java.lang.String, int)
     */
    @Override
    public Long getShipmentId(final String sn, final int tripCount) {
        return Long.parseLong(sn + tripCount);
    }
}
