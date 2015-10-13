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

public class CountryCode
{

    // ------------------------------------------------------------------------

    public static final String SUBDIVISION_SEPARATOR    = "/";

    // ------------------------------------------------------------------------

    private static HashMap<String,CountryInfo> GlobalCountryMap = new HashMap<String,CountryInfo>();

    /**
    *** CountryInfo class
    **/
    public static class CountryInfo
    {

        private String       code2    = null; // 2-letter code
        private String       code3    = null; // 3-letter code
        private String       name     = null; // name
        private MethodAction sdMeth   = null; // subdivision name method

        public CountryInfo(String code2, String code3, String name) {
            this(code2, code3, name, null, null);
        }

        public CountryInfo(String code2, String code3, String name, Class sdClass, String sdMeth) {
            this.code2   = code2;
            this.code3   = code3;
            this.name    = name;
            if (sdClass != null) {
                try {
                    this.sdMeth = new MethodAction(sdClass, sdMeth, String.class);
                } catch (Throwable th) {
                    Print.logException("Unable to create Subdivision lookup ["+name+"]",th);
                }
            }
            //String R = StringTools.replicateString(" ", 23-name.length());
            //Print.sysPrintln("new CountryInfo(\""+code2+"\", \""+code3+"\", \""+name+"\""+R+"),");
        }

        public String getCode2() {
            return this.code2;
        }

        public String getCode3() {
            return this.code2;
        }

        public String getCode() {
            return this.getCode2();
        }

        public String getName() {
            return this.name;
        }

        public boolean supportsSubdivisionName() {
            return (this.sdMeth != null)? true : false;
        }

        public String getSubdivisionName(String stateCode) {
            if ((this.sdMeth != null) && !StringTools.isBlank(stateCode)) {
                try {
                    return (String)this.sdMeth.invoke(stateCode);
                } catch (Throwable th) {
                    Print.logException("Unable to get Subdivision name ["+this.getName()+"]",th);
                }
            }
            return "";
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(this.getCode2());
            sb.append("/");
            sb.append(this.getCode3());
            sb.append(" ");
            sb.append(this.getName());
            return sb.toString();
        }

    }

    // ------------------------------------------------------------------------

    public static final CountryInfo CountryMapArray[] = new CountryInfo[] {
        //              Code  Code   Name
        //              2-dig 3-dig
        //              ----- ------ -------------------------
        new CountryInfo("AF", "AFG", "Afghanistan"            ),
        new CountryInfo("AL", "ALB", "Albania"                ),
        new CountryInfo("DZ", "DZA", "Algeria"                ),
        new CountryInfo("AD", "AND", "Andorra"                ),
        new CountryInfo("AO", "AGO", "Angola"                 ),
        new CountryInfo("AI", "AIA", "Anguilla"               ),
        new CountryInfo("AQ", "ATA", "Antarctica"             ),
        new CountryInfo("AG", "ATG", "Antigua/Barbuda"        ),
        new CountryInfo("AR", "ARG", "Argentina"              ),
        new CountryInfo("AM", "ARM", "Armenia"                ),
        new CountryInfo("AW", "ABW", "Aruba"                  ),
        new CountryInfo("AU", "AUS", "Australia"              ),
        new CountryInfo("AT", "AUT", "Austria"                ),
        new CountryInfo("AZ", "AZE", "Azerbaijan"             ),
        new CountryInfo("BS", "BHS", "Bahamas"                ),
        new CountryInfo("BH", "BHR", "Bahrain"                ),
        new CountryInfo("BD", "BGD", "Bangladesh"             ),
        new CountryInfo("BB", "BRB", "Barbados"               ),
        new CountryInfo("BY", "BLR", "Belarus"                ),
        new CountryInfo("BE", "BEL", "Belgium"                ),
        new CountryInfo("BZ", "BLZ", "Belize"                 ),
        new CountryInfo("BJ", "BEN", "Benin"                  ),
        new CountryInfo("BM", "BMU", "Bermuda"                ),
        new CountryInfo("BT", "BTN", "Bhutan"                 ),
        new CountryInfo("BO", "BOL", "Bolivia"                ),
        new CountryInfo("BQ", "BES", "Bonaire"                ),
        new CountryInfo("BA", "BIH", "Bosnia/Herzegovina"     ),
        new CountryInfo("BW", "BWA", "Botswana"               ),
        new CountryInfo("BR", "BRA", "Brazil"                 ),
        new CountryInfo("BN", "BRN", "Brunei Darussalam"      ),
        new CountryInfo("BG", "BGR", "Bulgaria"               ),
        new CountryInfo("BF", "BFA", "Burkina Faso"           ),
        new CountryInfo("BI", "BDI", "Burundi"                ),
        new CountryInfo("KH", "KHM", "Cambodia"               ),
        new CountryInfo("CM", "CMR", "Cameroon"               ),
        new CountryInfo("CA", "CAN", "Canada"                 , Canada.class, "getProvinceName"),
        new CountryInfo("CV", "CPV", "Cape Verde"             ),
        new CountryInfo("KY", "CYM", "Cayman Islands"         ),
        new CountryInfo("TD", "TCD", "Chad"                   ),
        new CountryInfo("CL", "CHL", "Chile"                  ),
        new CountryInfo("CN", "CHN", "China"                  ),
        new CountryInfo("CO", "COL", "Colombia"               ),
        new CountryInfo("KM", "COM", "Comoros"                ),
        new CountryInfo("CG", "COG", "Congo"                  ),
        new CountryInfo("CD", "COD", "Congo"                  ),
        new CountryInfo("CR", "CRI", "Costa Rica"             ),
        new CountryInfo("HR", "HRV", "Croatia"                ),
        new CountryInfo("CU", "CUB", "Cuba"                   ),
        new CountryInfo("CY", "CYP", "Cyprus"                 ),
        new CountryInfo("CZ", "CZE", "Czech Republic"         ),
        new CountryInfo("CI", "CIV", "Ivory Coast "           ),
        new CountryInfo("DK", "DNK", "Denmark"                ),
        new CountryInfo("DJ", "DJI", "Djibouti"               ),
        new CountryInfo("DM", "DMA", "Dominica"               ),
        new CountryInfo("DO", "DOM", "Dominican Republic"     ),
        new CountryInfo("EC", "ECU", "Ecuador"                ),
        new CountryInfo("EG", "EGY", "Egypt"                  ),
        new CountryInfo("SV", "SLV", "El Salvador"            ),
        new CountryInfo("EE", "EST", "Estonia"                ),
        new CountryInfo("ET", "ETH", "Ethiopia"               ),
        new CountryInfo("FJ", "FJI", "Fiji"                   ),
        new CountryInfo("FI", "FIN", "Finland"                ),
        new CountryInfo("FR", "FRA", "France"                 ),
        new CountryInfo("GA", "GAB", "Gabon"                  ),
        new CountryInfo("GM", "GMB", "Gambia"                 ),
        new CountryInfo("GE", "GEO", "Georgia"                ),
        new CountryInfo("DE", "DEU", "Germany"                ),
        new CountryInfo("GH", "GHA", "Ghana"                  ),
        new CountryInfo("GI", "GIB", "Gibraltar"              ),
        new CountryInfo("GR", "GRC", "Greece"                 ),
        new CountryInfo("GL", "GRL", "Greenland"              ),
        new CountryInfo("GD", "GRD", "Grenada"                ),
        new CountryInfo("GP", "GLP", "Guadeloupe"             ),
        new CountryInfo("GU", "GUM", "Guam"                   ),
        new CountryInfo("GT", "GTM", "Guatemala"              ),
        new CountryInfo("GG", "GGY", "Guernsey"               ),
        new CountryInfo("GN", "GIN", "Guinea"                 ),
        new CountryInfo("GY", "GUY", "Guyana"                 ),
        new CountryInfo("HT", "HTI", "Haiti"                  ),
        new CountryInfo("HN", "HND", "Honduras"               ),
        new CountryInfo("HK", "HKG", "Hong Kong"              ),
        new CountryInfo("HU", "HUN", "Hungary"                ),
        new CountryInfo("IS", "ISL", "Iceland"                ),
        new CountryInfo("IN", "IND", "India"                  ),
        new CountryInfo("ID", "IDN", "Indonesia"              ),
        new CountryInfo("IR", "IRN", "Iran"                   ),
        new CountryInfo("IQ", "IRQ", "Iraq"                   ),
        new CountryInfo("IE", "IRL", "Ireland"                ),
        new CountryInfo("IM", "IMN", "Isle of Man"            ),
        new CountryInfo("IL", "ISR", "Israel"                 ),
        new CountryInfo("IT", "ITA", "Italy"                  ),
        new CountryInfo("JM", "JAM", "Jamaica"                ),
        new CountryInfo("JP", "JPN", "Japan"                  ),
        new CountryInfo("JE", "JEY", "Jersey"                 ),
        new CountryInfo("JO", "JOR", "Jordan"                 ),
        new CountryInfo("KZ", "KAZ", "Kazakhstan"             ),
        new CountryInfo("KE", "KEN", "Kenya"                  ),
        new CountryInfo("KI", "KIR", "Kiribati"               ),
        new CountryInfo("KR", "KOR", "South Korea"            ),
        new CountryInfo("KW", "KWT", "Kuwait"                 ),
        new CountryInfo("KG", "KGZ", "Kyrgyzstan"             ),
        new CountryInfo("LA", "LAO", "Lao"                    ),
        new CountryInfo("LV", "LVA", "Latvia"                 ),
        new CountryInfo("LB", "LBN", "Lebanon"                ),
        new CountryInfo("LS", "LSO", "Lesotho"                ),
        new CountryInfo("LR", "LBR", "Liberia"                ),
        new CountryInfo("LY", "LBY", "Libya"                  ),
        new CountryInfo("LI", "LIE", "Liechtenstein"          ),
        new CountryInfo("LT", "LTU", "Lithuania"              ),
        new CountryInfo("LU", "LUX", "Luxembourg"             ),
        new CountryInfo("MO", "MAC", "Macao"                  ),
        new CountryInfo("MK", "MKD", "Macedonia"              ),
        new CountryInfo("MG", "MDG", "Madagascar"             ),
        new CountryInfo("MW", "MWI", "Malawi"                 ),
        new CountryInfo("MY", "MYS", "Malaysia"               ),
        new CountryInfo("MV", "MDV", "Maldives"               ),
        new CountryInfo("ML", "MLI", "Mali"                   ),
        new CountryInfo("MT", "MLT", "Malta"                  ),
        new CountryInfo("MH", "MHL", "Marshall Islands"       ),
        new CountryInfo("MQ", "MTQ", "Martinique"             ),
        new CountryInfo("MR", "MRT", "Mauritania"             ),
        new CountryInfo("MU", "MUS", "Mauritius"              ),
        new CountryInfo("YT", "MYT", "Mayotte"                ),
        new CountryInfo("MX", "MEX", "Mexico"                 , Mexico.class, "getStateName"),
        new CountryInfo("FM", "FSM", "Micronesia"             ),
        new CountryInfo("MD", "MDA", "Moldova"                ),
        new CountryInfo("MC", "MCO", "Monaco"                 ),
        new CountryInfo("MN", "MNG", "Mongolia"               ),
        new CountryInfo("ME", "MNE", "Montenegro"             ),
        new CountryInfo("MS", "MSR", "Montserrat"             ),
        new CountryInfo("MA", "MAR", "Morocco"                ),
        new CountryInfo("MZ", "MOZ", "Mozambique"             ),
        new CountryInfo("NA", "NAM", "Namibia"                ),
        new CountryInfo("NR", "NRU", "Nauru"                  ),
        new CountryInfo("NP", "NPL", "Nepal"                  ),
        new CountryInfo("NL", "NLD", "Netherlands"            ),
        new CountryInfo("NC", "NCL", "New Caledonia"          ),
        new CountryInfo("NZ", "NZL", "New Zealand"            ),
        new CountryInfo("NI", "NIC", "Nicaragua"              ),
        new CountryInfo("NE", "NER", "Niger"                  ),
        new CountryInfo("NG", "NGA", "Nigeria"                ),
        new CountryInfo("NU", "NIU", "Niue"                   ),
        new CountryInfo("NF", "NFK", "Norfolk Island"         ),
        new CountryInfo("NO", "NOR", "Norway"                 ),
        new CountryInfo("OM", "OMN", "Oman"                   ),
        new CountryInfo("PK", "PAK", "Pakistan"               ),
        new CountryInfo("PW", "PLW", "Palau"                  ),
        new CountryInfo("PS", "PSE", "Palestine"              ),
        new CountryInfo("PA", "PAN", "Panama"                 ),
        new CountryInfo("PY", "PRY", "Paraguay"               ),
        new CountryInfo("PE", "PER", "Peru"                   ),
        new CountryInfo("PH", "PHL", "Philippines"            ),
        new CountryInfo("PN", "PCN", "Pitcairn"               ),
        new CountryInfo("PL", "POL", "Poland"                 ),
        new CountryInfo("PT", "PRT", "Portugal"               ),
        new CountryInfo("PR", "PRI", "Puerto Rico"            ),
        new CountryInfo("QA", "QAT", "Qatar"                  ),
        new CountryInfo("RO", "ROU", "Romania"                ),
        new CountryInfo("RU", "RUS", "Russia"                 ),
        new CountryInfo("RW", "RWA", "Rwanda"                 ),
        new CountryInfo("RE", "REU", "Reunion"                ),
        new CountryInfo("LC", "LCA", "Saint Lucia"            ),
        new CountryInfo("MF", "MAF", "Saint Martin (French)"  ),
        new CountryInfo("WS", "WSM", "Samoa"                  ),
        new CountryInfo("SM", "SMR", "San Marino"             ),
        new CountryInfo("SA", "SAU", "Saudi Arabia"           ),
        new CountryInfo("SN", "SEN", "Senegal"                ),
        new CountryInfo("RS", "SRB", "Serbia"                 ),
        new CountryInfo("SC", "SYC", "Seychelles"             ),
        new CountryInfo("SL", "SLE", "Sierra Leone"           ),
        new CountryInfo("SG", "SGP", "Singapore"              ),
        new CountryInfo("SX", "SXM", "Sint Maarten (Dutch)"   ),
        new CountryInfo("SK", "SVK", "Slovakia"               ),
        new CountryInfo("SI", "SVN", "Slovenia"               ),
        new CountryInfo("SO", "SOM", "Somalia"                ),
        new CountryInfo("ZA", "ZAF", "South Africa"           ),
        new CountryInfo("ES", "ESP", "Spain"                  ),
        new CountryInfo("LK", "LKA", "Sri Lanka"              ),
        new CountryInfo("SD", "SDN", "Sudan"                  ),
        new CountryInfo("SR", "SUR", "Suriname"               ),
        new CountryInfo("SZ", "SWZ", "Swaziland"              ),
        new CountryInfo("SE", "SWE", "Sweden"                 ),
        new CountryInfo("CH", "CHE", "Switzerland"            ),
        new CountryInfo("SY", "SYR", "Syria"                  ),
        new CountryInfo("TW", "TWN", "Taiwan"                 ),
        new CountryInfo("TJ", "TJK", "Tajikistan"             ),
        new CountryInfo("TZ", "TZA", "Tanzania"               ),
        new CountryInfo("TH", "THA", "Thailand"               ),
        new CountryInfo("TL", "TLS", "Timor-Leste"            ),
        new CountryInfo("TG", "TGO", "Togo"                   ),
        new CountryInfo("TK", "TKL", "Tokelau"                ),
        new CountryInfo("TO", "TON", "Tonga"                  ),
        new CountryInfo("TT", "TTO", "Trinidad/Tobago"        ),
        new CountryInfo("TN", "TUN", "Tunisia"                ),
        new CountryInfo("TR", "TUR", "Turkey"                 ),
        new CountryInfo("TM", "TKM", "Turkmenistan"           ),
        new CountryInfo("TV", "TUV", "Tuvalu"                 ),
        new CountryInfo("UG", "UGA", "Uganda"                 ),
        new CountryInfo("UA", "UKR", "Ukraine"                ),
        new CountryInfo("AE", "ARE", "United Arab Emirates"   ),
        new CountryInfo("GB", "GBR", "United Kingdom"         ),
        new CountryInfo("US", "USA", "United States"          , USState.class, "getStateName"),
        new CountryInfo("UY", "URY", "Uruguay"                ),
        new CountryInfo("UZ", "UZB", "Uzbekistan"             ),
        new CountryInfo("VU", "VUT", "Vanuatu"                ),
        new CountryInfo("VE", "VEN", "Venezuela"              ),
        new CountryInfo("VN", "VNM", "Viet Nam"               ),
        new CountryInfo("VG", "VGB", "British Virgin Islands" ),
        new CountryInfo("VI", "VIR", "US Virgin Islands"      ),
        new CountryInfo("ZM", "ZMB", "Zambia"                 ),
        new CountryInfo("ZW", "ZWE", "Zimbabwe"               ),
    };

    // -- startup initialization
    static {
        for (int i = 0; i < CountryMapArray.length; i++) {
            // -- add CODE-2
            String code2 = CountryMapArray[i].getCode2(); // never blank
            GlobalCountryMap.put(code2, CountryMapArray[i]);
            // -- add CODE-3
            String code3 = CountryMapArray[i].getCode3(); // may be blank
            if (!StringTools.isBlank(code3)) {
                GlobalCountryMap.put(code3, CountryMapArray[i]);
            }
        }
    }

    /**
    *** Gets the collection of StateInfo keys (state codes)
    **/
    public static Collection<String> getCountryInfoKeys()
    {
        return GlobalCountryMap.keySet();
    }

    /**
    *** Gets the CountryInfo instance for the specified country code
    **/
    public static CountryInfo getCountryInfo(String code)
    {
        if (!StringTools.isBlank(code)) {
            return GlobalCountryMap.get(code);
        } else {
            return null;
        }
    }

    /**
    *** Returns true if the specified country code exists
    **/
    public static boolean hasCountryInfo(String code)
    {
        return (CountryCode.getCountryInfo(code) != null)? true : false;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified country code is defined
    *** @param code  The country code
    *** @return True if teh specified country is defined, false otherwise
    **/
    public static boolean isCountryCode(String code)
    {
        return CountryCode.hasCountryInfo(code);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the country name for the specified country code
    *** @param code  The country code
    *** @return The country name, or an empty String if the country code was not found
    **/
    public static String getCountryName(String code)
    {
        CountryInfo ci = CountryCode.getCountryInfo(code);
        return (ci != null)? ci.getName() : "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the subdivision name for the specified CountryCode/StateCode
    **/
    public static String getSubdivisionName(String subDiv)
    {
        if (!StringTools.isBlank(subDiv)) {
            int p = subDiv.indexOf(SUBDIVISION_SEPARATOR);
            if (p >= 0) {
                String CC = subDiv.substring(0,p);
                String SC = subDiv.substring(p+1);
                return CountryCode.getSubdivisionName(CC,SC);
            }
        }
        return "";
    }

    /**
    *** Gets the subdivision name for the specified CountryCode/StateCode
    **/
    public static String getSubdivisionName(String countryCode, String stateCode)
    {
        if (!StringTools.isBlank(countryCode) && !StringTools.isBlank(stateCode)) {
            CountryInfo ci = CountryCode.getCountryInfo(countryCode);
            if ((ci != null) && ci.supportsSubdivisionName()) {
                return ci.getSubdivisionName(stateCode);
            }
        }
        return "";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the 2-digit country code for the specified code
    *** @param code  The 2 or 3-digit country code
    *** @param dft   The default code to return if the specified country code is not found
    *** @return The state code
    **/
    public static String getCountryCode(String code, String dft)
    {
        CountryInfo ci = CountryCode.getCountryInfo(code);
        return (ci != null)? ci.getCode() : dft;
    }

    // ------------------------------------------------------------------------

}

