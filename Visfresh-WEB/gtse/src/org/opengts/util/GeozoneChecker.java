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
//  2008/05/14  Martin D. Flynn
//     -Reordered argument list to 'containsPoint'
// ----------------------------------------------------------------------------
package org.opengts.util;

import org.opengts.util.*;

/**
*** Inteface for functions finding if a point is inside of a polygon on a spherical surface
**/

public interface GeozoneChecker
{

    /**
    *** Checks if a specified point is inside the specified polygon on the surface of a sphere
    *** @param gpTest The point to check if is inside the polygon
    *** @param gpList The array of GeoPoints forming the polygon
    *** @param radiusKM The radius of the sphere that the points lie on
    *** @return True if the specified point is inside the polygon
    **/
    public boolean containsPoint(GeoPoint gpTest, GeoPoint gpList[], double radiusKM);
    
}
