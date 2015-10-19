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
// ----------------------------------------------------------------------------
package org.opengts.geocoder;

import org.opengts.util.*;

public interface GeocodeProvider
{

    /** 
    *** Returns the name of this GeocodeProvider 
    **/
    public String getName();

    /**
    *** Returns true if this GeocodeProvider is enabled
    *** @return True if this GeocodeProvider is enabled, false otherwise
    **/
    public boolean isEnabled();

    /** 
    *** Return true if this operation will take less than 20ms to complete 
    *** (The returned value is used to determine whether the 'getGeocode' operation
    *** should be performed immediately, or lazily.)
    **/
    public boolean isFastOperation();

    /**
    *** Returns GeoPoint of specified address
    **/
    public GeoPoint getGeocode(String address, String country);

}
