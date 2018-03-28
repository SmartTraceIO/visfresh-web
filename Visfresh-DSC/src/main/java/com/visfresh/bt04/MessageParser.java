/**
 *
 */
package com.visfresh.bt04;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageParser {
    private static final Logger log = LoggerFactory.getLogger(MessageParser.class);

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
        final String[] lines = splitByLines(rawData);
        parseHeader(lines[0].trim(), msg);

        for (int i = 1; i < lines.length; i++) {
            msg.getBeacons().add(parseBeacon(lines[i].trim()));
        }

        return msg;
    }

    /**
     * @param rawData
     * @return
     */
    protected String[] splitByLines(final String rawData) {
        final List<String> lines = new LinkedList<>();
        for (final String str : rawData.split("\\n")) {
            String line = str;
            if (line.endsWith("|")) {
                line = line.substring(0, line.length() - 1);
            }
            lines.add(line);
        }
        return lines.toArray(new String[lines.size()]);
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
        b.setTemperature(parseDouble(lines[2], "", false));
        b.setHumidity(parseDouble(lines[3], "", false));
        //RSSI
        b.setDistance(parseDouble(lines[5], "", false));
        b.setBattery(parseDouble(lines[6], "", false));
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
        if (headers.length > 2) { //possible location not provided
            msg.setLatitude(parseDouble(headers[2], "latitude", false));
            msg.setLongitude(parseDouble(headers[3], "longitude", false));
            msg.setAltitude(parseDouble(headers[4], "altitude", false));
            msg.setAccuracy(parseDouble(headers[5], "accuracy", false));
        }
    }

    /**
     * @param str string to parse.
     * @param parsingField TODO
     * @param isMandatory TODO
     * @return double value.
     */
    private Double parseDouble(final String str, final String parsingField, final boolean isMandatory) {
        if (!isMandatory && str.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (final RuntimeException e) {
            log.error("Failed to parse '" + parsingField + "' from value: " + str);
            throw e;
        }
    }
    /**
     * @param str
     * @return
     */
    private Date fromEpoch(final String str) {
        final long t = Long.parseLong(str);
        return new Date(t - TimeZone.getDefault().getOffset(t));
    }
    public static void main(final String[] args) {
        final String rawData = "358931060729765|1522255463121|\n"
            + "11160061|RT_T|23.58|69.34|-90|0.2052178548123738|99|1522255461770|3901|\n"
            + "11181929|RT_T|24.5|68.0|-41|2.1611482313284246E-4|100|1522255461769|3C01|\n"
            + "11181939|RT_T|24.5|68.0|-40|1.0485760000000006E-4|100|1522255462877|3C01|";

        final Bt04Message msg = new MessageParser().parse(rawData);
        System.out.println(msg.getImei());
    }
}
