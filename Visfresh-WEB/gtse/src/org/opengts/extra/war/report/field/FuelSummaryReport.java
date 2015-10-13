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
//  2010/06/17  Martin D. Flynn
//     -Initial release
//  2012/10/16  Martin D. Flynn
//     -Fixed EventData record date range end-time selection 
//  2013/08/06  Martin D. Flynn
//     -Added number of stops count (trip based on ignition, start/stop, speed)
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

public class FuelSummaryReport
    extends ReportData
{

    // ------------------------------------------------------------------------
    // Properties

    private static final String PROP_tripStartType          = "tripStartType";
    private static final String PROP_minimumSpeedKPH        = "minimumSpeedKPH"; 
    private static final String PROP_minimumStoppedTime     = "minimumStoppedTime"; // TRIP_ON_SPEED only
    private static final String PROP_stopOnIgnitionOff      = "stopOnIgnitionOff";
    private static final String PROP_countNumberOfStops     = "countNumberOfStops";

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
    *** Default mimimum stopped elapsed time to be considered stopped
    **/
    private static final long   MIN_STOPPED_TIME_SEC        = DateTime.MinuteSeconds(5);

    /**
    *** Delimit stop with ignition off? (if this occurs before the minimum stopped time)
    **/
    private static final boolean STOP_ON_IGNITION_OFF       = false;

    /**
    *** Count number of stops (requires reading the entire range of Events)
    **/
    private static final boolean COUNT_NUMBER_OF_STOPS      = false;

    // ------------------------------------------------------------------------
    // Summary report
    // 1 'count' record per device
    // ------------------------------------------------------------------------

    private java.util.List<FieldData>   rowData             = null;
    private java.util.List<FieldData>   totData             = null;

    /* global property vars */
    private double                      minSpeedKPH         = MIN_SPEED_KPH;
    private long                        minStoppedTimeSec   = MIN_STOPPED_TIME_SEC;
    private boolean                     stopOnIgnitionOff   = STOP_ON_IGNITION_OFF;
    private boolean                     countNumberOfStops  = COUNT_NUMBER_OF_STOPS;
    private long                        lookAheadSeconds    = 0L; // not used

    /* device count */
    private int                         deviceCount         = 0;
    
    /* device vars (reset for each device */
    private boolean                     startStopSupported  = false;
    private int                         ignitionCodes[]     = null;
    private int                         tripStartType       = TRIP_ON_SPEED;
    private boolean                     speedIsMoving       = false; // TRIP_ON_SPEED only
    private long                        lastMovingTime      = 0L;    // TRIP_ON_SPEED only
    private long                        lastStoppedTime     = 0L;    // TRIP_ON_SPEED only
    private EventData                   tripStartEvent      = null;
    private EventData                   tripStopEvent       = null;
    private int                         tripStopCount       = 0;

    /* grand total values */
    private double                      totEngineHours      = 0.0;    // engineHours
    private double                      totFuelTotal        = 0.0;    // fuelTotal
    private double                      totFuelLevel        = 0.0;    // fuelRemain
    private double                      totFuelRemain       = 0.0;    // fuelRemain
    private double                      totFuelEconomy      = 0.0;    // fuelEconomy
    private double                      totIdleHours        = 0.0;    // idleHours
    private double                      totFuelIdle         = 0.0;    // fuelIdle
    private double                      totWorkHours        = 0.0;    // 
    private double                      totFuelWork         = 0.0;    // 
    private double                      totPTOHours         = 0.0;    // ptoHours
    private double                      totFuelPTO          = 0.0;    // fuelPTO
    private double                      totOdometerKM       = 0.0;    // odometerKM
    private int                         totStopCount        = 0;      // stop count

    // ------------------------------------------------------------------------

    /**
    *** Fuel Summary Report Constructor
    *** @param rptEntry The ReportEntry
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public FuelSummaryReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        if (this.getAccount() == null) {
            throw new ReportException("Account-ID not specified");
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {
        RTProperties tripRTP     = this.getProperties();
        this.tripStartType       = TRIP_ON_IGNITION; // initialized later
        this.minSpeedKPH         = tripRTP.getDouble( PROP_minimumSpeedKPH   , MIN_SPEED_KPH);
        this.minStoppedTimeSec   = tripRTP.getLong(   PROP_minimumStoppedTime, MIN_STOPPED_TIME_SEC);
        this.stopOnIgnitionOff   = tripRTP.getBoolean(PROP_stopOnIgnitionOff , STOP_ON_IGNITION_OFF);
        this.countNumberOfStops  = tripRTP.getBoolean(PROP_countNumberOfStops, COUNT_NUMBER_OF_STOPS);
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Gets the bound ReportLayout singleton instance for this report
    *** @return The bound ReportLayout
    **/
    public static ReportLayout GetReportLayout()
    {
        // bind the report format to this data
        return FieldLayout.getReportLayout();
    }

    /**
    *** Gets the bound ReportLayout singleton instance for this report
    *** @return The bound ReportLayout
    **/
    public ReportLayout getReportLayout()
    {
        // bind the report format to this data
        return GetReportLayout();
    }

    // ------------------------------------------------------------------------

    private EventData getLookAheadEventData(int ofs)
    {
        return null;
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
                // "Stop" inferred by IgnitionOff
                return true;
            } else
            if (this.stopOnIgnitionOff && (this.ignitionCodes != null) && (sc == this.ignitionCodes[0])) {
                // "Stop" inferred by IgnitionOff
                return true;
            }
        } else
        if (this.tripStartType == TRIP_ON_SPEED) {
            //Print.logInfo("TripStop: moving="+this.speedIsMoving + ", speed="+ev.getSpeedKPH() + ", minSpeed="+this.minSpeedKPH);
            if (this.speedIsMoving) {
                int sc = ev.getStatusCode();
                if (this.stopOnIgnitionOff && (this.ignitionCodes != null) && (sc == this.ignitionCodes[0])) {
                    // "Stop" inferred by IgnitionOff
                    this.speedIsMoving   = false;
                    this.lastMovingTime  = 0L;
                    this.lastStoppedTime = 0L;
                    return true;
                } else
                if (ev.getSpeedKPH() < this.minSpeedKPH) {
                    // we're stopped, check minimum stopped time
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
                            // "Stop" inferred by excessive time-lapse to next event
                            this.speedIsMoving   = false;
                            this.lastMovingTime  = 0L;
                            this.lastStoppedTime = 0L;
                            return true;
                        }
                    } else {
                        // this is the last event, assume this is a stop
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

    /**
    *** Creates and returns an iterator for the row data displayed in the body of this report.
    *** @return The body row data iterator
    **/
    public DBDataIterator getBodyDataIterator()
    {

        /* init */
        this.rowData        = new Vector<FieldData>();
        this.deviceCount    = 0;
        this.totEngineHours = 0.0;    // engineHours
        this.totFuelTotal   = 0.0;    // fuelTotal
        this.totFuelLevel   = 0.0;    // fuelLevel
        this.totFuelRemain  = 0.0;    // fuelRemain
        this.totFuelEconomy = 0.0;    // fuelEconomy
        this.totIdleHours   = 0.0;    // idleHours
        this.totFuelIdle    = 0.0;    // fuelIdle
        this.totWorkHours   = 0.0;    // 
        this.totFuelWork    = 0.0;    // 
        this.totPTOHours    = 0.0;    // ptoHours
        this.totFuelPTO     = 0.0;    // fuelPTO
        this.totOdometerKM  = 0.0;    // odometerKM
        this.totStopCount   = 0;

        /* report date range */
        long startTime = this.getTimeStart();
        long endTime   = this.getTimeEnd();
        long selLimit  = this.getSelectionLimit();

        /* loop through devices */
        String devID = "";
        ReportDeviceList devList = this.getReportDeviceList();
        for (Iterator i = devList.iterator(); i.hasNext();) {
            devID = (String)i.next();
            try {

                /* get device */
                Device device = devList.getDevice(devID);
                if (device == null) {
                    // should never occur
                    Print.logError("Returned DeviceList 'Device' is null: " + devID);
                    continue;
                }
                String accountID = device.getAccountID();
                String deviceID  = device.getDeviceID();

                // Device supports start/stop
                this.startStopSupported = device.getStartStopSupported();
                if (!this.startStopSupported) {
                    // TODO: scan EventData for STATUS_MOTION_START/STATUS_MOTION_STOP
                }

                // Device ignition statusCodes
                this.ignitionCodes = device.getIgnitionStatusCodes();
                boolean hasIgnition = (this.ignitionCodes != null);

                // trip start/stop type
                this.speedIsMoving   = false; // TRIP_ON_SPEED only
                this.lastMovingTime  = 0L;    // TRIP_ON_SPEED only
                this.lastStoppedTime = 0L;    // TRIP_ON_SPEED only
                this.tripStartType   = TRIP_ON_IGNITION;
                String tt = this.getProperties().getString(PROP_tripStartType,MOTION_IGNITION[0]).toLowerCase();
                //Print.logInfo("Trip type: " + tt);
                if (ListTools.contains(MOTION_DEFAULT,tt)) {
                    // "default" (TRIP_ON_ENGINE will not be selected when using "default")
                    String devCode = device.getDeviceCode();
                    DCServerConfig dcs = DCServerFactory.getServerConfig(devCode);
                    if ((dcs == null) && StringTools.isBlank(devCode) && Account.IsDemoAccount(accountID)) {
                        // special case for "demo" account when 'deviceCode' is blank
                        dcs = DCServerFactory.getServerConfig(DCServerFactory.OPENDMTP_NAME);
                        if (dcs == null) {
                            Print.logWarn("Account 'demo' DCServerConfig not found: " + DCServerFactory.OPENDMTP_NAME);
                        }
                    }
                    if (dcs != null) {
                        // DCServerConfig found
                        if (dcs.getStartStopSupported(false/*default*/)) {
                            // Device supports start/stop
                            this.tripStartType = TRIP_ON_START;
                        } else
                        if (hasIgnition) {
                            // Device supports ignition state
                            this.tripStartType = TRIP_ON_IGNITION;
                        } else {
                            // Default to speed, if not ignition, or start/stop
                            this.tripStartType = TRIP_ON_SPEED;
                        }
                    } else {
                        // DCServerConfig not found ('deviceCode' is either blank or invalid)
                        if (hasIgnition) {
                            // Device supports ignition state
                            this.tripStartType = TRIP_ON_IGNITION;
                        } else {
                            // Default
                            this.tripStartType = TRIP_ON_SPEED;
                        }
                    }
                } else
                if (ListTools.contains(MOTION_STARTSTOP,tt)) {
                    // "startstop"
                    this.tripStartType = TRIP_ON_START;
                    //Print.logInfo("Trip delimiter: start/stop [ignition = " + hasIgnition + "]");
                } else
                if (ListTools.contains(MOTION_IGNITION,tt)/* && hasIgnition */) {
                    // "ignition"
                    this.tripStartType = TRIP_ON_IGNITION;
                    if (!hasIgnition) {
                         this.ignitionCodes = new int[] { StatusCodes.STATUS_IGNITION_OFF, StatusCodes.STATUS_IGNITION_ON };
                         hasIgnition = true;
                    }
                    //Print.logInfo("Trip delimiter: ignition");
                } else
                if (ListTools.contains(MOTION_ENGINE,tt)) {
                    // "engine"
                    this.tripStartType = TRIP_ON_ENGINE;
                    //Print.logInfo("Trip delimiter: engine");
                } else {
                    // "speed", "motion"
                    this.tripStartType = TRIP_ON_SPEED;
                    //Print.logInfo("Trip delimiter: speed");
                }
                
                /* trip events */
                this.tripStartEvent = null;
                this.tripStopEvent  = null;
                this.tripStopCount  = 0;

                // Starting values (read through events in ascending order)
                double strEngineHours = 0.0;    // engineHours
                double strFuelTotal   = 0.0;    // fuelTotal
                double strFuelLevel   = 0.0;    // fuelLevel
                double strFuelRemain  = 0.0;    // fuelRemain
                double strIdleHours   = 0.0;    // idleHours
                double strFuelIdle    = 0.0;    // fuelIdle
                double strPTOHours    = 0.0;    // ptoHours
                double strFuelPTO     = 0.0;    // fuelPTO
                double strOdometerKM  = 0.0;    // odometerKM
                try {

                    /* get first events in range */
                    long limit = this.countNumberOfStops? selLimit : 50L; // first events
                    EventData eda[] = EventData.getRangeEvents(
                        accountID, deviceID,
                        this.getTimeStart(), this.getTimeEnd(), // was "-1L" (v2.4.6-B01)
                        null/*statusCodes*/,
                        false/*validGPS*/,
                        EventData.LimitType.FIRST, limit, true/*ascending*/,
                        null/*addtnlSelect*/,
                        null/*rcdHandler*/);

                    /* get starting values */
                    if (!ListTools.isEmpty(eda)) {
                        for (EventData ed : eda) {

                            /* capture first occurance of each of these values */
                            if (strEngineHours <= 0.0) { strEngineHours = ed.getEngineHours();    }
                            if (strFuelTotal   <= 0.0) { strFuelTotal   = ed.getFuelTotal();      }
                            if (strFuelLevel   <= 0.0) { strFuelLevel   = ed.getFuelLevel(true);  }
                            if (strFuelRemain  <= 0.0) { strFuelRemain  = ed.getFuelRemain(true); }
                            if (strIdleHours   <= 0.0) { strIdleHours   = ed.getIdleHours();      }
                            if (strFuelIdle    <= 0.0) { strFuelIdle    = ed.getFuelIdle();       }
                            if (strPTOHours    <= 0.0) { strPTOHours    = ed.getPtoHours();       }
                            if (strFuelPTO     <= 0.0) { strFuelPTO     = ed.getFuelPTO();        }
                            if (strOdometerKM  <= 0.0) { strOdometerKM  = ed.getOdometerKM();     }

                            /* count trip stops */
                            if (this.countNumberOfStops) {

                                /* trip start */
                                boolean tripStart = this.isTripStart(ed);
                                if (tripStart) {
                                    this.tripStartEvent = ed;
                                }
    
                                /* trip stop */
                                boolean tripStop  = tripStart? false : this.isTripStop(ed);
                                if (tripStop) {
                                    this.tripStopEvent = ed;
                                    this.tripStopCount++;
                                }
                            
                            }

                        }
                    }

                    /*
                    Print.logInfo("Starting Values: " + accountID + "/" + deviceID);
                    Print.logInfo("  strEngineHours  : " + strEngineHours);
                    Print.logInfo("  strFuelTotal    : " + strFuelTotal);
                    Print.logInfo("  strFuelLevel    : " + strFuelLevel);
                    Print.logInfo("  strFuelRemain   : " + strFuelRemain);
                    Print.logInfo("  strIdleHours    : " + strIdleHours);
                    Print.logInfo("  strFuelIdle     : " + strFuelIdle);
                    Print.logInfo("  strPTOHours     : " + strPTOHours);
                    Print.logInfo("  strFuelPTO      : " + strFuelPTO);
                    Print.logInfo("  strOdometerKM   : " + strOdometerKM);
                    */

                } catch (DBException dbe) {
                    Print.logException("Unable to obtain EventData records", dbe);
                }
                    
                // Ending values (read through events in descending order)
                double endEngineHours = 0.0;
                double endFuelTotal   = 0.0;
                double endFuelLevel   = 0.0;
                double endFuelRemain  = 0.0;
                double endIdleHours   = 0.0;
                double endFuelIdle    = 0.0;
                double endPTOHours    = 0.0;
                double endFuelPTO     = 0.0;
                double endOdometerKM  = 0.0;
                try {

                    /* get last events in range */
                    long limit = 50L; // last events
                    EventData eda[] = EventData.getRangeEvents(
                        accountID, deviceID,
                        this.getTimeStart(), this.getTimeEnd(), // was "-1L" (v2.4.6-B01)
                        null/*statusCodes*/,
                        false/*validGPS*/,
                        EventData.LimitType.LAST, limit, false/*descending*/,
                        null/*addtnlSelect*/,
                        null/*rcdHandler*/);

                    /* get starting values */
                    if (!ListTools.isEmpty(eda)) {
                        for (EventData ed : eda) {

                            /* capture first occurance of each of these values */
                            if (endEngineHours <= 0.0) { endEngineHours = ed.getEngineHours();    }
                            if (endFuelTotal   <= 0.0) { endFuelTotal   = ed.getFuelTotal();      }
                            if (endFuelLevel   <= 0.0) { endFuelLevel   = ed.getFuelLevel(true);  }
                            if (endFuelRemain  <= 0.0) { endFuelRemain  = ed.getFuelRemain(true); }
                            if (endIdleHours   <= 0.0) { endIdleHours   = ed.getIdleHours();      }
                            if (endFuelIdle    <= 0.0) { endFuelIdle    = ed.getFuelIdle();       }
                            if (endPTOHours    <= 0.0) { endPTOHours    = ed.getPtoHours();       }
                            if (endFuelPTO     <= 0.0) { endFuelPTO     = ed.getFuelPTO();        }
                            if (endOdometerKM  <= 0.0) { endOdometerKM  = ed.getOdometerKM();     }

                        }
                    }

                    /*
                    Print.logInfo("Ending Values: " + accountID + "/" + deviceID);
                    Print.logInfo("  endEngineHours  : " + endEngineHours);
                    Print.logInfo("  endFuelTotal    : " + endFuelTotal);
                    Print.logInfo("  endFuelLevel    : " + endFuelLevel);
                    Print.logInfo("  endFuelRemain   : " + endFuelRemain);
                    Print.logInfo("  endIdleHours    : " + endIdleHours);
                    Print.logInfo("  endFuelIdle     : " + endFuelIdle);
                    Print.logInfo("  endPTOHours     : " + endPTOHours);
                    Print.logInfo("  endFuelPTO      : " + endFuelPTO);
                    Print.logInfo("  endOdometerKM   : " + endOdometerKM);
                    */

                } catch (DBException dbe) {
                    Print.logException("Unable to obtain EventData records", dbe);
                }

                // Report Fields
                double useEngineHours = (endEngineHours > strEngineHours)? (endEngineHours - strEngineHours) : 0.0; // +positive
                double useFuelTotal   = (endFuelTotal   > strFuelTotal  )? (endFuelTotal   - strFuelTotal  ) : 0.0; // +positive
                double useFuelLevel   = (endFuelLevel   < strFuelLevel  )? (endFuelLevel   - strFuelLevel  ) : 0.0; // -negative
                double useFuelRemain  = (endFuelRemain  < strFuelRemain )? (endFuelRemain  - strFuelRemain ) : 0.0; // -negative
                double useFuelEconomy = 0.0; // calculate below
                double useIdleHours   = (endIdleHours   > strIdleHours  )? (endIdleHours   - strIdleHours  ) : 0.0; // +positive
                double useFuelIdle    = (endFuelIdle    > strFuelIdle   )? (endFuelIdle    - strFuelIdle   ) : 0.0; // +positive
                double usePTOHours    = (endPTOHours    > strPTOHours   )? (endPTOHours    - strPTOHours   ) : 0.0; // +positive
                double useFuelPTO     = (endFuelPTO     > strFuelPTO    )? (endFuelPTO     - strFuelPTO    ) : 0.0; // +positive
                double useOdometerKM  = (endOdometerKM  > strOdometerKM )? (endOdometerKM  - strOdometerKM ) : 0.0; // +positive
                double useWorkHours   = useEngineHours - useIdleHours;
                double useFuelWork    = useFuelTotal   - useFuelIdle;
                int    useNumStops    = this.tripStopCount;
                double useAvgStopIdle = (useNumStops > 0)? (useIdleHours / (double)useNumStops) : 0.0;
                Device.FuelEconomyType fuelEconType = Device.FuelEconomyType.UNKNOWN;
                if (useFuelTotal > 0.0) {
                    // based on actual fuel consumed
                    useFuelEconomy = useOdometerKM / useFuelTotal;
                    fuelEconType   = Device.FuelEconomyType.FUEL_CONSUMED;
                } else
                if (useFuelRemain < 0.0) { // negative (we have less fuel now than when we started)
                    // based on the change in the tank fuel
                    double fuelDelta = -useFuelRemain;
                    useFuelEconomy = (fuelDelta > 0.0)? (useOdometerKM / fuelDelta) : 0.0;
                    fuelEconType   = Device.FuelEconomyType.FUEL_REMAINING;
                } else
                if ((useFuelLevel < 0.0) && (device.getFuelCapacity() > 0.0)) {
                    // based on the change in the fuel level and tank capacity
                    double fuelDelta = (-useFuelLevel) * device.getFuelCapacity();
                    useFuelEconomy = (fuelDelta > 0.0)? (useOdometerKM / fuelDelta) : 0.0;
                    fuelEconType   = Device.FuelEconomyType.FUEL_LEVEL;
                } else {
                    // just use the estimated fuel economy specify on the Device record
                    useFuelEconomy = (useOdometerKM > 0.0)? device.getFuelEconomy() : 0.0;
                    fuelEconType   = Device.FuelEconomyType.DEVICE_ECONOMY;
                }
                /*
                Print.logInfo("Used Values: " + accountID + "/" + deviceID);
                Print.logInfo("  useEngineHours  : " + useEngineHours);
                Print.logInfo("  useFuelTotal    : " + useFuelTotal);
                Print.logInfo("  useFuelLevel    : " + useFuelLevel);  // negative
                Print.logInfo("  useFuelRemain   : " + useFuelRemain); // negative
                Print.logInfo("  useFuelEconomy  : " + useFuelEconomy + " [" + fuelEconType + "]");
                Print.logInfo("  useIdleHours    : " + useIdleHours);
                Print.logInfo("  useFuelIdle     : " + useFuelIdle);
                Print.logInfo("  useWorkHours    : " + useWorkHours);
                Print.logInfo("  useFuelWork     : " + useFuelWork);
                Print.logInfo("  usePTOHours     : " + usePTOHours);
                Print.logInfo("  useFuelPTO      : " + useFuelPTO);
                Print.logInfo("  useOdometerKM   : " + useOdometerKM);
                Print.logInfo("  useNumStops     : " + useNumStops);
                Print.logInfo("  useAvgStopIdle  : " + useAvgStopIdle);
                */
                FieldData fd = new FieldData();
                fd.setDevice(device);
                fd.setDouble(FieldLayout.DATA_ENGINE_HOURS      , useEngineHours);
                fd.setDouble(FieldLayout.DATA_FUEL_TOTAL        , useFuelTotal);
                fd.setDouble(FieldLayout.DATA_FUEL_LEVEL        , useFuelLevel);    // negative
                fd.setDouble(FieldLayout.DATA_FUEL_REMAIN       , useFuelRemain);   // negative
                fd.setDouble(FieldLayout.DATA_FUEL_ECONOMY      , useFuelEconomy);
                fd.setValue( FieldLayout.DATA_FUEL_ECONOMY_TYPE , fuelEconType);
                fd.setDouble(FieldLayout.DATA_IDLE_HOURS        , useIdleHours);
                fd.setDouble(FieldLayout.DATA_FUEL_IDLE         , useFuelIdle);
                fd.setDouble(FieldLayout.DATA_WORK_HOURS        , useWorkHours);
                fd.setDouble(FieldLayout.DATA_FUEL_WORK         , useFuelWork);
                fd.setDouble(FieldLayout.DATA_PTO_HOURS         , usePTOHours);
                fd.setDouble(FieldLayout.DATA_FUEL_PTO          , useFuelPTO);
                fd.setDouble(FieldLayout.DATA_ODOMETER_DELTA    , useOdometerKM);   // odomDelta
                fd.setInt(   FieldLayout.DATA_STOP_COUNT        , useNumStops);
                fd.setDouble(FieldLayout.DATA_AVERAGE_IDLE_HOURS, useAvgStopIdle);  // avg idle per stop
                this.rowData.add(fd);
                this.deviceCount++;

                // save totals
                this.totEngineHours += useEngineHours;
                this.totFuelTotal   += useFuelTotal;
                this.totFuelLevel   += useFuelLevel;  // negative
                this.totFuelRemain  += useFuelRemain; // negative
                this.totFuelEconomy += useFuelEconomy;
                this.totIdleHours   += useIdleHours;
                this.totFuelIdle    += useFuelIdle;
                this.totWorkHours   += useWorkHours;
                this.totFuelWork    += useFuelWork;
                this.totPTOHours    += usePTOHours;
                this.totFuelPTO     += useFuelPTO;
                this.totOdometerKM  += useOdometerKM;
                this.totStopCount   += useNumStops;

            } catch (DBException dbe) {
                Print.logError("Error generating report for Device: " + devID);
            }
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
        
        /* no devices? */
        if (this.deviceCount <= 0) {
            return null;
        }

        /* init */
        this.totData = new Vector<FieldData>();
        String devTitles[] = this.getRequestProperties().getDeviceTitles();
        I18N i18n = this.getPrivateLabel().getI18N(FuelSummaryReport.class);

        /* totals */
        FieldData fdTot = new FieldData();
        fdTot.setRowType(DBDataRow.RowType.TOTAL);
        fdTot.setString(FieldLayout.DATA_DEVICE_DESC       , i18n.getString("FuelSummaryReport.total","Total",devTitles));
        fdTot.setDouble(FieldLayout.DATA_ENGINE_HOURS      , this.totEngineHours);
        fdTot.setDouble(FieldLayout.DATA_FUEL_TOTAL        , this.totFuelTotal);
      //fdTot.setDouble(FieldLayout.DATA_FUEL_LEVEL        , this.totFuelLevel);    // negative delta
      //fdTot.setDouble(FieldLayout.DATA_FUEL_REMAIN       , this.totFuelRemain);   // negative delta
      //fdTot.setDouble(FieldLayout.DATA_FUEL_ECONOMY      , this.totFuelEconomy);
      //fdTot.setValue( FieldLayout.DATA_FUEL_ECONOMY_TYPE , fuelEconType);
        fdTot.setDouble(FieldLayout.DATA_IDLE_HOURS        , this.totIdleHours);
        fdTot.setDouble(FieldLayout.DATA_FUEL_IDLE         , this.totFuelIdle);
        fdTot.setDouble(FieldLayout.DATA_WORK_HOURS        , this.totWorkHours);
        fdTot.setDouble(FieldLayout.DATA_FUEL_WORK         , this.totFuelWork);
        fdTot.setDouble(FieldLayout.DATA_PTO_HOURS         , this.totPTOHours);
        fdTot.setDouble(FieldLayout.DATA_FUEL_PTO          , this.totFuelPTO);
        fdTot.setDouble(FieldLayout.DATA_ODOMETER_DELTA    , this.totOdometerKM);   // odomDelta
        fdTot.setInt(   FieldLayout.DATA_STOP_COUNT        , this.totStopCount);    // stop count
        this.totData.add(fdTot);

        /* average */
        double avgEngineHours = this.totEngineHours / (double)this.deviceCount;
        double avgFuelTotal   = this.totFuelTotal   / (double)this.deviceCount;
        double avgFuelLevel   = this.totFuelLevel   / (double)this.deviceCount; // negative
        double avgFuelRemain  = this.totFuelRemain  / (double)this.deviceCount; // negative
        double avgFuelEconomy = this.totFuelEconomy / (double)this.deviceCount;
        double avgIdleHours   = this.totIdleHours   / (double)this.deviceCount;
        double avgFuelIdle    = this.totFuelIdle    / (double)this.deviceCount;
        double avgWorkHours   = this.totWorkHours   / (double)this.deviceCount;
        double avgFuelWork    = this.totFuelWork    / (double)this.deviceCount;
        double avgPTOHours    = this.totPTOHours    / (double)this.deviceCount;
        double avgFuelPTO     = this.totFuelPTO     / (double)this.deviceCount;
        double avgOdometerKM  = this.totOdometerKM  / (double)this.deviceCount;
        double avgStopCount   = this.totStopCount   / (double)this.deviceCount;
        FieldData fdAvg = new FieldData();
        fdAvg.setRowType(DBDataRow.RowType.TOTAL);
        fdAvg.setString(FieldLayout.DATA_DEVICE_DESC       , i18n.getString("FuelSummaryReport.average","Average/{0}",devTitles));
        fdAvg.setDouble(FieldLayout.DATA_ENGINE_HOURS      , avgEngineHours);
        fdAvg.setDouble(FieldLayout.DATA_FUEL_TOTAL        , avgFuelTotal);
        fdAvg.setDouble(FieldLayout.DATA_FUEL_LEVEL        , avgFuelLevel);     // negative average
        fdAvg.setDouble(FieldLayout.DATA_FUEL_REMAIN       , avgFuelRemain);    // negative average
        fdAvg.setDouble(FieldLayout.DATA_FUEL_ECONOMY      , avgFuelEconomy);
      //fdAvg.setValue( FieldLayout.DATA_FUEL_ECONOMY_TYPE , fuelEconType);
        fdAvg.setDouble(FieldLayout.DATA_IDLE_HOURS        , avgIdleHours);
        fdAvg.setDouble(FieldLayout.DATA_FUEL_IDLE         , avgFuelIdle);
        fdAvg.setDouble(FieldLayout.DATA_WORK_HOURS        , avgWorkHours);
        fdAvg.setDouble(FieldLayout.DATA_FUEL_WORK         , avgFuelWork);
        fdAvg.setDouble(FieldLayout.DATA_PTO_HOURS         , avgPTOHours);
        fdAvg.setDouble(FieldLayout.DATA_FUEL_PTO          , avgFuelPTO);
        fdAvg.setDouble(FieldLayout.DATA_ODOMETER_DELTA    , avgOdometerKM);    // odomDelta
        fdAvg.setDouble(FieldLayout.DATA_STOP_COUNT        , avgStopCount);     // stop count
        this.totData.add(fdAvg);

        /* return totals */
        return new ListDataIterator(this.totData);
        
    }


}
