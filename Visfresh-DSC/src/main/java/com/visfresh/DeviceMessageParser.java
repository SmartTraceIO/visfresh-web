/**
 *
 */
package com.visfresh;

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import au.smarttrace.gsm.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessageParser {
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * Default constructor.
     */
    public DeviceMessageParser() {
        super();
    }

    /**
     * @param msgData message data.
     * @return parsed device message.
     */
    public IncommingRequest parse(final String msgData) {
        final IncommingRequest messages = new IncommingRequest();
        messages.setRawData(msgData);

        final List<List<String>> splitted = splitToMessages(msgData.trim().split("\n"));

        for (final List<String> list : splitted) {
            final String[] lines = list.toArray(new String[list.size()]);

            final DeviceMessage msg = new DeviceMessage();
            final List<StationSignal> signals = new LinkedList<>();
            messages.addMessage(msg, signals);

            //first line
            //<IMEI>|<DATA_TYPE>|<TIME>|
            String[] line = lines[0].split(Pattern.quote("|"));
            msg.setImei(line[0]);
            msg.setTypeString(line[1]);
            msg.setType(getType(line[1]));
            msg.setTime(parseDate(line[2]));

            if (DeviceMessageType.RSP == msg.getType()) {
                msg.setMessage(lines[1]);
            } else {
                //second line
                //<BATTERY>|<TEMPERATURE>|
                line = lines[1].split(Pattern.quote("|"));
                msg.setBattery(Integer.parseInt(line[0]));
                msg.setTemperature(Double.parseDouble(line[1]));

                //stations
                for (int i = 2; i < lines.length; i++) {
                    //parse station
                    final StationSignal station = parseStationSignal(lines[i]);
                    signals.add(station);
                }
            }
        }

        return messages;
    }

    /**
     * @param typeString
     * @return
     */
    private DeviceMessageType getType(final String typeString) {
        try {
            return DeviceMessageType.valueOf(typeString);
        } catch (final Exception e) {
            return DeviceMessageType.UNDEF;
        }
    }

    /**
     * @param split
     * @return
     */
    private List<List<String>> splitToMessages(final String[] split) {
        final List<List<String>> list = new LinkedList<>();

        List<String> msg = null;
        for (final String str : split) {
            final String line = str.trim();
            if (line.length() == 0) {
                //stop of collect current message
                msg = null;
            } else  if (msg == null) {
                //start of collect new message.
                msg = new LinkedList<>();
                msg.add(line);
                list.add(msg);
            } else {
                //add line to current message
                msg.add(line);
            }
        }

        return list ;
    }

    /**
     * @param line
     * @return
     */
    public static StationSignal parseStationSignal(final String line) {
        final String[] splittedLine = line.split(Pattern.quote("|"));

        final StationSignal station = new StationSignal();
        //<MCC>|<MNC>|<LAC>|<CI>|<RXLEV>|
        station.setMcc(Integer.parseInt(splittedLine[0]));
        station.setMnc(Integer.parseInt(splittedLine[1]));
        station.setLac(Integer.parseInt(splittedLine[2]));
        station.setCi(Integer.parseInt(splittedLine[3]));
        station.setLevel(Integer.parseInt(splittedLine[4]));
        return station;
    }
    /**
     * @param dateString date string.
     * @return parsed date.
     */
    private Date parseDate(final String dateString) {
        // 2013/10/18 13:28:29
        final DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        format.setTimeZone(UTC);
        try {
            return format.parse(dateString);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param reader reader.
     * @return parsed device message.
     * @throws IOException
     */
    public IncommingRequest parse(final Reader reader) throws IOException {
        final String msgData = MessageParserUtils.getContent(reader);
        return parse(msgData);
    }
}
