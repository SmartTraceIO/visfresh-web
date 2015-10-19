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
//  2007/07/27  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.war.tools;

public class MapDimension
{

    // ------------------------------------------------------------------------

    private int width  = 0;
    private int height = 0;
    
    /**
    *** Consructor
    **/
    public MapDimension()
    {
        this(-1, -1);
    }
    
    /**
    *** Consructor
    *** @param width  The dimension width
    *** @param height The dimension height
    **/
    public MapDimension(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the dimension width
    *** @return The dimension width
    **/
    public int getWidth()
    {
        return this.width;
    }
    
    /**
    *** Gets the dimension height
    *** @return The dimension height
    **/
    public int getHeight()
    {
        return this.height;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this MapDimension defines a valid dimension
    *** @return True if this MapDimension defines a valid dimension
    **/
    public boolean isValid()
    {
        return (this.width > 0) && (this.height > 0);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a String representation of this MapDimension
    *** @return A String representation of this MapDimension
    **/
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getWidth());
        sb.append("/");
        sb.append(this.getHeight());
        return sb.toString();
    }

    // ------------------------------------------------------------------------

}
    
