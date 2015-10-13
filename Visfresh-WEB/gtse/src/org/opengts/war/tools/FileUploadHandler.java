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
//  2011/08/21  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.war.tools;

import java.lang.*;
import java.util.*;

import org.opengts.util.*;

public interface FileUploadHandler
{

    /**
    *** Handle a File Upload request
    *** @param context              The "context" of the File Upload
    *** @param name                 The MIME name
    *** @param contentType          The MIME "content-type" value
    *** @param contentDisposition   The MIME "content-disposition" value
    *** @param fileName             The MIME upload file name
    *** @param fileBytes            The MIME upload file bytes
    *** @return The response String
    **/
    public String handleFileUpload(
        String context, RequestProperties reqState, 
        String name, String contentType, String contentDisposition,
        String fileName, byte fileBytes[]);

}
