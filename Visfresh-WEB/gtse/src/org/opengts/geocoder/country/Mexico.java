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
//  2015/06/12  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.geocoder.country;

import java.util.*;

import org.opengts.util.*;

public class Mexico
{

    // ------------------------------------------------------------------------

    public static final String COUNTRY_MX       = "MX";
    public static final String COUNTRY_MX_      = COUNTRY_MX + CountryCode.SUBDIVISION_SEPARATOR;

    // ------------------------------------------------------------------------

    private static HashMap<String,StateInfo> GlobalStateMap = new HashMap<String,StateInfo>();

    /**
    *** StateInfo class
    **/
    public static class StateInfo
    {

        private String code     = null;
        private String name     = null;
        private String abbrev   = null;
        private String fips     = null;

        public StateInfo(String code, String name, String abbrev, String fips) {
            this.code   = code;
            this.name   = name;
            this.abbrev = abbrev;
            this.fips   = fips;
        }

        public String getCode() {
            return this.code;
        }

        public String getAbbreviation() {
            return this.abbrev;
        }

        public String getName() {
            return this.name;
        }

        public boolean hasFIPS() {
            return !StringTools.isBlank(this.getFIPS())? true : false;
        }
        public String getFIPS() {
            return this.fips;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.getCode());
            if (this.hasFIPS()) {
                sb.append("[").append(this.getFIPS()).append("]");
            }
            sb.append(" ");
            sb.append(this.getName());
            sb.append(" (").append(this.getAbbreviation()).append(")");
            return sb.toString();
        }

    }

    // ------------------------------------------------------------------------

    public static final StateInfo StateMapArray[] = new StateInfo[] {
        //            Code  Name                    Abbrev    FIPS
        //            ----  ----------------------  --------  ----
        new StateInfo("AG", "Aguascalientes"       , "Ags"  , "01"),
        new StateInfo("BN", "Baja California"      , "BCN"  , "02"),
        new StateInfo("BS", "Baja California Sur"  , "BCS"  , "03"),
        new StateInfo("CM", "Campeche"             , "Camp" , "04"),
        new StateInfo("CP", "Chiapas"              , "Chis" , "05"),
        new StateInfo("CH", "Chihuahua"            , "Chih" , "06"),
        new StateInfo("CA", "Coahuila"             , "Coah" , "07"),
        new StateInfo("CL", "Colima"               , "Col"  , "08"),
        new StateInfo("DF", "Distrito Federal"     , "DF"   , "09"),
        new StateInfo("DU", "Durango"              , "Dgo"  , "10"),
        new StateInfo("GJ", "Guanajuato"           , "Gto"  , "11"),
        new StateInfo("GR", "Guerrero"             , "Gro"  , "12"),
        new StateInfo("HI", "Hidalgo"              , "Hgo"  , "13"),
        new StateInfo("JA", "Jalisco"              , "Jal"  , "14"),
        new StateInfo("MX", "Mexico"               , "Mex"  , "15"),
        new StateInfo("MC", "Michoacan"            , "Mich" , "16"),
        new StateInfo("MR", "Morelos"              , "Mor"  , "17"),
        new StateInfo("NA", "Nayarit"              , "Nay"  , "18"),
        new StateInfo("NL", "Nuevo Leon"           , "NL"   , "19"),
        new StateInfo("OA", "Oaxaca"               , "Oax"  , "20"),
        new StateInfo("PU", "Puebla"               , "Pue"  , "21"),
        new StateInfo("QE", "Queretaro"            , "Qro"  , "22"),
        new StateInfo("QA", "Quintana Roo"         , "QR"   , "23"),
        new StateInfo("SL", "San Luis Potosi"      , "SLP"  , "24"),
        new StateInfo("SI", "Sinaloa"              , "Sin"  , "25"),
        new StateInfo("SO", "Sonora"               , "Son"  , "26"),
        new StateInfo("TB", "Tabasco"              , "Tab"  , "27"),
        new StateInfo("TM", "Tamaulipas"           , "Tamps", "28"),
        new StateInfo("TL", "Tlaxcala"             , "Tlax" , "29"),
        new StateInfo("VE", "Veracruz"             , "Ver"  , "30"),
        new StateInfo("YU", "Yucatan"              , "Yuc"  , "31"),
        new StateInfo("ZA", "Zacatecas"            , "Zac"  , "32"),
    };

    // -- startup initialization
    static {
        for (int i = 0; i < StateMapArray.length; i++) {
            // -- add CODE
            String code = StateMapArray[i].getCode(); // never blank
            GlobalStateMap.put(code, StateMapArray[i]);
            // -- add FIPS
            String fips = StateMapArray[i].getFIPS(); // may be blank
            if (!StringTools.isBlank(fips)) {
                GlobalStateMap.put(fips, StateMapArray[i]);
            }
        }
    }

    /**
    *** Gets the collection of StateInfo keys (state codes)
    **/
    public static Collection<String> getStateInfoKeys()
    {
        return GlobalStateMap.keySet();
    }

    /**
    *** Gets the StateInfo instance for the specified state code/abbreviation
    **/
    public static StateInfo getStateInfo(String code)
    {
        if (!StringTools.isBlank(code)) {
            if (code.startsWith(Mexico.COUNTRY_MX_)) { 
                // -- MX/##: remove prefixing "MX/"
                code = code.substring(Mexico.COUNTRY_MX_.length()); 
            } else
            if ((code.length() == 4) && code.startsWith(Mexico.COUNTRY_MX)) { 
                // -- MX##: remove prefixing "MX"
                code = code.substring(Mexico.COUNTRY_MX.length()); 
            }
            return GlobalStateMap.get(code);
        } else {
            return null;
        }
    }

    /**
    *** Returns true if the specified Mexico state code/abbreviation exists
    **/
    public static boolean hasStateInfo(String code)
    {
        return (Mexico.getStateInfo(code) != null)? true : false;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified Mexico state code is defined
    *** @param code  The Mexico state code
    *** @return True if teh specified Mexico state is defined, false otherwise
    **/
    public static boolean isStateCode(String code)
    {
        return Mexico.hasStateInfo(code);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Mexico state name for the specified state code
    *** @param code  The state code
    *** @return The state name, or an empty String if the state code was not found
    **/
    public static String getStateName(String code)
    {
        StateInfo pi = Mexico.getStateInfo(code);
        return (pi != null)? pi.getName() : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Mexico state code for the specified state name
    *** @param name  The state name
    *** @param dft   The default code to return if the specified state name is not found
    *** @return The state code
    **/
    public static String getStateCodeForName(String name, String dft)
    {
        if (!StringTools.isBlank(name)) {
            for (String code : Mexico.getStateInfoKeys()) {
                StateInfo pi = Mexico.getStateInfo(code);
                if ((pi != null) && pi.getName().equalsIgnoreCase(name)) {
                    return code;
                }
            }
        }
        return dft;
    }

    /**
    *** Gets the state code for the specified FIPS
    *** @param fips  The state FIPS code (as a 2-character String)
    *** @param dft   The default code to return if the specified state FIPS is not found
    *** @return The state code
    **/
    public static String getStateCodeForFIPS(String fips, String dft)
    {
        StateInfo si = Mexico.getStateInfo(fips);
        return (si != null)? si.getCode() : dft;
    }

    /**
    *** Gets the state code for the specified FIPS
    *** @param fips  The state FIPS code (as an int)
    *** @param dft   The default code to return if the specified state FIPS is not found
    *** @return The state code
    **/
    public static String getStateCodeForFIPS(int fips, String dft)
    {
        return Mexico.getStateCodeForFIPS(StringTools.format(fips,"00"), dft);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the Mexico state abbreviation for the specified state code
    *** @param code  The state code
    *** @param dft   The default abbreviation to return if the specified code was not found 
    *** @return The state abbreviation
    **/
    public static String getStateAbbreviation(String code, String dft)
    {
        StateInfo pi = Mexico.getStateInfo(code);
        return (pi != null)? pi.getAbbreviation() : "";
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static final String ARG_CODE[]          = { "code" };

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);

        // -- lookup code/fips
        if (RTConfig.hasProperty(ARG_CODE)) {
            String code = RTConfig.getString(ARG_CODE,"");
            StateInfo pi = Mexico.getStateInfo(code);
            if (pi == null) {
                Print.sysPrintln("ERROR: code not found");
                System.exit(1);
            }
            Print.sysPrintln("State: " + pi);
            System.exit(0);
        }

    }

}
