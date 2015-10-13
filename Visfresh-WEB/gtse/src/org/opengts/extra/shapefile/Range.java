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
//  2010/10/21  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.extra.shapefile;

import java.io.*;
import java.util.*;
import java.net.*;

import org.opengts.util.*;

public class Range
{

    // ------------------------------------------------------------------------

    private double Min  = 0.0;
    private double Max  = 0.0;
    
    public Range(Range r) 
    {
        if (r == null) {
            Print.logError("Invalid SHP data length: missing range");
        }
        this.Min = r.Min;
        this.Max = r.Max;
    }
    
    public Range(Payload p) 
    {
        if (p.getAvailableReadLength() < 16) { 
            Print.logError("Invalid SHP data length: missing range");
        }
        this.Min = p.readDouble(8, 0.0, false);
        this.Max = p.readDouble(8, 0.0, false);
    }
    
    public Range(double min, double max) 
    {
        this.Min = min;
        this.Max = max;
    }

    // ------------------------------------------------------------------------

    public double getMin() 
    {
        return this.Min;
    }
    
    public double getMax() 
    {
        return this.Max;
    }

    // ------------------------------------------------------------------------

    public Payload write(Payload p) 
    {
        p.writeDouble(this.getMin(),8,false);
        p.writeDouble(this.getMax(),8,false);
        return p;
    }

    // ------------------------------------------------------------------------

    public String toString() 
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Min=").append(this.getMin());
        sb.append(", ");
        sb.append("Max=").append(this.getMax());
        return sb.toString();
    }

    // ------------------------------------------------------------------------

}
