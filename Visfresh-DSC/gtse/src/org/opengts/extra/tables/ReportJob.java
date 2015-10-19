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
//  2013/04/08  Martin D. Flynn
//     -Initial release (cloned from Entity.java)
//  2015/05/03  Martin D. Flynn
//     -"setRecipients" now only saves email recipients.
// ----------------------------------------------------------------------------
package org.opengts.extra.tables;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;
import org.opengts.extra.service.*;

public class ReportJob
    extends AccountRecord<ReportJob>
{

    // ------------------------------------------------------------------------
    // Interval tags
    // Except for "none", the other tags must be defined in the "crontab.xml" file.

    public static final String CRONTAG_NONE             = "none";
    public static final String CRONTAG_DAILY            = "daily";
    public static final String CRONTAG_WEEKLY           = "weekly";

    /**
    *** IntervalTag class
    **/
    public static class IntervalTag
    {
        private String      tagID    = "";
        private I18N.Text   tagDesc  = null;
        private String      fromTime = null;
        private String      toTime   = null;
        public IntervalTag(String id, I18N.Text desc, String frTime, String toTime) {
            this.tagID    = id;
            this.tagDesc  = desc;
            this.fromTime = frTime;
            this.toTime   = toTime;
        }
        public String getTagID() {
            return this.tagID;
        }
        public I18N.Text getTagDescription() {
            return this.tagDesc;
        }
        public String getFromTime() {
            return this.fromTime;
        }
        public String getToTime() {
            return this.toTime;
        }
        public String toString(Locale locale) {
            return this.tagDesc.toString(locale);
        }
    }

    // --------------------------------

    private static Map<String,IntervalTag> _IntervalTagMap = new OrderedMap<String,IntervalTag>();

    static {
        // first IntervalTag should be "None"
        AddIntervalTag(CRONTAG_NONE, I18N.getString(ReportJob.class,"ReportJob.intervalTag.none","None"),"","");
    };

    /**
    *** Returns the IntervalTag map
    **/
    public static Map<String,IntervalTag> GetIntervalTagMap()
    {
        return _IntervalTagMap;
    }

    /**
    *** Returns true if the specified tagID is defined
    **/
    public static boolean HasIntervalTagID(String tagID)
    {
        return _IntervalTagMap.containsKey(tagID);
    }

    /**
    *** Adds the specified IntervalTag to the cached tag map
    **/
    public static void AddIntervalTag(IntervalTag tagInt)
    {
        String tagID = (tagInt != null)? tagInt.getTagID() : null;
        if (!StringTools.isBlank(tagID)) {
            _IntervalTagMap.put(tagID, tagInt);
        }
    }

    /**
    *** Adds the interval tag attributes to the cached tag map.<br>
    *** Called from ReportFactory while parsing "ReportJobs" tag.<br>
    *** See "reports.xml", "ReportJobs" tag for list of defined tags.
    **/
    public static void AddIntervalTag(String tagID, I18N.Text desc, String frTime, String toTime)
    {
        if (!StringTools.isBlank(tagID)) {
            AddIntervalTag(new IntervalTag(tagID, desc, frTime, toTime));
        }
    }

    /**
    *** Adds the interval tag attributes to the cached tag map.
    **/
    public static void AddIntervalTag(String tagID, String desc, String frTime, String toTime)
    {
        AddIntervalTag(tagID, new I18N.Text(desc), frTime, toTime);
    }

    /**
    *** Returns the localized description for the specified interval tag-id
    *** @param tagID  The Tag-id
    *** @param locale The language locale
    *** @return The tag-id description
    **/
    public static String GetIntervalTagDescription(String tagID, Locale locale)
    {
        ReportJob.IntervalTag rit = GetIntervalTagMap().get(tagID);
        return (rit != null)? rit.toString(locale) : tagID;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME              = "ReportJob";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    public static final String FLD_reportJobID          = "reportJobID";
    public static final String FLD_reportName           = "reportName";         // report name (as in 'reports.xml')
    public static final String FLD_reportOption         = "reportOption";       // report option
    public static final String FLD_deviceID             = "deviceID";           // selected DeviceID
    public static final String FLD_groupID              = "groupID";            // selected DeviceGroupID
    public static final String FLD_reportTimeFrom       = "reportTimeFrom";     // report "From" time
    public static final String FLD_reportTimeTo         = "reportTimeTo";       // report "To" time
    public static final String FLD_reportFormat         = "reportFormat";       // report format
    public static final String FLD_recipients           = "recipients";         // recipients
    public static final String FLD_intervalTag          = "intervalTag";        // report interval tag
    public static final String FLD_lastReportTime       = "lastReportTime";     // last report time
    private static DBField FieldInfo[] = {
        // ReportJob fields
        AccountRecord.newField_accountID(true),
        new DBField(FLD_reportJobID     , String.class  , DBField.TYPE_STRING(40)  , "Report Job ID"     , "key=true"),
        new DBField(FLD_reportName      , String.class  , DBField.TYPE_STRING(64)  , "Report Name"       , "edit=2"),
        new DBField(FLD_reportOption    , String.class  , DBField.TYPE_STRING(24)  , "Report Option"     , "edit=2"),
        new DBField(FLD_deviceID        , String.class  , DBField.TYPE_GROUP_ID()  , "Selected Device ID", "edit=2"),
        new DBField(FLD_groupID         , String.class  , DBField.TYPE_GROUP_ID()  , "Selected Group ID" , "edit=2"),
        new DBField(FLD_reportTimeFrom  , String.class  , DBField.TYPE_STRING(24)  , "Report 'From' Time", "edit=2"),
        new DBField(FLD_reportTimeTo    , String.class  , DBField.TYPE_STRING(24)  , "Report 'To' Time"  , "edit=2"),
        new DBField(FLD_reportFormat    , String.class  , DBField.TYPE_STRING(16)  , "Report Format"     , "edit=2"),
        new DBField(FLD_recipients      , String.class  , DBField.TYPE_STRING(200) , "Recipients"        , "edit=2"),
        new DBField(FLD_intervalTag     , String.class  , DBField.TYPE_STRING(16)  , "Cron Interval Tag" , "edit=2"),
        new DBField(FLD_lastReportTime  , Long.class    , DBField.TYPE_UINT32      , "Last Report Time"  , "edit=2"),
        // Common fields
        newField_isActive(),
        newField_description(),
        newField_lastUpdateTime(),
        newField_creationTime(),
    };

    /* key class */
    public static class Key
        extends AccountKey<ReportJob>
    {
        public Key() {
            super();
        }
        public Key(String accountId, String reportJobId) {
            super.setKeyValue(FLD_accountID  , ((accountId   != null)? accountId  .toLowerCase() : ""));
            super.setKeyValue(FLD_reportJobID, ((reportJobId != null)? reportJobId.toLowerCase() : ""));
        }
        public DBFactory<ReportJob> getFactory() {
            return ReportJob.getFactory();
        }
    }

    /* factory constructor */
    private static DBFactory<ReportJob> factory = null;
    public static DBFactory<ReportJob> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                ReportJob.TABLE_NAME(), 
                ReportJob.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                ReportJob.class, 
                ReportJob.Key.class,
                true/*editable*/, true/*viewable*/);
            factory.addParentTable(Account.TABLE_NAME());
        }
        return factory;
    }

    /* Bean instance */
    public ReportJob()
    {
        super();
    }

    /* database record */
    public ReportJob(ReportJob.Key key)
    {
        super(key);
    }
    
    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(ReportJob.class, loc);
        return i18n.getString("ReportJob.description", 
            "This table contains " + 
            "Account specific 'ReportJob' (ie. periodic reports) information."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    /**
    *** Gets the report ID for this entry
    **/
    public String getReportJobID()
    {
        String v = (String)this.getFieldValue(FLD_reportJobID);
        return (v != null)? v : "";
    }

    /**
    *** Sets the report ID for this entry
    **/
    public void setReportJobID(String v)
    {
        this.setFieldValue(FLD_reportJobID, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the report name (as defined in 'reports.xml')
    **/
    public String getReportName()
    {
        String v = (String)this.getFieldValue(FLD_reportName);
        return (v != null)? v : "";
    }

    /**
    *** Sets the report name (as defined in 'reports.xml')
    **/
    public void setReportName(String v)
    {
        this.setFieldValue(FLD_reportName, ((v != null)? v : ""));
    }
    
    /**
    *** Returns true if a report name is defined
    **/
    public boolean hasReportName()
    {
        return !StringTools.isBlank(this.getReportName());
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the assigned report options
    **/
    public String getReportOption()
    {
        String v = (String)this.getFieldValue(FLD_reportOption);
        return (v != null)? v : "";
    }

    /**
    *** Sets the assigned report options
    **/
    public void setReportOption(String v)
    {
        this.setFieldValue(FLD_reportOption, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the assigned report Device-ID
    **/
    public String getDeviceID()
    {
        String v = (String)this.getFieldValue(FLD_deviceID);
        return (v != null)? v : "";
    }

    /**
    *** Sets the assigned report Device-ID
    **/
    public void setDeviceID(String v)
    {
        this.setFieldValue(FLD_deviceID, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the assigned report Group-ID
    **/
    public String getGroupID()
    {
        String v = (String)this.getFieldValue(FLD_groupID);
        return (v != null)? v : "";
    }

    /**
    *** Sets the assigned report Group-ID
    **/
    public void setGroupID(String v)
    {
        this.setFieldValue(FLD_groupID, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Report "From" time.
    *** Specified as a String to allow custom relative time specifications.
    **/
    public String getReportTimeFrom()
    {
        String v = (String)this.getFieldValue(FLD_reportTimeFrom);
        return (v != null)? v : "";
    }

    /**
    *** Sets the Report "From" time.
    *** Specified as a String to allow custom relative time specifications.
    **/
    public void setReportTimeFrom(String v)
    {
        //  2011/08/20              - a specific day
        //  2011/08/20 14:23:12     - a specific day and time
        //  -2d                     - two days ago
        //  -1d                     - one day ago (yesterday)
        //  -0d                     - today
        this.setFieldValue(FLD_reportTimeFrom, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Report "To" time.
    *** Specified as a String to allow custom relative time specifications.
    **/
    public String getReportTimeTo()
    {
        String v = (String)this.getFieldValue(FLD_reportTimeTo);
        return (v != null)? v : "";
    }

    /**
    *** Sets the Report "To" time.
    *** Specified as a String to allow custom relative time specifications.
    **/
    public void setReportTimeTo(String v)
    {
        // Example time specifications
        //   2011/08/20              - a specific day
        //   2011/08/20 14:23:12     - a specific day and time
        //   -2d                     - two days ago
        //   -1d                     - one day ago (yesterday)
        //   -0d                     - today
        //   -1h                     - one hour ago
        //   -1800s                  - 1800 seconds ago (30 minutes)
        //   -0s                     - now
        // Note: Parsed by "DateTime.parseDateTime(...)"
        this.setFieldValue(FLD_reportTimeTo, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the report format
    **/
    public String getReportFormat()
    {
        String v = (String)this.getFieldValue(FLD_reportFormat);
        return (v != null)? v : "";
    }

    /**
    *** Gets the report format
    **/
    public String getReportFormat(String dft)
    {
        String fmt = this.getReportFormat();
        return !StringTools.isBlank(fmt)? fmt : dft;
    }

    /**
    *** Gets the report format
    **/
    public void setReportFormat(String v)
    {
        // url
        // email
        // ehtml
        // html
        // xml
        this.setFieldValue(FLD_reportFormat, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the list of email recipients
    **/
    public String getRecipients()
    {
        String v = (String)this.getFieldValue(FLD_recipients);
        return (v != null)? v : "";
    }

    /**
    *** Sets the list of email recipients
    **/
    public void setRecipients(String v)
    {
        Recipients r = new Recipients(v); // comma-separated list
        this.setFieldValue(FLD_recipients, r.getEmailRecipientsString()); // EMail only
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the reporting interval tag
    **/
    public String getIntervalTag()
    {
        String v = (String)this.getFieldValue(FLD_intervalTag);
        return (v != null)? v : "";
    }

    /**
    *** Sets the reporting interval
    **/
    public void setIntervalTag(String v)
    {
        this.setFieldValue(FLD_intervalTag, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the time of the last generated report
    **/
    public long getLastReportTime()
    {
        Long v = (Long)this.getFieldValue(FLD_lastReportTime);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the time of the last generated report
    **/
    public void setLastReportTime(long v)
    {
        this.setFieldValue(FLD_lastReportTime, v);
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
 
    /**
    *** Returns a String representation of instance 
    **/
    public String toString()
    {
        return this.getAccountID() + "/" + this.getReportJobID();
    }

    // ------------------------------------------------------------------------

    /**
    *** Override to set default values 
    **/
    public void setCreationDefaultValues()
    {
        super.setRuntimeDefaultValues();
        this.setIntervalTag(ReportJob.CRONTAG_NONE);
        this.setIsActive(false); // default to inactive
        this.setDescription("");
        this.setReportFormat(ReportURL.FORMAT_EMAIL);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Account owned ReportJobs

    /**
    *** Gets a list of ReportJob instances for the specified Account
    **/
    public static ReportJob[] getReportJobsForAccount(String accountID)
        throws DBException
    {
        return ReportJob.getReportJobsForAccount(accountID, -1L, null);
    }

    /**
    *** Gets a list of ReportJob instances for the specified Account
    **/
    public static ReportJob[] getReportJobsForAccount(String accountID, long limit, DBRecordHandler<ReportJob> handler)
        throws DBException
    {

        /* select ReportJob */
        // DBSelect: SELECT * FROM ReportJob WHERE (accountID='acct') ORDER BY reportJobID
        DBSelect<ReportJob> jsel = new DBSelect<ReportJob>(ReportJob.getFactory());
        DBWhere jwh = jsel.createDBWhere();
        jsel.setWhere(jwh.WHERE(jwh.EQ(ReportJob.FLD_accountID,accountID)));
        jsel.setOrderByFields(ReportJob.FLD_reportJobID);
        if (limit > 0L) {
            jsel.setLimit(limit);
        }

        /* get ReportJobs */
        //return (ReportJob[])DBRecord.select(ReportJob.getFactory(), esel.toString(false), handler);
        return DBRecord.select(jsel, handler);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Account owned ReportJobs

    /**
    *** Gets a list of ReportJob IDs for the specified Account
    **/
    public static OrderedSet<String> getReportJobIDsForAccount(String accountID, boolean activeOnly)
        throws DBException
    {
        return ReportJob.getReportJobIDsForAccount(accountID, null/*tagID*/, activeOnly, -1L, null);
    }

    /**
    *** Gets a list of ReportJob IDs for the specified Account
    **/
    public static OrderedSet<String> getReportJobIDsForAccount(String accountID, String tagID, boolean activeOnly)
        throws DBException
    {
        return ReportJob.getReportJobIDsForAccount(accountID, tagID, activeOnly, -1L, null);
    }

    /**
    *** Gets a list of ReportJob IDs for the specified Account
    **/
    public static OrderedSet<String> getReportJobIDsForAccount(String accountID, String tagID, boolean activeOnly, long limit)
        throws DBException
    {
        return ReportJob.getReportJobIDsForAccount(accountID, tagID, activeOnly, limit, null);
    }

    /**
    *** Gets a list of ReportJob IDs for the specified Account
    **/
    public static OrderedSet<String> getReportJobIDsForAccount(String accountID, String tagIDs, boolean activeOnly, long limit, DBRecordHandler<ReportJob> handler)
        throws DBException
    {

        /* no account specified? */
        if (StringTools.isBlank(accountID)) {
            Print.logError("Account not specified!");
            return new OrderedSet<String>();
        }

        /* tags */
        String tagID[] = null;
        if (!StringTools.isBlank(tagIDs)) {
            if (tagIDs.indexOf(",") >= 0) {
                Vector<String> idList = new Vector<String>();
                for (String id : StringTools.split(tagIDs,',')) {
                    if (!StringTools.isBlank(id)) {
                        idList.add(id);
                    }
                }
                if (!ListTools.isEmpty(idList)) {
                    tagID = idList.toArray(new String[idList.size()]);
                } else {
                    Print.logError("Invalid tag specification: " + tagIDs);
                    return new OrderedSet<String>();
                }
            } else {
                tagID = new String[] { tagIDs };
            }
        }

        /* read ReportJobs for account */
        OrderedSet<String> rjList = new OrderedSet<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM ReportJob WHERE (accountID='acct') ORDER BY reportJobID
            DBSelect<ReportJob> jsel = new DBSelect<ReportJob>(ReportJob.getFactory());
            jsel.setSelectedFields(
                ReportJob.FLD_accountID,
                ReportJob.FLD_reportJobID,
                ReportJob.FLD_isActive,
                ReportJob.FLD_intervalTag
                );
            // WHERE (accountID='acct') AND (intervalTag='tag') and (isActive != 0) and (reportName != "")
            DBWhere jwh = jsel.createDBWhere();
            jsel.setWhere(
                jwh.WHERE(
                    jwh.AND(
                        jwh.EQ(ReportJob.FLD_accountID  , accountID),
                        //!StringTools.isBlank(tagID)?jwh.EQ(ReportJob.FLD_intervalTag,tagID):null,
                        (tagID != null)? jwh.INLIST(ReportJob.FLD_intervalTag,tagID) : null,
                        activeOnly?jwh.EQ(ReportJob.FLD_isActive,true):null,
                        activeOnly?jwh.NE(ReportJob.FLD_reportName,""):null
                    )
                )
            );
            jsel.setOrderByFields(ReportJob.FLD_reportJobID);
            jsel.setLimit(limit);
            //Print.logDebug("ReportJob 'select': " + jsel);

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(jsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String rjId = rs.getString(ReportJob.FLD_reportJobID);
                rjList.add(rjId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account ReportJob List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return rjList;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ReportJob administration

    /**
    *** Returns true if the specified ReportJob exists 
    **/
    public static boolean exists(String accountID, String reportJobID)
        throws DBException // if error occurs while testing existance
    {
        if ((accountID != null) && (reportJobID != null)) {
            ReportJob.Key rjKey = new ReportJob.Key(accountID, reportJobID);
            return rjKey.exists();
        }
        return false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the specified ReportJob instance 
    **/
    public static ReportJob getReportJob(Account account, String reportJobID, boolean createOK)
        throws DBException
    {
        // does not return null, if 'createOK' is true

        /* account-id specified? */
        if (account == null) {
            throw new DBException("Account not specified.");
        }

        /* reportJobID specified? */
        if (StringTools.isBlank(reportJobID)) {
            throw new DBException("Device ReportJob-ID not specified.");
        }

        /* get/create ReportJob */
        ReportJob.Key rjKey = new ReportJob.Key(account.getAccountID(), reportJobID);
        if (rjKey.exists()) { // may throw DBException
            ReportJob rj = rjKey.getDBRecord(true);
            rj.setAccount(account);
            return rj;
        } else
        if (createOK) {
            ReportJob rj = rjKey.getDBRecord();
            rj.setAccount(account);
            rj.setCreationDefaultValues();
            return rj; // not yet saved!
        } else {
            // record doesn't exist, and caller doesn't want us to create it
            return null;
        }

    }

    /**
    *** Gets the specified ReportJob instance 
    **/
    public static ReportJob getReportJob(Account account, String reportJobID)
        throws DBException
    {
        if ((account != null) && !StringTools.isBlank(reportJobID)) {
            return ReportJob.getReportJob(account, reportJobID, false); // may return null
        } else {
            // throw new DBException("Invalid Account/ReportJobID specified");
            return null;
        }
    }

    /**
    *** Creates the specified ReportJob, and returns an instance to the record 
    **/
    public static ReportJob createNewReportJob(Account account, String reportJobID)
        throws DBException
    {
        if ((account != null) && !StringTools.isBlank(reportJobID)) {
            ReportJob rj = ReportJob.getReportJob(account, reportJobID, true); // does not return null
            rj.save();
            return rj;
        } else {
            throw new DBException("Invalid Account/ReportJobID specified");
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns an XML formated Report "GTSRequest" (long form)
    **/
    public String createGTSRequest_Report(Account A, User U)
    {

        /* account/user */
        String accID   = (A != null)? A.getAccountID() : "";
        String usrID   = (U != null)? U.getUserID() : "";
        String passwd  = (U != null)? U.getPassword() : (A != null)? A.getPassword() : "";

        /* ReportJob info */
        String rptID   = this.getReportJobID();
        String rptName = this.getReportName();
        String rptOptn = this.getReportOption();
        String rptFmt  = this.getReportFormat(ReportURL.FORMAT_EMAIL);
        String devID   = this.getDeviceID();
        String grpID   = this.getGroupID();
        String timeFrm = this.getReportTimeFrom();
        String timeTo  = this.getReportTimeTo();
        String emailAd = this.getRecipients();

        /* create GTSRequest */
        StringBuffer sb = new StringBuffer();
        sb.append("<GTSRequest command=\"report\">\n");
        sb.append("  <Authorization account=\""+accID+"\" user=\""+usrID+"\" password=\""+passwd+"\"/>\n");
        sb.append("  <Report name=\""+rptName+"\" option=\""+rptOptn+"\" format=\""+rptFmt+"\">\n");
        sb.append("     <Device>"+devID+"</Device>\n");
        sb.append("     <DeviceGroup>"+grpID+"</DeviceGroup>\n");
        sb.append("     <TimeFrom>"+timeFrm+"</TimeFrom>\n");
        sb.append("     <TimeTo>"+timeTo+"</TimeTo>\n");
        sb.append("     <EmailAddress>"+emailAd+"</EmailAddress>\n");
        sb.append("  </Report>\n");
        sb.append("</GTSRequest>\n");

        /* return GTSRequest */
        return sb.toString();

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns an XML formated Report "GTSRequest" (short form)
    **/
    public static String createGTSRequest_Report(Account A, User U, String cronTags)
    {

        /* account/user */
        String accID   = (A != null)? A.getAccountID() : "";
        String usrID   = (U != null)? U.getUserID() : "";
        String passwd  = (U != null)? U.getPassword() : (A != null)? A.getPassword() : "";

        /* create GTSRequest */
        StringBuffer sb = new StringBuffer();
        sb.append("<GTSRequest command=\"report\">\n");
        sb.append("  <Authorization account=\""+accID+"\" user=\""+usrID+"\" password=\""+passwd+"\"/>\n");
        sb.append("  <ReportJob reportGroupTag=\""+cronTags+"\">\n");
        sb.append("</GTSRequest>\n");

        /* return GTSRequest */
        return sb.toString();

    }

    /**
    *** Returns an XML formated Report "GTSRequest" (short form)
    **/
    public static String createGTSRequest_Report(Account A, User U, Collection<String> rptJobIDs)
    {

        /* account/user */
        String accID   = (A != null)? A.getAccountID() : "";
        String usrID   = (U != null)? U.getUserID() : "";
        String passwd  = (U != null)? U.getPassword() : (A != null)? A.getPassword() : "";

        /* create GTSRequest */
        StringBuffer sb = new StringBuffer();
        sb.append("<GTSRequest command=\"report\">\n");
        sb.append("  <Authorization account=\""+accID+"\" user=\""+usrID+"\" password=\""+passwd+"\"/>\n");
        for (String jobID : rptJobIDs) {
            sb.append("  <Report reportJobID=\""+jobID+"\">\n");
        }
        sb.append("</GTSRequest>\n");

        /* return GTSRequest */
        return sb.toString();

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Cron entry point below

    private static final String SELECT_ALL          = "ALL";
    private static final String SELECT_NONE         = "NONE";

    private static final String ARG_URL[]           = new String[] { "url"         , "SERVICE_URL"    , DBConfig.PROP_GTSRequest_url };
    private static final String ARG_VERSBOSE[]      = new String[] { "verbose"     , "v"              };
    private static final String ARG_QUIET[]         = new String[] { "quiet"       , "q"              };
    private static final String ARG_ACCOUNT[]       = new String[] { "account"     , "acct"           };
    private static final String ARG_TAG[]           = new String[] { "tag"         , "reportGroupTag" };
    private static final String ARG_NOEMAIL[]       = new String[] { "noemail"     , "debug"          };
    private static final String ARG_ERROR_EMAIL[]   = new String[] { "onErrorEmail", "errorNotify"    };
    
    public static int cron(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        //Print.setLogLevel(Print.LOG_ALL);
        Print.setLogHeaderLevel(Print.LOG_ALL);

        /* verbose/quiet */
        boolean verbose = false;
        if (RTConfig.hasProperty(ARG_VERSBOSE)) {
            verbose = RTConfig.getBoolean(ARG_VERSBOSE,true);
        } else
        if (RTConfig.hasProperty(ARG_QUIET)) {
            verbose = !RTConfig.getBoolean(ARG_QUIET,false);
        }

        /* SendMail thread mode */
        if (RTConfig.getBoolean(ARG_NOEMAIL,false)) {
            SendMail.SetThreadModel(SendMail.THREAD_NONE);
        } else {
            SendMail.SetThreadModel(SendMail.THREAD_CURRENT);
        }

        /* account/jobTag */
        String cronTags = RTConfig.getString(ARG_TAG, "");
        if (StringTools.isBlank(cronTags)) {
            Print.logWarn("Missing CronTag value");
            return 1;
        }
        Print.logInfo("ReportJob cron tag = " + cronTags);
        String cronTag[] = StringTools.split(cronTags,',');
        if (ListTools.isEmpty(cronTag)) {
            // unlikely, but check anyway
            Print.logWarn("CronTag list is empty!");
            return 1;
        }

        /* missing Account-ID */
        String accountIDs = RTConfig.getString(ARG_ACCOUNT, "");
        if (StringTools.isBlank(accountIDs)) {
            Print.logWarn("Missing Account-ID specification");
            return 1;
        }

        /* list of accounts */
        Collection<String> accountList = null;
        if (accountIDs.equals(SELECT_ALL)) {
            try {
                accountList = Account.getAllAccounts(); // returns Account IDs
            } catch (DBException dbe) {
                Print.logException("Error getting list of Account IDs", dbe);
                return 99;
            }
        } else {
            accountList = ListTools.toList(StringTools.split(accountIDs,','));
        }

        /* no matching Accounts */
        if (ListTools.isEmpty(accountList)) {
            Print.logWarn("No matching Account found");
            return 1;
        }

        /* URL */
        String urlStr = RTConfig.getString(ARG_URL, null);
        if (StringTools.isBlank(urlStr)) {
            Print.logError("GTSRequest URL not specified");
            return 1;
        }

        /* loop through accounts */
        int rtnStat = 0;
        for (String accountID : accountList) {
            if (StringTools.isBlank(accountID)) {
                //Print.logWarn("Skipping blank account-ID specification");
                continue;
            }

            /* get Account */
            Account account = null;
            try {
                account = Account.getAccount(accountID); // may throw DBException
                if (account == null) {
                    Print.logInfo("====================================================");
                    Print.logError("Account-ID does not exist: " + accountID);
                    continue;
                }
            } catch (DBException dbe) {
                Print.logInfo("====================================================");
                Print.logException("Reading Account: " + accountID, dbe);
                return 99;
            }

            /* ReportJob table exists? */
            DBFactory<ReportJob> rjFact = ReportJob.getFactory();
            try {
                if (!rjFact.tableExists()) {
                    //Print.logInfo("====================================================");
                    //Print.logError("'" + ReportJob.TABLE_NAME() + "' table does not exist.");
                    return 99;
                }
            } catch (DBException dbe) {
                Print.logInfo("====================================================");
                Print.logException("Checking '" + ReportJob.TABLE_NAME() + "' table existence", dbe);
                return 99;
            }

            /* does this account have at least one active ReportJob entries? */
            try {
                boolean foundRJ = false;
                for (String ct : cronTag) {
                    OrderedSet<String> rjList = ReportJob.getReportJobIDsForAccount(accountID,ct,true,1L);
                    if (!ListTools.isEmpty(rjList)) {
                        foundRJ = true;
                        break;
                    }
                }
                if (!foundRJ) {
                    // -- this Account has no defined/active ReportJob's
                    //Print.logDebug("Account has no defined/active ReportJob's: " + accountID);
                    continue;
                }
            } catch (DBException dbe) {
                Print.logInfo("====================================================");
                Print.logException("Unable to get ReportJob list", dbe);
                continue;
            }

            /* get User */
            String userID = User.getAdminUserID();
            User user = null;
            try {
                user = User.getUser(account, userID); // may throw DBException
            } catch (DBException dbe) {
                // -- assume user null
                user = null;
            }

            /* password */
            String password = (user != null)? user.getPassword() : (account != null)? account.getPassword() : "";

            /* header */
            Print.logInfo("====================================================");
            Print.logInfo("Account="+accountID + ", User="+userID); // + ", Password="+password);

            /* GTS Service Request */
            GTSServiceRequest servReq = null;
            try {
                servReq = new GTSServiceRequest(urlStr); // cron
                servReq.setAuthorization(accountID, userID, password); // cron
            } catch (MalformedURLException mue) {
                Print.logException("Error", mue);
                return 1;
            }

            /* request report */
            Document gtsResp = null;
            try {
                gtsResp = servReq.getReportJob_Document(cronTags);
                if (gtsResp == null) {
                    Print.logError("Request error ...");
                    return 99;
                }
                if (verbose) {
                    Print.logInfo("Response:");
                    Print.logInfo(XMLTools.nodeToString(gtsResp));
                }
            } catch (IOException ioe) {
                Print.logException("Error", ioe);
                return 99;
            }

            /* parse request error */
            // <GTSResponse result="error">
            //    <Message code="RQ0010"><![CDATA[Service Request disabled]]></Message>
            //    <Comment><![CDATA[Service disabled]]></Comment>
            // </GTSResponse>
            if (GTSServiceRequest.isGTSResponseError(gtsResp)) {
                String msgCode = GTSServiceRequest.getGTSResponse_Message_code(gtsResp);
                ServiceMessage srvMsg = ServiceMessage.getMessageForCode(msgCode);
                if (srvMsg == null) {
                    // no matching ServiceMessage
                } else
                if (srvMsg.equals(ServiceMessage.MSG_REQUEST_DISABLED)) {
                    Print.logError(srvMsg.getMessage(urlStr));
                    String errorNotifyEmail = RTConfig.getString(ARG_ERROR_EMAIL,null);
                    if (!StringTools.isBlank(errorNotifyEmail)) {
                        // send error notification
                        String subj = "ReportJob web-service is not enabled!";
                        String body = "A periodic ReportJob was requested, but the ReportJob web-service is not enabled\n" +
                                      "URL: " + urlStr;
                        SendMail.SmtpProperties smtpProps = new SendMail.SmtpProperties();
                        boolean retry   = false;
                        String fromAddr = smtpProps.getUserEmail();
                        String toAddr   = errorNotifyEmail;
                        if (SendMail.send(fromAddr,toAddr,subj,body,smtpProps,retry)) {
                            // notify email sent
                            Print.logInfo("Error notification email sent: " + toAddr);
                        } else {
                            // error sending email
                            Print.logError("Una le to send error notification");
                        }
                    }
                    // skip remaining Accounts, then they would not succeed either
                    break;
                } else {
                    // other error
                }
            }

        }

        /* return result */
        return rtnStat;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below

    private static final String ARG_REPORTJOB[] = new String[] { "reportJob", "jobID", "rj"  };
    private static final String ARG_DELETE[]    = new String[] { "delete"               };
    private static final String ARG_CREATE[]    = new String[] { "create"               };
    private static final String ARG_EDIT[]      = new String[] { "edit"     , "ed"      };
    private static final String ARG_EDITALL[]   = new String[] { "editall"  , "eda"     };
    private static final String ARG_LIST_TAGS[] = new String[] { "listTags" , "listTag" };
    private static final String ARG_LIST[]      = new String[] { "list"                 };
    private static final String ARG_CRON[]      = new String[] { "cron"                 };

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + ReportJob.class.getName() + " {options}");
        Print.logInfo("Options:");
        Print.logInfo("  -cron=true      Run in 'Cron' test mode");
        Print.logInfo("  -account=<id>   Account ID owning ReportJob");
        Print.logInfo("  -reportJob=<id> ReportJob ID to delete/edit");
        Print.logInfo("  -create         Create a new ReportJob");
        Print.logInfo("  -edit           To edit an existing ReportJob");
        Print.logInfo("  -delete         Delete specified ReportJob");
        System.exit(1);
    }
    
    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main

        /* cron */
        if (RTConfig.getBoolean(ARG_CRON,false)) {
            int rtn = ReportJob.cron(argv);
            System.exit(rtn);
        }

        /* create/edit */
        String accountID   = RTConfig.getString(ARG_ACCOUNT  , "");
        String reportJobID = RTConfig.getString(ARG_REPORTJOB, "");

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

        /* reportJob-id specified? */
        boolean reportJobSpecified = !StringTools.isBlank(reportJobID);

        /* ReportJob exists? */
        boolean reportJobExists = false;
        if (reportJobSpecified) {
            try {
                reportJobExists = ReportJob.exists(accountID, reportJobID);
            } catch (DBException dbe) {
                Print.logError("Error determining if ReportJob exists: " + accountID + "/" + reportJobID);
                System.exit(99);
            }
        }

        /* option count */
        int opts = 0;

        /* delete */
        if (RTConfig.getBoolean(ARG_DELETE,false)) {
            opts++;
            if (!reportJobSpecified) {
                Print.logWarn("ReportJob name not specified ...");
                usage();
            } else
            if (!reportJobExists) {
                Print.logWarn("ReportJob does not exist: " + accountID + "/" + reportJobID);
                Print.logWarn("Continuing with delete process ...");
            }
            try {
                ReportJob.Key rjKey = new ReportJob.Key(accountID, reportJobID);
                rjKey.delete(true); // also delete dependencies (if any)
                Print.logInfo("ReportJob deleted: " + accountID + "/" + reportJobID);
                reportJobExists = false;
            } catch (DBException dbe) {
                Print.logError("Error deleting ReportJob: " + accountID + "/" + reportJobID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* create */
        if (RTConfig.getBoolean(ARG_CREATE, false)) {
            opts++;
            if (!reportJobSpecified) {
                Print.logWarn("ReportJob name not specified ...");
                usage();
            } else
            if (reportJobExists) {
                Print.logWarn("ReportJob already exists: " + accountID + "/" + reportJobID);
            } else {
                try {
                    ReportJob.createNewReportJob(account, reportJobID);
                    Print.logInfo("Created ReportJob: " + accountID + "/" + reportJobID);
                    reportJobExists = true;
                } catch (DBException dbe) {
                    Print.logError("Error creating ReportJob: " + accountID + "/" + reportJobID);
                    dbe.printException();
                    System.exit(99);
                }
            }
        }

        /* edit */
        if (RTConfig.getBoolean(ARG_EDIT,false) || RTConfig.getBoolean(ARG_EDITALL,false)) { 
            opts++;
            if (!reportJobSpecified) {
                Print.logWarn("ReportJob name not specified ...");
                usage();
            } else
            if (!reportJobExists) {
                Print.logError("ReportJob does not exist: " + accountID + "/" + reportJobID);
            } else {
                try {
                    boolean allFlds = RTConfig.getBoolean(ARG_EDITALL, false);
                    ReportJob rptJob = ReportJob.getReportJob(account, reportJobID, false); // may throw DBException
                    DBEdit editor = new DBEdit(rptJob);
                    editor.edit(allFlds); // may throw IOException
                } catch (IOException ioe) {
                    if (ioe instanceof EOFException) {
                        Print.logError("End of input");
                    } else {
                        Print.logError("IO Error");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error editing ReportJob: " + accountID + "/" + reportJobID);
                    dbe.printException();
                }
            }
            System.exit(0);
        }

        /* list IDs */
        if (RTConfig.hasProperty(ARG_LIST_TAGS)) {
            String tagIDs = RTConfig.getString(ARG_LIST_TAGS,"");
            opts++;
            try {
                OrderedSet<String> idList = ReportJob.getReportJobIDsForAccount(accountID, tagIDs, false);
                if (!ListTools.isEmpty(idList)) {
                    for (String rji : idList) {
                        ReportJob rj = ReportJob.getReportJob(account, rji, false); // may throw DBException
                        String acctID = rj.getAccountID();
                        String   rjID = rj.getReportJobID();
                        String rjDesc = rj.getDescription();
                        String  rjTag = rj.getIntervalTag();
                        Print.logInfo("  ReportJob   : " + acctID + "/" + rjID + " [" + rj.getDescription() + "] Tag=" + rjTag);
                    }
                } else {
                    Print.sysPrintln("(no ReportsJobs found)");
                }
            } catch (DBException dbe) {
                Print.logError("Error listing ReportJob IDs: " + accountID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* list */
        if (RTConfig.getBoolean(ARG_LIST, false)) {
            opts++;
            try {
                ReportJob rjList[] = ReportJob.getReportJobsForAccount(accountID);
                for (int i = 0; i < rjList.length; i++) {
                    ReportJob  rj = rjList[i];
                    String  acctID = rj.getAccountID();
                    String    rjID = rj.getReportJobID();
                    String  rjDesc = rj.getDescription();
                    String   rjTag = rj.getIntervalTag();
                    boolean active = rj.isActive();
                    Print.logInfo("  ReportJob   : " + acctID + "/" + rjID + " [" + rj.getDescription() + "] Tag=" + rjTag + (active?"":" (inactive)"));
                }
            } catch (DBException dbe) {
                Print.logError("Error listing ReportJobs: " + accountID);
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
