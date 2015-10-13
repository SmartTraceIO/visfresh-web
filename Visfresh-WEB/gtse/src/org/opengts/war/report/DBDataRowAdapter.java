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
package org.opengts.war.report;

import java.io.*;

import org.opengts.util.*;

import org.opengts.war.tools.*;

public abstract class DBDataRowAdapter
    implements DBDataRow, CSSRowClass
{

    // ------------------------------------------------------------------------
    
    private ReportData  reportData = null;

    public DBDataRowAdapter()
    {
        //
    }

    public DBDataRowAdapter(ReportData rd)
    {
        this.reportData = rd;
    }

    // ------------------------------------------------------------------------

    public boolean hasCssClass()
    {
        return !StringTools.isBlank(this.getCssClass());
    }

    public String getCssClass()
    {
        Object rowObj = this.getRowObject();
        if ((rowObj instanceof CSSRowClass) && ((CSSRowClass)rowObj).hasCssClass()) {
            return ((CSSRowClass)rowObj).getCssClass();
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------

    public ReportData getReportData()
    {
        return this.reportData;
    }
    
    public ReportColumn[] getReportColumns()
    {
        ReportData rd = this.getReportData();
        return (rd != null)? rd.getReportColumns() : null; // ReportData
    }
    
    public DataRowTemplate getDataRowTemplate()
    {
        ReportData rd = this.getReportData();
        return (rd != null)? rd.getDataRowTemplate() : null;
    }

    // ------------------------------------------------------------------------

    public abstract Object getRowObject();
    
    public abstract Object getDBValue(String fldName, int rowNdx, ReportColumn rptCol);

    // ------------------------------------------------------------------------

    public RowType getRowType()
    {
        DataRowTemplate drt = this.getDataRowTemplate();
        if (drt != null) {
            return drt.getRowType(this.getRowObject());
        } else {
            return RowType.DETAIL;
        }
    }
    
    // ------------------------------------------------------------------------

}
