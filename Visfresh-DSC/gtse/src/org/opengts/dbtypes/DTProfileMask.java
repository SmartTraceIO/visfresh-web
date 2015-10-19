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
//  2006/03/26  Martin D. Flynn
//      Initial release
//  2006/06/05  Martin D. Flynn
//      Moved to "OpenGTS"
// ----------------------------------------------------------------------------
package org.opengts.dbtypes;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

public class DTProfileMask
    extends DBFieldType
{
    
    // ------------------------------------------------------------------------

    private byte profileMask[] = new byte[0];
    
    public DTProfileMask(byte profileMask[])
    {
        this.profileMask = (profileMask != null)? profileMask : new byte[0];
    }
    
    public DTProfileMask(String val)
    {
        super(val);
        this.profileMask = DBField.parseBlobString(val);
        if (this.profileMask == null) {
            // -- should not occur
            this.profileMask = new byte[0];
        }
    }

    public DTProfileMask(ResultSet rs, String fldName)
        throws SQLException
    {
        super(rs, fldName);
        // -- set to default value if 'rs' is null
        this.profileMask = (rs != null)? rs.getBytes(fldName) : new byte[0];
        if (this.profileMask == null) {
            // -- should not occur
            this.profileMask = new byte[0];
        }
    }

    // ------------------------------------------------------------------------

    public Object getObject()
    {
        //Print.logWarn("ProfileMask length = " + this.profileMask.length);
        return this.profileMask;
    }
    
    public String toString()
    {
        return "0x" + StringTools.toHexString(this.profileMask);
    }

    // ------------------------------------------------------------------------

    public void setLimitTimeInterval(int minutes)
    {
        int byteLen = (minutes + 7) / 8;
        if (this.profileMask.length != byteLen) {
            byte newMask[] = new byte[byteLen];
            if (newMask.length > 0) {
                int len = (this.profileMask.length < byteLen)? this.profileMask.length : byteLen;
                System.arraycopy(this.profileMask, 0, newMask, 0, len);
            }
            this.profileMask = newMask;
        }
    }

    // ------------------------------------------------------------------------

    public byte[] getByteMask()
    {
        return this.profileMask;
    }
    
}
