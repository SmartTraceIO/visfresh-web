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
//  2007/01/25  Martin D. Flynn
//     -Initial release
//  2007/07/14  Martin D. Flynn
//     -Moved to 'org.opengts.opt.db.tables'.
//  2007/11/28  Martin D. Flynn
//     -Added command-line admin tools
//     -Added fields 'deviceOdomKM' and 'address'
//     -Added '-editall' command-line option to display all fields.
//  2007/12/13  Martin D. Flynn
//     -Added check for 'reasonable' trailer mileage on detach
//  2008/03/28  Martin D. Flynn
//     -Incorporate "DBRecord.select(DBSelect,...) method
//  2009/07/01  Martin D. Flynn
//     -Repackaged
//  2011/05/13  Martin D. Flynn
//     -Changed all 'entityType' references to 'long' (to match column type).
//     -Added additional "connect"/"disconnect" status codes.
//  2012/10/16  Martin D. Flynn
//     -Changed to support multiple Entities attached/detached
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

public class Entity
    extends AccountRecord<Entity>
{
    
    // ------------------------------------------------------------------------

    /* "Entity" title (ie. "Entity", "Trailer", etc) */
    public static String[] GetTitles(Locale loc) 
    {
        I18N i18n = I18N.getI18N(Entity.class, loc);
        return new String[] {
            i18n.getString("Entity.title.singular", "Trailer"),
            i18n.getString("Entity.title.plural"  , "Trailers"),
        };
    }

    // ------------------------------------------------------------------------

    // maximum reasonable odometer value for a trailer
    // TODO: this value should be entity dependent
    public  static final double MAX_ENTITY_ODOM_KM      = 10000.0 * GeoPoint.KILOMETERS_PER_MILE;

    public  static       int    AddressColumnLength     = -1;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // SQL table definition below

    /* table name */
    public static final String _TABLE_NAME              = "Entity";
    public static String TABLE_NAME() { return DBProvider._preTranslateTableName(_TABLE_NAME); }

    /* field definition */
    public static final String FLD_entityID             = "entityID";
    public static final String FLD_entityType           = "entityType";         // entity type
    public static final String FLD_timestamp            = "timestamp";          // last timestamp
    public static final String FLD_statusCode           = "statusCode";         // last event status code
    public static final String FLD_isAttached           = "isAttached";         // true if hooked
    public static final String FLD_deviceID             = Device.FLD_deviceID;  // last device/asset attached to
    public static final String FLD_deviceOdomKM         = "deviceOdomKM";       // last device/asset odometer
    public static final String FLD_latitude             = "latitude";           // last known latitude
    public static final String FLD_longitude            = "longitude";          // last known longitude
    public static final String FLD_altitude             = "altitude";           // last known altitude meters
    public static final String FLD_address              = "address";            // last known address
    public static final String FLD_odometerKM           = "odometerKM";         // entity accumulated odometer
    private static DBField FieldInfo[] = {
        // Entity fields
        AccountRecord.newField_accountID(true),
        new DBField(FLD_entityID        , String.class  , DBField.TYPE_ENTITY_ID() , "Entity ID"        , "key=true"),
        new DBField(FLD_entityType      , Long.TYPE     , DBField.TYPE_UINT32      , "Entity Type"      , "key=true"),
        new DBField(FLD_timestamp       , Long.TYPE     , DBField.TYPE_UINT32      , "Timestamp"        , null),
        new DBField(FLD_statusCode      , Integer.TYPE  , DBField.TYPE_UINT32      , "Status Code"      , "format=X2"),
        new DBField(FLD_isAttached      , Boolean.TYPE  , DBField.TYPE_BOOLEAN     , "Is Attached"      , "edit=2"),
        DeviceRecord.newField_deviceID(false,null,"Attached_Device/Asset_ID"),
        new DBField(FLD_deviceOdomKM    , Double.TYPE   , DBField.TYPE_DOUBLE      , "Asset Odometer KM", "edit=2 format=#0.0"),
        new DBField(FLD_latitude        , Double.TYPE   , DBField.TYPE_DOUBLE      , "Latitude"         , "edit=2 format=#0.00000"),
        new DBField(FLD_longitude       , Double.TYPE   , DBField.TYPE_DOUBLE      , "Longitude"        , "edit=2 format=#0.00000"),
        new DBField(FLD_altitude        , Double.TYPE   , DBField.TYPE_DOUBLE      , "Altitude Meters"  , "edit=2 format=#0.0"),
        new DBField(FLD_address         , String.class  , DBField.TYPE_ADDRESS()   , "Address"          , "edit=2 utf8=true"),
        new DBField(FLD_odometerKM      , Double.TYPE   , DBField.TYPE_DOUBLE      , "Odometer KM"      , "edit=2 format=#0.0"),
        // Common fields
        newField_description(),
        newField_lastUpdateTime(),
        newField_creationTime(),
    };

    /* key class */
    public static class Key
        extends AccountKey<Entity>
    {
        public Key() {
            super();
        }
        public Key(String accountId, String entityId, long entityType) {
            super.setKeyValue(FLD_accountID , ((accountId != null)? accountId.toLowerCase() : ""));
            super.setKeyValue(FLD_entityID  , ((entityId  != null)? entityId .toLowerCase() : ""));
            super.setKeyValue(FLD_entityType, entityType);
        }
        public DBFactory<Entity> getFactory() {
            return Entity.getFactory();
        }
    }

    /* factory constructor */
    private static DBFactory<Entity> factory = null;
    public static DBFactory<Entity> getFactory()
    {
        if (factory == null) {
            factory = DBFactory.createDBFactory(
                Entity.TABLE_NAME(), 
                Entity.FieldInfo, 
                DBFactory.KeyType.PRIMARY,
                Entity.class, 
                Entity.Key.class,
                true/*editable*/, true/*viewable*/);
            factory.addParentTable(Account.TABLE_NAME());
            Entity.initEntityManager();
            DBField addrFld = factory.getField(FLD_address);
            Entity.AddressColumnLength = (addrFld != null)? addrFld.getStringLength() : 0;
            //Print.logInfo("Entity "+FLD_address+" length = " + Entity.AddressColumnLength);
        }
        return factory;
    }

    /* Bean instance */
    public Entity()
    {
        super();
    }

    /* database record */
    public Entity(Entity.Key key)
    {
        super(key);
    }
    
    // ------------------------------------------------------------------------

    /* table description */
    public static String getTableDescription(Locale loc)
    {
        I18N i18n = I18N.getI18N(Entity.class, loc);
        return i18n.getString("Entity.description", 
            "This table contains " + 
            "Account specific 'Entity' (ie. trailer drop/hook, etc) information."
            );
    }

    // SQL table definition above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Bean access fields below
        
    public String getEntityID()
    {
        String v = (String)this.getFieldValue(FLD_entityID);
        return (v != null)? v : "";
    }
    
    private void setEntityID(String v)
    {
        this.setFieldValue(FLD_entityID, ((v != null)? v : ""));
    }
    
    // ------------------------------------------------------------------------

    /* return the entity type (default EntityType.DEFAULT) */
    public long getEntityType()
    {
        Long v = (Long)this.getFieldValue(FLD_entityType);
        return (v != null)? v.longValue() : (long)EnumTools.getDefault(EntityManager.EntityType.class).getIntValue();
    }

    /* set the entity type */
    public void setEntityType(long v)
    {
        int vi = (int)v;
        this.setFieldValue(FLD_entityType, (long)EnumTools.getValueOf(EntityManager.EntityType.class,vi).getIntValue());
    }

    /* set the entity type */
    public void setEntityType(EntityManager.EntityType v)
    {
        this.setFieldValue(FLD_entityType, (long)EnumTools.getValueOf(EntityManager.EntityType.class,v).getIntValue());
    }

    /* set the entity type */
    public void setEntityType(String v, Locale locale)
    {
        this.setFieldValue(FLD_entityType, (long)EnumTools.getValueOf(EntityManager.EntityType.class,v,locale).getIntValue());
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

    public String getTimestampString()
    {
        Account a = this.getAccount();
        String dateFmt = (a != null)? a.getDateFormat() : BasicPrivateLabel.getDefaultDateFormat();
        String timeFmt = (a != null)? a.getTimeFormat() : BasicPrivateLabel.getDefaultTimeFormat();
        DateTime dt = new DateTime(this.getTimestamp());
        return dt.gmtFormat(dateFmt + " " + timeFmt + " z");
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the last event status code for this entity
    *** @return The last event status code for this entity
    **/
    public int getStatusCode()
    {
        return this.getFieldValue(FLD_statusCode, 0);
    }

    /**
    *** Sets the last event status code for this entity
    *** @param v The last event status code for this entity
    **/
    public void setStatusCode(int v)
    {
        this.setFieldValue(FLD_statusCode, v);
    }

    // ------------------------------------------------------------------------

    public boolean getIsAttached()
    {
        Boolean v = (Boolean)this.getFieldValue(FLD_isAttached);
        return (v != null)? v.booleanValue() : false;
    }

    public void setIsAttached(boolean v)
    {
        this.setFieldValue(FLD_isAttached, v);
    }
    
    public boolean isAttached()
    {
        return this.getIsAttached();
    }

    // ------------------------------------------------------------------------
        
    public String getDeviceID()
    {
        String v = (String)this.getFieldValue(FLD_deviceID);
        return (v != null)? v : "";
    }
    
    private void setDeviceID(String v)
    {
        this.setFieldValue(FLD_deviceID, ((v != null)? v : ""));
    }

    // ------------------------------------------------------------------------

    public double getLatitude()
    {
        Double v = (Double)this.getFieldValue(FLD_latitude);
        return (v != null)? v.doubleValue() : 0.0;
    }
    public void setLatitude(double v)
    {
        this.setFieldValue(FLD_latitude, v);
    }

    public double getLongitude()
    {
        Double v = (Double)this.getFieldValue(FLD_longitude);
        return (v != null)? v.doubleValue() : 0.0;
    }
    public void setLongitude(double v)
    {
        this.setFieldValue(FLD_longitude, v);
    }

    public GeoPoint getGeoPoint()
    {
        return new GeoPoint(this.getLatitude(), this.getLongitude());
    }
    public void setGeoPoint(double lat, double lng)
    {
        this.setLatitude(lat);
        this.setLongitude(lng);
    }
    public void setGeoPoint(GeoPoint gp)
    {
        if (gp != null) {
            this.setLatitude(gp.getLatitude());
            this.setLongitude(gp.getLongitude());
        } else {
            this.setLatitude(0.0);
            this.setLongitude(0.0);
        }
    }

    // ------------------------------------------------------------------------

    public double getAltitude() // meters
    {
        return this.getFieldValue(FLD_altitude, 0.0);
    }

    public void setAltitude(double v) // meters
    {
        this.setFieldValue(FLD_altitude, v);
    }

    // ------------------------------------------------------------------------

    public String getAddress()
    {
        String v = (String)this.getFieldValue(FLD_address);
        if ((v == null) || v.equals("")) {
            // should we try to go get the reverse-geocode?
            v = ""; // in case it was null
        }
        return v;
    }
    
    public void setAddress(String v)
    {
        String addr = StringTools.trim(v);
        if ((Entity.AddressColumnLength > 0)             &&
            (addr.length() >= Entity.AddressColumnLength)  ) {
            // -1 so we are not so close to the edge of the cliff
            int newLen = Entity.AddressColumnLength - 1; 
            addr = addr.substring(0, newLen).trim();
            // Note: MySQL will refuse to insert the record if the data length
            // is greater than the table column length.
        }
        this.setFieldValue(FLD_address, addr);
    }

    // ------------------------------------------------------------------------

    public double getDeviceOdomKM()
    {
        Double v = (Double)this.getFieldValue(FLD_deviceOdomKM);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setDeviceOdomKM(double v)
    {
        this.setFieldValue(FLD_deviceOdomKM, v);
    }

    // ------------------------------------------------------------------------

    public double getOdometerKM()
    {
        Double v = (Double)this.getFieldValue(FLD_odometerKM);
        return (v != null)? v.doubleValue() : 0.0;
    }
    
    public void setOdometerKM(double v)
    {
        this.setFieldValue(FLD_odometerKM, v);
    }
    
    public void addDeltaOdometerKM(double deltaKM)
    {
        if (deltaKM > 0.0) {
            double accumKM = this.getOdometerKM() + deltaKM;
            this.setOdometerKM(accumKM);
        }
    }

    // Bean access fields above
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
 
    /* debug: string representation of instance */
    public String toString()
    {
        return this.getAccountID() + "/" + this.getEntityID();
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
    // EntityManager interface support

    private static EntityManager entityManager = null;

    private static void initEntityManager()
    {
        if (Entity.entityManager == null) {
            //Print.logInfo("Initializing EntityManager ...");
            Entity.entityManager = new EntityManager() {
                public boolean insertEntityChange(EventData event) {
                    try { 
                        return Entity.insertEntityChange(event); 
                    } catch (Throwable th) {
                        // catch any/all stray exceptions
                        Print.logError("Error inserting entity change: " + th);
                        return false;
                    }
                }
                public String[] getAttachedEntityIDs(String accountID, String deviceID, long entityType) 
                    throws DBException {
                    return Entity.getAttachedEntityIDs(accountID, deviceID, entityType);
                }
                public String[] getAttachedEntityDescriptions(String accountID, String deviceID, long entityType) 
                    throws DBException {
                    return Entity.getAttachedEntityDescriptions(accountID, deviceID, entityType);
                }
                public String getEntityDescription(String accountID, String entityID, long entityType) {
                    return Entity.getEntityDescription(accountID, entityID, entityType);
                }
                public boolean isEntityAttached(String accountID, String deviceID, String entityID, long entityType) {
                    return Entity.isEntityAttached(accountID, deviceID, entityID, entityType);
                }
            };
            Device.setEntityManager(Entity.entityManager);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Entities attached to a specific Device

    /* get entities currently attached to deviceID */
    public static String[] getAttachedEntityIDs(String accountID, String deviceID, long entityType)
        throws DBException
    {
        Entity e[] = Entity.getAttachedEntities(accountID, deviceID, entityType, null);
        if (ListTools.isEmpty(e)) {
            return new String[0];
        } else {
            String id[] = new String[e.length];
            for (int i = 0; i < e.length; i++) {
                id[i] = e[i].getEntityID();
            }
            return id;
        }
    }

    /* get entities currently attached to deviceID */
    public static String[] getAttachedEntityDescriptions(String accountID, String deviceID, long entityType)
        throws DBException
    {
        Entity e[] = Entity.getAttachedEntities(accountID, deviceID, entityType, null);
        if (ListTools.isEmpty(e)) {
            return new String[0];
        } else {
            String desc[] = new String[e.length];
            for (int i = 0; i < e.length; i++) {
                String d = e[i].getDescription();
                desc[i] = ((d != null) && !d.equals(""))? d : e[i].getEntityID();
            }
            return desc;
        }
    }

    /* get entities currently attached to deviceID */
    public static Entity[] getAttachedEntities(String accountID, String deviceID, long entityType)
        throws DBException
    {
        return Entity.getAttachedEntities(accountID, deviceID, entityType, null);
    }

    /* get entities currently attached to deviceID */
    public static Entity[] getAttachedEntities(String accountID, String deviceID, long entityType,
        DBRecordHandler<Entity> handler)
        throws DBException
    {

        /* select Entity */
        // DBSelect: SELECT * FROM Entity WHERE (accountID='acct') ORDER BY entityID
        DBSelect<Entity> esel = new DBSelect<Entity>(Entity.getFactory());
        DBWhere ewh = esel.createDBWhere();
        esel.setWhere(
            ewh.WHERE(
                ewh.AND(
                    ewh.EQ(Entity.FLD_accountID , accountID),
                    ewh.EQ(Entity.FLD_isAttached, true),
                    ewh.EQ(Entity.FLD_deviceID  , deviceID),
                    ewh.EQ(Entity.FLD_entityType, entityType)
                )
            )
        );
        esel.setOrderByFields(Entity.FLD_entityID);
        esel.setLimit(50);

        /* get Entities */
        //return (Entity[])DBRecord.select(Entity.getFactory(), esel.toString(false), handler);
        return DBRecord.select(esel, handler);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Entity attach/detach handler

    /* apply event to an entity 'attach/detach' */
    public static boolean insertEntityChange(EventData event)
    {

        /* no event? */
        if (event == null) {
            return false;
        }

        /* work vars */
        int  statusCode = event.getStatusCode();
        Account account = event.getAccount();

        /* RFID Entity */
        if (statusCode == StatusCodes.STATUS_RFID_CONNECT) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.RFID_00);
            String rfidTag = event.getRfidTag();
            if (entityType.isRFID()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._attach(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected RFID Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._attach(account, entIDs, EntityManager.EntityType.RFID_00, event);
            } else {
                Print.logError("Expected RFID Entity type for status code: " + entityType);
                return false;
            }
        } else
        if (statusCode == StatusCodes.STATUS_RFID_DISCONNECT) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.RFID_00);
            String rfidTag = event.getRfidTag();
            if (entityType.isRFID()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._detach(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected RFID Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._detach(account, entIDs, EntityManager.EntityType.RFID_00, event);
            } else {
                Print.logError("Expected RFID Entity type for status code: " + entityType);
                return false;
            }
        } else
        if (statusCode == StatusCodes.STATUS_RFID_INVENTORY) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.RFID_00);
            String rfidTag = event.getRfidTag();
            if (entityType.isRFID()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._inventory(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected RFID Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._inventory(account, entIDs, EntityManager.EntityType.RFID_00, event);
            } else {
                Print.logError("Expected RFID Entity type for status code: " + entityType);
                return false;
            }
        } 

        /* Trailer Entity */
        if (statusCode == StatusCodes.STATUS_TRAILER_HOOK) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.TRAILER);
            String rfidTag = event.getRfidTag();
            if (entityType.isTrailer()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._attach(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected TRAILER Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._attach(account, entIDs, EntityManager.EntityType.TRAILER, event);
            } else {
                Print.logError("Expected TRAILER Entity type for status code: " + entityType);
                return false;
            }
        } else
        if (statusCode == StatusCodes.STATUS_TRAILER_UNHOOK) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.TRAILER);
            String rfidTag = event.getRfidTag();
            if (entityType.isTrailer()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._detach(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected TRAILER Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._detach(account, entIDs, EntityManager.EntityType.TRAILER, event);
            } else {
                Print.logError("Expected TRAILER Entity type for status code: " + entityType);
                return false;
            }
        } else
        if (statusCode == StatusCodes.STATUS_TRAILER_INVENTORY) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.TRAILER);
            String rfidTag = event.getRfidTag();
            if (entityType.isTrailer()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._inventory(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected TRAILER Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._inventory(account, entIDs, EntityManager.EntityType.TRAILER, event);
            } else {
                Print.logError("Expected TRAILER Entity type for status code: " + entityType);
                return false;
            }
        }

        /* Person/Passenger Entity */
        if (statusCode == StatusCodes.STATUS_PERSON_ENTER) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.PERSON);
            String rfidTag = event.getRfidTag();
            if (entityType.isPerson()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._attach(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected PERSON Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._attach(account, entIDs, EntityManager.EntityType.PERSON, event);
            } else {
                Print.logError("Expected PERSON Entity type for status code: " + entityType);
                return false;
            }
        } else
        if (statusCode == StatusCodes.STATUS_PERSON_EXIT) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.PERSON);
            String rfidTag = event.getRfidTag();
            if (entityType.isPerson()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._detach(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected PERSON Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._detach(account, entIDs, EntityManager.EntityType.PERSON, event);
            } else {
                Print.logError("Expected PERSON Entity type for status code: " + entityType);
                return false;
            }
        } else
        if (statusCode == StatusCodes.STATUS_PERSON_INVENTORY) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.PERSON);
            String rfidTag = event.getRfidTag();
            if (entityType.isPerson()) {
                String entIDs[] = event.getEntityIDList(rfidTag);
                return Entity._inventory(account, entIDs, entityType, event);
            } else
            if (!StringTools.isBlank(rfidTag)) {
                Print.logWarn("Expected PERSON Entity type for status code: " + entityType);
                String entIDs[] = new String[] { rfidTag };
                return Entity._inventory(account, entIDs, EntityManager.EntityType.PERSON, event);
            } else {
                Print.logError("Expected PERSON Entity type for status code: " + entityType);
                return false;
            }
        } 

        /* General RFID/Entity */
        if (statusCode == StatusCodes.STATUS_ENTITY_CONNECT) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.GENERAL);
            String rfidTag  = event.getRfidTag();
            String entIDs[] = event.getEntityIDList(rfidTag);
            return Entity._attach(account, entIDs, entityType, event);
        } else
        if (statusCode == StatusCodes.STATUS_ENTITY_DISCONNECT) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.GENERAL);
            String rfidTag  = event.getRfidTag();
            String entIDs[] = event.getEntityIDList(rfidTag);
            return Entity._detach(account, entIDs, entityType, event);
        } else
        if (statusCode == StatusCodes.STATUS_ENTITY_INVENTORY) {
            EntityManager.EntityType entityType = event._getEntityType(EntityManager.EntityType.GENERAL);
            String rfidTag  = event.getRfidTag();
            String entIDs[] = event.getEntityIDList(rfidTag);
            return Entity._inventory(account, entIDs, entityType, event);
        }

        /* Driver Entity */
        if (statusCode == StatusCodes.STATUS_LOGIN) {
            EntityManager.EntityType entityType = EntityManager.EntityType.DRIVER;
            String entIDs[] = new String[] { event.getDriverID() };
            return Entity._attach(account, entIDs, entityType, event);
        } else
        if (statusCode == StatusCodes.STATUS_LOGOUT) {
            EntityManager.EntityType entityType = EntityManager.EntityType.DRIVER;
            String entIDs[] = new String[] { event.getDriverID() };
            return Entity._detach(account, entIDs, entityType, event);
        } 

        /* no attach/detach processed */
        return false;

    }

    /* attach entity */
    private static boolean _attach(Account account, String entIDList[], EntityManager.EntityType entityType, EventData evdb)
    {
        //Print.logInfo("Entity attach check ...:" + StringTools.join(entIDList,","));

        /* validate */
        if (account == null) {
            Print.logWarn("Skipping Entity attach due to null Account");
            return false;
        } else
        if (evdb == null) {
            Print.logWarn("Skipping Entity attach due to null EventData");
            return false;
        } else
        if (ListTools.isEmpty(entIDList)) {
            Print.logWarn("Skipping Entity attach due to empty EntityID list");
            return false;
        } else
        if (entityType == null) {
            Print.logWarn("Skipping Entity detach due to null EntityType");
            return false;            
        }
        // we will assume that "account.getAccountID().equals(evdb.getAccountID())" is true.

        /* work vars */
        String evAcctID = account.getAccountID();
        String evDevID  = evdb.getDeviceID();
        String devName  = evAcctID + "/" + evDevID;

        /* attach: loop through entities */
        for (String entityID : entIDList) {
            if (StringTools.isBlank(entityID)) { continue; } // quietly ignore blank entries
            String entName = evAcctID + "/" + entityID + "/" + entityType;
            try {
                // get last Entity
                Entity entity   = Entity.getEntity(account, entityID, (long)entityType.getIntValue(), true); // create if non-existent
                String entDevID = entity.getDeviceID();
                if (entity.getIsAttached()) {
                    Print.logWarn("Missing last 'detach' for entity: "+entName + " [attached to "+entDevID+"]");
                    // already attached, continue anyway
                }
                // update Entity
                entity.setIsAttached(true);
                entity.setTimestamp(evdb.getTimestamp());
                entity.setStatusCode(evdb.getStatusCode());
                entity.setDeviceID(evDevID);
                entity.setDeviceOdomKM(evdb.getOdometerKM());
                entity.setLatitude(evdb.getLatitude());
                entity.setLongitude(evdb.getLongitude());
                entity.setAltitude(evdb.getAltitude());
                entity.setAddress(evdb.getAddress());
                // insert/update
                entity.save();
                Print.logInfo("Entity attached to device '" + devName + "': " + entName);
                continue;
            } catch (DBException dbe) {
                Print.logException("Unable to attach Entity " + entName, dbe);
                return false;
            }
        }

        /* success */
        return true;

    }

    /* detach entity */
    private static boolean _detach(Account account, String entIDList[], EntityManager.EntityType entityType, EventData evdb)
    {
        //Print.logInfo("Entity detach check ...:" + StringTools.join(entIDList,","));

        /* validate */
        if (account == null) {
            Print.logWarn("Skipping Entity detach due to null Account");
            return false;
        } else
        if (evdb == null) {
            Print.logWarn("Skipping Entity detach due to null EventData");
            return false;
        } else
        if (ListTools.isEmpty(entIDList)) {
            Print.logWarn("Skipping Entity detach due to empty EntityID list");
            return false;
        } else
        if (entityType == null) {
            Print.logWarn("Skipping Entity detach due to null EntityType");
            return false;            
        }
        // we will assume that "account.getAccountID().equals(evdb.getAccountID())" is true.

        /* work vars */
        String evAcctID = account.getAccountID();
        String evDevID  = evdb.getDeviceID();
        String devName  = evAcctID + "/" + evDevID;

        /* detach: loop through entities */
        for (String entityID : entIDList) {
            if (StringTools.isBlank(entityID)) { continue; } // quietly ignore blank entries
            String entName = evAcctID + "/" + entityID + "/" + entityType;
            try {
                // get last Entity
                Entity entity   = Entity.getEntity(account, entityID, (long)entityType.getIntValue(), true);
                String entDevID = entity.getDeviceID();
                // get distance traveled since last 'attach'
                if (!entDevID.equals(evDevID)) {
                    Print.logWarn("Last 'attach' Device[" + entDevID + "] does not match this 'detach' Device[" + devName + "]");
                    // Missing previous detach from 'entDevID' and attach to 'evDevID'
                } else
                if (!entity.getIsAttached()) {
                    Print.logWarn("Missing last 'attach' for entity: " + entName);
                    // already detached, continue anyway
                } else {
                    double devOdomKM = entity.getDeviceOdomKM();    // last device 'attach' odometer
                    double evtOdomKM = evdb.getOdometerKM();        // this 'detach' odometer
                    if ((devOdomKM > 0.0) && (evtOdomKM > devOdomKM)) {
                        double deltaKM = evtOdomKM - devOdomKM;
                        if (deltaKM <= Entity.MAX_ENTITY_ODOM_KM) {
                            // odometer is within a reasonable value
                            entity.addDeltaOdometerKM(deltaKM);
                        } else {
                            // odometer value is unreasonable
                            // TODO: search backwards through events to find a reasonable delta?
                            Print.logWarn("Unreasonable trailer delta odometer: " + deltaKM + " [ignored]");
                        }
                    }
                }
                // update Entity
                entity.setIsAttached(false);
                entity.setTimestamp(evdb.getTimestamp());
                entity.setStatusCode(evdb.getStatusCode());
                entity.setDeviceID(evDevID);
                entity.setDeviceOdomKM(evdb.getOdometerKM());
                entity.setLatitude(evdb.getLatitude());
                entity.setLongitude(evdb.getLongitude());
                entity.setAltitude(evdb.getAltitude());
                entity.setAddress(evdb.getAddress());
                // insert/update
                entity.save(); 
                Print.logInfo("Entity detached from device '" + devName + "': " + entName);
                continue;
            } catch (DBException dbe) {
                Print.logException("Unable to detach Entity " + entName, dbe);
                return false;
            }
        }

        /* success */
        return true;

    }

    /* inventory entity */
    private static boolean _inventory(Account account, String entIDList[], EntityManager.EntityType entityType, EventData evdb)
    {
        //Print.logInfo("Entity inventory check ...:" + StringTools.join(entIDList,","));

        /* validate */
        if (account == null) {
            Print.logWarn("Skipping Entity inventory due to null Account");
            return false;
        } else
        if (evdb == null) {
            Print.logWarn("Skipping Entity inventory due to null EventData");
            return false;
        } else
        if (ListTools.isEmpty(entIDList)) {
            Print.logWarn("Skipping Entity inventory due to empty EntityID list");
            return false;
        } else
        if (entityType == null) {
            Print.logWarn("Skipping Entity inventory due to null EntityType");
            return false;            
        }
        // we will assume that "account.getAccountID().equals(evdb.getAccountID())" is true.

        /* get Device record */
        Device device = evdb.getDevice();
        if (device == null) {
            Print.logWarn("Skipping Entity inventory due to null Device");
            return false;
        }

        /* work vars */
        String evAcctID = account.getAccountID();
        String evDevID  = evdb.getDeviceID();
        String devName  = evAcctID + "/" + evDevID;

        /* get list of all currently attached entities */
        Set<String> attEntIDs = ListTools.toSet(device.getAttachedEntityIDs(entityType));

        /* inventory: attach inventory entities not listed in the currently attached list */
        for (String entityID : entIDList) {
            if (StringTools.isBlank(entityID)) { continue; } // quietly ignore blank entries
            if (attEntIDs.contains(entityID)) {
                attEntIDs.remove(entityID);
                continue; // already attached, skip
            }
            String entName = evAcctID + "/" + entityID + "/" + entityType;
            try {
                // get existing Entity
                Entity entity   = Entity.getEntity(account, entityID, (long)entityType.getIntValue(), true); // create if non-existent
                String entDevID = entity.getDeviceID();
                if (entity.getIsAttached()) { // likely not already attached.
                    // already attached, skip (should not occur here since we skipped attached entries above)
                    continue;
                }
                // attach Entity
                entity.setIsAttached(true);
                entity.setTimestamp(evdb.getTimestamp());
                entity.setStatusCode(evdb.getStatusCode());
                entity.setDeviceID(evDevID);
                entity.setDeviceOdomKM(evdb.getOdometerKM());
                entity.setLatitude(evdb.getLatitude());
                entity.setLongitude(evdb.getLongitude());
                entity.setAltitude(evdb.getAltitude());
                entity.setAddress(evdb.getAddress());
                // insert/update
                entity.save();
                Print.logInfo("Entity attached to device '" + devName + "': " + entName);
                continue;
            } catch (DBException dbe) {
                Print.logException("Unable to attach Entity " + entName, dbe);
                return false;
            }
        }

        /* inventory: detach remaining attached entities (if any) */
        for (String entityID : attEntIDs) {
            if (StringTools.isBlank(entityID)) { continue; } // quietly ignore blank entries
            String entName = evAcctID + "/" + entityID + "/" + entityType;
            try {
                // get last Entity
                Entity entity = Entity.getEntity(account, entityID, (long)entityType.getIntValue(), false);
                if ((entity == null) || !entity.getIsAttached()) {
                    // already detached (should not occur here
                    continue; // skip
                }
                // attached to this device?
                String entDevID = entity.getDeviceID();
                if (!entDevID.equals(evDevID)) {
                    // missing previous detach from 'entDevID' and attach to 'evDevID' (unlikely)
                    Print.logWarn("Entity [" + entDevID + "] not attached to this Device[" + devName + "]");
                    continue; // skip
                }
                // update odometer
                double devOdomKM = entity.getDeviceOdomKM();    // last device 'attach' odometer
                double evtOdomKM = evdb.getOdometerKM();        // this 'detach' odometer
                if ((devOdomKM > 0.0) && (evtOdomKM > devOdomKM)) {
                    double deltaKM = evtOdomKM - devOdomKM;
                    if (deltaKM <= Entity.MAX_ENTITY_ODOM_KM) {
                        // odometer is within a reasonable value
                        entity.addDeltaOdometerKM(deltaKM);
                    } else {
                        // odometer value is unreasonable
                        // TODO: search backwards through events to find a reasonable delta?
                        Print.logWarn("Unreasonable trailer delta odometer: " + deltaKM + " [ignored]");
                    }
                }
                // detach Entity
                entity.setIsAttached(false);
                entity.setTimestamp(evdb.getTimestamp());
                entity.setStatusCode(evdb.getStatusCode());
                entity.setDeviceID(evDevID);
                entity.setDeviceOdomKM(evdb.getOdometerKM());
                entity.setLatitude(evdb.getLatitude());
                entity.setLongitude(evdb.getLongitude());
                entity.setAltitude(evdb.getAltitude());
                entity.setAddress(evdb.getAddress());
                // insert/update
                entity.save(); 
                Print.logInfo("Entity detached from device '" + devName + "': " + entName);
                continue;
            } catch (DBException dbe) {
                Print.logException("Unable to detach Entity " + entName, dbe);
                return false;
            }
        }

        /* success */
        return true;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // specific Entity retrieval

    /* return the description for the specified entity */
    public static String getEntityDescription(String accountID, String entityID, long entityType)
    {
        if (entityID == null) {
            return "";
        } else
        if (accountID == null) {
            return entityID;
        } else {
            try {
                Entity e = Entity.getEntity(accountID, entityID, entityType);
                String d = (e != null)? e.getDescription() : "";
                return ((d != null) && !d.equals(""))? d : entityID;
            } catch (DBException dbe) {
                Print.logException("Retrieving Entity description", dbe);
                return entityID;
            }
        }
    }

    /* return the description for the specified entity */
    public static boolean isEntityAttached(String accountID, String deviceID, String entityID, long entityType)
    {
        if (entityID == null) {
            return false;
        } else
        if (accountID == null) {
            return false;
        } else {
            try {
                Entity e = Entity.getEntity(accountID, entityID, entityType);
                if (e == null) {
                    return false;
                } else
                if (!e.getIsAttached()) {
                    return false;
                } else
                if ((deviceID != null) && !deviceID.equalsIgnoreCase(e.getDeviceID())) {
                    return false;
                } else {
                    return true;
                }
            } catch (DBException dbe) {
                Print.logException("Retrieving Entity", dbe);
                return false;
            }
        }
    }

    /* Return specified entity (or null if non-existent) */
    public static Entity getEntity(String accountID, String entityID, long entityType)
        throws DBException
    {
        return Entity._getEntity(accountID, null, entityID, entityType, false);
    }

    /* Return specified entity (or null if non-existent) */
    public static Entity getEntity(Account account, String entityID, long entityType)
        throws DBException
    {
        return Entity._getEntity(null, account, entityID, entityType, false);
    }

    /* Return specified entity, create if specified */
    public static Entity getEntity(Account account, String entityID, long entityType, boolean createOK)
        throws DBException
    {
        return Entity._getEntity(null, account, entityID, entityType, createOK);
    }
    
    /* Return specified entity, create if specified */
    private static Entity _getEntity(String accountID, Account account, String entityID, long entityType, boolean createOK)
        throws DBException
    {
        // does not return null if 'createOK' is true

        /* account-id specified? */
        if (accountID == null) {
            if (account == null) {
                throw new DBException("Account not specified.");
            } else {
                accountID = account.getAccountID();
            }
        } else
        if ((account != null) && !account.getAccountID().equals(accountID)) {
            throw new DBException("Account does not match specified AccountID.");
        }

        /* entity-id specified? */
        if (StringTools.isBlank(entityID)) {
            throw new DBException("Entity-ID not specified.");
        }

        /* get/create entity */
        Entity.Key entityKey = new Entity.Key(accountID, entityID, entityType);
        if (entityKey.exists()) { // may throw DBException
            Entity entity = entityKey.getDBRecord(true);
            if (account != null) {
                entity.setAccount(account);
            }
            return entity;
        } else
        if (createOK) {
            Entity entity = entityKey.getDBRecord();
            if (account != null) {
                entity.setAccount(account);
            }
            entity.setCreationDefaultValues();
            return entity; // not yet saved!
        } else {
            // record doesn't exist, and caller doesn't want us to create it
            return null;
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Account owned entities

    /* get entities */
    public static Entity[] getEntitiesForAccount(String accountID)
        throws DBException
    {
        return Entity.getEntitiesForAccount(accountID, -1L, null);
    }

    /* get entities */
    public static Entity[] getEntitiesForAccount(String accountID, long limit)
        throws DBException
    {
        return Entity.getEntitiesForAccount(accountID, limit, null);
    }

    /* get entities */
    public static Entity[] getEntitiesForAccount(String accountID, long limit, DBRecordHandler<Entity> handler)
        throws DBException
    {
        
        /* select Entity */
        // DBSelect: SELECT * FROM Entity WHERE (accountID='acct') ORDER BY entityID
        DBSelect<Entity> esel = new DBSelect<Entity>(Entity.getFactory());
        DBWhere ewh = esel.createDBWhere();
        esel.setWhere(ewh.WHERE(ewh.EQ(Entity.FLD_accountID,accountID)));
        esel.setOrderByFields(Entity.FLD_entityID);
        if (limit > 0L) {
            esel.setLimit(limit);
        }
        
        /* get Entities */
        //return (Entity[])DBRecord.select(Entity.getFactory(), esel.toString(false), handler);
        return DBRecord.select(esel, handler);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Account owned entities

    /* get entities */
    public static OrderedSet<String> getEntityIDsForAccount(String accountID)
        throws DBException
    {
        return Entity.getEntityIDsForAccount(accountID, -1L, null);
    }

    /* get entities */
    public static OrderedSet<String> getEntityIDsForAccount(String accountID, long limit)
        throws DBException
    {
        return Entity.getEntityIDsForAccount(accountID, limit, null);
    }

    /* get entities */
    public static OrderedSet<String> getEntityIDsForAccount(String accountID, long limit, DBRecordHandler<Entity> handler)
        throws DBException
    {

        /* no account specified? */
        if (StringTools.isBlank(accountID)) {
            Print.logError("Account not specified!");
            return new OrderedSet<String>();
        }

        /* read entities for account */
        OrderedSet<String> entList = new OrderedSet<String>();
        DBConnection dbc = null;
        Statement   stmt = null;
        ResultSet     rs = null;
        try {

            /* select */
            // DBSelect: SELECT * FROM Entity WHERE (accountID='acct') ORDER BY entityID
            DBSelect<Entity> dsel = new DBSelect<Entity>(Entity.getFactory());
            dsel.setSelectedFields(Entity.FLD_accountID,Entity.FLD_entityID);
            DBWhere dwh = dsel.createDBWhere();
            dsel.setWhere(dwh.WHERE(dwh.EQ(Entity.FLD_accountID,accountID)));
            dsel.setOrderByFields(Entity.FLD_entityID);
            dsel.setLimit(limit);

            /* get records */
            dbc  = DBConnection.getDefaultConnection();
            stmt = dbc.execute(dsel.toString());
            rs   = stmt.getResultSet();
            while (rs.next()) {
                String entId = rs.getString(Entity.FLD_entityID);
                entList.add(entId);
            }

        } catch (SQLException sqe) {
            throw new DBException("Getting Account Entity List", sqe);
        } finally {
            if (rs   != null) { try { rs.close();   } catch (Throwable t) {} }
            if (stmt != null) { try { stmt.close(); } catch (Throwable t) {} }
            DBConnection.release(dbc);
        }

        /* return list */
        return entList;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Entity administration

    /* return true if Entity exists */
    public static boolean exists(String accountID, String entityID, long entityType)
        throws DBException // if error occurs while testing existance
    {
        if ((accountID != null) && (entityID != null)) {
            Entity.Key entKey = new Entity.Key(accountID, entityID, entityType);
            return entKey.exists();
        }
        return false;
    }

    /* create a new Entity */
    public static Entity createNewEntity(Account account, String entityID, long entityType, String description)
        throws DBException
    {
        if ((account != null) && (entityID != null) && !entityID.equals("")) {
            Entity entity = Entity.getEntity(account, entityID, entityType, true); // does not return null
            if ((description != null) && !description.equals("")) {
                entity.setDescription(description);
            }
            entity.save();
            return entity;
        } else {
            throw new DBException("Invalid Account/EntityID specified");
        }
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Main admin entry point below
    
    private static final String ARG_ACCOUNT[]   = new String[] { "account" , "acct" };
    private static final String ARG_ENTITY[]    = new String[] { "entity"  , "id"   };
    private static final String ARG_TYPE[]      = new String[] { "type"    , "ty"   };
    private static final String ARG_DELETE[]    = new String[] { "delete"           };
    private static final String ARG_CREATE[]    = new String[] { "create"           };
    private static final String ARG_EDIT[]      = new String[] { "edit"    , "ed"   };
    private static final String ARG_EDITALL[]   = new String[] { "editall" , "eda"  };
    private static final String ARG_LIST[]      = new String[] { "list"             };

    private static void usage()
    {
        Print.logInfo("Usage:");
        Print.logInfo("  java ... " + Entity.class.getName() + " {options}");
        Print.logInfo("Options:");
        Print.logInfo("  -account=<id>   Account ID owning Entity");
        Print.logInfo("  -entity=<id>    Entity ID to delete/edit");
        Print.logInfo("  -create         Create a new Entity");
        Print.logInfo("  -edit           To edit an existing Entity");
        Print.logInfo("  -delete         Delete specified Entity");
        System.exit(1);
    }
    
    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main
        String accountID  = RTConfig.getString(ARG_ACCOUNT, "");
        String entityID   = RTConfig.getString(ARG_ENTITY , "");
        long   entityType = RTConfig.getLong  (ARG_TYPE   , 0L);

        /* account-id specified? */
        if ((accountID == null) || accountID.equals("")) {
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

        /* entity-id specified? */
        boolean entitySpecified = ((entityID != null) && !entityID.equals(""));
        //if ((entityID == null) || entityID.equals("")) {
        //    Print.logError("Entity-ID not specified.");
        //    usage();
        //}

        /* entity exists? */
        boolean entityExists = false;
        if (entitySpecified) {
            try {
                entityExists = Entity.exists(accountID, entityID, entityType);
            } catch (DBException dbe) {
                Print.logError("Error determining if Entity exists: " + accountID + "/" + entityID + "/" + entityType);
                System.exit(99);
            }
        }

        /* option count */
        int opts = 0;

        /* delete */
        if (RTConfig.getBoolean(ARG_DELETE,false)) {
            opts++;
            if (!entitySpecified) {
                Print.logWarn("Entity name not specified ...");
                usage();
            } else
            if (!entityExists) {
                Print.logWarn("Entity does not exist: " + accountID + "/" + entityID + "/" + entityType);
                Print.logWarn("Continuing with delete process ...");
            }
            try {
                Entity.Key entKey = new Entity.Key(accountID, entityID, entityType);
                entKey.delete(true); // also delete dependencies (if any)
                Print.logInfo("Entity deleted: " + accountID + "/" + entityID);
                entityExists = false;
            } catch (DBException dbe) {
                Print.logError("Error deleting Entity: " + accountID + "/" + entityID + "/" + entityType);
                dbe.printException();
                System.exit(99);
            }
            System.exit(0);
        }

        /* create */
        if (RTConfig.getBoolean(ARG_CREATE, false)) {
            opts++;
            if (!entitySpecified) {
                Print.logWarn("Entity name not specified ...");
                usage();
            } else
            if (entityExists) {
                Print.logWarn("Entity already exists: " + accountID + "/" + entityID + "/" + entityType);
            } else {
                try {
                    Entity.createNewEntity(account, entityID, entityType, null);
                    Print.logInfo("Created Entity: " + accountID + "/" + entityID);
                    entityExists = true;
                } catch (DBException dbe) {
                    Print.logError("Error creating Entity: " + accountID + "/" + entityID);
                    dbe.printException();
                    System.exit(99);
                }
            }
        }

        /* edit */
        if (RTConfig.getBoolean(ARG_EDIT,false) || RTConfig.getBoolean(ARG_EDITALL,false)) { 
            opts++;
            if (!entitySpecified) {
                Print.logWarn("Entity name not specified ...");
                usage();
            } else
            if (!entityExists) {
                Print.logError("Entity does not exist: " + accountID + "/" + entityID + "/" + entityType);
            } else {
                try {
                    boolean allFlds = RTConfig.getBoolean(ARG_EDITALL, false);
                    Entity entity = Entity.getEntity(account, entityID, entityType, false); // may throw DBException
                    DBEdit editor = new DBEdit(entity);
                    editor.edit(allFlds); // may throw IOException
                } catch (IOException ioe) {
                    if (ioe instanceof EOFException) {
                        Print.logError("End of input");
                    } else {
                        Print.logError("IO Error");
                    }
                } catch (DBException dbe) {
                    Print.logError("Error editing Entity: " + accountID + "/" + entityID + "/" + entityType);
                    dbe.printException();
                }
            }
            System.exit(0);
        }

        /* list */
        if (RTConfig.getBoolean(ARG_LIST, false)) {
            opts++;
            try {
                Entity entityList[] = Entity.getEntitiesForAccount(accountID);
                for (int i = 0; i < entityList.length; i++) {
                    Entity e = entityList[i];
                    Print.logInfo("  Entity      : " + e.getAccountID() + "/" + e.getEntityID() + "/" + e.getEntityType() + " [" + e.getDescription() + "]");
                    Print.logInfo("    isAttached: " + e.getIsAttached() + " [" + e.getDeviceID() + "]");
                    Print.logInfo("    GeoPoint  : " + e.getGeoPoint());
                    Print.logInfo("    Address   : " + e.getAddress());
                    Print.logInfo("    OdometerKM: " + e.getOdometerKM() + " [" + (e.getOdometerKM() * GeoPoint.MILES_PER_KILOMETER) + " miles]");
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
