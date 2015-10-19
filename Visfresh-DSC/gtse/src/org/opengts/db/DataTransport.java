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
//  2008/05/14  Martin D. Flynn
//     -Initial release
//  2008/10/16  Martin D. Flynn
//     -Added 'getLastPingTime', 'getTotalPingCount'
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.dbtypes.*;
import org.opengts.db.tables.*;

public interface DataTransport
{

    // ------------------------------------------------------------------------

    public String getTransportID();

    public String getUniqueID();
    
    public String getDescription();

    // ------------------------------------------------------------------------
    
    public String getAssocAccountID();
    
    public String getAssocDeviceID();

    // ------------------------------------------------------------------------

    public String getDeviceCode();
    public void setDeviceCode(String v);

    public String getDeviceType();
    public void setDeviceType(String v);

    public String getSerialNumber();
    public void setSerialNumber(String v);

    public String getSimPhoneNumber();
    public void setSimPhoneNumber(String v);

    public String getSmsEmail();
    public void setSmsEmail(String v);

    public String getImeiNumber();
    public void setImeiNumber(String v);

    // ------------------------------------------------------------------------

    public DTIPAddrList getIpAddressValid();
    public void setIpAddressValid(DTIPAddrList v);
    public boolean isValidIPAddress(String ipAddr);

    public DTIPAddress getIpAddressCurrent();
    public void setIpAddressCurrent(String v);

    public int getRemotePortCurrent();
    public void setRemotePortCurrent(int v);

    public int getListenPortCurrent();
    public void setListenPortCurrent(int v);

    // ------------------------------------------------------------------------

    public long getLastInputState();
    public void setLastInputState(long v);

    public long getLastOutputState();
    public void setLastOutputState(long v);

    public int getIgnitionIndex();
    public void setIgnitionIndex(int v);
    public int[] getIgnitionStatusCodes();

    // ------------------------------------------------------------------------

    public String getCodeVersion();
    public void setCodeVersion(String v);

    public String getFeatureSet();
    public void setFeatureSet(String v);

    public boolean getSupportsDMTP();
    public void setSupportsDMTP(boolean v);

    public int getSupportedEncodings();
    public void setSupportedEncodings(int encodingMask);

    // ------------------------------------------------------------------------

    public int getUnitLimitInterval();
    
    public int getMaxAllowedEvents();
    
    public DTProfileMask getTotalProfileMask();
    public void setTotalProfileMask(DTProfileMask v);
    
    public int getTotalMaxConn();
    
    public int getTotalMaxConnPerMin();

    public DTProfileMask getDuplexProfileMask();
    public void setDuplexProfileMask(DTProfileMask v);
    
    public int getDuplexMaxConn();
    
    public int getDuplexMaxConnPerMin();

    // ------------------------------------------------------------------------

    public long getLastPingTime();
    public int getTotalPingCount();
    public int getMaxPingCount();
    //public boolean getExpectAck();
    //public long getLastAckTime();

    // ------------------------------------------------------------------------

    public long getLastTotalConnectTime();
    public void setLastTotalConnectTime(long v);
    
    public long getLastDuplexConnectTime();
    public void setLastDuplexConnectTime(long v);

    // ------------------------------------------------------------------------

    public void update(String... updFldArray)
        throws DBException;

    public void update(Set<String> updFldSet)
        throws DBException;

    // ------------------------------------------------------------------------

}
