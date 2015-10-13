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
//  2009/10/02  Martin D. Flynn
//     -Initial Release
// ----------------------------------------------------------------------------

// ----------------------------------------------------------------------------

function deviceAlertOnLoad()
{
    startAlertRefreshTimer();
    if (AlertActiveCount > 0) {
        // play sound!
        //alert("Has Active Alerts!");
    }
};

// ----------------------------------------------------------------------------

/* sets the AlertRefreshButton button text */
var _alertRefreshButtonElem = null;
function _setAlertRefreshButtonText(text)
{
    if (_alertRefreshButtonElem == null) {
        _alertRefreshButtonElem = document.getElementById(ID_ALERT_REFRESH_BTN);
    }
    if (_alertRefreshButtonElem != null) { 
        _alertRefreshButtonElem.value = text; 
    }
};

// ----------------------------------------------------------------------------

var MIN_REFRESH_INTERVAL = 10;

function startAlertRefreshTimer() 
{
    if (AlertRefreshInterval > 0) {
        AlertRefreshCount = (AlertRefreshInterval > MIN_REFRESH_INTERVAL)? AlertRefreshInterval : MIN_REFRESH_INTERVAL;
        AlertRefreshTimer = setInterval('_timerAlertRefresh()',1000); // setTimeout
        _setAlertRefreshButtonText(TEXT_Refresh + " : " + AlertRefreshCount);
    } else {
        _setAlertRefreshButtonText(TEXT_Refresh);
    }
};

/* periodic map update timer target */
function _timerAlertRefresh() 
{
    if (--AlertRefreshCount <= 0) {
        // timer expired
        var dev = getCheckedRadioValue(document.DeviceAlertsSelect.device); // PARM_DEVICE
        var url = REFRESH_URL + "&device=" + strEncode(dev);
        //alert("URL: " + url);
        openURL(url, "_self");
        AlertRefreshCount = (AlertRefreshInterval > MIN_REFRESH_INTERVAL)? AlertRefreshInterval : MIN_REFRESH_INTERVAL; // start over
    }
    _setAlertRefreshButtonText(TEXT_Refresh + " : " + AlertRefreshCount);
};

// ----------------------------------------------------------------------------
