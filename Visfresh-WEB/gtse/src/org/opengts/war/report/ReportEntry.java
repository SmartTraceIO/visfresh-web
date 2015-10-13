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
//  2007/03/30  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.war.report;

import java.util.*;
import java.io.*;

import org.opengts.util.*;

public class ReportEntry
{

    // ------------------------------------------------------------------------

    private ReportFactory   reportFactory = null;
    private String          aclName = null;
        
    // ------------------------------------------------------------------------

    public ReportEntry()
    {
    }

    public ReportEntry(ReportFactory rf, String aclName)
    {
        this.reportFactory = rf;
        this.aclName = aclName;
    }

    // ------------------------------------------------------------------------

    public ReportFactory getReportFactory()
    {
        return this.reportFactory;
    }

    // ------------------------------------------------------------------------

    public String getAclName()
    {
        return this.aclName;
    }

    // ------------------------------------------------------------------------

}
