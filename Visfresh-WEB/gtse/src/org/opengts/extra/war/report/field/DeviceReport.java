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
//  2009/08/23  Martin D. Flynn
//     -Initial release
//  2011/12/06  Martin D. Flynn
//     -Updated start/stop odometer calculation. [B32]
//  2014/11/30  Martin D. Flynn
//     -Added check for minimum check-in age filter (including "never").
//  2015/01/04  Martin D. Flynn
//     -Added smarter "countStatusCodes" parsing.
// ----------------------------------------------------------------------------
package org.opengts.extra.war.report.field;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.report.*;
import org.opengts.war.report.field.*;

public class DeviceReport
    extends ReportData
{

    // ------------------------------------------------------------------------
    // Properties

    private static final String PROP_includeDeltaValues     = "includeDeltaValues";
    private static final String PROP_countStatusCodes       = "countStatusCodes";
    private static final String PROP_minimumCheckInAge      = "minimumCheckInAge";

    // ------------------------------------------------------------------------
    // Summary report
    // 1 'maintenance' record per device
    // ------------------------------------------------------------------------

    private java.util.List<FieldData>   rowData             = null;

    private boolean                     includeDeltaValues  = false;
    private int                         countStatusCodes[]  = null;
    private long                        minCheckInAgeSec    = 0L;

    // ------------------------------------------------------------------------

    /**
    *** Device Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public DeviceReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        if (this.getAccount() == null) {
            throw new ReportException("Account-ID not specified");
        }
        //if (this.getDeviceCount() < 1) {
        //    throw new ReportException("At least 1 Device must be specified");
        //}
        // -- report on all authorized devices
        //this.getReportDeviceList().addAllAuthorizedDevices();
    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {
        RTProperties rtp = this.getProperties();

        /* include detla values */
        this.includeDeltaValues = rtp.getBoolean(PROP_includeDeltaValues, false);

        /* list of status codes to count */
        //this.countStatusCodes = rtp.getIntArray(PROP_countStatusCodes, null);
        String scArry[] = rtp.getStringArray(PROP_countStatusCodes, null);
        if (!ListTools.isEmpty(scArry)) {
            Vector<Integer> scL = new Vector<Integer>();
            for (String sca : scArry) {
                // -- skip invalid entries
                if (StringTools.isBlank(sca)) {
                    // -- skip blank entries
                    continue;
                } else
                if (!Character.isDigit(sca.charAt(0))) {
                    // -- skip entries that do not start with a digit
                    continue;
                }
                // -- parse and check for invalid status codes
                int sc = StringTools.parseInt(sca, 0);
                if (sc <= 0) {
                    // -- skip entries that have an invalid status code
                    continue;
                }
                // -- add to list
                scL.add(new Integer(sc));
            }
            this.countStatusCodes = !ListTools.isEmpty(scL)? ListTools.toIntArray(scL) : null; 
        } else {
            this.countStatusCodes = null;
        }

        /* minimum check-in age */
        String minAge = rtp.getString(PROP_minimumCheckInAge, null); // minimum check-in age filter
        if (StringTools.isBlank(minAge)) {
            this.minCheckInAgeSec = 0L; // disregard minimum check-in age
        } else
        if (minAge.equalsIgnoreCase("never")) {
            this.minCheckInAgeSec = -1L; // include "never" checked-in
        } else {
            this.minCheckInAgeSec = StringTools.parseLong(minAge,0L); // specific minimum check-in age
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the bound ReportLayout singleton instance for this report
    *** @return The bound ReportLayout
    **/
    public static ReportLayout GetReportLayout()
    {
        // -- bind the report format to this data
        return FieldLayout.getReportLayout();
    }

    /**
    *** Gets the bound ReportLayout singleton instance for this report
    *** @return The bound ReportLayout
    **/
    public ReportLayout getReportLayout()
    {
        // -- bind the report format to this data
        return GetReportLayout();
    }

    // ------------------------------------------------------------------------

    /**
    *** Creates and returns an iterator for the row data displayed in the body of this report.
    *** @return The body row data iterator
    **/
    public DBDataIterator getBodyDataIterator()
    {
        long nowSec = DateTime.getCurrentTimeSec();

        /* init */
        this.rowData = new Vector<FieldData>();

        /* loop through devices */
        String devID = "";
        ReportDeviceList devList = this.getReportDeviceList();
        for (Iterator i = devList.iterator(); i.hasNext();) {
            devID = (String)i.next();
            try {
                Device device = devList.getDevice(devID);
                if (device == null) {
                    // -- should never occur
                    Print.logError("Returned DeviceList 'Device' is null: " + devID);
                    continue;
                }
                // -- filter on check-in age
                if (this.minCheckInAgeSec != 0L) {
                    long lastConnTS = device.getLastTotalConnectTime();
                    try {
                        EventData lastEv = device.getLastEvent(-1L, false);
                        if ((lastEv != null) && (lastEv.getTimestamp() > lastConnTS)) {
                            lastConnTS = lastEv.getTimestamp();
                        }
                    } catch (DBException dbe) {
                        // -- error retrieving event record
                    }
                    if (this.minCheckInAgeSec < 0L) {
                        // -- include "never" only
                        if (lastConnTS > 0L) {
                            // -- device has had at least one event 
                            continue;
                        }
                    } else
                    if (this.minCheckInAgeSec > 0L) {
                        long lcAgeSec = nowSec - lastConnTS;
                        if (lcAgeSec < this.minCheckInAgeSec) {
                            // -- device has had a recent event
                            continue;
                        }
                    }
                }
                // -- create report row
                FieldData fd = new FieldData();
                fd.setDevice(device);
                this.rowData.add(fd);
            } catch (DBException dbe) {
                Print.logError("Error retrieving EventData count for Device: " + devID);
            }
        }

        /* report start/stop time */
        long startTime = this.getTimeStart();
        long endTime   = this.getTimeEnd();

        /* add delta values? (hours, odometer, fuel) */
        if (this.includeDeltaValues) {

            /* iterate through devices */
            for (FieldData fd : this.rowData) { // one row per Device
                Device dev = fd.getDevice(); // should not be null

                /* acumulators */
                double startHours = 0.0;
                double stopHours  = 0.0;
                double startOdom  = 0.0;
                double stopOdom   = 0.0;
                double startFuel  = 0.0;
                double stopFuel   = 0.0;

                /* first event following startTime containing a valid odometer/fuel */
                try {
                    EventData ed[] = dev.getRangeEvents(
                        startTime, -1L/*endtime*/, 
                        // - all statusCodes
                        false, // validGPS?
                        EventData.LimitType.FIRST, 4L/*limit*/);
                    if (!ListTools.isEmpty(ed)) {
                        for (int e = 0; e < ed.length; e++) {
                            // -- Start Hours
                            if (startHours <= 0.0) {
                                double hours = ed[e].getEngineHours(); // hours
                                if (hours > 0.0) {
                                    startHours = hours;
                                }
                            }
                            // -- Start Odometer
                            if (startOdom <= 0.0) {
                                double odom = ed[e].getOdometerKM(); // kilometers
                                if (odom > 0.0) {
                                    startOdom = odom;
                                }
                            }
                            // -- Start Fuel
                            if (startFuel <= 0.0) {
                                double fuel = ed[e].getFuelTotal(); // Liters
                                if (fuel > 0.0) {
                                    startFuel = fuel;
                                }
                            }
                            // -- break?
                            if ((startHours > 0.0) && (startOdom > 0.0) && (startFuel > 0.0)) {
                                break;
                            }
                        }
                    }
                } catch (DBException dbe) {
                    Print.logException("Getting FIRST Device Event Records", dbe);
                }

                /* last event prior to endTime containing a valid odometer */
                if (endTime <= 0L) {
                    // -- end of time
                    stopHours = dev.getLastEngineHours();
                    stopOdom  = dev.getLastOdometerKM();
                    stopFuel  = dev.getLastFuelTotal();
                } else {
                    try {
                        EventData ed[] = dev.getRangeEvents(
                            -1L, endTime, 
                            // - all statusCodes
                            false, // validGPS?
                            EventData.LimitType.LAST, 4L/*limit*/);
                        if (!ListTools.isEmpty(ed)) {
                            for (int e = ed.length - 1; e > 0; e--) {
                                // -- Stop Hours
                                if (stopHours <= 0.0) {
                                    double hours = ed[e].getEngineHours(); // hours
                                    if (hours > 0.0) {
                                        stopHours = hours;
                                    }
                                }
                                // -- Stop Odometer
                                if (stopOdom <= 0.0) {
                                    double odom = ed[e].getOdometerKM(); // kilometers
                                    if (odom > 0.0) {
                                        stopOdom = odom;
                                    }
                                }
                                // -- Stop Fuel
                                if (stopFuel <= 0.0) {
                                    double fuel = ed[e].getFuelTotal(); // Liters
                                    if (fuel > 0.0) {
                                        stopFuel = fuel;
                                    }
                                }
                                // -- break?
                                if ((stopHours > 0.0) && (stopOdom > 0.0) && (stopFuel > 0.0)) {
                                    break;
                                }
                            }
                        }
                    } catch (DBException dbe) {
                        Print.logException("Getting FIRST Device Event Records", dbe);
                    }
                }

                /* save engine-hour values */
                if (startHours > 0) {
                    fd.setDouble(FieldLayout.DATA_START_HOURS, startHours);
                }
                if (stopHours > 0) {
                    fd.setDouble(FieldLayout.DATA_START_HOURS, stopHours);
                }
                if ((startHours > 0.0) && (stopHours > 0.0)) {
                    double deltaHours = stopHours - startHours; // hours
                    if (deltaHours < 0.0) { deltaHours = 0.0; }
                    //fd.setDouble(FieldLayout.DATA_HOURS_DELTA, deltaHours);
                }

                /* save odometer values */
                if (startOdom > 0.0) {
                    fd.setDouble(FieldLayout.DATA_START_ODOMETER, startOdom);
                }
                if (stopOdom > 0.0) {
                    fd.setDouble(FieldLayout.DATA_STOP_ODOMETER, stopOdom);
                }
                if ((startOdom > 0.0) && (stopOdom > 0.0)) {
                    double deltaOdom = stopOdom - startOdom; // kilometers
                    if (deltaOdom < 0.0) { deltaOdom = 0.0; }
                    fd.setDouble(FieldLayout.DATA_ODOMETER_DELTA, deltaOdom);
                    fd.setDouble(FieldLayout.DATA_DISTANCE      , deltaOdom);
                }

                /* planned distance */
                // -- If DATA_PLAN_DISTANCE is left undefined, FieldLayout will pull
                // -  the value from "dev.getPlanDistanceKM()" by default.
                double planDelta = dev.getPlanDistanceKM();
                fd.setDouble(FieldLayout.DATA_PLAN_DISTANCE, planDelta);

                /* save fuel values */
                if (startFuel > 0.0) {
                    fd.setDouble(FieldLayout.DATA_START_FUEL, startFuel);
                }
                if (stopFuel > 0.0) {
                    fd.setDouble(FieldLayout.DATA_STOP_FUEL, stopFuel);
                }
                if ((startFuel > 0.0) && (stopFuel > 0.0)) {
                    double deltaFuel = stopFuel - startFuel; // Liters
                    fd.setDouble(FieldLayout.DATA_FUEL_DELTA, deltaFuel);
                }

            } // iterate through devices

        }

        /* count status codes? */
        if (!ListTools.isEmpty(this.countStatusCodes)) {

            /* iterate through devices */
            for (FieldData fd : this.rowData) { // one row per Device
                Device dev = fd.getDevice(); // should not be null

                /* get events */
                Map<Integer,AccumulatorLong> countMap = null;
                try {
                    // -- get events
                    EventData ed[] = dev.getRangeEvents(
                        startTime, -1L/*endtime*/, 
                        this.countStatusCodes, // list of specific status codes
                        false, // validGPS?
                        EventData.LimitType.FIRST, -1L/*limit*/);
                    // -- count status codes
                    if (!ListTools.isEmpty(ed)) {
                        countMap = new HashMap<Integer,AccumulatorLong>();
                        for (EventData ev : ed) {
                            Integer sci = new Integer(ev.getStatusCode());
                            AccumulatorLong acc = countMap.get(sci);
                            if (acc != null) {
                                acc.increment();
                            } else {
                                countMap.put(sci, new AccumulatorLong(1L));
                            }
                        }
                    }
                } catch (DBException dbe) {
                    Print.logException("Getting Event StatusCodes", dbe);
                }

                /* populate FieldData entry */
                for (int sc : this.countStatusCodes) {
                    AccumulatorLong acc = (countMap != null)? countMap.get(new Integer(sc)) : null;
                    long count = (acc != null)? acc.get() : 0L;
                    String scKey = FieldLayout.DATA_STATUS_COUNT + "_" + StringTools.toHexString(sc,16);
                    fd.setLong(scKey, count);
                }

            } // iterate through devices

        }

        /* return data iterator */
        FieldData.sortByDeviceDescription(this.rowData);
        return new ListDataIterator(this.rowData);

    }

    /**
    *** Creates and returns an iterator for the row data displayed in the total rows of this report.
    *** @return The total row data iterator
    **/
    public DBDataIterator getTotalsDataIterator()
    {
        return null;
    }

    // ------------------------------------------------------------------------

}
