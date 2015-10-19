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
//  2013/08/06  Martin D. Flynn
//     -Initial release
//  2013/11/11  Martin D. Flynn
//     -Changed to use new "Driver.getRecordCallback"
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

public class DriverReport
    extends ReportData
    implements DBRecordHandler<org.opengts.db.tables.Driver>
{

    // ------------------------------------------------------------------------
    // Properties

    private static final String PROP_licenseWillExpireDays  = "licenseWillExpireDays";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private I18N    i18n                    = null;
    private long    licenseWillExpireDays   = -1L;  // -1=all, 0=expired only, N=expired or will expire in N days

    // ------------------------------------------------------------------------

    /**
    *** Driver Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public DriverReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        if (this.getAccount() == null) {
            throw new ReportException("Account-ID not specified");
        }
        this.i18n = reqState.getPrivateLabel().getI18N(DriverReport.class);
    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {
        RTProperties rtp = this.getProperties();
        this.licenseWillExpireDays = rtp.getLong(PROP_licenseWillExpireDays, -1L);
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

    /**
    *** Creates and returns an iterator for the row data displayed in the body of this report.
    *** @return The body row data iterator
    **/
    public DBDataIterator getBodyDataIterator()
    {

        /* init */
        final java.util.List<FieldData> rowData = new Vector<FieldData>();

        /* pending expired drivers only? */
        final TimeZone tz = null;
        final long currDay = DateTime.getCurrentDayNumber(tz);
        final long expOffs = this.licenseWillExpireDays;

        /* record handler */
        DBRecordHandler<org.opengts.db.tables.Driver> rcdHandler = new DBRecordHandler<org.opengts.db.tables.Driver>() {
            public int handleDBRecord(org.opengts.db.tables.Driver rcd) throws DBException {
                org.opengts.db.tables.Driver driver = rcd;

                /* pending expired driver licenses only? */
                long licExp = driver.getLicenseExpire();
                if (expOffs >= 0L) {
                    // pending expiration check requested
                    if (licExp <= 0L) {
                        // this license does not expire, do not include in report
                        return DBRH_SKIP;
                    } else
                    if (licExp > (currDay + expOffs)) {
                        // this license will not expire within the specified day interval, do not include in report
                        Print.logInfo("Current Day   : " + currDay);
                        Print.logInfo("License Expire: " + licExp);
                        Print.logInfo("Offset Days   : " + expOffs);
                        return DBRH_SKIP;
                    }
                    // license is expired, or will expire within the specified day interval
                }

                /* save record */
                FieldData fd = new FieldData();
                fd.setAccount(DriverReport.this.getAccount());  // FieldLayout.DATA_ACCOUNT_ID
                fd.setDriver(driver);                           // FieldLayout.DATA_DRIVER_ID
                fd.setValue(FieldLayout.DATA_DRIVER_DESC        , driver.getDescription());
                fd.setValue(FieldLayout.DATA_DISPLAY_NAME       , driver.getDisplayName());
                fd.setValue(FieldLayout.DATA_DRIVER_NICKNAME    , driver.getDisplayName());
                // drv.getContactPhone()
                // drv.getContactEmail()
                fd.setValue(FieldLayout.DATA_DRIVER_LICENSE     , driver.getLicenseNumber());
                fd.setValue(FieldLayout.DATA_DRIVER_LICENSE_TYPE, driver.getLicenseType());
                fd.setValue(FieldLayout.DATA_DRIVER_LICENSE_EXP , driver.getLicenseExpire());
                fd.setValue(FieldLayout.DATA_DRIVER_BADGEID     , driver.getBadgeID());
                fd.setValue(FieldLayout.DATA_ADDRESS            , driver.getAddress());
                fd.setValue(FieldLayout.DATA_DRIVER_BIRTHDATE   , driver.getBirthdate());
                fd.setValue(FieldLayout.DATA_DRIVER_DEVICE_ID   , driver.getDeviceID());
                fd.setValue(FieldLayout.DATA_DEVICE_ID          , driver.getDeviceID());
                fd.setValue(FieldLayout.DATA_DRIVER_STATUS      , driver.getDriverStatus());
                rowData.add(fd);

                return DBRH_SKIP;
            }
        };

        /* iterate through records */
        try {
            Account acct = this.getAccount();
            org.opengts.db.tables.Driver.getRecordCallback(acct, rcdHandler);
        } catch (DBException dbe) {
            Print.logException("Error", dbe);
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
    // ------------------------------------------------------------------------

    /**
    *** Custom DBRecord callback handler class
    *** @param rcd  The EventData record
    *** @return The returned status indicating whether to continue, or stop
    **/
    public int handleDBRecord(org.opengts.db.tables.Driver rcd)
        throws DBException
    {
        org.opengts.db.tables.Driver drv = rcd;
        return DBRH_SKIP;
    }
    
    // ------------------------------------------------------------------------

}
