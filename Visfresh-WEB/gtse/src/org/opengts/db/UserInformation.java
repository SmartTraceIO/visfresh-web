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
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.dbtypes.*;
import org.opengts.db.tables.*;

public interface UserInformation
{

    public String getEncodedPassword();
    public void setDecodedPassword(BasicPrivateLabel bpl, String enteredPass);
    public boolean checkPassword(BasicPrivateLabel bpl, String enteredPass);
    
    //public int getGender();
    //public void setGender(int gender);

    public String getContactName();
    public void setContactName(String v);

    public String getContactPhone();
    public void setContactPhone(String v);

    public String getContactEmail();
    public void setContactEmail(String v);

    public String getTimeZone();
    public void setTimeZone(String v);

    public long getPasswdQueryTime();
    public void setPasswdQueryTime(long v);

    public long getLastLoginTime();
    public void setLastLoginTime(long v);

}
