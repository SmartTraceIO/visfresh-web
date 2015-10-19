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
//  2008/02/11  Martin D. Flynn
//     -Initial release
//  2008/02/17  Martin D. Flynn
//     -Added 'getMapIconIndex'
//  2008/09/12  Martin D. Flynn
//     -Added 'getSatelliteCount', 'getBatteryLevel'
//  2008/10/16  Martin D. Flynn
//     -Added 'setIsLastEvent', 'getIsLastEvent'
//  2009/07/01  Martin D. Flynn
//     -Renamed "getMapIconIndex(...)" to "getPushpinIconIndex(...)"
//  2011/05/13  Martin D. Flynn
//     -Added "setEventIndex", "getEventIndex", "getIsFirstEvent"
//  2012/06/29  Martin D. Flynn
//     -Added "getGpsAge", "getCreationAge"
// ----------------------------------------------------------------------------
package org.opengts.db;
 
import java.lang.*;
import java.util.*;

import org.opengts.util.*;

public interface EventDataProvider
{

    // - Event ID
    public String               getAccountID();
    public String               getDeviceID();
    public String               getDeviceDescription();
    public String               getDeviceVIN();

    public long                 getTimestamp();
    public int                  getStatusCode();
    public String               getStatusCodeDescription(BasicPrivateLabel bpl);
    public StatusCodeProvider   getStatusCodeProvider(BasicPrivateLabel bpl);

    public int                  getPushpinIconIndex(
                                    String iconSelector, OrderedSet<String> iconKeys,
                                    boolean isFleet, BasicPrivateLabel bpl);

    // - GPS based latitude/longitude
    public boolean              isValidGeoPoint();
    public double               getLatitude();
    public double               getLongitude();
    public GeoPoint             getGeoPoint();
    public long                 getGpsAge();
    public long                 getCreationAge();
    public double               getHorzAccuracy();
    
    // - GPS based lat/lon if available, otherwise Cell lat/lon
    public GeoPoint             getBestGeoPoint();
    public double               getBestAccuracy();

    public int                  getSatelliteCount();

    public double               getBatteryLevel();

    public double               getSpeedKPH();
    public double               getHeading();

    public double               getAltitude(); // meters

    public String               getGeozoneID();
    public String               getAddress();

    public long                 getInputMask();

    public double               getOdometerKM();

    /* icon selector properties */

    public void                 setEventIndex(int ndx);
    public int                  getEventIndex();
    public boolean              getIsFirstEvent();

    public void                 setIsLastEvent(boolean isLast);
    public boolean              getIsLastEvent();

}
