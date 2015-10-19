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
//  2009/08/07  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.extra.tables;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.dbtypes.*;

import org.opengts.db.*;
import org.opengts.db.tables.*;

public class UnassignedDevices
    extends DBRecord<UnassignedDevices>
{

    // ------------------------------------------------------------------------

    public static final long   MAX_AGE_SEC              = DateTime.DaySeconds(60);

    /* MobileID separator for "account,device" */
    // separator may be a multi char sequence (ie. "::", etc)
    public static final String ACCOUNT_DEVICE_SEP       = ",";

    // ------------------------------------------------------------------------

    public static       int    MobileIDColumnLength     = -1;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME              = "UnassignedDevices";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    public static final String FLD_serverID             = "serverID";
    public static final String FLD_mobileID             = "mobileID";
    public static final String FLD_timestamp            = "timestamp";
    public static final String FLD_ipAddress            = "ipAddress";
    public static final String FLD_isDuplex             = "isDuplex";
    public static final String FLD_latitude             = "latitude";
    public static final String FLD_longitude            = "longitude";
    public static final String FLD_data                 = "data";
    private static DBField FieldInfo[] = {
        // UnassignedDevices fields
        new DBField(FLD_serverID        , String.class      , DBField.TYPE_ID()        , "Server ID"        , "key=true"),
        new DBField(FLD_mobileID        , String.class      , DBField.TYPE_STRING(32)  , "Mobile ID"        , "key=true"),
        new DBField(FLD_timestamp       , Long.TYPE         , DBField.TYPE_UINT32      , "Timestamp"        , ""),
        new DBField(FLD_ipAddress       , DTIPAddress.class , DBField.TYPE_STRING(32)  , "IP Address"       , ""),
        new DBField(FLD_isDuplex        , Boolean.TYPE      , DBField.TYPE_BOOLEAN     , "Is Duplex"        , ""),
        new DBField(FLD_latitude        , Double.TYPE       , DBField.TYPE_DOUBLE      , "Latitude"         , "format=#0.00000"),
        new DBField(FLD_longitude       , Double.TYPE       , DBField.TYPE_DOUBLE      , "Longitude"        , "format=#0.00000"),
        new DBField(FLD_data            , String.class      , DBField.TYPE_STRING(255) , "Data"             , ""),
        // Common fields
        newField_creationTime(),
    };

    /* key class */
    public static class Key
        extends DBRecordKey<UnassignedDevices>
    {
        public Key() {
            super();
        }
        public Key(String serverId, String mobileId) {
            String sid = (serverId != null)? serverId.toLowerCase() : "";
            String mid = DBRecord.adjustStringLength(mobileId,UnassignedDevices.MobileIDColumnLength).toLowerCase();
            super.setKeyValue(FLD_serverID, sid);
            super.setKeyValue(FLD_mobileID, mid);
        }
        public DBFactory<UnassignedDevices> getFactory() {
            return UnassignedDevices.getFactory();
        }
    }
    
    /* factory constructor */
    private static DBFactory<UnassignedDevices> factory = null;
    public static DBFactory<UnassignedDevices> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                UnassignedDevices.TABLE_NAME(), 
                UnassignedDevices.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                UnassignedDevices.class, 
                UnassignedDevices.Key.class,
                true/*editable*/,true/*viewable*/);
            // no parent tables
            // FLD_mobileID max length
            UnassignedDevices.MobileIDColumnLength = factory.getFieldStringLength(FLD_mobileID);
            if (UnassignedDevices.MobileIDColumnLength <= 0) { Print.logWarn("Could not find field: "+FLD_mobileID); }
        }
        return factory;
    }

    /* Bean instance */
    public UnassignedDevices()
    {
        super();
    }

    /* database record */
    public UnassignedDevices(UnassignedDevices.Key key)
    {
        super(key);
    }
    
    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(UnassignedDevices.class, loc);
        return i18n.getString("UnassignedDevices.description", 
            "This table contains " +
            "Mobile IDs for which no Device record was found."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    public String getServerID()
    {
        String v = (String)this.getFieldValue(FLD_serverID);
        return StringTools.trim(v);
    }
    
    public void seServerID(String v)
    {
        this.setFieldValue(FLD_serverID, StringTools.trim(v));
    }
    
    // ------------------------------------------------------------------------

    public String getMobileID()
    {
        String v = (String)this.getFieldValue(FLD_mobileID);
        return StringTools.trim(v);
    }
    
    public void setMobileID(String v)
    {
        String mid = DBRecord.adjustStringLength(v,UnassignedDevices.MobileIDColumnLength);
        this.setFieldValue(FLD_mobileID, mid);
    }
    
    // ------------------------------------------------------------------------

    public long getTimestamp()
    {
        Long v = (Long)this.getFieldValue(FLD_timestamp);
        return (v != null)? v.longValue() : 0L;
    }
    
    public void setTimestamp(long v)
    {
        this.setFieldValue(FLD_timestamp, v);
    }
    
    // ------------------------------------------------------------------------

    public DTIPAddress getIpAddress()
    {
        DTIPAddress v = (DTIPAddress)this.getFieldValue(FLD_ipAddress);
        return v; // May return null!!
    }

    public String getIpAddressString()
    {
        return StringTools.trim(this.getIpAddress());
    }

    public void setIpAddress(DTIPAddress v)
    {
        this.setFieldValue(FLD_ipAddress, v);
    }

    public void setIpAddress(String v)
    {
        this.setIpAddress((v != null)? new DTIPAddress(v) : null);
    }

    // ------------------------------------------------------------------------

    public boolean getIsDuplex()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_isDuplex);
        return (v != null)? v.booleanValue() : false;
    }

    public void setIsDuplex(boolean v)
    {
        this.setFieldValue(FLD_isDuplex, v);
    }
    
    public boolean isDuplex()
    {
        return this.getIsDuplex();
    }

    // ------------------------------------------------------------------------

    public double getLatitude()
    {
        return this.getFieldValue(FLD_latitude, 0.0);
    }
    
    public void setLatitude(double v)
    {
        this.setFieldValue(FLD_latitude, v);
    }

    // ------------------------------------------------------------------------

    public double getLongitude()
    {
        return this.getFieldValue(FLD_longitude, 0.0);
    }
    
    public void setLongitude(double v)
    {
        this.setFieldValue(FLD_longitude, v);
    }
    
    public GeoPoint getGeoPoint()
    {
        return new GeoPoint(this.getLatitude(), this.getLongitude());
    }

    // ------------------------------------------------------------------------
    
    public String getData()
    {
        String v = (String)this.getFieldValue(FLD_data);
        return StringTools.trim(v);
    }

    public void setData(String v)
    {
        this.setFieldValue(FLD_data, StringTools.truncate(v,255));
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public String toString()
    {
        return this.getServerID() + "/" + this.getMobileID();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* overridden to set default values */
    public void setCreationDefaultValues()
    {
        //super.setRuntimeDefaultValues();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void add(String serverID, String mobileID)
    {
        UnassignedDevices.add(serverID, mobileID, 0L, null, false, 0.0, 0.0, null);
    }
    
    public static void add(String serverID, String mobileID,
        double latitude, double longitude)
    {
        UnassignedDevices.add(serverID, mobileID, 0L, null, false, latitude, longitude, null);
    }
    
    public static void add(String serverID, String mobileID,
        String ipAddress, boolean isDuplex,
        double latitude, double longitude)
    {
        UnassignedDevices.add(serverID, mobileID, 0L, ipAddress, isDuplex, latitude, longitude, null);
    }
    
    /* This is the method called by DCServerFactory.addUnassignedDevice(...) */
    public static void add(String serverID, String mobileID,
        String ipAddress, boolean isDuplex,
        double latitude, double longitude,
        String data)
    {
        UnassignedDevices.add(serverID, mobileID, 0L, ipAddress, isDuplex, latitude, longitude, data);
    }

    public static void add(String serverID, String mobileID,
        long timestamp, String ipAddress, boolean isDuplex,
        double latitude, double longitude,
        String data)
    {
        
        try {

            /* create key */
            UnassignedDevices.Key rcdKey = new UnassignedDevices.Key(serverID, mobileID);

            /* update */
            long ts = (timestamp > 0L)? timestamp : DateTime.getCurrentTimeSec();
            boolean exists = rcdKey.exists();
            if (exists) {
                UnassignedDevices rcd = rcdKey.getDBRecord(true);
                rcd.setTimestamp(ts);
                rcd.setIpAddress(ipAddress);
                rcd.setIsDuplex(isDuplex);
                rcd.setLatitude(latitude);
                rcd.setLongitude(longitude);
                rcd.setData(data);
                rcd.update();
            } else {
                UnassignedDevices rcd = rcdKey.getDBRecord();
                rcd.setCreationDefaultValues();
                rcd.setTimestamp(ts);
                rcd.setIpAddress(ipAddress);
                rcd.setIsDuplex(isDuplex);
                rcd.setLatitude(latitude);
                rcd.setLongitude(longitude);
                rcd.setData(data);
                rcd.insert();
            }
        
        } catch (Throwable th) {
            Print.logException("Unable to add UnknownDevice",th);
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    public static void getRecordCallback(DBRecordHandler<UnassignedDevices> rcdHandler)
        throws DBException
    {

        // DBSelect: SELECT * FROM UnassignedDevices 
        DBSelect<UnassignedDevices> dsel = new DBSelect<UnassignedDevices>(UnassignedDevices.getFactory());

        /* iterate through records */
        DBRecord.select(dsel, rcdHandler);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void _iterate(DBRecordHandler<UnassignedDevices> rcdHandler, boolean orderByTimestamp)
        throws DBException
    {

        /* DBRecordHandler is required */
        if (rcdHandler == null) {
            throw new DBException("Missing DBRecordHandler");
        }
        
        // DBSelect: SELECT * FROM UnassignedDevices 
        DBSelect<UnassignedDevices> dsel = new DBSelect<UnassignedDevices>(UnassignedDevices.getFactory());
        if (orderByTimestamp) {
            dsel.setOrderByFields(FLD_serverID,FLD_mobileID,FLD_timestamp);
        } else {
            dsel.setOrderByFields(FLD_serverID,FLD_mobileID);
        }
        
        /* iterate through records */
        Statement stmt = null;
        ResultSet rs = null;
        try {
            DBRecord.select(dsel, rcdHandler);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
        }

    }

    // ------------------------------------------------------------------------

    public static String GetMobileIDDevice(DCServerConfig dcsc, String mobileID)
    {

        /* invalid mobile id */
        if (StringTools.isBlank(mobileID)) {
            return null;
        }

        /* check for "account,device", then unique-id */
        int p = mobileID.indexOf(ACCOUNT_DEVICE_SEP);
        if (p >= 0) {
            String accountID = mobileID.substring(0,p);
            String deviceID  = mobileID.substring(p+ACCOUNT_DEVICE_SEP.length());
            try {
                if (Transport.exists(accountID,deviceID)) {
                    return accountID + "," + deviceID;
                }
            } catch (DBException dbe) {
                Print.logException("Transport 'exists' error", dbe);
                return null;
            }
        } else
        if (dcsc != null) {
            Device device = DCServerConfig.loadDeviceUniqueID(dcsc, mobileID);
            if (device != null) {
                return device.getAccountID() + "," + device.getDeviceID() + "[" + device.getUniqueID() + "]";
            }
        }

        /* not found */
        return null;

    }

    // ------------------------------------------------------------------------

    /**
    *** Prints a list of unassigned devices to stdout
    **/
    private static long udList(final long maxAgeSec)
    {
        final AccumulatorLong counter = new AccumulatorLong(0L);

        /* record handler */
        final long nowTime = DateTime.getCurrentTimeSec();
        DBRecordHandler<UnassignedDevices> rcdHandler = new DBRecordHandler<UnassignedDevices>() {
            public int handleDBRecord(UnassignedDevices rcd) throws DBException {
                UnassignedDevices ud = rcd;
                String      serverID = ud.getServerID();
                String      mobileID = ud.getMobileID();
                long          udTime = ud.getTimestamp();
                String        ipAddr = StringTools.trim(ud.getIpAddress());
                GeoPoint          gp = ud.getGeoPoint();
                DCServerConfig  dcsc = DCServerFactory._getServerConfig(serverID); // may be null
                long          ageSec = nowTime - udTime;

                /* under maximum ages (seconds) */
                if ((maxAgeSec > 0L) && (ageSec > maxAgeSec)) {
                    // -- skip this entry
                    return DBRH_SKIP;
                }

                /* see if device now exists in the database */
                String devStr = GetMobileIDDevice(dcsc, mobileID);
                String exists = StringTools.blankDefault(devStr, "false");

                /* log String */
                StringBuffer sb = new StringBuffer();
                sb.append("ServerID=").append(serverID);
                sb.append(" MobileID=").append(mobileID);
                sb.append(" Time=").append(udTime);
                sb.append(" Age=").append(ageSec);
                if (!StringTools.isBlank(ipAddr)) {
                    sb.append(" IP=").append(ipAddr);
                }
                if (GeoPoint.isValid(gp)) {
                    sb.append(" GPS=").append(gp.toString());
                }
                sb.append(" Exists=").append(exists);
                Print.sysPrintln(sb.toString());

                /* count */
                counter.increment();
                return DBRH_SKIP;

            }
        };

        /* iterate */
        try {
            UnassignedDevices._iterate(rcdHandler,true); // udList
        } catch (DBException dbe) {
            Print.logException("Listing UnassignedDevices", dbe);
        }

        /* return count */
        return counter.get();

    }

    /**
    *** Prints a list of unassigned devices to stdout
    **/
    private static void udUpdate(final boolean okDelete, final long maxAgeSec)
    {
        final Vector<UnassignedDevices> deleteList = new Vector<UnassignedDevices>();

        /* record handler */
        final long nowTime = DateTime.getCurrentTimeSec();
        DBRecordHandler<UnassignedDevices> rcdHandler = new DBRecordHandler<UnassignedDevices>() {
            public int handleDBRecord(UnassignedDevices rcd) throws DBException {
                UnassignedDevices ud = rcd;
                String      serverID = ud.getServerID();
                String      mobileID = ud.getMobileID();
                long          udTime = ud.getTimestamp();
                DCServerConfig  dcsc = DCServerFactory._getServerConfig(serverID); // may be null
                long          ageSec = nowTime - udTime;

                /* check age */
                if ((maxAgeSec > 0L) && (ageSec > maxAgeSec)) {
                    Print.logInfo("Record exceeds maximum age: [%s] %s", serverID, mobileID);
                    if (okDelete) {
                        deleteList.add(ud);
                    }
                    return DBRH_SKIP;
                }

                /* see if device now exists in the database */
                String devStr = GetMobileIDDevice(dcsc, mobileID);
                if (!StringTools.isBlank(devStr)) {
                    Print.logInfo("Record is now defined: [%s] %s", serverID, devStr);
                    if (okDelete) {
                        deleteList.add(ud);
                    }
                }

                /* skip to next */
                return DBRH_SKIP;

            }
        };

        /* iterate */
        try {
            UnassignedDevices._iterate(rcdHandler,false); // udUpdate
        } catch (DBException dbe) {
            Print.logException("Listing UnassignedDevices", dbe);
        }

        /* delete */
        if (okDelete && !ListTools.isEmpty(deleteList)) {
            // TODO: delete records in "deleteList"
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below

    private static final String ARG_LIST[]      = new String[] { "list"   };
    private static final String ARG_UPDATE[]    = new String[] { "update" };

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + UserAcl.class.getName() + " {options}");
        Print.logInfo("Common Options:");
        Print.logInfo("  -list        List table contents");
        Print.logInfo("  -update      Update unassigned devices");
        System.exit(1);
    }

    public static void main(String args[])
    {
        DBConfig.cmdLineInit(args,true);  // main

        /* list */
        if (RTConfig.hasProperty(ARG_LIST)) {
            String list = RTConfig.getString(ARG_LIST,"");
            if (StringTools.isBlank(list) || !list.equalsIgnoreCase("false")) {
                long maxAgeSec = StringTools.parseLong(list,0L);
                UnassignedDevices.udList(maxAgeSec);
            }
            System.exit(0);
        }

        /* update */
        if (RTConfig.hasProperty(ARG_UPDATE)) {
            boolean delete = RTConfig.getString(ARG_UPDATE,"").equals("delete");
            long maxAgeSec = MAX_AGE_SEC;
            UnassignedDevices.udUpdate(delete,maxAgeSec);
            System.exit(0);
        }

        /* no options specified */
        Print.logWarn("Missing options ...");
        usage();

    }

    // ------------------------------------------------------------------------

}
