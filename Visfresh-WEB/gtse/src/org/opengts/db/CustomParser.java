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
//  2010/10/25  Martin D. Flynn
//     -Initial release
//  2015/09/18  Martin D. Flynn
//     -Added MOBILEID, EVENTCODE field types
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;
import org.opengts.db.tables.*;

public interface CustomParser
{

    // ------------------------------------------------------------------------

    public static final String  PREFIX_     = "$";

    public static final String  ACCOUNT     = "$account";   // Account
    public static final String  DEVICE      = "$device";    // Device
    public static final String  DATA_TYPE   = "$type";      // String | Integer
    public static final String  DATA        = "$data";      // byte[] | String
    public static final String  TRANSPORT   = "$transport"; // String
    public static final String  DUPLEX      = "$duplex";    // Boolean (obsolete)
    public static final String  COUNT       = "$count";     // Integer
    public static final String  INDEX       = "$index";     // Integer
    public static final String  MOBILEID    = "$mobileid";  // String
    public static final String  EVENTCODE   = "$eventcode"; // String

    // ------------------------------------------------------------------------

    /**
    *** Callback to parse raw data received from a remote tracking device through its
    *** device communication server.
    *** @param account  The assigned device Account instance
    *** @param device   The assigned Device instance
    *** @param type     The type of data contained in "data"
    *** @param data     The byte array containing the raw data
    *** @param props    A map where parsed data should be placed (to be inserted into the EventData record)
    *** @return The response which will be sent back to the device
    **/
    public byte[] parseData(
        Account account, Device device, 
        String type, byte data[], 
        Map<String,Object> props);

}
