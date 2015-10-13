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
//  2007/01/10  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.tables.*;

public interface SessionStatsFactory
{

    // ------------------------------------------------------------------------

    public  static long IP_OVERHEAD             =  20L; // per packet (included below)
    public  static long UDP_OVERHEAD            =   8L + IP_OVERHEAD; // 28 bytes per packet
    public  static long TCP_OVERHEAD            =  24L + IP_OVERHEAD; // 44 bytes per packet
    public  static long TCP_SESSION_OVERHEAD    = 240L; // per TCP session

    // ------------------------------------------------------------------------

    /* add session statistic */
    public void addSessionStatistic(Device device, long timestamp, 
        String ipAddr, boolean isDuplex,
        long bytesRead, long bytesWritten, long eventsRecv)
        throws DBException;
        
    /* return bytes read/written */
    public long[] getByteCounts(Device device, long timeStart, long timeEnd)
        throws DBException;

    /* return number of tcp/udp connections made */
    public long[] getConnectionCounts(Device device, long timeStart, long timeEnd) throws DBException;

    // ------------------------------------------------------------------------

}
