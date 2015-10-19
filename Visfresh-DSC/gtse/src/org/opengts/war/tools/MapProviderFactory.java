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
//  2007/01/25  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
// Map alternatives:
//   http://mapserver.gis.umn.edu
//   http://www.cartoweb.org
//   http://tiger.census.gov/instruct.html
// ----------------------------------------------------------------------------
package org.opengts.war.tools;

import java.util.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.db.*;

public class MapProviderFactory
{
    
    // ------------------------------------------------------------------------

    private static final String MAP_PROVIDER_PACKAGE = DBConfig.PACKAGE_WAR_ + "maps";
    
    // ------------------------------------------------------------------------

    private static MapProviderFactory mapFactory = null;
    
    public static MapProviderFactory getInstance()
    {
        if (mapFactory == null) {
            mapFactory = new MapProviderFactory();
        }
        return mapFactory;
    }
    
    // ------------------------------------------------------------------------

    public static MapProvider getMapProviderForName(String providerClassName)
    {
        return getInstance().getMapProvider(providerClassName);
    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private HashMap<String,MapProvider> mapProviderMap = new HashMap<String,MapProvider>();
    
    private MapProviderFactory()
    {
        super();
    }

    // ------------------------------------------------------------------------

    private MapProvider getMapProvider(String providerClassName)
    {
        MapProvider mp = null;
        if (this.mapProviderMap.containsKey(providerClassName)) {

            /* already initialized */
            mp = this.mapProviderMap.get(providerClassName);

        } else {

            /* construct class name */
            String clzName = null;
            if (providerClassName.indexOf(".") >= 0) {
                clzName = providerClassName;
            } else {
                clzName = MAP_PROVIDER_PACKAGE + "." + providerClassName;
            }
            
            /* get instance of MapProvider */
            try {
                Class providerClass = Class.forName(clzName);
                mp = (MapProvider)providerClass.newInstance();
                this.mapProviderMap.put(providerClassName, mp);
                //Print.logInfo("Found MapProvider: " + clzName);
            } catch (Throwable t) { // ClassNotFoundException, ClassCastException, etc.
                Print.logError("MapProvider creation error: " + clzName + " [" + t);
                mp = null;
            }
            
        }
        return mp;
    }
    
}
