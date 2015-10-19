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
//  2009/07/01  Martin D. Flynn
//     -Initial release
//  2014/03/03  Martin D. Flynn
//     -Added MSG_ACCOUNT_SERVICE
// ----------------------------------------------------------------------------
package org.opengts.extra.service;

import java.util.*;
import java.math.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.opengts.Version;
import org.opengts.util.*;
import org.opengts.dbtools.*;

public class ServiceMessage
{

    // ------------------------------------------------------------------------

    private static ServiceMessage errMsg(String c, String m) { return new ServiceMessage(true , c, m); }
    private static ServiceMessage okMsg( String c, String m) { return new ServiceMessage(false, c, m); }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* MessageMap initialization must be first */
    private static Map<String,ServiceMessage> MessageMap           = new OrderedMap<String,ServiceMessage>();

    public static final ServiceMessage MSG_SUCCESSFUL              = okMsg( "OK0000","Successful");

    public static final ServiceMessage MSG_COMMAND_MISSING         = errMsg("CM0010","Command invalid");
    public static final ServiceMessage MSG_COMMAND_UNSUPPORTED     = errMsg("CM0011","Command not supported");
    public static final ServiceMessage MSG_COMMAND_DISABLED        = errMsg("CM0015","Command disabled");
    public static final ServiceMessage MSG_COMMAND_NOTAUTH         = errMsg("CM0020","Command not authorized");

    public static final ServiceMessage MSG_AUTH_FAILED             = errMsg("AU0010","Authorization failed");

    public static final ServiceMessage MSG_ACCOUNT_INVALID         = MSG_AUTH_FAILED;
    public static final ServiceMessage MSG_ACCOUNT_INACTIVE        = errMsg("AC0020","Account inactive");
    public static final ServiceMessage MSG_ACCOUNT_EXPIRED         = errMsg("AC0030","Account expired");
    public static final ServiceMessage MSG_ACCOUNT_HOST            = errMsg("AC0040","Account not authorized for host");
    public static final ServiceMessage MSG_ACCOUNT_COMMAND         = errMsg("AC0042","Account not authorized for command");
    public static final ServiceMessage MSG_ACCOUNT_SERVICE         = errMsg("AC0046","Account web-service disabled");

    public static final ServiceMessage MSG_USER_INVALID            = MSG_AUTH_FAILED;
    public static final ServiceMessage MSG_USER_INACTIVE           = errMsg("US0020","User inactive");
    
    public static final ServiceMessage MSG_DEVICE_INVALID          = errMsg("DV0010","DeviceID invalid");
    
    public static final ServiceMessage MSG_GROUP_INVALID           = errMsg("GR0010","DeviceGroup ID invalid");
    
    public static final ServiceMessage MSG_DATETIME                = errMsg("DT0010","from/to date invalid");

    public static final ServiceMessage MSG_PRIVATE_XML_SYNTAX      = errMsg("PL0010","'private.xml' syntax/parsing errors");
    public static final ServiceMessage MSG_PRIVATE_XML_CONFIG      = errMsg("PL0020","'private.xml' configuration errors");
    public static final ServiceMessage MSG_URL_NOT_ALLOWED         = errMsg("PL0030","Specified URL not allowed");
    
    public static final ServiceMessage MSG_REQUEST_DISABLED        = errMsg("RQ0010","Service Request disabled");
    public static final ServiceMessage MSG_REQUEST_POST_REQUIRED   = errMsg("RQ0020","Request XML requires 'POST'");
    public static final ServiceMessage MSG_REQUEST_XML_SYNTAX      = errMsg("RQ0030","Request XML syntax errors");
	public static final ServiceMessage MSG_REQUEST_SOAP_XML_SYNTAX = errMsg("RQ0031","SOAP XML syntax error");
    public static final ServiceMessage MSG_REQUEST_XML_INVALID     = errMsg("RQ0040","Request XML is invalid");
    public static final ServiceMessage MSG_REQUEST_NOT_SUPPORTED   = errMsg("RQ0050","Request not supported");

    public static final ServiceMessage MSG_BAD_TABLE               = errMsg("DB0010","Invalid table name");
    public static final ServiceMessage MSG_BAD_RECORD_KEY          = errMsg("DB0020","Invalid DBRecordKey");
    public static final ServiceMessage MSG_BAD_RECORD              = errMsg("DB0030","Invalid DBRecord");
    public static final ServiceMessage MSG_NOT_FOUND               = errMsg("DB0040","Record not found");
    public static final ServiceMessage MSG_ALREADY_EXISTS          = errMsg("DB0045","Record already exists");
    public static final ServiceMessage MSG_READ_FAILED             = errMsg("DB0050","Record read failed");
    public static final ServiceMessage MSG_UPDATE_FAILED           = errMsg("DB0060","Record update failed");
    public static final ServiceMessage MSG_INSERT_FAILED           = errMsg("DB0065","Record insert failed");
    public static final ServiceMessage MSG_DELETE_FAILED           = errMsg("DB0070","Record delete failed");

    public static final ServiceMessage MSG_PROP_BAD_KEY            = errMsg("PR0010","Invalid property key");

    public static final ServiceMessage MSG_NO_REPORT_SPECIFIED     = errMsg("RP0007","No Report specified");
    public static final ServiceMessage MSG_REPORT_NOT_FOUND        = errMsg("RP0010","Report not found");
    public static final ServiceMessage MSG_REPORT_INVALID_NAME     = errMsg("RP0015","Invalid report name");
    public static final ServiceMessage MSG_REPORT_DEVICE           = errMsg("RP0030","Report missing Device/Group");
    public static final ServiceMessage MSG_REPORT_CREATE           = errMsg("RP0040","Unable to create report");
    public static final ServiceMessage MSG_REPORT_GENERAL_ERROR    = errMsg("RP0800","Reporting Error occurred");
    public static final ServiceMessage MSG_REPORT_UNEXPECTED       = errMsg("RP0999","Unexpected Reporting Error");

    public static final ServiceMessage MSG_MAP_NOT_FOUND           = errMsg("MP0010","MapProvider not found");

    public static final ServiceMessage MSG_CUSTOM_NOT_SUPPORTED    = errMsg("CU0010","Custom command not supported");
    public static final ServiceMessage MSG_CUSTOM_TYPE_INVALID     = errMsg("CU0020","Custom type invalid");

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the MessageMap
    **/
    public static Map<String,ServiceMessage> getMessageMap()
    {
        return MessageMap;
    }

    /**
    *** Gets the specific ServiceMessage for the specified code
    **/
    public static ServiceMessage getMessageForCode(String code)
    {
        if (StringTools.isBlank(code)) {
            return null;
        } else {
            return MessageMap.get(code);
        }
    }

    // ------------------------------------------------------------------------

    private boolean isError = false;
    private String  code    = null;
    private String  message = null;

    /**
    *** Constructor 
    **/
    public ServiceMessage(boolean isError, String code, String msg) 
    {
        this.isError = isError;
        this.code    = code;
        this.message = msg;
        if (MessageMap.containsKey(code)) {
            Print.logWarn("Message code already defined: " + code);
        }
        MessageMap.put(code, this);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this message indicates an error
    **/
    public boolean isError() 
    {
        return this.isError;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the message code
    **/
    public String getCode()
    {
        return this.code;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the message description
    **/
    public String getMessage() 
    {
        return this.message;
    }

    /**
    *** Gets the message description
    **/
    public String getMessage(String text) 
    {
        if (StringTools.isBlank(text)) {
            return this.message;
        } else {
            return this.message + " [" + text + "]";
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Specific check for web-service disabled error
    **/
    public boolean isServiceDisabledError()
    {
        return this.equals(MSG_REQUEST_DISABLED);
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a String representation of this Message
    **/
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getCode());
        sb.append(": ");
        sb.append(this.getMessage());
        return sb.toString();
    }

    // ------------------------------------------------------------------------

    /**
    *** Return true if the specified object is equal to this object
    **/
    public boolean equals(Object other)
    {
        if (other instanceof ServiceMessage) {
            return ((ServiceMessage)other).getCode().equals(this.getCode());
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------

}
