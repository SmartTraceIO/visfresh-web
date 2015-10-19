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
// Some [reverse]geocoder options/references:
//   http://www.johnsample.com/articles/GeocodeWithSqlServer.aspx   (incl reverse)
//   http://www.extendthereach.com/products/OSGeocoder.srct
//   http://datamining.anu.edu.au/student/honours-proj2005-geocoding.html
//   http://geocoder.us/
//   http://www.nacgeo.com/reversegeocode.asp
//   http://wsfinder.jot.com/WikiHome/Maps+and+Geography
/// ----------------------------------------------------------------------------
// Change History:
//  2007/01/25  Martin D. Flynn
//     -Initial release
//  2010/07/04  Martin D. Flynn
//     -Added "isEnabled" method
// ----------------------------------------------------------------------------
package org.opengts.geocoder;

import org.opengts.util.*;

public interface ReverseGeocodeProvider
{
    
    /**
    *** Returns the name of this ReverseGeocodeProvider 
    *** @return The name of this ReverseGeocodeProvider
    **/
    public String getName();

    /**
    *** Returns true if this ReverseGeocodeProvider is enabled
    *** @return True if this ReverseGeocodeProvider is enabled, false otherwise
    **/
    public boolean isEnabled();

    /**
    *** Returns true if this operation will take less than about 20ms to complete
    *** the returned value is used to determine whether the 'getReverseGeocode' operation
    *** should be performed immediately, or lazily (ie. in a separate thread).
    *** @return True if this is a fast (ie. local) operation
    **/
    public boolean isFastOperation();

    /**
    *** Returns the best address for the specified GeoPoint 
    *** @return The reverse-geocoded adress
    **/
    public ReverseGeocode getReverseGeocode(GeoPoint gp, String localeStr, boolean cache);

    /**
    *** Sets the failover ReverseGeocodeProvider
    *** @param rgp  The failover ReverseGeocodeProvider
    **/
    public void setFailoverReverseGeocodeProvider(ReverseGeocodeProvider rgp);

    /**
    *** Gets the failover ReverseGeocodeProvider
    *** @return The failover ReverseGeocodeProvider
    **/
    public ReverseGeocodeProvider getFailoverReverseGeocodeProvider();

}
