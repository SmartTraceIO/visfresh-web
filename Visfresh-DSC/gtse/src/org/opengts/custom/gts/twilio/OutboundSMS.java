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
// Description:
//  Twilio Outbound SMS Gateway support
// ----------------------------------------------------------------------------
// Change History:
//  2014/12/05  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.custom.gts.twilio;

// -- Java standard
import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

// -- Apache http library (HttpComponents/HttpCore) [http://hc.apache.org/]
// -  http://www.us.apache.org/dist/httpcomponents/httpcore/binary/httpcomponents-core-4.4.1-bin.zip
// -  Jar: httpcore-4.4.1.jar
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

// -- Twilio SDK [https://www.twilio.com/docs/java/install]
// -  http://search.maven.org/remotecontent?filepath=com/twilio/sdk/twilio-java-sdk/4.0.2/twilio-java-sdk-4.0.2-jar-with-dependencies.jar
// -  Jar: twilio-java-sdk-4.0.2-jar-with-dependencies.jar
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import com.twilio.sdk.resource.list.MessageList;

// -- OpenGTS
import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.tables.*;
import org.opengts.db.*;

/**
*** Twilio Outbound SMS gateway handler
**/
public class OutboundSMS
    extends SMSOutboundGateway
{

    // ------------------------------------------------------------------------

    /* Twilio properties */
    public  static final String PROP_twilio_className           = SMSOutboundGateway.PROP_twilio_className;
    public  static final String PROP_twilio_maxMessageLength    = SMSOutboundGateway.PROP_twilio_maxMessageLength;
    public  static final String PROP_twilio_accountSID          = SMSOutboundGateway.PROP_twilio_accountSID;
    public  static final String PROP_twilio_authToken           = SMSOutboundGateway.PROP_twilio_authToken;
    public  static final String PROP_twilio_fromPhoneNumber     = SMSOutboundGateway.PROP_twilio_fromPhoneNumber;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Adjust phone number to conform to Twilio requirements
    **/
    private static String AdjustPhoneNumber(String phNum)
    {

        /* trim and perform simple checks */
        String phn = StringTools.trim(phNum);
        if (phn.equals("")) {
            // -- blank phone number
            return phn;
        } else
        if (phn.startsWith("+")) {
            // -- already prefixed with "+" 
            return phn;
        }

        /* add required prefix */
        if ((phn.length() == 11) && phn.startsWith("1")) {
            // -- assume US with "1" prefix
            // -  "14155551212" ==> "+14155551212"
            return "+" + phn;
        } else
        if ((phn.length() == 10) && !phn.startsWith("1") && !phn.startsWith("0")) {
            // -- assume US without "1" prefix
            // -  "4155551212" ==> "+14155551212"
            return "+1" + phn;
        } else {
            // -- assume non-US with prefixing country code
            // -  "9876543" ==> "+9876543"
            return "+" + phn;
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Constructor
    **/
    public OutboundSMS()
    {
        //
    }

    // ------------------------------------------------------------------------

    /**
    *** Send SMS
    **/
    protected DCServerFactory.ResultCode _sendSMS(String smsPhone, String message)
    {

        /* Twilio properties */
        String twilioAccountSID = RTConfig.getString(PROP_twilio_accountSID     ,"");
        String twilioAuthToken  = RTConfig.getString(PROP_twilio_authToken      ,"");
        String twilioFromPhone  = RTConfig.getString(PROP_twilio_fromPhoneNumber,"");

        /* Twilio parameters */
        java.util.List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("From"    , AdjustPhoneNumber(twilioFromPhone)));
        params.add(new BasicNameValuePair("To"      , AdjustPhoneNumber(smsPhone)));
        params.add(new BasicNameValuePair("Body"    , StringTools.trim(message)));
      //params.add(new BasicNameValuePair("MediaUrl", "http://www.example.com/image.png"));

        /* try sending SMS */
        try {
            TwilioRestClient trc = new TwilioRestClient(twilioAccountSID, twilioAuthToken);
            MessageFactory msgFactory = trc.getAccount().getMessageFactory();
            Message msg = msgFactory.create(params);
            // -- assume successful?
            return DCServerFactory.ResultCode.SUCCESS;
        } catch (TwilioRestException tre) {
            // -- Twilio exception
            // 200 OK: The request was successful and the response body contains the representation requested.
            // 201 CREATED: The request was successful, we created a new resource and the response body contains the representation.
            // 204 OK: The request was successful; the resource was deleted.
            // 302 FOUND: A common redirect response; you can GET the representation at the URI in the Location response header.
            // 304 NOT MODIFIED: Your client's cached version of the representation is still up to date.
            // 400 BAD REQUEST: The data given in the POST or PUT failed validation. Inspect the response body for details.
            // 401 UNAUTHORIZED: The supplied credentials, if any, are not sufficient to access the resource.
            // 404 NOT FOUND: You know this one.
            // 405 METHOD NOT ALLOWED: You can't POST or PUT to the resource.
            // 429 TOO MANY REQUESTS: Your application is sending too many simultaneous requests.
            // 500 SERVER ERROR: We couldn't return the representation due to an internal server error.
            // 503 SERVICE UNAVAILABLE: We are temporarily unable to return the representation. Please wait for a bit and try again.
            int errCode = tre.getErrorCode();
            Print.logError("Error sending SMS: " + tre);
            return DCServerFactory.ResultCode.GATEWAY_ERROR;
        } catch (Throwable th) {
            // -- unknown exception
            Print.logException("Unknown error sending SMS", th);
            return DCServerFactory.ResultCode.UNKNOWN;
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Send an SMS command to the specified device
    *** @param device  The Device to which the command is sent (must not be null).
    *** @param command The command String to send to the Device.
    *** @return The result code
    **/
    public DCServerFactory.ResultCode sendSMSCommand(Device device, String commandStr)
    {

        /* pre-validate */
        if (device == null) { 
            return DCServerFactory.ResultCode.INVALID_DEVICE; 
        }
        if (!SMSOutboundGateway.IsDeviceAuthorized(device)) { 
            return DCServerFactory.ResultCode.NOT_AUTHORIZED; 
        }

        /* check message length */
        int maxLen = RTConfig.getInt(PROP_twilio_maxMessageLength, SMSOutboundGateway.GetDefaultMaximumMessageLength());
        String message = commandStr;
        if (StringTools.length(message) > maxLen) {
            // -- exceeds maximum allowed SMS text length
            Print.logWarn("Invalid SMS text length: " + StringTools.length(message));
        }

        /* send SMS */
        String smsPhone = device.getSimPhoneNumber();
        return this._sendSMS(smsPhone, message);

    }

    // ------------------------------------------------------------------------

    /**
    *** Send an SMS message to the specified phone number.
    *** @param account    The Account for which this SMS message is being sent (must not be null)
    *** @param device     The Device for which this SMS message is being sent (may be null)
    *** @param smsMessage The SMS message sent to the destination phone number
    *** @param smsPhone   The destination phone number
    *** @return The result code
    **/
    public DCServerFactory.ResultCode sendSMSMessage(Account account, Device device, String smsMessage, String smsPhone)
    {

        /* pre-validate */
        if (account == null) { 
            return DCServerFactory.ResultCode.INVALID_ACCOUNT; 
        }

        /* check message length */
        int maxLen = RTConfig.getInt(PROP_twilio_maxMessageLength, SMSOutboundGateway.GetDefaultMaximumMessageLength());
        String message = SMSOutboundGateway.TruncateMessageToLength(smsMessage, maxLen);

        /* send SMS */
        return this._sendSMS(smsPhone, message);
 
    }

}
