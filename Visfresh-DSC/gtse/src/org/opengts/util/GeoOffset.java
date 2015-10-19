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
//  2008/08/15  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.util.*;

import org.opengts.util.*;

/**
*** A container for a single latitude/longitude offset value pair
**/

public class GeoOffset
    implements Cloneable
{

    // ------------------------------------------------------------------------

    private double offsetLatitude  = 0.0;
    private double offsetLongitude = 0.0;

    /** 
    *** Constructor
    *** @param ofsLat The latitude offset
    *** @param ofsLon The longitude offset
    **/
    public GeoOffset(double ofsLat, double ofsLon)
    {
        this.setOffsetLatitude(ofsLat);
        this.setOffsetLongitude(ofsLon);
    }

    /** 
    *** Copy Constructor
    *** @param gb The other GeoOffset
    **/
    public GeoOffset(GeoOffset gb)
    {
        if (gb != null) {
            this.setOffsetLatitude( gb.getOffsetLatitude() );
            this.setOffsetLongitude(gb.getOffsetLongitude());
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a clone of this GeoOffset
    *** @return A clone of this GeoOffset
    **/
    public Object clone()
    {
        return new GeoOffset(this);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the Latitude offset
    *** @param dLat The latitude offset
    **/
    public void setOffsetLatitude(double dLat)
    {
        this.offsetLatitude = dLat;
    }

    /**
    *** Gets the Latitude offset
    *** @return The latitude offset
    **/
    public double getOffsetLatitude()
    {
        return this.offsetLatitude;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the Longitude offset
    *** @param dLon The longitude offset
    **/
    public void setOffsetLongitude(double dLon)
    {
        this.offsetLongitude = dLon;
    }

    /**
    *** Gets the Longitude offset
    *** @return The longitude offset
    **/
    public double getOffsetLongitude()
    {
        return this.offsetLongitude;
    }

    // ------------------------------------------------------------------------

}
