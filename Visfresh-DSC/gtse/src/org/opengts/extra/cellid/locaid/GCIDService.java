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
//  2013/03/01  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.extra.cellid.locaid;

import java.util.*;
import java.io.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import net.locaid.asl.common.webservice.Credentials;
import net.locaid.asl.common.webservice.LocationInfo;
import net.locaid.asl.common.webservice.LocationXpress; // subclass of LocationInfo
import net.locaid.asl.common.webservice.LocationParams;
import net.locaid.asl.common.webservice.CoorTypeEnum;
import net.locaid.asl.common.webservice.SyncTypeEnum;
import net.locaid.asl.common.webservice.ICID;
import net.locaid.asl.common.webservice.LocationXpressResponse;
import net.locaid.asl.common.webservice.CoordinateGeo;

import net.locaid.asl.webservice.locationservice.LocationDBRequest;
import net.locaid.asl.webservice.locationservice.LocationDBResponse;
import net.locaid.asl.webservice.impl.locationservice.LocationServicePortType;
import net.locaid.asl.webservice.impl.locationservice.LocationService;

import org.opengts.util.*;

import org.opengts.db.*;
import org.opengts.cellid.*;

public class GCIDService
    extends MobileLocationProviderAdapter
    implements MobileLocationProvider
{

    // ------------------------------------------------------------------------

    private static final String  VERSION                        = "0.1.1";

    // ------------------------------------------------------------------------

    private static final long    DefaultServiceTimeout          = 5000L; // milliseconds

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // MobileLocationProvider interface
    // https://asl.loc-aid.com/ASLWS/LocationService?wsdl
    
    private Credentials     locaidCredentials       = null;
    private LocationParams  locaidLocationParams    = null;

    public GCIDService(String name, String key, RTProperties rtProps)
    {
        super(name, key, rtProps);

        /* open service */
        LocationService ls = null;
        LocationServicePortType lspt = null;
        try {

            /* connect */
            Print.logInfo("Opening GTSInterfaceService ...");
            ls   = new LocationService();
            lspt = ls.getLocationServicePort(
                //javax.xml.ws.soap.AddressingFeature
                ); // port

            /* timeouts */
            //1 minute for connection
            ((BindingProvider)lspt).getRequestContext().put("com.sun.xml.ws.connect.timeout"       ,  60000); 
            ((BindingProvider)lspt).getRequestContext().put("javax.xml.ws.client.connectionTimeout", "60000");
            //3 minutes for request
            ((BindingProvider)lspt).getRequestContext().put("com.sun.xml.ws.request.timeout"       ,  180000); 
            ((BindingProvider)lspt).getRequestContext().put("javax.xml.ws.client.receiveTimeout"   , "180000");

        } catch (Throwable th) {
            // javax.xml.ws.WebServiceException
            Print.logException("Error", th);
            return;
        }

    }

    public MobileLocation getMobileLocation(CellTower servCT, CellTower nborCT[]) 
    {

        /* invalid CellTower? */
        if (cervCT == null) {
            return null;
        }

        /* extract */
        int mcc = servCT.hasMobileCountryCode()? servCT.getMobileCountryCode() : 0;
        int mnc = servCT.hasMobileNetworkCode()? servCT.getMobileNetworkCode() : 0;
        int lac = servCT.hasLocationAreaCode() ? servCT.getLocationAreaCode()  : 0;
        int cid = servCT.hasCellTowerID()      ? servCT.getCellTowerID()       : 0;
        int ta  = servCT.hasTimingAdvance()    ? servCT.getTimingAdvance()     : 0;

        /* Credentials */
        String userName = "";
        String password = "";
        Credentials creds = new Credentials();
        creds.setLogin(userName);
        creds.setPassword(password);

        /* LocationParams */
        LocationParams locParams = new LocationParams();
        locParams.setAge("Y"); // ???
        locParams.setCoorType(CoorTypeEnum.DECIMAL);
        locParams.setLocationMethod("ICID-DB"); // LEAST_EXPENSIVE | MOST_ACCURATE | CELL | A-GPS | ICID-DB | OTHER | GSM
        locParams.setSynType(SyncTypeEnum.SYN);

        /* LocationXpress */
        LocationXpress locXpress = new LocationXpress();
        java.util.List<ICID> icidList = locXpress.getXpress();
        ICID icid = new ICID();
        icid.setMcc(String.valueOf(mcc));        // MCC
        icid.setMnc(String.valueOf(mnc));        // MNC
        icid.setLac(String.valueOf(lac));        // LAC
        icid.setCellid(String.valueOf(cid));     // CID
        icid.setTa(String.valueOf(ta));          // TA

        /* LocationDBRequest */
        LocationDBRequest locReq = new LocationDBRequest();
        locReq.setCredentials(creds);
        locReq.setLocationParams(locParams);
        locReq.setLocationInfo(locXpress);

        /* LocationDBResponse */
        LocationDBResponse locResp = lspt.getLocationsDB(locReq);
        java.util.List<LocationXpressResponse> xpressResp = locResp.getLocationXpressResponse();
        CoordinateGeo coordGeo = xpressResp.getCoordinateGeo();
        String coordType = coordGeo.getCoorType().toString(); // "DMS", "DECIMAL"
        double longitude = GCICService.parseCoord(coordGeo.getX()); // 80 14 24 W
        double latitude  = GCICService.parseCoord(coordGeo.getY()); // 25 43 40 N
        double altitudeM = StringTools.parseDouble(coordGeo.getZ(),0.0);

        /* MobileLocation */
        return new MobileLocation(latitude, longitude);

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
        String key = RTConfig.getString("key","");
        GCIDService mobLoc = new GCIDService("locaid", key, null);
        MobileLocation ml = mobLoc.getMobileLocation(ct, null);
        Print.logInfo("Mobile Location: " + ml);

    }

}
