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
//  2011/03/08  Martin D. Flynn
//     -Added Geozone 'visit' count to GeozoneTotal
//  2011/12/06  Martin D. Flynn
//     -Added "estimateDepartTime" property for setting last depart-time to
//      report-time if vehicle is still inside a geozone.
//  2015/09/15  Martin D. Flynn
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

public class GeozoneReport
    extends ReportData
    implements DBRecordHandler<EventData>
{

    // ------------------------------------------------------------------------
    // Properties

    private static final String PROP_geozoneID              = Geozone.FLD_geozoneID;
    private static final String PROP_geozoneCategory        = "geozoneCategory";    // ZoneArrive|JobArrive
    private static final String PROP_totalByGeozone         = "totalByGeozone";     // true|false
    private static final String PROP_estimateDepartTime     = "estimateDepartTime"; // true|false
    private static final String PROP_singleDeviceOnly       = "singleDeviceOnly";   // true|false

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // global defined vars (per report instance)

    private boolean                     totalsByGeozone     = false;

    private boolean                     isJobCategory       = true;
    private String                      zoneCategory        = StatusCodes.CAT_ZoneArrive;
    private int                         zoneCodes[]         = null;

    private String                      selGeozoneID        = null;
    private Geozone                     selGeozone          = null;

    private I18N                        i18n                = null;
    private String                      unknownText         = null;

    private boolean                     singleDeviceOnly    = true;

    private boolean                     estimateDepartTime  = false;

    // -- per device vars below (these need to be initialized in "getBodyDataIterator")

    private EventData                   firstEvent          = null;

    private long                        arriveTime          = 0L;
    private EventData                   arriveEvent         = null;
    private EventData                   departEvent         = null;
    private long                        lastDepartTime      = 0L;

    private GeozoneDetail               lastGeozoneDetail   = null;
    
    private Vector<FieldData>           rowData             = null;

    // ------------------------------------------------------------------------

    /**
    *** Geozone Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public GeozoneReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        this.i18n = reqState.getPrivateLabel().getI18N(GeozoneReport.class);
        this.unknownText = this.i18n.getString("GeozoneReport.unknownZone","(unknown)");

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
    *** Returns true if this report handles only a single device at a time
    *** @return True If this report handles only a single device at a time
    **/
    public boolean isSingleDeviceOnly()
    {
        return this.singleDeviceOnly;
    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {

        /* properties */
        RTProperties rptRTP     = this.getProperties();
        this.singleDeviceOnly   = rptRTP.getBoolean(PROP_singleDeviceOnly, true);
        this.estimateDepartTime = rptRTP.getBoolean(PROP_estimateDepartTime, false);
        this.totalsByGeozone    = rptRTP.getBoolean(PROP_totalByGeozone, false);
        this.zoneCategory       = rptRTP.getString(PROP_geozoneCategory, StatusCodes.CAT_ZoneArrive);
        this.isJobCategory      = StatusCodes.IsPairedStatusCodesJobCategory(this.zoneCategory);
        this.selGeozoneID       = rptRTP.getString(PROP_geozoneID,null);

        /* alias for "All Geozones" */
        if ((this.selGeozoneID != null) && this.selGeozoneID.equalsIgnoreCase("ALL")) {
            this.selGeozoneID = null;
        }

        /* zone delimiter status codes */
        this.zoneCodes = StatusCodes.GetPairedStatusCodesPair(this.zoneCategory);
        if (ListTools.size(this.zoneCodes) < 2) {
            this.zoneCategory = StatusCodes.CAT_ZoneArrive;
            this.zoneCodes    = StatusCodes.GetPairedStatusCodesPair(this.zoneCategory);
        }

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
        ReportConstraints rc = this.getReportConstraints();
        rc.setStatusCodes(this.zoneCodes);
        rc.setValidGPSRequired(false); // don't need just valid gps events

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

        /* init report body data */
        Vector<FieldData> bodyData = new Vector<FieldData>(); // [2.6.0-B82]

        /* loop through devices (should be only 1) */
        String accountID = this.getAccountID();
        String devID     = "";
        ReportDeviceList devList = this.getReportDeviceList();
        for (Iterator i = devList.iterator(); i.hasNext();) {
            devID = (String)i.next();

            /* init new Device vars */
            this.firstEvent         = null;
            this.arriveTime         = 0L;
            this.arriveEvent        = null;
            this.departEvent        = null;
            this.lastDepartTime     = 0L;
            this.lastGeozoneDetail  = null;
            this.rowData            = new Vector<FieldData>();

            /* get device and analyze events */
            try {
                Device device = devList.getDevice(devID);
                this.getEventData_Device(device, this); // <== callback to 'handleDBRecord'
            } catch (DBException dbe) {
                Print.logError("Error retrieving EventData for Device: " + devID);
            }

            /* handle final record (if any) */
            if (this.arriveEvent != null) {
                // -- still in zone
                GeozoneDetail gzd = new GeozoneDetail(this.arriveTime, this.arriveEvent, null, this.i18n);
                this.rowData.add(gzd);
                this.lastDepartTime = 0L;
                this.arriveEvent    = null;
                this.arriveTime     = 0L;
            }

            /* first event is Departure? */
            // -- If the first event was a 'Departure', then search for the previous 'Arrival' event
            // -  and fixup the first Geozone accumulator information
            if ((this.rowData.size() > 0) && (this.firstEvent != null) && 
                (this.firstEvent.getStatusCode() == this.zoneCodes[1])) {
                // -- get arrival event prior to this first received departure
                Print.logInfo("First event was a departure, searching for prior arrival ...");
                try {
                    EventData arrEv = this.firstEvent.getPreviousEventData(
                        new int[] { this.zoneCodes[0] },
                        false/*validGPS*/);
                    if (arrEv != null) {
                        // -- update first Geozone accumulator record with actual arrival information
                        Print.logInfo("... Found prior arrival.");
                        GeozoneDetail gzd = (GeozoneDetail)this.rowData.get(0);
                        gzd.fixupArrivalEvent(arrEv);
                    } else {
                        Print.logInfo("... Prior arrival not found!");
                    }
                } catch (DBException dbe) {
                    Print.logException("Error retreiving EventData record",dbe);
                }
            }

            /* totals by Geozone? */
            if (this.totalsByGeozone) {
    
                /* sort by geozoneID (must be a stable sort) */
                ListTools.sort(this.rowData,null); // StringComparator
        
                /* find the GeozoneID breaks */
                String  lastZoneTypeID    = null;
                boolean stillInZone       = false;
                long totalDwellElapsedSec = 0L;
                long totalDriveElapsedSec = 0L;
                long totalVisitCount      = 0L;
                for (int ri = 0; ri < this.rowData.size(); ri++) {
    
                    /* new record */
                    GeozoneDetail gzd = (GeozoneDetail)this.rowData.get(ri);
    
                    /* zone break? (reset accumulators) */
                    if (lastZoneTypeID == null) {
                        // -- first geozone
                        // -  reset accumulators
                        lastZoneTypeID       = gzd.getZoneTypeID();
                        stillInZone          = false;
                        totalDwellElapsedSec = 0L;
                        totalDriveElapsedSec = 0L;
                        totalVisitCount      = 0L;
                    } else
                    if (!lastZoneTypeID.equals(gzd.getZoneTypeID())) {
                        // -- geozone break
                        // -  save totals for prior zone
                        GeozoneTotal gzt = new GeozoneTotal(lastZoneTypeID, 
                            totalVisitCount, stillInZone, 
                            totalDwellElapsedSec, totalDriveElapsedSec, this.i18n);
                        this.rowData.insertElementAt(gzt,ri++); // insert total, increment record pointer also
                        // -  reset accumulators
                        lastZoneTypeID       = gzd.getZoneTypeID();
                        stillInZone          = false;
                        totalDwellElapsedSec = 0L;
                        totalDriveElapsedSec = 0L;
                        totalVisitCount      = 0L;
                    }
    
                    /* still in zone */
                    stillInZone |= gzd.stillInZone();
    
                    /* count this Geozone visit */
                    totalVisitCount++;
    
                    /* inside zone accumulator */
                    long insideZoneSec = gzd.getZoneElapseSec();
                    if (insideZoneSec < 0L) {
                        // -- missing either the arrive or depart time
                        totalDwellElapsedSec = -1L; // skip accumulator, since it would be underestimated
                    } else
                    if ((totalDwellElapsedSec >= 0L) && (insideZoneSec >= 0L)) {
                        totalDwellElapsedSec += insideZoneSec;
                    }
    
                    /* outside zone accoumulator */
                    long outsideZoneSec = gzd.getDriveElapseSec();
                    if (outsideZoneSec < 0L) {
                        // -- missing arrive time, or last depart time
                        totalDriveElapsedSec = -1L; // skip accumulator
                    } else
                    if ((totalDriveElapsedSec >= 0L) && (outsideZoneSec >= 0L)) {
                        totalDriveElapsedSec += outsideZoneSec;
                    }
    
                } // 'rowData' iterator
    
                /* final total record */
                if (lastZoneTypeID != null) {
                    GeozoneTotal gzt = new GeozoneTotal(lastZoneTypeID, 
                        totalVisitCount, stillInZone, 
                        totalDwellElapsedSec, totalDriveElapsedSec, 
                        this.i18n);
                    this.rowData.insertElementAt(gzt,this.rowData.size());
                }
                
            }

            /* add this Device row data to report body data */
            bodyData.addAll(this.rowData); // [2.6.0-B82]
            this.rowData.clear();
            this.rowData = null;

        } // iterate through devices
    
        /* return row iterator */
        return new ListDataIterator(bodyData); // [2.6.0-B82]
        
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

    /**
    *** Custom DBRecord callback handler class
    *** @param rcd  The EventData record
    *** @return The returned status indicating whether to continue, or stop
    **/
    public int handleDBRecord(EventData rcd)
        throws DBException
    {
        EventData ev = rcd;
        if (ev == null) {
            throw new DBException("EventData record is null!!");
        }

        /* report initialized? */
        if (this.rowData == null) {
            throw new DBException("Report has not been properly initialized! (rowData)");
        } else
        if (this.zoneCodes == null) {
            throw new DBException("Report has not been properly initialized! (zoneCodes)");
        }

        /* selected GeozoneID? */
        if (!StringTools.isBlank(this.selGeozoneID)) {
            String gzid = ev.getGeozoneID();
            if (!this.selGeozoneID.equalsIgnoreCase(gzid)) {
                // -- skip this event (not in Geozone)
                return DBRH_SKIP;
            }
        }

        /* status code */
        int code = ev.getStatusCode();

        /* first event */
        if (this.firstEvent == null) {
            if (code == this.zoneCodes[1]) { // Depart
                // we started out inside a zone, start at report start time
                this.arriveTime = this.getTimeStart();
            } else
            if (code == this.zoneCodes[0]) { // Arrive
                this.lastDepartTime = 0L; // we don't know the last depart time
            }
            this.firstEvent = ev;
        }

        /* geozone/job arrive */
        if (code == this.zoneCodes[0]) {

            /* save arrival event */
            this.arriveEvent = ev;
            this.arriveTime  = ev.getTimestamp();

            /* fixup prior GeozoneDetail with 'outside' time */
            if (this.lastGeozoneDetail != null) {
                long driveElapseSec = 
                    ((this.lastDepartTime > 0L) && (this.arriveTime > this.lastDepartTime))? 
                    (this.arriveTime - this.lastDepartTime) : -1L;
                this.lastGeozoneDetail.setDriveElapseSec(driveElapseSec);
                this.lastGeozoneDetail = null;
            }

        } else
        if (code == this.zoneCodes[1]) {

            /* save (explicit) departure event */
            this.lastGeozoneDetail = new GeozoneDetail(this.arriveTime, this.arriveEvent, ev, this.i18n);
            this.rowData.add(this.lastGeozoneDetail);
            this.departEvent    = ev;
            this.lastDepartTime = ev.getTimestamp();

            /* clear arrival event */
            this.arriveEvent    = null;
            this.arriveTime     = 0L;

        }

        /* return record limit status */
        return (this.rowData.size() < this.getReportLimit())? DBRH_SKIP : DBRH_STOP;

    }

    protected long getReportEndTime()
    {
        long nowTime = DateTime.getCurrentTimeSec();
        long endTime = this.getTimeEnd();
        return (nowTime < endTime)? nowTime : endTime;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
        
    private static ColumnValue getColumnValue(Object val) {
        if (val == null) {
            return new ColumnValue("");
        } else
        if (val instanceof ColumnValue) {
            return (ColumnValue)val;
        } else {
            return new ColumnValue(val.toString());
        }
    }

    /**
    *** Custom GeozoneDetail class
    **/
    private class GeozoneDetail
        extends FieldData
    {

        private boolean  arriveApprox     = false;
        private long     arriveTime       = 0L;
        private double   arriveOdomKM     = 0.0;
        private double   arriveFuelTot    = 0.0;
        private double   arriveEngHours   = 0.0;

        private boolean  departApprox     = false;
        private long     departTime       = 0L;
        private double   departOdomKM     = 0.0;
        private double   departFuelTot    = 0.0;
        private double   departEngHours   = 0.0;

        private String   geozoneID        = null;
        private GeoPoint geozoneGPS       = null;
        private String   geozoneAddr      = null;

        private String   corridorID       = null;

        private long     zoneElapseSec    = -1L;
        private long     driveElapseSec   = -1L;
        private double   zoneFuelDelta    = -1.0;
        private double   zoneOdomDelta    = -1.0;
        private double   zoneHoursDelta   = -1.0;

        public GeozoneDetail(long arrTime, EventData arrEv, EventData depEv, I18N i18n) {
            super();
            EventData ev = (arrEv != null)? arrEv : depEv;
            // 'depEv' may be null for the last row in the data iterator

            /* Account/Device */
            this.setAccount(ev.getAccount());
            this.setDevice( ev.getDevice());
            Device device = ev.getDevice();

            /* GeozoneID/Address */
            this.geozoneID      = GeozoneReport.this.isJobCategory? ev.getJobNumber() : ev.getGeozoneID();
            this.geozoneGPS     = ev.getGeoPoint();
            this.geozoneAddr    = ev.getAddress();

            /* GeoCorridor ID */
            this.corridorID     = null; // TODO: for now, delegate to FieldLayout DATA_GEOCORRIDOR_ID

            /* arrive times */
            this.arriveApprox   = (arrEv == null); // may not be an accurate arrive time
            this.arriveTime     = (arrEv != null)? arrEv.getTimestamp()  : arrTime;
            this.arriveOdomKM   = (arrEv != null)? arrEv.getOdometerKM() : 0.0;
            this.arriveFuelTot  = (arrEv != null)? arrEv.getFuelTotal()  : 0.0;
            this.arriveEngHours = (arrEv != null)? arrEv.getEngineHours(): 0.0;

            /* depart times */
            this.departApprox   = (depEv == null); // may not be an accurate depart time
            this.departTime     = (depEv != null)? depEv.getTimestamp() : GeozoneReport.this.getReportEndTime();
            this.departOdomKM   = (depEv != null)? depEv.getOdometerKM() : 0.0;
            this.departFuelTot  = (depEv != null)? depEv.getFuelTotal()  : 0.0;
            this.departEngHours = (depEv != null)? depEv.getEngineHours(): 0.0;

            /* elapsed time */
            if ((this.departTime > 0L) && (this.arriveTime > 0L)) {
                this.zoneElapseSec = this.departTime - this.arriveTime;
            } else {
                this.zoneElapseSec = -1L;
            }

            /* delta odometer */
            if ((this.departOdomKM > 0.0) && (this.arriveOdomKM > 0.0)) {
                this.zoneOdomDelta = this.departOdomKM - this.arriveOdomKM;
            } else {
                this.zoneOdomDelta = -1.0;
            }

            /* delta engine hours */
            if ((this.departEngHours > 0.0) && (this.arriveEngHours > 0.0)) {
                this.zoneHoursDelta = this.departEngHours - this.arriveEngHours;
            } else {
                this.zoneHoursDelta = -1.0;
            }

            /* delta fuel */
            if ((this.departFuelTot > 0.0) && (this.arriveFuelTot > 0.0)) {
                this.zoneFuelDelta = this.departFuelTot - this.arriveFuelTot;
            } else
            if ((this.zoneHoursDelta > 0.0) && (device != null) && (device.getFuelRatePerHour() > 0.0)) {
                this.zoneFuelDelta = this.zoneHoursDelta * device.getFuelRatePerHour(); // H * L/H
            } else
            if ((this.zoneOdomDelta > 0.0) && (device != null) && (device.getFuelEconomy() > 0.0)) {
                this.zoneFuelDelta = this.zoneOdomDelta / device.getFuelEconomy(); // Km / km/L
            } else {
                this.zoneFuelDelta = -1.0;
            }

            /* Geozone elapsed time */
            this.setValue(FieldLayout.DATA_GEOZONE_ID       , this.geozoneID);
            this.setValue(FieldLayout.DATA_GEOPOINT         , this.geozoneGPS);
            this.setValue(FieldLayout.DATA_ADDRESS          , this.geozoneAddr);
            this.setValue(FieldLayout.DATA_GEOCORRIDOR_ID   , this.corridorID);
            // - enter/start
            this.setValue(FieldLayout.DATA_ENTER_GEOZONE_ID , this.geozoneID);
            this.setValue(FieldLayout.DATA_ENTER_TIMESTAMP  , this.arriveTime);
            this.setValue(FieldLayout.DATA_ENTER_FUEL       , this.arriveFuelTot);
            this.setValue(FieldLayout.DATA_START_FUEL       , this.arriveFuelTot);
            this.setValue(FieldLayout.DATA_ENTER_ODOMETER   , this.arriveOdomKM);
            this.setValue(FieldLayout.DATA_START_ODOMETER   , this.arriveOdomKM);
            this.setValue(FieldLayout.DATA_ENTER_HOURS      , this.arriveEngHours);
            this.setValue(FieldLayout.DATA_START_HOURS      , this.arriveEngHours);
            this.setValue(FieldLayout.DATA_ENTER_EVENT      , arrEv);
            this.setValue(FieldLayout.DATA_START_EVENT      , arrEv);
          //this.setValue(FieldLayout.DATA_EVENTDATA        , arrEv);
            // - exit/stop
            this.setValue(FieldLayout.DATA_EXIT_GEOZONE_ID  , this.geozoneID);
            this.setValue(FieldLayout.DATA_EXIT_TIMESTAMP   , (!this.departApprox || GeozoneReport.this.estimateDepartTime)? this.departTime : 0L);
            this.setValue(FieldLayout.DATA_EXIT_FUEL        , this.departFuelTot);
            this.setValue(FieldLayout.DATA_STOP_FUEL        , this.departFuelTot);
            this.setValue(FieldLayout.DATA_EXIT_ODOMETER    , this.departOdomKM);
            this.setValue(FieldLayout.DATA_STOP_ODOMETER    , this.departOdomKM);
            this.setValue(FieldLayout.DATA_EXIT_HOURS       , this.departEngHours);
            this.setValue(FieldLayout.DATA_STOP_HOURS       , this.departEngHours);
            this.setValue(FieldLayout.DATA_EXIT_EVENT       , depEv);
            this.setValue(FieldLayout.DATA_STOP_EVENT       , depEv);
            // - delta
            this.setValue(FieldLayout.DATA_ELAPSE_SEC       , this.zoneElapseSec);
            this.setValue(FieldLayout.DATA_INSIDE_ELAPSED   , this.zoneElapseSec);
            this.setValue(FieldLayout.DATA_FUEL_DELTA       , this.zoneFuelDelta);
            this.setValue(FieldLayout.DATA_ODOMETER_DELTA   , this.zoneOdomDelta);
            this.setValue(FieldLayout.DATA_HOURS_DELTA      , this.zoneHoursDelta);
            this.setValue(FieldLayout.DATA_COUNT            , -1L);

        }

        public String getGeozoneID() {
            return this.geozoneID;
        }

        public String getZoneTypeID() {
            return this.geozoneID; // "geozoneID" or "jobID"
        }

        public void fixupArrivalEvent(EventData arrEv) {
            if (arrEv != null) {
                /* GeozoneID/Address */
                this.geozoneID      = arrEv.getGeozoneID();
                this.geozoneGPS     = arrEv.getGeoPoint();
                this.geozoneAddr    = arrEv.getAddress();
                this.arriveTime     = arrEv.getTimestamp();
                this.corridorID     = null;
                this.zoneElapseSec  = ((this.departTime > 0L) && (this.arriveTime > 0L))? 
                    (this.departTime - this.arriveTime) : 
                    -1L;
                this.setValue(FieldLayout.DATA_GEOZONE_ID       , this.geozoneID);
                this.setValue(FieldLayout.DATA_ENTER_GEOZONE_ID , this.geozoneID);
                this.setValue(FieldLayout.DATA_ENTER_TIMESTAMP  , this.arriveTime);
                this.setValue(FieldLayout.DATA_GEOPOINT         , this.geozoneGPS);
                this.setValue(FieldLayout.DATA_ADDRESS          , this.geozoneAddr);
                this.setValue(FieldLayout.DATA_GEOCORRIDOR_ID   , this.corridorID);
                this.setValue(FieldLayout.DATA_ELAPSE_SEC       , this.zoneElapseSec);
                this.setValue(FieldLayout.DATA_INSIDE_ELAPSED   , this.zoneElapseSec);
            }
        }

        public void setDriveElapseSec(long elapseSec) {
            this.driveElapseSec = elapseSec;
            this.setValue(FieldLayout.DATA_DRIVING_ELAPSED  , this.driveElapseSec);
            this.setValue(FieldLayout.DATA_OUTSIDE_ELAPSED  , this.driveElapseSec);
        }

        public long getDriveElapseSec() {
            return this.driveElapseSec;
        }

        public long getZoneElapseSec() {
            return this.zoneElapseSec;
        }

        public String toString() {
            return this.getZoneTypeID();
        }

        public boolean stillInZone() {
            return this.departApprox;
        }

        public Object filterReturnedValue(String key, Object rtnVal) {
            if (rtnVal == null) {
                return "";
            } else
            if (key == null) {
                return rtnVal;
            } else
            if (key.equals(FieldLayout.DATA_GEOZONE_ID)) {
                if (StringTools.isBlank(rtnVal.toString())) {
                    return GeozoneReport.getColumnValue(rtnVal).setValue(GeozoneReport.this.unknownText);
                } else {
                    return rtnVal;
                }
            } else
            if (key.equals(FieldLayout.DATA_ENTER_TIMESTAMP) || 
                key.equals(FieldLayout.DATA_ENTER_DATETIME )   )  {
                if (this.arriveApprox) {
                    return GeozoneReport.getColumnValue(rtnVal).setForegroundColor(ColorTools.DARK_YELLOW);
                } else {
                    return rtnVal;
                }
            } else
            if (key.equals(FieldLayout.DATA_ELAPSE_SEC    ) || 
                key.equals(FieldLayout.DATA_INSIDE_ELAPSED)   )  {
                if (this.departApprox) {
                    return GeozoneReport.getColumnValue(rtnVal).setForegroundColor(ColorTools.DARK_YELLOW);
                } else {
                    return rtnVal;
                }
            } else
            if (key.equals(FieldLayout.DATA_EXIT_TIMESTAMP) || 
                key.equals(FieldLayout.DATA_EXIT_DATETIME )   )  {
                if (this.departApprox) {
                    return GeozoneReport.getColumnValue(rtnVal).setForegroundColor(ColorTools.DARK_YELLOW);
                } else {
                    return rtnVal;
                }
            } else {
                return rtnVal;
            }
        }

    }

    /**
    *** Custom GeozoneTotal class
    **/
    private static class GeozoneTotal
        extends FieldData
    {

        private boolean stillInZone = false;

        public GeozoneTotal(String zoneTypeID, 
            long totalVisitCount, boolean stillInZone,
            long totalDwellElapsed, long totalDriveElapsed, I18N i18n) {
            this.stillInZone = stillInZone;
            this.setCssClass(ReportLayout.CSS_CLASS_BODY_TOTAL);
            String totalText = stillInZone?
                i18n.getString("GeozoneReport.stillInZone","Still in Geozone") :
                i18n.getString("GeozoneReport.timeInZone" ,"Total Time in Geozone");
            this.setValue(FieldLayout.DATA_GEOZONE_ID     , zoneTypeID);
            this.setValue(FieldLayout.DATA_ADDRESS        , totalText);
            this.setValue(FieldLayout.DATA_ELAPSE_SEC     , totalDwellElapsed);
            this.setValue(FieldLayout.DATA_INSIDE_ELAPSED , totalDwellElapsed);
            this.setValue(FieldLayout.DATA_DRIVING_ELAPSED, totalDriveElapsed);
            this.setValue(FieldLayout.DATA_OUTSIDE_ELAPSED, totalDriveElapsed);
            this.setValue(FieldLayout.DATA_COUNT          , totalVisitCount);
        }

        public Object filterReturnedValue(String key, Object rtnVal) {
            if (key == null) {
                return rtnVal;
            } else
            if (rtnVal == null) {
                return "";
            } else
            if (key.equals(FieldLayout.DATA_ELAPSE_SEC    ) || 
                key.equals(FieldLayout.DATA_INSIDE_ELAPSED)   )  {
                if (this.stillInZone) {
                    return GeozoneReport.getColumnValue(rtnVal).setForegroundColor(ColorTools.DARK_YELLOW);
                } else {
                    return rtnVal;
                }
            } else {
                return rtnVal;
            }
        }

    }

}
