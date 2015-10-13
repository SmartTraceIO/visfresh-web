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
// Simple .dbf file parser (only limited functionality is supported)
// ----------------------------------------------------------------------------
// Change History:
//  2010/09/09  Martin D. Flynn
//     -Initial release
//  2010/10/21  Martin D. Flynn
//     -Updated
// ----------------------------------------------------------------------------
package org.opengts.extra.shapefile;

import java.io.*;
import java.util.*;
import java.net.*;

import org.opengts.util.*;

public class Field
{
    
    // ------------------------------------------------------------------------

    public static final int  DATATYPE_STRING        = (int)'C';
    public static final int  DATATYPE_DATE          = (int)'D';
    public static final int  DATATYPE_FLOAT         = (int)'F';
    public static final int  DATATYPE_DOUBLE        = (int)'N';
    public static final int  DATATYPE_BOOLEAN       = (int)'L';
    public static final int  DATATYPE_MEMO          = (int)'M';
    public static final int  DATATYPE_TIMESTAMP     = (int)'@';

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static String TRIM(String str)
    {
        String s = StringTools.trim(str);
        int z = s.indexOf((char)0);
        if (z >= 0) {
            s = s.substring(0,z);
        }
        return s;
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private String  name        = "";
    private int     dataType    = 0; // C=String, D=Date, F=Float, N=Double, L=Boolean, M=?
    private int     length      = 0;
    private int     decimal     = 0;
    private int     workAreaID  = 0;
    private int     flags       = 0;
    private int     index       = 0;

    /**
    *** Constructor
    **/
    public Field(Payload p) 
    {
        this.name       = TRIM(p.readString(11,false));
        this.dataType   = (int)p.readULong(1,0L);
        p.readSkip(4);
        this.length     = (int)p.readULong(1,0L,false);
        this.decimal    = (int)p.readULong(1,0L);
        p.readSkip(2);
        this.workAreaID = (int)p.readULong(1,0L);
        p.readSkip(2);
        this.flags      = (int)p.readULong(1,0L);
        p.readSkip(7);
        this.index      = (int)p.readULong(1,0L);
        //Print.logDebug("Field(Payload): Name="+this.name +" Type="+this.dataType +" Len="+this.length +" Dec="+this.decimal +" Index="+this.index);
    }

    /**
    *** Constructor
    **/
    public Field(String name, int type, int len, int dec, int waid, int flags, int ndx) 
    {
        this.name       = TRIM(name);
        this.dataType   = type;
        this.length     = len;
        this.decimal    = dec;
        this.workAreaID = waid;
        this.flags      = flags;
        this.index      = ndx;
        //Print.logDebug("Field(Set): Name="+this.name +" Type="+this.dataType +" Len="+this.length +" Dec="+this.decimal +" Index="+this.index);
    }

    /**
    *** Constructor
    **/
    public Field(String name, int len) 
    {
        this.name       = TRIM(name);
        this.dataType   = DATATYPE_STRING;
        this.length     = len;
        this.decimal    = 0;
        this.workAreaID = 0;
        this.flags      = 0;
        this.index      = 0;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the field name
    **/
    public String getName() 
    {
        return this.name;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the field data type
    **/
    public int getDataType() 
    {
        return this.dataType;
    }

    /**
    *** Get PostgreSQL data type
    **/
    public String getPostgresDataType()
    {
        switch (this.getDataType()) {
            case DATATYPE_STRING    : // 'C';
                return "varchar(" + this.getLength() + ")";
            case DATATYPE_DATE      : // 'D';
                return "bigint";
            case DATATYPE_FLOAT     : // 'F';
                return "double precision";
            case DATATYPE_DOUBLE    : // 'N';
                return "double precision";
            case DATATYPE_BOOLEAN   : // 'L';
                return "smallint";
            case DATATYPE_MEMO      : // 'M';
                return "varchar(" + this.getLength() + ")";
            case DATATYPE_TIMESTAMP : // '@';
                return "bigint";
            default :
                return "?";
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the field length
    **/
    public int getLength()
    {
        return this.length;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the field decimal count
    **/
    public int getDecimal() 
    {
        return this.decimal;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the field work area id
    **/
    public int getWorkAreaID() 
    {
        return this.workAreaID;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the field flags
    **/
    public int getFlags() 
    {
        return this.flags;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the field index
    **/
    public int getIndex() 
    {
        return this.index;
    }
    
    /**
    *** Sets the field index
    **/
    public void setIndex(int ndx)
    {
        this.index = ndx;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the String representation of this instance
    **/
    public String toString() 
    {
        StringBuffer sb = new StringBuffer();
        int dt = this.getDataType();
        sb.append("Field:");
        sb.append(" name=").append(this.getName());
        sb.append(" type=").append(dt).append(",").append(((dt > ' ') && (dt < 127))? (char)dt : '?');
        sb.append(" len=" ).append(this.getLength());
        if ((dt == DATATYPE_FLOAT) || (dt == DATATYPE_DOUBLE) || (this.getDecimal() > 0)) {
            sb.append(" dec=" ).append(this.getDecimal());
        }
        if (this.getWorkAreaID() != 0) {
            sb.append(" waid=").append(this.getWorkAreaID());
        }
        if (this.getFlags() != 0) {
            sb.append(" flag=").append(this.getFlags());
        }
        if (this.getIndex() >= 0) {
            sb.append(" ndx=" ).append(this.getIndex());
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------------

    /**
    *** Writes this instance to the specified Payload
    **/
    public Payload write(Payload p) 
    {
        if (p != null) {
            p.writeString(this.name, 11, false);
            p.writeULong((long)this.dataType, 1);
            p.writeZeroFill(4);
            p.writeULong((long)this.length, 1);
            p.writeULong((long)this.decimal, 1);
            p.writeZeroFill(2);
            p.writeULong((long)this.workAreaID, 1);
            p.writeZeroFill(2);
            p.writeULong((long)this.flags, 1);
            p.writeZeroFill(7);
            p.writeULong((long)this.index, 1);
        }
        return p;
    }

    // ------------------------------------------------------------------------

}
