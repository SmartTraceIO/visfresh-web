/**
 *
 */
package com.visfresh.autodetect;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.visfresh.csv.CsvEventParser;
import com.visfresh.model.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NearestEventsSearchCSV extends AbstractNearestEventsSearch {
    /**
     * Default constructor.
     * @throws IOException
     * @throws ParseException
     */
    public NearestEventsSearchCSV(final double distance) throws IOException, ParseException {
        super(distance);
    }
    /**
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws Exception
     */
    @Override
    protected List<DeviceMessage> loadMessages() {
        try {
            return new CsvEventParser().parseEvents(NearestEventsSearchCSV.class.getResource("events.csv"));
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) throws IOException, ParseException {
        new NearestEventsSearchCSV(500000).run();
    }
}
