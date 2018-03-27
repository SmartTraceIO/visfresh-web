/**
 *
 */
package com.visfresh.bt04;

import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParser {
    /**
     * Default constructor.
     */
    public MessageParser() {
        super();
    }

    public Bt04Message parse(final String rawData) {
        if (rawData == null) {
            return null;
        }

        final Bt04Message msg = new Bt04Message();
        msg.setRawData(rawData);

        //parse raw data
        final String[] lines = rawData.split("\\n");
        parseHeader(lines[0].trim(), msg);

        for (int i = 1; i < lines.length; i++) {
            msg.getBeacons().add(parseBeacon(lines[i].trim()));
        }

        return msg;
    }

    /**
     * @param str string to parse.
     * @return
     */
    private Beacon parseBeacon(final String str) {
        final Beacon b = new Beacon();
        // SN|Name|Temperature|Humidity|RSSI|Distance|battery|LastScannedTime|HardwareModel|<\n>
        // 11160058|RT_T|26.28|79.68|-82|0.16822005355867573|98|1522093633809|3901|
        final String[] lines = str.split(Pattern.quote("|"));
        b.setSn(lines[0]);
        b.setName(lines[1]);
        b.setTemperature(parseDouble(lines[2]));
        b.setHumidity(parseDouble(lines[3]));
        //RSSI
        b.setDistance(parseDouble(lines[5]));
        b.setBattery(parseDouble(lines[6]));
        b.setLastScannedTime(fromEpoch(lines[7]));
        b.setHardwareModel(lines[8]);

        return b;
    }

    /**
     * @param header header line.
     * @param msg message.
     */
    private void parseHeader(final String header, final Bt04Message msg) {
        // phone-imei|epoch-time|latitude|longitude|altitude|accuracy|speedKPH|<\n>
        // 356024089973101|1522093635378|21.0512713|105.7945854|0.0|20.0|0.0|
        final String[] headers = header.split(Pattern.quote("|"));
        msg.setImei(headers[0]);
        msg.setTime(fromEpoch(headers[1]));
        msg.setLatitude(parseDouble(headers[2]));
        msg.setLongitude(parseDouble(headers[3]));
        msg.setAltitude(parseDouble(headers[4]));
        msg.setAccuracy(parseDouble(headers[5]));
    }

    /**
     * @param str string to parse.
     * @return double value.
     */
    private Double parseDouble(final String str) {
        return Double.parseDouble(str);
    }
    /**
     * @param str
     * @return
     */
    private Date fromEpoch(final String str) {
        final long t = Long.parseLong(str);
        return new Date(t - TimeZone.getDefault().getOffset(t));
    }
}
