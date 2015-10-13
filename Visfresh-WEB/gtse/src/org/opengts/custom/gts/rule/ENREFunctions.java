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
// Refer to "Event Notification Rules Engine Technical Manual" for more info:
//   http://www.geotelematic.com/docs/RulesEngine.pdf
// Property required to install rule notification listener callback:
//   rule.ruleListenerClass=org.opengts.custom.gts.rule.ENREFunctions$ENRERuleListener
// Property required to install custom Rule functions/identifiers:
//   ENREInitialize.class=org.opengts.custom.gts.rule.ENREFunctions
// ----------------------------------------------------------------------------
// Change History:
//  2011/01/28  Martin D. Flynn
//     -Initial release
//  2011/03/08  Martin D. Flynn
//     -Added Canned-Rule-Actions support
//  2013/09/08  Martin D. Flynn
//     -Move RuleListener to ENRERuleListener
//  2015/08/16  Martin D. Flynn
//     -Added additional example code for sending HTTP GET requests.
// ----------------------------------------------------------------------------
package org.opengts.custom.gts.rule;

import java.util.*;
import java.io.*;
import java.net.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.rule.selector.FunctionMap;
import org.opengts.rule.selector.IdentifierMap;
import org.opengts.rule.event.EventFunctionHandler;
import org.opengts.rule.event.EventIdentifierHandler;
import org.opengts.rule.EventRuleFactory;

import org.opengts.rule.tables.Rule;
import org.opengts.rule.RuleListener;
import org.opengts.rule.EventRuleAction;

/**
*** This class can be modified to install custom Rule functions to be used
*** within the Event Notification Rules Engine.
**/

public class ENREFunctions
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* runtime configuration properties */
    private static final String PROP_ENREListener_getRequestURL             = "ENRERuleListener.getRequestURL";
    private static final String PROP_ENREListener_getRequestURL_            = "ENRERuleListener.getRequestURL.";
    private static final String PROP_ENREListener_getRequestURL_timeoutMS   = "ENRERuleListener.getRequestURL.timeoutMS";
    private static final String PROP_ENREListener_getRequestURL_retryCount  = "ENRERuleListener.getRequestURL.retryCount";

    // --

    private static final String CRANAME_HTTP_GET = 
        // -- use default key name
        "%http.get";

    // ------------------------------------------------------------------------

    /**
    *** Display ENRERuleLister http GET info message
    **/
    private static void LogRuleListener_info(String craKey, String acctID, String devID, String url, String msg)
    {
        Print.logInfo("ENRERuleListener("+craKey+") "+acctID+"/"+devID+": ["+url+"] "+msg);
    }

    /**
    *** Display ENRERuleLister http GET warning message
    **/
    private static void LogRuleListener_warn(String craKey, String acctID, String devID, String url, String msg)
    {
        Print.logWarn("ENRERuleListener("+craKey+") "+acctID+"/"+devID+": ["+url+"] "+msg);
    }

    /**
    *** Display ENRERuleLister http GET error message
    **/
    private static void LogRuleListener_error(String craKey, String acctID, String devID, String url, String msg)
    {
        Print.logError("ENRERuleListener("+craKey+") "+acctID+"/"+devID+": ["+url+"] "+msg);
    }

    // ------------------------------------------------------------------------

    /**
    *** Construct a URL which can be used by "ENRERuleListener"  to forward rule
    *** trigger information to an external URL.
    **/
    private static String GetCustomURL(
        Account account, Device device, EventData event, Rule rule,
        String craKey, String craArg[]) 
    {
        String       accountID  = (account != null)? account.getAccountID() : "";
        String       acctDesc   = (account != null)? account.getDescription() : "";
        String       deviceID   = (device  != null)? device.getDeviceID() : "";
        String       devDesc    = (device  != null)? device.getDescription() : "";
        long         evTimeSec  = (event   != null)? event.getTimestamp() : 0L;
        int          statusCode = (event   != null)? event.getStatusCode() : StatusCodes.STATUS_NONE;
        RTProperties craArgRTP  = new RTProperties(craArg);

        /* get base URL */
        String baseURL = RTConfig.getString(PROP_ENREListener_getRequestURL,null);
        if (StringTools.isBlank(baseURL)) {
            // -- no base URL
            //LogRuleListener_warn(craKey,accountID,deviceID,"?","URL not defined (ignored) ...");
            return null;
        }
        URIArg uriArg = new URIArg(baseURL,true/*uniqueArgs*/);
        //uriArg.setNonEncodedCharacters(URIArg.DFT_NonEncodedChars+"\""); // "A_-.\"" - do not encode quotes

        /* assemble URL here */
        // -- assemble URL here using data from Account/Device/EventData/Rule

        /* return as URL String */
        return uriArg.toString();

    }

    /**
    *** Send an HTTP request to an external URL
    **/
    private static byte[] GetHttpRequest(
        String urlStr, int timeoutMS, 
        Account account, Device device, EventData event, Rule rule,
        String craKey, String craArg[]) 
    {
        String accountID = (account != null)? account.getAccountID() : "";
        String deviceID  = (device  != null)? device.getDeviceID()   : "";
        LogRuleListener_info(craKey,accountID,deviceID,urlStr,"GET ...");

        /* convert URL */
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException mue) {
            LogRuleListener_warn(craKey,accountID,deviceID,urlStr,"Invalid URL");
            return null;
        }

        /* read page */
        byte resp[];
        try {
            resp = HTMLTools.readPage_GET(url, timeoutMS);
            LogRuleListener_info(craKey,accountID,deviceID,urlStr,"Success");
        } catch (Throwable th) {
            Throwable cause = OSTools.getExceptionRootCause(th); // root cause
            LogRuleListener_warn(craKey,accountID,deviceID,urlStr,"GET Failed - "+cause);
            Print.logException("ENRERuleListener GET Failed", cause);
            return null;
        }

        /* handle returned response? */
        String respS = StringTools.toStringValue(resp,'.');
        LogRuleListener_info(craKey,accountID,deviceID,urlStr,"Response - "+respS);
        return resp; // non-null

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* background thread handler for cannedRuleActions */
    // -- This is used by the custom "EventRuleAction.addRuleListener(...)" defined below
    private static ThreadPool ENREThreadPool = new ThreadPool("ENREFunctions", 5);

    /* name of ENRERuleListener */
    public static final String ENRERuleListener_Name = "ENRERuleListener";

    public static class ENRERuleListener
        implements RuleListener
    {

        /**
        *** Constructor
        **/
        public ENRERuleListener() {
            super();
            Print.logDebug("RuleListener installed: " + StringTools.className(this));
        }

        /**
        *** Gets the name od this RuleListener
        *** @return The name od this RuleListener
        **/
        public String getName() {
            return ENRERuleListener_Name;
        }

        /**
        *** Gets the String representation od this RuleListener
        *** @return The String representation of this RuleListener
        **/
        public String toString() {
            return this.getName();
        }

        /**
        *** Callback to handle a rule notification trigger
        *** @param account    The account which triggered the rule
        *** @param device     The device which triggered the rule (may be null)
        *** @param event      The event which triggered the rule (may be null)
        *** @param isCronMode True is exectued fron the 'Cron' task, 
        ***                   False is executed from a normal incoming event analysis.
        *** @param selector   The rule selector that triggered this notification
        *** @param rule       The rule record from which the selector was obtained. May
        ***                   be null if the selector was not provided by a Rule record.
        **/
        public void handleRuleNotification(
            final Account account, final Device device, final EventData event, 
            final boolean isCronMode, final String selector, final Rule rule) {
            final String accountID = (account != null)? account.getAccountID()  : "";
            final String deviceID  = (device  != null)? device.getDeviceID()    : "";
            final String ruleID    = (rule    != null)? rule.getRuleID()        : "";
            final String cannedAct = (rule    != null)? rule.getCannedActions() : "";
            Print.logDebug("ENRERuleListener for Device: " + accountID + "/" + deviceID);
            if (!StringTools.isBlank(cannedAct)) {
                String caList[] = StringTools.split(cannedAct,',');
                for (String craItem : caList) {
                    final String cra[] = StringTools.split(craItem,':');
                    if (ListTools.size(cra) <= 0) {
                        continue;
                    }
                    final String craKey   = cra[0];
                    final String craArg[] = ListTools.toArray(cra,1,-1); // cra[1+]

                    /* skip blank commands */
                    if (StringTools.isBlank(craKey)) {
                        // -- quietly ignore actions with a blank key
                        //Print.logWarn("CRA: Blank command: " + craItem);
                        continue; // blank action
                    }

                    /* skip custom craKeys prefixed with special characters */
                    char firstChar = craKey.charAt(0);
                    if (firstChar == '#') {
                        // -- quietly ignore commented keys
                        continue;
                    } else
                    if (Character.isLetter(firstChar)) {
                        // -- quietly ignore non-custom craKeys
                        continue;
                    }
                    // -- only %/@ prefixed craKeys should remain

                    // -- Example custom rule predefined 'cannedAction' handler
                    if (craKey.equalsIgnoreCase(CRANAME_HTTP_GET)) {
                        // -- we need a ThreadPool to continue
                        if (ENREThreadPool == null) {
                            LogRuleListener_error(craKey,accountID,deviceID,"","'ENREThreadPool' is null");
                            continue;
                        }
                        // -- get URL String
                        final String urlStr = GetCustomURL(account,device,event,rule,craKey,craArg);
                        if (StringTools.isBlank(urlStr)) {
                            // -- no URL string defined
                            continue;
                        }
                        // -- send http request
                        if (ENREThreadPool == null) {
                            // -- Included only for demonstration purposes.
                            // -  (this code will not be executed, since ENREThreadPool was precheded above)
                            // -- HTTP request executed in the same thread.
                            // -  Not recommended: connection delays will block EventData record processing.
                            int tmoMS = RTConfig.getInt(PROP_ENREListener_getRequestURL_timeoutMS,1500); // short delay
                            byte resp[] = GetHttpRequest(urlStr,tmoMS,account,device,event,rule,craKey,craArg);
                        } else {
                            // -- HTTP request executed in a separate thread.
                            // -  Recommended: does not block the EventData record processing.
                            ENREThreadPool.run(new Runnable() {
                                public void run() {
                                    int tmoMS = RTConfig.getInt(PROP_ENREListener_getRequestURL_timeoutMS ,5000);
                                    int retry = RTConfig.getInt(PROP_ENREListener_getRequestURL_retryCount,   0);
                                    int count = 1 + ((retry > 0)? retry : 0);
                                    for (int r = 0; (r < count) && (r < 4); r++) { // maximum 4 attempts
                                        // -- display retry message, if applicable
                                        if (r > 0) {
                                            // -- this is a retry pass [retry #(r), attempt #(r+1)]
                                            LogRuleListener_error(craKey,accountID,deviceID,urlStr,"Connection failed, retry #"+r);
                                        }
                                        // -- connect to URL web service
                                        byte resp[] = GetHttpRequest(urlStr,tmoMS,account,device,event,rule,craKey,craArg);
                                        if (resp != null) {
                                            // -- successful connection to web-service
                                            // -  (TODO: check for returned successful response?)
                                            break;
                                        }
                                    }
                                }
                            });
                        }
                        continue;
                    }

                    /* command not found */
                    Print.logWarn("CRA: Custom command not found - " + craItem);

                } // CRA loop
            }
        } // handleRuleNotification

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public ENREFunctions()
    {

        /* get EventRuleFactory */
        RuleFactory ruleFact = Device.getRuleFactory();
        if (!(ruleFact instanceof EventRuleFactory)) {
            return;
        }
        EventRuleFactory erf = (EventRuleFactory)ruleFact;
        Print.logDebug("Installing custom rule functions: " + StringTools.className(this));

        // --------------------------------------------------------------------

        /* add functions */
        erf.addFunction(new EventFunctionHandler("$SUM") {
            public String   getUsage()        { return "Double $SUM(Double A, Double B [, Double C [, Double D]])"; }
            public String   getDescription()  { return "Sum arguments and return value."; }
            public Class<?> getReturnType()   { return Double.class; }
            public Object   getDefaultValue() { return new Double(0.0); }
            public ArgCk    getArgCheck()     { return new ArgCk(0,1,2,3,4,Double.class,Double.class,Double.class,Double.class); }
            public Object   invokeFunction(FunctionMap fm, Object args[]) {
                // -- Useful method calls: (should always check for null/blank returned values)
                // -   EventData   event     = GetEventData(fm);
                // -   String      accountID = GetAccountID(fm);
                // -   Account     account   = GetAccount(fm);
                // -   String      deviceID  = GetDeviceID(fm);
                // -   Device      device    = GetDevice(fm);
                if (!this.checkArgs(args)) {
                    return null; // invalid arguments
                } else {
                    double accum = 0.0;
                    for (int i = 0; i < args.length; i++) { accum += DoubleValue(args[i]); }
                    return new Double(accum);
                }
            }
        });

        // --------------------------------------------------------------------

        /* add identifiers */
        erf.addIdentifier(new EventIdentifierHandler("random") {
            public String getDescription() { return "Random number between 0.0 and 1.0"; }
            public Object getValue(IdentifierMap idm) {
                // -- Useful method calls:
                // -   EventData   event     = GetEventData(idm);
                // -   String      accountID = GetAccountID(idm);
                // -   Account     account   = GetAccount(idm);
                // -   String      deviceID  = GetDeviceID(idm);
                // -   Device      device    = GetDevice(idm);
                return new Double(Math.random());
            }
        });

        // --------------------------------------------------------------------

        /* Add rule trigger action callback listener */
        if (!EventRuleAction.hasRuleListener(ENRERuleListener_Name)) {
            // -- ENRERuleListener not already added
            Print.logDebug("Adding ENRERuleListener ...");
            EventRuleAction.addRuleListener(new ENRERuleListener());
        } else {
            //Print.logDebug("ENRERuleListener already added ...");
        }

        // --------------------------------------------------------------------

    }
    
}
