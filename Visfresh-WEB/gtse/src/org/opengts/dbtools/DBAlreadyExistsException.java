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
package org.opengts.dbtools;

import java.lang.*;
import java.util.*;
import java.sql.*;

import org.opengts.util.*;

/**
*** <code>DBAlreadyExistsException</code> is thrown in cases where no record was expected,
*** but a record was found.
**/

public class DBAlreadyExistsException
    extends DBException
{
    
    // ----------------------------------------------------------------------------

    /**
    *** Constructor
    *** @param msg  The String message associated with this Exception
    **/
    public DBAlreadyExistsException(String msg)
    {
        super(msg);
    }

    /**
    *** Constructor
    *** @param msg   The String message associated with this Exception
    *** @param cause The cause of this exception
    **/
    public DBAlreadyExistsException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    // ----------------------------------------------------------------------------

}
