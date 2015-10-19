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
//  02/19/2006  Martin D. Flynn
//      Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;
import java.net.*;
import java.security.cert.Certificate;

public class ZipTools
{
    
    /**
    *** Get Zip table of contents
    **/
    public static java.util.List<String> getTableOfContents(byte zipData[])
    {
        java.util.List<String> toc = new Vector<String>();
        ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(bais);
            for (;;) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) { break; }
                toc.add(ze.getName());
                zis.closeEntry();
            }
        } catch (IOException ioe) {
            Print.logError("Reading Zip: " + ioe);
        } finally {
            try { zis.close();  } catch (Throwable th) {}
            try { bais.close(); } catch (Throwable th) {}
        }
        return toc;
    }
    
    /**
    *** Get file data 
    **/
    public static byte[] readEntry(byte zipData[], String name)
    {
        byte data[] = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(zipData);
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(bais);
            for (;;) {
                ZipEntry ze = zis.getNextEntry();
                if (ze == null) {
                    break;
                } else
                if (name.equals(ze.getName())) {
                    byte d[] = new byte[10*1024];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (;;) {
                        int size = zis.read(d,0,d.length);
                        if (size < 0) { break; }
                        baos.write(d,0,size);
                    }
                    data = baos.toByteArray();
                    zis.closeEntry();
                    break;
                }
                zis.closeEntry();
            }
        } catch (IOException ioe) {
            Print.logError("Reading Zip: " + ioe);
        } finally {
            try { zis.close();  } catch (Throwable th) {}
            try { bais.close(); } catch (Throwable th) {}
        }
        return data;
    }
    
}
