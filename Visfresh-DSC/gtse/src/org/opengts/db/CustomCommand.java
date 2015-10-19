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
//  2013/03/01  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;
import org.opengts.db.tables.*;

public interface CustomCommand
{

    // ------------------------------------------------------------------------

    /* command argument separator */
    public  static final String  ARG_SEPARATOR          = "|";
    public  static final char    ARG_SEPARATOR_CHAR     = '|';

    // ------------------------------------------------------------------------

    /**
    *** Callback to handle custom web-service commands.  
    *** device communication server.
    *** @param cmdID    The command id
    *** @param cmdArg   The command argument string
    *** @param account  The current Account instance
    *** @param user     The current User instance
    *** @param bpl      The context BasicPrivateLabel (if available)
    *** @param respType The expect response type (ie. "xml", "json", "csv", etc).
    *** @return The response which will be sent back to the requestor
    **/
    public byte[] handleCommand(
        String cmdID, String cmdArg,
        Account account, User user, 
        BasicPrivateLabel bpl,
        String respType);

}
