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
//  2013/04/08  Martin D. Flynn
//     -Initial release (cloned from "GoogleGeocodeV3.java")
//  2013/05/19  Martin D. Flynn
//     -Tested and tweaked.
// ----------------------------------------------------------------------------
package org.opengts.extra.geocoder.nacgeo;

import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;

import org.opengts.db.*;
import org.opengts.geocoder.*;

public class NacGeoService
    extends ReverseGeocodeProviderAdapter
    implements ReverseGeocodeProvider, GeocodeProvider
{

    // ------------------------------------------------------------------------
    // References:
    //   - http://www.nacgeo.com/geocode.asp
    //
    // API URLs:
    //   - http://mlbs.net/nacgeoservicesv5.1/apidocument.htm#Geocoding
    //
    // Nearest Address: V3 API
	//   - http://mlbs.net/nacgeoservicesV5.1/Geocoding.aspx?UserID=1236549870&Query=43.6288802325726,-79.4135751575232
	//   {"GeocodingResult":{
    //      "InputInfo": "43.6288802325726,-79.4135751575232",
    //      "Latitude": 43.6306411027908,
    //      "Longitude": -79.41350877285,
    //      "NAC": "8CFZL Q84KV",
    //      "Entity": "973 Lake Shore Blvd W, Toronto, ON, M6K, Canada",
    //      "Street": "973 Lake Shore Blvd W",
    //      "City": "Toronto",
    //      "County": "",
    //      "State": "ON",
    //      "ZIP": "M6K",
    //      "Country": "Canada",
    //      "FormattedAddress": "973 Lake Shore Blvd W, Toronto, ON, M6K, Canada",
    //      "EntityType":"Address",
    //      "Confidence":"Medium",
    //      "AllMatchedAddresses": "973 Lake Shore Blvd W,Toronto,,ON,M6K,Canada:43.630641102790833,-79.413508772850037,8CFZL Q84KV:973 Lake Shore Blvd W, Toronto, ON, M6K, Canada;",
    //      "AllMatchedPlaces": "",
    //      "Message": "OK"
	//   }}
    //
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    protected static final String TAG_GeocodingResult           = "GeocodingResult";

    protected static final String TAG_Latitude                  = "Latitude";
    protected static final String TAG_Longitude                 = "Longitude";

    protected static final String TAG_FormattedAddress          = "FormattedAddress";

    protected static final String TAG_Message                   = "Message";   

    protected static final String TAG_status                    = "status";   

    protected static final String TAG_long_name                 = "long_name";
    protected static final String TAG_short_name                = "short_name";
    protected static final String TAG_types                     = "types";
    protected static final String TAG_formatted_address         = "formatted_address";
    protected static final String TAG_geometry                  = "geometry";
    protected static final String TAG_bounds                    = "bounds";
    protected static final String TAG_northeast                 = "northeast";
    protected static final String TAG_southwest                 = "southwest";
    protected static final String TAG_location                  = "location";
    protected static final String TAG_location_type             = "location_type";

    /* V3 URLs */
    protected static final String URL_ReverseGeocode_           = "http://mlbs.net/nacgeoservicesV5.1/Geocoding.aspx?";
    protected static final String URL_Geocode_                  = "http://mlbs.net/nacgeoservicesV5.1/Geocoding.aspx?";
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    protected static final String  PROP_reverseGeocodeURL       = "reverseGeocodeURL";
    protected static final String  PROP_geocodeURL              = "geocodeURL";

    // ------------------------------------------------------------------------

    protected static final int     TIMEOUT_ReverseGeocode       = 2500; // milliseconds
    protected static final int     TIMEOUT_Geocode              = 5000; // milliseconds

    protected static final String  DEFAULT_COUNTRY              = "US"; // http://en.wikipedia.org/wiki/CcTLD

    // ------------------------------------------------------------------------

    protected static final String  STATUS_UNDEFINED             = "?";
    protected static final String  STATUS_OK                    = "OK";
    protected static final String  STATUS_LIMIT_EXCEEDED        = "620";
    protected static final String  STATUS_FORBIDDEN_403         = "403";
    protected static final String  STATUS_NOT_FOUND_404         = "404";
    protected static final String  STATUS_IP_ADDRESS_           = "IP ";

    protected static final JSON    JSON_LIMIT_EXCEEDED          = JSONStatus(STATUS_LIMIT_EXCEEDED);
    protected static final JSON    JSON_FORBIDDEN_403           = JSONStatus(STATUS_FORBIDDEN_403);
    protected static final JSON    JSON_NOT_FOUND_404           = JSONStatus(STATUS_NOT_FOUND_404);

    private static JSON JSONStatus(String status)
    {
        // ("GeocodingResult":{"Message":"OK"})
        StringBuffer J = new StringBuffer();
        J.append("{\"").append(TAG_GeocodingResult).append("\":");
        J.append("{\"").append(TAG_Message).append("\":\"").append(status).append("\"}}");
        String j = J.toString();
        try {
            return new JSON(j);
        } catch (JSON.JSONParsingException jpe) {
            Print.logError("Invalid JSON: " + J);
            return null;
        }
    }

    // ------------------------------------------------------------------------

    /* MUST BE FALSE IN PRODUCTION!!! */
    protected static final boolean FAILOVER_DEBUG               = false;

    // ------------------------------------------------------------------------

    protected static final String  ENCODING_UTF8                = StringTools.CharEncoding_UTF_8;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public NacGeoService(String name, String key, RTProperties rtProps)
    {
        super(name, key, rtProps);
    }

    // ------------------------------------------------------------------------

    public boolean isFastOperation()
    {
        // this is a slow operation
        return super.isFastOperation();
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the Geocode timeout
    **/
    protected int getGeocodeTimeout()
    {
        return TIMEOUT_Geocode;
    }

    /**
    *** Returns the ReverseGeocode timeout
    **/
    protected int getReverseGeocodeTimeout()
    {
        return TIMEOUT_ReverseGeocode;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* return reverse-geocode */
    public ReverseGeocode getReverseGeocode(GeoPoint gp, String localeStr, boolean cache)
    {
        ReverseGeocode rg = this.getAddressReverseGeocode(gp, localeStr, cache);
        return rg;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* nearest address URI */
    protected String getAddressReverseGeocodeURI()
    {
        return URL_ReverseGeocode_;
    }

    /* encode GeoPoint into nearest address URI */
    protected String getAddressReverseGeocodeURL(GeoPoint gp, String localeStr)
    {
        StringBuffer sb = new StringBuffer();

        /* predefined URL */
        String rgURL = this.getProperties().getString(PROP_reverseGeocodeURL,null);
        if (!StringTools.isBlank(rgURL)) {
            // assume "&UserID=" is already part of this URL
            sb.append(rgURL);
            sb.append("&Query=");
            if (gp != null) {
                String lat = gp.getLatitudeString( GeoPoint.SFORMAT_DEC_5,null);
                String lon = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
                sb.append(lat).append(",").append(lon);
            }
            String defURL = sb.toString();
            return defURL;
        }

        /* assemble URL */
        sb.append(this.getAddressReverseGeocodeURI());

        /* UserID */
        String userID = this.getAuthorization();
        if (StringTools.isBlank(userID) || userID.startsWith("*")) {
            // invalid key
        } else {
            sb.append("&UserID=").append(userID);
        }

        /* Query */
        sb.append("&Query=");
        if (gp != null) {
            String lat = gp.getLatitudeString( GeoPoint.SFORMAT_DEC_5,null);
            String lon = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
            sb.append(lat).append(",").append(lon);
        }

        /* Supress error? */
        //sb.append("&SuppressError=yes");

        /* Format */
        sb.append("&Format=JSON");

        /* return url */
        String defURL = sb.toString();
        return defURL;

    }

    /* return reverse-geocode using nearest address */
    public ReverseGeocode getAddressReverseGeocode(GeoPoint gp, String localeStr, boolean cache)
    {

        /* check for failover mode */
        if (this.isReverseGeocodeFailoverMode()) {
            ReverseGeocodeProvider frgp = this.getFailoverReverseGeocodeProvider();
            return frgp.getReverseGeocode(gp, localeStr, cache);
        }

        /* URL */
        String url = this.getAddressReverseGeocodeURL(gp, localeStr);
        Print.logInfo("NacGeo RG URL: " + url);

        /* create JSON document */
        JSON jsonDoc = null;
        JSON._Object jsonObj = null;
        try {
            jsonDoc = GetJSONDocument(url, this.getReverseGeocodeTimeout());
            jsonObj = (jsonDoc != null)? jsonDoc.getObject() : null;
            if (jsonObj == null) {
                return null;
            }
        } catch (Throwable th) {
            Print.logException("Error", th);
        }

        /* parse address */
        String status = STATUS_UNDEFINED;
        String address = null;
        JSON._Object results = jsonObj.getObjectForName(TAG_GeocodingResult, null);
        if (results != null) {
            status  = results.getStringForName(TAG_Message, ""); // expect "OK"
            address = results.getStringForName(TAG_FormattedAddress, null);
        } else {
            Print.logInfo("No address found: null");
        }
        status = StringTools.trim(status);

        /* create address */
        if (FAILOVER_DEBUG) {
            status = STATUS_LIMIT_EXCEEDED;
        } else 
        if (!StringTools.isBlank(address)) {
            // address found 
            Print.logDebug("Address: " + address);
            ReverseGeocode rg = new ReverseGeocode();
            rg.setFullAddress(address);
            return rg;
        } else
        if ((status.equals(STATUS_OK) || status.equals(""))) {
            // address not found, but status indicates successful
            Print.logDebug("No Address found for location: " + gp);
            ReverseGeocode rg = new ReverseGeocode();
            rg.setFullAddress("");
            return rg;
        }

        /* check for failover */
        boolean failover = false;
        if (status.startsWith(STATUS_IP_ADDRESS_)) {
            // "IP (xx.xx.xx.xx) is not correct or your account runs out of fund."
            Print.logError("NacGeo IP address invalid/expired! ["+status+"]");
            failover = true;
        } else
        if (status.equals(STATUS_UNDEFINED)) {
            // undefined status? (only if no result was returned)
            Print.logError("NacGeo Reverse-Geocode Undefined! ["+status+"]");
            failover = true;
        } else
        if (status.equals(STATUS_LIMIT_EXCEEDED)) {
            // NOTE: not verified that NacGeo returns this error under these conditions
            Print.logError("NacGeo Reverse-Geocode Limit Exceeded! ["+status+"]");
            failover = true;
        } else
        if (status.equals(STATUS_FORBIDDEN_403)) {
            // NOTE: not verified that NacGeo returns this error under these conditions
            Print.logError("NacGeo Reverse-Geocode Not Authorized! ["+status+"]");
            failover = true;
        } else {
            Print.logError("NacGeo Unrecognized Error! ["+status+"]");
            failover = false;
        }
        // failover?
        if (failover && this.hasFailoverReverseGeocodeProvider()) {
            this.startReverseGeocodeFailoverMode();
            ReverseGeocodeProvider frgp = this.getFailoverReverseGeocodeProvider();
            Print.logWarn("Failing over to '" + frgp.getName() + "'");
            return frgp.getReverseGeocode(gp, localeStr, cache);
        }

        /* no reverse-geocode available */
        return null;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* nearest address URI */
    protected String getGeoPointGeocodeURI()
    {
        return URL_Geocode_;
    }

    /* encode GeoPoint into nearest address URI */
    protected String getGeoPointGeocodeURL(String address, String country)
    {
        StringBuffer sb = new StringBuffer();

        /* predefined URL */
        String gcURL = this.getProperties().getString(PROP_geocodeURL,null);
        if (!StringTools.isBlank(gcURL)) {
            // assume "&UserID=" is already part of this URL
            sb.append(gcURL);
            sb.append("&Query=").append(URIArg.encodeArg(address));
            if (!StringTools.isBlank(country)) {
                // country code bias: http://en.wikipedia.org/wiki/CcTLD
                sb.append("&country=").append(country);
            }
            String defURL = sb.toString();
            return defURL;
        }

        /* assemble URL */
        sb.append(this.getGeoPointGeocodeURI());

        /* UserID */
        String userID = this.getAuthorization();
        if (StringTools.isBlank(userID) || userID.startsWith("*")) {
            // invalid key
        } else {
            sb.append("&UserID=").append(userID);
        }

        /* address/country */
        sb.append("&Query=").append(URIArg.encodeArg(address));
        if (!StringTools.isBlank(country)) {
            sb.append("&country=").append(country);
        }

        /* Supress error? */
        //sb.append("&SuppressError=yes");

        /* Format */
        sb.append("&Format=JSON");

        /* return url */
        String defURL = sb.toString();
        return defURL;

    }

    /* return geocode */
    public GeoPoint getGeocode(String address, String country)
    {

        /* URL */
        String url = this.getGeoPointGeocodeURL(address, country);
        Print.logDebug("NacGeo GC URL: " + url);

        /* create JSON document */
        JSON jsonDoc = GetJSONDocument(url, this.getReverseGeocodeTimeout());
        JSON._Object jsonObj = (jsonDoc != null)? jsonDoc.getObject() : null;
        if (jsonObj == null) {
            return null;
        }

        /* parse GeoPoint */
        String status = STATUS_UNDEFINED;
        GeoPoint geoPoint = null;
        JSON._Object results = jsonObj.getObjectForName(TAG_GeocodingResult, null);
        if (results != null) {
            status = StringTools.trim(results.getStringForName(TAG_Message,"")); // expect "OK"
            double lat = results.getDoubleForName(TAG_Latitude,0.0);
            double lon = results.getDoubleForName(TAG_Longitude,0.0);
            if (GeoPoint.isValid(lat,lon)) {
                geoPoint = new GeoPoint(lat,lon);
            }
        } else {
            Print.logDebug("'GeocodingResult' is null");
        }

        /* create address */
        if (geoPoint != null) {
            // GeoPoint found 
            Print.logDebug("GeoPoint: " + geoPoint);
            return geoPoint;
        } else
        if ((status.equals(STATUS_OK) || status.equals(""))) {
            Print.logDebug("No GeoPoint returned for address: " + address);
            return null;
        }

        /* check for errors */
        if (status.startsWith(STATUS_IP_ADDRESS_)) {
            // "IP (xx.xx.xx.xx) is not correct or your account runs out of fund."
            Print.logError("NacGeo IP address invalid/expired! ["+status+"]");
        } else
        if (status.equals(STATUS_UNDEFINED)) {
            Print.logError("NacGeo Geocode Undefined! ["+status+"]");
        } else
        if (status.equals(STATUS_LIMIT_EXCEEDED)) {
            // NOTE: not verified that NacGeo returns this error under these conditions
            Print.logError("NacGeo Geocode Limit Exceeded! ["+status+"]");
        }

        /* no reverse-geocode available */
        return null;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    protected static JSON GetJSONDocument(String url, int timeoutMS)
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
            if (rc == 403) {
                jsonDoc = JSON_FORBIDDEN_403; // STATUS_FORBIDDEN_403: not authorized
            } else
            if (rc == 404) {
                jsonDoc = JSON_NOT_FOUND_404; // STATUS_NOT_FOUND_404: path not found
            }
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
    // ------------------------------------------------------------------------
    
    private static final String ARG_ACCOUNT[]       = new String[] { "account", "a"  };
    private static final String ARG_GEOCODE[]       = new String[] { "geocode", "gc" };
    private static final String ARG_REVGEOCODE[]    = new String[] { "revgeo" , "rg" };
    
    private static String FilterID(String id)
    {
        if (id == null) {
            return null;
        } else {
            StringBuffer newID = new StringBuffer();
            int st = 0;
            for (int i = 0; i < id.length(); i++) {
                char ch = Character.toLowerCase(id.charAt(i));
                if (Character.isLetterOrDigit(ch)) {
                    newID.append(ch);
                    st = 1;
                } else
                if (st == 1) {
                    newID.append("_");
                    st = 0;
                } else {
                    // ignore char
                }
            }
            while ((newID.length() > 0) && (newID.charAt(newID.length() - 1) == '_')) {
                newID.setLength(newID.length() - 1);
            }
            return newID.toString();
        }
    }

    /**
    *** Main entery point for debugging/testing
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        Print.setAllOutputToStdout(true);
        Print.setEncoding(ENCODING_UTF8);
        String accountID = RTConfig.getString(ARG_ACCOUNT,"demo");
        NacGeoService gn = new NacGeoService("nacgeo", null, null);

        /* [ENTERPRISE] geocode */
        if (RTConfig.hasProperty(ARG_GEOCODE)) {
            String address = RTConfig.getString(ARG_GEOCODE,null);
            if (FileTools.isFile(address)) {
                StringBuffer sb = new StringBuffer();
                sb.append("\"accountID\",");
                sb.append("\"geozoneID\",");
                sb.append("\"sortID\",");
                sb.append("\"description\",");
                sb.append("\"radius\",");
                sb.append("\"latitude1\",");
                sb.append("\"longitude1\"");
                Print.sysPrintln(sb.toString());
                String aList[] = StringTools.split(StringTools.toStringValue(FileTools.readFile(address)),'\n');
                int zoneCnt = 0;
                char stipChars[] = new char[] { '\"' };
                for (String a : aList) {
                    String dftzoneID = "zone_" + (++zoneCnt);
                    String geozoneID = dftzoneID;
                    int    sortID    = 0;
                    String descr     = "";
                    int    radiusM   = 200; 
                    String addr      = "";
                    String f[]       = StringTools.split(a,'|');
                    if (ListTools.size(f) == 1) {
                        geozoneID = dftzoneID;
                        descr     = StringTools.stripChars(StringTools.trim(f[0]),stipChars);
                        addr      = StringTools.stripChars(StringTools.trim(f[0]),stipChars);
                    } else 
                    if (ListTools.size(f) >= 3) {
                        geozoneID = StringTools.blankDefault(FilterID(f[0]),dftzoneID);
                        descr     = StringTools.stripChars(StringTools.trim(f[0]),stipChars);
                        sortID    = 0;
                        radiusM   = StringTools.parseInt(f[1],200); 
                        addr      = StringTools.stripChars(StringTools.trim(f[2]),stipChars);
                    }
                    // skip if no address
                    if (StringTools.isBlank(addr)) {
                        continue;
                    }
                    // truncate "geozoneID" to 32 characters max
                    int maxLen_geozoneID = 32;
                    if (geozoneID.length() > maxLen_geozoneID) {
                        // trucate length
                        geozoneID = geozoneID.substring(0, maxLen_geozoneID);
                        if (geozoneID.endsWith("_")) {
                            geozoneID = geozoneID.substring(0, geozoneID.length() - 1);
                        }
                    }
                    // geocode
                    GeoPoint gp = gn.getGeocode(addr, DEFAULT_COUNTRY);
                    if ((gp != null) && gp.isValid()) {
                        // accountID, geozoneID, sortID, radius, latitude1, longitude1
                        sb.setLength(0);
                        sb.append("\"").append(accountID        ).append("\",");
                        sb.append("\"").append(geozoneID        ).append("\",");
                        sb.append("")  .append(sortID           ).append(",");
                        sb.append("\"").append(descr            ).append("\",");
                        sb.append("")  .append(radiusM          ).append(",");
                        sb.append("")  .append(gp.getLatitude() ).append(",");
                        sb.append("")  .append(gp.getLongitude()).append("");
                        Print.sysPrintln(sb.toString());
                    } else {
                        Print.sysPrintln("// GPS location not found for address: " + addr);
                    }
                }
            } else {
                GeoPoint gp = gn.getGeocode(address, DEFAULT_COUNTRY);
                Print.sysPrintln("Location " + gp);
            }
            System.exit(0);
        }
        /* reverse geocode */
        if (RTConfig.hasProperty(ARG_REVGEOCODE)) {
            GeoPoint gp = new GeoPoint(RTConfig.getString(ARG_REVGEOCODE,null));
            if (!gp.isValid()) {
                Print.logInfo("Invalid GeoPoint specified");
                System.exit(1);
            }
            Print.logInfo("Reverse-Geocoding GeoPoint: " + gp);
            Print.sysPrintln("RevGeocode = " + gn.getReverseGeocode(gp,null/*localeStr*/,false/*cache*/));
            // Note: Even though the values are printed in UTF-8 character encoding, the
            // characters may not appear to be properly displayed if the console display
            // does not support UTF-8.
            System.exit(0);
        }

        /* no options */
        Print.sysPrintln("No options specified");
        System.exit(1);

    }

}
