/**
 *
 */
package com.visfresh.drools;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.AbstractJsonSerializer;
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
        final String str = bundle.getString(alert.getType().toString());
        return StringUtils.getMessage(str, createReplacementMap(alert, user));
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
        map.put("date", sdf.format(alert.getDate()));
        //${type} alert type
        map.put("type", alert.getType().toString());
        //${device} device IMEI
        map.put("device", alert.getDevice().getImei());
        //${devicesn} device serial number
        map.put("devicesn", alert.getDevice().getSn());
        //${tripCount} trip count for given device of shipment.
        map.put("tripCount", Integer.toString(alert.getShipment().getTripCount()));
        //
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
    private String getTemperatureString(final double t, final TemperatureUnits units) {
        double temp;
        String degree;
        switch (units) {
            case Fahrenheit:
                degree = "F";
                temp = t * 1.8 + 32;
                break;
                default:
                    degree = "C";
                    temp = t;
                    //nothing
                    break;
        }
        return new DecimalFormat("#0.0").format(temp) + degree;
    }
}
