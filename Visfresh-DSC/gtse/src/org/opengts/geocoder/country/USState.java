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
//  2006/06/30  Martin D. Flynn
//     -Initial release
//  2015/06/12  Martin D. Flynn
//     -Moved to "org.opengts.geocoder.country"
//     -Added State FIPS codes
// ----------------------------------------------------------------------------
package org.opengts.geocoder.country;

import java.util.*;

import org.opengts.util.*;

public class USState
{

    // ------------------------------------------------------------------------

    public static final String COUNTRY_US       = "US";
    public static final String COUNTRY_US_      = COUNTRY_US + CountryCode.SUBDIVISION_SEPARATOR;

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
        //            Code  Name                  Abbrev    FIPS
        //            ----  -------------------   --------  ----
        new StateInfo("AL", "Alabama"           , "Ala."  , "01"),
        new StateInfo("AK", "Alaska"            , "Alaska", "02"),
      //new StateInfo("AS", "American Samoa"    , "Samoa" , "60"), // X
        new StateInfo("AZ", "Arizona"           , "Ariz." , "04"),
        new StateInfo("AR", "Arkansas"          , "Ark."  , "05"),
        new StateInfo("CA", "California"        , "Calif.", "06"),
        new StateInfo("CO", "Colorado"          , "Colo." , "08"),
        new StateInfo("CT", "Connecticut"       , "Conn." , "09"),
        new StateInfo("DE", "Delaware"          , "Del."  , "10"),
        new StateInfo("DC", "Dist. of Columbia" , "D.C."  , "11"),
        new StateInfo("FL", "Florida"           , "Fla."  , "12"),
        new StateInfo("GA", "Georgia"           , "Ga."   , "13"),
      //new StateInfo("GU", "Guam"              , "Guam"  , "66"), // X
        new StateInfo("HI", "Hawaii"            , "Hawaii", "15"),
        new StateInfo("ID", "Idaho"             , "Idaho" , "16"),
        new StateInfo("IL", "Illinois"          , "Ill."  , "17"),
        new StateInfo("IN", "Indiana"           , "Ind."  , "18"),
        new StateInfo("IA", "Iowa"              , "Iowa"  , "19"),
        new StateInfo("KS", "Kansas"            , "Kans." , "20"),
        new StateInfo("KY", "Kentucky"          , "Ky."   , "21"),
        new StateInfo("LA", "Louisiana"         , "La."   , "22"),
        new StateInfo("ME", "Maine"             , "Maine" , "23"),
        new StateInfo("MD", "Maryland"          , "Md."   , "24"),
      //new StateInfo("MH", "Marshall Islands"  , "MH"    , "68"), // X
        new StateInfo("MA", "Massachusetts"     , "Mass." , "25"),
        new StateInfo("MI", "Michigan"          , "Mich." , "26"),
      //new StateInfo("FM", "Micronesia"        , "FM"    , "64"), // X
        new StateInfo("MN", "Minnesota"         , "Minn." , "27"),
        new StateInfo("MS", "Mississippi"       , "Miss." , "28"),
        new StateInfo("MO", "Missouri"          , "Mo."   , "29"),
        new StateInfo("MT", "Montana"           , "Mont." , "30"),
        new StateInfo("NE", "Nebraska"          , "Nebr." , "31"),
        new StateInfo("NV", "Nevada"            , "Nev."  , "32"),
        new StateInfo("NH", "New Hampshire"     , "N.H."  , "33"),
        new StateInfo("NJ", "New Jersey"        , "N.J."  , "34"),
        new StateInfo("NM", "New Mexico"        , "N.M."  , "35"),
        new StateInfo("NY", "New York"          , "N.Y."  , "36"),
        new StateInfo("NC", "North Carolina"    , "N.C."  , "37"),
        new StateInfo("ND", "North Dakota"      , "N.D."  , "38"),
      //new StateInfo("MP", "Northern Marianas" , "MP"    , "69"), // X
        new StateInfo("OH", "Ohio"              , "Ohio"  , "39"),
        new StateInfo("OK", "Oklahoma"          , "Okla." , "40"),
        new StateInfo("OR", "Oregon"            , "Ore."  , "41"),
      //new StateInfo("PW", "Palau"             , "PW"    , "70"), // X
        new StateInfo("PA", "Pennsylvania"      , "Pa."   , "42"),
      //new StateInfo("PR", "Puerto Rico"       , "P.R."  , "72"), // X
        new StateInfo("RI", "Rhode Island"      , "R.I."  , "44"),
        new StateInfo("SC", "South Carolina"    , "S.C."  , "45"),
        new StateInfo("SD", "South Dakota"      , "S.D."  , "46"),
        new StateInfo("TN", "Tennessee"         , "Tenn." , "47"),
        new StateInfo("TX", "Texas"             , "Tex."  , "48"),
        new StateInfo("UT", "Utah"              , "Utah"  , "49"),
        new StateInfo("VT", "Vermont"           , "Vt."   , "50"),
        new StateInfo("VA", "Virginia"          , "Va."   , "51"),
      //new StateInfo("VI", "Virgin Islands"    , "V.I."  , "78"), // X
        new StateInfo("WA", "Washington"        , "Wash." , "53"),
        new StateInfo("WV", "West Virginia"     , "W.Va." , "54"),
        new StateInfo("WI", "Wisconsin"         , "Wis."  , "55"),
        new StateInfo("WY", "Wyoming"           , "Wyo."  , "56"),
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
            if (code.startsWith(USState.COUNTRY_US_)) { 
                // -- remove prefixing "US/"
                code = code.substring(USState.COUNTRY_US_.length()); 
            }
            return GlobalStateMap.get(code);
        } else {
            return null;
        }
    }

    /**
    *** Returns true if the specified state code/abbreviation exists
    **/
    public static boolean hasStateInfo(String code)
    {
        return (USState.getStateInfo(code) != null)? true : false;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified state code is defined
    *** @param code  The state code
    *** @return True if teh specified state is defined, false otherwise
    **/
    public static boolean isStateCode(String code)
    {
        return USState.hasStateInfo(code);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the state name for the specified state code
    *** @param code  The state code
    *** @return The state name, or an empty String if the state code was not found
    **/
    public static String getStateName(String code)
    {
        StateInfo si = USState.getStateInfo(code);
        return (si != null)? si.getName() : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the state code for the specified state name
    *** @param name  The state name
    *** @param dft   The default code to return if the specified state name is not found
    *** @return The state code
    **/
    public static String getStateCodeForName(String name, String dft)
    {
        if (!StringTools.isBlank(name)) {
            for (String code : USState.getStateInfoKeys()) {
                StateInfo si = USState.getStateInfo(code);
                if ((si != null) && si.getName().equalsIgnoreCase(name)) {
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
        StateInfo si = USState.getStateInfo(fips);
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
        return USState.getStateCodeForFIPS(StringTools.format(fips,"00"), dft);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the state abbreviation for the specified state code
    *** @param code  The state code
    *** @param dft   The default abbreviation to return if the specified code was not found 
    *** @return The state abbreviation
    **/
    public static String getStateAbbreviation(String code, String dft)
    {
        StateInfo si = USState.getStateInfo(code);
        return (si != null)? si.getAbbreviation() : "";
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
            StateInfo si = USState.getStateInfo(code);
            if (si == null) {
                Print.sysPrintln("ERROR: code not found");
                System.exit(1);
            }
            Print.sysPrintln("State: " + si);
            System.exit(0);
        }

    }

}
