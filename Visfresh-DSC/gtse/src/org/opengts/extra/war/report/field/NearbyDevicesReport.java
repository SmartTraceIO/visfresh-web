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
//  2015/05/14  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.extra.war.report.field;

import java.io.*;
import java.util.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.report.*;
import org.opengts.war.report.field.*;

public class NearbyDevicesReport
    extends ReportData
{

    // ------------------------------------------------------------------------
    // Properties

    private static final String PROP_supportMapDisplay      = "supportMapDisplay";
    private static final String PROP_maximumDistanceMeters  = "maximumDistanceMeters";
    private static final String PROP_geozoneID              = Geozone.FLD_geozoneID;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private I18N                        i18n                    = null;
    private boolean                     supportMapDisplay       = true;
    private double                      maximumDistanceMeters   = 0.0;

    private String                      selGeozoneID            = null;
    private Geozone                     selGeozone              = null;

    private boolean                     activeOnly              = true;

    // ------------------------------------------------------------------------

    /**
    *** Motion Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public NearbyDevicesReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        if (this.getAccount() == null) {
            throw new ReportException("Account-ID not specified");
        }
        this.i18n = reqState.getPrivateLabel().getI18N(NearbyDevicesReport.class);
    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {

        /* properties */
        RTProperties rtp = this.getProperties();
        this.supportMapDisplay     = rtp.getBoolean(PROP_supportMapDisplay, this.supportMapDisplay);
        this.maximumDistanceMeters = rtp.getDouble(PROP_maximumDistanceMeters, this.maximumDistanceMeters);
        this.selGeozoneID          = rtp.getString(PROP_geozoneID,null);

        /* get specified Geozone */
        // -- a specified geozoneID means that this is a fleet/group report
        if (!StringTools.isBlank(this.selGeozoneID)) {
            Account acct = this.getAccount();
            try {
                Geozone gz[] = Geozone.getGeozone(acct, this.selGeozoneID);
                if (!ListTools.isEmpty(gz)) {
                    this.selGeozone = gz[0];
                } else {
                    Print.logError("Geozone not found: "+this.selGeozoneID);
                }
            } catch (DBException dbe) {
                Print.logError("Unable to read Geozone ["+this.selGeozoneID+"]: " + dbe);
            }
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this report handles only a single device at a time
    *** @return True If this report handles only a single device at a time
    **/
    public boolean isSingleDeviceOnly()
    {
        return StringTools.isBlank(this.selGeozoneID)? true : false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Override 'getEventData' to reset selected status codes
    *** @param device       The Device for which EventData records will be selected
    *** @param rcdHandler   The DBRecordHandler
    *** @return An array of EventData records for the device
    **/
    protected EventData[] getEventData(Device device, DBRecordHandler rcdHandler)
    {
        return new EventData[0];
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this report supports displaying a map
    *** @return True if this report supports displaying a map, false otherwise
    **/
    public boolean getSupportsMapDisplay() // true
    {
        return this.supportMapDisplay;
    }

    /** 
    *** Returns true if the map route-line is to be displayed, false otherwise.<br>
    *** This implementation overrides the superclass default and always returns false.
    *** @param isFleet  True if this maps represents a Group/Fleet of devices
    **/
    public boolean showMapRouteLine(boolean isFleet)
    {
        // -- always return false
        // -  This is a Device map, however the data contains Fleet pushpins.
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
    *** Custom DigitalInputDetail class
    **/
    private class NearbyDeviceDetail
        extends FieldData
        implements EventDataProvider
    {
        private Device.NearbyDevice nearbyDev = null;
        public NearbyDeviceDetail(Device.NearbyDevice nb) {
            this.nearbyDev = nb; // not null
            FieldData fd = this;
            fd.setString(  FieldLayout.DATA_ACCOUNT_ID , nb.getAccountID());
            fd.setString(  FieldLayout.DATA_DEVICE_ID  , nb.getDeviceID());
            fd.setString(  FieldLayout.DATA_DEVICE_DESC, nb.getDeviceDescription());
            fd.setGeoPoint(FieldLayout.DATA_GEOPOINT   , nb.getGeoPoint());
            fd.setDouble(  FieldLayout.DATA_SPEED      , nb.getSpeedKPH());
            fd.setDouble(  FieldLayout.DATA_HEADING    , nb.getHeading());
            fd.setLong(    FieldLayout.DATA_TIMESTAMP  , nb.getTimestamp());
            fd.setDouble(  FieldLayout.DATA_DISTANCE   , nb.getDistanceKM());
            fd.setString(  FieldLayout.DATA_ADDRESS    , nb.getAddress());
        }
        public long getTimestamp() {
            return this.nearbyDev.getTimestamp();
        }
        public int getStatusCode() {
            return this.nearbyDev.getStatusCode(); // STATUS_NONE
        }
        public boolean isValidGeoPoint() {
            return this.nearbyDev.hasGeoPoint();
        }
        public double getLatitude() {
            return this.nearbyDev.getLatitude();
        }
        public double getLongitude() {
            return this.nearbyDev.getLongitude();
        }
        public GeoPoint getGeoPoint() {
            return this.nearbyDev.getGeoPoint();
        }
        public long getGpsAge() {
            return this.nearbyDev.getGpsAge();
        }
        public long getCreationAge() {
            return 0L; // not available
        }
        public double getHorzAccuracy() {
            return -1.0; // not available
        }
        public GeoPoint getBestGeoPoint() {
            return this.getGeoPoint();
        }
        public double getBestAccuracy() {
            return this.getHorzAccuracy();
        }
        public int getSatelliteCount() {
            return 0; // not available
        }
        public double getBatteryLevel() {
            return 0.0; // not available
        }
        public double getSpeedKPH() {
            return this.nearbyDev.getSpeedKPH();
        }
        public double getHeading() {
            return this.nearbyDev.getHeading();
        }
        public double getAltitude() {
            return 0.0; // not available
        }
        public double getOdometerKM() {
            return 0.0;  // not available
        }
        public double getDistanceKM() {
            return this.nearbyDev.getDistanceKM();
        }
        public String getGeozoneID() {
            return "";  // not available
        }
        public String getAddress() {
            return StringTools.trim(this.nearbyDev.getAddress()); // must not return null
        }
        public long getInputMask() {
            return 0L;  // not available
        }
        public void setEventIndex(int ndx)
        {
            super.setInt(FieldLayout.DATA_EVENT_INDEX,ndx);
        }
        public int getEventIndex()
        {
            return super.getInt(FieldLayout.DATA_EVENT_INDEX,0);
        }
        public boolean getIsFirstEvent()
        {
            return true;
        }
        public void setIsLastEvent(boolean isLast) {
            super.setBoolean(FieldLayout.DATA_LAST_EVENT,isLast);
        }
        public boolean getIsLastEvent() {
            return super.getBoolean(FieldLayout.DATA_LAST_EVENT,true);
        }
        public String getStatusCodeDescription(BasicPrivateLabel bpl) {
            Device dev  = this.getDevice();
            int    code = this.getStatusCode();
            if (code == StatusCodes.STATUS_NONE) {
                return "";
            } else {
                return StatusCode.getDescription(dev, code, bpl, "");
            }
        }
        public StatusCodeProvider getStatusCodeProvider(BasicPrivateLabel bpl) {
            Device dev  = this.getDevice();
            int    code = this.getStatusCode();
            return StatusCode.getStatusCodeProvider(dev, code, bpl, null/*dftSCP*/);
        }
        public int getPushpinIconIndex(String iconSelector, OrderedSet<String> iconKeys, 
            boolean isFleet, BasicPrivateLabel bpl) {
            if (this.nearbyDev.isTargetDevice()) {
                return EventData._getPushpinIconIndex(EventData.PPNAME_last, iconKeys, EventData.ICON_PUSHPIN_BLUE);
            } else
            if (this.getSpeedKPH() > 0.0) {
                return EventData.ICON_PUSHPIN_GREEN;
            } else {
                return EventData.ICON_PUSHPIN_RED;
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Creates and returns an iterator for the row data displayed in the body of this report.
    *** @return The body row data iterator
    **/
    public DBDataIterator getBodyDataIterator()
    {

        /* init */
        final Vector<NearbyDeviceDetail> rowData = new Vector<NearbyDeviceDetail>();

        /* do we have a "geozoneID"? */
        boolean  useGeozoneGP = !StringTools.isBlank(this.selGeozoneID)? true : false;
        GeoPoint geozoneGP    = null;
        if (useGeozoneGP) {
            geozoneGP = (this.selGeozone != null)? this.selGeozone.getCenterGeoPoint() : null;
            if ((geozoneGP == null) || !geozoneGP.isValid()) {
                // -- Geozone center GP required, but invalid 
                return new ListDataIterator(rowData);
            }
        }

        /* list of devices */
        ReportDeviceList devList   = this.getReportDeviceList();
        Account          account   = this.getAccount();
        User             user      = this.getUser();
        long             startTime = this.getTimeStart(); // -1L; 
        long             endTime   = this.getTimeEnd();   // -1L;
        double           radiusM   = this.maximumDistanceMeters; // may be zero
        boolean          actvOnly  = this.activeOnly;

        /* loop through devices (1 for Device, >=1 for group) */
        try {
            if (useGeozoneGP) {
                // -- Fleet report:
                // -    - "useGeozoneGP" must be true
                // -    - "this.maximumDistanceMeters" is optional
                // -    - Geozone must have a valid center GeoPoint
                // -    - Displayed nearby devices filtered by specified group
                // -- target location
                GeoPoint targetGP = geozoneGP; // guaranteed valid (verified above)
                // -- get nearby devices
                //Print.logInfo("Finding nearby devices for Geozone '"+this.selGeozoneID+"' rad="+radiusM+" gp="+targetGP);
                Map<String,Device.NearbyDevice> nbMap = Device.GetNearbyDeviceMap(
                    account, null/*targetDev*/, true/*inclTargID*/,
                    startTime, endTime,
                    targetGP, radiusM, actvOnly, user, true/*sortByDistance*/);
                // -- add nearby devices to row-data
                if (ListTools.size(nbMap) > 0) {
                    for (String nbDevID : nbMap.keySet()) {
                        if (devList.containsDevice(nbDevID)) {
                            Device.NearbyDevice nb = nbMap.get(nbDevID);
                            // -- create report row
                            NearbyDeviceDetail fd = new NearbyDeviceDetail(nb);
                            rowData.add(fd);
                        } else {
                            //Print.logInfo("Device list does not contain '"+nbDevID+"'");
                        }
                    }
                }
            } else {
                // -- (Device report: 
                // -    - "useGeozoneGP" must be false
                // -    - "this.maximumDistanceMeters" must be > 0
                // -    - Device must have a valid last GeoPoint
                // -    - Displayed nearby devices default to group "ALL"
                for (Iterator i = devList.iterator(); i.hasNext();) {
                    // -- get Device
                    String devID  = (String)i.next();
                    Device device = devList.getDevice(devID);
                    if (device == null) {
                        // -- should never occur
                        Print.logError("Returned DeviceList 'Device' is null: " + devID);
                        continue;
                    }
                    // -- target location
                    GeoPoint targetGP = device.getLastValidLocation();
                    if (!GeoPoint.isValid(targetGP)) {
                        // -- unknown target location
                        continue;
                    }
                    // -- get nearby devices
                    //Print.logInfo("Finding nearby devices for '"+devID+"', radius="+radiusM + " meters");
                  //Map<String,Device.NearbyDevice> nbMap = device.getNearbyDevices(radiusM, actvOnly, user);
                    Map<String,Device.NearbyDevice> nbMap = Device.GetNearbyDeviceMap(
                        account, devID/*targetDev*/, true/*inclTargID*/,
                        startTime, endTime,
                        targetGP, radiusM, actvOnly, user, true/*sortByDistance*/);
                    // -- add nearby devices to row-data
                    if (ListTools.size(nbMap) > 0) {
                        for (String nbDevID : nbMap.keySet()) {
                            Device.NearbyDevice nb = nbMap.get(nbDevID);
                            // -- create report row
                            NearbyDeviceDetail fd = new NearbyDeviceDetail(nb);
                            if (nb.isTargetDevice()) {
                                rowData.add(0, fd); // -- make target device first element
                            } else {
                                rowData.add(fd);
                            }
                        }
                    }
                }
            }
        } catch (DBException dbe) {
            Print.logError("Error reading nearby Devices");
        }

        /* return row iterator */
        return new ListDataIterator(rowData);

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
