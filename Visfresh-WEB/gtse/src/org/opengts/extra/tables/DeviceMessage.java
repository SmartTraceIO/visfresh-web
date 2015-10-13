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
//  2014/06/29  Martin D. Flynn
//     -Initial release
//  2014/09/16  Martin D. Flynn
//     -Added "expireTimestamp" column
// ----------------------------------------------------------------------------
package org.opengts.extra.tables;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

public class DeviceMessage
    extends DeviceRecord<DeviceMessage>
{

    public static final long   DEFAULT_EXPIRATION_DAYS  = 7L;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Canned responses

    public static final String CR_READ                  = "{read}"; // message was read
    public static final String CR_OK                    = "{ok}";   // response was "OK"
    public static final String CR_YES                   = "{yes}";  // response was "Yes"
    public static final String CR_NO                    = "{no}";   // response was "No"
    public static final String CR_TEXT                  = "{text}"; // response was free text

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Message type

    public enum MessageType implements EnumTools.StringLocale, EnumTools.IntValue {
        NONE        (   0, I18N.getString(DeviceMessage.class,"DeviceMessageInterface.messageType.none"   ,"None"   )),
        OK          (   1, I18N.getString(DeviceMessage.class,"DeviceMessageInterface.messageType.ok"     ,"OK"     )),
        YES_NO      (   2, I18N.getString(DeviceMessage.class,"DeviceMessageInterface.messageType.yesNo"  ,"Yes/No" )),
        TEXT        (   3, I18N.getString(DeviceMessage.class,"DeviceMessageInterface.messageType.text"   ,"Text"   )),
        COMMAND     ( 100, I18N.getString(DeviceMessage.class,"DeviceMessageInterface.messageType.command","Command"));
        // ---
        private int         vv = 0;
        private I18N.Text   aa = null;
        MessageType(int v, I18N.Text a)         { vv = v; aa = a; }
        public int     getIntValue()            { return vv; }
        public String  toString()               { return aa.toString(); }
        public String  toString(Locale loc)     { return aa.toString(loc); }
        public boolean isNone()                 { return this.equals(MessageType.NONE);   }
        public boolean isOK()                   { return this.equals(MessageType.OK);     }
        public boolean isYesNo()                { return this.equals(MessageType.YES_NO); }
        public boolean isText()                 { return this.equals(MessageType.TEXT);   }
    };

    /**
    *** Returns the defined MessageType for the specified DeviceMessageInterface.
    *** @param dmi  The DeviceMessageInterface from which the MessageType will be obtained.  
    ***           If null, the default MessageType will be returned.
    *** @return The MessageType
    **/
    public static MessageType getMessageType(DeviceMessage dmi)
    {
        return (dmi != null)? 
            EnumTools.getValueOf(MessageType.class,dmi.getMessageType()) : 
            EnumTools.getDefault(MessageType.class);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME              = "DeviceMessage";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    public static final String FLD_messageID            = "messageID";          // message ID
    public static final String FLD_queueTimestamp       = "queueTimestamp";     // queue timestamp
    public static final String FLD_messageSender        = "messageSender";      // sender/from
    public static final String FLD_messageTitle         = "messageTitle";       // message title
    public static final String FLD_message              = "message";            // message
    public static final String FLD_messageType          = "messageType";        // message type (enum)
    public static final String FLD_priority             = "priority";           // priority
    public static final String FLD_expireTimestamp      = "expireTimestamp";    // expire timestamp
    public static final String FLD_sentTimestamp        = "sentTimestamp";      // sent timestamp
    public static final String FLD_readTimestamp        = "readTimestamp";      // read timestamp
    public static final String FLD_ackTimestamp         = "ackTimestamp";       // ack timestamp
    public static final String FLD_ackLatitude          = "ackLatitude";        // ack latitude
    public static final String FLD_ackLongitude         = "ackLongitude";       // ack longitude
    public static final String FLD_ackResponse          = "ackResponse";        // ack response (canned)
    private static DBField FieldInfo[] = {
        // DeviceMessage fields
        AccountRecord.newField_accountID(true),
        DeviceRecord.newField_deviceID(true),
        new DBField(FLD_messageID       , String.class  , DBField.TYPE_STRING(24)  , "Message ID"       , "key=true"),
        new DBField(FLD_queueTimestamp  , Long.TYPE     , DBField.TYPE_UINT32      , "Queue Timestamp"  , "edit=2 format=time"),
        new DBField(FLD_messageSender   , String.class  , DBField.TYPE_STRING(30)  , "Sender/From"      , "edit=2"),
        new DBField(FLD_messageTitle    , String.class  , DBField.TYPE_STRING(60)  , "Message Title"    , "edit=2"),
        new DBField(FLD_message         , String.class  , DBField.TYPE_STRING(300) , "Message"          , "edit=2"),
        new DBField(FLD_messageType     , Integer.TYPE  , DBField.TYPE_UINT16      , "Message Type"     , "edit=2 enum=DeviceMessage$MessageType"),
        new DBField(FLD_priority        , Integer.TYPE  , DBField.TYPE_UINT32      , "Priority"         , "edit=2"),
        new DBField(FLD_expireTimestamp , Long.TYPE     , DBField.TYPE_UINT32      , "Expire Timestamp" , "edit=2 format=time"),
        new DBField(FLD_sentTimestamp   , Long.TYPE     , DBField.TYPE_UINT32      , "Sent Timestamp"   , "edit=2 format=time"),
        new DBField(FLD_readTimestamp   , Long.TYPE     , DBField.TYPE_UINT32      , "Read Timestamp"   , "edit=2 format=time"),
        new DBField(FLD_ackTimestamp    , Long.TYPE     , DBField.TYPE_UINT32      , "Ack Timestamp"    , "edit=2 format=time"),
        new DBField(FLD_ackLatitude     , Double.TYPE   , DBField.TYPE_DOUBLE      , "Ack Latitude"     , "edit=2 format=#0.00000"),
        new DBField(FLD_ackLongitude    , Double.TYPE   , DBField.TYPE_DOUBLE      , "Ack Longitude"    , "edit=2 format=#0.00000"),
        new DBField(FLD_ackResponse     , String.class  , DBField.TYPE_STRING(300) , "Response"         , "edit=2"),
        // Common fields
        newField_lastUpdateTime(),
        newField_creationTime(),
    };

    /* key class */
    public static class Key
        extends DeviceKey<DeviceMessage>
    {
        public Key() {
            super();
        }
        public Key(String acctId, String devId, String msgID) {
            super.setKeyValue(FLD_accountID , ((acctId != null)? acctId.toLowerCase() : ""));
            super.setKeyValue(FLD_deviceID  , ((devId  != null)? devId.toLowerCase()  : ""));
            super.setKeyValue(FLD_messageID , msgID);
        }
        public DBFactory<DeviceMessage> getFactory() {
            return DeviceMessage.getFactory();
        }
    }
    
    /* factory constructor */
    private static DBFactory<DeviceMessage> factory = null;
    public static DBFactory<DeviceMessage> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                DeviceMessage.TABLE_NAME(), 
                DeviceMessage.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                DeviceMessage.class, 
                DeviceMessage.Key.class,
                true/*editable*/, true/*viewable*/);
            factory.addParentTable(Account.TABLE_NAME());
            factory.addParentTable(Device.TABLE_NAME());
        }
        return factory;
    }

    /* Bean instance */
    public DeviceMessage()
    {
        super();
    }

    /* database record */
    public DeviceMessage(DeviceMessage.Key key)
    {
        super(key);
    }

    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(DeviceMessage.class, loc);
        return i18n.getString("DeviceMessage.description", 
            "This table contains " + 
            "DeviceMessage information."
            );
    }

    // ------------------------------------------------------------------------

    /**
    *** Updates the specified fields in this DBRecord.
    *** @param updFldSet  A Set of fields to update.
    *** @throws DBException if a database error occurs.
    **/
    public void update(Set<String> updFldSet)
        throws DBException
    {
        super.update(updFldSet);
    }

    /** 
    *** Insert this DBRecord in the database.<br>
    *** An exception will be throw if the record already exists
    *** @throws DBException if a database error occurs.
    **/
    public void insert()
        throws DBException
    {
        super.insert();
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below

    /**
    *** Gets the MessageID
    **/
    public String getMessageID()
    {
        String v = (String)this.getFieldValue(FLD_messageID);
        return StringTools.trim(v);
    }

    /**
    *** Sets the MessageID as a long value
    **/
    public void setMessageID(long v)
    {
        this.setMessageID(String.valueOf(v));
    }

    /**
    *** Sets the MessageID
    **/
    public void setMessageID(String v)
    {
        this.setFieldValue(FLD_messageID, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the time that the message was initially queued
    **/
    public long getQueueTimestamp()
    {
        Long v = (Long)this.getFieldValue(FLD_queueTimestamp);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the time that the message was initially queued
    **/
    public void setQueueTimestamp(long v)
    {
        this.setFieldValue(FLD_queueTimestamp, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the time that the message expires
    **/
    public long getExpireTimestamp()
    {
        Long v = (Long)this.getFieldValue(FLD_expireTimestamp);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the time that the message expires
    **/
    public void setExpireTimestamp(long v)
    {
        this.setFieldValue(FLD_expireTimestamp, v);
    }

    /**
    *** Gets the time that the message expires
    **/
    public long getExpireTimestamp(long dft)
    {
        long exTS = this.getExpireTimestamp();
        return (exTS > 0L)? exTS : dft;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the time that the message was initially sent
    **/
    public long getSentTimestamp()
    {
        Long v = (Long)this.getFieldValue(FLD_sentTimestamp);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the time that the message was initially sent
    **/
    public void setSentTimestamp(long v)
    {
        this.setFieldValue(FLD_sentTimestamp, v);
    }

    /**
    *** Returns true if message has been sent to device
    **/
    public boolean isSent()
    {
        return (this.getSentTimestamp() > 0L)? true : false;
    }
    
    /**
    *** Sets this DeviceMessage as sent, and optionally updates the record
    **/
    public void setSent(long sentTime, boolean update)
        throws DBException
    {
        // -- update DeviceMessage sent
        if (!this.isSent()) {
            // -- set timestamp iff not already sent
            this.setSentTimestamp(sentTime);
            if (update) {
                this.update(DeviceMessage.FLD_sentTimestamp);
            }
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the time that the message was initially read
    **/
    public long getReadTimestamp()
    {
        Long v = (Long)this.getFieldValue(FLD_readTimestamp);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the time that the message was initially read
    **/
    public void setReadTimestamp(long v)
    {
        this.setFieldValue(FLD_readTimestamp, v);
    }

    /**
    *** Returns true if message has read by device
    **/
    public boolean isRead()
    {
        return (this.getReadTimestamp() > 0L)? true : false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the message sender
    **/
    public String getMessageSender()
    {
        String v = (String)this.getFieldValue(FLD_messageSender);
        return StringTools.trim(v);
    }

    /**
    *** Sets the message sender
    **/
    public void setMessageSender(String v)
    {
        this.setFieldValue(FLD_messageSender, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the message title
    **/
    public String getMessageTitle()
    {
        String v = (String)this.getFieldValue(FLD_messageTitle);
        return StringTools.trim(v);
    }

    /**
    *** Sets the message title
    **/
    public void setMessageTitle(String v)
    {
        this.setFieldValue(FLD_messageTitle, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the message
    **/
    public String getMessage()
    {
        String v = (String)this.getFieldValue(FLD_message);
        return StringTools.trim(v);
    }

    /**
    *** Sets the message
    **/
    public void setMessage(String v)
    {
        this.setFieldValue(FLD_message, StringTools.trim(v));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the enumerated message type
    **/
    public int getMessageType()
    {
        Integer v = (Integer)this.getFieldValue(FLD_messageType);
        return (v != null)? v.intValue() : EnumTools.getDefault(MessageType.class).getIntValue();
    }

    /**
    *** Sets the enumerated message type
    **/
    public void setMessageType(int v)
    {
        this.setFieldValue(FLD_messageType, EnumTools.getValueOf(MessageType.class,v).getIntValue());
    }

    /**
    *** Sets the enumerated message type
    **/
    public void setMessageType(MessageType v)
    {
        this.setFieldValue(FLD_messageType, EnumTools.getValueOf(MessageType.class,v).getIntValue());
    }
    
    // ------------------------------------------------------------------------

    /**
    *** Gets the message priority
    **/
    public int getPriority()
    {
        Integer v = (Integer)this.getFieldValue(FLD_priority);
        return (v != null)? v.intValue() : 0;
    }

    /**
    *** Sets the message priority
    **/
    public void setPriority(int v)
    {
        this.setFieldValue(FLD_priority, v);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the time that the message was acknowledged
    **/
    public long getAckTimestamp()
    {
        Long v = (Long)this.getFieldValue(FLD_ackTimestamp);
        return (v != null)? v.longValue() : 0L;
    }

    /**
    *** Sets the time that the message was acknowledged
    **/
    public void setAckTimestamp(long v)
    {
        this.setFieldValue(FLD_ackTimestamp, v);
    }

    /**
    *** Returns true if message has acknowledged by device
    **/
    public boolean isAcknowledged()
    {
        return (this.getAckTimestamp() > 0L)? true : false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the ack latitude
    *** @return The ack latitude
    **/
    public double getAckLatitude()
    {
        return this.getOptionalFieldValue(FLD_ackLatitude, 0.0);
    }

    /**
    *** Gets the ack latitude
    *** @param v The ack latitude
    **/
    public void setAckLatitude(double v)
    {
        this.setOptionalFieldValue(FLD_ackLatitude, v);
    }

    /**
    *** Gets the ack longitude 
    *** @return The ack longitude 
    **/
    public double getAckLongitude()
    {
        return this.getOptionalFieldValue(FLD_ackLongitude, 0.0);
    }

    /**
    *** Sets the ack longitude
    *** @param v The ack longitude
    **/
    public void setAckLongitude(double v)
    {
        this.setOptionalFieldValue(FLD_ackLongitude, v);
    }

    /**
    *** Returns true if ack location has been defined
    *** @return True if ack location has been defined
    **/
    public boolean hasAckLocation()
    {
        double lat = this.getAckLatitude();
        double lon = this.getAckLongitude();
        return GeoPoint.isValid(lat,lon);
    }

    /**
    *** Gets the ack GeoPoint
    *** @return The ack GeoPoint, null if not defined/invalid
    **/
    public GeoPoint getAckLocation()
    {
        double lat = this.getAckLatitude();
        double lon = this.getAckLongitude();
        return GeoPoint.isValid(lat,lon)? new GeoPoint(lat,lon) : null;
    }

    /**
    *** Sets the ack GeoPoint
    **/
    public void setAckLocation(GeoPoint gp)
    {
        if (GeoPoint.isValid(gp)) {
            this.setAckLatitude( gp.getLatitude());
            this.setAckLongitude(gp.getLongitude());
        } else {
            this.setAckLatitude( 0.0);
            this.setAckLongitude(0.0);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the acknowledged response
    **/
    public String getAckResponse()
    {
        String v = (String)this.getFieldValue(FLD_ackResponse);
        return StringTools.trim(v);
    }

    /**
    *** Sets the acknowledged response
    **/
    public void setAckResponse(String v)
    {
        this.setFieldValue(FLD_ackResponse, StringTools.trim(v));
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Override to set default values
    **/
    public void setCreationDefaultValues()
    {
        super.setRuntimeDefaultValues();
    }

    // ------------------------------------------------------------------------

    /**
    *** Formats the specified timestamp as a string
    **/
    public String formatTimestamp(long timestamp)
    {
        Account a = this.getAccount();
        String dateFmt = (a != null)? a.getDateFormat() : BasicPrivateLabel.getDefaultDateFormat();
        String timeFmt = (a != null)? a.getTimeFormat() : BasicPrivateLabel.getDefaultTimeFormat();
        DateTime dt = new DateTime(timestamp);
        return dt.gmtFormat(dateFmt + " " + timeFmt + " z");
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** MessageComparator class for sorting DeviceMessage objects
    **/
    public static class MessageComparator
        implements Comparator<DeviceMessage>
    {
        public MessageComparator() {
            super();
        }
        public int compare(DeviceMessage dm1, DeviceMessage dm2) {
            long dmQT1 = dm1.getQueueTimestamp();
            long dmQT2 = dm2.getQueueTimestamp();
            if (dmQT1 < dmQT2) {
                return -1;
            } else 
            if (dmQT1 > dmQT2) {
                return 1;
            } else {
                return 0;
            }
        }
        public boolean equals(Object other) {
            if (other instanceof MessageComparator) {
                MessageComparator mc = (MessageComparator)other;
                return true;
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets an existing DeviceMessage record.
    *** Returns null if not found
    **/
    public static DeviceMessage getDeviceMessage(Device dev, String msgID)
        throws DBException
    {

        /* device specified? */
        if (dev == null) {
            return null;
        }
        String acctID = dev.getAccountID();
        String devID  = dev.getDeviceID();

        /* get DeviceMessage */
        DeviceMessage.Key dmKey = new DeviceMessage.Key(acctID, devID, msgID);
        if (dmKey.exists()) {
            DeviceMessage dm = dmKey.getDBRecord(true);
            dm.setDevice(dev);
            return dm;
        } else {
            // DeviceMessage does not exist
            return null;
        }

    }

    /**
    *** Gets/Creates a DeviceMessage
    *** Note: does NOT return null (throws exception if not found)
    **/
    public static DeviceMessage getDeviceMessage(Device dev, String msgID, boolean create)
        throws DBException
    {

        /* device specified? */
        if (dev == null) {
            return null;
        }
        String acctID = dev.getAccountID();
        String devID  = dev.getDeviceID();

        /* message-id specified? */
        if (StringTools.isBlank(msgID)) {
            throw new DBNotFoundException("DeviceMessage-ID not specified for device: " + acctID + "/" + devID);
        }

        /* get/create */
        DeviceMessage.Key dmKey = new DeviceMessage.Key(acctID, devID, msgID);
        if (!dmKey.exists()) {
            if (create) {
                DeviceMessage dm = dmKey.getDBRecord();
                dm.setDevice(dev);
                dm.setCreationDefaultValues();
                return dm; // not yet saved!
            } else {
                throw new DBNotFoundException("DeviceMessage-ID does not exists: " + dmKey);
            }
        } else
        if (create) {
            // -- we've been asked to create the DeviceMessage, and it already exists
            throw new DBAlreadyExistsException("DeviceMessage-ID already exists: " + dmKey);
        } else {
            DeviceMessage dm = DeviceMessage.getDeviceMessage(dev, msgID);
            if (dm == null) {
                throw new DBException("Unable to read existing DeviceMessage-ID: " + dmKey);
            }
            return dm;
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Acknowledges the specified message.
    *** @return The acknowledge timestamp if successfully acknowledged, 
    ***     otherwise returns 0 is the message does not exist, or if the message is already acknowledged.
    **/
    public static long ackMessage(Device dev, String msgID, long ackTime, String response, GeoPoint gp)
        throws DBException
    {

        /* device specified? */
        if (dev == null) {
            Print.logError("DeviceMessage Device not specified");
            return 0L;
        }
        String acctID = dev.getAccountID();
        String devID  = dev.getDeviceID();

        /* get message */
        DeviceMessage dm = DeviceMessage.getDeviceMessage(dev,msgID);
        if (dm == null) {
            Print.logError("DeviceMessage not found: " + acctID + "/" + devID + "/" + msgID);
            return 0L;
        }

        /* already acknowledged? */
        if (dm.isAcknowledged()) {
            // -- message already acknowledged
            return 0L;
        }

        /* has message been sent? */
        if (!dm.isSent()) {
            // -- do not ack/read messages that have not yet been sent to device
            return 0L;
        }

        /* read/acknowledge */
        if (StringTools.startsWithIgnoreCase(response,CR_READ)) {
            long readTS = (ackTime > 0L)? ackTime : DateTime.getCurrentTimeSec();
            // -- message was read
            dm.setReadTimestamp(readTS); 
            // --
            dm.setAckResponse(response); // {read} (overwritten by actual ack)
            dm.setAckLocation(gp); // 'gp' may be null
            dm.update(
                DeviceMessage.FLD_readTimestamp, // read time
                DeviceMessage.FLD_ackResponse,
                DeviceMessage.FLD_ackLatitude,
                DeviceMessage.FLD_ackLongitude
                );
            return readTS;
        } else {
            long ackTS = (ackTime > 0L)? ackTime : DateTime.getCurrentTimeSec();
            // -- message was acknowledged
            dm.setAckTimestamp(ackTS); 
            // --
            dm.setAckResponse(response); // {ok}, {yes}, {no}, ...
            dm.setAckLocation(gp); // ok if 'gp' is null
            dm.update(
                DeviceMessage.FLD_ackTimestamp, // ack time
                DeviceMessage.FLD_ackResponse,
                DeviceMessage.FLD_ackLatitude,
                DeviceMessage.FLD_ackLongitude
                );
            return ackTS;
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns a list of MessageIDs owned by the specified Account/Device.
    *** Does not return null
    **/
    public static OrderedSet<String> getMessageIDsForDevice(Device dev, boolean pendingOnly, long limit)
        throws DBException
    {

        /* no device specified? */
        if (dev == null) {
            Print.logError("Device not specified!");
            return new OrderedSet<String>();
        }
        String acctId = dev.getAccountID();
        String devId  = dev.getDeviceID();

        /* return list */
        return DeviceMessage.getMessageIDsForDevice(acctId, devId, pendingOnly, limit);

    }

    /**
    *** Returns a list of MessageIDs owned by the specified Account/Device.
    *** Does not return null
    **/
    public static OrderedSet<String> getMessageIDsForDevice(String acctId, String devId, boolean pendingOnly, long limit)
        throws DBException
    {

        /* no account specified? */
        if (StringTools.isBlank(acctId)) {
            Print.logError("Account not specified!");
            return new OrderedSet<String>();
        }

        /* no device specified? */
        if (StringTools.isBlank(devId)) {
            Print.logError("Device not specified!");
            return new OrderedSet<String>();
        }

        /* read messages for account */
        OrderedSet<String> msgList = new OrderedSet<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM DeviceMessage WHERE (accountID='acct' and deviceID='dev') ORDER BY messageID
            DBSelect<DeviceMessage> dsel = new DBSelect<DeviceMessage>(DeviceMessage.getFactory());
            dsel.setSelectedFields(DeviceMessage.FLD_messageID);
            DBWhere dwh = dsel.createDBWhere();
            if (pendingOnly) {
                // -- non-Acknowledged only
                dsel.setWhere(dwh.WHERE(
                    dwh.AND(
                        dwh.EQ(DeviceMessage.FLD_accountID   , acctId),
                        dwh.EQ(DeviceMessage.FLD_deviceID    , devId),
                        dwh.EQ(DeviceMessage.FLD_ackTimestamp, 0L) // non-acknowledged
                    )
                ));
            } else {
                // -- all messages
                dsel.setWhere(dwh.WHERE(
                    dwh.AND(
                        dwh.EQ(DeviceMessage.FLD_accountID   , acctId),
                        dwh.EQ(DeviceMessage.FLD_deviceID    , devId)
                    )
                ));
            }
            dsel.setOrderByFields(DeviceMessage.FLD_messageID);
            dsel.setLimit(limit);

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String msgId = rs.getString(DeviceMessage.FLD_messageID);
                msgList.add(msgId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting DeviceMessage List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return msgList;

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified Account/Device has pending messages.
    **/
    public static boolean hasPendingMessagesForDevice(Device dev)
        throws DBException
    {
        DeviceMessage dm[] = DeviceMessage.getPendingMessagesForDevice(dev, 1L/*limit*/);
        return !ListTools.isEmpty(dm)? true : false;
    }

    /**
    *** Returns a list of pending Messages owned by the specified Account/Device.
    *** Will return null if there are no pending messages.
    **/
    public static DeviceMessage[] getPendingMessagesForDevice(Device dev, long limit)
        throws DBException
    {
        return DeviceMessage.getMessagesForDevice(dev, 
            0L/*lastUpdTS*/, true/*pendingOnly*/, true/*nonExpired*/, limit);
    }

    /**
    *** Returns a list of Messages owned by the specified Account/Device.
    *** Will return null if there are no pending messages.
    **/
    public static DeviceMessage[] getMessagesForDevice(Device dev, 
        long lastUpdTS, boolean pendingOnly, boolean nonExpiredOnly, long limit)
        throws DBException
    {

        /* no device specified? */
        if (dev == null) {
            Print.logError("Device not specified!");
            return null;
        }
        String acctId = dev.getAccountID();
        String devId  = dev.getDeviceID();

        /* select */
        // DBSelect: SELECT * FROM DeviceMessage WHERE (accountID='acct' and deviceID='dev') ORDER BY messageID
        DBSelect<DeviceMessage> dsel = new DBSelect<DeviceMessage>(DeviceMessage.getFactory());
        // dsel.setSelectedFields(DeviceMessage.FLD_messageID); <== select all
        DBWhere dwh = dsel.createDBWhere();
        // -- equal to account/device
        dwh.append(         dwh.EQ(DeviceMessage.FLD_accountID, acctId));
        dwh.append(dwh.AND_(dwh.EQ(DeviceMessage.FLD_deviceID , devId )));
        // -- non-expired?
        if (nonExpiredOnly) {
            long nowTimeSec = DateTime.getCurrentTimeSec();
            dwh.append(dwh.AND_(dwh.GT(DeviceMessage.FLD_expireTimestamp,nowTimeSec)));
        }
        // -- changed messages only
        if (lastUpdTS > 0L) {
            dwh.append(dwh.AND_(dwh.GT(DeviceMessage.FLD_lastUpdateTime,lastUpdTS)));
        }
        // -- non-acknowledged messages
        if (pendingOnly) {
            dwh.append(dwh.AND_(dwh.LE(DeviceMessage.FLD_ackTimestamp,0L))); // non-acknowledged
        }
        // -- init DBSelect
        dsel.setWhere(dwh.WHERE(dwh.toString()));
        dsel.setOrderByFields(DeviceMessage.FLD_queueTimestamp);
        dsel.setOrderAscending(false); // descending (reverse order)
        if (limit == 0L) {
            // -- set limit to "1" so we know that there is at least 1 match
            dsel.setLimit(1L);
        } else 
        if (limit > 0L) {
            // -- set specified limit
            dsel.setLimit(limit);
        }

        /* get DeviceMessages */
        DeviceMessage dma[] = null;
        try {
            DBProvider.lockTables(new String[] { TABLE_NAME() }, null);
            dma = DBRecord.select(dsel); // select:DBSelect
        } catch (DBException dbe) {
            Print.logError("DeviceMessage error: " + dbe);
            throw dbe;
        } finally {
            try {
                DBProvider.unlockTables();
            } catch (DBException dbe) {
                // ignore
            }
        }

        /* "0" limit? */
        if ((limit == 0) && (ListTools.size(dma) > 0L)) {
            // -- we found at least one match, but we've specified limit=0
            return new DeviceMessage[0];
        }

        /* set Device instance */
        if (dma != null) {
            for (DeviceMessage dm : dma) {
                dm.setDevice(dev);
            }
        }

        /* return list */
        return !ListTools.isEmpty(dma)? dma : null;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below

    private static final String ARG_ACCOUNT[]       = new String[] { "account"   , "acct"  , "a" };
    private static final String ARG_DEVICE[]        = new String[] { "device"    , "dev"   , "d" };
    private static final String ARG_MSGTYPE[]       = new String[] { "msgType"   , "messageType" };
    private static final String ARG_PENDING[]       = new String[] { "pending"                   };
    private static final String ARG_LIST[]          = new String[] { "list"                      };
    private static final String ARG_DELETE[]        = new String[] { "delete"                    };
    private static final String ARG_CREATE[]        = new String[] { "create"    , "message"     };
    private static final String ARG_SENDER[]        = new String[] { "sender"    , "from"        };
    private static final String ARG_TITLE[]         = new String[] { "title"                     };

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + DeviceMessage.class.getName() + " {options}");
        Print.logInfo("Options:");
        Print.logInfo("  -account=<id>   Account ID owning Device");
        Print.logInfo("  -device=<id>    Device ID owning message");
        System.exit(1);
    }
    
    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main
        String acctID  = RTConfig.getString(ARG_ACCOUNT, "");
        String devID   = RTConfig.getString(ARG_DEVICE , "");

        /* get account */
        Account account = null;
        try {
            account = Account.getAccount(acctID); // may throw DBException
            if (account == null) {
                Print.logError("Account-ID does not exist: " + acctID);
                usage();
            }
        } catch (DBException dbe) {
            Print.logException("Error loading Account: " + acctID, dbe);
            dbe.printException();
            System.exit(99);
        }

        /* get device */
        Device device = null;
        try {
            device = Device.getDevice(account, devID, false); // may throw DBException
            if (device == null) {
                Print.logError("Device-ID does not exist: " + acctID + "/" + devID);
                usage();
            }
        } catch (DBException dbe) {
            Print.logException("Error getting Device: " + acctID + "/" + devID, dbe);
            System.exit(99);
        }

        /* list pending messages */
        if (RTConfig.hasProperty(ARG_PENDING)) {
            Print.sysPrintln("Pending messages: ");
            try {
                DeviceMessage dma[] = DeviceMessage.getPendingMessagesForDevice(device,-1L);
                for (DeviceMessage dm : dma) {
                    String msgID   = dm.getMessageID();
                    String msg     = dm.getMessage();
                    long   queTime = dm.getQueueTimestamp();
                    StringBuffer sb = new StringBuffer();
                    sb.append("MsgID=").append(msgID).append(" ");
                    sb.append("QTime=").append(queTime).append(" ");
                    sb.append("Msg=").append(msg);
                    Print.sysPrintln(sb.toString());
                }
            } catch (DBException dbe) {
                Print.logError("Error listing DeviceMessage: " + acctID + "/" + devID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* list all messages */
        if (RTConfig.hasProperty(ARG_LIST)) {
            Print.sysPrintln("All messages: ");
            try {
                DeviceMessage dma[] = DeviceMessage.getMessagesForDevice(device,
                    0L/*lastUpdTS*/, false/*pendingOnly?*/, false/*nonExpired?*/, -1L/*limit*/);
                for (DeviceMessage dm : dma) {
                    long   nowTime  = DateTime.getCurrentTimeSec();
                    String msgID    = dm.getMessageID();
                    String msg      = dm.getMessage();
                    long   queTime  = dm.getQueueTimestamp();
                    long   expTime  = dm.getExpireTimestamp();
                    long   sentTime = dm.getSentTimestamp();
                    long   readTime = dm.getReadTimestamp();
                    long   ackTime  = dm.getAckTimestamp();
                    GeoPoint ackLoc = dm.getAckLocation();
                    String ackResp  = dm.getAckResponse();
                    StringBuffer sb = new StringBuffer();
                    sb.append("MsgID=").append(msgID).append(" ");
                    sb.append("QTime=").append(queTime).append(" ");
                    sb.append("XTime=").append(expTime).append(" ");
                    sb.append("STime=").append(sentTime).append(" ");
                    sb.append("RTime=").append(readTime).append(" ");
                    sb.append("ATime=").append(ackTime).append(" ");
                    sb.append("ALoc=" ).append((ackLoc!=null)?ackLoc.toString():"n/a").append(" ");
                    sb.append("Msg="  ).append(msg).append(" ");
                    sb.append("Resp=" ).append(ackResp);
                    Print.sysPrintln(sb.toString());
                }
            } catch (DBException dbe) {
                Print.logError("Error listing DeviceMessage: " + acctID + "/" + devID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* delete */
        if (RTConfig.hasProperty(ARG_DELETE)) {
            String msgID = RTConfig.getString(ARG_DELETE,"");
            try {
                DeviceMessage.Key dmKey = new DeviceMessage.Key(acctID, devID, msgID);
                dmKey.delete(true);
                Print.logInfo("DeviceMessage deleted: " + acctID + "/" + devID);
            } catch (DBException dbe) {
                Print.logError("Error deleting DeviceMessage: " + acctID + "/" + devID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* create message */
        // bin/admin.pl DeviceMessage -a=demo -d=demo -create=HelloWorld -msgType=1 -title=WorldMessage -sender=me
        if (RTConfig.hasProperty(ARG_CREATE)) {
            String msgID  = String.valueOf(DateTime.getCurrentTimeMillis() / 100L); // auto created
            String sender = RTConfig.getString(ARG_SENDER,null);
            String title  = RTConfig.getString(ARG_TITLE,null);
            String msg    = RTConfig.getString(ARG_CREATE,null);
            int    ackTp  = RTConfig.getInt(ARG_MSGTYPE,MessageType.NONE.getIntValue());
            if (StringTools.isBlank(msg)) {
                Print.sysPrintln("Error: Message is blank");
                System.exit(99);
            }
            try {
                long nowTS = DateTime.getCurrentTimeSec();
                DeviceMessage dm = DeviceMessage.getDeviceMessage(device, msgID, true/*create*/);
                dm.setMessageSender(sender);
                dm.setMessageTitle(title);
                dm.setMessage(msg);
                dm.setMessageType(ackTp);
                dm.setPriority(1);
                dm.setQueueTimestamp(nowTS);
                dm.setExpireTimestamp(nowTS + DateTime.DaySeconds(DEFAULT_EXPIRATION_DAYS));
                dm.save();
            } catch (DBException dbe) {
                Print.logError("Error creating DeviceMessage: " + acctID + "/" + devID);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

    }

}
