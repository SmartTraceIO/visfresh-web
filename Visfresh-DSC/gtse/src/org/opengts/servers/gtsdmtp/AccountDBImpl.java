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
//     -Initial release
//  2006/04/09  Martin D. Flynn
//     -Integrated DBException
//  2006/04/23  Martin D. Flynn
//     -Integrated logging changes made to Print
//  2007/01/25  Martin D. Flynn
//     -Moved to "OpenGTS"
//  2007/07/27  Martin D. Flynn
//     -Repackaged to "org.opengts.servers.gtsdmtp"
// ----------------------------------------------------------------------------
package org.opengts.servers.gtsdmtp;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.dbtypes.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;
import org.opengts.db.dmtp.*;

import org.opendmtp.server.db.AccountDB;

public class AccountDBImpl
    implements AccountDB
{
    
    // ------------------------------------------------------------------------

    private Account account     = null;
    private String  accountName = null;

    private String  prefixID    = null;
    
    public AccountDBImpl(Account acct) 
    {
        this.setAccount(acct);
    }

    public AccountDBImpl(String prefixID) 
    {
        this.account     = null;
        this.accountName = null;
        this.prefixID    = StringTools.trim(prefixID);
    }

    // ------------------------------------------------------------------------

    /* not part of the AccountDB interface! */
    public Account getAccount()
    {
        return this.account; // may be null
    }
    
    /* not part of the AccountDB interface! */
    public void setAccount(Account acct)
    {
        this.account     = acct;
        this.accountName = (acct != null)? acct.getAccountID() : null;
        this.prefixID    = null;
    }
    
    /* not part of the AccountDB interface! */
    public void setAccountName(String acctName)
    {
        this.account     = null;
        this.accountName = StringTools.trim(acctName);
        this.prefixID    = null;
    }

    // ------------------------------------------------------------------------

    /* not part of the AccountDB interface! */
    public String getPrefixID() 
    {
        return (this.account != null)? null : this.prefixID;
    }

    // ------------------------------------------------------------------------

    public String getAccountName() 
    {
        return !StringTools.isBlank(this.accountName)? this.accountName : StringTools.trim(this.prefixID);
    }
    
    public String getDescription()
    {
        return (this.account != null)? this.account.getDescription() : "";
    }
    
    public boolean isActive()
    {
        return (this.account != null)? this.account.getIsActive() : true;
    }
   
    // ------------------------------------------------------------------------

    public String toString()
    {
        return this.getAccountName();
    }
    
    // ------------------------------------------------------------------------

}
