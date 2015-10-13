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
// NOT YET COMPLETE
// ----------------------------------------------------------------------------
// Change History:
//  2017/08/16  Martin D. Flynn
//     -Initial release (not yet fully supported)
// ----------------------------------------------------------------------------
package org.opengts.geocoder.google;

import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;

import org.opengts.db.*;
import org.opengts.geocoder.*;

public class GoogleRoads
    extends ReverseGeocodeProviderAdapter
{

    // ------------------------------------------------------------------------
    // References:
    //  - https://developers.google.com/maps/documentation/roads/speed-limits
    //  - https://developers.google.com/console/help/new/#usingkeys
    //  - https://console.developers.google.com/project/164795502565/apiui/credential
    //
    // Speed-Limit API
    //  - https://roads.googleapis.com/v1/speedLimits?key=API_KEY&path=60.170880,24.942795
    //  - https://roads.googleapis.com/v1/speedLimits?key=API_KEY&placeId=ChIJ1Wi6I2pNFmsRQL9GbW7qABM
    //    {
    //      "speedLimits": [
    //        {
    //          "placeId": "ChIJ1Wi6I2pNFmsRQL9GbW7qABM",
    //          "speedLimit": 104.60736,
    //          "units": "KPH"
    //        }
    //      ]
    //    }
    // ------------------------------------------------------------------------

    public    static final String   TAG_speedLimits         = "speedLimits";
    public    static final String   TAG_placeId             = "placeId";
    public    static final String   TAG_speedLimit          = "speedLimit";
    public    static final String   TAG_units               = "units";      // "KPH"|"MPH"

    /* V1 URL */
    protected static final String   URL_SpeedLimits_        = "https://roads.googleapis.com/v1/speedLimits?";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    protected static final String   PROP_speedLimitsURL     = "speedLimitsURL";
    protected static final String   PROP_roadsApiKey        = "roadsApiKey";

    // ------------------------------------------------------------------------

    protected static final int      TIMEOUT_SpeedLimits     = 2500; // milliseconds

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static JSON GetJSONDocument(String url, int timeoutMS)
    {
        JSON jsonDoc = null;
        HTMLTools.HttpBufferedInputStream input = null;
        try {
            input = HTMLTools.inputStream_GET(url, timeoutMS);
            jsonDoc = new JSON(input);
        } catch (JSON.JSONParsingException jpe) {
            Print.logError("JSON parse error: " + jpe);
        } catch (HTMLTools.HttpIOException hioe) {
            // IO error: java.io.IOException: 
            int    rc = hioe.getResponseCode();
            String rm = hioe.getResponseMessage();
            Print.logError("HttpIOException ["+rc+"-"+rm+"]: " + hioe.getMessage());
        } catch (IOException ioe) {
            Print.logError("IOException: " + ioe.getMessage());
        } finally {
            if (input != null) {
                try { input.close(); } catch (Throwable th) {/*ignore*/}
            }
        }
        return jsonDoc;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public GoogleRoads(String name, String key, RTProperties rtProps)
    {
        super(name, key, rtProps);
    }

    // ------------------------------------------------------------------------

    public boolean isFastOperation()
    {
        // -- this is a slow operation
        return false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the SpeedLimits timeout
    **/
    protected int getSpeedLimitsTimeout()
    {
        return TIMEOUT_SpeedLimits;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* return reverse-geocode */
    public ReverseGeocode getReverseGeocode(GeoPoint gp, String localeStr, boolean cache)
    {

        /* check "cache" state */
        // -- NOTE: This method depends on "cache" being true when stopped, and false when 
        // -  moving (ie. speed > 0).  See EventData.updateAddress(...) for more info.
        boolean isStopped = cache;      // true if stopped, false if moving
        boolean isMoving  = !isStopped; // moving is opposite of stopped
        
        /* URL */
        String url = this.getSpeedLimitsURL(gp, localeStr);
        Print.logInfo("Google Roads URL: " + url);

        /* create JSON document */
        JSON jsonDoc = null;
        JSON._Object jsonObj = null;
        try {
            jsonDoc = GetJSONDocument(url, this.getSpeedLimitsTimeout());
            //Print.logInfo("Response:\n"+jsonDoc);
            jsonObj = (jsonDoc != null)? jsonDoc.getObject() : null;
            if (jsonObj == null) {
                Print.logWarn("Unable to obtain top-level JSON object");
                return null;
            }
        } catch (Throwable th) {
            Print.logException("Error", th);
            return null;
        }

        /* parse speed limit */
        JSON._Array speedLimits = jsonObj.getArrayForName(TAG_speedLimits,null);
        if (ListTools.isEmpty(speedLimits)) {
            // -- nothing to report
            return null;
        }

        /* iterate through speed limits */
        int slSize = speedLimits.size();
        for (int n = 0; n < slSize; n++) {
            JSON._Object speedLimitObj = speedLimits.getObjectValueAt(n,null);
            if (speedLimitObj != null) {
                String speedUnits = speedLimitObj.getStringForName(TAG_units, "KPH");
                double speedLimit = speedLimitObj.getDoubleForName(TAG_speedLimit, -1.0);
                if (speedLimit >= 0.0) {
                    ReverseGeocode rg = new ReverseGeocode();
                    if (speedUnits.equalsIgnoreCase("MPH")) {
                        rg.setSpeedLimitKPH(speedLimit * GeoPoint.KILOMETERS_PER_MILE);
                    } else {
                        rg.setSpeedLimitKPH(speedLimit);
                    }
                    return rg;
                }
            }
        }

        /* not found */
        return null;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the authorization key of this ReverseGeocodeProvider
    *** @return The access key of this reverse-geocode provider
    **/
    public String getAuthorization()
    {
        String apiKey = this.getProperties().getString(PROP_roadsApiKey,null);
        if (!StringTools.isBlank(apiKey)) {
            return apiKey;
        } else {
            return super.getAuthorization();
        }
    }

    // ------------------------------------------------------------------------

    /* nearest address URI */
    protected String getSpeedLimitsURI()
    {
        return URL_SpeedLimits_;
    }

    /* encode GeoPoint into nearest address URI */
    protected String getSpeedLimitsURL(GeoPoint gp, String localeStr)
    {
        String gps = (gp != null)? (gp.getLatitudeString(GeoPoint.SFORMAT_DEC_5,null)+","+gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null)) : "";

        /* predefined URL */
        String rgURL = this.getProperties().getString(PROP_speedLimitsURL,null); // must already contain API_KEY
        if (!StringTools.isBlank(rgURL)) {
            URIArg uriArg = new URIArg(rgURL, true);
            uriArg.addArg("path", gps);
            return uriArg.toString();
        }

        /* assemble URL */
        URIArg uriArg = new URIArg(this.getSpeedLimitsURI(), true);

        /* latitude/longitude */
        uriArg.addArg("units", "KPH");
        uriArg.addArg("path", gps);

        /* API_KEY */
        String apiKey = this.getAuthorization(); // API_KEY
        if (StringTools.isBlank(apiKey) || apiKey.startsWith("*")) {
            // -- invalid key
        } else {
            uriArg.addArg("key", apiKey);
        }

        /* return url */
        return uriArg.toString();

    }

    // ------------------------------------------------------------------------

}
