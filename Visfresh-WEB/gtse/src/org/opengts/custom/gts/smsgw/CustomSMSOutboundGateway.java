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
// Custom SMS Outbound Gateway
// ----------------------------------------------------------------------------
// Change History:
//  2014/11/30  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.custom.gts.smsgw;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

/**
*** Custom Outbound SMS gateway handler
**/
public class CustomSMSOutboundGateway
    extends SMSOutboundGateway
{

    // ------------------------------------------------------------------------

    public CustomSMSOutboundGateway()
    {

        /* Add any required custom initialization here */
        // -- TODO:

    }

    // ------------------------------------------------------------------------

    /**
    *** Send an SMS command to the specified device
    *** @param device  The Device to which the command is sent (must not be null).
    *** @param command The command String to send to the Device.
    *** @return The result code
    **/
    public DCServerFactory.ResultCode sendSMSCommand(Device device, String command)
    {

        /* pre-validate */
        if (device == null) { 
            return DCServerFactory.ResultCode.INVALID_DEVICE; 
        }
        if (!SMSOutboundGateway.IsDeviceAuthorized(device)) { 
            return DCServerFactory.ResultCode.NOT_AUTHORIZED; 
        }

        /* exceeds maximum message/command length? */
        String message = command;
        if (StringTools.length(message) > SMSOutboundGateway.GetDefaultMaximumMessageLength()) {
            Print.logWarn("Invalid SMS text command length: " + StringTools.length(message));
            message = SMSOutboundGateway.TruncateMessageToMaximumLength(message);
        }

        /* receipient */
        String recipient = device.getSimPhoneNumber();

        /* Add outbound SMS message delivery here */
        // -- TODO: send "message" to "recipient"
        Print.logWarn("Custom 'sendSMSCommand' not yet implemented");
        Print.logWarn("Recipient: " + recipient);
        Print.logWarn("Command  : " + message);
        return DCServerFactory.ResultCode.NOT_SUPPORTED;
        //return DCServerFactory.ResultCode.SUCCESS;

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
    public DCServerFactory.ResultCode sendSMSMessage(Account account, Device device, 
        String smsMessage, String smsPhone)
    {

        /* pre-validate */
        if (account == null) { 
            return DCServerFactory.ResultCode.INVALID_ACCOUNT; 
        }

        /* exceeds maximum message length? */
        String message = smsMessage;
        int maxLen = RTConfig.getInt(SMSOutboundGateway.PROP_custom_maxMessageLength, SMSOutboundGateway.GetDefaultMaximumMessageLength());
        if (StringTools.length(message) > maxLen) {
            Print.logWarn("Invalid SMS text length: " + StringTools.length(message));
            message = SMSOutboundGateway.TruncateMessageToLength(message, maxLen);
        }

        /* receipient */
        String recipient = smsPhone;

        /* Add outbound SMS message delivery here */
        // -- TODO: send "message" to "recipient"
        Print.logWarn("Custom 'sendSMSMessage' not yet implemented");
        Print.logWarn("Recipient: " + recipient);
        Print.logWarn("Message  : " + message);
        return DCServerFactory.ResultCode.NOT_SUPPORTED;
        //return DCServerFactory.ResultCode.SUCCESS;

    }

    // ------------------------------------------------------------------------

}
