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
//  2008/02/21  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.db;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.db.tables.*;

public abstract class RuleFactoryAdapter
    implements RuleFactory
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* adjust action mask */
    public static int ValidateActionMask(int actionMask)
    {
        int m = actionMask & (int)EnumTools.getValueMask(NotifyAction.class);
        if (m != 0) {
            if (((m & ACTION_NOTIFY_MASK) != 0) && ((m & RuleFactory.ACTION_VIA_MASK) == 0)) {
                // Apparently an action notify recipient was specified 
                // (ie Account/Device/Rule), but no 'via' was specified.
                // Enable ACTION_VIA_EMAIL by default.
                m |= RuleFactory.ACTION_VIA_EMAIL;
            }
        }
        return m;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public RuleFactoryAdapter() 
    {
        super();
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns this RuleFactory name 
    *** @return This RuleFactory name
    **/
    public abstract String getName();

    /**
    *** Return this RuleFactory version String
    *** @return This RuleFactory version String
    **/
    public String getVersion()
    {
        return "0.0.0";
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a list of predefined rule actions
    *** @param bpl   The context BasicPrivateLabel instance
    *** @return The list of predefined rule actions (or null, if no predefined
    ***    rule actions have been defined
    **/
    public PredefinedRuleAction[] getPredefinedRuleActions(BasicPrivateLabel bpl)
    {
        return null;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the description for the specified GeoCorridor ID.
    *** Will return null if GeoCorridor is not supported.
    *** Will return blank if the specified GeoCorridor ID was not found.
    *** @param account   The Account that owns the specified GeoCorridor ID
    *** @param corrID    The GeoCorridor ID
    **/
    public String getGeoCorridorDescription(Account account, String corrID)
    {
        return null;
    }

}
