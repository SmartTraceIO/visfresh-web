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

public class Point
    implements GeoPointProvider
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns Point array containing a single GeoPoint
    **/
    public static Point[] getPoints(GeoPoint gp)
    {
        if (gp != null) {
            return new Point[] { new Point(gp) };
        } else {
            return new Point[0];
        }
    }

    /**
    *** Returns Point array containing the specified array of GeoPoints
    **/
    public static Point[] getPoints(GeoPoint... gp)
    {
        if (gp != null) {
            Point pt[] = new Point[gp.length];
            for (int i = 0; i < gp.length; i++) {
                pt[i] = new Point(gp[i]);
            }
            return pt;
        } else {
            return new Point[0];
        }
    }

    /**
    *** Returns Point array of the GeoPoints contained within the GeoPolygon
    **/
    public static Point[] getPoints(GeoPolygon gp)
    {
        if (gp != null) {
            return Point.getPoints(gp.getGeoPoints());
        } else {
            return new Point[0];
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private double  X       = 0.0;  // longitude
    private double  Y       = 0.0;  // latitude
    private boolean hasZ    = false;
    private double  Z       = 0.0;
    private boolean hasM    = false;
    private double  M       = 0.0;

    /**
    *** Constructor
    **/
    public Point(Point p) 
    {
        if (p == null) { 
            Print.logError("Invalid SHP data length: missing point");
        }
        this.X    = p.X;
        this.Y    = p.Y;
        this.hasZ = p.hasZ;
        this.Z    = p.Z;
        this.hasM = p.hasM;
        this.M    = p.M;
    }

    /**
    *** Constructor
    **/
    public Point(Payload p) 
    {
        if (p.getAvailableReadLength() < 16) { 
            Print.logError("Invalid SHP data length: missing point");
        }
        this.X = p.readDouble(8, 0.0, false);
        this.Y = p.readDouble(8, 0.0, false);
    }

    /**
    *** Constructor
    **/
    public Point(double x, double y) 
    {
        this.X = x;
        this.Y = y;
    }

    /**
    *** Constructor
    **/
    public Point(GeoPoint gp) 
    {
        this.X = (gp != null)? gp.getX() : 0.0;
        this.Y = (gp != null)? gp.getY() : 0.0;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the X (Longitude) value
    **/
    public double getX() 
    {
        return this.X; // longitude
    }

    /**
    *** Gets the X (Longitude) value
    **/
    public double getLongitude() 
    {
        return this.getX(); // longitude
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Y (Latitude) value
    **/
    public double getY() 
    {
        return this.Y; // latitude
    }

    /**
    *** Gets the Y (Latitude) value
    **/
    public double getLatitude() {
        return this.getY(); // latitude
    }

    // ------------------------------------------------------------------------

    /**
    *** Creates a GeoPoint from this Point instance
    **/
    public GeoPoint getGeoPoint() 
    {
        return new GeoPoint(this.getLatitude(), this.getLongitude());
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the Z-axis value
    **/
    public void setZ(double z) 
    {
        this.Z    = z;
        this.hasZ = true;
    }

    /**
    *** Returns true if the Z-axis has been defined
    **/
    public boolean hasZ() 
    {
        return this.hasZ;
    }

    /**
    *** Gets the Z-axis value
    **/
    public double getZ() 
    {
        return this.Z;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the M value
    **/
    public void setM(double m) 
    {
        this.M    = m;
        this.hasM = true;
    }

    /**
    *** Returns true if the M value has been defined
    **/
    public boolean hasM() 
    {
        return this.hasM;
    }

    /**
    *** Gets the M value
    **/
    public double getM() 
    {
        return this.M;
    }

    // ------------------------------------------------------------------------

    /**
    *** Writes this instance X/Y value to the specified Payload
    **/
    public Payload write(Payload p) 
    {
        if (p != null) {
            p.writeDouble(this.getX(),8,false);
            p.writeDouble(this.getY(),8,false);
        }
        return p;
    }

    /**
    *** Writes this instance Z value to the specified Payload
    **/
    public Payload writeZ(Payload p) 
    {
        if (this.hasZ()) {
            p.writeDouble(this.getZ(),8,false);
        }
        return p;
    }

    /**
    *** Writes this instance M value to the specified Payload
    **/
    public Payload writeM(Payload p) 
    {
        if (this.hasM()) {
            p.writeDouble(this.getM(),8,false);
        }
        return p;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a String representation of this instance
    **/
    public String toString() 
    {
        StringBuffer sb = new StringBuffer();
        sb.append("X=").append(this.getX());
        sb.append(", ");
        sb.append("Y=").append(this.getY());
        if (this.hasZ()) {
            sb.append(", ");
            sb.append("Z=").append(this.getZ());
        }
        if (this.hasM()) {
            sb.append(", ");
            sb.append("M=").append(this.getM());
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------

}
