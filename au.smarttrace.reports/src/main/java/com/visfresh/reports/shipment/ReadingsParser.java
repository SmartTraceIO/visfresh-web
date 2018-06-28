/**
 *
 */
package com.visfresh.reports.shipment;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.visfresh.controllers.DeviceController;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Language;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.utils.LocalizationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ReadingsParser {
    private ReadingsHandler handler;
    protected DateFormat dateParser = DeviceController.createCsvDateFormat(
          Language.English, TimeZone.getTimeZone("UTC"));

    /**
     * Default constructor.
     */
    public ReadingsParser() {
        super();
    }

    /**
     * @return the dateParser
     */
    public DateFormat getDateParser() {
        return dateParser;
    }
    /**
     * @param dateParser the dateParser to set
     */
    public void setDateParser(final DateFormat dateParser) {
        this.dateParser = dateParser;
    }
    /**
     * @param handler the handler to set
     */
    public void setHandler(final ReadingsHandler handler) {
        this.handler = handler;
    }
    /**
     * @return the handler
     */
    public ReadingsHandler getHandler() {
        return handler;
    }

    public void parse(final Reader r) throws IOException, ParseException {
        String[] headers = null;
        final LineNumberReader lnr = new LineNumberReader(r);

        String line;
        while ((line = lnr.readLine()) != null) {
            if (headers == null) {
                headers = parseHeaders(line);
            } else {
                final Map<String, String> reading = parseReading(line, headers);

                final AlertType[] alerts = getAlerts(reading);
                final ShortTrackerEvent e = buildEvent(reading);

                if (getHandler() != null) {
                    getHandler().handleEvent(e, alerts);
                }
            }
        }
    }

    /**
     * @param reading
     * @return
     * @throws ParseException
     */
    private ShortTrackerEvent buildEvent(final Map<String, String> reading) throws ParseException {
        final ShortTrackerEvent e = new ShortTrackerEvent();
//      id,shipment,time,temperature °C,battery,latitude,longitude,device,createdon,type,alerts
        e.setId(Long.parseLong(reading.remove("id")));

        final String shipment = reading.remove("shipment");
        if (shipment != null && shipment.length() > 0 && getHandler() != null) {
            //769(10)
            final int offset = shipment.indexOf('(');
            final String sn = shipment.substring(0, offset);
            final int tripCount = Integer.parseInt(shipment.substring(offset + 1, shipment.length() - 1));
            e.setShipmentId(getHandler().getShipmentId(sn, tripCount));
        }

        e.setTime(dateParser.parse(reading.remove("time")));
        e.setBattery(Integer.parseInt(reading.remove("battery")));
        final String humidity = reading.remove("humidity");
        if (humidity != null && !humidity.isEmpty()) {
            //cut percents symbol.
            final String hum = humidity.endsWith("%") ? humidity.substring(0, humidity.length() - 1) : humidity;
            e.setHumidity(Integer.parseInt(hum));
        }
        final String lat = reading.remove("latitude");
        if (!lat.isEmpty()) {
            e.setLatitude(Double.parseDouble(lat));
        }
        final String lon = reading.remove("longitude");
        if (!lon.isEmpty()) {
            e.setLongitude(Double.parseDouble(lon));
        }
        e.setDeviceImei(reading.remove("device"));
        e.setCreatedOn(dateParser.parse(reading.remove("createdon")));
        e.setType(getReadingType(reading.remove("type")));

        //temperature
        if (reading.size() != 1) {
            throw new ParseException("Unexpected data" + reading, 0);
        }
        final String temperatureHeader = reading.keySet().iterator().next();
        final TemperatureUnits units = getTemperatureUnits(temperatureHeader);

        final String value = reading.get(temperatureHeader);
        e.setTemperature(LocalizationUtils.convertFromUnits(Double.parseDouble(value), units));

        return e;
    }

    /**
     * @param h
     * @return
     * @throws ParseException
     */
    private TemperatureUnits getTemperatureUnits(final String h) throws ParseException {
//      temperature °C
        if (!h.startsWith("temperature")) {
            throw new ParseException("Unexpected temperature header: " + h, 0);
        }

        if (h.endsWith("F")) {
            return TemperatureUnits.Fahrenheit;
        }
        if (h.endsWith("C")) {
            return TemperatureUnits.Celsius;
        }
        throw new ParseException("Unexpected temperature header: " + h, 0);
    }

    /**
     * @param type
     * @return
     */
    private TrackerEventType getReadingType(final String type) {
//      case DRK:
//      return "LightOff";
        if ("LightOff".equals(type)) {
            return TrackerEventType.DRK;
        }
        //
//      case BRT:
//      return "LightOn";
        if ("LightOn".equals(type)) {
            return TrackerEventType.BRT;
        }
//      case INIT:
//      return "SwitchedOn";
        if ("SwitchedOn".equals(type)) {
            return TrackerEventType.INIT;
        }
//      case VIB:
//      return "Moving";
        if ("Moving".equals(type)) {
            return TrackerEventType.VIB;
        }
//      case AUT:
//      return "Reading";
        if ("Reading".equals(type)) {
            return TrackerEventType.AUT;
        }
//      case STP:
//      return "Stop";
        if ("Stop".equals(type)) {
            return TrackerEventType.STP;
        }
//      default:
//      return type.name();

        return TrackerEventType.valueOf(type);
    }

    /**
     * @param reading
     * @return
     */
    private AlertType[] getAlerts(final Map<String, String> reading) {
//        id,shipment,time,temperature °C,battery,latitude,longitude,device,createdon,type,alerts
        final String alerts = reading.remove("alerts");
        if (alerts == null || alerts.length() == 0) {
            return new AlertType[0];
        }

        final List<AlertType> result = new LinkedList<AlertType>();
        final String[] split = alerts.split(",");
        for (final String str : split) {
            result.add(AlertType.valueOf(str));
        }

        return result.toArray(new AlertType[result.size()]);
    }

    /**
     * @param line
     * @param headers
     * @return
     * @throws ParseException
     */
    private Map<String, String> parseReading(final String line, final String[] headers) throws ParseException {
        final Map<String, String> result = new HashMap<>();

        // escape commas inside of item
        final String escape = "_321_";
        final String[] split = splitByComma(escapeCommas(line, escape));
        if (split.length != headers.length) {
            throw new ParseException("Numbere of items "
                    + split.length
                    + " is not equals by headers number "
                    + headers.length
                    + " : " + line, 0);
        }

        for (int i = 0; i < split.length; i++) {
            final String key = headers[i];
            String value = split[i];
            if (value.startsWith("\"")) {
                value = value.substring(1, value.length() - 1);
                value = value.replace(escape, ",");
            }

            result.put(key, value);
        }

        return result;
    }

    /**
     * @param str
     * @return
     */
    private String[] splitByComma(final String str) {
        final String escape = "--<<>>>---";

        final String[] split = (escape + str + escape).split(",");
        split[0] = split[0].replace(escape, "");
        split[split.length - 1] = split[split.length - 1].replace(escape, "");

        return split;
    }

    /**
     * @param origin origin line.
     * @param escape escape string
     * @return
     */
    private String escapeCommas(final String origin, final String escape) {
        String line = origin;
        boolean found = false;
        int offset = 0;

        while (offset < line.length() - 1) {
            if (found) {
                final int end = line.indexOf('\"', offset + 1);
                if (end == -1) {
                    break;
                }

                final String oldStr = line.substring(offset + 1, end);
                final String newStr = oldStr.replace(",", escape);

                line = line.substring(0, offset + 1) + newStr + line.substring(end);
                offset += newStr.length() + 2;
                found = false;
            } else {
                offset = line.indexOf('\"', offset + 1);
                if (offset == -1) {
                    break;
                }
                found = true;
            }
        }

        return line;
    }

    /**
     * @param line
     * @return
     */
    private String[] parseHeaders(final String line) {
        return line.split(",");
    }
}
