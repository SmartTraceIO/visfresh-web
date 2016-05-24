/**
 *
 */
package com.visfresh.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.TemperatureUnits;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class LocalizationUtils {

    /**
     * Default constructor.
     */
    private LocalizationUtils() {
        super();
    }
    /**
     * @param tCelsium
     * @param units
     * @return
     */
    public static double convertToUnits(final double tCelsium, final TemperatureUnits units) {
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
    public static double convertFromUnits(final double tInUnits, final TemperatureUnits units) {
        double temp;
        switch (units) {
            case Fahrenheit:
                temp = (tInUnits - 32) / 1.8;
                break;
                default:
                    temp = tInUnits;
                    //nothing
                    break;
        }
        return temp;
    }
    /**
     * @param t temperature.
     * @param units temperature units.
     * @return temperature string.
     */
    public static String getTemperatureString(final double t, final TemperatureUnits units) {
        final double temp = convertToUnits(t, units);
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
     * @param distance distance in meters.
     * @param measurementUnits measurement units.
     * @return
     */
    public static String getDistanceString(final int distance,
            final MeasurementUnits measurementUnits) {
        final double d = getDistance(distance, measurementUnits);

        String units;
        switch (measurementUnits) {
            case English:
                //to foots
                units = "foots";
                break;
            case Metric:
                units = "m";
                break;
                default:
                    throw new IllegalArgumentException("Unsupported measurement units: " + measurementUnits);
        }

        return Math.round(d) + units;
    }
    /**
     * @param distance distance in meters.
     * @param measurementUnits measurement units.
     * @return distance in given metric system.
     */
    public static double getDistance(final int distance,
            final MeasurementUnits measurementUnits) {
        switch (measurementUnits) {
            case English:
                //to foots
                return distance * 0.3048;
            case Metric:
                return distance;
                default:
                    throw new IllegalArgumentException("Unsupported measurement units: " + measurementUnits);
        }
    }
}

