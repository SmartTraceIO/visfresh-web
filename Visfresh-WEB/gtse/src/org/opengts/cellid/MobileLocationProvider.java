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
//  2011/07/01  Martin D. Flynn
//      -Initial release
// ----------------------------------------------------------------------------
package org.opengts.cellid;

import org.opengts.util.*;

/**
*** Interface for obtaining the location of a Cell Tower
**/
public interface MobileLocationProvider
{

    /** 
    *** Returns the name of this MobileLocationProvider 
    **/
    public String getName();

    /**
    *** Returns true if this MobileLocationProvider is enabled
    *** @return True if this MobileLocationProvider is enabled, false otherwise
    **/
    public boolean isEnabled();

    /**
    *** Returns the location of Cell Tower indicated by the attributes
    *** specified in the CellTower instance.
    *** @param servCT  The serving Cell Tower information
    *** @param nborCT  Neightbor Cell Tower information
    *** @return The Mobile location of the Cell Tower, or null if no
    ***     location could be determined.
    **/
    public MobileLocation getMobileLocation(CellTower servCT, CellTower nborCT[]);

}
