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
package org.opengts.war.report;

import java.util.*;
import java.io.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.report.*;

public class ReportHeaderGroup
{
    
    // ------------------------------------------------------------------------

    private int       colIndex          = -1;
    private int       colSpan           = 1;
    private I18N.Text colTitle          = null;

    public ReportHeaderGroup(int index, int span, I18N.Text title)
    {
        this.colIndex = index;
        this.colSpan  = span;
        this.colTitle = title;
    }
    
    // ------------------------------------------------------------------------

    /* set column index */
    public void setColIndex(int index)
    {
        this.colIndex = index;
    }

    /* get column index */
    public int getColIndex()
    {
        return this.colIndex;
    }

    // ------------------------------------------------------------------------

    /* set column span */
    public void setColSpan(int span)
    {
        this.colSpan = span;
    }

    /* get column span */
    public int getColSpan()
    {
        return this.colSpan;
    }

    // ------------------------------------------------------------------------

    /* set column title */
    public void setTitle(I18N.Text title)
    {
        this.colTitle = title;
    }
    
    /* return true if a title is defined */
    public boolean hasTitle()
    {
        return (this.colTitle != null);
    }

    /* get column title */
    public String getTitle(Locale loc)
    {
        return (this.colTitle != null)? this.colTitle.toString(loc) : "";
    }

    // ------------------------------------------------------------------------

    /* debug: convert to string */
    public String toString() 
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append("colIndex=").append(this.getColIndex());
        sb.append(", ");
        sb.append("colspan=").append(this.getColSpan());
        sb.append("] ");
        sb.append(this.getTitle(null));
        return sb.toString();
    }
    
    // ------------------------------------------------------------------------

}
