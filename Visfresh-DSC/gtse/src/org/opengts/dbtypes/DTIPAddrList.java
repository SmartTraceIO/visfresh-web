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
//  2006/10/17  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opengts.dbtypes;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

public class DTIPAddrList
    extends DBFieldType
{

    // ------------------------------------------------------------------------

    private IPTools.IPAddressList ipList = null;

    public DTIPAddrList(IPTools.IPAddressList ipList)
    {
        this.ipList = (ipList != null)? ipList : new IPTools.IPAddressList();
    }

    public DTIPAddrList(IPTools.IPAddress ipAddr[])
    {
        this(new IPTools.IPAddressList(ipAddr));
    }

    public DTIPAddrList(String ipList)
    {
        this(new IPTools.IPAddressList(ipList));
    }

    public DTIPAddrList(ResultSet rs, String fldName)
        throws SQLException
    {
        // set to default value if 'rs' is null
        this((rs != null)? new IPTools.IPAddressList(rs.getString(fldName)) : null);
    }

    // ------------------------------------------------------------------------

    public boolean isEmpty()
    {
        return (this.ipList == null) || this.ipList.isEmpty();
    }
    
    public boolean isMatch(String ipAddr)
    {
        if (this.ipList != null) {
            return this.ipList.isMatch(ipAddr);
        } else {
            return true;
        }
    }

    // ------------------------------------------------------------------------

    public Object getObject()
    {
        return this.toString();
    }

    public String toString()
    {
        return (this.ipList != null)? this.ipList.toString() : "";
    }

    // ------------------------------------------------------------------------

    public boolean equals(Object other)
    {
        if (this == other) {
            // same object
            return true;
        } else
        if (other instanceof DTIPAddrList) {
            DTIPAddrList otherList = (DTIPAddrList)other;
            if (otherList.ipList == this.ipList) {
                // will also match if both are null
                return true;
            } else
            if ((this.ipList == null) || (otherList.ipList == null)) {
                // one is null, the other isn't
                return false;
            } else {
                // IPAddressList match
                return this.ipList.equals(otherList.ipList);
            }
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

}
