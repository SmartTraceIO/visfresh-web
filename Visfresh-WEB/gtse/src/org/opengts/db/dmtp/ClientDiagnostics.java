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
//  2008/03/28  Martin D. Flynn
//     -Extracted from org.opendmtp.codes.ClientDiagnostics
// ----------------------------------------------------------------------------
package org.opengts.db.dmtp;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;

public class ClientDiagnostics
    implements org.opendmtp.codes.ClientDiagnostics
{

    // ------------------------------------------------------------------------

    /**
    *** Gets the code description
    *** @param diagCode  The diagnostic code
    *** @return The diagnostic description
    **/
    public static String getDescription(int diagCode)
    {
        return getStringValue(diagCode, true, null, null);
    }

    /**
    *** Gets the String representation of the specified data for the specified diagnostic code
    *** @param diagCode  The diagnostic code
    *** @param diagData  The binary payload data
    *** @return The value String representation
    **/
    public static String getStringValue(int diagCode, byte diagData[])
    {
        return getStringValue(diagCode, false, diagData, null);
    }

    /**
    *** Gets the String representation of the specified data for the specified diagnostic code
    *** @param diagCode  The diagnostic code
    *** @param diagData  The binary payload data
    *** @param tz        A TimeZone used convert any dates encountered
    *** @return The value String representation
    **/
    public static String getStringValue(int diagCode, byte diagData[], TimeZone tz)
    {
        return getStringValue(diagCode, false, diagData, tz);
    }

    /**
    *** Gets the Description and/or converts the specified binary value to a String
    *** @param diagCode  The diagnostic code
    *** @param inclDesc  True to include the description, false to omit
    *** @param diagData  The binary payload data
    *** @return The value String representation
    **/
    public static String getStringValue(int diagCode, boolean inclDesc, byte diagData[])
    {
        return getStringValue(diagCode, inclDesc, diagData, null);
    }

    /**
    *** Gets the Description and/or converts the specified binary value to a String
    *** @param diagCode  The diagnostic code
    *** @param inclDesc  True to include the description, false to omit
    *** @param diagData  The binary payload data
    *** @param tz        A TimeZone used convert any dates encountered
    *** @return The Description and/or value String representation
    **/
    public static String getStringValue(int diagCode, boolean inclDesc, byte diagData[], TimeZone tz)
    {
        Payload payload = ((diagData != null) && (diagData.length > 0))? new Payload(diagData) : null;
        StringBuffer sb = new StringBuffer();
        switch (diagCode) {
            case DIAG_UPLOAD_ACK: 
                if (inclDesc) { 
                    sb.append("Upload Acknowledgement");
                    if (payload != null) { sb.append(": "); }
                }
                if (payload != null) {
                    sb.append("0x");
                    sb.append(StringTools.toHexString(payload.readLong(1,0L),8));
                }
                return sb.toString();
            case DIAG_OBC_J1708_VALUE:
                if (inclDesc) { 
                    sb.append("J1708 Value"); 
                    if (payload != null) { sb.append(": "); }
                }
                if (payload != null) {
                    sb.append("mid=").append(payload.readLong(2,0L)).append(", ");
                    sb.append("pid=").append(payload.readLong(2,0L)).append(", ");
                    sb.append("val=0x");
                    sb.append(StringTools.toHexString(payload.readBytes(payload.getAvailableReadLength())));
                }
                return sb.toString();
        }

        /* internal error */
        if ((diagCode >= DIAG_INTERNAL_DIAG_E000) && (diagCode <= DIAG_INTERNAL_DIAG_FFFF)) {
            if (inclDesc) { 
                sb.append("Internal diagnostic"); 
                if (payload != null) { sb.append(": "); }
            }
            if (payload != null) {
                int dlen = payload.getAvailableReadLength();
                sb.append("0x").append(StringTools.toHexString(payload.readBytes(dlen)));
            }
            return sb.toString();
        }

        /* unknown */
        if (inclDesc) { 
            sb.append("Unknown[0x").append(StringTools.toHexString(diagCode,16)).append("]"); 
            if (payload != null) { sb.append(": "); }
        }
        if (payload != null) {
            int dlen = payload.getAvailableReadLength();
            sb.append("0x").append(StringTools.toHexString(payload.readBytes(dlen)));
        }
        return sb.toString();
        
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private ClientDiagnostics()
    {
        // not instantiated
    }

}
