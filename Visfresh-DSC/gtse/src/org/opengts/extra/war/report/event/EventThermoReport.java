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
//  2008/12/01  Martin D. Flynn
//     -Initial release
//  2009/07/01  Martin D. Flynn
//     -Repackaged
//  2012/04/03  Martin D. Flynn
//     -Accept 0-C as a valid temperature.
//  2013/12/29  Martin D. Flynn
//     -Added support for Google JavaScript charting
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

public class EventThermoReport
    extends EventDetailReport
{

    // ------------------------------------------------------------------------

    private static final boolean USE_IMAGE_GRAPH        = false;

    private static final String  PROP_TITLE_COLOR       = "graph.titleColor";
    private static final String  PROP_TITLE_FONT_SIZE   = "graph.titleFontSize";
    private static final String  PROP_X_TICK_COUNT      = "graph.xTickCount";
    private static final String  PROP_Y_TICK_COUNT      = "graph.yTickCount";

    // ------------------------------------------------------------------------

    private static final String COL_THERMO[] = new String[] {
        EventDataLayout.DATA_THERMO_1,
        EventDataLayout.DATA_THERMO_2,
        EventDataLayout.DATA_THERMO_3,
        EventDataLayout.DATA_THERMO_4,
        EventDataLayout.DATA_THERMO_6,
        EventDataLayout.DATA_THERMO_7,
        EventDataLayout.DATA_THERMO_8,
    };
    
    private static Color TEMP_COLOR[] = new Color[] { 
        Color.red, Color.green, Color.blue, Color.cyan, Color.yellow, Color.gray, Color.black,
    };

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* convert F to C */
    private static double F2C(double F)
    {
        return (F - 32.0) * 5 / 9;
    }

    /* convert C to F */
    private static double C2F(double C)
    {
        return (C * 9 / 5) + 32.0;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Event Detail Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public EventThermoReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
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
                    for (int t = 0; t < 8; t++) {
                        double C = ev.getThermoAverage(t);
                        if (EventData.isValidTemperature(C)/* && (C != 0.0)*/) {
                            tempMask |= 1 << t;
                        } else {
                            //Print.logInfo("Invalid TempC: " + t + " " + C);
                        }
                    }
                }
                // -- clear all temperatures with invalid data
                for (int i = 0; i < oa.length; i++) {
                    EventData ev = (EventData)oa[i];
                    for (int t = 0; t < 8; t++) {
                        if ((tempMask & (1 << t)) == 0) {
                            ev.setThermoAverage(t,EventData.INVALID_TEMPERATURE);
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
        if (USE_IMAGE_GRAPH) {
            return new MapDimension(680,440); // max 300000 pixels
        } else {
            return new MapDimension(800,440);
        }
    }

    // ------------------------------------------------------------------------

    public void writeJavaScript(PrintWriter pw, RequestProperties reqState)
    {
        if (USE_IMAGE_GRAPH) {
            // -- image does not require JavaScript
        } else {
            try {
                this._writeJavaScript(pw, reqState);
            } catch (Throwable th) {
                Print.logException("Error", th);
            }
        }
    }

    public void writeHtmlBody(PrintWriter pw, RequestProperties reqState)
    {
        try {
            if (USE_IMAGE_GRAPH) {
                // -- OBSOLETE
                URIArg imageURL = this.getGraphImageURL(); // OBSOLETE
                pw.write("<img src='"+imageURL+"'/>\n");
            } else {
                pw.write("<div id='graph_div' style='width:100%; height:100%;'></div>\n");
            }
        } catch (Throwable th) {
            Print.logException("Error", th);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the Temperature data sets
    **/
    private TemperatureSet[] getTemperatureData()
    {

        /* which "thermoX" columns does this report have */
        int thermoCount = 0;
        TemperatureSet dataSet[] = new TemperatureSet[COL_THERMO.length];
        boolean dataSetValid[] = new boolean[COL_THERMO.length];
        for (int i = 0; i < COL_THERMO.length; i++) {
            if (this.hasReportColumn(COL_THERMO[i])) {
                dataSet[i] = new TemperatureSet();
                thermoCount++;
            } else {
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
                            double tempC = ed.getThermoAverage(d);
                            if (EventData.isValidTemperature(tempC)) {
                                dataSet[d].addTemperature(ts, tempC);
                                if (tempC != 0.0) {
                                    // contains a valid temperature that is not 0.0
                                    dataSetValid[d] = true; // contains valid data
                                }
                                lastTempC = tempC;
                            }
                        }
                    }
                } else {
                    Print.logWarn("Not an EventData instance: " + StringTools.className(ev));
                }
            }
        }

        /* clear datasets with no data */
        int maxLen = 0;
        for (int d = 0; d < dataSet.length; d++) {
            if (!dataSetValid[d]) {
                dataSet[d] = null;
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
        return dataSet;

    }

    public void _writeJavaScript(PrintWriter pw, RequestProperties reqState)
        throws Exception
    {
        TimeZone tz        = this.getTimeZone();
        DateTime timeStart = new DateTime(this.getTimeStart(),tz);
        DateTime timeEnd   = new DateTime(this.getTimeEnd(),tz);
        ReportLayout rl    = this.getReportLayout();
        Locale locale      = this.getPrivateLabel().getLocale();
        I18N i18n          = I18N.getI18N(EventThermoReport.class, locale);

        /* Temperature units */
        Account.TemperatureUnits TU = Account.getTemperatureUnits(this.getAccount());
        String TUStr = TU.toString(locale);

        /* get temperature data */
        String title = i18n.getString("EventThermoReport.temperature","Temperature") + " (" + TUStr + ")";
        TemperatureSet TS[] = this.getTemperatureData();
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
    // The Graph Image linechart support below is obsolete, due to its many
    // limitations.

    public URIArg getGraphImageURL() // OBSOLETE
    {
        if (USE_IMAGE_GRAPH) {
            // -- OBSOLETE
            try {
                return this._getImageURL();
            } catch (Throwable th) {
                Print.logException("Error", th);
                return new URIArg("");
            }
        } else {
            return null;
        }
    }

    private URIArg _getImageURL() // OBSOLETE
        throws Exception
    {
        TimeZone tz        = this.getTimeZone();
        DateTime timeStart = new DateTime(this.getTimeStart(),tz);
        DateTime timeEnd   = new DateTime(this.getTimeEnd(),tz);
        ReportLayout rl    = this.getReportLayout();
        String dateFormat  = "MM/dd";
        String timeFormat  = rl.getTimeFormat(this.getPrivateLabel());
        MapDimension size  = this.getGraphWindowSize();
        Locale locale      = this.getPrivateLabel().getLocale();
        I18N i18n          = I18N.getI18N(EventThermoReport.class, locale);

        /* Temperature units */
        Account.TemperatureUnits TU = Account.getTemperatureUnits(this.getAccount());
        String TUStr = TU.toString(locale);

        /* which "thermoX" columns does this report have */
        int thermoCount = 0;
        @SuppressWarnings("unchecked")
        Vector<Temperature> dataSet[] = new Vector[COL_THERMO.length];
        boolean dataSetValid[] = new boolean[COL_THERMO.length];
        for (int i = 0; i < COL_THERMO.length; i++) {
            if (this.hasReportColumn(COL_THERMO[i])) {
                dataSet[i] = new Vector<Temperature>();
                thermoCount++;
            } else {
                dataSet[i] = null;
            }
            dataSetValid[i] = false; // verified below
        }

        /* fill datasets */
        double maxTempC = -9999.0;
        double minTempC =  9999.0;
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
                            long ts = ed.getTimestamp();
                            double tempC = ed.getThermoAverage(d);
                            if (EventData.isValidTemperature(tempC)) {
                                if (tempC > maxTempC) { maxTempC = tempC; } // maximum temp encountered
                                if (tempC < minTempC) { minTempC = tempC; } // minimum temp encountered
                                dataSet[d].add(new Temperature(ts, tempC));
                                if (tempC != 0.0) {
                                    // contains a valid temperature that is not 0.0
                                    dataSetValid[d] = true; // contains valid data
                                }
                                lastTempC = tempC;
                            } else
                            if (EventData.isValidTemperature(lastTempC)) {
                                dataSet[d].add(new Temperature(ts, lastTempC));
                            } else {
                                dataSet[d].add(new Temperature(ts, 0.0));
                            }
                        }
                    }
                } else {
                    Print.logWarn("Not an EventData: " + StringTools.className(ev));
                }
            }
        }

        /* clear datasets with no data */
        for (int d = 0; d < dataSet.length; d++) {
            if (!dataSetValid[d]) {
                dataSet[d] = null;
            }
        }

        /* adjust min/max temperatures */
        if (!EventData.isValidTemperature(minTempC) || !EventData.isValidTemperature(maxTempC)) {
            minTempC =  0.0; //  32.0 F
            maxTempC = 54.0; // 122.0 F
        } else
        if (TU.isC()) {
            minTempC = Math.floor(minTempC / 10.0) * 10.0;
            maxTempC = Math.ceil( maxTempC / 10.0) * 10.0;
        } else
        if (TU.isF()) {
            minTempC = F2C(Math.floor(C2F(minTempC) / 10.0) * 10.0);
            maxTempC = F2C(Math.ceil( C2F(maxTempC) / 10.0) * 10.0);
        } else {
            minTempC = Math.floor(minTempC / 10.0) * 10.0;
            maxTempC = Math.ceil( maxTempC / 10.0) * 10.0;
        }

        /* init graph */
        Color titleColor  = ColorTools.parseColor(this.getProperties().getString(PROP_TITLE_COLOR,"black"),Color.black);
        int titleFontSize = this.getProperties().getInt(PROP_TITLE_FONT_SIZE, 16);
        int xTickCount    = this.getProperties().getInt(PROP_X_TICK_COUNT   , 10);
        int yTickCount    = this.getProperties().getInt(PROP_Y_TICK_COUNT   ,  8);
        GoogleChartTemperature gct = new GoogleChartTemperature();
        gct.setSize(size.getWidth() - 30, size.getHeight() - 40);
        gct.setTitle(titleColor, titleFontSize, i18n.getString("EventThermoReport.temperature","Temperature"));
        gct.setTemperatureRange(this.getAccount().getTemperatureUnits(), minTempC, maxTempC, xTickCount);
        gct.setDateRange(timeStart, timeEnd, yTickCount);
        gct.setDateFormat("MM/dd");
        gct.setTimeFormat(timeFormat);

        /* add datasets */
        if (thermoCount > 0) {
            Color tempColor[] = TEMP_COLOR;
            for (int d = 0; d < dataSet.length; d++) {
                if (dataSet[d] != null) {
                    Color color = tempColor[d % tempColor.length];
                    String title = i18n.getString("EventThermoReport.temp","Temp {0}",String.valueOf(d+1));
                    gct.addDataSet(color, title, dataSet[d].toArray(new Temperature[dataSet[d].size()]));
                }
            }
        } else {
            gct._addRandomSampleData(4,15);
        }

        /* return URL */
        String url = gct.toString();
        //Print.logInfo("URL:\n" + url);
        return new URIArg(url);

    }

    // ------------------------------------------------------------------------

}
