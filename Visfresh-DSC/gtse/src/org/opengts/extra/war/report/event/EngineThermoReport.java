// ----------------------------------------------------------------------------
// Copyright 2007-2015, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// This source module is PROPRIETARY and CONFIDENTIAL.
// NOT INTENDED FOR PUBLIC RELEASE.
// 
// Use of this software is subject to the terms and conditions outlined in
// the 'Commercial' license provided with this software.  If you did not obtain
// a copy of the license with this software please request a copy from the
// Software Provider.
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Change History:
//  2015/02/04  Martin D. Flynn
//     -Initial release (cloned from "EventThermoReport.java")
// ----------------------------------------------------------------------------
package org.opengts.extra.war.report.event;

import java.io.*;
import java.util.*;
import java.awt.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.google.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.report.*;
import org.opengts.war.report.event.*;

public class EngineThermoReport
    extends EventDetailReport
{

    // ------------------------------------------------------------------------

    /* properties */
    private static final String  PROP_graph_titleColor      = "graph.titleColor";
    private static final String  PROP_graph_titleFontSize   = "graph.titleFontSize";
    private static final String  PROP_graph_xTickCount      = "graph.xTickCount";
    private static final String  PROP_graph_yTickCount      = "graph.yTickCount";
    private static final String  PROP_zeroCIsInvalid        = "zeroCIsInvalid";

    // ------------------------------------------------------------------------

    /* special/virtual temperature fields */
    private static final String  VFLD_tireTemp_             = "$" + EventData.FLD_tireTemp + ":";

    // ------------------------------------------------------------------------

    private static class ThermoColumn
    {
        private String    rptColumn  = null;
        private String    fieldName  = null;
        private I18N.Text fieldLabel = null;
        public ThermoColumn(String rptColumn, String fieldName, I18N.Text fieldLabel) {
            this.rptColumn  = StringTools.trim(rptColumn);
            this.fieldName  = StringTools.trim(fieldName);
            this.fieldLabel = fieldLabel;
        }
        public String getReportColumn() {
            return this.rptColumn;
        }
        public String getFieldName() {
            return this.fieldName;
        }
        public String getFieldLabel(Locale locale, String dft) {
            if (this.fieldLabel == null) {
                return dft;
            } else {
                return this.fieldLabel.toString(locale);
            }
        }
        public double getFieldValue(EventData ev) { // <== returns a temperature in degrees-C
            String fn = this.getFieldName();
            if (StringTools.isBlank(fn)) {
                // -- unlikely
                return EventData.INVALID_TEMPERATURE; // unlikely
            } else
            if (fn.startsWith("$")) {
                if (fn.startsWith(VFLD_tireTemp_)) {
                    // -- tire temperatures
                    int p = fn.indexOf(":");
                    if (p > 0) {
                        int n = StringTools.parseInt(fn.substring(p+1),-1);
                        if (n >= 0) {
                            double TT[] = ev.getTireTemp_C();
                            if (n < TT.length) {
                                return TT[n];
                            }
                        }
                    }
                }
                return EventData.INVALID_TEMPERATURE;
            } else {
                // -- standard field
                Object obj = ev.getFieldValue(fn);
                if (obj instanceof Number) {
                    return ((Number)obj).doubleValue();
                } else {
                    return EventData.INVALID_TEMPERATURE;
                }
            }
        }
        public void setFieldValue(EventData ev, double value) {
            ev.setFieldValue(this.getFieldName(), value);
        }
    }

    /**
    *** The available temperature columns
    **/
    private static final ThermoColumn COL_THERMO[] = new ThermoColumn[] {
        // --
        new ThermoColumn(EventDataLayout.DATA_COOLANT_TEMP      , EventData.FLD_coolantTemp   , I18N.getString(EngineThermoReport.class,"EngineThermoReport.CoolantTemp"  ,"Coolant"     )),
        new ThermoColumn(EventDataLayout.DATA_BATTERY_TEMP      , EventData.FLD_batteryTemp   , I18N.getString(EngineThermoReport.class,"EngineThermoReport.BatteryTemp"  ,"Battery"     )),
        new ThermoColumn(EventDataLayout.DATA_OIL_TEMP          , EventData.FLD_oilTemp       , I18N.getString(EngineThermoReport.class,"EngineThermoReport.OilTemp"      ,"Oil"         )),
        new ThermoColumn(EventDataLayout.DATA_TRANS_OIL_TEMP    , EventData.FLD_transOilTemp  , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TransOilTemp" ,"Transmission")),
        new ThermoColumn(EventDataLayout.DATA_INTAKE_TEMP       , EventData.FLD_intakeTemp    , I18N.getString(EngineThermoReport.class,"EngineThermoReport.IntakeTemp"   ,"Intake"      )),
        new ThermoColumn(EventDataLayout.DATA_AMBIENT_TEMP      , EventData.FLD_ambientTemp   , I18N.getString(EngineThermoReport.class,"EngineThermoReport.AmbientTemp"  ,"Ambient"     )),
        new ThermoColumn(EventDataLayout.DATA_CABIN_TEMP        , EventData.FLD_cabinTemp     , I18N.getString(EngineThermoReport.class,"EngineThermoReport.CabinTemp"    ,"Cabin"       )),
        // --
        new ThermoColumn(EventDataLayout.DATA_THERMO_1          , EventData.FLD_thermoAverage0, I18N.getString(EngineThermoReport.class,"EngineThermoReport.Temp_1"       ,"Temp-1"      )),
        new ThermoColumn(EventDataLayout.DATA_THERMO_2          , EventData.FLD_thermoAverage1, I18N.getString(EngineThermoReport.class,"EngineThermoReport.Temp_2"       ,"Temp-2"      )),
        new ThermoColumn(EventDataLayout.DATA_THERMO_3          , EventData.FLD_thermoAverage2, I18N.getString(EngineThermoReport.class,"EngineThermoReport.Temp_3"       ,"Temp-3"      )),
        new ThermoColumn(EventDataLayout.DATA_THERMO_4          , EventData.FLD_thermoAverage3, I18N.getString(EngineThermoReport.class,"EngineThermoReport.Temp_4"       ,"Temp-4"      )),
        new ThermoColumn(EventDataLayout.DATA_THERMO_5          , EventData.FLD_thermoAverage4, I18N.getString(EngineThermoReport.class,"EngineThermoReport.Temp_5"       ,"Temp-5"      )),
        new ThermoColumn(EventDataLayout.DATA_THERMO_6          , EventData.FLD_thermoAverage5, I18N.getString(EngineThermoReport.class,"EngineThermoReport.Temp_6"       ,"Temp-6"      )),
        new ThermoColumn(EventDataLayout.DATA_THERMO_7          , EventData.FLD_thermoAverage6, I18N.getString(EngineThermoReport.class,"EngineThermoReport.Temp_7"       ,"Temp-7"      )),
        new ThermoColumn(EventDataLayout.DATA_THERMO_8          , EventData.FLD_thermoAverage7, I18N.getString(EngineThermoReport.class,"EngineThermoReport.Temp_8"       ,"Temp-8"      )),
        // --
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_1, VFLD_tireTemp_+ 1           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_01"  ,"Tire-01"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_2, VFLD_tireTemp_+ 2           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_02"  ,"Tire-02"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_3, VFLD_tireTemp_+ 3           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_03"  ,"Tire-03"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_4, VFLD_tireTemp_+ 4           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_04"  ,"Tire-04"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_5, VFLD_tireTemp_+ 5           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_05"  ,"Tire-05"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_6, VFLD_tireTemp_+ 6           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_06"  ,"Tire-06"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_1, VFLD_tireTemp_+ 7           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_07"  ,"Tire-07"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_1, VFLD_tireTemp_+ 8           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_08"  ,"Tire-08"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_1, VFLD_tireTemp_+ 9           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_09"  ,"Tire-09"     )),
        new ThermoColumn(EventDataLayout.DATA_TIRE_TEMPERATURE_1, VFLD_tireTemp_+10           , I18N.getString(EngineThermoReport.class,"EngineThermoReport.TireTemp_10"  ,"Tire-10"     )),
        // --
    };

    // ------------------------------------------------------------------------

    /**
    *** The data-set line colors
    **/
    private static Color TEMP_COLOR[] = new Color[] { 
        ColorTools.red, 
        ColorTools.green, 
        ColorTools.blue, 
        ColorTools.cyan, 
        ColorTools.yellow, 
        ColorTools.gray, 
        ColorTools.black, 
        ColorTools.orange, 
        ColorTools.magenta, 
        ColorTools.purple, 
        ColorTools.darkYellow, 
        ColorTools.pink, 
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Converts the specified Fahrenheit temperture to Celsius
    **/
    private static double F2C(double F)
    {
        return (F - 32.0) * 5 / 9;
    }

    /**
    *** Converts the specified Celsius temperture to Fahrenheit
    **/
    private static double C2F(double C)
    {
        return (C * 9 / 5) + 32.0;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private boolean     zeroCIsInvalid      = false;

    /**
    *** Event Detail Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public EngineThermoReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {

        /* properties */
        RTProperties rtp = this.getProperties();
        this.zeroCIsInvalid = rtp.getBoolean(PROP_zeroCIsInvalid, this.zeroCIsInvalid);

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified temperature is valid, false otherwise
    **/
    private boolean isValidTemperature(double C)
    {
        if (!EventData.isValidTemperature(C)) {
            // -- not a valid temperature
            return false;
        } else
        if (C != 0.0) {
            // -- valid and not zero
            return true;
        } else {
            // -- temperature is 0-C
            return this.zeroCIsInvalid? false : true;
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Creates and returns an iterator for the row data displayed in the body of this report.
    *** @return The body row data iterator
    **/
    public DBDataIterator getBodyDataIterator()
    {
        DBDataIterator dbi = super.getBodyDataIterator();
        if (dbi instanceof ArrayDataIterator) {
            ArrayDataIterator adi = (ArrayDataIterator)dbi;
            Object oa[] = adi.getArray();
            if (!ListTools.isEmpty(oa)) {
                int tempMask = 0;
                // -- find temperature set with/without valid data
                for (int i = 0; i < oa.length; i++) {
                    EventData ev = (EventData)oa[i];
                    for (int t = 0; t < COL_THERMO.length; t++) {
                      //double C = ev.getThermoAverage(t);
                        double C = COL_THERMO[t].getFieldValue(ev);
                        if (this.isValidTemperature(C)) {
                            tempMask |= 1 << t;
                        } else {
                            Print.logDebug("Invalid TempC: " + COL_THERMO[t].getFieldName() + " ==> " + C);
                        }
                    }
                }
                // -- clear all temperatures with invalid data (local only, not updated)
                for (int i = 0; i < oa.length; i++) {
                    EventData ev = (EventData)oa[i];
                    for (int t = 0; t < COL_THERMO.length; t++) {
                        if ((tempMask & (1 << t)) == 0) {
                            //ev.setThermoAverage(t,EventData.INVALID_TEMPERATURE);
                            COL_THERMO[t].setFieldValue(ev, EventData.INVALID_TEMPERATURE);
                        }
                    }
                }
            }
        }
        return dbi;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this report supports displaying a graph
    *** @return True if this report supports displaying a graph, false otherwise
    **/
    public boolean getSupportsGraphDisplay()
    {
        return true;
    }

    /**
    *** Graph window size
    **/
    public MapDimension getGraphWindowSize()
    {
        return new MapDimension(800,440);
    }

    // ------------------------------------------------------------------------

    /**
    *** Writes the required JavaScript
    **/
    public void writeJavaScript(PrintWriter pw, RequestProperties reqState)
    {
        try {
            this._writeJavaScript(pw, reqState);
        } catch (Throwable th) {
            Print.logException("Error", th);
        }
    }

    /**
    *** Writes the HTML body
    **/
    public void writeHtmlBody(PrintWriter pw, RequestProperties reqState)
    {
        try {
            pw.write("<div id='graph_div' style='width:100%; height:100%;'></div>\n");
        } catch (Throwable th) {
            Print.logException("Error", th);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the Temperature data sets
    **/
    private TemperatureSet[] getTemperatureData(Locale locale)
    {

        /* which temperature columns does this report have? */
        int thermoCount = 0;
        TemperatureSet dataSet[] = new TemperatureSet[COL_THERMO.length];
        boolean dataSetValid[] = new boolean[COL_THERMO.length];
        for (int i = 0; i < COL_THERMO.length; i++) {
            if (this.hasReportColumn(COL_THERMO[i].getReportColumn())) {
                String label = COL_THERMO[i].getFieldLabel(locale,null);
                dataSet[i] = new TemperatureSet(label);
                thermoCount++;
            } else {
                Print.logDebug("Report column not found: " + COL_THERMO[i].getReportColumn());
                dataSet[i] = null;
            }
            dataSetValid[i] = false; // verified below
        }

        /* fill datasets */
        if (thermoCount > 0) {
            int evNdx = 0;
            for (DBDataIterator dbi = this.getBodyDataIterator(); dbi.hasNext();) {
                Object ev = dbi.next().getRowObject();
                if (ev instanceof EventData) {
                    EventData ed = (EventData)ev;
                    ed.setEventIndex(evNdx++);
                    if (!dbi.hasNext()) { ed.setIsLastEvent(true); }
                    double lastTempC = EventData.INVALID_TEMPERATURE;
                    for (int d = 0; d < dataSet.length; d++) {
                        if (dataSet[d] != null) {
                            long   ts    = ed.getTimestamp();
                            double tempC = COL_THERMO[d].getFieldValue(ed);
                            if (this.isValidTemperature(tempC)) {
                                dataSet[d].addTemperature(ts, tempC);
                                if (tempC != 0.0) {
                                    // -- contains a valid temperature that is not 0.0
                                    dataSetValid[d] = true; // contains valid data
                                }
                                lastTempC = tempC;
                            } else {
                                Print.logDebug("Report column '"+COL_THERMO[d].getFieldName()+"' has invalid temp value");
                            }
                        }
                    }
                } else {
                    Print.logWarn("Not an EventData instance: " + StringTools.className(ev));
                }
            }
        } else {
            Print.logWarn("No temperature columns");
        }

        /* clear datasets with no data */
        int maxLen = 0;
        for (int d = 0; d < dataSet.length; d++) {
            if (!dataSetValid[d]) {
                if (dataSet[d] != null) {
                    Print.logWarn("Data set contains no valid temperatures: " + COL_THERMO[d].getFieldName());
                    dataSet[d] = null; // clear
                }
            } else {
                maxLen = d + 1;
            }
        }
        if (maxLen < dataSet.length) {
            TemperatureSet newTS[] = new TemperatureSet[maxLen];
            System.arraycopy(dataSet,0,newTS,0,maxLen);
            dataSet = newTS;
        }

        /* min/max */
        /*
        int thermoCount = 0;
        double maxTempC = -9999.0;
        double minTempC =  9999.0;
        for (int d = 0; d < dataSet.length; d++) {
            if (dataSet[d] != null) {
                thermoCount++;
                for (TemperatureSet TS : dataSet[d]) {
                    double minC = TS.getMinimumTemperature();
                    double maxC = TS.getMaximumTemperature();
                    if (tempC < minC) { minTempC = minC; }
                    if (tempC > maxC) { maxTempC = maxC; }
                }
            }
        }
        */

        /* return */
        if (ListTools.isEmpty(dataSet)) {
            Print.logWarn("No temperature data sets");
        } else {
            Print.logInfo("Returning "+dataSet.length+" temperature data set(s)");
        }
        return dataSet;

    }

    /**
    *** Writes the Graph JavaScript to the HTTP stream
    **/
    public void _writeJavaScript(PrintWriter pw, RequestProperties reqState)
        throws Exception
    {
        TimeZone tz        = this.getTimeZone();
        DateTime timeStart = new DateTime(this.getTimeStart(),tz);
        DateTime timeEnd   = new DateTime(this.getTimeEnd(),tz);
        ReportLayout rl    = this.getReportLayout();
        Locale locale      = this.getPrivateLabel().getLocale();
        I18N i18n          = I18N.getI18N(EngineThermoReport.class, locale);

        /* Temperature units */
        Account.TemperatureUnits TU = Account.getTemperatureUnits(this.getAccount());
        String TUStr = TU.toString(locale);

        /* get temperature data */
        String title = i18n.getString("EngineThermoReport.temperature","Temperature") + " (" + TUStr + ")";
        TemperatureSet TS[] = this.getTemperatureData(locale);
        JavaScriptTools.writeStartJavaScript(pw);
        JavaScriptTools.writeJSVar(pw, "TEMP_TITLE", title);
        JavaScriptTools.writeJSVar(pw, "TEMP_DATA", TemperatureSet.CreateGoogleDataTableJavaScript(TU.isF(),TS),false);
        JavaScriptTools.writeEndJavaScript(pw);

        /* include Google Chart JS */
        String chartJSInclude = "https://www.google.com/jsapi";
        JavaScriptTools.writeJSInclude(pw, chartJSInclude, reqState.getHttpServletRequest());

        /* load chart JS */
        JavaScriptTools.writeStartJavaScript(pw);
        pw.write("google.load('visualization', '1', { packages: ['corechart'] });\n");
        pw.write("google.setOnLoadCallback(drawTempChart);\n");
        pw.write("function drawTempChart() {\n");
        pw.write("  var data = new google.visualization.DataTable(TEMP_DATA);\n");
        pw.write("  var options = {\n");
        pw.write("    title: TEMP_TITLE,\n");
      //pw.write("    backgroundColor: 'white',\n");
      //pw.write("    colors: [ 'green', 'orange' ],\n");
      //pw.write("    dataOpacity: 1.0,\n");
      //pw.write("    lineWidth: 2,\n");
        pw.write("    pointSize: 5,\n");
        pw.write("    interpolateNulls: true\n");
        pw.write("  };\n");
        pw.write("  var chart = new google.visualization.LineChart(document.getElementById('graph_div'));\n");
        pw.write("  chart.draw(data, options);\n");
        pw.write("}\n");
        JavaScriptTools.writeEndJavaScript(pw);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
