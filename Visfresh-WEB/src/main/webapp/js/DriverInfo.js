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
//  2013/05/19  Martin D. Flynn
//     -Initial Creation
// ----------------------------------------------------------------------------

var CALENDAR_FADE = false;

// ----------------------------------------------------------------------------

/**
*** Show License Expire Calander
**/
function driverToggleLicExpCalendar()
{
    if (licExpCal) {
        var cal = licExpCal;
        var fld = document.getElementById(ID_LICENSE_EXPIRE);
        if (cal.isExpanded()) {
            // collapse
            cal.setExpanded(false, CALENDAR_FADE);
            if (cal) { fld.value = cal.getDateAsString(); }
        } else {
            // expand
            if (fld) { cal.setDateAsString(fld.value); }
            cal.setExpanded(true, CALENDAR_FADE);
            cal.setCallbackOnSelect(function() {
                cal.setExpanded(false, CALENDAR_FADE);
                if (fld) { fld.value = cal.getDateAsString(); }
            });
        }
    }
};
