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
//  2009/07/01  Martin D. Flynn
//     -Initial release
//  2009/11/01  Martin D. Flynn
//     -Added property 'stopOnIgnitionOff'
//  2010/04/11  Martin D. Flynn
//     -Added ability to apply an estimated fuel usage at report time (fuelTotal)
//  2011/06/16  Martin D. Flynn
//     -Now use device fuel-economy if available, "kilometersPerLiter" otherwise.
//     -Added property "tripStartStopOnly" to omit all detail between start/stop.
//  2012/02/03  Martin D. Flynn
//     -Remove any current data records if a 'tripStart' was never encountered.
//  2013/05/28  Martin D. Flynn
//     -Added speeding duration column support (see PROP_speedingThresholdKPH)
//  2013/08/06  Martin D. Flynn
//     -Added support for report column DATA_ODOMETER_DELTA_BIT
//     -Added support for report column DATA_IDLE_ELAPSED
//  2013/08/27  Martin D. Flynn
//     -Added "device" option to "speedingThresholdKPH" property to allow for
//      checking the Device "speedLimitKPH" value.
//  2013/09/20  Martin D. Flynn
//     -Added DATA_DRIVER_ID
//  2014/10/22  Martin D. Flynn
//     -Added option "anylimit" for "speedingThresholdKPH" property. [2.5.8-B01]
//  2015/08/16  Martin D. Flynn
//     -Fixed per-device (XML output "by Group") report generation [2.6.0-B61]
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

public class TripReport
    extends ReportData
    implements DBRecordHandler<EventData>
{

    // ------------------------------------------------------------------------
    // Properties

    private static final String PROP_tripStartType              = "tripStartType";
    private static final String PROP_minimumStoppedTime         = "minimumStoppedTime"; // TRIP_ON_SPEED only
    private static final String PROP_minimumSpeedKPH            = "minimumSpeedKPH";
    private static final String PROP_stopOnIgnitionOff          = "stopOnIgnitionOff";
    private static final String PROP_estimateFuelTotal          = "estimateFuelTotal";
    private static final String PROP_kilometersPerLiter         = "kilometersPerLiter";
    private static final String PROP_tripStartStopOnly          = "tripStartStopOnly";
    private static final String PROP_speedingThresholdKPH       = "speedingThresholdKPH";
    private static final String PROP_minimumSpeedingDuration    = "minimumSpeedingDuration";
    private static final String PROP_tripStopLookAheadSeconds   = "tripStopLookAheadSeconds";
    private static final String PROP_odometerDeltaBit           = "odometerDeltaBit";

    // ------------------------------------------------------------------------
    // trip-stop look-ahead support

    private static final long   TRIPSTOP_LOOKAHEAD_SEC          = 0L;

    // ------------------------------------------------------------------------
    // Trip start types

    private static final String MOTION_DEFAULT[]            = new String[] { "default" };
    private static final String MOTION_SPEED[]              = new String[] { "speed", "motion" };
    private static final String MOTION_IGNITION[]           = new String[] { "ignition" };
    private static final String MOTION_ENGINE[]             = new String[] { "engine" };
    private static final String MOTION_STARTSTOP[]          = new String[] { "start", "startstop" };

    private static final int    TRIP_ON_SPEED               = 0;
    private static final int    TRIP_ON_IGNITION            = 1;
    private static final int    TRIP_ON_ENGINE              = 2;
    private static final int    TRIP_ON_START               = 3;

    /**
    *** Minimum speed used for determining in-motion when the device does not
    *** support start/stop events
    **/
    private static final double MIN_SPEED_KPH               = 5.0;

    /**
    *** Speeding threshold used for calculating the duration of a "speeding" incidence
    **/
    private static final int    SPEED_TYPE_NONE             = 0;
    private static final int    SPEED_TYPE_THRESHOLD        = 1;   // specified threshold
    private static final int    SPEED_TYPE_DEVICE           = 2;   // device speed limit only
    private static final int    SPEED_TYPE_POSTED           = 3;   // posted speed limit only
    private static final int    SPEED_TYPE_ANYLIMIT         = 6;   // device/posted/geozone limit
    private static final double SPEEDING_THRESHOLD          = 0.0;
    private static final long   MIN_SPEED_DURATION          = 0L;

    /**
    *** Input Mask bit to check for odometer accumulation
    **/
    private static final int    ODOMETER_DELTA_BIT          = -1;

    /**
    *** Default mimimum stopped elapsed time to be considered stopped
    **/
    private static final long   MIN_STOPPED_TIME_SEC        = DateTime.MinuteSeconds(5);

    /**
    *** Delimit stop with ignition off? (if this occurs before the minimum stopped time)
    **/
    private static final boolean STOP_ON_IGNITION_OFF       = false;

    /**
    *** Display only trip "Start" and "Stop" events (ie. omit interleaving detail)?
    **/
    private static final boolean TRIP_START_STOP_ONLY       = false;

    /**
    *** Default to estimate fuel usage total (should be 'false')
    **/
    private static final boolean ESTIMATE_FUEL_TOTAL        = false;
    private static final double  KILOMETERS_PER_LITER       = 20.0 * GeoPoint.KILOMETERS_PER_MILE * Account.US_GALLONS_PER_LITER;
    // mi/G * 1.609344 km/mi * 0.264172052 G/L = km/L
    // mi/G * 0.425144 ((km*G)/(mi*L)) = km/L
    
    /**
    *** Non-zero Liters value (but still near-equivalent to zero)
    *** (Used to trick layout into displaying a "0.0" value, rather than blank)
    **/
    private static final double  ZERO_LITERS                = 0.001;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private boolean                     startStopSupported  = false;
    private int                         ignitionCodes[]     = null;

    private long                        ignitionOnTime      = 0L;
    private long                        ignitionOffTime     = 0L;

    private long                        motionStartTime     = 0L;
    private long                        motionStopTime      = 0L;

    private boolean                     idleIsMoving        = false;
    private double                      idleMinSpeedKPH     = 0.0; // <= is considered idling
    private long                        idleStartTime       = 0L;
    private long                        idleDuraTotalSec    = 0L;

    private int                         tripStartType       = TRIP_ON_SPEED;
    private EventData                   tripStartEvent      = null;
    private EventData                   firstEvent          = null;
    private EventData                   previousEvent       = null;
    private boolean                     approxStart         = true;
    
    private long                        lookAheadSeconds    = TRIPSTOP_LOOKAHEAD_SEC;
    private Vector<EventData>           lookAheadEvents     = null;
    private int                         lookAheadNdx        = -1;

    private boolean                     devEstFuelTotal     = ESTIMATE_FUEL_TOTAL;
    private double                      devFuelEconomy      = 0.0;

    private boolean                     speedIsMoving       = false; // TRIP_ON_SPEED only
    private long                        lastMovingTime      = 0L;    // TRIP_ON_SPEED only
    private long                        lastStoppedTime     = 0L;    // TRIP_ON_SPEED only

    private double                      minSpeedKPH         = MIN_SPEED_KPH;
    private long                        minStoppedTimeSec   = MIN_STOPPED_TIME_SEC;
    private boolean                     stopOnIgnitionOff   = STOP_ON_IGNITION_OFF;
    private boolean                     estimateFuelTotal   = ESTIMATE_FUEL_TOTAL;
    private double                      kilometersPerLiter  = KILOMETERS_PER_LITER;
    private boolean                     tripStartStopOnly   = TRIP_START_STOP_ONLY;

    private int                         speedingType        = SPEED_TYPE_THRESHOLD;
    private double                      speedingThreshold   = SPEEDING_THRESHOLD;
    private long                        minSpeedingDuration = 0L;
    private EventData                   speedingStartEvent  = null;
    private EventData                   speedingLastEvent   = null;
    private long                        speedDuraTotalSec   = 0L;

    private int                         odometerDeltaBit    = -1;
    private double                      bitOdometerTripKM   = 0.0;
    
    private double                      lastFuelLevel       = 0.0;
    private double                      lastFuelRemain      = 0.0;

    private java.util.List<FieldData>   rowData             = null;
    
    private I18N                        i18n                = null;

    // ------------------------------------------------------------------------

    /**
    *** Trip Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public TripReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        this.i18n = reqState.getPrivateLabel().getI18N(TripReport.class);

        /* Account check */
        if (this.getAccount() == null) {
            throw new ReportException("Account-ID not specified");
        }

        /* Device check */
        if (this.getDeviceCount() <= 0) {
            throw new ReportException("No Devices specified");
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {

        /* Trip type vars */
        RTProperties tripRTP     = this.getProperties();
        this.minSpeedKPH         = tripRTP.getDouble( PROP_minimumSpeedKPH         , MIN_SPEED_KPH);
        this.minStoppedTimeSec   = tripRTP.getLong(   PROP_minimumStoppedTime      , MIN_STOPPED_TIME_SEC);
        this.stopOnIgnitionOff   = tripRTP.getBoolean(PROP_stopOnIgnitionOff       , STOP_ON_IGNITION_OFF);
        this.estimateFuelTotal   = tripRTP.getBoolean(PROP_estimateFuelTotal       , ESTIMATE_FUEL_TOTAL);
        this.kilometersPerLiter  = tripRTP.getDouble( PROP_kilometersPerLiter      , KILOMETERS_PER_LITER);
        this.tripStartStopOnly   = tripRTP.getBoolean(PROP_tripStartStopOnly       , TRIP_START_STOP_ONLY);
        this.minSpeedingDuration = tripRTP.getLong(   PROP_minimumSpeedingDuration , MIN_SPEED_DURATION);
        this.odometerDeltaBit    = tripRTP.getInt(    PROP_odometerDeltaBit        , ODOMETER_DELTA_BIT);

        /* speed duration check */
        String speedingTypeStr   = tripRTP.getString( PROP_speedingThresholdKPH    , String.valueOf(SPEEDING_THRESHOLD));
        if (speedingTypeStr.equalsIgnoreCase("device")) {
            // -- device speed limit only
            this.speedingType      = SPEED_TYPE_DEVICE;
            this.speedingThreshold = 0.0;
        } else
        if (speedingTypeStr.equalsIgnoreCase("posted")) {
            // -- posted speed limit only
            this.speedingType      = SPEED_TYPE_POSTED;
            this.speedingThreshold = 0.0;
        } else
        if (speedingTypeStr.equalsIgnoreCase("anylimit")) {
            // -- device/posted/geozone speed-limit
            this.speedingType      = SPEED_TYPE_ANYLIMIT;
            this.speedingThreshold = 0.0;
        } else {
            double threshold = StringTools.parseDouble(speedingTypeStr,0.0);
            if (threshold <= 0.0) {
                this.speedingType      = SPEED_TYPE_NONE;
                this.speedingThreshold = 0.0;
            } else {
                this.speedingType      = SPEED_TYPE_THRESHOLD;
                this.speedingThreshold = threshold;
            }
        }

        /* debug */
        if (this.estimateFuelTotal) {
            Print.logInfo("Estimating Fuel Usage: default = " + this.kilometersPerLiter + " km/L");
        }
        if (this.tripStartStopOnly) {
            Print.logInfo("Trip detail will be omitted (per 'tripStartStopOnly')");
        }

        /* debug log */
        if (Print.isDebugLoggingLevel()) {
        Print.logInfo("Date Range          : " + this.getTimeStart() + " ==> " + this.getTimeEnd());
        Print.logInfo("Trip Start Type     : " + tripRTP.getString(PROP_tripStartType,"?"));
        Print.logInfo("Trip Start/Stop Only: " + this.tripStartStopOnly);
        Print.logInfo("Minimum Speed km/h  : " + this.minSpeedKPH);
        Print.logInfo("Minimum Stopped Time: " + this.minStoppedTimeSec);
        Print.logInfo("Speeding Type       : " + this.speedingType);
        Print.logInfo("Speeding Threshold  : " + this.speedingThreshold);
        Print.logInfo("Stop on Ign-OFF     : " + this.stopOnIgnitionOff);
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this report handles only a single device at a time
    *** @return True If this report handles only a single device at a time
    **/
    public boolean isSingleDeviceOnly()
    {
        return true;
    }

    // ------------------------------------------------------------------------

    /**
    *** Override 'getEventData' to reset selected status codes
    *** @param device       The Device for which EventData records will be selected
    *** @param rcdHandler   The DBRecordHandler
    *** @return An array of EventData records for the device
    **/
    @Override
    protected EventData[] getEventData_Device(Device device, DBRecordHandler<EventData> rcdHandler)
    {
        return super.getEventData_Device(device, rcdHandler);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this report supports displaying a map
    *** @return True if this report supports displaying a map, false otherwise
    **/
    public boolean getSupportsMapDisplay() // false
    {
        return false;
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
        //Print.logInfo("Requesting DBDataIterator ...");

        /* init report body data */
        Vector<FieldData> bodyData = new Vector<FieldData>(); // [2.6.0-B61]

        /* loop through devices */
        String accountID = this.getAccountID();
        ReportDeviceList devList = this.getReportDeviceList();
        for (Iterator i = devList.iterator(); i.hasNext();) {
            this.rowData = new Vector<FieldData>(); // per device
            String devID = (String)i.next();
            //Print.logInfo("Reading data for device: " + devID);
            try {

                // -- get device
                Device device = devList.getDevice(devID);
                if (device == null) {
                    // -- (unlikely) no Device, no report data
                    continue;
                }

                // -- trip vars init (new Device)
                this.firstEvent         = null;
                this.previousEvent      = null;
                this.tripStartType      = TRIP_ON_SPEED; // reset below
                this.tripStartEvent     = null;
                this.approxStart        = true;
                // -- clear odometer calcs
                this.bitOdometerTripKM  = 0.0;
                // -- clear TRIP_ON_SPEED vars
                this.speedIsMoving      = false; // TRIP_ON_SPEED only
                this.lastMovingTime     = 0L;
                this.lastStoppedTime    = 0L;
                // -- clear ignition calcs
                this.ignitionOnTime     = 0L;
                this.ignitionOffTime    = 0L;
                // -- clear motion start/stop status code calcs
                this.motionStartTime    = 0L;
                this.motionStopTime     = 0L;
                // -- clear fuel calcs
                this.lastFuelLevel      = 0.0;
                this.lastFuelRemain     = 0.0;
                // -- clear speeding calcs
                this.speedingStartEvent = null;
                this.speedingLastEvent  = null;
                this.speedDuraTotalSec  = 0L;
                // -- clear idle calcs
                this.idleIsMoving       = false;
                this.idleStartTime      = 0L;
                this.idleDuraTotalSec   = 0L;

                /* estimate fuel consumption? */
                this.devFuelEconomy     = 0.0;
                this.devEstFuelTotal    = false;
                if (this.estimateFuelTotal) {
                    double fuelEcon = device.getFuelEconomy();
                    if (fuelEcon > 0.0) { // device has fuel economy
                        this.devFuelEconomy  = fuelEcon;
                        this.devEstFuelTotal = true;
                    } else
                    if (this.kilometersPerLiter > 0.0) { // default fuel economy
                        this.devFuelEconomy  = this.kilometersPerLiter;
                        this.devEstFuelTotal = true;
                    }
                }

                // -- Device supports start/stop
                this.startStopSupported = device.getStartStopSupported();
                if (!this.startStopSupported) {
                    // -- TODO: scan EventData for STATUS_MOTION_START/STATUS_MOTION_STOP
                }

                // -- Device ignition statusCodes
                this.ignitionCodes = device.getIgnitionStatusCodes();
                boolean hasIgnition = (this.ignitionCodes != null);

                // -- trip start/stop type
                String tt = this.getProperties().getString(PROP_tripStartType,MOTION_SPEED[0]).toLowerCase();
                //Print.logInfo("Trip type: " + tt);
                if (ListTools.contains(MOTION_DEFAULT,tt)) {
                    // -- "default" (TRIP_ON_ENGINE will not be selected when using "default")
                    String devCode = device.getDeviceCode();
                    DCServerConfig dcs = DCServerFactory.getServerConfig(devCode);
                    if ((dcs == null) && StringTools.isBlank(devCode) && Account.IsDemoAccount(accountID)) {
                        // -- special case for "demo" account when 'deviceCode' is blank
                        dcs = DCServerFactory.getServerConfig(DCServerFactory.OPENDMTP_NAME);
                        if (dcs == null) {
                            Print.logWarn("Account 'demo' DCServerConfig not found: " + DCServerFactory.OPENDMTP_NAME);
                        }
                    }
                    if (dcs != null) {
                        // -- DCServerConfig found
                        if (dcs.getStartStopSupported(false/*default*/)) {
                            // -- Device supports start/stop
                            this.tripStartType = TRIP_ON_START;
                        } else
                        if (hasIgnition) {
                            // -- Device supports ignition state
                            this.tripStartType = TRIP_ON_IGNITION;
                        } else {
                            // -- Default to speed
                            this.tripStartType = TRIP_ON_SPEED;
                        }
                    } else {
                        // -- DCServerConfig not found ('deviceCode' is either blank or invalid)
                        if (hasIgnition) {
                            // -- Device supports ignition state
                            this.tripStartType = TRIP_ON_IGNITION;
                        } else {
                            // -- Default
                            this.tripStartType = TRIP_ON_SPEED;
                        }
                    }
                } else
                if (ListTools.contains(MOTION_STARTSTOP,tt)) {
                    // -- "startstop"
                    this.tripStartType = TRIP_ON_START;
                    //Print.logInfo("Trip delimiter: start/stop [ignition = " + hasIgnition + "]");
                } else
                if (ListTools.contains(MOTION_IGNITION,tt)/* && hasIgnition */) {
                    // -- "ignition"
                    this.tripStartType = TRIP_ON_IGNITION;
                    if (!hasIgnition) {
                         this.ignitionCodes = new int[] { StatusCodes.STATUS_IGNITION_OFF, StatusCodes.STATUS_IGNITION_ON };
                         hasIgnition = true;
                    }
                    //Print.logInfo("Trip delimiter: ignition");
                } else
                if (ListTools.contains(MOTION_ENGINE,tt)) {
                    // -- "engine"
                    this.tripStartType = TRIP_ON_ENGINE;
                    //Print.logInfo("Trip delimiter: engine");
                } else {
                    // -- "speed", "motion"
                    this.tripStartType = TRIP_ON_SPEED;
                    //Print.logInfo("Trip delimiter: speed");
                }

                // -- look-ahead seconds
                if (this.tripStartType == TRIP_ON_SPEED) {
                    String las = this.getProperties().getString(PROP_tripStopLookAheadSeconds, null);
                    if (StringTools.isBlank(las)) {
                        this.lookAheadSeconds = TRIPSTOP_LOOKAHEAD_SEC;
                    } else
                    if (las.equalsIgnoreCase("minimumStoppedTime")) {
                        this.lookAheadSeconds = this.minStoppedTimeSec + 1L;
                    } else {
                        this.lookAheadSeconds = StringTools.parseLong(las,TRIPSTOP_LOOKAHEAD_SEC);
                    }
                } else {
                    this.lookAheadSeconds = 0L; // disable
                }

                // -- get events
                if (this.lookAheadSeconds > 0L) {
                    //Print.logInfo("Reading EventData records in 'look-ahead' mode");
                    // -- load all EventData records, then iterate through the list
                    this.lookAheadEvents = new Vector<EventData>();
                    final Vector<EventData> evList = this.lookAheadEvents;
                    final long lim = TripReport.this.getSelectionLimit();
                    // -- get all pertinent events
                    this.getEventData_Device(device, new DBRecordHandler<EventData>() {
                        public int handleDBRecord(EventData ev) throws DBException {
                            evList.add(ev); // DBRH_SAVE
                            return ((lim <= 0L) || (evList.size() < lim))? DBRH_SKIP : DBRH_STOP;
                        }
                    });
                    // -- send events to trip analyzer
                    int evLen = ListTools.size(this.lookAheadEvents);
                    //Print.logInfo("Read " + evLen + " EventData records");
                    for (this.lookAheadNdx = 0; this.lookAheadNdx < evLen; this.lookAheadNdx++) {
                        EventData ev = this.lookAheadEvents.get(this.lookAheadNdx);
                        int dbrh = this.handleDBRecord(ev);
                        if (dbrh == DBRH_STOP) { break; }
                    }
                } else {
                    // -- pass EventData records to 'handleDBRecord' as they are read
                    //Print.logInfo("Reading EventData records in 'DBRecordHandler' mode");
                    this.getEventData_Device(device, this); // <== callback to 'handleDBRecord' (adds to 'this.rowData')
                }

                // -- if we never found a 'tripStart' we need to clear 'this.rowData'
                if (this.approxStart) {
                    // -- we never found a tripStart'
                    Print.logWarn("Actual Trip 'Start' not found, clearing row data ...");
                    this.rowData.clear(); // for this device only
                }

            } catch (DBException dbe) {
                Print.logError("Error retrieving EventData for Device: " + devID);
            }

            /* add this Device row data to report body data */
            bodyData.addAll(this.rowData); // [2.6.0-B61]
            this.rowData.clear();
            this.rowData = null;

        } // loop through devices

        /* return row iterator */
        return new ListDataIterator(bodyData); // [2.6.0-B61]

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
    // ------------------------------------------------------------------------

    private EventData getLookAheadEventData(int ofs)
    {

        /* not supported? */
        if (this.lookAheadEvents == null) {
            return null;
        }

        /* offset out of bounds? */
        int ndx = this.lookAheadNdx + ofs;
        if (ndx < 0) {
            return null;
        } else
        if (ndx >= this.lookAheadEvents.size()) {
            return null;
        }

        /* return event */
        return this.lookAheadEvents.get(ndx);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Check in-motion state for idle calculations
    **/
    private boolean isInMotion(EventData ev)
    {
        if (this.startStopSupported) {
            // -- look for STATUS_MOTION_START/STATUS_MOTION_STOP codes
            if ((this.motionStartTime > 0L) && (this.motionStartTime > this.motionStopTime)) {
                // -- STATUS_MOTION_START initialized, assume moving (not idling)
                return true; // not idling
            } else {
                // -- STATUS_MOTION_START not initialized, assume not moving (idling)
                return false;  // idling 
            }
        } else {
            // -- check event speed
            double evKPH = ev.getSpeedKPH();
            if (evKPH > this.idleMinSpeedKPH) {
                // -- vehicle is moving (not idling)
                return true; // not idling
            } else {
                // -- vehicle is stopped (idling)
                return false;  // idling 
            }
        }
    }

    /**
    *** Check speeding condition
    **/
    private boolean isSpeeding(EventData ev, int speedingType, double speedThresholdKPH)
    {
        if (ev == null) {
            return false;
        } else
        if (speedingType == SPEED_TYPE_ANYLIMIT) {
            int speedCond = ev.checkSpeedingCondition(speedThresholdKPH,0.0/*offset*/);
            return (speedCond > 0)? true : false;
        } else {
            double limitKPH = speedThresholdKPH;
            return (ev.getSpeedKPH() > limitKPH)? true : false;
        }
    }

    /**
    *** Custom DBRecord callback handler class
    *** @param rcd  The EventData record
    *** @return The returned status indicating whether to continue, or stop
    **/
    public int handleDBRecord(EventData rcd)
        throws DBException
    {
        EventData ev = rcd; // not null
        int statusCode = ev.getStatusCode();
        long timestamp = ev.getTimestamp();

        /* start/stop supported */
        /*
        if (!this.startStopSupported && 
            (statusCode == StatusCodes.STATUS_MOTION_START)) {
            Print.logWarn("Found Motion-Start status code, assuming device supports Start/Stop");
            this.startStopSupported = true;
        }
        */

        /* trip delimiter */
        boolean tripStart = this.isTripStart(ev);
        boolean tripStop  = tripStart? false : this.isTripStop(ev);

        /* debug */
        /*
        Print.logInfo("Event: " + 
            rcd.getAccountID() + ", " +
            rcd.getDeviceID() + ", " +
            StatusCodes.ToString(rcd.getStatusCode()) + ", " +
            (new DateTime(rcd.getTimestamp())) + ", " +
            rcd.getGeoPoint()
            );
        */

        /* first event */
        if (this.firstEvent == null) {
            this.firstEvent = ev;
            // -- if we start in the middle of a trip, designate the first record as a start
            if (!tripStop && !tripStart) {
                this.tripStartEvent = ev;
                this.approxStart    = true; // we may be in the middle of a 'stop'
            }
            if (this.devEstFuelTotal) {
                if (this.firstEvent.getOdometerKM() <= 0.0) {
                    Print.logWarn("First trip event missing odometer: " + ev.getDeviceID());
                    this.devEstFuelTotal = false;
                } else 
                if (this.firstEvent.getFuelTotal() > ZERO_LITERS) {
                    Print.logWarn("First trip event already has fuel-total: " + ev.getDeviceID());
                    this.devEstFuelTotal = false;
                }
            }
        }

        /* estimate fuel total (skip first event) */
        if (this.devEstFuelTotal) { // "this.devFuelEconomy" is greater than 0.0
            if (ev.getFuelTotal() > ZERO_LITERS) {
                Print.logDebug("WARN: Skipping FuelTotal estimate: CurrentEvent already has a fuel-total");
            } else
            if (ev.getOdometerKM() <= 0.0) {
                Print.logDebug("Skipping FuelTotal estimate: CurrentEvent does not have an odometer value");
            } else {
                double km = ev.getOdometerKM() - this.firstEvent.getOdometerKM();
                double liters = (km * (1.0 / this.devFuelEconomy)); // km * L/km = L
                ev.setFuelTotal((liters > 0.0)? liters : ZERO_LITERS);  // non-zero
                Print.logDebug("Setting FuelTotal estimate: " + ev.getFuelTotal() + " Liters");
            }
        }

        /* ignition on/off (independent of trip start/stop) */
        if (this.isIgnitionOn(ev)) {
            this.ignitionOnTime  = timestamp;
            this.ignitionOffTime = 0L;
        } else
        if (this.isIgnitionOff(ev)) {
            this.ignitionOnTime  = 0L;
            this.ignitionOffTime = timestamp;
        }

        /* ignition on/off (independent of trip start/stop) */
        if (statusCode == StatusCodes.STATUS_MOTION_START) {
            this.motionStartTime = timestamp;
            this.motionStopTime  = 0L;
        } else
        if (statusCode == StatusCodes.STATUS_MOTION_STOP) {
            this.motionStartTime = 0L;
            this.motionStopTime  = timestamp;
        }

        /* trip start */
        if (tripStart) {
            // -- this is a Trip Start event
            if (this.tripStartEvent != null) {
                // -- already have a Trip Start event
                if (this.approxStart) {
                    // -- This 'start' isn't real anyway, we likely started in the middle of a 'stop'
                    // -  Remove everything up to this point from the Row Data list
                    Print.logInfo("Found Trip 'Start', clearing previous approximate start ...");
                    this.rowData.clear(); // for this device only
                } else {
                    // -- two real 'start' events received without an interleaving 'stop'
                    this.rowData.add(new BlankRow()); // add blank row
                }
            }
            // -- start of trip
            this.tripStartEvent     = ev;
            this.approxStart        = false;
            // -- init/reset "Trip" vars
            this.idleStartTime      = 0L;
            this.idleDuraTotalSec   = 0L;
            this.speedDuraTotalSec  = 0L;
            this.bitOdometerTripKM  = 0.0;
            this.lastFuelLevel      = 0.0;
            this.lastFuelRemain     = 0.0;
        }

        /* for TRIP_ON_IGNITION, attempt to determine idle time */
        long idleElapse = -1L;
        /*if (this.tripStartType == TRIP_ON_IGNITION)*/ {
            boolean totalIdle = false;
            if (this.tripStartEvent == null) {
                // -- not inside a "trip" (should not be moving)
                // -  disregard idle
            } else
            if (!this.isInMotion(ev)) { 
                // -- outside a Start-Stop (if start/stop supported), or not-moving
                // -  I am not currently moving
                if (tripStart) {
                    // -- trip-start/ignition-on and I'm not moving
                    this.idleIsMoving  = false;
                    this.idleStartTime = timestamp;
                    idleElapse = 0L; // no idle elapsed, we just started idling
                } else
                if (this.idleIsMoving) {
                    // -- I was previously moving, but now I am not, start of idle
                    this.idleIsMoving  = false; // stopped moving
                    this.idleStartTime = timestamp;  // the time I stopped
                    idleElapse = 0L; // no idle elapsed, we just started idling
                } else {
                    // -- I am still not moving
                    if (this.idleStartTime > 0L) { // <-- should always be true here
                        // -- total idle elapsed since last stop time
                        idleElapse = timestamp - this.idleStartTime;
                    }
                }
            } else {
                // -- I am currently moving
                if (!this.idleIsMoving || tripStop) {
                    // -- I was not previously moving and now I am, end of idle
                    if (this.idleStartTime > 0L) {
                        idleElapse = timestamp - this.idleStartTime;
                        totalIdle = true;
                    }
                    this.idleIsMoving  = true;
                    this.idleStartTime = 0L;
                } else {
                    // -- I am still moving
                }
            }
            if (totalIdle || tripStop) {
                if (idleElapse > 0L) {
                    this.idleDuraTotalSec += idleElapse;
                }
            }
        }

        /* trip bit odometer */
        if (this.odometerDeltaBit >= 0) {
            // -- valid bit, accumulate bit odometer delta
            if (this.isOdometerBitOn(rcd) && 
                this.isOdometerBitOn(this.previousEvent)) {
                // -- previous event and this event both have the bit "On"
                double deltaKM = rcd.getOdometerKM() - this.previousEvent.getOdometerKM(); // Kilometers
                this.bitOdometerTripKM += deltaKM;
            }
        }

        /* speeding/duration */
        long    speedDurationSec  = -1L;
        double  speedThresholdKPH = 0.0; // default disabled
        boolean checkSpeeding     = false;
        // -- set speed threshold type
        if (this.speedingType == SPEED_TYPE_THRESHOLD) {
            // -- check threashold value in "this.speedingThreshold"
            speedThresholdKPH = this.speedingThreshold;
            checkSpeeding = (speedThresholdKPH > 0.0)? true : false;
        } else
        if (this.speedingType == SPEED_TYPE_DEVICE) {
            // -- check device specified "speedLimitKPH"
            Device device = ev.getDevice(); // should not be null
            speedThresholdKPH = (device != null)? device.getSpeedLimitKPH() : 0.0;
            checkSpeeding = (speedThresholdKPH > 0.0)? true : false;
        } else
        if (this.speedingType == SPEED_TYPE_POSTED) {
            // -- check posted speed limit
            speedThresholdKPH = ev.getSpeedLimitKPH(); // posted limit
            checkSpeeding = (speedThresholdKPH > 0.0)? true : false;
        } else
        if (this.speedingType == SPEED_TYPE_ANYLIMIT) {
            // -- check any speeding condition
          //speedThresholdKPH = ev.getMinimumSpeedingThreshold(this.speedingThreshold,0.0);
            speedThresholdKPH = this.speedingThreshold; // will be "0.0"
            checkSpeeding = true;
        }
        // -- check for speeding
        if (checkSpeeding) {
            if (this.tripStartEvent == null) {
                // -- not inside a "trip" (should not be moving, not-speeding)
                if (this.speedingStartEvent != null) {
                    // -- was speeding (end of speeding incident)
                    long beginSpeedTime     = this.speedingStartEvent.getTimestamp();
                    long endSpeedTime       = this.speedingLastEvent.getTimestamp();
                    this.speedDuraTotalSec += Math.max((endSpeedTime - beginSpeedTime), this.minSpeedingDuration);
                    this.speedingStartEvent = null;
                    this.speedingLastEvent  = null;
                } else {
                    // -- was not speeding
                }
            } else
            if (this.isSpeeding(ev,this.speedingType,speedThresholdKPH)) {
                // -- is speeding
                this.speedingLastEvent = ev;
                if (this.speedingStartEvent == null) {
                    // -- start of speeding incident
                    this.speedingStartEvent = ev;
                    speedDurationSec        = this.minSpeedingDuration;
                } else {
                    // -- continued speeding incident
                    long beginSpeedTime     = this.speedingStartEvent.getTimestamp();
                    long endSpeedTime       = this.speedingLastEvent.getTimestamp();
                    speedDurationSec        = Math.max((endSpeedTime - beginSpeedTime), this.minSpeedingDuration);
                }
            } else {
                // -- not speeding
                if (this.speedingStartEvent != null) {
                    // -- was speeding (end of speeding incident)
                    long beginSpeedTime     = this.speedingStartEvent.getTimestamp();
                    long endSpeedTime       = this.speedingLastEvent.getTimestamp();
                    this.speedDuraTotalSec += Math.max((endSpeedTime - beginSpeedTime), this.minSpeedingDuration);
                    this.speedingStartEvent = null;
                    this.speedingLastEvent  = null;
                } else {
                    // -- was not speeding
                }
            }
        }

        /* trip record */
        if (this.tripStartEvent != null) {
            // -- we have a Trip-Start for this event
            double bitOdomKM = this.bitOdometerTripKM;
            // -- save starting fuel-level if necessary
            double fuelLevel = ev.getFuelLevel(true/*estimate*/);
            if (fuelLevel > 0.0) {
                if (this.tripStartEvent.getFuelLevel(false/*noEstimate*/) <= 0.0) {
                    this.tripStartEvent.setFuelLevel(fuelLevel);
                }
                this.lastFuelLevel = fuelLevel; // > 0.0
            } else
            if (this.lastFuelLevel > 0.0) {
                // -- this assumes that a tank is never completely empty
                ev.setFuelLevel(this.lastFuelLevel);
                fuelLevel = this.lastFuelLevel;
            }
            // -- save starting fuel-remaining if necessary
            double fuelRemain = ev.getFuelRemain(true/*estimate*/);
            if (fuelRemain > 0.0) {
                if (this.tripStartEvent.getFuelRemain(false/*noEstimate*/) <= 0.0) {
                    this.tripStartEvent.setFuelRemain(fuelRemain);
                }
                this.lastFuelRemain = fuelRemain; // > 0.0
            } else
            if (this.lastFuelRemain > 0.0) {
                // -- this assumes that a tank is never completely empty
                ev.setFuelRemain(this.lastFuelRemain);
                fuelRemain = this.lastFuelRemain;
            }
            // -- save detail
            if (!this.tripStartStopOnly) {
                // -- save all trip detail events between start and stop
                //Print.logInfo("Saving event row data ...");
                TripDetail td = new TripDetail(
                    this.tripStartEvent, ev, 
                    speedDurationSec, bitOdomKM, idleElapse,
                    this.i18n);
                this.rowData.add(td);
            } else
            if (tripStart) {
                // -- always save trip start detail record
                //Print.logInfo("Saving event trip start row data ...");
                TripDetail td = new TripDetail(
                    this.tripStartEvent, ev, 
                    speedDurationSec, bitOdomKM, idleElapse,
                    this.i18n);
                this.rowData.add(td);
            } else
            if (tripStop) {
                // -- always save trip stop detail record
                //Print.logInfo("Saving event trip stop row data ...");
                TripDetail td = new TripDetail(
                    this.tripStartEvent, ev, 
                    speedDurationSec, bitOdomKM, idleElapse,
                    this.i18n);
                this.rowData.add(td);
            } else {
                // -- skipping this detail record?
                //Print.logInfo("Skipping detail record ... " + (new DateTime(ev.getTimestamp())));
            }
            // also end of trip? (save "total" record)
            if (tripStop) {
                // -- end of trip add item to total
                //Print.logInfo("Saving event end-of-trip ...");
                long totalDura = (this.speedDuraTotalSec > 0L)? this.speedDuraTotalSec : -1L;
                TripTotal tt = new TripTotal(
                    this.tripStartEvent, ev, 
                    totalDura, bitOdomKM, this.idleDuraTotalSec,
                    this.i18n);
                this.rowData.add(tt);
                // -- reset for next trip
                this.tripStartEvent     = null;
                this.idleStartTime      = 0L;
                this.idleDuraTotalSec   = 0L;
                this.speedDuraTotalSec  = 0L;
                this.bitOdometerTripKM  = 0.0;
                this.lastFuelLevel      = 0.0;
                this.lastFuelRemain     = 0.0;
            }
        }

        /* save previous event */
        this.previousEvent = ev;

        /* return record limit status */
        return (this.rowData.size() < this.getReportLimit())? DBRH_SKIP : DBRH_STOP;

    }

    // ------------------------------------------------------------------------

    private boolean isIgnitionOn(EventData ev)
    {
        int sc = ev.getStatusCode();
        if (sc == StatusCodes.STATUS_IGNITION_ON) {
            return true;
        } else
        if ((this.ignitionCodes != null) && (sc == this.ignitionCodes[1])) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isIgnitionOff(EventData ev)
    {
        int sc = ev.getStatusCode();
        if (sc == StatusCodes.STATUS_IGNITION_OFF) {
            return true;
        } else
        if ((this.ignitionCodes != null) && (sc == this.ignitionCodes[0])) {
            return true;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    private boolean isTripStart(EventData ev)
    {
        if (this.tripStartType == TRIP_ON_IGNITION) {
            int sc = ev.getStatusCode();
            if (sc == StatusCodes.STATUS_IGNITION_ON) {
                return true;
            } else
            if ((this.ignitionCodes != null) && (sc == this.ignitionCodes[1])) {
                return true;
            }
        } else
        if (this.tripStartType == TRIP_ON_ENGINE) {
            int sc = ev.getStatusCode();
            if (sc == StatusCodes.STATUS_ENGINE_START) {
                return true;
            }
        } else
        if (this.tripStartType == TRIP_ON_START) {
            int sc = ev.getStatusCode();
            if (sc == StatusCodes.STATUS_MOTION_START) {
                return true;
            }
        } else 
        if (this.tripStartType == TRIP_ON_SPEED) {
            if (ev.getSpeedKPH() >= this.minSpeedKPH) {
                this.lastStoppedTime = 0L;
                if (!this.speedIsMoving) {
                    //Print.logInfo("TripStart: moving="+this.speedIsMoving + ", speed="+ev.getSpeedKPH() + ", minSpeed="+this.minSpeedKPH);
                    this.speedIsMoving = true;
                    this.lastMovingTime = ev.getTimestamp();
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isTripStop(EventData ev)
    {
        if (this.tripStartType == TRIP_ON_IGNITION) {
            int sc = ev.getStatusCode();
            if (sc == StatusCodes.STATUS_IGNITION_OFF) {
                return true;
            } else
            if ((this.ignitionCodes != null) && (sc == this.ignitionCodes[0])) {
                return true;
            }
        } else
        if (this.tripStartType == TRIP_ON_ENGINE) {
            int sc = ev.getStatusCode();
            if (sc == StatusCodes.STATUS_ENGINE_STOP) {
                return true;
            }
        } else
        if (this.tripStartType == TRIP_ON_START) {
            int sc = ev.getStatusCode();
            if (sc == StatusCodes.STATUS_MOTION_STOP) {
                return true;
            } else
            if (sc == StatusCodes.STATUS_IGNITION_OFF) {
                // -- "Stop" inferred by IgnitionOff
                return true;
            } else
            if (this.stopOnIgnitionOff && (this.ignitionCodes != null) && (sc == this.ignitionCodes[0])) {
                // -- "Stop" inferred by IgnitionOff
                return true;
            }
        } else
        if (this.tripStartType == TRIP_ON_SPEED) {
            //Print.logInfo("TripStop: moving="+this.speedIsMoving + ", speed="+ev.getSpeedKPH() + ", minSpeed="+this.minSpeedKPH);
            if (this.speedIsMoving) {
                int sc = ev.getStatusCode();
                if (this.stopOnIgnitionOff && (this.ignitionCodes != null) && (sc == this.ignitionCodes[0])) {
                    // -- "Stop" inferred by IgnitionOff
                    this.speedIsMoving   = false;
                    this.lastMovingTime  = 0L;
                    this.lastStoppedTime = 0L;
                    return true;
                } else
                if (ev.getSpeedKPH() < this.minSpeedKPH) {
                    // -- we're stopped, check minimum stopped time
                    if (this.lastStoppedTime == 0L) {
                        this.lastStoppedTime = ev.getTimestamp();
                    } else {
                        long stoppedDeltaSec = ev.getTimestamp() - this.lastStoppedTime; // lastMovingTime;
                        if (stoppedDeltaSec >= this.minStoppedTimeSec) {
                            //Print.logInfo("TripStop: stopAge="+stoppedDeltaSec + ", minStopAge="+this.minStoppedTimeSec);
                            this.speedIsMoving   = false;
                            this.lastMovingTime  = 0L;
                            this.lastStoppedTime = 0L;
                            return true;
                        }
                    }
                } else
                if (this.lookAheadSeconds > 0L) {
                    EventData nextEV = this.getLookAheadEventData(1);
                    if (nextEV != null) {
                        long deltaSec = nextEV.getTimestamp() - ev.getTimestamp();
                        if (deltaSec >= this.lookAheadSeconds) {
                            // -- "Stop" inferred by excessive time-lapse to next event
                            this.speedIsMoving   = false;
                            this.lastMovingTime  = 0L;
                            this.lastStoppedTime = 0L;
                            return true;
                        }
                    } else {
                        // -- this is the last event, assume this is a stop
                        //this.speedIsMoving   = false;
                        //this.lastMovingTime  = 0L;
                        //this.lastStoppedTime = 0L;
                        //return true;
                    }
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------

    private boolean isOdometerBitOn(EventData ev)
    {
        if ((ev != null) && (this.odometerDeltaBit >= 0)) {
            return ev.getInputMaskBitState(this.odometerDeltaBit)? true : false;
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Custom BlankRow class
    **/
    private static class BlankRow
        extends FieldData
    {
        public BlankRow() {
            super();
            this.setCssClass(ReportLayout.CSS_CLASS_BODY_TOTAL);
            this.setValue(FieldLayout.DATA_ADDRESS, "");
        }
    }

    /**
    *** Custom TripDetail class
    **/
    private static class TripDetail
        extends FieldData
    {
        public TripDetail(
            EventData startEv, EventData tripEv, 
            long speedDuraSec, double bitOdomKM, long idleElapse,
            I18N i18n) {
            super();
            // -- Account/Device
            Account account = tripEv.getAccount();
            Device  device  = tripEv.getDevice();
            this.setAccount(account);
            this.setDevice(device);
            // -- EventData fields
            this.setValue(FieldLayout.DATA_TIMESTAMP         , tripEv.getTimestamp());
            this.setValue(FieldLayout.DATA_STATUS_CODE       , tripEv.getStatusCode());
            this.setValue(FieldLayout.DATA_LATITUDE          , tripEv.getLatitude());
            this.setValue(FieldLayout.DATA_LONGITUDE         , tripEv.getLongitude());
            this.setValue(FieldLayout.DATA_ALTITUDE          , tripEv.getAltitude());
            this.setValue(FieldLayout.DATA_SPEED             , tripEv.getSpeedKPH());
            this.setValue(FieldLayout.DATA_SPEED_DURATION    , speedDuraSec);
            this.setValue(FieldLayout.DATA_HEADING           , tripEv.getHeading());
            this.setValue(FieldLayout.DATA_ODOMETER          , tripEv.getOdometerKM());
            this.setValue(FieldLayout.DATA_ODOMETER_DELTA_BIT, bitOdomKM);
            this.setValue(FieldLayout.DATA_ADDRESS           , tripEv.getAddress());
            this.setValue(FieldLayout.DATA_DRIVER_ID         , tripEv.getDriverID()); // 2.5.2-B51
            // -- accumulator fields
            double odomDelta   = tripEv.getOdometerKM() - startEv.getOdometerKM();    // Kilometers
            long   driveElapse = tripEv.getTimestamp()  - startEv.getTimestamp();     // Seconds
            double fuelLevel   = tripEv.getFuelLevel(true/*estimate*/);               // %
            double fuelRemain  = tripEv.getFuelRemain(true/*estimate*/);              // Litres
            double fuelTotal   = tripEv.getFuelTotal();                               // Litres
            double fuelTrip    = 0.0;  /* calculate below */                          // Litres
            double fuelEcon    = 0.0;  /* calculate below */                          // km / L
            Device.FuelEconomyType fuelEconType = Device.FuelEconomyType.UNKNOWN;
            if (fuelTotal > 0.0) {
                // -- calculate trip fuel used/economy based on total fuel used
                fuelTrip       = fuelTotal - startEv.getFuelTotal();                  // Litres
                fuelEcon       = (fuelTrip > 0.0)? (odomDelta / fuelTrip) : 0.0;      // km / L
                fuelEconType   = Device.FuelEconomyType.FUEL_CONSUMED;
            } else
            if (fuelRemain > 0.0) {
                // -- calculate trip fuel used/economy based on delta fuel remaining in tank (highly inaccurate)
                fuelTrip       = startEv.getFuelRemain(true/*estimate*/) - fuelRemain;// Litres
                fuelEcon       = (fuelTrip > 0.0)? (odomDelta / fuelTrip) : 0.0;      // km / L
                fuelEconType   = Device.FuelEconomyType.FUEL_REMAINING;
            } else
            if (device.getFuelCapacity() > 0.0) {
                // -- calculate trip fuel used based on the change in fuel levels (highly inaccurate)
                double flDelta = startEv.getFuelLevel(true/*estimate*/) - fuelLevel;  // delta %
                fuelTrip       = (flDelta  > 0.0)? (flDelta * device.getFuelCapacity()) : 0.0; // Litres
                fuelEcon       = (fuelTrip > 0.0)? (odomDelta / fuelTrip) : 0.0;      // km / L
                fuelEconType   = Device.FuelEconomyType.FUEL_LEVEL;
            } else
            if (device.getFuelEconomy() > 0.0) {
                // -- calculate trip fuel used based on device's estimated economy (fairly inaccurate)
                fuelEcon       = (odomDelta > 0.0)? device.getFuelEconomy() : 0.0;    // km / L
                fuelTrip       = (fuelEcon  > 0.0)? (odomDelta / fuelEcon) : 0.0;     // Litres
                fuelEconType   = Device.FuelEconomyType.DEVICE_ECONOMY;
            }
            this.setValue(FieldLayout.DATA_FUEL_LEVEL       , fuelLevel);
            this.setValue(FieldLayout.DATA_FUEL_REMAIN      , fuelRemain);
            this.setValue(FieldLayout.DATA_FUEL_TOTAL       , fuelTotal);
            this.setValue(FieldLayout.DATA_FUEL_TRIP        , ((fuelTrip > 0.0)? fuelTrip : 0.0001));
            this.setValue(FieldLayout.DATA_ODOMETER_DELTA   , odomDelta);
            this.setValue(FieldLayout.DATA_FUEL_ECONOMY     , fuelEcon);
            this.setValue(FieldLayout.DATA_FUEL_ECONOMY_TYPE, fuelEconType);
            this.setValue(FieldLayout.DATA_DRIVING_ELAPSED  , driveElapse);
            this.setValue(FieldLayout.DATA_IDLE_ELAPSED     , idleElapse);
        }
    }

    /**
    *** Custom TripTotal class
    **/
    private static class TripTotal
        extends FieldData
    {
        public TripTotal(
            EventData startEv, EventData stopEv, 
            long speedDuraSec, double bitOdomKM, long idleElapse,
            I18N i18n) {
            super();
            this.setRowType(DBDataRow.RowType.SUBTOTAL);
            this.setCssClass(ReportLayout.CSS_CLASS_BODY_TOTAL);
            if ((startEv != null) && (stopEv != null)) {
                // -- Trip time/distance
                Device device      = stopEv.getDevice();
                long   driveElapse = stopEv.getTimestamp()  - startEv.getTimestamp();     // Seconds
                double odomDelta   = stopEv.getOdometerKM() - startEv.getOdometerKM();    // Kilometers
                double fuelLevel   = stopEv.getFuelLevel(true/*estimate*/);               // %
                double fuelRemain  = stopEv.getFuelRemain(true/*estimate*/);              // Litres
                double fuelTotal   = stopEv.getFuelTotal();                               // Litres
                double fuelTrip    = 0.0;                                                 // Litres
                double fuelEcon    = 0.0;                                                 // km / L
                String driverID    = stopEv.getDriverID();
                Device.FuelEconomyType fuelEconType = Device.FuelEconomyType.UNKNOWN;
                if (fuelTotal > 0.0) {
                    // -- calculate trip fuel used/economy based on total fuel used (very accurate)
                    fuelTrip       = fuelTotal - startEv.getFuelTotal();                  // Litres
                    fuelEcon       = (fuelTrip > 0.0)? (odomDelta / fuelTrip) : 0.0;      // km / L
                    fuelEconType   = Device.FuelEconomyType.FUEL_CONSUMED;
                } else
                if (fuelRemain > 0.0) {
                    // -- calculate trip fuel used/economy based on delta fuel remaining in tank (highly inaccurate)
                    fuelTrip       = startEv.getFuelRemain(true/*estimate*/) - fuelRemain;// Litres
                    fuelEcon       = (fuelTrip > 0.0)? (odomDelta / fuelTrip) : 0.0;      // km / L
                    fuelEconType   = Device.FuelEconomyType.FUEL_REMAINING;
                } else
                if (device.getFuelCapacity() > 0.0) {
                    // -- calculate trip fuel used based on the change in fuel levels (highly inaccurate)
                    double flDelta = startEv.getFuelLevel(true/*estimate*/) - fuelLevel;  // delta %
                    fuelTrip       = (flDelta  > 0.0)? (flDelta * device.getFuelCapacity()) : 0.0; // Litres
                    fuelEcon       = (fuelTrip > 0.0)? (odomDelta / fuelTrip) : 0.0;      // km / L
                    fuelEconType   = Device.FuelEconomyType.FUEL_LEVEL;
                } else
                if (device.getFuelEconomy() > 0.0) {
                    // -- calculate trip fuel used based on device's estimated economy (fairly inaccurate)
                    fuelEcon       = (odomDelta > 0.0)? device.getFuelEconomy() : 0.0;    // km / L
                    fuelTrip       = (fuelEcon  > 0.0)? (odomDelta / fuelEcon) : 0.0;     // Litres
                    fuelEconType   = Device.FuelEconomyType.DEVICE_ECONOMY;
                }
                this.setValue(FieldLayout.DATA_SPEED_DURATION    , speedDuraSec); // total speeding seconds
                this.setValue(FieldLayout.DATA_FUEL_LEVEL        , fuelLevel);
                this.setValue(FieldLayout.DATA_FUEL_REMAIN       , fuelRemain);
                this.setValue(FieldLayout.DATA_FUEL_TOTAL        , fuelTotal);
                this.setValue(FieldLayout.DATA_FUEL_TRIP         , ((fuelTrip > 0.0)? fuelTrip : 0.0001));
                this.setValue(FieldLayout.DATA_ODOMETER_DELTA    , odomDelta);
                this.setValue(FieldLayout.DATA_ODOMETER_DELTA_BIT, bitOdomKM);
                this.setValue(FieldLayout.DATA_FUEL_ECONOMY      , fuelEcon);
                this.setValue(FieldLayout.DATA_FUEL_ECONOMY_TYPE , fuelEconType);
                this.setValue(FieldLayout.DATA_DRIVING_ELAPSED   , driveElapse);
                this.setValue(FieldLayout.DATA_IDLE_ELAPSED      , idleElapse);
                this.setValue(FieldLayout.DATA_ADDRESS           , i18n.getString("TripReport.tripTimeDistance","Trip Time/Distance"));
                this.setValue(FieldLayout.DATA_DRIVER_ID         , driverID); // 2.5.2-B51
            } else {
                // -- blank row
                this.setValue(FieldLayout.DATA_ADDRESS, "");
            }
        }
    }
        
}
