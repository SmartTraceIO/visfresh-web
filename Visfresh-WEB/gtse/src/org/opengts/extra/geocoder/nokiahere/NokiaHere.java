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
//  2015/01/11  Martin D. Flynn
//     -Initial release (cloned from "NacGeoService.java")
//     -Updated to support speed-limit RG when moving, and standard RG when not [2.5.8-B65]
// ----------------------------------------------------------------------------
package org.opengts.extra.geocoder.nokiahere;

import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;

import org.opengts.db.*;
import org.opengts.geocoder.*;
import org.opengts.geocoder.country.*;

public class NokiaHere
    extends ReverseGeocodeProviderAdapter
    implements ReverseGeocodeProvider, GeocodeProvider
{
    
    // ------------------------------------------------------------------------
    // References:
    //   - https://developer.here.com/documentation
    //   - https://developer.here.com/documentation/download/geocoding_nlp/6.2.67/Geocoder%20API%20v6.2.67%20Developer%27s%20Guide.pdf
    //   - https://developer.here.com/documentation/download/routing_nlp/7.2.52/Routing%20API%20v7.2.52%20Developer%27s%20Guide.pdf
    // RG: Nearest Address: 
	//   - http://reverse.geocoder.cit.api.here.com/6.2/reversegeocode.json
    //       ?gen=8&mode=retrieveAddresses
    //       &app_id=APP_ID&app_code=APP_CODE
    //       &prox=LATITUDE,LONGITUDE,RADIUS
    //     JSON response:
    //       {
    //          "Response": {
    //             "MetaInfo": {
    //                "Timestamp": "2015-01-11T22:01:37.939+0000"
    //             },
    //             "View": [
    //                {
    //                   "_type": "SearchResultsViewType",
    //                   "ViewId": 0,
    //                   "Result": [
    //                      {
    //                         "Relevance": 1.0,
    //                         "Distance": -9585.8,
    //                         "Direction": 31.7,
    //                         "MatchLevel": "postalCode",
    //                         "MatchQuality": {
    //                            "Country": 1.0,
    //                            "State": 1.0,
    //                            "County": 1.0,
    //                            "PostalCode": 1.0
    //                         },
    //                         "Location": {
    //                            "LocationId": "K7pvdb+TFkQcSidbS2eAdA",
    //                            "LocationType": "area",
    //                            "DisplayPosition": {
    //                               "Latitude": 39.26605,
    //                               "Longitude": -121.00973
    //                            },
    //                            "MapView": {
    //                               "TopLeft": {
    //                                  "Latitude": 39.52683,
    //                                  "Longitude": -121.2799
    //                               },
    //                               "BottomRight": {
    //                                  "Latitude": 39.00528,
    //                                  "Longitude": -120.00372
    //                               }
    //                            },
    //                            "Address": {
    //                               "Label": "95949, CA, United States",
    //                               "Country": "USA",
    //                               "State": "CA",
    //                               "County": "Nevada",
    //                               "PostalCode": "95949",
    //                               "AdditionalData": [
    //                                  {
    //                                     "value": "United States",
    //                                     "key": "CountryName"
    //                                  },
    //                                  {
    //                                     "value": "California",
    //                                     "key": "StateName"
    //                                  }
    //                               ]
    //                            },
    //                            "MapReference": {
    //                               "ReferenceId": "957821797",
    //                               "MapId": "NAAM143W4",
    //                               "MapVersion": "Q3/2014",
    //                               "SideOfStreet": "neither",
    //                               "CountryId": "21000001",
    //                               "StateId": "21009408",
    //                               "CountyId": "21009885"
    //                            }
    //                         }
    //                      }
    //                   ]
    //                }
    //             ]
    //          }
    //       }
    // Speed Limit: 
    //   - http://reverse.geocoder.cit.api.here.com/routing/6.2/getlinkinfo.json
    //       ?app_id=your_app_id&app_code=your_app_code
    //       &waypoint=52.5308,13.3846
    //   - http://route.st.nlp.nokia.com/routing/6.2/getlinkinfo.json
    //       ?app_id=your_app_id&app_code=your_app_code
    //       &waypoint=52.5308,13.3846
    //     JSON response:
    //       {
    //          "Response": {
    //             "MetaInfo": {
    //                "MapVersion": "2014Q2",
    //                "ModuleVersion": "0.2",
    //                "InterfaceVersion": "4.2",
    //                "Timestamp": "2015-01-03T05:03:48.485Z"
    //             },
    //             "Link": [
    //                {
    //                   "_type": "PrivateTransportLinkType",
    //                   "LinkId": "-931447246",
    //                   "Shape": [
    //                      "52.5309486,13.38447",
    //                      "52.5306702,13.38344",
    //                      "52.5305786,13.38307"
    //                   ],
    //                   "SpeedLimit": 13.89,
    //                   "DynamicSpeedInfo": {
    //                      "TrafficSpeed": 12.5,
    //                      "TrafficTime": 8.2,
    //                      "BaseSpeed": 13.89,
    //                      "BaseTime": 7.4
    //                   },
    //                   "Address": {
    //                      "Label": "InvalidenstraBe",
    //                      "Country": "DE",
    //                      "State": "Berlin",
    //                      "County": "Berlin",
    //                      "City": "Berlin",
    //                      "District": "Mitte",
    //                      "Street": "InvalidenstraBe"
    //                   }
    //                }
    //             ]
    //          }
    //       }
    // GC: Geocode 
    //  - http://geocoder.cit.api.here.com/6.2/geocode.json
    //      ?gen=8&app_id=APP_ID&app_code=APP_CODE
    //      &searchtext=425%20W%20Randolph%20Street%2c%20Chicago
    //     JSON response:
    //      {
    //         "Response": {
    //            "MetaInfo": {
    //               "Timestamp": "2015-01-13T01:12:15.199+0000"
    //            },
    //            "View": [
    //               {
    //                  "_type": "SearchResultsViewType",
    //                  "ViewId": 0,
    //                  "Result": [
    //                     {
    //                        "Relevance": 1.0,
    //                        "MatchLevel": "houseNumber",
    //                        "MatchQuality": {
    //                           "City": 1.0,
    //                           "Street": [
    //                              1.0
    //                           ],
    //                           "HouseNumber": 1.0
    //                        },
    //                        "MatchType": "pointAddress",
    //                        "Location": {
    //                           "LocationId": "NT_krOz+rwboyk4Jvih55MwPB_425",
    //                           "LocationType": "address",
    //                           "DisplayPosition": {
    //                              "Latitude": 41.8838692,
    //                              "Longitude": -87.6389008
    //                           },
    //                           "NavigationPosition": [
    //                              {
    //                                 "Latitude": 41.8844719,
    //                                 "Longitude": -87.6387711
    //                              }
    //                           ],
    //                           "MapView": {
    //                              "TopLeft": {
    //                                 "Latitude": 41.8849933,
    //                                 "Longitude": -87.6404107
    //                              },
    //                              "BottomRight": {
    //                                 "Latitude": 41.882745,
    //                                 "Longitude": -87.6373908
    //                              }
    //                           },
    //                           "Address": {
    //                              "Label": "425 W Randolph St, Chicago, IL 60606, United States",
    //                              "Country": "USA",
    //                              "State": "IL",
    //                              "County": "Cook",
    //                              "City": "Chicago",
    //                              "District": "West Loop",
    //                              "Street": "W Randolph St",
    //                              "HouseNumber": "425",
    //                              "PostalCode": "60606",
    //                              "AdditionalData": [
    //                                 {
    //                                    "value": "United States",
    //                                    "key": "CountryName"
    //                                 },
    //                                 {
    //                                    "value": "Illinois",
    //                                    "key": "StateName"
    //                                 }
    //                              ]
    //                           }
    //                        }
    //                     }
    //                  ]
    //               }
    //            ]
    //         }
    //      }
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private   static final String TAG_Response                  = "Response";
    
    private   static final String TAG_MetaInfo                  = "MetaInfo";
    private   static final String TAG_Timestamp                 = "Timestamp";
    
    private   static final String TAG_View                      = "View";            // Array   (Response.View[0])
    private   static final String TAG_Result                    = "Result";          // Array   (Response.View[0].Result[0])
    private   static final String TAG_MatchLevel                = "MatchLevel";      // String  (Response.View[0].Result[0].MatchLevel)
    private   static final String TAG_Location                  = "Location";        // Object  (Response.View[0].Result[0].Location)
    private   static final String TAG_DisplayPosition           = "DisplayPosition"; // Object  (Response.View[0].Result[0].Location.DisplayPosition)
    private   static final String TAG_Latitude                  = "Latitude";        // double  (Response.View[0].Result[0].Location.DisplayPosition.Latitude)
    private   static final String TAG_Longitude                 = "Longitude";       // double  (Response.View[0].Result[0].Location.DisplayPosition.Longitude)
    private   static final String TAG_Address                   = "Address";         // String  (Response.View[0].Result[0].Location.Address)
    private   static final String TAG_Label                     = "Label";           // String  (Response.View[0].Result[0].Location.Address.Label)
    private   static final String TAG_HouseNumber               = "HouseNumber";     // String  (Response.View[0].Result[0].Location.Address.HouseNumber)
    private   static final String TAG_Street                    = "Street";          // String  (Response.View[0].Result[0].Location.Address.Street)
    private   static final String TAG_City                      = "City";            // String  (Response.View[0].Result[0].Location.Address.City)
    private   static final String TAG_State                     = "State";           // String  (Response.View[0].Result[0].Location.Address.State)
    private   static final String TAG_County                    = "County";          // String  (Response.View[0].Result[0].Location.Address.County)
    private   static final String TAG_PostalCode                = "PostalCode";      // String  (Response.View[0].Result[0].Location.Address.PostalCode)
    private   static final String TAG_Country                   = "Country";         // String  (Response.View[0].Result[0].Location.Address.Country)

    private   static final String TAG_Link                      = "Link";            // Array   (Response.Link[0])
    private   static final String TAG_SpeedLimit                = "SpeedLimit";      // double  (Response.Link[0] SpeedLimit)
    private   static final String TAG_SpeedCategory             = "SpeedCategory";   // String  (Response.Link[0] SpeedCategory)
  //private   static final String TAG_Address                   = "Address";         // String  (Response.Link[0].Address)

    // ---
    private   static final String TAG_Status                    = "Status";   

    /* URLs */
    private   static final String URL_ReverseGeocodeRouting_    = "http://route.nlp.nokia.com/routing/6.2/getlinkinfo.json?";
  //private   static final String URL_ReverseGeocodeRouting_    = "http://route.cit.api.here.com/routing/7.2/getlinkinfo.json?";
    private   static final String URL_ReverseGeocode_           = "http://reverse.geocoder.cit.api.here.com/6.2/reversegeocode.json?";
    private   static final String URL_Geocode_                  = "http://geocoder.cit.api.here.com/6.2/geocode.json?";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private   static final String  PROP_reverseGeocodeURL       = "reverseGeocodeURL";
    private   static final String  PROP_geocodeURL              = "geocodeURL";
    
    private   static final String  PROP_hereAppID               = "hereAppID";
    private   static final String  PROP_hereAppCode             = "hereAppCode";
    private   static final String  PROP_hereProxRadius          = "hereProxRadius";
    
    private   static final String  PROP_includeSpeedLimit       = "hereIncludeSpeedLimit";

    // ------------------------------------------------------------------------

    private   static final int     TIMEOUT_ReverseGeocode       = 2500; // milliseconds
    private   static final int     TIMEOUT_Geocode              = 5000; // milliseconds

    private   static final String  DEFAULT_COUNTRY              = "US"; // http://en.wikipedia.org/wiki/CcTLD

    // ------------------------------------------------------------------------

    private   static final int     SPEEDLIMIT_NEVER             = 0;
    private   static final int     SPEEDLIMIT_ALWAYS            = 1;
    private   static final int     SPEEDLIMIT_MOVING            = 2;

    private   static final int     DEFAULT_INCLUDE_SPEED_LIMIT  = SPEEDLIMIT_NEVER;

    // ------------------------------------------------------------------------

    private   static final String  STATUS_UNDEFINED             = "?";
    private   static final String  STATUS_OK                    = "OK";
    private   static final String  STATUS_LIMIT_EXCEEDED        = "620";
    private   static final String  STATUS_BADREQUEST_400        = "400";
    private   static final String  STATUS_UNAUTHORIZED_401      = "401";
    private   static final String  STATUS_FORBIDDEN_403         = "403";
    private   static final String  STATUS_NOT_FOUND_404         = "404";
    private   static final String  STATUS_IP_ADDRESS_           = "IP ";

    private   static final JSON    JSON_LIMIT_EXCEEDED          = JSONStatus(STATUS_LIMIT_EXCEEDED);
    private   static final JSON    JSON_BADREQUEST_400          = JSONStatus(STATUS_BADREQUEST_400);
    private   static final JSON    JSON_UNAUTHORIZED_401        = JSONStatus(STATUS_UNAUTHORIZED_401);
    private   static final JSON    JSON_FORBIDDEN_403           = JSONStatus(STATUS_FORBIDDEN_403);
    private   static final JSON    JSON_NOT_FOUND_404           = JSONStatus(STATUS_NOT_FOUND_404);

    private static JSON JSONStatus(String status)
    {
        // ("GeocodingResult":{"Message":"OK"})
        StringBuffer J = new StringBuffer();
        J.append("{\"").append(TAG_Response).append("\":");
        J.append("{\"").append(TAG_Status).append("\":\"").append(status).append("\"}}");
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
    private   static final boolean FAILOVER_DEBUG               = false;

    // ------------------------------------------------------------------------

    private   static final String  ENCODING_UTF8                = StringTools.CharEncoding_UTF_8;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public NokiaHere(String name, String key, RTProperties rtProps)
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
    private int getGeocodeTimeout()
    {
        return TIMEOUT_Geocode;
    }

    /**
    *** Returns the ReverseGeocode timeout
    **/
    private int getReverseGeocodeTimeout()
    {
        return TIMEOUT_ReverseGeocode;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the runtime properties indicate that speed limit should be included
    **/
    private int getIncludeSpeedLimit()
    {
        String inclSpeedLim = this.getProperties().getString(PROP_includeSpeedLimit,null);
        if (StringTools.isBlank(inclSpeedLim)) {
            return DEFAULT_INCLUDE_SPEED_LIMIT;
        } else
        if (inclSpeedLim.equalsIgnoreCase("never")  || 
            inclSpeedLim.equalsIgnoreCase("0")      || 
            inclSpeedLim.equalsIgnoreCase("false")    ) {
            return SPEEDLIMIT_NEVER;
        } else
        if (inclSpeedLim.equalsIgnoreCase("always") || 
            inclSpeedLim.equalsIgnoreCase("1")      || 
            inclSpeedLim.equalsIgnoreCase("true")     ) {
            return SPEEDLIMIT_ALWAYS;
        } else 
        if (inclSpeedLim.equalsIgnoreCase("moving") || 
            inclSpeedLim.equalsIgnoreCase("2")        ) {
            return SPEEDLIMIT_MOVING;
        } else {
            return DEFAULT_INCLUDE_SPEED_LIMIT;
        }
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

    /* encode GeoPoint into nearest address URI */
    private String _getAddressReverseGeocodeURL(GeoPoint gp, String localeStr, boolean inclSpeedLim)
    {
        StringBuffer sb = new StringBuffer();

        /* predefined URL */
        String rgURL = this.getProperties().getString(PROP_reverseGeocodeURL,null);
        if (!StringTools.isBlank(rgURL)) {
            // -- assume "&app_id="/"&app_code=" is already part of this URL
            sb.append(rgURL);
            if (inclSpeedLim) {
                // -- linkInfo
                sb.append("&waypoint=");
                if (gp != null) {
                    String lat = gp.getLatitudeString( GeoPoint.SFORMAT_DEC_5,null);
                    String lon = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
                    sb.append(lat).append(",").append(lon);
                }
            } else {
                // -- retrieveAddress
                sb.append("&prox=");
                if (gp != null) {
                    String lat = gp.getLatitudeString( GeoPoint.SFORMAT_DEC_5,null);
                    String lon = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
                    sb.append(lat).append(",").append(lon);
                    int radiusM = this.getProperties().getInt(PROP_hereProxRadius,50);
                    if (radiusM <= 5) { radiusM = 5; }
                    sb.append(",").append(radiusM);
                }
            }
            String defURL = sb.toString();
            return defURL;
        }

        /* assemble URL */
        {
            String url = inclSpeedLim? URL_ReverseGeocodeRouting_ : URL_ReverseGeocode_;
            sb.append(url);
            if (!url.endsWith("?")) { sb.append("&"); }
        }

        /* metric units */
        sb.append("metricSystem=metric"); // .append("&language=???");

        /* "app_id" */
        sb.append("&app_id=");
        String appID = this.getProperties().getString(PROP_hereAppID,"");
        if (StringTools.isBlank(appID) || appID.startsWith("*")) {
            // -- invalid app_id
            Print.logWarn("Property '" + PROP_hereAppID + "' not specified ("+appID+")");
        } else {
            sb.append(appID);
        }

        /* "app_code" */
        sb.append("&app_code=");
        String appCode = this.getProperties().getString(PROP_hereAppCode,null);
        if (StringTools.isBlank(appCode) || appCode.startsWith("*")) {
            // -- invalid app_code
            Print.logWarn("Property '" + PROP_hereAppID + "' not specified ("+appCode+")");
        } else {
            sb.append(appCode);
        }

        /* location */
        if (inclSpeedLim) {
            // -- linkInfo
            sb.append("&linkAttributes=speedLimit,speedCategory");
            sb.append("&waypoint=geo!");
            if (gp != null) {
                String lat = gp.getLatitudeString( GeoPoint.SFORMAT_DEC_5,null);
                String lon = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
                sb.append(lat).append(",").append(lon);
            }
        } else {
            // -- retrieveAddress
            sb.append("&gen=8&mode=retrieveAddresses");
            sb.append("&prox=");
            if (gp != null) {
                String lat = gp.getLatitudeString( GeoPoint.SFORMAT_DEC_5,null);
                String lon = gp.getLongitudeString(GeoPoint.SFORMAT_DEC_5,null);
                sb.append(lat).append(",").append(lon);
                int radiusM = this.getProperties().getInt(PROP_hereProxRadius,50);
                if (radiusM <= 5) { radiusM = 5; }
                sb.append(",").append(radiusM);
            }
        }

        /* return url */
        String defURL = sb.toString();
        return defURL;

    }

    /* return reverse-geocode using nearest address */
    private ReverseGeocode _getAddressReverseGeocode(GeoPoint gp, String localeStr, boolean cache, boolean inclSpeedLim)
    {

        /* check for failover mode */
        if (this.isReverseGeocodeFailoverMode()) {
            ReverseGeocodeProvider frgp = this.getFailoverReverseGeocodeProvider();
            return frgp.getReverseGeocode(gp, localeStr, cache);
        }

        /* URL */
        String url = this._getAddressReverseGeocodeURL(gp, localeStr, inclSpeedLim);
        Print.logInfo("NokiaHere RG URL: " + url);

        /* create JSON document */
        JSON jsonDoc = null;
        JSON._Object jsonObj = null;
        try {
            jsonDoc = GetJSONDocument(url, this.getReverseGeocodeTimeout());
            //Print.logInfo("Response:\n"+jsonDoc);
            jsonObj = (jsonDoc != null)? jsonDoc.getObject() : null;
            if (jsonObj == null) {
                Print.logWarn("Unable to obtain top-level JSON object");
                return null;
            }
        } catch (Throwable th) {
            Print.logException("Error", th);
        }

        /* parse address */
        String status        = STATUS_UNDEFINED;
        String fullAddress   = null;
        String streetAddr    = null;
        String cityName      = null;
        String stateProv     = null;
        String postalCode    = null;
        String countryCode   = null;
        double speedLimitKPH = -1.0;
        double speedCategory = -1.0;
        JSON._Object response = jsonObj.getObjectForName(TAG_Response,null);
        if (response != null) {
            status = response.getStringForName(TAG_Status,status);
            JSON._Array _view = response.getArrayForName(TAG_View,null);
            JSON._Object _view0 = (_view != null)? _view.getObjectValueAt(0,null) : null;
            if (_view0 != null) {
                // -- standard reverse-geocode
                JSON._Array _result = _view0.getArrayForName(TAG_Result,null);
                JSON._Object _result0 = (_result != null)? _result.getObjectValueAt(0,null) : null;
                if (_result0 != null) {
                    JSON._Object _location = _result0.getObjectForName(TAG_Location,null);
                    if (_location != null) {
                        JSON._Object _address = _location.getObjectForName(TAG_Address,null);
                        if (_address != null) {
                            // -- address is more detailed for standard reverse-geocoding
                            String _fullAddr = StringTools.trim(_address.getStringForName(TAG_Label,""));
                            String _houseNum = StringTools.trim(_address.getStringForName(TAG_HouseNumber,""));
                            String _street   = StringTools.trim(_address.getStringForName(TAG_Street,""));
                            String _city     = StringTools.trim(_address.getStringForName(TAG_City,""));
                            String _state    = StringTools.trim(_address.getStringForName(TAG_State,""));
                            String _postal   = StringTools.trim(_address.getStringForName(TAG_PostalCode,""));
                            String _country  = StringTools.trim(_address.getStringForName(TAG_Country,""));
                            // --
                            streetAddr  = StringTools.trim(_houseNum + " " + _street);
                            cityName    = _city;
                            postalCode  = _postal;
                            countryCode = _country.toUpperCase();
                            if (countryCode.equalsIgnoreCase("United States")) { 
                                countryCode = "US"; 
                            } else
                            if (countryCode.equalsIgnoreCase("USA")) { 
                                countryCode = "US"; 
                            }
                            if (countryCode.startsWith("US")) {
                                // -- US: set state abbreviation
                                String stateCode = USState.getStateCodeForName(_state,null);
                                if (!StringTools.isBlank(stateCode)) {
                                    stateProv = stateCode.toUpperCase();
                                } else {
                                    stateProv = StringTools.setFirstUpperCase(_state);
                                }
                            } else {
                                // -- non-US: set state/province as-is
                                stateProv = _state;
                            }
                            // --
                            fullAddress = _fullAddr;
                            if (fullAddress.endsWith(", United States")) {
                                // -- return trailing ", United States"
                                fullAddress = fullAddress.substring(0,fullAddress.length() - ", United States".length());
                            }
                        } else {
                            Print.logWarn("'Address' tag not found");
                        }
                    } else {
                        Print.logWarn("'Location' tag not found");
                    }
                } else {
                    Print.logWarn("'Result[0]' tag not found");
                }
            } else {
                // -- routing linkInfo (speed limit)
                Print.logDebug("LinkInfo(SpeedLimit) ...");
                JSON._Array _link = response.getArrayForName(TAG_Link,null);
                JSON._Object _link0 = (_link != null)? _link.getObjectValueAt(0,null) : null;
                if (_link0 != null) {
                    // -- speed category
                    String  SpeedCatList[] = { "SC0", "SC1", "SC2", "SC3", "SC4", "SC5", "SC6", "SC7", "SC8" };
                    String  spdCat      = _link0.getStringForName(TAG_SpeedCategory,"");
                    double  spdCatKPH   = 0.0;
                    boolean isCatMetric = false; // "false" for US
                    if (StringTools.startsWithIgnoreCase(spdCat,"SC")) {
                        int spdCatNdx = ListTools.indexOfIgnoreCase(SpeedCatList, spdCat);
                        switch (spdCatNdx) {
                            case 0: // SC0 : (does not exist)
                                spdCatKPH = isCatMetric? 200.0 : (100.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                            case 1: // SC1 : >130 km/h / >80 MPH
                                spdCatKPH = isCatMetric? 140.0 : ( 85.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                            case 2: // SC2 : 101-130 km/h / 65-80 MPH
                                spdCatKPH = isCatMetric? 130.0 : ( 80.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                            case 3: // SC3 :  91-100 km/h / 55-64 MPH
                                spdCatKPH = isCatMetric? 100.0 : ( 65.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                            case 4: // SC4 :  71-90 km/h / 41-54 MPH
                                spdCatKPH = isCatMetric?  90.0 : ( 55.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                            case 5: // SC5 :  51-70 km/h / 31-40 MPH
                                spdCatKPH = isCatMetric?  70.0 : ( 40.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                            case 6: // SC6 :  31-50 km/h / 21-30 MPH
                                spdCatKPH = isCatMetric?  50.0 : ( 30.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                            case 7: // SC7 :  11-30 km/h / 6-20 MPH
                                spdCatKPH = isCatMetric?  30.0 : ( 20.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                            case 8: // SC8 : <11 km/h / <6 MPH
                            default:
                                spdCatKPH = isCatMetric?  10.0 : (  5.0 * GeoPoint.KILOMETERS_PER_MILE);
                                break;
                        }
                        Print.logDebug("SpeedCategory: " + spdCat + " [#" + spdCatNdx + "] + " + spdCatKPH + " km/h");
                    }
                    // -- speed limit
                    double spdLimMPS = _link0.getDoubleForName(TAG_SpeedLimit,1000.0);
                    if (spdLimMPS >= 999.0) {
                        // -- no speed limit available
                        Print.logDebug("No SpeedLimit available (using SpeedCategory)");
                        speedLimitKPH = spdCatKPH;
                    } else
                    if (spdLimMPS >= 998.0) {
                        // -- on-ramp / off-ramp
                        Print.logDebug("No SpeedLimit available (using SpeedCategory)");
                        speedLimitKPH = spdCatKPH;
                    } else
                    if (spdLimMPS > 0.0) {
                        // -- convert meters/sec to km/h (m/s * km/m * s/hr)
                        double spdLimKPH = spdLimMPS * 0.001/*km/meter*/ * 3600.0/*sec/hr*/;
                        if (isCatMetric) {
                            // -- metric: round to nearest km/h
                            speedLimitKPH = (double)Math.round(spdLimKPH);
                        } else {
                            // -- imperial: round to nearest MPH (convert to MPH, round, covert back to KM)
                            speedLimitKPH = (double)Math.round(spdLimKPH * GeoPoint.MILES_PER_KILOMETER) * GeoPoint.KILOMETERS_PER_MILE;
                        }
                        Print.logDebug("SpeedLimit: " + speedLimitKPH);
                    } else {
                        Print.logDebug("SpeedCategory: " + speedLimitKPH);
                        speedLimitKPH = spdCatKPH;
                    }
                    // -- full address
                    JSON._Object _address = _link0.getObjectForName(TAG_Address,null);
                    if (_address != null) {
                        // -- address less detailed for routing 
                        String _label    = StringTools.trim(_address.getStringForName(TAG_Label,""));
                        String _street   = _label;
                        String _city     = StringTools.trim(_address.getStringForName(TAG_City,""));
                        String _state    = StringTools.trim(_address.getStringForName(TAG_State,""));
                        String _postal   = StringTools.trim(_address.getStringForName(TAG_PostalCode,""));
                        String _country  = StringTools.trim(_address.getStringForName(TAG_Country,""));
                        // --
                        streetAddr  = _street;
                        cityName    = _city;
                        postalCode  = _postal;
                        countryCode = _country.toUpperCase();
                        if (countryCode.equalsIgnoreCase("United States")) { 
                            countryCode = "US"; 
                        } else
                        if (countryCode.equalsIgnoreCase("USA")) { 
                            countryCode = "US"; 
                        }
                        if (countryCode.startsWith("US")) {
                            // -- US: set state abbreviation
                            String stateCode = USState.getStateCodeForName(_state,null);
                            if (!StringTools.isBlank(stateCode)) {
                                stateProv = stateCode.toUpperCase();
                            } else {
                                stateProv = StringTools.setFirstUpperCase(_state);
                            }
                        } else {
                            // -- non-US: set state/province as-is
                            stateProv = _state;
                        }
                        // -- assemble full address
                        StringBuffer sb = new StringBuffer();
                        if (!StringTools.isBlank(streetAddr)) { sb.append(streetAddr).append(", "); }
                        if (!StringTools.isBlank(cityName  )) { sb.append(cityName  ).append(", "); }
                        if (!StringTools.isBlank(stateProv )) { sb.append(stateProv ).append(" " ); }
                        if (!StringTools.isBlank(postalCode)) { sb.append(postalCode).append(" " ); }
                        fullAddress = StringTools.trim(sb.toString());
                    } else {
                        Print.logWarn("'Address' tag not found");
                    }
                } else {
                    Print.logWarn("'View[0]'/'Link[0]' tag not found");
                }
            }
        } else {
            Print.logWarn("'Response' tag not found");
        }
        status = StringTools.trim(status);

        /* create address */
        if (FAILOVER_DEBUG) {
            status = STATUS_LIMIT_EXCEEDED;
        } else 
        if (!StringTools.isBlank(fullAddress)) {
            // -- address found 
            Print.logDebug("Address: " + fullAddress);
            ReverseGeocode rg = new ReverseGeocode();
            rg.setFullAddress(fullAddress);
            rg.setStreetAddress(streetAddr);
            rg.setCity(cityName);
            rg.setStateProvince(stateProv);
            rg.setPostalCode(postalCode);
            rg.setCountryCode(countryCode);
            if (speedLimitKPH > 0.0) {
                rg.setSpeedLimitKPH(speedLimitKPH);
            }
            return rg;
        } else
        if ((status.equals(STATUS_OK) || status.equals(""))) {
            // -- address not found, but status indicates successful
            Print.logDebug("No Address found for location: " + gp);
            ReverseGeocode rg = new ReverseGeocode();
            rg.setFullAddress("");
            if (speedLimitKPH > 0.0) {
                rg.setSpeedLimitKPH(speedLimitKPH);
            }
            return rg;
        }

        /* check for failover */
        boolean failover = false;
        if (status.startsWith(STATUS_IP_ADDRESS_)) {
            // -- "IP (xx.xx.xx.xx) is not correct or your account runs out of fund."
            Print.logError("NokiaHere IP address invalid/expired! ["+status+"]");
            failover = true;
        } else
        if (status.equals(STATUS_UNDEFINED)) {
            // -- undefined status? (only if no result was returned)
            Print.logError("NokiaHere Reverse-Geocode Undefined! ["+status+"]");
            failover = true;
        } else
        if (status.equals(STATUS_LIMIT_EXCEEDED)) {
            // -- NOTE: not verified that NokiaHere returns this error under these conditions
            Print.logError("NokiaHere Reverse-Geocode Limit Exceeded! ["+status+"]");
            failover = true;
        } else
        if (status.equals(STATUS_BADREQUEST_400)) {
            Print.logError("NokiaHere Bad/Invalid Request! ["+status+"]");
            failover = true;
        } else
        if (status.equals(STATUS_UNAUTHORIZED_401)) {
            Print.logError("NokiaHere Reverse-Geocode Not Authorized! ["+status+"]");
            failover = true;
        } else
        if (status.equals(STATUS_FORBIDDEN_403)) {
            // -- NOTE: not verified that NokiaHere returns this error under these conditions
            Print.logError("NokiaHere Reverse-Geocode Not Authorized! ["+status+"]");
            failover = true;
        } else {
            Print.logError("NokiaHere Unrecognized Error! ["+status+"]");
            failover = false;
        }
        // -- failover?
        if (failover && this.hasFailoverReverseGeocodeProvider()) {
            this.startReverseGeocodeFailoverMode();
            ReverseGeocodeProvider frgp = this.getFailoverReverseGeocodeProvider();
            Print.logWarn("Failing over to '" + frgp.getName() + "'");
            return frgp.getReverseGeocode(gp, localeStr, cache);
        }

        /* no reverse-geocode available */
        return null;

    }

    /* return reverse-geocode using nearest address */
    public ReverseGeocode getAddressReverseGeocode(GeoPoint gp, String localeStr, boolean cache)
    {

        /* check "cache" state */
        // -- NOTE: This method depends on "cache" being true when stopped, and false when 
        // -  moving (ie. speed > 0).  See EventData.updateAddress(...) for more info.
        boolean isStopped = cache;      // true if stopped, false if moving
        boolean isMoving  = !isStopped; // moving is opposite of stopped

        /* include speed limit? */
        int inclSpeedLim = this.getIncludeSpeedLimit(); // never/always/speed

        /* run specific reverse-geocode request(s) */
        ReverseGeocode rg = null;
        if (inclSpeedLim == SPEEDLIMIT_NEVER) {
            // -- never: standard reverse-geocode only (no speed limit)
            rg = this._getAddressReverseGeocode(gp, localeStr, false/*cache*/, false/*speedLimit?*/);
        } else
        if (inclSpeedLim == SPEEDLIMIT_ALWAYS) {
            // -- always: standard reverse-geocode, followed by speed-limit reverse-geocode
            rg = this._getAddressReverseGeocode(gp, localeStr, false/*cache*/, false/*speedLimit?*/);
            if (rg == null) {
                // -- no reverse-geocode for this location, skip speed-limit 
            } else
            if (rg.hasSpeedLimitKPH()) {
                // -- already contains speed-limit
            } else
            if (this.isReverseGeocodeFailoverMode()) {
                // -- we are in reverse-geocode failover mode, skip speed limit
            } else {
                // -- attempt to get speed-limit
                ReverseGeocode rgSL = this._getAddressReverseGeocode(gp, localeStr, false/*cache*/, true/*speedLimit?*/);
                if ((rgSL != null) && rgSL.hasSpeedLimitKPH()) {
                    rg.setSpeedLimitKPH(rgSL.getSpeedLimitKPH());
                }
            }
        } else
        if (isMoving) {
            // -- moving: speed-limit reverse-geocode (abbreviated address)
            rg = this._getAddressReverseGeocode(gp, localeStr, false/*cache*/, true/*speedLimit?*/);
        } else {
            // -- stopped: standard reverse-geocode only (no speed-limit) */
            rg = this._getAddressReverseGeocode(gp, localeStr, false/*cache*/, false/*speedLimit?*/);
        }

        /* cache? */
        if (cache) {
            // -- TODO: 
        }

        /* return */
        return rg;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* encode GeoPoint into nearest address URI */
    private String getGeoPointGeocodeURL(String address, String country)
    {
        StringBuffer sb = new StringBuffer();

        /* predefined URL */
        String gcURL = this.getProperties().getString(PROP_geocodeURL,null);
        if (!StringTools.isBlank(gcURL)) {
            // -- assume "&app_id="/"&app_code=" is already part of this URL
            sb.append(gcURL);
            sb.append("&searchtext=").append(URIArg.encodeArg(address));
            String defURL = sb.toString();
            return defURL;
        }

        /* assemble URL */
        String url = URL_Geocode_;
        sb.append(url);
        if (!url.endsWith("?")) { sb.append("&"); }
        sb.append("gen=8");

        /* "app_id" */
        sb.append("&app_id=");
        String appID = this.getProperties().getString(PROP_hereAppID,"");
        if (StringTools.isBlank(appID) || appID.startsWith("*")) {
            // -- invalid app_id
            Print.logWarn("Property '" + PROP_hereAppID + "' not specified ("+appID+")");
        } else {
            sb.append(appID);
        }

        /* "app_code" */
        sb.append("&app_code=");
        String appCode = this.getProperties().getString(PROP_hereAppCode,null);
        if (StringTools.isBlank(appCode) || appCode.startsWith("*")) {
            // -- invalid app_code
            Print.logWarn("Property '" + PROP_hereAppCode + "' not specified ("+appCode+")");
        } else {
            sb.append(appCode);
        }

        /* address/country */
        sb.append("&searchtext=").append(URIArg.encodeArg(address));

        /* return url */
        String defURL = sb.toString();
        return defURL;

    }

    /* return geocode */
    public GeoPoint getGeocode(String address, String country)
    {

        /* URL */
        String url = this.getGeoPointGeocodeURL(address, country);
        Print.logDebug("NokiaHere GC URL: " + url);

        /* create JSON document */
        JSON jsonDoc = null;
        JSON._Object jsonObj = null;
        try {
            jsonDoc = GetJSONDocument(url, this.getReverseGeocodeTimeout());
            //Print.logInfo("Response:\n"+jsonDoc);
            jsonObj = (jsonDoc != null)? jsonDoc.getObject() : null;
            if (jsonObj == null) {
                Print.logWarn("Unable to obtain top-level JSON object");
                return null;
            }
        } catch (Throwable th) {
            Print.logException("Error", th);
        }

        /* parse GeoPoint */
        String status = STATUS_UNDEFINED;
        GeoPoint geoPoint = null;
        JSON._Object response = jsonObj.getObjectForName(TAG_Response,null);
        if (response != null) {
            status = response.getStringForName(TAG_Status,status);
            JSON._Array _view = response.getArrayForName(TAG_View,null);
            JSON._Object _view0 = (_view != null)? _view.getObjectValueAt(0,null) : null;
            if (_view0 != null) {
                JSON._Array _result = _view0.getArrayForName(TAG_Result,null);
                JSON._Object _result0 = (_result != null)? _result.getObjectValueAt(0,null) : null;
                if (_result0 != null) {
                    JSON._Object _location = _result0.getObjectForName(TAG_Location,null);
                    if (_location != null) {
                        JSON._Object _dispPos = _location.getObjectForName(TAG_DisplayPosition,null);
                        if (_dispPos != null) {
                            double lat = _dispPos.getDoubleForName(TAG_Latitude,0.0);
                            double lon = _dispPos.getDoubleForName(TAG_Longitude,0.0);
                            if (GeoPoint.isValid(lat,lon)) {
                                geoPoint = new GeoPoint(lat,lon);
                            }
                        }
                    }
                }
            }
        }

        /* creaturn GeoPoint */
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
            Print.logError("NokiaHere IP address invalid/expired! ["+status+"]");
        } else
        if (status.equals(STATUS_UNDEFINED)) {
            Print.logError("NokiaHere Geocode Undefined! ["+status+"]");
        } else
        if (status.equals(STATUS_LIMIT_EXCEEDED)) {
            // NOTE: not verified that NokiaHere returns this error under these conditions
            Print.logError("NokiaHere Geocode Limit Exceeded! ["+status+"]");
        } else
        if (status.equals(STATUS_BADREQUEST_400)) {
            Print.logError("NokiaHere Bad/Invalid Request! ["+status+"]");
        } else
        if (status.equals(STATUS_UNAUTHORIZED_401)) {
            Print.logError("NokiaHere Geocode Not Authorized! ["+status+"]");
        } else
        if (status.equals(STATUS_FORBIDDEN_403)) {
            Print.logError("NokiaHere Geocode Not Authorized! ["+status+"]");
        }

        /* no reverse-geocode available */
        return null;

    }

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
            if (rc == 400) {
                jsonDoc = JSON_BADREQUEST_400; // STATUS_BADREQUEST_400: bad request
            } else
            if (rc == 401) {
                jsonDoc = JSON_UNAUTHORIZED_401; // STATUS_UNAUTHORIZED_401: not authorized
            } else
            if (rc == 403) {
                jsonDoc = JSON_FORBIDDEN_403;    // STATUS_FORBIDDEN_403: not authorized
            } else
            if (rc == 404) {
                jsonDoc = JSON_NOT_FOUND_404;    // STATUS_NOT_FOUND_404: path not found
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
        NokiaHere gn = new NokiaHere("nokiahere", null, null);

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
