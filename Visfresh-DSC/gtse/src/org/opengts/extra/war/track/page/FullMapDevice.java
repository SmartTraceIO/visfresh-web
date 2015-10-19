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
//  2011/10/03  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.extra.war.track.page;

import java.util.TimeZone;
import java.util.Iterator;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.track.*;

public class FullMapDevice
    extends FullMap
{
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // WebPage interface
    
    public FullMapDevice()
    {
        this.setBaseURI(RequestProperties.TRACK_BASE_URI());
        this.setPageName(PAGE_MAP_DEVICE);
        this.setPageNavigation(new String[] { PAGE_LOGIN, PAGE_MENU_TOP });
        this.setLoginRequired(true);
        this.setFleet(false);
    }

    // ------------------------------------------------------------------------

    public String getMenuName(RequestProperties reqState)
    {
        return MenuBar.MENU_TRACK_DEVICE;
    }

    public String getMenuDescription(RequestProperties reqState, String parentMenuName)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N   i18n            = privLabel.getI18N(FullMapDevice.class);
        String devTitles[]     = reqState.getDeviceTitles();
        return super._getMenuDescription(reqState,i18n.getString("FullMapDevice.menuDesc","Track {0} locations on a map", devTitles));
    }
   
    public String getMenuHelp(RequestProperties reqState, String parentMenuName)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N   i18n            = privLabel.getI18N(FullMapDevice.class);
        String devTitles[]     = reqState.getDeviceTitles();
        return super._getMenuHelp(reqState,i18n.getString("FullMapDevice.menuHelp","Select and Track the location of a {0} on a map", devTitles));
    }

    // ------------------------------------------------------------------------

    public String getNavigationDescription(RequestProperties reqState)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N   i18n            = privLabel.getI18N(FullMapDevice.class);
        String devTitles[]     = reqState.getDeviceTitles();
        return super._getNavigationDescription(reqState,i18n.getString("FullMapDevice.navDesc","{0}", devTitles));
    }

    public String getNavigationTab(RequestProperties reqState)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N   i18n            = privLabel.getI18N(FullMapDevice.class);
        String devTitles[]     = reqState.getDeviceTitles();
        return super._getNavigationTab(reqState,i18n.getString("FullMapDevice.navTab","{0} Map", devTitles));
    }

    // ------------------------------------------------------------------------

}
