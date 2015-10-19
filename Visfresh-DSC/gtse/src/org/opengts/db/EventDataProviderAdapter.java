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
//  2012/06/29  Martin D. Flynn
//     -Added "getGpsAge", "getCreationAge"
// ----------------------------------------------------------------------------
package org.opengts.db;
 
import java.lang.*;
import java.util.*;

import org.opengts.util.*;

public class EventDataProviderAdapter
    implements EventDataProvider
{

    public EventDataProviderAdapter()
    {
    }

    // ------------------------------------------------------------------------

    public String getAccountID()
    {
        return null;
    }

    public String getDeviceID()
    {
        return null;
    }

    public String getDeviceDescription() {
        return ""; 
    }

    public String getDeviceVIN() {
        return ""; 
    }

    public long getTimestamp()
    {
        return 0L;
    }

    public int getStatusCode()
    {
        return StatusCodes.STATUS_NONE;
    }

    public String getStatusCodeDescription(BasicPrivateLabel bpl)
    {
        return "";
    }

    public StatusCodeProvider getStatusCodeProvider(BasicPrivateLabel bpl)
    {
        return null;
    }

    public int getPushpinIconIndex(String iconSelector, OrderedSet<String> iconKeys,
        boolean isFleet, BasicPrivateLabel bpl)
    {
        return 0; // black
    }

    public boolean isValidGeoPoint()
    {
        return GeoPoint.isValid(this.getLatitude(), this.getLongitude());
    }

    public double getLatitude()
    {
        return 0.0;
    }

    public double getLongitude()
    {
        return 0.0;
    }

    public GeoPoint getGeoPoint()
    {
        return new GeoPoint(this.getLatitude(), this.getLongitude());
    }

    public long getGpsAge()
    {
        return 0L;
    }

    public long getCreationAge()
    {
        return 0L;
    }

    public double getHorzAccuracy()
    {
        return 0.0;
    }
    
    public GeoPoint getBestGeoPoint()
    {
        return this.getGeoPoint();
    }
    
    public double getBestAccuracy()
    {
        return -1.0;
    }

    public int getSatelliteCount()
    {
        return 0;
    }

    public double getBatteryLevel()
    {
        return 0.0;
    }

    public double getSpeedKPH()
    {
        return 0.0;
    }

    public double getHeading()
    {
        return 0.0;
    }

    public double getAltitude()
    {
        return 0.0;
    }

    public String getGeozoneID()
    {
        return "";
    }

    public String getAddress()
    {
        return "";
    }

    public long   getInputMask()
    {
        return 0L;
    }

    public double getOdometerKM()
    {
        return 0.0;
    }

    // ------------------------------------------------------------------------

    private int eventIndex = -1;
    public void setEventIndex(int ndx)
    {
        this.eventIndex = ndx;
    }

    public int getEventIndex()
    {
        return this.eventIndex;
    }

    public boolean getIsFirstEvent()
    {
        return (this.getEventIndex() == 0);
    }

    // ------------------------------------------------------------------------
    
    private boolean isLastEvent = false;

    public void setIsLastEvent(boolean isLast)
    {
        this.isLastEvent = isLast;
    }

    public boolean getIsLastEvent()
    {
        return this.isLastEvent;
    }

    // ------------------------------------------------------------------------

}
