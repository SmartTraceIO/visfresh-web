/**
 *
 */
package com.visfresh.rules;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.json.AbstractJsonSerializer;
import com.visfresh.l12n.XmlControl;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertDescriptionBuilder {
    /**
     * Default constructor.
     */
    public AlertDescriptionBuilder() {
        super();
    }

    /**
     *  supported place holders:
     *    ${date} alert issue date include day and year
     *    ${time} the time in scope of day.
     *    ${type} alert type
     *    ${device} device IMEI
     *    ${devicesn} device serial number
     *    ${tripCount} trip count for given device of shipment.
     *
     *  for temperature alerts:
     *    ${temperature}
     *    ${period}
     *
     * @param alert alert
     * @param user target user.
     * @return description for given alert.
     */
    public String buildDescription(final Alert alert, final User user) {
        final ResourceBundle bundle = ResourceBundle.getBundle("alerts", XmlControl.INSTANCE);
        final String str = bundle.getString(createBundleKey(alert));
        return StringUtils.getMessage(str, createReplacementMap(alert, user));
    }

    /**
     * @param alert
     * @return
     */
    private String createBundleKey(final Alert alert) {
        String key = alert.getType().toString();
        if (alert instanceof TemperatureAlert && ((TemperatureAlert) alert).isCumulative()) {
            key += ".cumulative";
        }
        return key;
    }
    /**
     * @param alert alert.
     * @return map of replacements.
     */
    private Map<String, String> createReplacementMap(final Alert alert, final User user) {
        final Map<String, String> map = new HashMap<String, String>();
        //supported place holders:
        //${date} alert issue date include day and year
        SimpleDateFormat sdf = new SimpleDateFormat(AbstractJsonSerializer.DATE_FORMAT);
        sdf.setTimeZone(user.getTimeZone());
        map.put("date", sdf.format(alert.getDate()));
        //${time} the time in scope of day.
        sdf = new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(user.getTimeZone());
        map.put("time", sdf.format(alert.getDate()));
        //${type} alert type
        map.put("type", alert.getType().toString());
        //${device} device IMEI
        map.put("device", alert.getDevice().getImei());
        //${devicesn} device serial number
        map.put("devicesn", alert.getDevice().getSn());
        //${tripCount} trip count for given device of shipment.
        map.put("tripCount", Integer.toString(alert.getShipment().getTripCount()));

        //for temperature alerts:
        if (alert instanceof TemperatureAlert) {
            final TemperatureAlert ta = (TemperatureAlert) alert;
            //${temperature}
            map.put("temperature", getTemperatureString(ta.getTemperature(), user.getTemperatureUnits()));
            //${period}
            map.put("period", Integer.toString(ta.getMinutes()));
        }

        return map;
    }
    /**
     * @param t temperature.
     * @param units temperature units.
     * @return temperature string.
     */
    public static String getTemperatureString(final double t, final TemperatureUnits units) {
        final double temp = getTemperature(t, units);
        String degree;
        switch (units) {
            case Fahrenheit:
                degree = "\u00B0F";
                break;
                default:
                    degree = "\u00B0C";
                    //nothing
                    break;
        }

        //create US locale decimal format
        final DecimalFormat fmt = new DecimalFormat("#0.0");
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
        fmt.setDecimalFormatSymbols(decimalFormatSymbols);

        //format temperature string
        return fmt.format(temp) + degree;
    }

    /**
     * @param tCelsium
     * @param units
     * @return
     */
    public static double getTemperature(final double tCelsium, final TemperatureUnits units) {
        double temp;
        switch (units) {
            case Fahrenheit:
                temp = tCelsium * 1.8 + 32;
                break;
                default:
                    temp = tCelsium;
                    //nothing
                    break;
        }
        return temp;
    }
}
