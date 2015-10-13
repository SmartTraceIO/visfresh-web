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
//  2011/07/01  Martin D. Flynn
//     -Initial release
//  2015/??/??  Martin D. Flynn
//     -Made call to "mac.doFinal(..)" thread safe
// ----------------------------------------------------------------------------
package org.opengts.geocoder.google;

import java.math.BigInteger;
import java.net.URL;
import java.net.MalformedURLException;

import org.opengts.util.*;
import org.opengts.db.*;

public class GoogleSig
{

    // ------------------------------------------------------------------------

    private static final String ARG_SIGNATURE_  = "&signature=";
    
    private static final String HMACSHA1        = "HmacSHA1";

    // ------------------------------------------------------------------------

    private static MACProvider macProvider = null;
    
    public static void SetMACProvider(MACProvider mp)
    {
        GoogleSig.macProvider = mp;
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private Object keyMac = null;

    public GoogleSig(String keyStr)
    {
        super();
        // [ENTERPRISE]
        if (!StringTools.isBlank(keyStr)) {
            if (GoogleSig.macProvider != null) {
                this.keyMac = GoogleSig.macProvider.getMAC(keyStr,HMACSHA1);
            } else {
                try {
                    BigInteger seed = null;
                    byte kb[] = Base64.decode(keyStr.replace('-','+').replace('_','/'),seed); // 20 bytes (160 bits)
                    javax.crypto.Mac mac = javax.crypto.Mac.getInstance(HMACSHA1); //NoSuchAlgorithmException
                    mac.init(new javax.crypto.spec.SecretKeySpec(kb,HMACSHA1)); // InvalidKeyException
                    this.keyMac = mac;
                } catch (java.security.NoSuchAlgorithmException nsae) {
                    this.keyMac = null;
                } catch (java.security.InvalidKeyException ike) {
                    this.keyMac = null;
                } catch (Throwable th) {
                    this.keyMac = null;
                }
            }
        }
    }

    // ------------------------------------------------------------------------

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
    *** Calculate and append Google "&signature=" hash.
    *** @param urlStr  The URL string to which the hash is appended.
    *** @return The URL string with the "&signature=" hash appended, or null if unable to sign
    **/
    public String signURL(String urlStr)
    {
        // HMAC-SHA1 signature code here
        // [ENTERPRISE]
        if (this.keyMac != null) {
            try {
                URL    url   = new URL(urlStr);
                String urlQ  = url.getPath() + "?" + url.getQuery();
                byte   pqb[] = _MacDoFinal((javax.crypto.Mac)this.keyMac,urlQ.getBytes());
                if (pqb == null) {
                    // -- unable to sign
                    return null;
                }
                String signature = Base64.encode(pqb,Base64.Base64HttpAlpha);
                return urlStr + ARG_SIGNATURE_ + signature;
            } catch (MalformedURLException mue) { // URISyntaxException
                // -- invalid URL
                return null;
            } catch (Throwable th) {
                // -- unknown error
                return null;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------

    public boolean validateURL(String urlStr)
    {
        // -- HMAC-SHA1 signature validation code here
        // [ENTERPRISE]
        if (urlStr == null) {
            Print.logError("URL is null");
        } else
        if (this.keyMac == null) {
            Print.logError("Mac is null");
        } else {
            // -- extract path
            int    ss    = urlStr.indexOf("//");
            int    pathP = (ss >= 0)? urlStr.indexOf("/",ss+2) : -1;
            String pathS = (pathP > 0)? urlStr.substring(pathP) : null;
            int    sigP  = (pathS != null)? pathS.indexOf(ARG_SIGNATURE_) : -1;
            if (sigP > 0) {
                String urlSigS = pathS.substring(sigP + ARG_SIGNATURE_.length());
                pathS = pathS.substring(0,sigP); // remove signature
                //Print.logInfo("Validate Path: " + pathS + " [sig=" + urlSigS + "]");
                try {
                    // -- sign URL
                    byte actSigB[] = _MacDoFinal((javax.crypto.Mac)this.keyMac,pathS.getBytes());
                    if (ListTools.isEmpty(actSigB)) {
                        Print.logInfo("Unable to sign URL: " + urlStr);
                        return false;
                    }
                    // -- compare with signature specified in URL
                    String actSigS = Base64.encode(actSigB,Base64.Base64HttpAlpha);
                    if (actSigS.equals(urlSigS)) {
                        return true;
                    } else {
                        Print.logInfo("Expected Sig: " + actSigS);
                        return false;
                    }
                } catch (Throwable th) {
                    Print.logError("Unexpected Error: " + th);
                    return false;
                }
            } else {
                Print.logError("'&signature=' not found: " + urlStr);
                return false;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);

        if (RTConfig.hasProperty("url")) {
            String urlStr = RTConfig.getString("url",null);
            String keyStr = RTConfig.getString("key",null);
            if (StringTools.isBlank(urlStr) || StringTools.isBlank(keyStr)) {
                Print.sysPrintln("ERROR: Missing url or key");
                System.exit(99);
            }
            GoogleSig gs = new GoogleSig(keyStr);
            String sigURL = gs.signURL(urlStr);
            Print.sysPrintln("");
            Print.sysPrintln(sigURL);
            Print.sysPrintln("");
            Print.sysPrintln("Validated: " + gs.validateURL(sigURL));
            System.exit(0);
        }

    }
    
}
