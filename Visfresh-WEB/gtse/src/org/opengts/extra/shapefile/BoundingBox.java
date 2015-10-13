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

public class BoundingBox
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Creates a BoundingBox from the list of specified GeoPoints
    **/
    public static BoundingBox getBoundingBox(GeoPoint... gp)
    {
        return new BoundingBox(new GeoBounds(gp));
    }

    /**
    *** Creates a BoundingBox from the specified GeoPolygon
    **/
    public static BoundingBox getBoundingBox(GeoPolygon gp)
    {
        return new BoundingBox(new GeoBounds(gp));
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private double Xmin = 0.0;
    private double Xmax = 0.0;
    private double Ymin = 0.0;
    private double Ymax = 0.0;

    /**
    *** Constructor
    **/
    public BoundingBox(Shape sh) 
    {
        if (sh != null) {
            GeoBounds gb = new GeoBounds();
            gb.extendByPoint(sh.getShapePoints());
            this.Xmin = gb.getMinX();
            this.Ymin = gb.getMinY();
            this.Xmax = gb.getMaxX();
            this.Ymax = gb.getMaxY();
            //Print.logInfo("Shape BB: " + this);
        }
    }

    /**
    *** Constructor
    **/
    public BoundingBox(Collection<Shape> shapes) 
    {
        if (shapes != null) {
            GeoBounds gb = new GeoBounds();
            for (Shape sh : shapes) {
                Point pts[] = sh.getShapePoints();
                if (!ListTools.isEmpty(pts)) {
                    //Print.logInfo("Point[0]: " + pts[0] + " ==> " + pts[0].getGeoPoint());
                    gb.extendByPoint(pts);
                }
            }
            this.Xmin = gb.getMinX();
            this.Ymin = gb.getMinY();
            this.Xmax = gb.getMaxX();
            this.Ymax = gb.getMaxY();
        }
    }

    /**
    *** Constructor
    **/
    public BoundingBox(GeoBounds gb) 
    {
        if (gb != null) {
            this.Xmin = gb.getMinX();
            this.Ymin = gb.getMinY();
            this.Xmax = gb.getMaxX();
            this.Ymax = gb.getMaxY();
        }
    }

    /**
    *** Constructor
    **/
    public BoundingBox(BoundingBox bb)
    {
        if (bb == null) {
            Print.logError("Invalid SHP data length: missing bounding box");
        }
        this.Xmin = bb.Xmin;
        this.Ymin = bb.Ymin;
        this.Xmax = bb.Xmax;
        this.Ymax = bb.Ymax;
    }

    /**
    *** Constructor
    **/
    public BoundingBox(Payload p) 
    {
        if ((p == null) || (p.getAvailableReadLength() < 32)) { 
            Print.logError("Invalid SHP data length: missing bounding box");
        }
        this.Xmin = p.readDouble(8,0.0,false);
        this.Ymin = p.readDouble(8,0.0,false);
        this.Xmax = p.readDouble(8,0.0,false);
        this.Ymax = p.readDouble(8,0.0,false);
    }

    /**
    *** Constructor
    **/
    public BoundingBox(double xmin, double ymin, double xmax, double ymax) 
    {
        this.Xmin = xmin;
        this.Ymin = ymin;
        this.Xmax = xmax;
        this.Ymax = ymax;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the minimum X (Longitude) value
    **/
    public double getXMin() 
    {
        return this.Xmin;
    }

    /**
    *** Gets the maximum X (Longitude) value
    **/
    public double getXMax() 
    {
        return this.Xmax;
    }

    /**
    *** Gets the minimum Y (Latitude) value
    **/
    public double getYMin() 
    {
        return this.Ymin;
    }

    /**
    *** Gets the maximum Y (Latitude) value
    **/
    public double getYMax() 
    {
        return this.Ymax;
    }

    /**
    *** Gets the min (lower-left) point of the bounding box
    **/
    public GeoPoint getMinGeoPoint() 
    {
        return new GeoPoint(this.getYMin(), this.getXMin());
    }

    /**
    *** Gets the max (upper-right) point of the bounding box
    **/
    public GeoPoint getMaxGeoPoint() 
    {
        return new GeoPoint(this.getYMax(), this.getXMax());
    }

    // ------------------------------------------------------------------------

    /**
    *** Creates a GeoBounds object from this BoundingBox instance
    **/
    public GeoBounds getGeoBounds() 
    {
        return new GeoBounds(this.getMinGeoPoint(), this.getMaxGeoPoint());
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the average center of the bounds.
    *** Note that this does not return true "Centroid" of a shape, but rather the
    *** "Middle" point.
    *** @return The GepPoint representing the center of the bounds
    **/
    public GeoPoint getCentroid()
    {
        double avgLat = (this.getYMin() + this.getYMax()) / 2.0;
        double avgLon = (this.getXMin() + this.getXMax()) / 2.0;
        return new GeoPoint(avgLat, avgLon);
    }

    // ------------------------------------------------------------------------

    /**
    *** Writes this BoundingBox to the specified Payload 
    **/
    public Payload write(Payload p) 
    {
        if (p != null) {
            p.writeDouble(this.getXMin(),8,false);
            p.writeDouble(this.getYMin(),8,false);
            p.writeDouble(this.getXMax(),8,false);
            p.writeDouble(this.getYMax(),8,false);
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
        sb.append("X min/max = " + this.getXMin() + "/" + this.getXMax());
        sb.append(", ");
        sb.append("Y min/max = " + this.getYMin() + "/" + this.getYMax());
        return sb.toString();
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a String representation of this GeoPoint suitable for using as a
    *** PostgreSQL/PostGIS "point" data type.
    *** Format is "(LON1,LAT1),(LON1,LAT1)"
    **/
    public StringBuffer toPostgres_box(StringBuffer sb)
    {
        if (sb == null) { sb = new StringBuffer(); }
        GeoPoint.toPostgres_point(sb, this.getYMin(), this.getXMin());
        sb.append(",");
        GeoPoint.toPostgres_point(sb, this.getYMax(), this.getXMax());
        return sb;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
