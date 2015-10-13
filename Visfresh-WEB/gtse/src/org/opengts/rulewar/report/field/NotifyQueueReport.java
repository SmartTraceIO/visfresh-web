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
//  2011/12/06  Martin D. Flynn
//     -Initial release
//  2013/02/07  Martin D. Flynn
//     -Added override "isSingleDeviceOnly()" to return true.
// ----------------------------------------------------------------------------
package org.opengts.rulewar.report.field;

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

import org.opengts.rule.tables.*;

public class NotifyQueueReport
    extends ReportData
{

    // ------------------------------------------------------------------------

    private I18N                        i18n                    = null;

    private java.util.List<FieldData>   rowData                 = null;

    // ------------------------------------------------------------------------

    /**
    *** Device Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public NotifyQueueReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        this.i18n = reqState.getPrivateLabel().getI18N(NotifyQueueReport.class);

        /* Account check */
        if (this.getAccount() == null) {
            throw new ReportException("Account-ID not specified");
        }

        /* Device check */
        int deviceCount = this.getDeviceCount();
        if (deviceCount <= 0) {
            throw new ReportException("No Devices specified");
        }

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
        return true;
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
        final Account account   = this.getAccount();
        final String  accountID = this.getAccountID();
        final long startTime    = this.getTimeStart();
        final long endTime      = this.getTimeEnd();

        /* init */
        this.rowData = new Vector<FieldData>();

        /* loop through devices */
        String deviceID = "";
        ReportDeviceList devList = this.getReportDeviceList();
        for (Iterator i = devList.iterator(); i.hasNext();) {

            /* get Device */
            deviceID = (String)i.next();
            final Device device;
            try {
                device = devList.getDevice(deviceID);
                if (device == null) {
                    Print.logError("Returned DeviceList 'Device' is null: " + deviceID);
                    continue;
                }
            } catch (DBException dbe) {
                Print.logError("Error retrieving EventData count for Device: " + deviceID);
                continue;
            }
            final String _accountID = device.getAccountID();
            final String _deviceID  = device.getDeviceID();

            /* record handler */
            DBRecordHandler<NotifyQueue> rcdHandler = new DBRecordHandler<NotifyQueue>() {
                public int handleDBRecord(NotifyQueue rcd) throws DBException {
                    if (!_accountID.equals(rcd.getAccountID())) {
                        // unlikely
                        Print.logWarn("NotifyQueue record AccountID does not match Device: " + rcd.getAccountID());
                        return DBRH_SKIP;
                    } else 
                    if (!_deviceID.equals(rcd.getDeviceID())) {
                        // unlikely
                        Print.logWarn("NotifyQueue record DeviceID does not match Device: " + rcd.getDeviceID());
                        return DBRH_SKIP;
                    } else
                    if ((startTime > 0L) && (rcd.getTimestamp() < startTime)) {
                        // unlikely
                        Print.logWarn("NotifyQueue record timestamp < start: " + rcd.getTimestamp());
                        return DBRH_SKIP;
                    } else 
                    if ((endTime > 0L) && (rcd.getTimestamp() > endTime)) {
                        // unlikely
                        Print.logWarn("NotifyQueue record timestamp > end: " + rcd.getTimestamp());
                        return DBRH_SKIP;
                    } 
                    FieldData fd = new FieldData();
                    fd.setAccount(account);
                    fd.setDevice(device);
                    fd.setLong(  FieldLayout.DATA_TIMESTAMP  , rcd.getTimestamp());
                    fd.setInt(   FieldLayout.DATA_STATUS_CODE, rcd.getStatusCode());
                    fd.setString(FieldLayout.DATA_RULE_ID    , rcd.getRuleID());
                    fd.setString(FieldLayout.DATA_MESSAGE_ID , rcd.getMessageID());
                    // TODO: add additional "NotifyQueue" fields
                    NotifyQueueReport.this.rowData.add(fd);
                    return DBRH_SKIP;
                }
            };

            /* record handler callback */
            try {
                NotifyQueue.getRecordCallback(
                    accountID, deviceID,
                    startTime, endTime,
                    rcdHandler);
            } catch (DBException dbe) {
                Print.logException("Error", dbe);
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
        return null;
    }

    // ------------------------------------------------------------------------

}
