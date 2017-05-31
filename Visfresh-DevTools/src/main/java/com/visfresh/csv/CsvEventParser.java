/**
 *
 */
package com.visfresh.csv;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.model.DeviceMessage;
import com.visfresh.model.Location;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CsvEventParser {
    /**
     * DEfault constructor.
     */
    public CsvEventParser() {
        super();
    }
    /**
     * @return
     * @throws ParseException
     */
    public List<DeviceMessage> parseEvents(final URL eventsDataUrl) throws IOException, ParseException {
        final String eventsData = getContent(eventsDataUrl);
        final List<DeviceMessage> events = new LinkedList<>();
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        final String[] rows = eventsData.split("\n");
        for (int i = 1; i < rows.length; i++) {
            final String row = rows[i].trim();
            if (!row.isEmpty()) {
//                id,shipment,time,temperature °C,battery,latitude,longitude,device,createdon,type,alerts
//                988649,821(8),2017-02-14 16:25,10.4,4096,-33.849359,151.053748,"354430070008215",2017-02-14 16:25,SwitchedOn,
                final String[] data = row.split(",");

                final DeviceMessage e = new DeviceMessage();
                //time
                e.setTime(df.parse(data[2]));
                //temperature
                e.setTemperature(new Double(data[3]));
                //battery
                e.setBattery(new Integer(data[4]));

                //location
                final Location location = new Location();
                e.setLocation(location);
                //latitude
                final String latitude = data[5];
                if (!(latitude.equals("NULL") || latitude.length() == 0)) {
                    location.setLatitude(Double.parseDouble(latitude));
                }
                //latitude
                final String longitude = data[6];
                if (!(longitude.equals("NULL") || longitude.length() == 0)) {
                    location.setLongitude(Double.parseDouble(longitude));
                }
                //device
                e.setImei(data[7]);

                events.add(e);
            }
        }
        return events;
    }
    /**
     * @param eventsDataUrl
     * @return
     * @throws IOException
     */
    private String getContent(final URL eventsDataUrl) throws IOException {
        try (InputStream in = new BufferedInputStream(eventsDataUrl.openStream())) {
            return StringUtils.getContent(in, "UTF-8");
        }
    }
}
