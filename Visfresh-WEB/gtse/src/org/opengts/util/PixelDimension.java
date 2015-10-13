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
//  2008/08/08  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.util.*;

import org.opengts.util.*;

/**
*** A container for pixel width and height
**/

public class PixelDimension
    implements Cloneable
{

    // ------------------------------------------------------------------------

    private int width  = 0;
    private int height = 0;
    
    /**
    *** Constructor
    *** @param w The width of the pixel
    *** @param h The height of the pixel
    **/
    public PixelDimension(int w, int h)
    {
        this.setWidth( w);
        this.setHeight(h);
    }
    
    /**
    *** Copy constructor
    *** @param pd The PixelDimension to copy
    **/
    public PixelDimension(PixelDimension pd)
    {
        this.setWidth( (pd != null)? pd.getWidth()  : 0);
        this.setHeight((pd != null)? pd.getHeight() : 0);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a copy of this PixelDimension
    *** @return A copy of this PixelDimension object
    **/
    public Object clone()
    {
        return new PixelDimension(this);
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the witdh of the pixel
    *** @param w The width of the pixel
    **/
    public void setWidth(int w)
    {
        this.width = w;
    }

    /**
    *** Gets the width of the pixel
    *** @return The width of the pixel
    **/
    public int getWidth()
    {
        return this.width;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the height of the pixel
    *** @param h The height of the pixel
    **/
    public void setHeight(int h)
    {
        this.height = h;
    }

    /**
    *** Gets the height of the pixel
    *** @return The height of the pixel
    **/
    public int getHeight()
    {
        return this.height;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns ture if the pixel dimensions are valid
    *** @return True if the pixel dimensions are valid
    **/
    public boolean isValid()
    {
        return (this.width > 0) && (this.height > 0);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a String representation of this instance
    *** @return String representation of this instance
    **/
    public String toString()
    {
        StringBuffer sb= new StringBuffer();
        sb.append("W=").append(this.getWidth());
        sb.append(" ");
        sb.append("H=").append(this.getHeight());
        return sb.toString();
    }

    // ------------------------------------------------------------------------

}
