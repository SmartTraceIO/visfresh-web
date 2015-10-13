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
//  2007/03/11  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.war.tools;

import java.util.*;
import java.io.*;

import org.opengts.util.*;

public interface PageDecorations
{

    // ------------------------------------------------------------------------
    
    public void setDefaultPageDecorations(PageDecorations dftPageDecor);

    // ------------------------------------------------------------------------

    /* returns true if this PageDecorations instance is backed by a JSP page */
    public boolean hasJspURI();
    
    /* sets the backing JSP page URI */
    public void setJspURI(String jspURI);

    /* returns backing JSP page URI, or null if there is no JSP page backing */
    public String getJspURI();

    // ------------------------------------------------------------------------

    /* set the page style html */
    public void setPageStyle(String style);

    /* write the page header to the specified output stream */
    public void writeStyle(PrintWriter out, RequestProperties reqState)
        throws IOException;

    // ------------------------------------------------------------------------

    /* set the page header html */
    public void setPageHeader(String header);

    /* write the page header to the specified output stream */
    public void writeHeader(PrintWriter out, RequestProperties reqState)
        throws IOException;

    // ------------------------------------------------------------------------

    /* set the page navigation html */
    public void setPageNavigation(String navigate);

    /* write the page header to the specified output stream */
    public void writeNavigation(PrintWriter out, RequestProperties reqState)
        throws IOException;

    // ------------------------------------------------------------------------

    /* set the page footer html */
    public void setPageFooter(String footer);

    /* write the page footer to the specified output stream */
    public void writeFooter(PrintWriter out, RequestProperties reqState) 
        throws IOException;

    // ------------------------------------------------------------------------

    /* set the left banner html */
    public void setPageLeft(String left);

    /* write the left page banner to the specified output stream */
    public void writeLeft(PrintWriter out, RequestProperties reqState) 
        throws IOException;

    // ------------------------------------------------------------------------

    /* set the right banner html */
    public void setPageRight(String right);

    /* write the right page banner to the specified output stream */
    public void writeRight(PrintWriter out, RequestProperties reqState) 
        throws IOException;

    // ------------------------------------------------------------------------

}
