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
//  2011/04/01  Martin D. Flynn
//     -Added map support
//  2012/05/27  Martin D. Flynn
//     -Fixed map timestamp issue
//     -Added engine status code support
//  2015/02/06  Martin D. Flynn
//     -Fixed summary report date range [2.5.8-B62]
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

public class DailySummaryReport
    extends ReportData
    implements DBRecordHandler<EventData>
{

    // ------------------------------------------------------------------------
    // Properties

    private static final String          PROP_reportType        = "reportType"; // detail|summary

    private static final String          REPORT_TYPE_detail     = "detail";     // daily detail
    private static final String          REPORT_TYPE_summary    = "summary";    // range summary
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private I18N                         i18n                   = null;

    private Device                       device                 = null;
    private boolean                      isDetailReport         = false;

    private EventData                    firstEvent             = null;
    private EventData                    lastEvent              = null;

    private Map<Integer,AccumulatorLong> codeCount              = new OrderedMap<Integer,AccumulatorLong>();

    private Vector<FieldData>            rowData                = null;
    private Vector<FieldData>            totalData              = null;

    // ------------------------------------------------------------------------

    /**
    *** DigitalInput Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public DailySummaryReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        this.i18n = reqState.getPrivateLabel().getI18N(DailySummaryReport.class);

        /* Account check */
        Account account = this.getAccount();
        if (account == null) {
            throw new ReportException("Account-ID not specified");
        }

        /* Device check */
        if (this.getDeviceCount() <= 0) {
            throw new ReportException("No Devices specified");
        }

        /* detail report (default to summary) */
        this.isDetailReport = this.getProperties().getString(PROP_reportType,REPORT_TYPE_summary).equalsIgnoreCase(REPORT_TYPE_detail);

    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {
        //ReportConstraints rc = this.getReportConstraints();
        //Print.logInfo("LimitType=" + rc.getSelectionLimitType() + ", Limit=" + rc.getSelectionLimit());
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this report handles only a single device at a time
    *** @return True If this report handles only a single device at a time
    **/
    public boolean isSingleDeviceOnly()
    {
        return false;
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

        /* Device */
        if (device == null) {
            return EventData.EMPTY_ARRAY;
        }

        /* adjust report constraints */
        //ReportConstraints rc = this.getReportConstraints();
        //rc.setValidGPSRequired(false); // don't need just valid gps events

        /* get events */
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

        /* timezone and starting day-number */
        TimeZone TZ = this.getTimeZone();

        /* init */
        this.rowData = new Vector<FieldData>();

        /* loop through devices */
        String accountID = this.getAccountID();
        String devID = "";
        ReportDeviceList devList = this.getReportDeviceList();
        for (Iterator i = devList.iterator(); i.hasNext();) {
            devID = (String)i.next();
            try {

                /* get device */
                this.device = devList.getDevice(devID);

                // --------------------------------------------------
                // -- Summary: single-pass through the following loop
                // -- Detail: one-pass for each day
                long dayStart;
                long dayEnd;
                long dayIndex;
                long timeStart;
                long timeEnd;
                if (this.isDetailReport) {
                    // -- Detail
                    dayStart  = DateTime.getDayNumberFromDate(new DateTime(this.getTimeStart(),TZ));
                    dayEnd    = DateTime.getDayNumberFromDate(new DateTime(this.getTimeEnd(),TZ));
                    dayIndex  = dayStart;
                    timeStart = this.getTimeStart();
                    timeEnd   = DateTime.getDateFromDayNumber(dayIndex,TZ).createDateTime().getDayEnd();
                } else {
                    // -- Summary
                    dayStart  = -1L;
                    dayEnd    = -1L;
                    dayIndex  = -1L;
                    timeStart = this.getTimeStart();
                    timeEnd   = this.getTimeEnd(); // fixed [2.5.8-B62]
                }

                /* clear counters */
                this.firstEvent = null;
                this.lastEvent  = null;
                this.codeCount.clear();

                /* totals */
                double totOdomDeltaKM = 0.0;
                double totFuelDeltaL  = 0.0;
                Map<Integer,AccumulatorLong> totCodeCount =  null;

                /* last FieldData retained for linear interprolation */
                FieldData lastFieldData = null;

                /* daily/full time-range loop */
                for (;;) {

                    /* reset */
                    EventData prevDayLastEvent = this.lastEvent; // previous days last event
                    this.firstEvent = null;
                    this.lastEvent  = null;
                    this.codeCount.clear();
    
                    /* get events */
                    //Print.logInfo("Reading EventData: dev="+this.device +", ts="+timeStart +", te="+timeEnd);
                    this.getEventData_Device(this.device, timeStart, timeEnd, this); // <== callback to 'handleDBRecord'
                    // -- sets "this.firstEvent" and "this.lastEvent"
    
                    /* first/last event */
                    double odomDeltaKM = 0.0;
                    double fuelDeltaL  = 0.0;
                    if ((this.firstEvent != null) && (this.lastEvent != null)) {
                        odomDeltaKM = this.lastEvent.getOdometerKM() - this.firstEvent.getOdometerKM();
                        fuelDeltaL  = this.lastEvent.getFuelTotal()  - this.firstEvent.getFuelTotal();
                        if (prevDayLastEvent != null) {
                            // -- Note: changes in distance/fuel that occur over midnight are not handled in the 
                            // -  above calculation.  Check for this condition and perform linear interprolation 
                            // -  to apply the resulting values to the proper day.
                            long prevTimeSec  = prevDayLastEvent.getTimestamp(); // previous day last timestamp
                            long firstTimeSec = this.firstEvent.getTimestamp();  // current day first timestamp
                            long deltaPrevSec = timeStart - prevTimeSec;    // time before midnight (must be >= 0)
                            long deltaTimeSec = firstTimeSec - prevTimeSec; // time between events (must be > 0)
                            if ((deltaPrevSec >= 0L) && (deltaTimeSec > 0L) && (deltaPrevSec <= deltaTimeSec)) {
                                double prevDayPct = (double)deltaPrevSec / (double)deltaTimeSec;
                                double currDayPct = 1.0 - prevDayPct;
                                // -- check odometer
                                if (prevDayLastEvent.getOdometerKM() < this.firstEvent.getOdometerKM()) {
                                    // -- odometer changed over midnight
                                    double deltaKM = this.firstEvent.getOdometerKM() - prevDayLastEvent.getOdometerKM();
                                    // -- update previous day
                                    double prevKM = prevDayPct * deltaKM; // amount to apply to previous day
                                    if ((lastFieldData != null) && (prevKM > 0.0)) {
                                        double fdDistKM = lastFieldData.getDouble(FieldLayout.DATA_DISTANCE) + prevKM;
                                        lastFieldData.setDouble(FieldLayout.DATA_DISTANCE      , fdDistKM);
                                        lastFieldData.setDouble(FieldLayout.DATA_ODOMETER_DELTA, fdDistKM);
                                    }
                                    // -- update current day
                                    double currKM = currDayPct * deltaKM; // amount to apply to current day
                                    if (currKM > 0.0) {
                                        odomDeltaKM += currKM;
                                    }
                                }
                                // -- check fuel
                                if (prevDayLastEvent.getFuelTotal() < this.firstEvent.getFuelTotal()) {
                                    // -- fuel total changed over midnight
                                    double deltaL = this.firstEvent.getFuelTotal() - prevDayLastEvent.getFuelTotal();
                                    // -- update previous day
                                    double prevL = prevDayPct * deltaL; // amount to apply to previous day
                                    if ((lastFieldData != null) && (prevL > 0.0)) {
                                        double fdFuelL = lastFieldData.getDouble(FieldLayout.DATA_FUEL_TOTAL) + prevL;
                                        lastFieldData.setDouble(FieldLayout.DATA_FUEL_TOTAL, fdFuelL);
                                        lastFieldData.setDouble(FieldLayout.DATA_FUEL_TRIP , fdFuelL);
                                    }
                                    // -- update current day
                                    double currL = currDayPct * deltaL; // amount to apply to current day
                                    if (currL > 0.0) {
                                        fuelDeltaL += currL;
                                    }
                                }
                            }
                        }
                    } else {
                        Print.logWarn("Device missing first/last event: " + devID);
                    }

                    /* accumulate totals */
                    totOdomDeltaKM += odomDeltaKM;
                    totFuelDeltaL  += fuelDeltaL;
    
                    /* create report line entry */
                    FieldData fd = new FieldData();
                    // -- Account/Device
                    fd.setAccount(this.getAccount());
                    fd.setString(FieldLayout.DATA_ACCOUNT_ID    , this.getAccountID());
                    fd.setDevice(this.device);
                    fd.setString(FieldLayout.DATA_DEVICE_ID     , devID);
                    // -- Distance
                    fd.setDouble(FieldLayout.DATA_DISTANCE      , odomDeltaKM);
                    fd.setDouble(FieldLayout.DATA_ODOMETER_DELTA, odomDeltaKM);
                    // -- Fuel
                    fd.setDouble(FieldLayout.DATA_FUEL_TOTAL    , fuelDeltaL);
                    fd.setDouble(FieldLayout.DATA_FUEL_TRIP     , fuelDeltaL);
                    // -- Date/DayNumber
                    fd.setLong(  FieldLayout.DATA_DATE          , dayIndex); // may be <= 0 for summary
                    // -- Status code counts
                    if (this.codeCount != null) { // never null
                        for (Integer sc : this.codeCount.keySet()) {
                            int code = sc.intValue();
                            AccumulatorLong al = this.codeCount.get(sc);
                            String cKey = FieldLayout.DATA_STATUS_COUNT + "_" + StringTools.toHexString(code,16);
                            fd.setLong(cKey, al.get());
                            // -- accumulate totals
                            if (this.isDetailReport) {
                                // -- Detail: accumulator totals for end of detail
                                if (totCodeCount == null) {
                                    totCodeCount = new OrderedMap<Integer,AccumulatorLong>();
                                }
                                AccumulatorLong tal = totCodeCount.get(sc);
                                if (tal != null) {
                                    tal.add(al.get());
                                } else {
                                    totCodeCount.put(sc,new AccumulatorLong(al.get()));
                                }
                            }
                        }
                    }
                    this.rowData.add(fd);
                    lastFieldData = fd;

                    /* exit loop if summary */
                    if (this.isDetailReport) {
                        // -- Detail: advance to next day
                        dayIndex++;
                        if (dayIndex > dayEnd) {
                            // -- we've completed the day range
                            break;
                        }
                        DateTime dt = DateTime.getDateFromDayNumber(dayIndex,TZ).createDateTime();
                        timeStart = dt.getDayStart();
                        timeEnd   = (dayIndex < dayEnd)? dt.getDayEnd() : this.getTimeEnd();
                    } else {
                        // -- Summary: done, exit loop
                        break;
                    }

                } // for (;;)

                /* totals */
                if (this.isDetailReport) {
                    // -- Detail: display totals for end of Detail
                    this.totalData = new Vector<FieldData>();
                    FieldData fd = new FieldData();
                    fd.setAccount(this.getAccount());
                    fd.setString(FieldLayout.DATA_ACCOUNT_ID    , this.getAccountID());
                    fd.setDevice(this.device);
                    fd.setString(FieldLayout.DATA_DEVICE_ID     , devID);
                    fd.setDouble(FieldLayout.DATA_DISTANCE      , totOdomDeltaKM);
                    fd.setDouble(FieldLayout.DATA_ODOMETER_DELTA, totOdomDeltaKM);
                    fd.setDouble(FieldLayout.DATA_FUEL_TOTAL    , totFuelDeltaL);
                    fd.setDouble(FieldLayout.DATA_FUEL_TRIP     , totFuelDeltaL);
                    if (totCodeCount != null) {
                        for (Integer sc : totCodeCount.keySet()) {
                            int code = sc.intValue();
                            AccumulatorLong al = totCodeCount.get(sc);
                            String cKey = FieldLayout.DATA_STATUS_COUNT + "_" + StringTools.toHexString(code,16);
                            fd.setLong(cKey, al.get());
                        }
                    }
                    this.totalData.add(fd);
                }

            } catch (DBException dbe) {
                Print.logError("Error retrieving EventData for Device: " + devID);
            }

        } // loop through devices

        /* return row iterator */
        // TODO: sort by deviceID
        return new ListDataIterator(this.rowData);
        
    }

    /**
    *** Creates and returns an iterator for the row data displayed in the total rows of this report.
    *** @return The total row data iterator
    **/
    public DBDataIterator getTotalsDataIterator()
    {

        /* return total iterator */
        if (this.totalData != null) {
            return new ListDataIterator(this.totalData);
        } else {
            return null;
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    /**
    *** Custom DBRecordHandler callback
    *** @param rcd  The EventData record
    *** @return The returned status indicating whether to continue, or stop
    **/
    public int handleDBRecord(EventData rcd)
        throws DBException
    {
        EventData ev = rcd;

        /* first event */
        if (this.firstEvent == null) {
            //Print.logInfo("First Event at " + ev.getTimestamp());
            this.firstEvent = ev;
        }

        /* count status codes */
        Integer sci = new Integer(ev.getStatusCode());
        AccumulatorLong acc = this.codeCount.get(sci);
        if (acc != null) {
            acc.increment();
        } else {
            this.codeCount.put(sci, new AccumulatorLong(1L));
        }

        /* last event */
        //Print.logInfo("Saving Last Event at " + ev.getTimestamp());
        this.lastEvent = ev;

        /* return record limit status */
        return (this.rowData.size() < this.getReportLimit())? DBRH_SKIP : DBRH_STOP;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    protected long getReportStartTime()
    {
        long startTime = this.getTimeStart();
        return startTime;
    }

    protected long getReportEndTime()
    {
        long nowTime = DateTime.getCurrentTimeSec();
        long endTime = this.getTimeEnd();
        return (nowTime < endTime)? nowTime : endTime;
    }

    // ------------------------------------------------------------------------

}
