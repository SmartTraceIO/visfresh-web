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
//import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.report.*;
import org.opengts.war.report.field.*;

public class TableReport
    extends ReportData
{

    // ------------------------------------------------------------------------
    // Properties

    private static final String PROP_tableName      = "tableName";

    // ------------------------------------------------------------------------

    public static class TableEntry {
        private String tableName = null;
        private String fldAcctID = null;
        private String orderBy[] = null;
        public TableEntry(String tableName, String fldAcctID, String... orderBy) {
            this.tableName = tableName;
            this.fldAcctID = fldAcctID;
            this.orderBy   = orderBy;
        }
        public String getTableName() {
            return this.tableName;
        }
        public DBFactory getTableFactory() {
            return DBFactory.getFactoryByName(this.getTableName());
        }
        public String getFieldAccountID() {
            return this.fldAcctID;
        }
        public String[] getOrderBy() {
            return this.orderBy;
        }
    }

    private static final TableEntry TableEntryList[] = {
        new TableEntry(Device.TABLE_NAME()     , Device.FLD_accountID, Device.FLD_deviceID),
        new TableEntry(DeviceGroup.TABLE_NAME(), DeviceGroup.FLD_accountID, DeviceGroup.FLD_groupID),
        new TableEntry(Driver.TABLE_NAME()     , Driver.FLD_accountID, Driver.FLD_driverID),
        new TableEntry(Geozone.TABLE_NAME()    , Geozone.FLD_accountID, Geozone.FLD_geozoneID, Geozone.FLD_sortID),
        new TableEntry(Role.TABLE_NAME()       , Role.FLD_accountID, Role.FLD_roleID),
        new TableEntry(StatusCode.TABLE_NAME() , StatusCode.FLD_accountID, Device.FLD_deviceID, StatusCode.FLD_statusCode),
        new TableEntry(User.TABLE_NAME()       , User.FLD_accountID, User.FLD_userID),
    };

    private static final Map<String,TableEntry> TableEntryMap = new HashMap<String,TableEntry>();
    static {
        for (TableEntry TE : TableEntryList) {
            TableEntryMap.put(TE.getTableName(),TE);
        }
    }

    public static TableEntry GetTableEntry(String tn)
    {
        return TableEntryMap.get(tn);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private I18N        i18n        = null;
    private String      tableName   = null;
    private TableEntry  tableEntry  = null;

    // ------------------------------------------------------------------------

    /**
    *** Table Report Constructor
    *** @param rptEntry The ReportEntry that generated this report
    *** @param reqState The session RequestProperties instance
    *** @param devList  The list of devices
    **/
    public TableReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        if (this.getAccount() == null) {
            throw new ReportException("Account-ID not specified");
        }
        this.i18n = reqState.getPrivateLabel().getI18N(TableReport.class);
    }

    // ------------------------------------------------------------------------

    /**
    *** Post report initialization
    **/
    public void postInitialize()
    {
        RTProperties rtp = this.getProperties();
        this.tableName = rtp.getString(PROP_tableName, "");
        if (StringTools.isBlank(this.tableName)) {
            Print.logError("'"+PROP_tableName+"' property not specified");
        } else {
            this.tableEntry = GetTableEntry(this.tableName); // null if table invalid
            if (this.tableEntry == null) {
                Print.logError("'"+PROP_tableName+"' property is invalid (table not supported)");
            }
        }
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

    // --------------------------------

    /**
    *** Returns true if the report time-start is required
    *** @return True if the report time-start is required
    **/
    @Override
    public boolean getRequiresTimeStart()
    {
        return false;
    }

    /**
    *** Returns true if the report time-end is required
    *** @return True if the report time-end is required
    **/
    @Override
    public boolean getRequiresTimeEnd()
    {
        return false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Creates and returns an iterator for the row data displayed in the body of this report.
    *** @return The body row data iterator
    **/
    @SuppressWarnings("unchecked")
    public DBDataIterator getBodyDataIterator()
    {

        /* init */
        final java.util.List<FieldData> rowData = new Vector<FieldData>();
        final String  accountID = this.getAccountID();
        final Account account   = this.getAccount();
        final User    user      = this.getUser();

        /* no valid table entry? */
        if (this.tableEntry == null) {
            return new ListDataIterator(rowData); // empty
        }
        DBFactory tblFact       = this.tableEntry.getTableFactory();
        String    FLD_accountID = this.tableEntry.getFieldAccountID();
        String    tblOrderBy[]  = this.tableEntry.getOrderBy();

        /* record handler */
        DBRecordHandler<DBRecord> rcdHandler = new DBRecordHandler<DBRecord>() {
            public int handleDBRecord(DBRecord rcd) throws DBException {
                // -- user authorized to device?
                if ((user != null) && (rcd instanceof Device) &&
                    !user.isAuthorizedDevice(((Device)rcd).getDeviceID())) {
                    // -- user not authorized to this Device
                    return DBRecordHandler.DBRH_SKIP;
                }
                // -- add record to list
                FieldData fd = new FieldData();
                fd.setAccount(account);
                fd.setDBRecord(FieldLayout.DATA_DBRECORD, rcd); // FieldLayout.DATA_DBRECORD                        
                if (rcd instanceof Device) {
                    fd.setDevice((Device)rcd);                  
                } else
                if (rcd instanceof Driver) {
                    fd.setDriver((Driver)rcd);
                }
                rowData.add(fd);
                return DBRecordHandler.DBRH_SKIP;
            }
        };

        /* DBSelect */
        // DBSelect: select * from TABLE_NAME where accountID="ACCOUNT" orderBy ORDER_BY;
        DBSelect  dbSel = new DBSelect(tblFact);
        DBWhere   dbWh  = dbSel.createDBWhere();
        dbSel.setWhere(dbWh.WHERE(dbWh.EQ(FLD_accountID,accountID)));
        dbSel.setOrderByFields(tblOrderBy);

        /* iterate through records */
        try {
            DBRecord._getRecords(dbSel, rcdHandler);
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

}
