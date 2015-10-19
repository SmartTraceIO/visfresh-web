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
//  2014/09/28  Martin D. Flynn
//     -Initial Release
//  2015/??/??  Martin D. Flynn
//     -Made call to "mac.doFinal(..)" thread safe
// ----------------------------------------------------------------------------
package org.opengts.extra.geocoder.rgproxy;

//import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;

import org.opengts.db.*;
import org.opengts.geocoder.*;

public class RGProxy
    extends ReverseGeocodeProviderAdapter
    implements ReverseGeocodeProvider
{

    // ------------------------------------------------------------------------
    // API URLs:
    //   - http://localhost:35001
    //      RGLength:142
    //      RGSignature:xr5BXQ280HkflI67YhfKruJ3VJg=
    //      {
    //          "Provider" : "googleV3",
    //          "Latitude" : 39.1234,
    //          "Longitude" : -121.1234
    //      }
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public    static final String   TAG_Provider[]              = ReverseGeocode.TAG_Provider;
    public    static final String   TAG_Latitude[]              = ReverseGeocode.TAG_Latitude;
    public    static final String   TAG_Longitude[]             = ReverseGeocode.TAG_Longitude;
    public    static final String   TAG_Status                  = "Status";
    public    static final String   TAG_Message                 = "Message";

    public    static final String   REQ_RGLength                = ReverseGeocode.REQ_RGLength;
    public    static final String   REQ_RGSignature             = ReverseGeocode.REQ_RGSignature;

    public    static final String   STATUS_OK                   = "ok";
    public    static final String   STATUS_ERROR                = "error";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private   static final String   RG_NAME                     = DCServerFactory.RGPROXY_NAME;
    
    private   static final boolean  USE_HTTP_POST               = false;

    protected static final String   PROP_signatureKey           = "signatureKey";
    protected static final String   PROP_proxyHost              = "proxyHost";
    protected static final String   PROP_proxyPort              = "proxyPort";
    protected static final String   PROP_proxyURL               = "proxyURL";
    protected static final String   PROP_proxyTimeoutMS         = "proxyTimeoutMS";

    protected static final String   DEFAULT_RGPROXY_HOST        = null;
    protected static final int      DEFAULT_RGPROXY_PORT        = 0; 
    protected static final String   DEFAULT_RGPROXY_URL         = null;

    // ------------------------------------------------------------------------

    protected static final int      TIMEOUT_ReverseGeocode      = 2500; // milliseconds

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String HMACSHA1 = "HmacSHA1";

    /**
    *** Creates a new "javax.crypto.Mac" instance.
    **/
    public static javax.crypto.Mac GetCryptoMac(String sigKey)
    {
        if (!StringTools.isBlank(sigKey)) {
            try {
                byte kb[] = Base64.decode(sigKey.replace('-','+').replace('_','/')); // 20 bytes (160 bits)
                javax.crypto.Mac mac = javax.crypto.Mac.getInstance(HMACSHA1); //NoSuchAlgorithmException
                mac.init(new javax.crypto.spec.SecretKeySpec(kb,HMACSHA1)); // InvalidKeyException
                return mac;
            } catch (java.security.NoSuchAlgorithmException nsae) {
                return null;
            } catch (java.security.InvalidKeyException ike) {
                return null;
            } catch (Throwable th) {
                return null;
            }
        } else {
            return null;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private boolean          cryptoMac_init = false;
    private javax.crypto.Mac cryptoMac      = null;
    
    public RGProxy(String name, String key, RTProperties rtProps)
    {
        super(name, key, rtProps);
    }

    // ------------------------------------------------------------------------

    public boolean isFastOperation()
    {
        // -- this is a slow operation
        return super.isFastOperation();
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the signature key
    **/
    protected String getSignatureKey()
    {
        RTProperties rtp = this.getProperties();
        return rtp.getString(PROP_signatureKey, "");
    }
    
    /**
    *** Gets the javax.crypto.Mac instance
    **/
    protected javax.crypto.Mac getCryptoMac()
    {
        if (!this.cryptoMac_init) {
            this.cryptoMac_init = true;
            this.cryptoMac = GetCryptoMac(this.getSignatureKey());
        }
        return this.cryptoMac; // may be null
    }
    
    /** 
    *** Gets the signature for the specified String
    **/
    protected String getSignature(String val)
    {
        if (val != null) {
            javax.crypto.Mac mac = this.getCryptoMac();
            if (mac != null) {
                try {
                    byte b[] = _MacDoFinal(mac, val.getBytes());
                    if (b == null) {
                        // -- unable to sign
                        return null;
                    }
                    return Base64.encode(b);
                } catch (Throwable th) {
                    return "";
                }
            }
        }
        return "";
    }

    /**
    *** Thread safe call to (javax.crypto.Mac).doFinal(...)
    **/
    private static byte[] _MacDoFinal(javax.crypto.Mac mac, byte b[])
    {
        byte pqb[] = null;
        if ((mac != null) && !ListTools.isEmpty(b)) {
            synchronized (mac) {
                try {
                    pqb = mac.doFinal(b); // _MacDoFinal (now thread safe)
                } catch (Throwable th) {
                    pqb = null;
                }
            }
        }
        return pqb;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the RGProxy server host
    **/
    protected String getProxyHost()
    {
        RTProperties rtp = this.getProperties();
        String host = rtp.getString(PROP_proxyHost, null);
        if (StringTools.isBlank(host) || host.equalsIgnoreCase("default")) {
            if (!StringTools.isBlank(DEFAULT_RGPROXY_HOST)) {
                return DEFAULT_RGPROXY_HOST;
            } else {
                long startMS = System.currentTimeMillis();
                // -- try getting bind-address from specific RGProxy 
                DCServerConfig dcs = DCServerFactory.getServerConfig(DCServerFactory.RGPROXY_NAME,false);
                if (dcs != null) {
                    String bindAddr = dcs.getBindAddress();
                    if (!StringTools.isBlank(bindAddr)) {
                        return bindAddr;
                    }
                }
                // -- try getting bind-address from global DCServerFactory
                String bindAddr = DCServerFactory.getBindAddress();
                if (!StringTools.isBlank(bindAddr)) {
                    return bindAddr;
                }
                // -- still no host
                Print.logWarn("No RGProxy host defined!");
                return null;
            }
        } else {
            // -- parse/return specified port
            return host.trim();
        }
    }

    /**
    *** Gets the RGProxy server port
    **/
    protected int getProxyPort()
    {
        RTProperties rtp = this.getProperties();
        String portS = rtp.getString(PROP_proxyPort, null);
        if (StringTools.isBlank(portS) || portS.equalsIgnoreCase("default")) {
            // -- return the default port
            if (DEFAULT_RGPROXY_PORT > 0) {
                return DEFAULT_RGPROXY_PORT;
            } else {
                DCServerConfig dcs = DCServerFactory.getServerConfig(DCServerFactory.RGPROXY_NAME,false);
                if (dcs != null) {
                    int rgPorts[] = dcs.getTcpPorts();
                    if (ListTools.size(rgPorts) > 0) {
                        return rgPorts[0];
                    }
                }
                Print.logWarn("No RGProxy port defined!");
                return 0;
            }
        } else {
            // -- parse/return specified port
            return StringTools.parseInt(portS,0);
        }
    }

    /**
    *** Gets the RGProxy server URL (null if undefined)
    **/
    protected String getProxyURL()
    {
        RTProperties rtp = this.getProperties();
        // -- explicit URL specified
        String url = rtp.getString(PROP_proxyURL, null);
        if (!StringTools.isBlank(url)) {
            return url;
        }
        // -- assemble host:port
        String host = this.getProxyHost();
        int    port = this.getProxyPort();
        if (!StringTools.isBlank(host) && (port > 0) && (port < 0xFFFF)) {
            return "http://" + host + ":" + port;
        }
        // -- return default
        return DEFAULT_RGPROXY_URL;
    }

    /**
    *** Gets the ReverseGeocode timeout
    **/
    protected int getProxyTimeoutMS()
    {
        RTProperties rtp = this.getProperties();
        return rtp.getInt(PROP_proxyTimeoutMS, TIMEOUT_ReverseGeocode);
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

    /* return reverse-geocode using nearest address */
    public ReverseGeocode getAddressReverseGeocode(GeoPoint gp, String localeStr, boolean cache)
    {

        /* invalid GeoPoint */
        if (!GeoPoint.isValid(gp)) {
            // -- invalid GeoPoint
            return null;
        }

        /* assemble request JSON */
        String rgProvName = this.getFailoverReverseGeocodeProviderName();
        JSON._Object reqJsonObj = new JSON._Object();
        reqJsonObj.addKeyValue(TAG_Provider[1] , rgProvName);
        reqJsonObj.addKeyValue(TAG_Latitude[1] , gp.getLatitude());
        reqJsonObj.addKeyValue(TAG_Longitude[1], gp.getLongitude());

        /* send POST, get JSON response */
        long startMS = System.currentTimeMillis();
        boolean failover = false;
        JSON._Object respJSON = null;
        try {
            byte respB[] = this._getServerResponse(reqJsonObj);
            String respS = StringTools.trim(StringTools.toStringValue(respB));
            int leftCB = respS.indexOf("{");
            if (leftCB > 0) {
                // -- clear to start of JSON
                respS = respS.substring(leftCB);
            }
            // -- parse JSON response
            if (!StringTools.isBlank(respS)) {
                // -- we have data from the RGProxy service
                respJSON = JSON.parse_Object(respS);
                failover = false;
            } else {
                // -- blank return from RGProxy service
                Print.logWarn("Null/Empty JSON read from RGProxy service");
                failover = true;
            }
        } catch (HTMLTools.HttpIOException hioe) {
            // -- IO error: java.io.IOException: 
            int    rc = hioe.getResponseCode();
            String rm = hioe.getResponseMessage();
            Print.logError("HttpIOException ["+rc+"-"+rm+"]: " + hioe.getMessage());
            failover = true; // failover ok
        } catch (IOException ioe) {
            Print.logError("IOException: " + ioe.getMessage());
            failover = true; // failover ok
        } catch (JSON.JSONParsingException jpe) {
            // -- JSON error
            Print.logException("Error", jpe);
            failover = true; // failover ok
        } catch (Throwable th) {
            // -- unknown error
            Print.logException("Error", th);
            failover = true; // failover ok
        }

        /* parse ReverseGeocode instance */
        if (respJSON != null) {
            // -- check status
            String status = respJSON.getStringForName(TAG_Status,STATUS_OK);
            if (StringTools.isBlank(status) || status.equalsIgnoreCase(STATUS_OK)) {
                // -- return ReverseGeocode
                if (RTConfig.isDebugMode()) {
                    Print.logDebug("Completed in " + (System.currentTimeMillis()-startMS) + " ms");
                }
                return new ReverseGeocode(respJSON);
            }
            // -- display error
            String message = respJSON.getStringForName(TAG_Message,"");
            Print.logWarn("RGProxy error: " + message);
        }

        /* failover */
        if (failover && this.hasFailoverReverseGeocodeProvider()) {
            this.startReverseGeocodeFailoverMode();
            ReverseGeocodeProvider frgp = this.getFailoverReverseGeocodeProvider();
            Print.logWarn("Failing over to '" + frgp.getName() + "'");
            ReverseGeocode frg = frgp.getReverseGeocode(gp, localeStr, cache);
            if (RTConfig.isDebugMode()) {
                Print.logDebug("Completed in " + (System.currentTimeMillis()-startMS) + " ms");
            }
            return frg;
        }

        /* no reverse-geocode available */
        return null;

    }

    /**
    *** Send request to server, and return response
    **/
    private byte[] _getServerResponse(JSON._Object reqJsonObj)
        throws IOException
    {

        /* assemble request */
        byte reqB[];
        {
            // -- request JSON
            String reqStr = reqJsonObj.toString(true);
            // -- RGSignature
            StringBuffer sigSB = new StringBuffer();
            String sig = this.getSignature(reqStr);
            if (!StringTools.isBlank(sig)) {
                sigSB.append(REQ_RGSignature).append(sig).append("\n");
            }
            // -- assemble request
            StringBuffer reqSB = new StringBuffer();
            reqSB.append(REQ_RGLength).append(sigSB.length()+reqStr.length()).append("\n");
            reqSB.append(sigSB); // already includes "\n"
            reqSB.append(reqStr);
            Print.logDebug("Request:\n" + reqSB);
            reqB = reqSB.toString().getBytes();
        }

        /* send request, read response */
        byte respB[] = null;
        int timeoutMS = this.getProxyTimeoutMS();
        if (USE_HTTP_POST) {
            String url = this.getProxyURL();
            if (!StringTools.isBlank(url)) {
                Print.logDebug("URL - " + url);
                respB = HTMLTools.readPage_POST(url, HTMLTools.CONTENT_TYPE_JSON, reqB, timeoutMS);
            } else {
                Print.logWarn("No URL defined!");
            }
        } else {
            String reqHost = this.getProxyHost();
            int    reqPort = this.getProxyPort();
            if (!StringTools.isBlank(reqHost) && (reqPort > 0) && (reqPort < 0xFFFF)) {
                Print.logDebug("Host:Port - " + reqHost + ":" + reqPort);
                ClientSocketThread cst = new ClientSocketThread(reqHost, reqPort);
                try {
                    cst.openSocket();
                    cst.socketWriteBytes(reqB);
                    cst.setSocketReadTimeout((long)timeoutMS);
                    // -- read response length
                    int jsonLen = -1;
                    String jsonLenStr = cst.socketReadLine(15); // "RGLength:1234\n"
                    //Print.logDebug("Read length line: " + jsonLenStr);
                    if (StringTools.startsWithIgnoreCase(jsonLenStr,REQ_RGLength)) {
                        int p = jsonLenStr.indexOf(":");
                        jsonLen = StringTools.parseInt(jsonLenStr.substring(p+1),-1);
                    }
                    // -- read remaining response data
                    if (jsonLen <= 0) {
                        Print.logWarn("Remaining length not specified");
                    }
                    int readLen = (jsonLen > 0)? jsonLen : 4000;
                    respB = cst.socketReadBytes((jsonLen > 0)? jsonLen : 4000);
                    // -- send $CLOSE to RGProxy service
                    cst.socketWriteBytes("$CLOSE\n".getBytes());
                } catch (ConnectException ce) { // "Connection refused"
                    Print.logError("Unable to connect to RGProxy service ["+reqHost+":"+reqPort+"] - " + ce.getMessage());
                } catch (Throwable t) {
                    Print.logException("RGProxy service error", t);
                } finally {
                    cst.closeSocket();
                }
            } else {
                Print.logWarn("No Host:Port defined!");
            }
        }
        return respB;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_ACCOUNT[]       = new String[] { "account", "a"  };
    private static final String ARG_REVGEOCODE[]    = new String[] { "revgeo" , "rg" };

    /**
    *** Main entery point for debugging/testing
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        Print.setAllOutputToStdout(true);
        Print.setEncoding(StringTools.CharEncoding_UTF_8);
        String accountID = RTConfig.getString(ARG_ACCOUNT,"demo");
        RGProxy gn = new RGProxy(RG_NAME, null, null);

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
            // characters may not appear to to be properly displayed if the console display
            // does not support UTF-8.
            System.exit(0);
        }

        /* no options */
        Print.sysPrintln("No options specified");
        System.exit(1);

    }

}
