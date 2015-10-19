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
//  2007/11/28  Martin D. Flynn
//     -Initial release
//  2009/07/01  Martin D. Flynn
//     -Repackaged
// ----------------------------------------------------------------------------
package org.opengts.extra.war.report.entity;

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

import org.opengts.extra.tables.*;

public class EntityReport
    extends ReportData
    implements DBRecordHandler<Entity>
{

    // ------------------------------------------------------------------------
    // Summary report
    // ------------------------------------------------------------------------
    // Columns:
    //   index timestamp entityId entityDesc geoPoint/latitude/longitude deviceID odometer address attached
    // ------------------------------------------------------------------------

    private static String PROP_entityType        = "entityType";
    private static String PROP_entityStatusCode  = "entityMapStatusCode";
    private static String PROP_entityPushpinID   = "entityMapPushpinID";

    // ------------------------------------------------------------------------
    
    private EntityManager.EntityType    entityType          = null;
    private long                        entityTypeCode      = -1L;
    private int                         entityStatusCode    = StatusCodes.STATUS_LOCATION;
    private String                      entityPushpinID     = null;
        
    private java.util.List<FieldData>   rowData             = null;
    
    private Map<String,EventData>       lastDeviceEvents    = new HashMap<String,EventData>();

    // ------------------------------------------------------------------------

    public EntityReport(ReportEntry rptEntry, RequestProperties reqState, ReportDeviceList devList)
        throws ReportException
    {
        super(rptEntry, reqState, devList);
        if (this.getAccount() == null) {
            throw new ReportException("Account must be specified");
        }
    }

    // ------------------------------------------------------------------------

    public void postInitialize()
    {
        //ReportConstraints rc = this.getReportConstraints();
        //Print.logInfo("LimitType=" + rc.getSelectionLimitType() + ", Limit=" + rc.getSelectionLimit());

        /* EntityType */
        String entType = this.getProperties().getString(PROP_entityType,"").trim();
        if (StringTools.isBlank(entType)) {
            this.entityType = null;
            this.entityTypeCode = -1L;
        } else {
            if (Character.isDigit(entType.charAt(0))) {
                long etc = StringTools.parseLong(entType,-1L);
                this.entityType = EntityManager.getEntityTypeFromCode(etc,null);
            } else {
                this.entityType = EntityManager.getEntityTypeFromName(entType,null);
            }
            if (this.entityType == null) {
                Print.logWarn("Unable to recognize EntityType: " + entType);
                this.entityTypeCode = -1L;
            } else {
                this.entityTypeCode = (long)this.entityType.getIntValue();
            }
        }

        /* Entity StatusCode */
        this.entityStatusCode = this.getProperties().getInt(PROP_entityStatusCode,StatusCodes.STATUS_LOCATION);

        /* Entity pushpin */
        this.entityPushpinID = this.getProperties().getString(PROP_entityPushpinID,"").trim();

    }
    
    public int getStatusCode()
    {
        return this.entityStatusCode;
    }
    
    public String getPushpinID()
    {
        return this.entityPushpinID;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this report supports displaying a map
    *** @return True if this report supports displaying a map, false otherwise
    **/
    public boolean getSupportsMapDisplay() // true
    {
        return true;
    }

    /**
    *** Returns true if this report supports displaying KML
    *** @return True if this report supports displaying KML, false otherwise
    **/
    public boolean getSupportsKmlDisplay()
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

    /* return an object that will iterate through the selected row object */
    public DBDataIterator getBodyDataIterator()
    {
        this.rowData = new Vector<FieldData>();
        String acctID = this.getAccountID();
        long limit = 0L; // no limit

        /* get Entities */
        try {
            Entity.getEntitiesForAccount(acctID, limit, this);
        } catch (DBException dbe) {
            Print.logException("Getting Entities ...", dbe);
        }

        /* return data iterator */
        return new ListDataIterator(this.rowData);
    }

    /* return the totals data list */
    public DBDataIterator getTotalsDataIterator()
    {
        return null;
    }

    // ------------------------------------------------------------------------

    private EventData getLastDeviceEvent(String devID, long endTime)
    {
        if (devID != null) {
            try {
                EventData event = this.lastDeviceEvents.get(devID);
                if (event == null) {
                    Device device = Device.getDevice(this.getAccount(), devID); // null if non-existent
                    if (device != null) {
                        event = device.getLastEvent(endTime, true);
                        if (event != null) {
                            this.lastDeviceEvents.put(devID, event);
                        }
                    }
                }
                return event; // may still be null
            } catch (Throwable th) { // DBException
                Print.logException("Error retrieving last EventData for Device: " + devID, th);
            }
        }
        return null;
    }
    
    public int handleDBRecord(Entity rcd)
        throws DBException
    {
        Entity    ent = rcd;
        boolean   isAttached = ent.getIsAttached();
        String    entDev     = isAttached? ent.getDeviceID() : "";
        EventData evt        = isAttached? this.getLastDeviceEvent(entDev,-1L) : null;
        long      entTime    = ent.getTimestamp();
        long      entType    = ent.getEntityType();

        /* discard unspecified entity types? */
        if ((this.entityTypeCode >= 0) && (this.entityTypeCode != entType)) {
            // ignore this entity type
            return DBRH_SKIP;
        }

        /* fill data */
        FieldData fd  = new EntityFieldData(this);
        fd.setString(  FieldLayout.DATA_ACCOUNT_ID      , this.getAccountID());
        fd.setString(  FieldLayout.DATA_ENTITY_ID       , ent.getEntityID());
        fd.setLong(    FieldLayout.DATA_ENTITY_TYPE     , entType);
        fd.setDouble(  FieldLayout.DATA_ODOMETER        , ent.getOdometerKM()); // entity is not associated with a single device
        fd.setString(  FieldLayout.DATA_ENTITY_DESC     , ent.getDescription());
        fd.setBoolean( FieldLayout.DATA_ATTACHED        , isAttached);
        fd.setString(  FieldLayout.DATA_DEVICE_ID       , entDev);
        if ((evt != null) && (evt.getTimestamp() >= entTime)) {
            // use the last location of the Device towing the trailer
            fd.setLong(    FieldLayout.DATA_TIMESTAMP   , evt.getTimestamp());
            fd.setGeoPoint(FieldLayout.DATA_GEOPOINT    , evt.getGeoPoint());
            fd.setString(  FieldLayout.DATA_ADDRESS     , evt.getAddress());
        } else {
            // use the last "drop" location
            fd.setLong(    FieldLayout.DATA_TIMESTAMP   , entTime);
            fd.setGeoPoint(FieldLayout.DATA_GEOPOINT    , ent.getGeoPoint());
            fd.setString(  FieldLayout.DATA_ADDRESS     , ent.getAddress());
        }

        /* add record to list */
        this.rowData.add(fd);

        /* return */
        return (this.rowData.size() < this.getReportLimit())? DBRH_SKIP : DBRH_STOP;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Custom MotionFieldData class
    **/
    private static class EntityFieldData
        extends FieldData.FieldEventDataProvider
    {
        // Available fields:
        // FieldLayout.DATA_ACCOUNT_ID
        // FieldLayout.DATA_ENTITY_ID
        // FieldLayout.DATA_ENTITY_TYPE
        // FieldLayout.DATA_ODOMETER
        // FieldLayout.DATA_ENTITY_DESC
        // FieldLayout.DATA_ATTACHED
        // FieldLayout.DATA_DEVICE_ID
        // FieldLayout.DATA_TIMESTAMP
        // FieldLayout.DATA_GEOPOINT
        // FieldLayout.DATA_ADDRESS
        private EntityReport entityReport = null;
        public EntityFieldData(EntityReport er) {
            super();
            this.entityReport = er;
        }
        public int getStatusCode() {
            return this.entityReport.getStatusCode();
        }
        public int getPushpinIconIndex(String iconSelector, OrderedSet<String> iconKeys, 
            boolean isFleet, BasicPrivateLabel bpl) {
            String pid = this.entityReport.getPushpinID();
            return EventData._getPushpinIconIndex(pid, iconKeys, EventData.ICON_PUSHPIN_ORANGE);
        }
    }

    // ------------------------------------------------------------------------

}
