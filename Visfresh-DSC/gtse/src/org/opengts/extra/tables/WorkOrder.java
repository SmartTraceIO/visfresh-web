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
//  2010/09/09  Martin D. Flynn
//     -Initial release
//  2014/11/30  Martin D. Flynn
//     -Removed Zone/Sampling/Shapes
// ----------------------------------------------------------------------------
package org.opengts.extra.tables;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.extra.shapefile.*;

import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

public class WorkOrder
    extends AccountRecord<WorkOrder>
{

    // ------------------------------------------------------------------------
    
    public static final String TYPE_HIER_KEY            = HierarchyRecord.TYPE_HIER_KEY;
    public static final String TYPE_ITEM_ID             = HierarchyRecord.TYPE_ITEM_ID;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Work type [FLD_workType]
    // 'sampling','chemical','fertilizer','aerial_photo','veriss', and 'bound_collect'

    /*
    public enum WorkType implements EnumTools.StringLocale, EnumTools.IntValue {
        NONE          (  0, "none"         , I18N.getString(WorkOrder.class,"WorkOrder.workType.none"         , "None"         )), // default
        // ---
        private int         vv = 0;
        private String      nn = null;
        private I18N.Text   aa = null;
        WorkType(int v, String n, I18N.Text a)      { vv = v; nn = n; aa = a; }
        public int     getIntValue()                { return vv; }
        public String  getName()                    { return nn; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isDefault()                  { return this.equals(NONE); }
        public boolean isType(int type)             { return this.getIntValue() == type; }
    };
    */

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Work status [FLD_workStatus]

    public enum WorkStatus implements EnumTools.StringLocale, EnumTools.IntValue {
        PENDING     (  0, I18N.getString(WorkOrder.class,"WorkOrder.workType.pending"   ,"Pending"    )), // default
        ON_HOLD     (  1, I18N.getString(WorkOrder.class,"WorkOrder.workType.onHold"    ,"On Hold"    )),
        IN_PROGRESS ( 10, I18N.getString(WorkOrder.class,"WorkOrder.workType.inProgress","In Progress")),
        CANCELLED   ( 80, I18N.getString(WorkOrder.class,"WorkOrder.workType.cancelled" ,"Cancelled"  )),
        COMPLETED   ( 99, I18N.getString(WorkOrder.class,"WorkOrder.workType.completed" ,"Completed"  ));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        WorkStatus(int v, I18N.Text a)              { vv = v; aa = a; }
        public int     getIntValue()                { return vv; }
        public String  toString()                   { return aa.toString(); }
        public String  toString(Locale loc)         { return aa.toString(loc); }
        public boolean isDefault()                  { return this.equals(PENDING); }
        public boolean isType(int type)             { return this.getIntValue() == type; }
    };

    /**
    *** Returns the defined WorkStatus for the specified WorkOrder.
    *** @param wo  The WorkOrder from which the WorkStatus will be obtained.  
    ***            If null, the default WorkStatus will be returned.
    *** @return The WorkStatus
    **/
    public static WorkStatus getWorkStatus(WorkOrder wo)
    {
        return (wo != null)? 
            EnumTools.getValueOf(WorkStatus.class,wo.getWorkStatus()) : 
            EnumTools.getDefault(WorkStatus.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME              = "WorkOrder";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    public static final String FLD_orderID              = "orderID";            // JSON: "id"
    // --
    public static final String FLD_workType             = "workType";           // JSON: "type"
    public static final String FLD_workStatus           = "workStatus";
    // --
    public static final String FLD_deviceID             = "deviceID";
    // --
    public static final String FLD_startByDay           = "startByDay";         // JSON: "no_earlier"
    public static final String FLD_endByDay             = "endByDay";           // JSON: "no_later"
    // --
    public static final String FLD_jobID                = "jobID";
    public static final String FLD_jobName              = "jobName";
    public static final String FLD_jobLocation          = "jobLocation";
    public static final String FLD_jobStartTime         = "jobStartTime";       // 
    public static final String FLD_jobEndTime           = "jobEndTime";         // 
    // --
    public static final String FLD_clientID             = "clientID";
    public static final String FLD_clientName           = "clientName";
    public static final String FLD_clientContact        = "clientContact";
    // --
    public static final String FLD_referenceID          = "referenceID";
    // --
    private static DBField FieldInfo[] = {
        // WorkOrder fields
        AccountRecord.newField_accountID(true),
        new DBField(FLD_orderID         , String.class  , DBField.TYPE_STRING(64) , "Work Order ID"    , "key=true"),
        new DBField(FLD_deviceID        , String.class  , DBField.TYPE_DEV_ID()   , "Assigned Vehicle" , "edit=2"),
        new DBField(FLD_workType        , String.class  , DBField.TYPE_STRING(32) , "Work Type"        , "edit=2"),
        new DBField(FLD_workStatus      , Integer.class , DBField.TYPE_UINT16     , "Work Status"      , "edit=2 enum=WorkOrder$WorkStatus"),
        new DBField(FLD_startByDay      , Long.TYPE     , DBField.TYPE_UINT32     , "Start By Day"     , "edit=2 format=date"),
        new DBField(FLD_endByDay        , Long.TYPE     , DBField.TYPE_UINT32     , "End By Day"       , "edit=2 format=date"),
        new DBField(FLD_jobID           , String.class  , DBField.TYPE_STRING(64) , "Job ID"           , "edit=2 altkey=job"),
        new DBField(FLD_jobName         , String.class  , DBField.TYPE_STRING(128), "Job Name"         , "edit=2"),
        new DBField(FLD_jobLocation     , String.class  , DBField.TYPE_STRING(200), "Job Location"     , "edit=2"),
        new DBField(FLD_jobStartTime    , Long.TYPE     , DBField.TYPE_UINT32     , "Job Start Time"   , "edit=2 format=time"),
        new DBField(FLD_jobEndTime      , Long.TYPE     , DBField.TYPE_UINT32     , "Job End Time"     , "edit=2 format=time"),
        new DBField(FLD_clientID        , String.class  , DBField.TYPE_STRING(64) , "Client ID"        , "edit=2"),
        new DBField(FLD_clientName      , String.class  , DBField.TYPE_STRING(128), "Client Name"      , "edit=2"),
        new DBField(FLD_clientContact   , String.class  , DBField.TYPE_STRING(128), "Client Contact"   , "edit=2"),
        new DBField(FLD_referenceID     , String.class  , DBField.TYPE_STRING(255), "Reference ID"     , "edit=2"),
        // Common fields
        newField_displayName(),
        newField_description(),
        newField_notes(),
        newField_lastUpdateTime(),
        newField_creationTime(),
    };

    /* key class */
    public static class Key
        extends AccountKey<WorkOrder>
    {
        public Key() {
            super();
        }
        public Key(String accountId, String orderId) {
            super.setKeyValue(FLD_accountID, ((accountId != null)? accountId.toLowerCase() : ""));
            super.setKeyValue(FLD_orderID  , ((orderId   != null)? orderId.toLowerCase()   : ""));
        }
        public DBFactory<WorkOrder> getFactory() {
            return WorkOrder.getFactory();
        }
    }
    
    /* factory constructor */
    private static DBFactory<WorkOrder> factory = null;
    public static DBFactory<WorkOrder> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                WorkOrder.TABLE_NAME(), 
                WorkOrder.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                WorkOrder.class, 
                WorkOrder.Key.class,
                true/*editable*/, true/*viewable*/);
            factory.addParentTable(Account.TABLE_NAME());
        }
        return factory;
    }

    /* Bean instance */
    public WorkOrder()
    {
        super();
    }

    /* database record */
    public WorkOrder(WorkOrder.Key key)
    {
        super(key);
    }
    
    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(WorkOrder.class, loc);
        return i18n.getString("WorkOrder.description", 
            "This table contains " + 
            "WorkOrder information."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    public String getOrderID()
    {
        String v = (String)this.getFieldValue(FLD_orderID);
        return (v != null)? v : "";
    }
        
    public String getWorkOrderID()
    {
        return this.getOrderID();
    }

    public void setOrderID(String v)
    {
        this.setFieldValue(FLD_orderID, ((v != null)? v : ""));
    }

    public void setWorkOrderID(String v)
    {
        this.setOrderID(v);
    }

    // ------------------------------------------------------------------------

    public String getWorkType()
    {
        String v = (String)this.getFieldValue(FLD_workType);
        return StringTools.trim(v);
    }

    public void setWorkType(String v)
    {
        this.setFieldValue(FLD_workType, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    public int getWorkStatus()
    {
        Integer v = (Integer)this.getFieldValue(FLD_workStatus);
        return (v != null)? v.intValue() : EnumTools.getDefault(WorkStatus.class).getIntValue();
    }

    public void setWorkStatus(int v)
    {
        this.setFieldValue(FLD_workStatus, EnumTools.getValueOf(WorkStatus.class,v).getIntValue());
    }

    /* set the work status */
    public void setWorkStatus(WorkStatus v)
    {
        this.setFieldValue(FLD_workStatus, EnumTools.getValueOf(WorkStatus.class,v).getIntValue());
    }

    /* set the work status */
    public void setWorkStatus(String v, Locale locale)
    {
        this.setFieldValue(FLD_workStatus, EnumTools.getValueOf(WorkStatus.class,v,locale).getIntValue());
    }

    // ------------------------------------------------------------------------
        
    public String getDeviceID()
    {
        String v = (String)this.getFieldValue(FLD_deviceID);
        return (v != null)? v : "";
    }

    public void setDeviceID(String v)
    {
        this.setFieldValue(FLD_deviceID, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public long getStartByDay()
    {
        return this.getFieldValue(FLD_startByDay, 0L);
    }

    public DayNumber getStartByDay_DayNumber()
    {
        long dn = this.getStartByDay();
        return new DayNumber(dn);
    }

    public DateTime getStartByDay_DateTime()
    {
        TimeZone tmz = Account.getTimeZone(this.getAccount(),null);
        return this.getStartByDay_DayNumber().getDayStart(tmz);
    }

    public void setStartByDay(long v)
    {
        this.setFieldValue(FLD_startByDay, ((v >= 0L)? v : 0L));
    }
    
    public void setStartByDay(int year, int month1, int day)
    {
        this.setStartByDay(DateTime.getDayNumberFromDate(year, month1, day));
    }
    
    public void setStartByDay(DayNumber dn)
    {
        this.setStartByDay((dn != null)? dn.getDayNumber() : 0L);
    }

    // ------------------------------------------------------------------------

    public long getEndByDay()
    {
        return this.getFieldValue(FLD_endByDay, 0L);
    }

    public DayNumber getEndByDay_DayNumber()
    {
        long dn = this.getEndByDay();
        return new DayNumber(dn);
    }

    public DateTime getEndByDay_DateTime()
    {
        TimeZone tmz = Account.getTimeZone(this.getAccount(),null);
        return this.getEndByDay_DayNumber().getDayEnd(tmz);
    }

    public void setEndByDay(long v)
    {
        this.setFieldValue(FLD_endByDay, ((v >= 0L)? v : 0L));
    }
    
    public void setEndByDay(int year, int month1, int day)
    {
        this.setEndByDay(DateTime.getDayNumberFromDate(year, month1, day));
    }
    
    public void setEndByDay(DayNumber dn)
    {
        this.setEndByDay((dn != null)? dn.getDayNumber() : 0L);
    }

    // ------------------------------------------------------------------------
        
    public String getJobID()
    {
        String v = (String)this.getFieldValue(FLD_jobID);
        return (v != null)? v : "";
    }

    public void setJobID(String v)
    {
        this.setFieldValue(FLD_jobID, ((v != null)? v : ""));
    }
   
    // ------------------------------------------------------------------------
        
    public String getJobName()
    {
        String v = (String)this.getFieldValue(FLD_jobName);
        return (v != null)? v : "";
    }

    public void setJobName(String v)
    {
        this.setFieldValue(FLD_jobName, ((v != null)? v : ""));
    }
   
    // ------------------------------------------------------------------------
        
    public String getJobLocation()
    {
        String v = (String)this.getFieldValue(FLD_jobLocation);
        return (v != null)? v : "";
    }

    public void setJobLocation(String v)
    {
        this.setFieldValue(FLD_jobLocation, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public long getJobStartTime()
    {
        return this.getFieldValue(FLD_jobStartTime, 0L);
    }

    public String getJobStartTimeString()
    {
        long ts = this.getJobStartTime();
        if (ts <= 0L) {
            return "";
        } else {
            TimeZone tmz = Account.getTimeZone(this.getAccount(), null);
            DateTime dt = new DateTime(ts,tmz);
            String fmt = dt.format("yyyy/MM/dd HH:mm:ss");
            return fmt;
        }
    }

    public void setJobStartTime(long v)
    {
        this.setFieldValue(FLD_jobStartTime, v);
    }
    
    public boolean setJobStartTime(String v)
    {
        try {
            TimeZone tmz = Account.getTimeZone(this.getAccount(), null);
            DateTime dt = DateTime.parseArgumentDate(v, tmz, DateTime.DefaultParsedTime.DayStart);
            this.setJobStartTime(dt.getTimeSec());
            return true;
        } catch (DateTime.DateParseException dpe) {
            return false;
        }
    }

    // ------------------------------------------------------------------------

    public long getJobEndTime()
    {
        return this.getFieldValue(FLD_jobEndTime, 0L);
    }

    public String getJobEndTimeString()
    {
        long ts = this.getJobEndTime();
        if (ts <= 0L) {
            return "";
        } else {
            TimeZone tmz = Account.getTimeZone(this.getAccount(), null);
            DateTime dt = new DateTime(ts,tmz);
            String fmt = dt.format("yyyy/MM/dd HH:mm:ss");
            return fmt;
        }
    }

    public void setJobEndTime(long v)
    {
        this.setFieldValue(FLD_jobEndTime, v);
    }

    public boolean setJobEndTime(String v)
    {
        try {
            TimeZone tmz = Account.getTimeZone(this.getAccount(), null);
            DateTime dt = DateTime.parseArgumentDate(v, tmz, DateTime.DefaultParsedTime.DayEnd);
            this.setJobEndTime(dt.getTimeSec());
            return true;
        } catch (DateTime.DateParseException dpe) {
            return false;
        }
    }

    // ------------------------------------------------------------------------
        
    public String getClientID()
    {
        String v = (String)this.getFieldValue(FLD_clientID);
        return (v != null)? v : "";
    }

    public void setClientID(String v)
    {
        this.setFieldValue(FLD_clientID, ((v != null)? v : ""));
    }
    
    // ------------------------------------------------------------------------

    public String getClientName()
    {
        String v = (String)this.getFieldValue(FLD_clientName);
        return (v != null)? v : "";
    }

    public void setClientName(String v)
    {
        this.setFieldValue(FLD_clientName, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public String getClientContact()
    {
        String v = (String)this.getFieldValue(FLD_clientContact);
        return (v != null)? v : "";
    }

    public void setClientContact(String v)
    {
        this.setFieldValue(FLD_clientContact, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------
        
    public String getReferenceID()
    {
        String v = (String)this.getFieldValue(FLD_referenceID);
        return (v != null)? v : "";
    }

    public void setReferenceID(String v)
    {
        this.setFieldValue(FLD_referenceID, ((v != null)? v : ""));
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
 
    /* debug: string representation of instance */
    public String toString()
    {
        return this.getAccountID() + "/" + this.getOrderID();
    }
    
    // ------------------------------------------------------------------------

    /* overridden to set default values */
    public void setCreationDefaultValues()
    {
        this.setDescription("");
        super.setRuntimeDefaultValues();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // specific WorkOrder retrieval

    /* return the description for the specified WorkOrder */
    public static String getWorkOrderDescription(String accountID, String orderID)
    {
        if (orderID == null) {
            return "";
        } else
        if (accountID == null) {
            return orderID;
        } else {
            try {
                WorkOrder w = WorkOrder.getWorkOrder(accountID, orderID);
                String d = (w != null)? w.getDescription() : "";
                return ((d != null) && !d.equals(""))? d : orderID;
            } catch (DBException dbe) {
                Print.logException("Retrieving WorkOrder description", dbe);
                return orderID;
            }
        }
    }

    /* Return specified WorkOrder (or null if non-existent) */
    public static WorkOrder getWorkOrder(String accountID, String orderID)
        throws DBException
    {
        return WorkOrder._getWorkOrder(accountID, null, orderID, false);
    }

    /* Return specified WorkOrder (or null if non-existent) */
    public static WorkOrder getWorkOrder(Account account, String orderID)
        throws DBException
    {
        return WorkOrder._getWorkOrder(null, account, orderID, false);
    }

    /* Return specified WorkOrder, create if specified */
    public static WorkOrder getWorkOrder(Account account, String orderID, boolean createOK)
        throws DBException
    {
        return WorkOrder._getWorkOrder(null, account, orderID, createOK);
    }
    
    /* Return specified WorkOrder, create if specified */
    private static WorkOrder _getWorkOrder(String accountID, Account account, String orderID, boolean createOK)
        throws DBException
    {
        // does not return null if 'createOK' is true

        /* account-id specified? */
        if (StringTools.isBlank(accountID)) {
            if (account == null) {
                throw new DBException("Account not specified.");
            } else {
                accountID = account.getAccountID();
            }
        } else
        if ((account != null) && !account.getAccountID().equals(accountID)) {
            throw new DBException("Account does not match specified AccountID.");
        }

        /* order-id specified? */
        if (StringTools.isBlank(orderID)) {
            throw new DBException("Order-ID not specified.");
        }

        /* get/create WorkOrder */
        WorkOrder.Key orderKey = new WorkOrder.Key(accountID, orderID);
        if (orderKey.exists()) { // may throw DBException
            WorkOrder wo = orderKey.getDBRecord(true);
            if (account != null) {
                wo.setAccount(account);
            }
            return wo;
        } else
        if (createOK) {
            WorkOrder wo = orderKey.getDBRecord();
            if (account != null) {
                wo.setAccount(account);
            }
            wo.setCreationDefaultValues();
            return wo; // not yet saved!
        } else {
            // record doesn't exist, and caller doesn't want us to create it
            return null;
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Account owned WorkOrders

    /* get WorkOrders */
    public static WorkOrder[] getWorkOrdersForAccount(
        String accountID, String jobID)
        throws DBException
    {
        return WorkOrder.getWorkOrdersForAccount(
            accountID, jobID,
            null/*workStatus*/,
            -1L/*start*/, -1L/*end*/, -1L/*limit*/, 
            null/*orderBy*/, true/*ascending*/,
            null/*handler*/);
    }

    /* get WorkOrders */
    public static WorkOrder[] getWorkOrdersForAccount(
        String accountID,  String jobID,
        long limit)
        throws DBException
    {
        return WorkOrder.getWorkOrdersForAccount(
            accountID, jobID,
            null/*workStatus*/,
            -1L/*start*/, -1L/*end*/, limit, 
            null/*orderBy*/, true/*ascending*/,
            null/*handler*/);
    }

    /* get WorkOrders */
    public static WorkOrder[] getWorkOrdersForAccount(
        String accountID, String jobID,
        int workStatus[],
        long startTime, long endTime, long limit)
        throws DBException
    {
        return WorkOrder.getWorkOrdersForAccount(
            accountID, jobID,
            workStatus,
            startTime, endTime, limit, 
            null/*orderBy*/, true/*ascending*/,
            null/*handler*/);
    }

    /* get WorkOrders */
    public static WorkOrder[] getWorkOrdersForAccount(
        String accountID, String jobID,
        int workStatus[],
        long startTime, long endTime, long limit,
        String orderBy[], boolean ascending,
        DBRecordHandler<WorkOrder> handler)
        throws DBException
    {

        /* select WorkOrder */
        // DBSelect: SELECT * FROM WorkOrder WHERE (accountID='acct') ORDER BY orderID
        DBSelect<WorkOrder> esel = new DBSelect<WorkOrder>(WorkOrder.getFactory());
        DBWhere ewh = esel.createDBWhere();

        /* DBWhere: add accountID */
        ewh.append(ewh.EQ(WorkOrder.FLD_accountID,accountID));

        /* DBWhere: add jobID */
        if (!StringTools.isBlank(jobID)) {
            ewh.append(ewh.AND_(ewh.EQ(WorkOrder.FLD_jobID,jobID)));
        }

        /* DBWhere: work status code(s) */
        // AND ( (workStatus=2) OR (workStatus=3) [OR ...] )
        if ((workStatus != null) && (workStatus.length > 0)) {
            ewh.append(ewh.AND_(ewh.INLIST(WorkOrder.FLD_workStatus,workStatus)));
        }

        /* DBWhere: start/end time */
        if (startTime > 0L) {
            // AND (jobStartTime>=123436789)
            ewh.append(ewh.AND_(ewh.GE(FLD_jobStartTime,startTime)));
        }
        if (endTime > 0L) {
            // AND (jobEndTime<=123436789)
            ewh.append(ewh.AND_(ewh.LE(FLD_jobEndTime,endTime)));
        }

        /* DBSelect: set Where */
        esel.setWhere(ewh);
        
        /* DBSelect: limit */
        if (limit > 0L) {
            esel.setLimit(limit);
        }

        /* DBSelect: order by */
        if (!ListTools.isEmpty(orderBy)) {
            esel.setOrderByFields(orderBy);
            esel.setOrderAscending(ascending);
        } else {
            esel.setOrderByFields(FLD_orderID);
            esel.setOrderAscending(ascending);
        }

        /* get WorkOrders */
        //return (WorkOrder[])DBRecord.select(WorkOrder.getFactory(), esel.toString(false), handler);
        Print.logInfo("WorkOrder Select: " + esel);
        return DBRecord.select(esel, handler);

    }

    // ------------------------------------------------------------------------

    /* get WorkOrders */
    public static WorkOrder[] getWorkOrdersForDevice(
        String accountID, 
        String deviceID)
        throws DBException
    {
        return WorkOrder.getWorkOrdersForDevice(accountID, deviceID, null);
    }

    /* get WorkOrders */
    public static WorkOrder[] getWorkOrdersForDevice(
        String accountID, 
        String deviceID, 
        DBRecordHandler<WorkOrder> handler)
        throws DBException
    {

        /* select WorkOrder */
        // DBSelect: SELECT * FROM WorkOrder WHERE (accountID='acct' and deviceID='dev') ORDER BY orderID
        DBSelect<WorkOrder> esel = new DBSelect<WorkOrder>(WorkOrder.getFactory());
        DBWhere ewh = esel.createDBWhere();
        esel.setWhere(ewh.WHERE(
            ewh.AND(
                ewh.EQ(WorkOrder.FLD_accountID, accountID),
                ewh.EQ(WorkOrder.FLD_deviceID , deviceID )
                // TODO: in-progress?, completed? etc.
            )
        ));
        esel.setOrderByFields(WorkOrder.FLD_orderID);
        
        /* get WorkOrders */
        //return (WorkOrder[])DBRecord.select(WorkOrder.getFactory(), esel.toString(false), handler);
        return DBRecord.select(esel, handler);

    }

    // ------------------------------------------------------------------------

    /* return list of all WorkOrders owned by the specified Account (NOT SCALABLE) */
    // does not return null
    public static OrderedSet<String> getWorkOrderIDsForAccount(
        String acctId, String jobID)
        throws DBException
    {
        return WorkOrder.getWorkOrderIDsForAccount(
            acctId, jobID,
            -1L);
    }

    /* return list of all WorkOrders owned by the specified Account (NOT SCALABLE) */
    // does not return null
    public static OrderedSet<String> getWorkOrderIDsForAccount(
        String acctID, String jobID,
        long limit)
        throws DBException
    {

        /* no account specified? */
        if (StringTools.isBlank(acctID)) {
            Print.logError("Account not specified!");
            return new OrderedSet<String>();
        }

        /* read drivers for account */
        OrderedSet<String> woList = new OrderedSet<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM WorkOrder WHERE (accountID='acct') ORDER BY orderID
            DBSelect<WorkOrder> dsel = new DBSelect<WorkOrder>(WorkOrder.getFactory());
            dsel.setSelectedFields(WorkOrder.FLD_orderID);
            DBWhere dwh = dsel.createDBWhere();
            if (StringTools.isBlank(jobID)) {
                dsel.setWhere(dwh.WHERE(
                    dwh.EQ(WorkOrder.FLD_accountID,acctID)
                    ));
            } else {
                dsel.setWhere(dwh.WHERE(
                    dwh.AND(
                        dwh.EQ(WorkOrder.FLD_accountID, acctID),
                        dwh.EQ(WorkOrder.FLD_jobID    , jobID )
                    )
                ));
            }
            dsel.setOrderByFields(WorkOrder.FLD_orderID);
            dsel.setLimit(limit);

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String woID = rs.getString(WorkOrder.FLD_orderID);
                woList.add(woID);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account WorkOrder List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return woList;

    }

    /* return list of all WorkOrders owned by the specified Account (NOT SCALABLE) */
    // does not return null
    public static OrderedSet<String> getWorkOrderIDsForDevice(String acctId, String devId)
        throws DBException
    {

        /* no account specified? */
        if (StringTools.isBlank(acctId)) {
            Print.logError("Account not specified!");
            return new OrderedSet<String>();
        } else 
        if (StringTools.isBlank(devId)) {
            Print.logError("Device not specified!");
            return new OrderedSet<String>();
        }

        /* read drivers for account */
        OrderedSet<String> woList = new OrderedSet<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM WorkOrder WHERE (accountID='acct') ORDER BY orderID
            DBSelect<WorkOrder> dsel = new DBSelect<WorkOrder>(WorkOrder.getFactory());
            dsel.setSelectedFields(WorkOrder.FLD_orderID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE(
                dwh.AND(
                    dwh.EQ(WorkOrder.FLD_accountID, acctId),
                    dwh.EQ(WorkOrder.FLD_deviceID , devId )
                    // TODO: in-progress?, completed? etc.
                )
            ));
            dsel.setOrderByFields(WorkOrder.FLD_orderID);
            //dsel.setLimit(limit);

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String woID = rs.getString(WorkOrder.FLD_orderID);
                woList.add(woID);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Device WorkOrder List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return woList;

    }

    // ------------------------------------------------------------------------

    /* return list of all Jobs owned by the specified Account (NOT SCALABLE) */
    // does not return null
    public static OrderedSet<String> getJobIDsForAccount(String acctID)
        throws DBException
    {
        return WorkOrder.getJobIDsForAccount(acctID, -1L);
    }

    /* return list of all Jobs owned by the specified Account (NOT SCALABLE) */
    // does not return null
    // NOTE: this method could stand to be optimized.
    public static OrderedSet<String> getJobIDsForAccount(String acctID, long limit)
        throws DBException
    {
        OrderedSet<String> jobIdList = new OrderedSet<String>();

        /* get all WorkOrders */
        WorkOrder woList[] = WorkOrder.getWorkOrdersForAccount(acctID, null/*null*/, limit);

        /* extract JobIDs */
        if (!ListTools.isEmpty(woList)) {
            for (WorkOrder wo : woList) {
                String jobID = wo.getJobID(); // may be blank
                jobIdList.add(jobID);
            }
        }

        /* return list */
        return jobIdList;

    }

    // ------------------------------------------------------------------------

    public static String[] GetWorkOrderID_array(String accountID, String woCSV)
    {
        String woArray[] = StringTools.split(woCSV,',');
        if (ListTools.isEmpty(woArray)) {
            return woArray;
        } else
        if (StringTools.isBlank(accountID)) {
            // just remove duplicates
            OrderedSet<String> woSet = new OrderedSet<String>(woArray);
            return woSet.toArray(new String[woSet.size()]);
        } else {
            // add only WorkOrders which exist
            OrderedSet<String> woSet = new OrderedSet<String>();
            for (String woid : woArray) {
                try {
                    if (WorkOrder.exists(accountID,woid)) {
                        woSet.add(woid);
                    }
                } catch (DBException dbe) {
                    // ignore
                    break;
                }
            }
            return woSet.toArray(new String[woSet.size()]);
        }
    }

    public static String GetWorkOrderID_csv(String accountID, String woArray[])
    {

        /* empty array */
        if (ListTools.isEmpty(woArray)) {
            return "";
        }

        /* validate all entries */
        OrderedSet<String> woList = new OrderedSet<String>();
        for (String woid : woArray) {
            if (!StringTools.isBlank(woid)) {

                /* no accountID to check against */
                if (StringTools.isBlank(accountID)) {
                    woList.add(woid.toLowerCase());
                    continue;
                }

                /* add if exists */
                try {
                    if (WorkOrder.exists(accountID,woid)) {
                        woList.add(woid.toLowerCase());
                    }
                } catch (DBException dbe) {
                    // ignore
                    break;
                }

            }
        }

        /* return CSV */
        return !ListTools.isEmpty(woList)? StringTools.join(woList,",") : "";

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // WorkOrder administration

    /* return true if WorkOrder exists */
    public static boolean exists(String accountID, String orderID)
        throws DBException // if error occurs while testing existance
    {
        if ((accountID != null) && (orderID != null)) {
            WorkOrder.Key woKey = new WorkOrder.Key(accountID, orderID);
            return woKey.exists();
        }
        return false;
    }

    /* create a new WorkOrder */
    public static WorkOrder createNewWorkOrder(Account account, String orderID, String description)
        throws DBException
    {
        if ((account != null) && (orderID != null) && !orderID.equals("")) {
            WorkOrder wo = WorkOrder.getWorkOrder(account, orderID, true); // does not return null
            if ((description != null) && !description.equals("")) {
                wo.setDescription(description);
            }
            wo.save();
            return wo;
        } else {
            throw new DBException("Invalid Account/OrderID specified");
        }
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below
    
    private static final String ARG_ACCOUNT[]   = new String[] { "account" , "acct" };
    private static final String ARG_DEVICE[]    = new String[] { "device"  , "dev"  };
    private static final String ARG_JOBID[]     = new String[] { "jobid"   , "job"  };
    private static final String ARG_WORKORDER[] = new String[] { "order"   , "wo"   , "id"   };
    private static final String ARG_DELETE[]    = new String[] { "delete"           };
    private static final String ARG_CREATE[]    = new String[] { "create"           };
    private static final String ARG_EDIT[]      = new String[] { "edit"    , "ed"   };
    private static final String ARG_EDITALL[]   = new String[] { "editall" , "eda"  };
    private static final String ARG_LIST[]      = new String[] { "list"             };

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + WorkOrder.class.getName() + " {options}");
        Print.logInfo("Options:");
        Print.logInfo("  -account=<id>      Account ID owning WorkOrder");
        Print.logInfo("  -device=<id>       Device ID assigned to WorkOrder");
        Print.logInfo("  -order=<id>        WorkOrder ID to delete/edit");
        Print.logInfo("  -create            Create a new WorkOrder");
        Print.logInfo("  -edit              To edit an existing WorkOrder");
        Print.logInfo("  -delete            Delete specified WorkOrder");
        Print.logInfo("  -list              List WorkOrders");
        System.exit(1);
    }
    
    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main
        String accountID = RTConfig.getString(ARG_ACCOUNT  , "");
        String deviceID  = RTConfig.getString(ARG_DEVICE   , "");
        String orderID   = RTConfig.getString(ARG_WORKORDER, "");
        String jobID     = RTConfig.getString(ARG_JOBID    , "");

        /* account-id specified? */
        if (StringTools.isBlank(accountID)) {
            Print.logError("Account-ID not specified.");
            usage();
        }

        /* get account */
        Account account = null;
        try {
            account = Account.getAccount(accountID); // may throw DBException
            if (account == null) {
                Print.logError("Account-ID does not exist: " + accountID);
                usage();
            }
        } catch (DBException dbe) {
            Print.logException("Error loading Account: " + accountID, dbe);
            //dbe.printException();
            System.exit(99);
        }

        /* order-id specified? */
        boolean orderSpecified = ((orderID != null) && !orderID.equals(""));
        //if ((orderID == null) || orderID.equals("")) {
        //    Print.logError("Order-ID not specified.");
        //    usage();
        //}

        /* order exists? */
        boolean orderExists = false;
        if (orderSpecified) {
            try {
                orderExists = WorkOrder.exists(accountID, orderID);
            } catch (DBException dbe) {
                Print.logError("Error determining if WorkOrder exists: " + accountID + "/" + orderID);
                System.exit(99);
            }
        }

        /* option count */
        int opts = 0;

        /* delete */
        if (RTConfig.getBoolean(ARG_DELETE,false)) {
            opts++;
            if (!orderSpecified) {
                Print.logWarn("WorkOrder name not specified ...");
                usage();
            } else
            if (!orderExists) {
                Print.logWarn("WorkOrder does not exist: " + accountID + "/" + orderID);
                Print.logWarn("Continuing with delete process ...");
            }
            try {
                WorkOrder.Key woKey = new WorkOrder.Key(accountID, orderID);
                woKey.delete(true); // also delete dependencies (if any)
                Print.logInfo("WorkOrder deleted: " + accountID + "/" + orderID);
                orderExists = false;
            } catch (DBException dbe) {
                Print.logError("Error deleting WorkOrder: " + accountID + "/" + orderID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* create */
        if (RTConfig.getBoolean(ARG_CREATE, false)) {
            opts++;
            if (!orderSpecified) {
                Print.logWarn("WorkOrder name not specified ...");
                usage();
            } else
            if (orderExists) {
                Print.logWarn("WorkOrder already exists: " + accountID + "/" + orderID);
            } else {
                try {
                    WorkOrder.createNewWorkOrder(account, orderID, null);
                    Print.logInfo("Created WorkOrder: " + accountID + "/" + orderID);
                    orderExists = true;
                } catch (DBException dbe) {
                    Print.logError("Error creating WorkOrder: " + accountID + "/" + orderID);
                    dbe.printException();
                    System.exit(99);
                }
            }
        }

        /* edit */
        if (RTConfig.getBoolean(ARG_EDIT,false) || RTConfig.getBoolean(ARG_EDITALL,false)) { 
            opts++;
            if (!orderSpecified) {
                Print.logWarn("WorkOrder name not specified ...");
                usage();
            } else
            if (!orderExists) {
                Print.logError("WorkOrder does not exist: " + accountID + "/" + orderID);
            } else {
                try {
                    boolean allFlds = RTConfig.getBoolean(ARG_EDITALL, false);
                    WorkOrder order = WorkOrder.getWorkOrder(account, orderID, false); // may throw DBException
                    DBEdit editor = new DBEdit(order);
                    editor.edit(allFlds); // may throw IOException
                } catch (IOException ioe) {
                    if (ioe instanceof EOFException) {
                        Print.logError("End of input");
                    } else {
                        Print.logError("IO Error");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error editing WorkOrder: " + accountID + "/" + orderID);
                    dbe.printException();
                }
            }
            System.exit(0);
        }

        /* list */
        if (RTConfig.getBoolean(ARG_LIST, false)) {
            opts++;
            try {
                WorkOrder orderList[] = null;
                if (!StringTools.isBlank(deviceID)) {
                    Print.sysPrintln("Device assigned WorkOrders ...");
                    orderList = WorkOrder.getWorkOrdersForDevice(accountID, deviceID);
                } else
                if (!StringTools.isBlank(jobID)) {
                    Print.sysPrintln("Account/JobID WorkOrders ...");
                    orderList = WorkOrder.getWorkOrdersForAccount(accountID, jobID);
                } else {
                    Print.sysPrintln("Account WorkOrders ...");
                    orderList = WorkOrder.getWorkOrdersForAccount(accountID, null);
                }
                for (int i = 0; i < orderList.length; i++) {
                    WorkOrder w = orderList[i];
                    Print.sysPrintln("  WorkOrder   : " + w.getAccountID() + "/" + w.getOrderID() + " [" + w.getDescription() + "]");
                }
            } catch (DBException dbe) {
                Print.logError("Error listing Entities: " + accountID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* no options specified */
        if (opts == 0) {
            Print.logWarn("Missing options ...");
            usage();
        }

    }
    
}
