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
//  2009/07/01  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.db.*;
import org.opengts.db.tables.*;

public class CommandPingDispatcher
    implements PingDispatcher
{
    
    // ------------------------------------------------------------------------

    public CommandPingDispatcher()
    {
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if 'ping' is supported for the specified device
    *** @param device The device
    *** @return True if 'ping' is supported for the specified device
    **/
    public boolean isPingSupported(Device device)
    {
        return DCServerFactory.supportsCommandDispatcher(device);
    }
    
    /**
    *** Sends a command notification to the specified Device
    *** @param device  The device to which the command is to be sent.
    *** @param cmdType The command type
    *** @param cmdName The command name
    *** @param cmdArgs The arguments to the command sent to the device.
    *** @return True if the command was sent successfully.
    **/
    public boolean sendDeviceCommand(Device device, String cmdType, String cmdName, String cmdArgs[])
    {
        RTProperties resp = DCServerFactory.sendServerCommand(device, cmdType, cmdName, cmdArgs);
        return DCServerFactory.isCommandResultOK(resp);
    }

    // ------------------------------------------------------------------------

}


