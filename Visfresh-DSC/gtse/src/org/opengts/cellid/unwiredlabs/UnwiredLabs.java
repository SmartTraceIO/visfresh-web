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
//  2014/09/26  Martin D. Flynn
//     -Initial release
//  2015/08/16  Martin D. Flynn
//     -Added support for neighoring cell-towers
// ----------------------------------------------------------------------------
package org.opengts.cellid.unwiredlabs;

import java.util.*;
import java.io.*;
import java.net.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.opengts.util.*;

import org.opengts.db.*;
import org.opengts.cellid.*;

public class UnwiredLabs
    extends MobileLocationProviderAdapter
    implements MobileLocationProvider
{

    private static final String  VERSION                        = "0.1.1";

    // ------------------------------------------------------------------------
    //
    // References:
    //   - https://unwiredlabs.com/api
    //
    // ------------------------------------------------------------------------

    private static final String  MOBILE_LOCATION_US_EAST        = "https://us1.unwiredlabs.com/v2/process.php"; // (Northern Virginia)
    private static final String  MOBILE_LOCATION_US_WEST        = "https://us2.unwiredlabs.com/v2/process.php"; // (San Francisco)
    private static final String  MOBILE_LOCATION_EUROPE         = "https://eu1.unwiredlabs.com/v2/process.php"; // (Ireland)
    private static final String  MOBILE_LOCATION_ASIA_PACIFIC   = "https://ap1.unwiredlabs.com/v2/process.php"; // (Singapore)

    // ------------------------------------------------------------------------

    private static final String  PROP_timeoutMS                 = "timeoutMS";

    // ------------------------------------------------------------------------

    private static final long    DefaultServiceTimeout          = 5000L; // milliseconds

    // ------------------------------------------------------------------------

    private static final String  TAG_token                      = "token";
    private static final String  TAG_radio                      = "radio";
    private static final String  TAG_mcc                        = "mcc";
    private static final String  TAG_mnc                        = "mnc";
    private static final String  TAG_cells                      = "cells";
    private static final String  TAG_lac                        = "lac";
    private static final String  TAG_cid                        = "cid";
    private static final String  TAG_signal                     = "signal";
    private static final String  TAG_tA                         = "tA";
    private static final String  TAG_asu                        = "asu";
    private static final String  TAG_psc                        = "psc";
    private static final String  TAG_address                    = "address";

    private static final String  TAG_status                     = "status";
    private static final String  TAG_balance                    = "balance";
    private static final String  TAG_message                    = "message";
    private static final String  TAG_lat                        = "lat";
    private static final String  TAG_lon                        = "lon";
    private static final String  TAG_accuracy                   = "accuracy";
    private static final String  TAG_aged                       = "aged";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static MobileLocation _getMobileLocation(
        CellTower servCT, CellTower nborCT[], 
        String token, long timeoutMS)
    {
        
        /* no serving cell-tower? */
        if (servCT == null) {
            Print.logWarn("Serving Cell-Tower is null");
            return null;
        }

        /* URL */
        String url = MOBILE_LOCATION_US_EAST;
        if (StringTools.isBlank(url)) {
            Print.logWarn("Cell-Tower web-service URL is blank/null");
            return null;
        }

        /* encode JSON request */
        JSON._Object jsonReq = new JSON._Object();
        jsonReq.addKeyValue(TAG_token , StringTools.trim(token));
      //jsonReq.addKeyValue(TAG_radio , "gsm"); // "gsm", "cdma", "umts", "lte"
        if (servCT.hasRadioType())             { jsonReq.addKeyValue(TAG_radio , servCT.getRadioType());             }
        if (servCT.hasMobileCountryCode())     { jsonReq.addKeyValue(TAG_mcc   , servCT.getMobileCountryCode());     }
        if (servCT.hasMobileNetworkCode())     { jsonReq.addKeyValue(TAG_mnc   , servCT.getMobileNetworkCode());     }
        JSON._Array  cellReqArry = new JSON._Array();
        // -- serving cell-tower
        JSON._Object servCellReq = new JSON._Object();
        if (servCT.hasCellTowerID())           { servCellReq.addKeyValue(TAG_cid   , servCT.getCellTowerID());           }
        if (servCT.hasLocationAreaCode())      { servCellReq.addKeyValue(TAG_lac   , servCT.getLocationAreaCode());      }
        if (servCT.hasMobileCountryCode())     { servCellReq.addKeyValue(TAG_mcc   , servCT.getMobileCountryCode());     }
        if (servCT.hasMobileNetworkCode())     { servCellReq.addKeyValue(TAG_mnc   , servCT.getMobileNetworkCode());     }
        if (servCT.hasReceptionLevel())        { servCellReq.addKeyValue(TAG_signal, servCT.getReceptionLevel());        }
        if (servCT.hasTimingAdvance())         { servCellReq.addKeyValue(TAG_tA    , servCT.getTimingAdvance());         }
        if (servCT.hasPrimaryScramblingCode()) { servCellReq.addKeyValue(TAG_psc   , servCT.getPrimaryScramblingCode()); }
        cellReqArry.addValue(servCellReq);
        // -- neighbor cell-towers
        if (!ListTools.isEmpty(nborCT)) {
            for (CellTower nCT : nborCT) {
                JSON._Object nborCellReq = new JSON._Object();
                if (nCT.hasCellTowerID())           { nborCellReq.addKeyValue(TAG_cid   , servCT.getCellTowerID());           }
                if (nCT.hasLocationAreaCode())      { nborCellReq.addKeyValue(TAG_lac   , servCT.getLocationAreaCode());      }
                if (nCT.hasMobileCountryCode())     { nborCellReq.addKeyValue(TAG_mcc   , servCT.getMobileCountryCode());     }
                if (nCT.hasMobileNetworkCode())     { nborCellReq.addKeyValue(TAG_mnc   , servCT.getMobileNetworkCode());     }
                if (nCT.hasReceptionLevel())        { nborCellReq.addKeyValue(TAG_signal, servCT.getReceptionLevel());        }
                if (nCT.hasTimingAdvance())         { nborCellReq.addKeyValue(TAG_tA    , servCT.getTimingAdvance());         }
                if (nCT.hasPrimaryScramblingCode()) { nborCellReq.addKeyValue(TAG_psc   , servCT.getPrimaryScramblingCode()); }
                cellReqArry.addValue(nborCellReq);
            }
        }
        // -- 
        jsonReq.addKeyValue(TAG_cells, cellReqArry);
        jsonReq.addKeyValue(TAG_address, 0);

        /* get HTTP result */
        JSON._Object jsonResp = null;
        try {
            Print.logDebug("CellTower loc URL: " + url);
            byte reqB[] = jsonReq.toString(true).getBytes();
            byte rspB[] = HTMLTools.readPage_POST(url, HTMLTools.MIME_JSON(), reqB, (int)timeoutMS);
            if (ListTools.isEmpty(rspB)) {
                // -- invalid response
                return null;
            } 
            jsonResp = JSON.parse_Object(StringTools.toStringValue(rspB));
        } catch (JSON.JSONParsingException jpe) {
            // -- invalid JSON
            return null;
        } catch (Throwable th) {
            // -- timeout, invalid response
            return null;
        }

        /* status/balance */
        String status  = jsonResp.getStringForName(TAG_status ,"");
        int    balance = jsonResp.getIntForName(   TAG_balance,-1);
        String message = jsonResp.getStringForName(TAG_message,"");
        if (status.equalsIgnoreCase("ok")) {
            // -- invalid status
            Print.logError("Invalid status: " + status + " [balance " + balance + "] " + message);
            return null;
        }

        /* parse lat/lon */
        double latitude  = jsonResp.getDoubleForName(TAG_lat     ,0.0);
        double longitude = jsonResp.getDoubleForName(TAG_lon     ,0.0);
        double accuracy  = jsonResp.getDoubleForName(TAG_accuracy,0.0);
 
        /* valid GeoPoint? */
        if (GeoPoint.isValid(latitude,longitude)) {
            return new MobileLocation(latitude,longitude,accuracy);
        } else {
            return null;
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // MobileLocationProvider interface

    public UnwiredLabs(String name, String key, RTProperties rtProps)
    {
        super(name, key, rtProps);
    }

    public MobileLocation getMobileLocation(CellTower servCT, CellTower nborCT[]) 
    {
        long tmoMS = this.getProperties().getLong(PROP_timeoutMS, DefaultServiceTimeout);
        return UnwiredLabs._getMobileLocation(servCT, nborCT, this.getAuthorization(), tmoMS);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Main entery point for debugging/testing
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        Print.setAllOutputToStdout(true);
        Print.setEncoding(StringTools.CharEncoding_UTF_8);

        /* geocode lookup */
        CellTower ct = new CellTower();
        ct.setCellTowerID(565110);
        ct.setMobileNetworkCode(8);
        ct.setMobileCountryCode(240);
        ct.setLocationAreaCode(318);

        /* get CellTower location */
        String key = RTConfig.getString(new String[]{"key","auth"}, "");
        UnwiredLabs mobLoc = new UnwiredLabs("unwiredlabs", key, null);
        MobileLocation ml = mobLoc.getMobileLocation(ct, null);
        Print.logInfo("Mobile Location: " + ml);

    }

}
