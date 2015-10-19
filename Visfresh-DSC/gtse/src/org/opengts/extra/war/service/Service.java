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
// ----------------------------------------------------------------------------
// Response:
// ---------
//
// Table Schema Response:
// <GTSResponse command="dbschema" result="success">
//    <TableSchema table="Device">
//       <Description><![CDATA[This table defines Device/Vehicle specific information for an Account.]]></Description>
//       <Field name="accountID" primaryKey="true" type="STRING[32]"/>
//       <Field name="deviceID" primaryKey="true" type="STRING[32]"/>
//       ...
//    </TableSchema>
// </GTSResponse>
//
// Report Response
// <GTSResponse command="report" result="success">
//    <Report name="EventDetail">
//       <Device>mydevice</Device>
//       <DeviceGroup>mydevice</DeviceGroup>
//       <TimeFrom timezone="GMT">2009/03/13,00:00:00</TimeFrom>
//       <TimeTo timezone="GMT">2009/03/13,23:59:59</TimeTo>
//       <Title>Report Title</Title>
//       <Subtitle>Subtitle</Subtitle>
//       <HeaderRow>
//          <HeaderColumn name="abc">title</Column>
//       </HeaderRow>
//       <BodyRow>
//          <BodyColumn name="abc">value</Column>
//       </BodyRow>
//    </Report>
// </GTSResponse>
//
// ----------------------------------------------------------------------------
//
// Example Error Message Response:
// <GTSResponse result="error">
//    <Message code="code"><![CDATA[Error Message]]></Message>
// </GTSResponse>
//
// Example Success Message Response:
// <GTSResponse result="success">
//    <Message code="code"><![CDATA[Success Message]]></Message>
// </GTSResponse>
//
// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
// Change History:
//  2009/07/01  Martin D. Flynn
//     -Initial release
//  2009/09/23  Clifton Flynn
//     -Began work to integrate and expose servlet as a SOAP web service wrapping
//      existing CMD_commands and xml into web service interface.
//     -Added code in doGet to look for soap messages
//     -Added code to set the Response Content Type to "text/xml;charset=utf-8"
//     -Added code in doPost to look for soap messages/re-arranged the first few paragraphs.
//     -Changed the call in dbget command to print xml
//       drk.printXML(pw, 0, 0, isSoapReq);  
//     -Changed the call in dbschema command to print xml with no indents soap encoded
//       f.toXML(sb,0,isSoapReq);
//     -Changed TAG_Record to TAG_RecordKey in the CMD_dbcreate, CMD_dbput
//     -Added isSoapReq mapProv.writeMapUpdate(pw, 1, reqState, isSoapReq)
//  2011/03/08  Martin D. Flynn
//     -Added User ACL check for command authorization
//     -Fixed bug in "Limit" parsing for "mapdata" and "eventdata" commands.
//  2011/12/06  Martin D. Flynn
//     -Added call to "report.postInitialize()" when generating emailed report
//     -Added "PARM_REQSTATE" attribute setting to set RequestProperties in the "request"
//     -Exclude "Driver" table when checking for authorized devices.
//  2013/08/27  Martin D. Flynn
//     -Added configuration support for disabling commands. (see PROP_track_service_command_)
//     -User command ACL check, use 'acctPrivateLabel', instead of 'privLabel'
//     -For 'admin' user command ACL, still check "acctPrivateLabel.hasAllAccess(...)"
//  2014/03/03  Martin D. Flynn
//     -Added Account "getAllowWebService" check.
//  2014/05/05  Martin D. Flynn
//     -Fixed NPE due to blank "rptFormat"
//  2014/06/29  Martin D. Flynn
//     -Fixed TAG_Limit specification within TAG_MapData section
//  2014/12/31  Martin D. Flynn
//     -Augmented PROP_track_service_commandEnabled_ to support overriding Account "allowWebService".
// ----------------------------------------------------------------------------
package org.opengts.extra.war.service;

import java.util.Locale;
import java.util.TimeZone;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.math.BigInteger;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.opengts.Version;
import org.opengts.util.*;
import org.opengts.dbtools.*;

import org.opengts.geocoder.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.report.*;

import org.opengts.extra.service.ServiceXML;
import org.opengts.extra.service.ServiceMessage;
import org.opengts.extra.tables.ReportJob;

public class Service
    extends CommonServlet
    implements ServiceXML
{

    private static boolean       DISPLAY_PARAMETERS         = false;

    // ------------------------------------------------------------------------

    // these must match their counterpart in ".../war/track/Constants.java"
    public  static final String  PARM_ACCOUNT               = "account";
    public  static final String  PARM_USER                  = "user";
    public  static final String  PARM_REQSTATE              = "$REQSTATE";

    // ------------------------------------------------------------------------

    /* list of commands */
    private static final String CommandList[] = new String[] {
        CMD_commands,       // list available commands
        CMD_dbdel,          // delete db entry
        CMD_dbget,          // get db entry
        CMD_dbput,          // put (save) db entry
        CMD_dbcreate,       // create db entry
        CMD_dbschema,       // get db schema (sysadmin only)
        CMD_mapdata,        // get map data
        CMD_eventdata,      // get EventData records
        CMD_messages,       // get list of messages
        CMD_propget,        // get runtime property (sysadmin only)
        CMD_pushpins,       // get list of pushpins
        CMD_reportlist,     // get list of available reports
        CMD_report,         // get report
        CMD_version,        // get current version
        CMD_devcmd,         // send device command
        CMD_statuscodes,    // get status codes
        CMD_custom,         // custom command
    };

    public static abstract class CommandHandler
    {
        private String name = null;
        public CommandHandler(String n) {
            this.name = n;
        }
        public String getName() { return this.name; }
        public abstract void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw)
            throws IOException;
    }

    // ------------------------------------------------------------------------

    public static class CustomCommandHandler
        extends CommandHandler
    {
        public CustomCommandHandler() {
            super(CMD_custom);
        }
        public void handleCommand(Element cuElem, RequestProperties reqState, PrintWriter pw) throws IOException {
            String       cmd        = XMLTools.getAttribute(cuElem, ATTR_name, this.getName(), false);
            String       cmdArg     = XMLTools.getAttribute(cuElem, ATTR_arg, null, false); // may be null
            Account      account    = reqState.getCurrentAccount();
            User         user       = reqState.getCurrentUser();
            PrivateLabel privLabel  = reqState.getPrivateLabel();
            Locale       locale     = reqState.getLocale();
            boolean      isSoapReq  = reqState.isSoapRequest();
            String       PFX1       = PREFIX(isSoapReq,3);
            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_CUSTOM_NOT_SUPPORTED,
                null);
        }
        public void writeResponse_success(RequestProperties reqState, PrintWriter pw) throws IOException {
            String  cmd       = this.getName();
            boolean isSoapReq = reqState.isSoapRequest();
            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_SUCCESSFUL,
                null);
        }
    }

    private static CustomCommand  _CustomCommand         = null;
    private static CommandHandler _CommandHandler_Custom = null;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static String startTAG(boolean isSoapReq, String tag, boolean endTag, boolean newLine)
    {
        return XMLTools.startTAG(isSoapReq, tag, "", endTag, newLine);
    }
    
    private static String startTAG(boolean isSoapReq, String tag, String attr, boolean endTag, boolean newLine)
    {
        return XMLTools.startTAG(isSoapReq, tag, attr, endTag, newLine);
    }

    private static String endTAG(boolean isSoapReq, String tag, boolean newLine)
    {
        return XMLTools.endTAG(isSoapReq, tag, newLine);
    }

    private static String ATTR(String key, String value)
    {
        return XMLTools.ATTR(key, value);
    }

    private static String ATTR(String key, int value)
    {
        return XMLTools.ATTR(key, value);
    }

    private static String CDATA(boolean isSoapReq, String content)
    {
        return XMLTools.CDATA(isSoapReq, content);
    }

    private static String PREFIX(boolean isSoapReq, int indent)
    {
        return XMLTools.PREFIX(isSoapReq, indent);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static PrintWriter writeResponse_begin( 
        boolean isSoapRequest, PrintWriter pw, 
        String cmd, boolean success)
        throws IOException
    {

        /* XML header */
        if (!isSoapRequest) {
	       	pw.write("<?xml version='1.0' encoding='UTF-8' standalone='no' ?>\n");
		} else {
	       	pw.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
			pw.write("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "); 
			pw.write("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
			pw.write("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
			pw.write("<soapenv:Body>");
			pw.write("<gtsServiceRequestReturn xmlns=\"http://service.war.extra.opengts.org\">");
			pw.write("&lt;?xml version=\"1.0\" encoding=\"utf-8\" ?&gt;");
		}

        /* Start GTSResponse tag */
        String result = success? "success" : "error";
        pw.write(startTAG(isSoapRequest, TAG_GTSResponse,
            ATTR(ATTR_command,cmd) +
            ATTR(ATTR_result,result),
            false,true));

        /* return PrintWriter */
        return pw;
    }

    private static void writeResponse_end(
        boolean isSoapReq, PrintWriter pw)
        throws IOException
    {
        pw.write(endTAG(isSoapReq,TAG_GTSResponse,true));
        if (isSoapReq) {
			pw.write("</gtsServiceRequestReturn>"); 
			pw.write("</soapenv:Body>"); 
			pw.write("</soapenv:Envelope>");
        }
        pw.close();
    }


    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static DateTime parseTimeNode(Element node, String dftTimeStr, boolean toTime, TimeZone acctTZ)
    {
        DateTime.DefaultParsedTime dftTime = toTime? 
            DateTime.DefaultParsedTime.ContextEnd  : 
            DateTime.DefaultParsedTime.ContextStart;
        if (node != null) {
            String tzStr = XMLTools.getAttribute(node, ATTR_timezone, null, false);
            TimeZone tz  = DateTime.getTimeZone(tzStr, acctTZ);
            String dtStr = StringTools.blankDefault(XMLTools.getNodeText(node,",",false),dftTimeStr);
            try {
                DateTime.ParsedDateTime pdt = DateTime.parseDateTime(dtStr, tz, dftTime);
                return pdt.createDateTime();
            } catch (DateTime.DateParseException dpe) {
                Print.logException("Time invalid: " + dtStr, dpe);
                return null;
            }
        } else
        if (!StringTools.isBlank(dftTimeStr)) {
            TimeZone tz  = acctTZ;
            String dtStr = dftTimeStr;
            try {
                DateTime.ParsedDateTime pdt = DateTime.parseDateTime(dtStr, tz, dftTime);
                return pdt.createDateTime();
            } catch (DateTime.DateParseException dpe) {
                Print.logException("Time invalid: " + dtStr, dpe);
                return null;
            }
        } else {
            return new DateTime(-1,acctTZ);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void writeMessageResponse(
        boolean isSoapReq, PrintWriter pw, 
        String cmd, 
        ServiceMessage msg, String comment)
        throws IOException
    {
        // <GTSResponse command="command" result="success|error">
        //    <Message code="code"><![CDATA[Error Message]]></Message>
        //    <Comment>![CDATA[Comment]]></Command>
        // </GTSResponse>
        String PFX1 = PREFIX(isSoapReq,3);
        // begin
        Service.writeResponse_begin(isSoapReq, pw, cmd, !msg.isError());
        // Message
        if (msg != null) {
            pw.write(PFX1);
            Service.writeMessage(isSoapReq, pw, msg);
        }
        // Comment
        if (!StringTools.isBlank(comment)) {
            pw.write(PFX1);
            Service.writeComment(isSoapReq, pw, comment);
        }
        // end
        Service.writeResponse_end(isSoapReq, pw);
    }

    private static void writeMessage(
        boolean isSoapReq, PrintWriter pw, 
        ServiceMessage msg)
        throws IOException
    {
        if (msg != null) {
            pw.write(startTAG(isSoapReq,TAG_Message,
                ATTR(ATTR_code,msg.getCode()),
                false,false));
            pw.write(CDATA(isSoapReq,msg.getMessage()));
            pw.write(endTAG(isSoapReq,TAG_Message,true));
        }
    }

    private static void writeComment(
        boolean isSoapReq, PrintWriter pw, 
        String comment)
        throws IOException
    {
        if (!StringTools.isBlank(comment)) {
            pw.write(startTAG(isSoapReq,TAG_Comment,false,false));
            pw.write(CDATA(isSoapReq,comment));
            pw.write(endTAG(isSoapReq,TAG_Comment,true));
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static Map<String,CommandHandler> CommandMap = new HashMap<String,CommandHandler>();
    private static void addCommand(CommandHandler ch)
    {
        CommandMap.put(ch.getName(),ch);
    }

    /* initialize commands */
    static {
        Print.logInfo("Initializing Service commands ...");

        // ------------------------------------------------
        // version
        addCommand(new CommandHandler(CMD_version) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                /* commands */
                Service.writeResponse_begin(isSoapReq, pw, cmd, true); // true);
                pw.write(PFX1);
                pw.write(startTAG(isSoapReq,TAG_Version,false,false));
                pw.write(CDATA(isSoapReq,Version.getVersion()));
                pw.write(endTAG(isSoapReq,TAG_Version,true));
                Service.writeResponse_end(isSoapReq, pw);
                return;

            }
        });

        // ------------------------------------------------
        // dbget
        addCommand(new CommandHandler(CMD_dbget) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // -- TAG_Where
                NodeList whNodeList = XMLTools.getChildElements(gtsRequest,TAG_Where);
                if (whNodeList.getLength() > 0) {
                    Element childElem = (Element)whNodeList.item(0);
                    String where = XMLTools.getNodeText(childElem, " ", false);
                    if (!StringTools.isBlank(where)) {
                        // not implemented (due to potention for rogue sql injection)
                    }
                }
            
                // -- TAG_RecordKey
                NodeList keyNodeList = XMLTools.getChildElements(gtsRequest,TAG_RecordKey);
                if (keyNodeList.getLength() > 0) {
                    Element childElem = (Element)keyNodeList.item(0);
                  //String  addWhere  = XMLTools.getAttribute(childElem,ATTR_where,null,false);
                    long    limit     = StringTools.parseLong(XMLTools.getAttribute(childElem,ATTR_limit,null,false),-1L);
                    boolean ascending = StringTools.parseBoolean(XMLTools.getAttribute(childElem,ATTR_ascending,null,false),true);
                    String  orderBy[] = StringTools.split(XMLTools.getAttribute(childElem,ATTR_orderBy,null,false),',');

                    // -- parse DBRecordKey
                    DBRecordKey rcdKey = parseDBRecordKey(isSoapReq, pw, cmd, childElem, authAcctID, authUserID);
                    if (rcdKey == null) {
                        // error response already sent
                        return;
                    }
                    DBFactory dbFact = rcdKey.getFactory();
    
                    // -- get list of matching record keys
                    java.util.List<DBRecordKey> keyList = new Vector<DBRecordKey>();
                    DBConnection dbc = null;
                    Statement     st = null;
                    ResultSet     rs = null;
                    try {
                        DBSelect dsel = new DBSelect(dbFact);
                        dsel.setSelectedFields(dbFact.getKeyNames());
                        dsel.setWhere(rcdKey.getWhereClause(DBWhere.KEY_PARTIAL_ALL_EMPTY)); // KEY_AUTO_INDEX?
                        // -- TODO: additional "where"
                        if (!ListTools.isEmpty(orderBy)) { dsel.setOrderByFields(orderBy); }
                        if (limit > 0L) { dsel.setLimit(limit); }
                        dsel.setOrderAscending(ascending);
                        dbc = DBConnection.getDefaultConnection();
                        st  = dbc.execute(dsel.toString());
                        rs  = st.getResultSet();
                        while (rs.next()) {
                            // -- TODO: filter through User allow device list
                            DBRecordKey fullKey = dbFact.createKey(rs);
                            if (_isAuthorizedDevice(fullKey, account, user)) {
                                keyList.add(fullKey);
                            }
                        }
                    } catch (SQLException sqe) {
                        Print.logException("DBRecordKey:", sqe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                            "RecordKey Error: " + sqe.getMessage());
                        return;
                    } catch (DBException dbe) {
                        Print.logException("DBRecordKey:", dbe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                            "RecordKey Error: " + dbe.getMessage());
                        return;
                    } finally {
                        if (rs != null) { try { rs.close(); } catch (Throwable t) {} }
                        if (st != null) { try { st.close(); } catch (Throwable t) {} }
                        DBConnection.release(dbc);
                    }
    
                    // -- return response
                    if (!ListTools.isEmpty(keyList)) {
                        Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                        for (DBRecordKey<?> drk : keyList) {
                            drk.printXML(pw, 4, -1, isSoapReq);
                        }
                        Service.writeResponse_end(isSoapReq, pw);
                    } else {
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_NOT_FOUND,
                            "No matching RecordKeys found");
                    }
                    return;
    
                }
            
                // -- TAG_Record
                NodeList rcdNodeList = XMLTools.getChildElements(gtsRequest,TAG_Record);
                if (rcdNodeList.getLength() > 0) {
                    Element childElem = (Element)rcdNodeList.item(0); // first element only
                  //String  addWhere  = XMLTools.getAttribute(childElem,ATTR_where  ,null,false);
                    long    limit     = StringTools.parseLong(XMLTools.getAttribute(childElem,ATTR_limit,null,false),-1L);
                    boolean ascending = StringTools.parseBoolean(XMLTools.getAttribute(childElem,ATTR_ascending,null,false),true);
                    String  orderBy[] = StringTools.split(XMLTools.getAttribute(childElem,ATTR_orderBy,null,false),',');

                    // -- parse DBRecordKey
                    DBRecordKey rcdKey = parseDBRecordKey(isSoapReq, pw, cmd, childElem, authAcctID, authUserID);
                    if (rcdKey == null) {
                        // -- error response already sent
                        return;
                    }
                    DBFactory dbFact = rcdKey.getFactory();
    
                    // -- get list of matching record keys
                    java.util.List<DBRecord> rcdList = new Vector<DBRecord>();
                    DBConnection dbc = null;
                    Statement     st = null;
                    ResultSet     rs = null;
                    try {
                        DBSelect dsel = new DBSelect(dbFact);
                        //dsel.setSelectedFields(dbFact.getFieldNames());
                        dsel.setWhere(rcdKey.getWhereClause(DBWhere.KEY_PARTIAL_ALL_EMPTY)); // KEY_AUTO_INDEX?
                        // -- TODO: additional "where"
                        if (!ListTools.isEmpty(orderBy)) { dsel.setOrderByFields(orderBy); }
                        if (limit > 0L) { dsel.setLimit(limit); }
                        dsel.setOrderAscending(ascending);
                        dbc = DBConnection.getDefaultConnection();
                        st  = dbc.execute(dsel.toString());
                        rs  = st.getResultSet();
                        while (rs.next()) {
                            DBRecord rcd = dbFact.createRecord(rs);
                            if (_isAuthorizedDevice(rcd.getRecordKey(), account, user)) {
                                rcdList.add(rcd);
                            }
                        }
                    } catch (SQLException sqe) {
                        Print.logException("DBRecord:", sqe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                            "Record Error: " + sqe.getMessage());
                        return;
                    } catch (DBException dbe) {
                        Print.logException("DBRecord:", dbe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                            "Record Error: " + dbe.getMessage());
                        return;
                    } finally {
                        if (rs != null) { try { rs.close(); } catch (Throwable t) {} }
                        if (st != null) { try { st.close(); } catch (Throwable t) {} }
                        DBConnection.release(dbc);
                    }

                    // -- return response
                    if (!ListTools.isEmpty(rcdList)) {
                        Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                        Set<String> selFields = rcdKey.getTaggedFieldNames();
                        for (DBRecord<?> dbr : rcdList) {
                            dbr.printXML(pw, 4, selFields);
                        }
                        Service.writeResponse_end(isSoapReq, pw);
                    } else {
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_NOT_FOUND,
                            "No matching Records found");
                    }
                    return;

                }

                /* invalid request */
                Print.logError("Missing 'RecordKey'/'Record' entry");
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                    "Missing Record/RecordKey entry");
    
            }
        });
        
        // ------------------------------------------------
        // dbput
        addCommand(new CommandHandler(CMD_dbput) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // TAG_Record
                NodeList rcdNodeList = XMLTools.getChildElements(gtsRequest,TAG_Record);
                if (rcdNodeList.getLength() <= 0) {
                    Print.logError("Missing 'Record' entry");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                        "Missing Record entry");
                    return;
                }
                Element childElem = (Element)rcdNodeList.item(0);

                // parse DBRecordKey
                DBRecordKey rcdKey = parseDBRecordKey(isSoapReq, pw, cmd, childElem, authAcctID, authUserID);
                if (rcdKey == null) {
                    // error response already sent
                    return;
                } else
                if (rcdKey.isPartialKey()) {
                    Print.logError("Partial DBRecordKey not allowed for '"+CMD_dbput+"'");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "Partial key not allowed");
                    return;
                }
                DBFactory dbFact = rcdKey.getFactory();

                // get list of matching records (only first record used)
                DBRecord<?> record = null;
                DBConnection dbc = null;
                Statement     st = null;
                ResultSet     rs = null;
                try {
                    DBSelect dsel = new DBSelect(dbFact);
                    //dsel.setSelectedFields(dbFact.getFieldNames());
                    dsel.setWhere(rcdKey.getWhereClause(DBWhere.KEY_FULL));
                    dbc = DBConnection.getDefaultConnection();
                    st  = dbc.execute(dsel.toString());
                    rs  = st.getResultSet();
                    while (rs.next()) {
                        DBRecord rcd = dbFact.createRecord(rs);
                        if (_isAuthorizedDevice(rcd.getRecordKey(), account, user)) {
                            record = rcd;
                            break; // only 1 record
                        }
                    }
                } catch (SQLException sqe) {
                    Print.logException("DBRecord:", sqe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "Record Error: " + sqe.getMessage());
                    return;
                } catch (DBException dbe) {
                    Print.logException("DBRecord:", dbe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "Record Error: " + dbe.getMessage());
                    return;
                } finally {
                    if (rs != null) { try { rs.close(); } catch (Throwable t) {} }
                    if (st != null) { try { st.close(); } catch (Throwable t) {} }
                    DBConnection.release(dbc);
                }
                
                // found DBRecord?
                if (record == null) { 
                    Print.logError("DBRecord["+rcdKey.getUntranslatedTableName()+"] not found: "+rcdKey);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_NOT_FOUND,
                        "Record key not found");
                    return;
                }
                
                // parse field values [TODO: optimize this is the second parsing of the fields]
                Map<String,String> valueMap = null;
                try {
                    valueMap = DBFactory.parseXML_FieldValueMap(childElem, dbFact);
                } catch (DBException dbe) { // will not occur (pre-tested above)
                    Print.logException("FieldValueMap:", dbe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "FieldValue Error: " + dbe.getMessage());
                }
                Set<String> changedFields = new HashSet<String>();
                for (String fn : valueMap.keySet()) {
                    DBField fld = dbFact.getField(fn);
                    if (fld == null) {
                        // should not be null, ignore for now
                        Print.logInfo("Field not found: " + fn);
                    } else
                    if (!fld.isPrimaryKey()) {
                        String val = valueMap.get(fn);
                        record.setFieldValue(fn, fld.parseStringValue(val));
                        changedFields.add(fn); // fields updated
                    }
                }
                if (!ListTools.isEmpty(changedFields)) {
                    String tn = rcdKey.getUntranslatedTableName();
                    try {
                        String cf = StringTools.join(changedFields,",");
                        Print.logInfo("Updating table: %s [%s]", tn, cf);
                        record.update(changedFields);
                    } catch (DBException dbe) {
                        Print.logError("DBRecord["+tn+"] update", dbe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_UPDATE_FAILED,
                            "Record Error: " + dbe.getMessage());
                    }
                }

                // return success response
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_SUCCESSFUL,
                    null);

            }
        });
        
        // ------------------------------------------------
        // dbcreate
        addCommand(new CommandHandler(CMD_dbcreate) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // TAG_Record
                NodeList rcdNodeList = XMLTools.getChildElements(gtsRequest,TAG_Record);
                if (rcdNodeList.getLength() <= 0) {
                    Print.logError("Missing 'Record' entry");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                        "Missing Record entry");
                    return;
                }
                Element childElem = (Element)rcdNodeList.item(0);

                // parse DBRecordKey
                DBRecordKey rcdKey = parseDBRecordKey(isSoapReq, pw, cmd, childElem, authAcctID, authUserID);
                if (rcdKey == null) {
                    // error response already sent
                    return;
                } else
                if (rcdKey.isPartialKey()) {
                    Print.logError("Partial DBRecordKey not allowed for '"+CMD_dbput+"'");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "Partial key not allowed");
                    return;
                }
                DBFactory dbFact = rcdKey.getFactory();

                // get list of matching records (only first record used)
                DBRecord<?> record = null;
                DBConnection dbc = null;
                Statement     st = null;
                ResultSet     rs = null;
                try {
                    DBSelect dsel = new DBSelect(dbFact);
                    //dsel.setSelectedFields(dbFact.getFieldNames());
                    dsel.setWhere(rcdKey.getWhereClause(DBWhere.KEY_FULL));
                    dbc = DBConnection.getDefaultConnection();
                    st  = dbc.execute(dsel.toString());
                    rs  = st.getResultSet();
                    while (rs.next()) {
                        DBRecord rcd = dbFact.createRecord(rs);
                        if (_isAuthorizedDevice(rcd.getRecordKey(), account, user)) {
                            record = rcd;
                            break; // only 1 record
                        }
                    }
                } catch (SQLException sqe) {
                    Print.logException("DBRecord:", sqe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "Record Error: " + sqe.getMessage());
                    return;
                } catch (DBException dbe) {
                    Print.logException("DBRecord:", dbe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "Record Error: " + dbe.getMessage());
                    return;
                } finally {
                    if (rs != null) { try { rs.close(); } catch (Throwable t) {} }
                    if (st != null) { try { st.close(); } catch (Throwable t) {} }
                    DBConnection.release(dbc);
                }
                
                // found DBRecord?
                if (record != null) { 
                    Print.logError("DBRecord["+rcdKey.getUntranslatedTableName()+"] exists: "+rcdKey);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ALREADY_EXISTS,
                        "Record key already exists");
                    return;
                }

                // check parent record existence
                try {
                    if (!rcdKey.parentsExist()) {
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_INSERT_FAILED,
                            "Parent records do not exists");
                        return;
                    }
                } catch (DBException dbe) {
                    Print.logException("Parent existence:", dbe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "Parent existence check: " + dbe.getMessage());
                }

                // create record 
                record = rcdKey.getDBRecord();
                
                // parse field values [TODO: optimize, this is the second parsing of the fields]
                Map<String,String> valueMap = null;
                try {
                    valueMap = DBFactory.parseXML_FieldValueMap(childElem, dbFact);
                } catch (DBException dbe) { // will not occur (pre-tested above)
                    Print.logException("FieldValueMap:", dbe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "FieldValue Error: " + dbe.getMessage());
                }
                Set<String> insertedFields = new HashSet<String>();
                for (String fn : valueMap.keySet()) {
                    DBField fld = dbFact.getField(fn);
                    if (fld == null) {
                        // should not be null, ignore for now
                        Print.logInfo("Field not found: " + fn);
                    } else
                    if (!fld.isPrimaryKey()) {
                        String val = valueMap.get(fn);
                        record.setFieldValue(fn, fld.parseStringValue(val));
                    }
                    insertedFields.add(fn);
                }
                if (!ListTools.isEmpty(insertedFields)) {
                    String tn = rcdKey.getUntranslatedTableName();
                    try {
                        String cf = StringTools.join(insertedFields,",");
                        Print.logInfo("Inserting table: %s [%s]", tn, cf);
                        record.insert();
                    } catch (DBException dbe) {
                        Print.logError("DBRecord["+tn+"] insert", dbe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_INSERT_FAILED,
                            "Record Error: " + dbe.getMessage());
                    }
                } else {
                    // no fields specified
                    Print.logError("DBRecord["+rcdKey.getUntranslatedTableName()+"] no fields: "+rcdKey);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_INSERT_FAILED,
                        "No fields specified");
                }

                // return success response
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_SUCCESSFUL,
                    null);

            }
        });

        // ------------------------------------------------
        // dbdel
        addCommand(new CommandHandler(CMD_dbdel) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // TAG_RecordKey
                NodeList keyNodeList = XMLTools.getChildElements(gtsRequest,TAG_RecordKey);
                if (keyNodeList.getLength() <= 0) {
                    Print.logError("Missing 'RecordKey' entry");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                        "Missing RecordKey entry");
                    return;
                }
                Element childElem = (Element)keyNodeList.item(0);

                // parse DBRecordKey
                DBRecordKey rcdKey = parseDBRecordKey(isSoapReq, pw, cmd, childElem, authAcctID, authUserID);
                if (rcdKey == null) {
                    // error response already sent
                    return;
                } else
                if (rcdKey.isPartialKey()) {
                    Print.logError("Full RecordKey required for '"+CMD_dbdel+"'");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                        "Full RecordKey required");
                }
                DBFactory dbFact = rcdKey.getFactory();

                // user authorized for device
                try {
                    if (!_isAuthorizedDevice(rcdKey, account, user)) {
                        Print.logError("Account/User not authorized for this device");
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_NOT_FOUND,
                            "Not Authorized");
                    }
                } catch (DBException dbe) {
                    Print.logException("Testing user authorized device", dbe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_NOT_FOUND,
                        "User Error: " + dbe.getMessage());
                }

                // exists?
                try {
                    if (!rcdKey.exists()) {
                        Print.logError("Record does not exist");
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_NOT_FOUND,
                            "RecordKey does not exist");
                    }
                } catch (DBException dbe) {
                    Print.logException("DBRecordKey Delete(exists):", dbe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DELETE_FAILED,
                        "Record Error: " + dbe.getMessage());
                    return;
                }

                // delete 
                try {
                    rcdKey.delete(true);
                } catch (DBException dbe) {
                    Print.logException("DBRecordKey Delete:", dbe);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DELETE_FAILED,
                        "Record Error: " + dbe.getMessage());
                    return;
                }

                // return response
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_SUCCESSFUL,
                    null);

            }
        });
        
        // ------------------------------------------------
        // propget
        addCommand(new CommandHandler(CMD_propget) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // is SysAdmin?
                if (!account.isSystemAdmin()) {
                    Print.logError("Must be SysAdmin");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_COMMAND,
                        "Not Authorized");
                    return;
                }

                // TAG_Property
                Map<String,String> propMap = null;
                NodeList propNodeList = XMLTools.getChildElements(gtsRequest,TAG_Property);
                for (int i = 0; i < propNodeList.getLength(); i++) {
                    Element propElem = (Element)propNodeList.item(i);
                    String key = XMLTools.getAttribute(propElem,ATTR_key,null,false);
                    if (!StringTools.isBlank(key)) {
                        if (propMap == null) { propMap = new OrderedMap<String,String>(); }
                        if (RTConfig.hasProperty(key)) {
                            propMap.put(key, RTConfig.getString(key,""));
                        } else {
                            propMap.put(key, null);
                        }
                    } else {
                        Print.logError("Invalid property key");
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_PROP_BAD_KEY,
                            null);
                        return;
                    }
                }

                // return response
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                if (!ListTools.isEmpty(propMap)) {
                    for (String k : propMap.keySet()) {
                        String v = propMap.get(k);
                        pw.write(PREFIX(isSoapReq,4));
                        pw.write(startTAG(isSoapReq,TAG_Property,
                            ATTR(ATTR_key,k),
                            false,false));
                        pw.write(CDATA(isSoapReq,v));
                        pw.write(endTAG(isSoapReq,TAG_Property,true));
                    }
                }
                Service.writeResponse_end(isSoapReq, pw);

            }
        });
        
        // ------------------------------------------------
        // dbschema
        addCommand(new CommandHandler(CMD_dbschema) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // is SysAdmin?
                //if (!account.isSystemAdmin()) {
                //    Print.logError("Must be SysAdmin");
                //    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_COMMAND,
                //        "Not Authorized");
                //    return;
                //}

                // TAG_TableSchema
                Set<DBFactory> factSet = null;
                NodeList dbNodeList = XMLTools.getChildElements(gtsRequest,TAG_TableSchema);
                for (int i = 0; i < dbNodeList.getLength(); i++) {
                    Element dbElem = (Element)dbNodeList.item(i);
                    String  table  = XMLTools.getAttribute(dbElem,ATTR_table,null,false);
                    if (factSet == null) { factSet = new OrderedSet<DBFactory>(); }
                    if (StringTools.isBlank(table) || table.equalsIgnoreCase("*all")) {
                        OrderedMap<String,DBFactory<? extends DBRecord>> factMap = DBAdmin.getTableFactoryMap();
                        for (Iterator<String> tni = factMap.keyIterator(); tni.hasNext();) {
                            String tn = tni.next();
                            DBFactory f = factMap.get(tn);
                            factSet.add(f);
                        }
                    } else {
                        DBFactory f = DBFactory.getFactoryByName(table);
                        if (f == null) {
                            Print.logError("Invalid table: %s", table);
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_TABLE,
                                "Table not found");
                            return;
                        }
                        factSet.add(f);
                    }
                }

                // return response
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                if (!ListTools.isEmpty(factSet)) {
                    for (DBFactory f : factSet) {
                        StringBuffer sb = new StringBuffer();
                        f.toXML(sb, 4, isSoapReq);
                        pw.write(sb.toString());
                    }
                }
                Service.writeResponse_end(isSoapReq, pw);

            }
        });
        
        // ------------------------------------------------
        // messages
        addCommand(new CommandHandler(CMD_messages) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);
                String              PFX2       = PREFIX(isSoapReq, 2 * 3);

                /* messages */
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                pw.write(PFX1);
                pw.write(startTAG(isSoapReq,TAG_Messages,false,true));
                Map<String,ServiceMessage> msgMap = ServiceMessage.getMessageMap();
                for (String code : msgMap.keySet()) {
                    ServiceMessage msg = msgMap.get(code);
                    pw.write(PFX2);
                    Service.writeMessage(isSoapReq, pw, msg);
                }
                pw.write(PFX1);
                pw.write(endTAG(isSoapReq,TAG_Messages,true));
                Service.writeResponse_end(isSoapReq, pw);

            }
        });
        
        // ------------------------------------------------
        // commands
        addCommand(new CommandHandler(CMD_commands) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);
                String              PFX2       = PREFIX(isSoapReq, 2 * 3);

                /* commands */
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                pw.write(PFX1);
                pw.write(startTAG(isSoapReq,TAG_Commands,false,true));
                for (String c : CommandList) {
                    pw.write(PFX2);
                    pw.write(startTAG(isSoapReq,TAG_Command,false,false));
                    pw.write(c);
                    pw.write(endTAG(isSoapReq,TAG_Command,true));
                }
                pw.write(PFX1);
                pw.write(endTAG(isSoapReq,TAG_Commands,true));
                Service.writeResponse_end(isSoapReq, pw);
                return;

            }
        });

        // ------------------------------------------------
        // reportlist
        final boolean INCLUDE_REPORT_LIST_COLUMNS = false;
        addCommand(new CommandHandler(CMD_reportlist) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                PrivateLabel        privLabel  = reqState.getPrivateLabel();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);
                String              PFX2       = PREFIX(isSoapReq, 2 * 3);
                String              PFX3       = PREFIX(isSoapReq, 3 * 3);
                String              PFX4       = PREFIX(isSoapReq, 4 * 3);

                /* ReportFactory list */
                Collection<ReportFactory> reports = ReportFactory.getReportFactories();
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                pw.write(PFX1);
                pw.write(startTAG(isSoapReq,TAG_ReportList,false,true));
                for (ReportFactory rf : reports) {
                    pw.write(PFX2);
                    pw.write(startTAG(isSoapReq,TAG_Report,
                        ATTR(ATTR_name,rf.getReportName()) + 
                        ATTR(ATTR_type,rf.getReportType()),
                        false,true));
                    pw.write(PFX3);
                    pw.write(startTAG(isSoapReq,TAG_MenuDescription,false,false));
                    pw.write(_xmlFilter(isSoapReq,rf.getMenuDescription(locale)));
                    pw.write(endTAG(isSoapReq,TAG_MenuDescription,true));
                    if (INCLUDE_REPORT_LIST_COLUMNS) {
                        pw.write(PFX3);
                        pw.write(startTAG(isSoapReq,TAG_Title,false,false));
                        pw.write(_xmlFilter(isSoapReq,rf.getReportTitle(locale)));
                        pw.write(endTAG(isSoapReq,TAG_Title,true));
                        pw.write(PFX3);
                        pw.write(startTAG(isSoapReq,TAG_Subtitle,false,false));
                        pw.write(_xmlFilter(isSoapReq,rf.getReportSubtitle(locale)));
                        pw.write(endTAG(isSoapReq,TAG_Subtitle,true));
                        pw.write(PFX3);
                        pw.write(startTAG(isSoapReq,TAG_Columns,false,true));
                        for (ReportColumn rc : rf.getReportColumns()) { // ReportFactory
                            // if (!rc.isVisible(privLabel)) { continue; }
                            pw.write(PFX4);
                            pw.write(startTAG(isSoapReq,TAG_Column,
                                ATTR(ATTR_name,rc.getName()) +
                                ATTR(ATTR_arg,rc.getArg()),
                                true,true));
                        }
                        pw.write(PFX3);
                        pw.write(endTAG(isSoapReq,TAG_Columns,true));
                    }
                    pw.write(PFX2);
                    pw.write(endTAG(isSoapReq,TAG_Report,true));
                }
                pw.write(PFX1);
                pw.write(endTAG(isSoapReq,TAG_ReportList,true));
                Service.writeResponse_end(isSoapReq, pw);
                return;

            }
        });

        // ------------------------------------------------
        // report
        addCommand(new CommandHandler(CMD_report) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);
                Print.logInfo("Current Account: " + account);

                // TAG_EmailAddress
                Set<String> dftEmailAddrs = new HashSet<String>();
                NodeList dftEmailNodeList = XMLTools.getChildElements(gtsRequest, TAG_EmailAddress);
                for (int i = 0; i < dftEmailNodeList.getLength(); i++) {
                    Element emailNode = (Element)dftEmailNodeList.item(i);
                    String emailAddrStr = XMLTools.getNodeText(emailNode,",",false);
                    String ema[] = StringTools.split(emailAddrStr,',');
                    for (String a : ema) {
                        if (!StringTools.isBlank(a)) {
                            dftEmailAddrs.add(a);
                        }
                    }
                }

                /* report list */
                Vector<ReportData> reportList = new Vector<ReportData>();

                /* <Report ...> */
                NodeList rptNodeList = XMLTools.getChildElements(gtsRequest, TAG_Report);
                if (rptNodeList.getLength() > 0) {
                    for (int r = 0; r < rptNodeList.getLength(); r++) {
                        Element rptElem = (Element)rptNodeList.item(r);
                        //Print.logInfo("'Report' tag # " + r);
    
                        /* Report "jobID=" */
                        ReportJob rptJob = null;
                        String    rptJID = XMLTools.getAttribute(rptElem,ATTR_reportJobID,null,false);
                        if (!StringTools.isBlank(rptJID)) {
                            try {
                                rptJob = ReportJob.getReportJob(account, rptJID);
                                if (rptJob == null) {
                                    Print.logError("Specified ReportJob does not exist: " + rptJID);
                                } else
                                if (!rptJob.isActive()) {
                                    Print.logError("Specified ReportJob is not active: " + rptJID);
                                    rptJob = null;
                                }
                            } catch (DBException dbe) {
                                Print.logError("Unable to read ReportJob: " + rptJID);
                                rptJob = null;
                            }
                        }
                        String rjidName     = (rptJob != null)? rptJob.getReportName()     : null;
                        String rjidOption   = (rptJob != null)? rptJob.getReportOption()   : null;
                        String rjidFormat   = (rptJob != null)? rptJob.getReportFormat()   : null;
                        String rjidTimeFrom = (rptJob != null)? rptJob.getReportTimeFrom() : null;
                        String rjidTimeTo   = (rptJob != null)? rptJob.getReportTimeTo()   : null;
                        String rjidDeviceID = (rptJob != null)? rptJob.getDeviceID()       : null;
                        String rjidGroupID  = (rptJob != null)? rptJob.getGroupID()        : null;
                        String rjidRecip    = (rptJob != null)? rptJob.getRecipients()     : null;
    
                        /* Report "name=" */
                        String rptName = XMLTools.getAttribute(rptElem, ATTR_name, rjidName, false);
                        if (StringTools.isBlank(rptName)) {
                            Print.logError("Missing Report name");
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_NOT_FOUND,
                                "Missing Report name");
                            return;
                        } else
                        if (!StringTools.isBlank(rjidName) && !rjidName.equals(rptName)) {
                            Print.logError("Explicit Report name does not match ReportJob report name");
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_INVALID_NAME,
                                "Invalid Report name");
                            return;
                        }
    
                        /* Report "option=" */
                        String rptOption = XMLTools.getAttribute(rptElem,ATTR_option,rjidOption,false);
        
                        /* Report "format=" */
                        String dftFormat = isSoapReq? ReportURL.FORMAT_SOAPXML : ReportURL.FORMAT_XML;
                        String rptFormat = XMLTools.getAttribute(rptElem,ATTR_format,rjidFormat,false);
                        if (StringTools.isBlank(rptFormat)) {
                            // -- leave as default
                            rptFormat = dftFormat;
                        } else
                        if (rptFormat.equalsIgnoreCase(ReportURL.FORMAT_URL)) {
                            dftFormat = ReportURL.FORMAT_URL;
                            reqState.setEncodeEMailHTML(true);
                        } else
                        if (rptFormat.equalsIgnoreCase(ReportURL.FORMAT_EHTML) || 
                            rptFormat.equalsIgnoreCase(ReportURL.FORMAT_HTML )   ) {
                            dftFormat = ReportURL.FORMAT_EHTML;
                            reqState.setEncodeEMailHTML(true);
                        } else
                        if (rptFormat.equalsIgnoreCase(ReportURL.FORMAT_EMAIL)) {
                            dftFormat = ReportURL.FORMAT_EMAIL;
                            reqState.setEncodeEMailHTML(true);
                        } // else "xml"
                        Print.logInfo("Report: name="+rptName+ " option="+rptOption+ " format="+rptFormat);
    
                        /* report entry */
                        PrivateLabel privLabel = reqState.getPrivateLabel();
                        ReportEntry rptEntry = privLabel.getReportEntry(rptName);
                        ReportFactory rptFact = null;
                        if (rptEntry != null) {
                            rptFact = rptEntry.getReportFactory();
                        } else {
                            try {
                                rptFact = ReportFactory.getReportFactory(rptName, false);
                            } catch (ReportException re) {
                                Print.logException("Report name not found: " + rptName, re);
                                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_NOT_FOUND,
                                    "Report name not found");
                                return;
                            }
                            // -- create a temporary ReportEntry
                            rptEntry = new ReportEntry(rptFact, "");
                        }
    
                        /* report "<TimeFrom>" */
                        NodeList tfNodeList = XMLTools.getChildElements(rptElem, TAG_TimeFrom);
                        Element tfNode = (tfNodeList.getLength() > 0)? (Element)tfNodeList.item(0) : null;
                        DateTime dtTimeStart = Service.parseTimeNode(tfNode, rjidTimeFrom, false, acctTZ);
                        if (dtTimeStart == null) {
                            Print.logError("Report: name="+rptName+ " Invalid 'TimeFrom'");
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                                "Invalid 'TimeFrom'");
                            return;
                        }
                        reqState.setEventDateFrom(dtTimeStart);
    
                        /* report "<TimeTo>" */
                        NodeList ttNodeList = XMLTools.getChildElements(rptElem, TAG_TimeTo);
                        Element ttNode = (ttNodeList.getLength() > 0)? (Element)ttNodeList.item(0) : null;
                        DateTime dtTimeEnd = Service.parseTimeNode(ttNode, rjidTimeTo, true, acctTZ);
                        if (dtTimeEnd == null) {
                            Print.logError("Report: name="+rptName+ " Invalid 'TimeTo'");
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                                "Invalid 'TimeTo'");
                            return;
                        }
                        reqState.setEventDateTo(dtTimeEnd);
    
                        /* get ReportData instance */
                        ReportData report = null;
    
                        /* report "<Device>" */
                        if (report == null) {
                            NodeList dvNodeList = XMLTools.getChildElements(rptElem, TAG_Device);
                            // -- get DeviceID
                            String deviceID;
                            if (dvNodeList.getLength() > 0) {
                                Element dvNode = (Element)dvNodeList.item(0);
                                deviceID = StringTools.trim(XMLTools.getNodeText(dvNode," ",false));
                            } else {
                                deviceID = rjidDeviceID;
                            }
                            // -- get Device
                            if (!StringTools.isBlank(deviceID)) {
                                reqState.setFleet(false);
                                Device device = null;
                                try {
                                    if ((user != null) && !user.isAuthorizedDevice(deviceID)) {
                                        Print.logError("Report Device not authorized for user: " + deviceID);
                                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                                            "Not Authorized");
                                        return;
                                    }
                                    device = Device.getDevice(account, deviceID); // null if non-existent
                                } catch (DBException dbe) {
                                    Print.logException("Device read error: " + deviceID, dbe);
                                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                                        "Device Error: " + dbe.getMessage());
                                    return;
                                }
                                if (device == null) {
                                    Print.logError("Report Device does not exist: " + deviceID);
                                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                                        "Device not found");
                                    return;
                                }
                                reqState.setSelectedDeviceID(deviceID);
                                try {
                                    report = rptFact.createReport(rptEntry, rptOption, reqState, device); //
                                } catch (ReportException re) {
                                    Print.logException("Unable to create Device report: " + rptName, re);
                                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_CREATE,
                                        re.getMessage());
                                    return;
                                }
                            }
                        }
    
                        /* report "<DeviceGroup>" */
                        if (report == null) {
                            NodeList dgNodeList = XMLTools.getChildElements(rptElem, TAG_DeviceGroup);  // report
                            // -- get DeviceGroupID
                            String groupID;
                            if (dgNodeList.getLength() > 0) {
                                Element dgNode = (Element)dgNodeList.item(0);
                                groupID = StringTools.trim(XMLTools.getNodeText(dgNode," ",false));
                            } else {
                                groupID = rjidGroupID;
                            }
                            // -- get DeviceGroup
                            if (!StringTools.isBlank(groupID)) {
                                reqState.setFleet(true);
                                if (groupID.equalsIgnoreCase(DeviceGroup.DEVICE_GROUP_ALL)) {
                                    groupID = DeviceGroup.DEVICE_GROUP_ALL; // proper case
                                    reqState.setSelectedDeviceGroupID(groupID);
                                    ReportDeviceList rdl = new ReportDeviceList(account, user);
                                    rdl.addAllAuthorizedDevices();
                                    try {
                                        report = rptFact.createReport(rptEntry, rptOption, reqState, rdl); //
                                    } catch (ReportException re) {
                                        Print.logException("Unable to create Group report: " + rptName, re);
                                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_CREATE,
                                            re.getMessage());
                                        return;
                                    }
                                } else {
                                    reqState.setSelectedDeviceGroupID(groupID);
                                    DeviceGroup group = null;
                                    try {
                                        group = DeviceGroup.getDeviceGroup(account, groupID);
                                        if (group == null) {
                                            Print.logError("Report DeviceGroup does not exist: " + groupID);
                                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_GROUP_INVALID,
                                                "DeviceGroup not found");
                                            return;
                                        }
                                     } catch (DBException dbe) {
                                        Print.logException("Group read error: " + groupID, dbe);
                                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                                            "Group Error: " + dbe.getMessage());
                                        return;
                                    }
                                    try {
                                        report = rptFact.createReport(rptEntry, rptOption, reqState, group); //
                                    } catch (ReportException re) {
                                        Print.logException("Unable to create Group report: " + rptName, re);
                                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_CREATE,
                                            re.getMessage());
                                        return;
                                    }
                                }
                            }
                        }
    
                        /* no report? */
                        if (report == null) {
                            Print.logError("Report missing Device/DeviceGroup");
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_DEVICE,
                                "Missing Device/DeviceGroup");
                            return;
                        }

                        /* Report: <IncludeColumns> [EXPERIMENTAL] */
                        NodeList rfNodeList = XMLTools.getChildElements(rptElem, TAG_IncludeColumns);  // report
                        if (rfNodeList.getLength() > 0) {
                            Set<String> rptIncludeCols = null;
                            for (int rf = 0; rf < rfNodeList.getLength(); rf++) {
                                Element rfElem   = (Element)rfNodeList.item(rf);
                                String  rfNames  = StringTools.trim(XMLTools.getNodeText(rfElem,null,false));
                                String  rfName[] = StringTools.parseStringArray(rfNames, " \t\r\n");
                                for (String RFN : rfName) {
                                    if (!StringTools.isBlank(RFN)) {
                                        if (rptIncludeCols == null) { rptIncludeCols = new HashSet<String>(); }
                                        rptIncludeCols.add(RFN.trim());
                                    }
                                }
                            }
                            report.setIncludeColumnNames(rptIncludeCols);
                        }

                        /* save ReportJob in ReportData */
                        if (rptJob != null) {
                            report.setReportJob(rptJob);
                        }

                        /* create report constraints */
                        ReportConstraints rc = report.getReportConstraints();
                        rc.setTimeStart(dtTimeStart.getTimeSec());
                        rc.setTimeEnd(dtTimeEnd.getTimeSec());
                        rc.setTimeZone(acctTZ);

                        /* Report: <EmailAddress> */
                        if (dftFormat.equals(ReportURL.FORMAT_EMAIL)) {
                            HashSet<String> emailAddrs = new HashSet<String>(dftEmailAddrs);
                            NodeList emailNodeList = XMLTools.getChildElements(rptElem, TAG_EmailAddress);
                            if (emailNodeList.getLength() > 0) {
                                for (int i = 0; i < emailNodeList.getLength(); i++) {
                                    Element emailNode = (Element)emailNodeList.item(i);
                                    String emailAddrStr = XMLTools.getNodeText(emailNode,",",false);
                                    String ema[] = StringTools.split(emailAddrStr,',');
                                    for (String a : ema) {
                                        if (!StringTools.isBlank(a)) {
                                            emailAddrs.add(a);
                                        }
                                    }
                                }
                            } else
                            if (!StringTools.isBlank(rjidRecip)) {
                                String ema[] = StringTools.split(rjidRecip,',');
                                for (String a : ema) {
                                    if (!StringTools.isBlank(a)) {
                                        emailAddrs.add(a);
                                    }
                                }
                            }
                            rc.setEmailAddresses(StringTools.join(emailAddrs,","));
                        }

                        /* save report */
                        report.setPreferredFormat(dftFormat); // see ReportURL.FORMAT_...
                        report.postInitialize(); // Fixed NPE on GeozoneReport
                        reportList.add(report);

                    } // list of "Report" tags

                } // <Report ...> loop

                /* <ReportJob ...> (only the first entry is processed) */
                NodeList rptJobNodeList = XMLTools.getChildElements(gtsRequest, TAG_ReportJob);
                if (rptJobNodeList.getLength() > 0) {
                    // -- get list of report job names
                    Collection<String> rptJobList;
                    Element rptJobElem = null;
                    try {
                        rptJobElem = (Element)rptJobNodeList.item(0); // first element only
                        String rptJobTag = rptJobElem.getAttribute(ATTR_reportGroupTag);
                        rptJobList = ReportJob.getReportJobIDsForAccount(authAcctID, rptJobTag, true);
                        if (ListTools.isEmpty(rptJobList)) {
                            // -- fatal (bail now)
                            Print.logWarn("No active '"+authAcctID+"' ReportJobs with tag: "+rptJobTag);
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_NOT_FOUND,
                                "No active '"+authAcctID+"' ReportJobs with tag: "+rptJobTag);
                            return;
                        }
                    } catch (DBException dbe) {
                        // -- fatal (bail now)
                        Print.logError("Unable to read ReportJob list: " + dbe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_NOT_FOUND,
                            "Error reading ReportJob list");
                        return;
                    }
                    // -- iterate through list of report job names
                    ServiceMessage rptJobErr    = null;
                    String         rptJobErrMsg = null;
                    reportJobList:
                    for (String rjid : rptJobList) {

                        /* get ReportJob */
                        ReportJob rptJob = null;
                        try {
                            rptJob = ReportJob.getReportJob(account, rjid);
                            if (rptJob == null)          { continue; }  // not found
                            if (!rptJob.isActive())      { continue; }  // not active
                            if (!rptJob.hasReportName()) { continue; }  // no specified report
                        } catch (DBException dbe) {
                            continue; // skip this entry
                        }

                        /* ReportJob vars */
                        String rjidName     = rptJob.getReportName();
                        String rjidOption   = rptJob.getReportOption();
                        String rjidFormat   = rptJob.getReportFormat();
                        String rjidTimeFrom = rptJob.getReportTimeFrom();
                        String rjidTimeTo   = rptJob.getReportTimeTo();
                        String rjidDeviceID = rptJob.getDeviceID();
                        String rjidGroupID  = rptJob.getGroupID();
                        String rjidRecip    = rptJob.getRecipients();

                        /* check format */
                        String dftFormat = isSoapReq? ReportURL.FORMAT_SOAPXML : ReportURL.FORMAT_XML;
                        if (StringTools.isBlank(rjidFormat)) {
                            // -- leave as default
                            rjidFormat = dftFormat;
                        } else
                        if (rjidFormat.equalsIgnoreCase(ReportURL.FORMAT_URL)) {
                            dftFormat = ReportURL.FORMAT_URL;
                            reqState.setEncodeEMailHTML(true);
                        } else
                        if (rjidFormat.equalsIgnoreCase(ReportURL.FORMAT_EHTML) || 
                            rjidFormat.equalsIgnoreCase(ReportURL.FORMAT_HTML )) {
                            dftFormat = ReportURL.FORMAT_EHTML;
                            reqState.setEncodeEMailHTML(true);
                        } else
                        if (rjidFormat.equalsIgnoreCase(ReportURL.FORMAT_EMAIL)) {
                            dftFormat = ReportURL.FORMAT_EMAIL;
                            reqState.setEncodeEMailHTML(true);
                        } // else "xml"
                        Print.logInfo("ReportJob: name="+rjidName+ " option="+rjidOption+ " format="+rjidFormat);

                        /* report entry */
                        PrivateLabel privLabel = reqState.getPrivateLabel();
                        ReportEntry rptEntry = privLabel.getReportEntry(rjidName);
                        ReportFactory rptFact = null;
                        if (rptEntry != null) {
                            rptFact = rptEntry.getReportFactory();
                        } else {
                            try {
                                rptFact = ReportFactory.getReportFactory(rjidName, false);
                            } catch (ReportException re) {
                                // -- fatal (bail now)
                                Print.logException("ReportJob name not found: " + rjidName, re);
                                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_NOT_FOUND,
                                    "ReportJob name not found: " + rjidName);
                                return;
                            }
                            // -- create a temporary ReportEntry
                            rptEntry = new ReportEntry(rptFact, "");
                        }

                        /* report "TimeFrom=" */
                        DateTime dtTimeStart = Service.parseTimeNode(null, rjidTimeFrom, false, acctTZ);
                        if (dtTimeStart == null) {
                            // -- non-fatal
                            if (rptJobErr == null) {
                                rptJobErr    = ServiceMessage.MSG_DATETIME;
                                rptJobErrMsg = "[" + rjidName + "] ReportJob Invalid 'TimeFrom': " + rjidTimeFrom;
                            }
                            //Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                            //    rptJobErrMsg);
                            //return;
                            continue;
                        }
                        reqState.setEventDateFrom(dtTimeStart);

                        /* report "TimeTo=" */
                        DateTime dtTimeEnd = Service.parseTimeNode(null, rjidTimeTo, true, acctTZ);
                        if (dtTimeEnd == null) {
                            // -- non-fatal
                            if (rptJobErr == null) {
                                rptJobErr    = ServiceMessage.MSG_DATETIME;
                                rptJobErrMsg = "[" + rjidName + "] ReportJob Invalid 'TimeTo': " + rjidTimeTo;
                            }
                            //Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                            //    rptJobErrMsg);
                            //return;
                            continue;
                        }
                        reqState.setEventDateTo(dtTimeEnd);

                        /* get ReportData instance */
                        ReportData report = null;
                        boolean foundReport = false;

                        /* report "Device=" */
                        if (!foundReport && (report == null)) {
                            // -- get DeviceID
                            String deviceID = rjidDeviceID;
                            // -- get Device
                            if (!StringTools.isBlank(deviceID)) {
                                foundReport = true;
                                reqState.setFleet(false);
                                Device device = null;
                                try {
                                    if ((user != null) && !user.isAuthorizedDevice(deviceID)) {
                                        // non-fatal
                                        Print.logError("Report Device not authorized for user: " + deviceID);
                                        if (rptJobErr == null) {
                                            rptJobErr    = ServiceMessage.MSG_DEVICE_INVALID;
                                            rptJobErrMsg = "[" + rjidName + "] ReportJob Device Not Authorized: " + deviceID;
                                        }
                                        //Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                                        //    rptJobErrMsg);
                                        //return;
                                        continue;
                                    }
                                    device = Device.getDevice(account, deviceID); // null if non-existent
                                } catch (DBException dbe) {
                                    // Fatal
                                    Print.logException("Device read error: " + deviceID, dbe);
                                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                                        "ReportJob Device Error: " + dbe.getMessage());
                                    return;
                                }
                                if (device == null) {
                                    // non-fatal
                                    Print.logError("Report Device does not exist: " + deviceID);
                                    if (rptJobErr == null) {
                                        rptJobErr    = ServiceMessage.MSG_DEVICE_INVALID;
                                        rptJobErrMsg = "[" + rjidName + "] ReportJob Device not found: " + deviceID;
                                    }
                                    //Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                                    //    rptJobErrMsg);
                                    //return;
                                    continue;
                                }
                                reqState.setSelectedDeviceID(deviceID);
                                try {
                                    report = rptFact.createReport(rptEntry, rjidOption, reqState, device); // X
                                } catch (ReportException re) {
                                    // non-fatal
                                    Print.logException("ReportJob Unable to create Device report: " + rjidName, re);
                                    if (rptJobErr == null) {
                                        rptJobErr    = ServiceMessage.MSG_REPORT_CREATE;
                                        rptJobErrMsg = "[" + rjidName + "] " + re.getMessage();
                                    }
                                    //Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                                    //    rptJobErrMsg);
                                    //return;
                                    continue;
                                }
                            }
                        }

                        /* report "DeviceGroup=" */
                        if (!foundReport && (report == null)) {
                            // -- get DeviceGroupID
                            String groupID = rjidGroupID;
                            // -- get DeviceGroup
                            if (!StringTools.isBlank(groupID)) {
                                foundReport = true;
                                reqState.setFleet(true);
                                if (groupID.equalsIgnoreCase(DeviceGroup.DEVICE_GROUP_ALL)) {
                                    groupID = DeviceGroup.DEVICE_GROUP_ALL; // proper case
                                    reqState.setSelectedDeviceGroupID(groupID);
                                    ReportDeviceList rdl = new ReportDeviceList(account, user);
                                    rdl.addAllAuthorizedDevices();
                                    try {
                                        report = rptFact.createReport(rptEntry, rjidOption, reqState, rdl); // X
                                    } catch (ReportException re) {
                                        // -- non-fatal
                                        Print.logException("ReportJob Unable to create Group ["+groupID+"] report: " + rjidName, re);
                                        if (rptJobErr == null) {
                                            rptJobErr    = ServiceMessage.MSG_REPORT_CREATE;
                                            rptJobErrMsg = "[" + rjidName + "] " + re.getMessage();
                                        }
                                        //Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                                        //    rptJobErrMsg);
                                        //return;
                                        continue;
                                    }
                                } else {
                                    reqState.setSelectedDeviceGroupID(groupID);
                                    DeviceGroup group = null;
                                    try {
                                        group = DeviceGroup.getDeviceGroup(account, groupID);
                                        if (group == null) {
                                            // -- non-fatal
                                            Print.logError("ReportJob DeviceGroup does not exist: " + groupID);
                                            if (rptJobErr == null) {
                                                rptJobErr    = ServiceMessage.MSG_GROUP_INVALID;
                                                rptJobErrMsg = "[" + rjidName + "] " + "ReportJob DeviceGroup not found: " + groupID;
                                            }
                                            //Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                                            //    rptJobErrMsg);
                                            //return;
                                            continue;
                                        }
                                    } catch (DBException dbe) {
                                        // -- Fatal
                                        Print.logException("ReportJob Group read error: " + groupID, dbe);
                                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                                            "ReportJob Group Error: " + dbe.getMessage());
                                        return;
                                    }
                                    try {
                                        report = rptFact.createReport(rptEntry, rjidOption, reqState, group); // X
                                    } catch (ReportException re) {
                                        // -- on-fatal
                                        Print.logException("ReportJob Unable to create Group ["+groupID+"] report: " + rjidName, re);
                                        if (rptJobErr == null) {
                                            rptJobErr    = ServiceMessage.MSG_REPORT_CREATE;
                                            rptJobErrMsg = "[" + rjidName + "] " + re.getMessage();
                                        }
                                        //Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                                        //    rptJobErrMsg);
                                        //return;
                                        continue;
                                    }
                                }
                            }
                        }

                        /* no report? */
                        if (report == null) {
                            if (!foundReport) {
                                // -- Fatal
                                Print.logError("ReportJob missing Device/DeviceGroup");
                                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REPORT_DEVICE,
                                    "ReportJob Missing Device/DeviceGroup");
                                return;
                            } else {
                                // -- non-fatal
                                Print.logError("Unable to generate report: " + rjidName + " ...");
                                continue; // reportJobList:
                            }
                        }

                        /* Report: <IncludeColumns> [EXPERIMENTAL] */
                        NodeList rfNodeList = XMLTools.getChildElements(rptJobElem, TAG_IncludeColumns);  // report job
                        if (rfNodeList.getLength() > 0) {
                            Set<String> rptIncludeCols = null;
                            for (int rf = 0; rf < rfNodeList.getLength(); rf++) {
                                Element rfElem   = (Element)rfNodeList.item(rf);
                                String  rfNames  = StringTools.trim(XMLTools.getNodeText(rfElem,null,false));
                                String  rfName[] = StringTools.parseStringArray(rfNames, " \t\r\n");
                                for (String RFN : rfName) {
                                    if (!StringTools.isBlank(RFN)) {
                                        if (rptIncludeCols == null) { rptIncludeCols = new HashSet<String>(); }
                                        rptIncludeCols.add(RFN.trim());
                                    }
                                }
                            }
                            report.setIncludeColumnNames(rptIncludeCols);
                        }

                        /* save ReportJob in ReportData */
                        if (rptJob != null) {
                            report.setReportJob(rptJob);
                        }

                        /* create report constraints */
                        ReportConstraints rc = report.getReportConstraints();
                        rc.setTimeStart(dtTimeStart.getTimeSec());
                        rc.setTimeEnd(dtTimeEnd.getTimeSec());
                        rc.setTimeZone(acctTZ);

                        /* reportjob: EmailAddress */
                        if (dftFormat.equals(ReportURL.FORMAT_EMAIL)) {
                            HashSet<String> emailAddrs = new HashSet<String>(dftEmailAddrs);
                            if (!StringTools.isBlank(rjidRecip)) {
                                String ema[] = StringTools.split(rjidRecip,',');
                                for (String a : ema) {
                                    if (!StringTools.isBlank(a)) {
                                        emailAddrs.add(a);
                                    }
                                }
                            }
                            rc.setEmailAddresses(StringTools.join(emailAddrs,","));
                        }

                        /* save report */
                        report.setPreferredFormat(dftFormat); // see ReportURL.FORMAT_...
                        report.postInitialize(); // Fixed NPE on GeozoneReport
                        reportList.add(report);

                    } // list of ReportJobs

                    /* report any errors error encountered */
                    if (rptJobErr != null) {
                        Service.writeMessageResponse(isSoapReq, pw, cmd, rptJobErr,
                            StringTools.trim(rptJobErrMsg));
                        return;
                    }

                } // ReportJob tag

                /* no reports? */
                if (ListTools.isEmpty(reportList)) {
                    Print.logError("No 'Report'/'ReportJob' entries");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_NO_REPORT_SPECIFIED,
                        "No Report/ReportJob entry specified");
                    return;
                }

                /* output reports */
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                for (ReportData report : reportList) {
                    String rptName = report.getReportName();
                    String rptFmt  = report.getPreferredFormat();
                    Print.logInfo("Generating report: "+rptName+" - "+StringTools.blankDefault(rptFmt,"XML")+" ...");
                    try { // "url", "html", "email", "xml", ...
                        // -- generate report
                        if (rptFmt.equals(ReportURL.FORMAT_EMAIL)) {
                            // TODO: eventually conmbine these into a single email
                            OutputProvider op = new OutputProvider(pw);
                            report.writeReport(rptFmt, op, 1);
                        } else {
                            OutputProvider op = new OutputProvider(pw);
                            report.writeReport(rptFmt, op, 1);
                        }
                        // -- update last ReportJob time
                        try {
                            ReportJob rptJob = (ReportJob)report.getReportJob();
                            if (rptJob != null) {
                                rptJob.setLastReportTime(DateTime.getCurrentTimeSec());
                                rptJob.update(ReportJob.FLD_lastReportTime);
                            }
                        } catch (DBException dbe) {
                            // -- ignore
                        } catch (Throwable th) {
                            // -- ignore
                        }
                    } catch (ReportException re) {
                        String code = ServiceMessage.MSG_REPORT_UNEXPECTED.getCode();
                        String msg  = ServiceMessage.MSG_REPORT_UNEXPECTED.getMessage(re.toString());
                        pw.write(startTAG(isSoapReq,TAG_Message,
                            ATTR(ATTR_code,code),
                            false,false));
                        pw.write(CDATA(isSoapReq,msg));
                        pw.write(endTAG(isSoapReq,TAG_Message,true));
                    }
                }
                Service.writeResponse_end(isSoapReq, pw);

            }
        });

        // ------------------------------------------------
        // mapdata
        addCommand(new CommandHandler(CMD_mapdata) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                /* TAG_MapData */
                NodeList mapNodeList = XMLTools.getChildElements(gtsRequest,TAG_MapData);
                if (mapNodeList.getLength() <= 0) {
                    Print.logError("Missing 'MapData' entry");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                        "Missing 'MapData' entry");
                    return;
                }
                Element mapElem = (Element)mapNodeList.item(0);

                /* map TAG_TimeFrom */
                NodeList tfNodeList = XMLTools.getChildElements(mapElem, TAG_TimeFrom);
                Element tfNode = (tfNodeList.getLength() > 0)? (Element)tfNodeList.item(0) : null;
                DateTime dtTimeStart = Service.parseTimeNode(tfNode, null, false, acctTZ);
                if (dtTimeStart == null) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                        "Invalid 'TimeFrom'");
                    return;
                }
                reqState.setEventDateFrom(dtTimeStart);

                /* map TAG_TimeTo */
                NodeList ttNodeList = XMLTools.getChildElements(mapElem, TAG_TimeTo);
                Element ttNode = (ttNodeList.getLength() > 0)? (Element)ttNodeList.item(0) : null;
                DateTime dtTimeEnd = Service.parseTimeNode(ttNode, null, true, acctTZ);
                if (dtTimeEnd == null) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                        "Invalid 'TimeTo'");
                    return;
                }
                reqState.setEventDateTo(dtTimeEnd);

                /* status codes/markers */
                reqState.setStatusCodes(null);
                reqState.setStatusMarkers(null);

                /* limit */
                long limit = 1000L;
                EventData.LimitType limitType = EventData.LimitType.LAST;
                NodeList limitNodeList = XMLTools.getChildElements(mapElem,TAG_Limit); // fixed, was "gtsRequest": 2.5.6-B15
                if (limitNodeList.getLength() > 0) {
                    Element node = (Element)limitNodeList.item(0);
                    String  lt   = XMLTools.getAttribute(node,ATTR_type,"",false);
                    limitType = lt.equalsIgnoreCase("first")? EventData.LimitType.FIRST : EventData.LimitType.LAST;
                    limit     = StringTools.parseLong(XMLTools.getNodeText(node," ",false), limit);
                }
                reqState.setEventLimit(limit);
                reqState.setEventLimitType(limitType);

                /* Device/DeviceGroup */
                NodeList dvNodeList = XMLTools.getChildElements(mapElem,TAG_Device);
                if (dvNodeList.getLength() > 0) {
                    reqState.setFleet(false);
                    Element dvNode = (Element)dvNodeList.item(0);
                    String deviceID = StringTools.trim(XMLTools.getNodeText(dvNode," ",false));
                    Device device = null;
                    try {
                        if ((user != null) && !user.isAuthorizedDevice(deviceID)) {
                            Print.logError("Report Device not authorized for user: " + deviceID);
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                                "Not Authorized");
                            return;
                        }
                        device = Device.getDevice(account, deviceID); // null if non-existent
                    } catch (DBException dbe) {
                        Print.logException("Device read error: " + deviceID, dbe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                            "Device Error: " + dbe.getMessage());
                        return;
                    }
                    if (device == null) {
                        Print.logError("Report Device does not exist: " + deviceID);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                            "Device not found");
                        return;
                    }
                    reqState.setSelectedDeviceID(deviceID);
                    try {
                        EventData evd[] = device.getLatestEvents(1L,true);
                        if (!ListTools.isEmpty(evd)) {
                            reqState.setLastEventTime(new DateTime(evd[0].getTimestamp()));
                        }
                    } catch (DBException dbe) {
                        // ignore
                    }
                } else {
                    NodeList dgNodeList = XMLTools.getChildElements(mapElem,TAG_DeviceGroup); // mapdata
                    if (dgNodeList.getLength() > 0) {
                        reqState.setFleet(true);
                        Element dgNode = (Element)dgNodeList.item(0);
                        String groupID = StringTools.trim(XMLTools.getNodeText(dgNode," ",false));
                        DeviceGroup group = null;
                        if (!groupID.equalsIgnoreCase(DeviceGroup.DEVICE_GROUP_ALL)) {
                            try {
                                group = DeviceGroup.getDeviceGroup(account, groupID);
                            } catch (DBException dbe) {
                                Print.logException("Device read error: " + groupID, dbe);
                                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                                    "Group Error: " + dbe.getMessage());
                                return;
                            }
                            if (group == null) {
                                Print.logError("Report DeviceGroup does not exist: " + groupID);
                                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_GROUP_INVALID,
                                    "DeviceGroup not found");
                                return;
                            }
                        }
                        reqState.setSelectedDeviceGroupID(groupID);
                    }
                }

                /* map data */
                MapProvider mapProv = reqState.getMapProvider();
                if (mapProv == null) {
                    Print.logError("MapProvider not found");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_MAP_NOT_FOUND,
                        "MapProvider not found");
                    return;
                }

                /* response */
                EventUtil.SetDefaultMapDataFormat(EventUtil.MAPDATA_XML);
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                mapProv.writeMapUpdate(
                    pw, 1, 
                    EventUtil.MAPDATA_XML, false/*isTopLevelTag*/,  // XML only
                    reqState);
                Service.writeResponse_end(isSoapReq, pw);

            }
        });

        // ------------------------------------------------
        // eventdata
        addCommand(new CommandHandler(CMD_eventdata) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // TAG_EventData
                NodeList edNodeList = XMLTools.getChildElements(gtsRequest,TAG_EventData);
                if (edNodeList.getLength() <= 0) {
                    Print.logError("Missing 'EventData' entry");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                        "Missing 'EventData' entry");
                    return;
                } else
                if (edNodeList.getLength() != 1) {
                    Print.logError("More than 1 'EventData' entry specified");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                        "More than 1 'EventData' entry specified");
                    return;
                }
                Element edElem = (Element)edNodeList.item(0);

                /* Selected Field[s] */
                Set<String> selFields = null;
                NodeList fldNodeList = XMLTools.getChildElements(edElem,TAG_Field);
                for (int i = 0; i < fldNodeList.getLength(); i++) {
                    Element fldNode = (Element)fldNodeList.item(i);
                    String  name    = XMLTools.getAttribute(fldNode, ATTR_name, null, false);
                    if (!StringTools.isBlank(name)) {
                        if (selFields == null) {
                            selFields = new OrderedSet<String>();
                        }
                        selFields.add(name);
                        Print.logInfo("Added selected field: " + name);
                    }
                }

                /* auto-index */
                // returns a specific EventData record only
                NodeList autoNodeList = XMLTools.getChildElements(edElem,TAG_AutoIndex);
                if (autoNodeList.getLength() > 0) {

                    /* only one 'AutoIndex' allowed */
                    if (autoNodeList.getLength() != 1) {
                        Print.logError("More than 1 'AutoIndex' values specified");
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                            "More than 1 'AutoIndex' value specified");
                        return;
                    }

                    /* get auto-index value */
                    Node autoNode = (Element)autoNodeList.item(0);
                    long autoIndex = StringTools.parseLong(XMLTools.getNodeText(autoNode," ",false), -1L);
                    if (autoIndex < 0L) {
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                            "AutoIndex is blank/invalid");
                        return;
                    }

                    /* get EventData record */
                    EventData ed = null;
                    try {
                        ed = EventData.getAutoIndexEvent(autoIndex);
                    } catch (DBException dbe) {
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                            "EventData Error: " + dbe.getMessage());
                        return;
                    }

                    /* write event XML */
                    Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                    if (ed != null) {
                        try {
                            if (_isAuthorizedDevice(ed.getRecordKey(), account, user)) {
                                ed.printXML(pw, 1 * 3, selFields, reqState.isSoapRequest());
                            }
                        } catch (DBException dbe) {
                            Print.logException("EventData authorization check error: " + autoIndex, dbe);
                            pw.write(PFX1);
                            Service.writeMessage(isSoapReq, pw, ServiceMessage.MSG_READ_FAILED);
                            pw.write(PFX1);
                            Service.writeComment(isSoapReq, pw, "EventData Error: " + dbe.getMessage());
                        }
                    }
                    Service.writeResponse_end(isSoapReq, pw);
                    return;

                }

                /* device list */
                OrderedSet<String> deviceIDList = new OrderedSet<String>();

                /* Device TAG_Device */
                NodeList dvNodeList = XMLTools.getChildElements(edElem,TAG_Device);
                if (dvNodeList.getLength() > 0) {
                    for (int i = 0; i < dvNodeList.getLength(); i++) {
                        Node dvNode = (Element)dvNodeList.item(i);

                        /* get single device */
                        String deviceID = StringTools.trim(XMLTools.getNodeText(dvNode," ",false));
                        if (StringTools.isBlank(deviceID)) {
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                                "DeviceID is blank");
                            return;
                        }

                        /* User authorized for device */
                        try {
                            if ((user != null) && !user.isAuthorizedDevice(deviceID)) {
                                Print.logError("Command Device not authorized for user: " + deviceID);
                                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                                    "Not Authorized: " + deviceID);
                                return;
                            }
                        } catch (DBException dbe) {
                            Print.logException("Device read error: " + deviceID, dbe);
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                                "User/Device Error: " + dbe.getMessage());
                            return;
                        }

                        /* add device */
                        deviceIDList.add(deviceID);

                    }
                }

                /* Device TAG_DeviceGroup */
                NodeList gpNodeList = XMLTools.getChildElements(edElem,TAG_DeviceGroup); // eventdata
                if (gpNodeList.getLength() > 0) {
                    for (int i = 0; i < gpNodeList.getLength(); i++) {
                        Node gpNode = (Element)gpNodeList.item(i);

                        /* get single device group */
                        String groupID = StringTools.trim(XMLTools.getNodeText(gpNode," ",false));
                        if (StringTools.isBlank(groupID)) {
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_GROUP_INVALID,
                                "DeviceGroupID is blank");
                            return;
                        }

                        /* get device list */
                        try {
                            if (groupID.equalsIgnoreCase(DeviceGroup.DEVICE_GROUP_ALL)) {
                                // set all devices and exit loop
                                deviceIDList = User.getAuthorizedDeviceIDs(user, account, false/*inclInactv*/);
                                break;
                            } else {
                                deviceIDList.addAll(DeviceGroup.getDeviceIDsForGroup(authAcctID, groupID, user, false/*inclInactv*/));
                            }
                        } catch (DBException dbe) {
                            Print.logException("DeviceGroup read error: " + groupID, dbe);
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                                "User/DeviceGroup Error: " + dbe.getMessage());
                            return;
                        }
                        
                    }
                }
                
                /* no devices? */
                if (ListTools.isEmpty(deviceIDList)) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                        "No Device/Group specified");
                    return;
                }

                /* Limit */
                long limit = -1L;
                EventData.LimitType limitType = EventData.LimitType.LAST;
                NodeList limitNodeList = XMLTools.getChildElements(edElem,TAG_Limit);
                if (limitNodeList.getLength() > 0) {
                    Element node = (Element)limitNodeList.item(0);
                    String  lt   = XMLTools.getAttribute(node,ATTR_type,"",false);
                    limitType = lt.equalsIgnoreCase("first")? EventData.LimitType.FIRST : EventData.LimitType.LAST;
                    limit     = StringTools.parseLong(XMLTools.getNodeText(node," ",false), limit);
                }
                if (deviceIDList.size() > 1) {
                    if (limit <= 1) {
                        limit = 1;
                    } else {
                        // Divide limit by the number of devices in the list
                        //Print.logInfo("limit=%d, size=%d", limit, deviceIDList.size());
                        //limit = (long)Math.ceil((double)limit / (double)deviceIDList.size());
                    }
                }
                Print.logInfo("Limit = " + limit);
                reqState.setEventLimitType(limitType);

                /* Limit */
                /* obsolete section below
                long limit = -1;
                NodeList limNodeList = XMLTools.getChildElements(edElem,TAG_Limit);
                Node limNode = (limNodeList.getLength() > 0)? (Element)limNodeList.item(0) : null;
                limit = StringTools.parseLong(XMLTools.getNodeText(limNode,"",false),-1);
                if (deviceIDList.size() > 1) {
                    if (limit <= 1) {
                        limit = 1;
                    } else {
                        //Print.logInfo("limit=%d, size=%d", limit, deviceIDList.size());
                        //limit = (long)Math.ceil((double)limit / (double)deviceIDList.size());
                    }
                }
                Print.logInfo("Limit = " + limit);
                String limType = XMLTools.getNodeText(limNode,"last",false);
                EventData.LimitType limitType = limType.equalsIgnoreCase("first")? 
                    EventData.LimitType.FIRST : EventData.LimitType.LAST;
                reqState.setEventLimitType(limitType);
                */

                /* based on "timestamp"? ie "TimeFrom" / "TimeTo" */
                NodeList tfNodeList = XMLTools.getChildElements(edElem,TAG_TimeFrom);
                Element tfNode = (tfNodeList.getLength() > 0)? (Element)tfNodeList.item(0) : null;
                NodeList ttNodeList = XMLTools.getChildElements(edElem,TAG_TimeTo);
                Element ttNode = (ttNodeList.getLength() > 0)? (Element)ttNodeList.item(0) : null;

                /* based on 'creationMillis'? ie. "CreationFromMS" / "CreationToMS" */
                NodeList ctfmsNodeList = XMLTools.getChildElements(edElem,TAG_CreationFromMS);
                Element ctfmsNode = (ctfmsNodeList.getLength() > 0)? (Element)ctfmsNodeList.item(0) : null;
                NodeList cttmsNodeList = XMLTools.getChildElements(edElem,TAG_CreationToMS);
                Element cttmsNode = (cttmsNodeList.getLength() > 0)? (Element)cttmsNodeList.item(0) : null;

                /* check mutual exclusivity between "CreationTime[From|To]MS" and "Time[From|To]" */
                if ((tfNode == null) && (ctfmsNode == null)) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                        "Missing 'From' time specification");
                    return;
                } else
                if ((tfNode != null) == (ctfmsNode != null)) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                        "'CreationFromMS'/'TimeFrom' are mutually exclusive");
                    return;
                } else
                if ((ttNode == null) && (cttmsNode == null)) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                        "Missing 'To' time specification");
                    return;
                } else
                if ((ttNode != null) == (cttmsNode != null)) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                        "'CreationToMS'/'TimeTo' are mutually exclusive");
                    return;
                }

                /* use "CreationFromMS" / "CreationToMS" */
                if (ctfmsNode != null) {
                    if (cttmsNode == null) {
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                            "Missing 'CreationToMS'");
                        return;
                    }
                    long ctf = StringTools.parseLong(XMLTools.getNodeText(ctfmsNode," ",false),-1L);
                    long ctt = StringTools.parseLong(XMLTools.getNodeText(cttmsNode," ",false),-1L);

                    /* write response */
                    Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                    for (String devID : deviceIDList) {
    
                        /* get EventData records */
                        EventData ed[] = null;
                        try {
                            ed = EventData.getEventsByCreationMillis(
                                authAcctID, devID,
                                ctf, ctt, limit);
                        } catch (DBException dbe) {
                            Print.logException("EventData read error: " + devID, dbe);
                            pw.write(PFX1);
                            Service.writeMessage(isSoapReq, pw, ServiceMessage.MSG_READ_FAILED);
                            pw.write(PFX1);
                            Service.writeComment(isSoapReq, pw, "EventData Error: " + dbe.getMessage());
                            break;
                        }
        
                        /* write response */
                        for (EventData evdb : ed) {
                            evdb.printXML(pw, 1 * 3, selFields, reqState.isSoapRequest());
                        }
    
                    }
                    Service.writeResponse_end(isSoapReq, pw);
                    return;

                }

                /* eventdata TAG_TimeFrom */
                DateTime dtTimeStart = Service.parseTimeNode(tfNode, null, false, acctTZ);
                if (dtTimeStart == null) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                        "Invalid 'TimeFrom'");
                    return;
                }
                reqState.setEventDateFrom(dtTimeStart);

                /* eventdata TAG_TimeTo */
                DateTime dtTimeEnd = Service.parseTimeNode(ttNode, null, true, acctTZ);
                if (dtTimeEnd == null) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DATETIME,
                        "Invalid 'TimeTo'");
                    return;
                }
                reqState.setEventDateTo(dtTimeEnd);

                /* GPSRequired */
                NodeList gpsNodeList = XMLTools.getChildElements(edElem,TAG_GPSRequired);
                Node gpsNode = (gpsNodeList.getLength() > 0)? (Element)gpsNodeList.item(0) : null;
                boolean validGPS = StringTools.parseBoolean(XMLTools.getNodeText(gpsNode,"",false),false);

                /* Ascending */
                NodeList ascNodeList = XMLTools.getChildElements(edElem,TAG_Ascending);
                Node ascNode = (ascNodeList.getLength() > 0)? (Element)ascNodeList.item(0) : null;
                boolean ascending = StringTools.parseBoolean(XMLTools.getNodeText(ascNode,"",false),true);

                /* StatusCode */
                OrderedSet<Integer> scList = new OrderedSet<Integer>();
                NodeList scNodeList = XMLTools.getChildElements(edElem,TAG_StatusCode);
                for (int i = 0; i < scNodeList.getLength(); i++) {
                    Node scNode = scNodeList.item(i);
                    String scStr = XMLTools.getNodeText(scNode,null,false);
                    if (!StringTools.isBlank(scStr)) {
                        int sc = StringTools.parseInt(scStr,-1);
                        if ((sc > 0) && (sc <= 0xFFFF)) {
                            scList.add(new Integer(sc));
                        }
                    }
                }
                int statCode[] = !scList.isEmpty()? ListTools.toIntArray(scList) : null;

                /* write response */
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                for (String devID : deviceIDList) {

                    /* get EventData records */
                    EventData ed[] = null;
                    try {
                        ed = EventData.getRangeEvents(
                            authAcctID, devID,
                            dtTimeStart.getTimeSec(), dtTimeEnd.getTimeSec(),
                            statCode,
                            validGPS,
                            limitType, limit, ascending,
                            null/*addtnlSelect*/);
                    } catch (DBException dbe) {
                        Print.logException("EventData read error: " + devID, dbe);
                        pw.write(PFX1);
                        Service.writeMessage(isSoapReq, pw, ServiceMessage.MSG_READ_FAILED);
                        pw.write(PFX1);
                        Service.writeComment(isSoapReq, pw, "EventData Error: " + dbe.getMessage());
                        break;
                    }
    
                    /* write response */
                    //try {
                        //String evAcctID = null;
                        //String evDevID  = null;
                        //boolean authOK  = false;
                        for (EventData evdb : ed) {
                            /* already authorized above
                            if ((evAcctID == null) || (evDevID == null)) {
                                evAcctID = evdb.getAccountID();
                                evDevID  = evdb.getDeviceID();
                                authOK   = _isAuthorizedDevice(evdb.getRecordKey(), account, user)) {
                            } else
                            if (!evAcctID.equals(evdb.getAccountID()) || !evDevID.equals(evdb.getDeviceID())) {
                                evAcctID = evdb.getAccountID();
                                evDevID  = evdb.getDeviceID();
                                authOK   = _isAuthorizedDevice(evdb.getRecordKey(), account, user)) {
                            }
                            if (authOK) {
                                evdb.printXML(pw, 1 * 3, selFields, reqState.isSoapRequest());
                            }
                            */
                            evdb.printXML(pw, 1 * 3, selFields, reqState.isSoapRequest());
                        }
                    /*
                    } catch (DBException dbe) {
                        Print.logException("EventData authorization check error: " + devID, dbe);
                        pw.write(PFX1);
                        Service.writeMessage(isSoapReq, pw, ServiceMessage.MSG_READ_FAILED);
                        pw.write(PFX1);
                        Service.writeComment(isSoapReq, pw, "EventData Error: " + dbe.getMessage());
                        break;
                    }
                    */

                }
                Service.writeResponse_end(isSoapReq, pw);

            }
        });

        // ------------------------------------------------
        // pushpins
        addCommand(new CommandHandler(CMD_pushpins) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);
                String              PFX2       = PREFIX(isSoapReq, 2 * 3);

                /* map data */
                MapProvider mapProv = reqState.getMapProvider();
                if (mapProv == null) {
                    Print.logError("MapProvider not found");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_MAP_NOT_FOUND,
                        "MapProvider not found");
                    return;
                }

                /* pushpins */
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                pw.write(PFX1);
                pw.write(startTAG(isSoapReq,TAG_Pushpins,false,true));
                OrderedMap<String,PushpinIcon> pushpins = mapProv.getPushpinIconMap(reqState);
                int ppNdx = 0;
                for (PushpinIcon ppi : pushpins.values()) {
                    String shadowURL = ppi.getShadowURL();
                    pw.write(PFX2);
                    pw.write(startTAG(isSoapReq,TAG_Pushpin,
                        ATTR(ATTR_index,(ppNdx++)) +
                        ATTR((ppi.getIconEval()?ATTR_eval:ATTR_url),ppi.getIconURL()) +
                        ATTR(ATTR_size,ppi.getIconWidth()+","+ppi.getIconHeight()) +
                        ATTR(ATTR_offset,ppi.getIconHotspotX()+","+ppi.getIconHotspotY()) +
                        (!StringTools.isBlank(shadowURL)?
                            ATTR(ATTR_shadowUrl,shadowURL) +
                            ATTR(ATTR_shadowSize,ppi.getShadowWidth()+","+ppi.getShadowHeight()) : ""),
                        true,true));
                }
                pw.write(PFX1);
                pw.write(endTAG(isSoapReq,TAG_Pushpins,true));
                Service.writeResponse_end(isSoapReq, pw);

            }
        });

        // ------------------------------------------------
        // devcmd
        addCommand(new CommandHandler(CMD_devcmd) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // TAG_DeviceCommand
                NodeList dcNodeList = XMLTools.getChildElements(gtsRequest,TAG_DeviceCommand);
                if (dcNodeList.getLength() <= 0) {
                    Print.logError("Missing 'DeviceCommand' entry");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                        "Missing 'DeviceCommand' entry");
                    return;
                }
                Element dcElem = (Element)dcNodeList.item(0);

                /* command type/name */
                String cmdType = XMLTools.getAttribute(dcElem,ATTR_type,DCServerConfig.COMMAND_CONFIG,false);
                String cmdName = XMLTools.getAttribute(dcElem,ATTR_name,null,false);

                /* command arguments */
                String cmdArgs[] = new String[ATTR_args_.length];
                for (int a = 0; a < ATTR_args_.length; a++) {
                    String argVal = XMLTools.getAttribute(dcElem, ATTR_args_[a], null, false); // may be null
                    if ((a == 0) && StringTools.isBlank(argVal)) {
                        argVal = XMLTools.getAttribute(dcElem, ATTR_arg, null, false);
                    }
                    cmdArgs[a] = argVal; // added 'null' values will result in an undefined corresponding 'argX'
                    //Print.logInfo("Arg " + a + " ==> " + argVal);
                }

                /* device commands */
                int devCmdCnt = 0;
                NodeList dvNodeList = XMLTools.getChildElements(dcElem,TAG_Device);
                for (int d = 0; d < dvNodeList.getLength(); d++) {
                    Element devElem = (Element)dvNodeList.item(d);
                    String deviceID = StringTools.trim(XMLTools.getNodeText(devElem," ",false));
                    Device device = null;
                    try {
                        if ((user != null) && !user.isAuthorizedDevice(deviceID)) {
                            Print.logError("Command Device not authorized for user: " + deviceID);
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                                "Not Authorized: " + deviceID);
                            return;
                        }
                        device = Device.getDevice(account, deviceID); // null if non-existent
                    } catch (DBException dbe) {
                        Print.logException("Device read error: " + deviceID, dbe);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                            "Device Error: " + dbe.getMessage());
                        return;
                    }
                    if (device == null) {
                        Print.logError("Command Device does not exist: " + deviceID);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                            "Device not found: " + deviceID);
                        return;
                    }
                    reqState.setSelectedDeviceID(deviceID);
                    String serverID = device.getDeviceCode();
                    RTProperties resp = DCServerFactory.sendServerCommand(device, cmdType, cmdName, cmdArgs); // proxy
                    if (resp == null) {
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                            "Device command failed: " + deviceID);
                        return;
                    } else {
                        Print.logInfo("Device Command response: " + resp);
                        boolean ok = DCServerFactory.isCommandResultOK(resp);
                        if (!ok) {
                            String err = DCServerFactory.getCommandResultID(resp);
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                                "Device command failed ["+err+"]: " + deviceID);
                            return;
                        } else {
                            devCmdCnt++;
                            //Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_SUCCESSFUL,
                            //    null);
                            //return;
                            // handled below
                        }
                    }
                }

                /* deviceGroup commands */
                NodeList dgNodeList = XMLTools.getChildElements(dcElem,TAG_DeviceGroup); // devcmd
                for (int g = 0; g < dgNodeList.getLength(); g++) {
                    Element grpElem = (Element)dgNodeList.item(g);
                    // TODO:
                }

                /* result message */
                if (devCmdCnt > 0) {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_SUCCESSFUL,
                        null);
                } else {
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_DEVICE_INVALID,
                        "Missing Device specification");
                }

            }
        });

        // ------------------------------------------------
        // statusCodes
        addCommand(new CommandHandler(CMD_statuscodes) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                Account             account    = reqState.getCurrentAccount();
                User                user       = reqState.getCurrentUser();
                String              authAcctID = reqState.getCurrentAccountID();
                String              authUserID = reqState.getCurrentUserID();
                TimeZone            acctTZ     = reqState.getTimeZone();
                Locale              locale     = reqState.getLocale();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                /* List of custom status codes */
                PrivateLabel privLabel = reqState.getPrivateLabel();
                Map<Integer,String> scDescMap = privLabel.getStatusCodeDescriptionMap();

                /* list status codes */
                Service.writeResponse_begin(isSoapReq, pw, cmd, true);
                for (Integer sc : scDescMap.keySet()) {
                    String desc = scDescMap.get(sc);
                    pw.write(PFX1);
                    pw.write(startTAG(isSoapReq,TAG_StatusCode,
                        ATTR(ATTR_code,"0x"+StringTools.toHexString(sc,16)),
                        false,false));
                    pw.write(CDATA(isSoapReq,desc));
                    pw.write(endTAG(isSoapReq,TAG_StatusCode,true));
                }
                Service.writeResponse_end(isSoapReq, pw);
                return;

            }
        });

        // ------------------------------------------------
        // custom
        String customClassName = RTConfig.getString(DBConfig.PROP_track_service_customCommandHandler, null);
        if (!StringTools.isBlank(customClassName)) {
            try {
                Class customClass = Class.forName(customClassName);
                Object obj = customClass.newInstance();
                if (obj instanceof CommandHandler) {
                    _CommandHandler_Custom = (CommandHandler)obj;
                    _CustomCommand         = null;
                } else
                if (obj instanceof CustomCommand) {
                    _CommandHandler_Custom = null;
                    _CustomCommand         = (CustomCommand)obj;
                } else {
                    Print.logError("Invalid custom command class: " + customClassName);
                }
            } catch (ClassNotFoundException cnfe) {
                Print.logError("Custom command class not found: " + customClassName);
            } catch (Throwable th) {
                Print.logException("Error instantiating custom command class", th);
            }
        }
        addCommand(new CommandHandler(CMD_custom) {
            @SuppressWarnings("unchecked")
            public void handleCommand(Element gtsRequest, RequestProperties reqState, PrintWriter pw) throws IOException {
                String              cmd        = this.getName();
                boolean             isSoapReq  = reqState.isSoapRequest();
                String              PFX1       = PREFIX(isSoapReq, 1 * 3);

                // TAG_CustomCommand
                NodeList cuNodeList = XMLTools.getChildElements(gtsRequest,TAG_CustomCommand);
                if (cuNodeList.getLength() <= 0) {
                    Print.logError("Missing 'CustomCommand' entry");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_REQUEST_XML_INVALID,
                        "Missing 'CustomCommand' entry");
                    return;
                }
                Element cuElem = (Element)cuNodeList.item(0); // CustomCommand tag

                /* callback to full custom command handler */
                if (_CommandHandler_Custom != null) {
                    _CommandHandler_Custom.handleCommand(cuElem, reqState, pw);
                    return;
                }

                /* try simple custom command */
                if (_CustomCommand != null) {
                    Account      account   = reqState.getCurrentAccount();
                    User         user      = reqState.getCurrentUser();
                    PrivateLabel privLabel = reqState.getPrivateLabel();
                    String       _cmd      = XMLTools.getAttribute(cuElem,ATTR_name,cmd,false);
                    String       _cmdArg   = XMLTools.getAttribute(cuElem,ATTR_arg,null,false); // may be null
                    byte R[] = _CustomCommand.handleCommand(
                        _cmd, _cmdArg,
                        account, user, 
                        privLabel, 
                        "");
                    // CustomCommand response
                    Service.writeResponse_begin(isSoapReq, pw, cmd, false);
                    pw.write(PFX1);
                    pw.write(startTAG(isSoapReq,TAG_CustomCommand,
                        ATTR(ATTR_name,_cmd   ) +
                        ATTR(ATTR_arg ,_cmdArg),
                        false,false));
                    pw.write(CDATA(isSoapReq,StringTools.toStringValue(R)));
                    pw.write(endTAG(isSoapReq,TAG_CustomCommand,true));
                    Service.writeResponse_end(isSoapReq, pw);
                    return;
                }

                /* return */
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_CUSTOM_NOT_SUPPORTED,
                    "Custom commands not supported");
                return;

            }
        });

    }; // static initializer

    // ------------------------------------------------------------------------

    /* check for user authorized device */
    // Note: this is called for all table records retrieved (from any DBFactory)
    // to see if the user has access to the "deviceID" in the record. 
    // The following DBFactories should not be subject to this check:
    //  - Driver
    protected static boolean _isAuthorizedDevice(DBRecordKey rcdKey, Account account, User user)
        throws DBException
    {

        /* quick checks */
        if (rcdKey == null) {
            // no record key
            Print.logWarn("Record Key is null");
            return false;
        } else
        if (account == null) {
            // no account, not authorized
            Print.logWarn("Account is null");
            return false;
        }

        /* account is system administrator */
        if (account.isSystemAdmin()) {
            return true;
        }

        /* account-id match? */
        String rcdKeyAcctID = StringTools.trim(rcdKey.getFieldValue(Account.FLD_accountID)); // may be blank
        if (!StringTools.isBlank(rcdKeyAcctID) && !rcdKeyAcctID.equals(account.getAccountID())) {
            Print.logWarn("Record Key Account ["+rcdKeyAcctID+"] does not match authorized Account ["+account.getAccountID()+"]");
            return false;
        }

        /* "admin" user is authorized */
        if ((user == null) || user.isAdminUser()) {
            return true;
        }

        /* excluded tables */
        // Exclude "deviceID" checking for the following tables:
        //  - Driver
        DBFactory dbFact = rcdKey.getFactory();
        //String utableName = dbFact.getUntranslatedTableName();
        Class rcdClass = dbFact.getRecordClass();
        if (org.opengts.db.tables.Driver.class.equals(rcdClass)) {
            //Print.logInfo("Record class is Driver ...");
            return true;
        }

        /* user authorized? */
        if (dbFact.hasField(Device.FLD_deviceID)) {
            String devID = (String)rcdKey.getFieldValue(Device.FLD_deviceID);
            if (!user.isAuthorizedDevice(devID)) {
                Print.logWarn("User not authorized to device: " + devID);
                return false;
            }
        }

        /* authorized */
        return true;

    }
    
    /* parse/return DBRecordKey */
    protected static DBRecordKey parseDBRecordKey(
        boolean isSoapReq, PrintWriter pw, 
        String cmd, 
        Element childElem, String authAcctID, String authUserID)
        throws IOException
    {
        DBRecordKey rcdKey = null;
        try {

            /* create key */
            rcdKey = DBFactory.parseXML_DBRecordKey(childElem);

            /* check for authorized account */
            DBFactory dbFact = rcdKey.getFactory();
            if (dbFact.hasField(Account.FLD_accountID)) {
                // Table contains an "accountID" field
                if (!AccountRecord.isSystemAdminAccountID(authAcctID)) {
                    // authorized user is not "sysadmin"
                    String aid = StringTools.trim(rcdKey.getFieldValue(Account.FLD_accountID));
                    if (StringTools.isBlank(aid)) {
                        if (!rcdKey.hasFieldValue(DBRecordKey.FLD_autoIndex)) {
                            Print.logWarn("DBRecordKey missing account-id: %s != blank", authAcctID);
                            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY, 
                                "No Account specified on RecordKey");
                            return null;
                        }
                    } else
                    if (!authAcctID.equals(aid)) {
                        // non-sysadmin account does not equals requested accountID
                        Print.logWarn("DBRecordKey account mismatch: %s != %s", authAcctID, aid);
                        Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY, 
                            "Invalid Account specified on RecordKey");
                        return null;
                    }
                }
            }

        } catch (DBException dbe) {
            Print.logException("DBRecordKey:", dbe);
            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_BAD_RECORD_KEY,
                "RecordKey Error: " + dbe.getMessage());
            return null;
        }
        return rcdKey;
    }

    // ------------------------------------------------------------------------

    private static final char XML_CHARS[] = new char[] { '_', '-', '.', ',', '/', '+', ':', '|', '=', ' ' };
    private static String _xmlFilter(boolean isSoapReq, String value)
    {
        if ((value == null) || value.equals("")) { // do not use StringTools.isBlank (spaces are significant)
            return "";
        } else
        if (StringTools.isAlphaNumeric(value,XML_CHARS)) {
            return value; // return all significant spaces
        } else {
            String v = StringTools.replace(value,"\n","\\n");
            return CDATA(isSoapReq,v);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    public Service()
    {
        // super
    }
    
    // ------------------------------------------------------------------------
    // GET/POST entry point

    private static final String SOAP_ENVELOPE = "schemas.xmlsoap.org/soap/envelope";

    /* GET request */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {

        /* look for SOAP message*/
        boolean isSoapReq = false;
        String xmlReqStr = StringTools.toStringValue(FileTools.readStream(request.getInputStream())).trim();
		if (xmlReqStr.toLowerCase().indexOf(SOAP_ENVELOPE) >= 0) {
			isSoapReq = true;
		}

        /* disabled anyway? */
        // do we really want to indicate to the casual web-browser user whether or not the service is enabeld?
        boolean enabled = RTConfig.getBoolean(DBConfig.PROP_track_enableService,false);
        String responseMsg = enabled?
            "'GET' not allowed" :
            "'GET' not allowed";

        /* response */
        CommonServlet.setResponseContentType(response, HTMLTools.MIME_XML(), "utf-8");
        response.setStatus(200);
        PrintWriter pw = response.getWriter();
        Print.logError(responseMsg);
        Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_REQUEST_POST_REQUIRED,
            responseMsg);

    }

    /* POST request */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        CommonServlet.setResponseContentType(response, HTMLTools.MIME_XML(), "utf-8");        
        response.setStatus(200);

        /* Access-Control-Allow-Origin */
        //response.setHeader("Access-Control-Allow-Origin", "*");
        String acao = RTConfig.getString(DBConfig.PROP_track_service_AccessControlAllowOrigin,null);
        if (!StringTools.isBlank(acao)) {
            // ie. "Access-Control-Allow-Origin: *"
            response.setHeader(HTMLTools.HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, acao);
        }

        /* stream PrintWriter */
        PrintWriter pw = response.getWriter();

        /* find/extract XML request */
        boolean isSoapReq = false;
        String xmlReqStr = StringTools.toStringValue(FileTools.readStream(request.getInputStream())).trim();
		if (xmlReqStr.toLowerCase().indexOf(SOAP_ENVELOPE) >= 0) {
			isSoapReq = true;
		}	
        if (!xmlReqStr.startsWith("<")) {
            Print.logWarn("Request not found in inputStream, checking parameters ...");
            java.util.Enumeration en = request.getParameterNames(); 
            while (en.hasMoreElements()) { // for some reason this is not picking up, xml=<GTSRequest?  
                String n = (String)en.nextElement(); 
                String v = StringTools.trim(request.getParameter(n));  //xml=<GTSRequest... parm=<GTSRequest
                if ((v != null) && v.startsWith("<")) {
                    Print.logWarn("Found possible request XML, parameter ==> %s", n);
                    xmlReqStr = v;
                    break;
                }
            }
            if (!xmlReqStr.startsWith("<")) {
                Print.logError("Request not found (empty/invalid):\n" + xmlReqStr);
                Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_REQUEST_XML_SYNTAX,
                    "Request XML is invalid/empty");
                return;
            }
        }

		/* convert SOAP payload to XML */
        if (isSoapReq) {
            /* To use the existing _parseXML function we need to strip the soap envelope and convert 
            the incoming entity referenced encoded xml < > */ 
            String inputXmlStart = "<inputxml xmlns=\"http://service.war.extra.opengts.org\">";
            String inputXmlEnd   = "</inputxml>";
            int start = xmlReqStr.toLowerCase().indexOf(inputXmlStart);
            int end   = xmlReqStr.toLowerCase().indexOf(inputXmlEnd);   // as long as the WSDL file is use to create the 					
            if ((start >= 0) && (end > 0)) {                           // web service client this will always be true.
                String embeddedEncodedXML = xmlReqStr.substring(start + inputXmlStart.length(), end); 
                String tmp1 = embeddedEncodedXML.replaceAll("&lt;"   ,"<");
                String tmp2 =               tmp1.replaceAll("&gt;"   ,">");
                String tmp3 =               tmp2.replaceAll("&quot;" ,"\"");  // some client will entity refence encode these 
                xmlReqStr =                 tmp3.replaceAll("&apos;" ,"'");   // characters, if left unchecked it just cause 
                //Print.logInfo("xmlReqStr=" + xmlReqStr);                     // org.xml.sax.SAXParseException
                //_parseXML will process this just fine now.				
            } else {
                Print.logError("Found SOAP Request: (Input Parameter Name is incorrect (inputXML):\n " + xmlReqStr);
                Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_REQUEST_SOAP_XML_SYNTAX,
                   "Found SOAP Request: (inputXML is missing refer to the WSDL file.");
                return;
            }			
        }		

        /* 'Service' disnabled? */
        if (!RTConfig.getBoolean(DBConfig.PROP_track_enableService,false)) {
            Print.logError("'Service' not enabled");
            Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_REQUEST_DISABLED, 
                "Service disabled");
            return;
        }

        /* get PrivateLabel instance */
        PrivateLabel privLabel = null;
        URL    requestURL      = null;
        String requestHostName = null;
        String requestUrlPath  = null;
        try {
            requestURL      = new URL(request.getRequestURL().toString());
            requestHostName = requestURL.getHost();
            requestUrlPath  = requestURL.getPath();
            privLabel       = (PrivateLabel)PrivateLabelLoader.getPrivateLabelForURL(requestURL);
        } catch (MalformedURLException mfue) {
            // invalid URL? (unlikely to occur)
            Print.logWarn("Invalid URL? " + request.getRequestURL());
            privLabel = (PrivateLabel)PrivateLabelLoader.getDefaultPrivateLabel();
        }

        /* PrivateLabel not found */
        if (privLabel == null) {
            if (BasicPrivateLabelLoader.hasParsingErrors()) {
                Print.logError("'private.xml' contains syntax errors");
                Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_PRIVATE_XML_SYNTAX,
                    "'private.xml' parsing failure");
            } else
            if (!BasicPrivateLabelLoader.hasDefaultPrivateLabel()) {
                Print.logError("URL not allowed");
                Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_URL_NOT_ALLOWED,
                    "URL not allowed for this Account");
            } else {
                Print.logError("'private.xml' contains config errors");
                Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_PRIVATE_XML_CONFIG,
                    "Invalid configuration found in 'private.xml'");
            }
            return;
        }

        /* from IP address */
        String ipAddr = request.getRemoteAddr();
        Print.logInfo("Connect from IPAddr: " + ipAddr);
        if (DISPLAY_PARAMETERS) {
            java.util.Enumeration en = request.getParameterNames(); 
            while (en.hasMoreElements()) { 
                String n = (String)en.nextElement(); 
                String v = request.getParameter(n);
                Print.logInfo(" "+n+" ==> ("+v+")");
            }
        }

        /* account/user/password in query string */
        AttributeTools.parseRTP(request);
        AttributeTools.parseMultipartFormData(request);
        String authAcctID = AttributeTools.getRequestString(request, ATTR_account , null); // query
        String authUserID = AttributeTools.getRequestString(request, ATTR_user    , null); // query
        String authPasswd = AttributeTools.getRequestString(request, ATTR_password, null); // query

        /* parse XML */
        RTProperties hostProps = Resource.getPrivateLabelPropertiesForHost(requestHostName, requestUrlPath);
        try {
            privLabel.pushRTProperties();
            if (hostProps != null) {
                RTConfig.pushTemporaryProperties(hostProps);
            }
            this._parseXML(
                xmlReqStr, isSoapReq, 
                request, response,
                pw, privLabel, 
                authAcctID, authUserID, authPasswd);
        } catch (ServletException se) {
            Print.logException(se.getMessage(), se);
            throw se;
        } catch (IOException ioe) {
            Print.logException(ioe.getMessage(), ioe);
            throw ioe;
        } catch (Throwable th) {
            Print.logException("Unrecognized Exception", th);
            throw new IOException("Unrecognized Exception", th);
        } finally {
            if (hostProps != null) {
                RTConfig.popTemporaryProperties(hostProps);
            }
            privLabel.popRTProperties();
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public String handleRequest(String xmlReqStr)
    {
        return this.handleRequest(xmlReqStr, null, null, null);
    }

    public String handleRequest(String xmlReqStr,
        String authAcctID, String authUserID, String authPasswd)
    {
        boolean isSoapReq = false;

        /* parse request and get response */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        try {
            this._parseXML(
                xmlReqStr, isSoapReq, 
                null, null,
                pw, null,
                authAcctID, authUserID, authPasswd);
        } catch (Throwable th) {
            return null;
        }
        
        /* return response */
        return StringTools.toStringValue(baos.toByteArray());

    }
    
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private void _parseXML(
        String xmlReqStr, boolean isSoapReq, 
        HttpServletRequest request, HttpServletResponse response,
        PrintWriter pw, PrivateLabel privLabel/*may be null*/,
        String authAcctID, String authUserID, String authPasswd)
        throws ServletException, IOException
    {

        /* simple syntax check (quick rejection) */
        if ((xmlReqStr == null) || !xmlReqStr.startsWith("<")) {
            Print.logError("Request not found (empty/invalid):\n" + xmlReqStr);
            Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_REQUEST_XML_SYNTAX,
                "Request XML is invalid/empty");
            return;
        }

        /* parse XML document */
        Document xmlDoc = XMLTools.getDocument(xmlReqStr);
        if (xmlDoc == null) {
            Print.logError("Request Syntax Error:\n" + xmlReqStr);
            Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_REQUEST_XML_SYNTAX,
                "Request XML parsing syntax error");
            return;
        }

        /* parse 'GTSRequest' */
        Element gtsRequest = xmlDoc.getDocumentElement();
        if (!gtsRequest.getTagName().equalsIgnoreCase(TAG_GTSRequest)) {
            Print.logError("Request XML does not start with '%s'", TAG_GTSRequest);
            Service.writeMessageResponse(isSoapReq, pw, null, ServiceMessage.MSG_REQUEST_XML_INVALID,
                "'GTSRequest' tag not found");
            return;
        }

        /* request command/argument */
        String cmd = gtsRequest.getAttribute(ATTR_command);
        String arg = gtsRequest.getAttribute(ATTR_parameter);

        /* command enabled? */
        boolean cmdEnabled_conditional; // allowed if Account "allowWebService" is true
        boolean cmdEnabled_always;      // allowed regardless of Account "allowWebService" setting
        {
            String _enabled = RTConfig.getString(DBConfig.PROP_track_service_commandEnabled_+cmd,"");
            if (StringTools.isBlank(_enabled)) { 
                // -- blank: default allow conditional only
                cmdEnabled_conditional = true;
                if (cmd.equalsIgnoreCase(CMD_report)) {
                    // -- "report" command defaults to unconditional, if blank
                    cmdEnabled_always  = true;
                } else {
                    // -- other commands default to conditional
                    cmdEnabled_always  = false;
                }
            } else
            if (_enabled.equalsIgnoreCase("yes")        ||
                _enabled.equalsIgnoreCase("true")       ||
                _enabled.equalsIgnoreCase("enable")     ||
                _enabled.equalsIgnoreCase("enabled")    ||
                _enabled.equalsIgnoreCase("conditional")  ) {
                // -- eplicit enabled: conditional only
                cmdEnabled_conditional = true;
                cmdEnabled_always      = false;
            } else
            if (_enabled.equalsIgnoreCase("always")       ||
                _enabled.equalsIgnoreCase("unconditional")  ) {
                // -- eplicit always: allow even if Account "allowWebService" is false
                cmdEnabled_conditional = true;
                cmdEnabled_always      = true;
            } else
            if (_enabled.equalsIgnoreCase("no")      ||
                _enabled.equalsIgnoreCase("false")   ||
                _enabled.equalsIgnoreCase("disable") ||
                _enabled.equalsIgnoreCase("disabled")  ) {
                // -- eplicit disabled: not allowed
                cmdEnabled_conditional = false;
                cmdEnabled_always      = false;
            } else {
                // -- otherwise: not allowed
                cmdEnabled_conditional = false;
                cmdEnabled_always      = false;
            }
        }

        /* exit now if command explicitly disabled */
        if (!cmdEnabled_conditional) {
            Print.logError("Command disabled: " + cmd);
            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_COMMAND_DISABLED,
                "Command disabled: " + cmd);
            return;
        }

        /* get specified Account/User/Password */
        if (StringTools.isBlank(authAcctID)) {
            // not specified on URL
            NodeList authList = XMLTools.getChildElements(gtsRequest,TAG_Authorization);
            if (authList.getLength() > 0) {
                // "Authorization" tag specified
                Element authElem = (Element)authList.item(0);
                authAcctID = XMLTools.getAttribute(authElem, ATTR_account , null, false);
                authUserID = XMLTools.getAttribute(authElem, ATTR_user    , null, false);
                authPasswd = XMLTools.getAttribute(authElem, ATTR_password, null, false);
                String aup = XMLTools.getAttribute(authElem, ATTR_auth    , null, false);
                if (StringTools.isBlank(authAcctID) && !StringTools.isBlank(aup)) {
                    String authKey = RTConfig.getString(DBConfig.PROP_track_service_authKey); // parse
                    if (StringTools.isBlank(authKey)) {
                        // -- authorization obfuscation key not provided
                        Print.logWarn("Authorization decode key not specified!");
                    }
                    ServiceRequest.Authorization auth = new ServiceRequest.Authorization(aup, authKey);
                    authAcctID = auth.getAccountID();
                    authUserID = auth.getUserID();
                    authPasswd = auth.getPassword();
                }
            }
            if (StringTools.isBlank(authAcctID)) { // still blank?
                Print.logError("Account not specified");
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_INVALID,
                    "Account not specified");
                return;
            }
        }

        /* load account */
        Account account = null;
        try {

            /* get account */
            account = Account.getAccount(authAcctID);
            if (account == null) {
                Print.logError("Account not found: %s", authAcctID);
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_INVALID,
                    "");
                return;
            } else
            if (!account.isActive()) {
                Print.logError("Account inactive: %s", authAcctID);
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_INACTIVE,
                    null);
                return;
            } else
            if (account.isExpired()) {
                Print.logError("Account expired: %s", authAcctID);
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_EXPIRED,
                    null);
                return;
            }

            /* web service allowed for Account? */
            if (!Account.GetAllowWebService(account)) {
                // -- Account general web-service not allowed
                if (cmdEnabled_always) {
                    // -- comand allowed even if Account "allowWebService" is false
                    Print.logInfo("Account web-service is disabled (%s), however command override allowed: %s", authAcctID, cmd);
                } else {
                    // -- Account web-service not allowed for this command
                    Print.logError("Account web-service now allowed: %s", authAcctID);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_SERVICE,
                        null);
                    return;
                }
            }

            /* check PrivateLabel */
            if (privLabel == null) {
                // -- use PrivateLabel from Account
                BasicPrivateLabel bpl = account.getPrivateLabel();
                if (bpl instanceof PrivateLabel) {
                    privLabel = (PrivateLabel)bpl;
                } else
                if (bpl != null) {
                    Print.logError("BasicPrivateLabel found (PrivateLabel environment not loaded)");
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_HOST,
                        "PrivateLabel environment not loaded");
                    return;
                } else {
                    String pln = account.getPrivateLabelName();
                    Print.logError("PrivateLabel not found: %s", pln);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_HOST,
                        "PrivateLabel not found ["+pln+"]");
                    return;
                }
            } else
            if (privLabel.isRestricted()) {
                // -- check that the Account is authorized for this host domain
                String acctDomain = account.getPrivateLabelName();
                String aliasName  = privLabel.getDomainName(); // may be null
                String hostName   = privLabel.getHostName();  // never null (may be PrivateLabel.DEFAULT_HOST)
                for (;;) {
                    if (!StringTools.isBlank(acctDomain)) {
                        if ((aliasName != null) && acctDomain.equals(aliasName)) {
                            break; // account domain matches alias name
                        } else
                        if ((hostName != null) && acctDomain.equals(hostName)) {
                            break; // account domain matches host name
                        } else
                        if (acctDomain.equals(BasicPrivateLabel.ALL_HOSTS)) {
                            break; // account domain explicitly states it is valid in all domains
                        }
                    }
                    // if we get here, we've failed the above tests
                    Print.logError("Account not allowed for this host/url: %s", authAcctID);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_ACCOUNT_HOST,
                        null);
                    return;
                }
            }

        } catch (DBException dbe) {

            // Internal error
            Print.logException("Account read error: " + authAcctID, dbe);
            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                "Account Error: " + dbe.getMessage());
            return;

        }
        // "account" is non-null here

        /* Account PrivateLabel */
        PrivateLabel acctPrivateLabel = null;
        if (account.hasPrivateLabel()) {
            try {
                acctPrivateLabel = (PrivateLabel)account.getPrivateLabel();
            } catch (ClassCastException cce) {
                Print.logWarn("Account did not return a valid 'PrivateLabel'");
                acctPrivateLabel = privLabel;
            }
        } else {
            acctPrivateLabel = privLabel;
        }

        /* default to 'admin' user */
        if (StringTools.isBlank(authUserID)) {
            authUserID = account.getDefaultUser();
            if (StringTools.isBlank(authUserID)) { 
                authUserID = acctPrivateLabel.getDefaultLoginUser();
            }
        }

        /* validate account/user/password */
        User user = null;
        try {

            /* lookup specified UserID */
            user = User.getUser(account, authUserID);
            if (user != null) {
                // we found a valid user
            } else
            if (User.isAdminUser(authUserID)) {
                // logging in as the 'admin' user, and we don't have an explicit user record
            } else {
                Print.logError("User not found: %s/%s", authAcctID, authUserID);
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_USER_INVALID,
                    "");
                return;
            }

            // check the password
            if (user != null) {
                if (!user.checkPassword(acctPrivateLabel,authPasswd)) {
                    // user password is invalid (invalidate account/user)
                    Print.logError("Invalid user password: %s/%s [%s]", authAcctID, authUserID, authPasswd);
                    Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_AUTH_FAILED,
                        "");
                    return;
                }
            } else
            if (!account.checkPassword(acctPrivateLabel,authPasswd)) {
                // account password is invalid (invalidate account)
                Print.logError("Invalid account password: %s [%s]", authAcctID, authPasswd);
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_AUTH_FAILED,
                    "");
                return;
            }

            /* inactive/expired user */
            if ((user != null) && !user.isActive()) {
                Print.logError("User inactive: %s/%s", authAcctID, authUserID);
                Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_USER_INACTIVE,
                    null);
                return;
            }

        } catch (DBException dbe) {

            // -- Internal error
            Print.logException("User read error: " + authAcctID + "/" + authUserID, dbe);
            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_READ_FAILED,
                "User Error: " + dbe.getMessage());
            return;

        }

        /* check user ACL authorization for command */
        boolean allowCommand = false;
        if (user != null) {
            // -- check user authorization
            String commandACL = AclEntry.ACL_SERVICE_ + cmd;
            allowCommand = acctPrivateLabel.hasAllAccess(user,commandACL);
        } else {
            // -- assume "admin" user
            //allowCommand = true; // 2.5.2-B05
            String commandACL = AclEntry.ACL_SERVICE_ + cmd;
            allowCommand = acctPrivateLabel.hasAllAccess(user,commandACL); // 
        }
        if (!allowCommand) {
            Print.logError("Command not allowed for user (per ACL): " + cmd + " [user '"+authAcctID+"/"+authUserID+"']");
            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_COMMAND_NOTAUTH,
                "Command not allowed: " + cmd);
            return;
        }

        /* init RequestProperties */
        RequestProperties reqState = new RequestProperties();
        reqState.setHttpServletRequest(request);
        request.setAttribute(PARM_REQSTATE, reqState);
        reqState.setHttpServletResponse(response);
        reqState.setCurrentAccount(account);
        reqState.setCurrentUser(user); // may be null
        reqState.setPrivateLabel(acctPrivateLabel); // account private label
        reqState.setTimeZone(account.getTimeZone(null), account.getTimeZone());
        reqState.setSoapRequest(isSoapReq);

        /* run command */
        CommandHandler cmdHndlr = CommandMap.get(cmd);
        if (cmdHndlr != null) {
            Print.logInfo("Handling command request: " + cmd);
            cmdHndlr.handleCommand(gtsRequest, reqState, pw);
        } else {
            Print.logError("Command not found: " + cmd);
            Service.writeMessageResponse(isSoapReq, pw, cmd, ServiceMessage.MSG_COMMAND_MISSING,
                "Command not found: " + cmd);
        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // Command-line entry for for testing/debugging:
    // ------------------------------------------------------------------------
    // bin/exeJava -cp build/service/WEB-INF/classes org.opengts.war.service.Service -url=http://localhost:8080/ws/Service -c=dbget    -a=demo -u=admin -p= -arg=rcd -table=Device
    // bin/exeJava -cp build/service/WEB-INF/classes org.opengts.war.service.Service -url=http://localhost:8080/ws/Service -c=dbget    -a=sysadmin -p=sysadmin -arg=rcd -table=Account
    // bin/exeJava -cp build/service/WEB-INF/classes org.opengts.war.service.Service -url=http://localhost:8080/ws/Service -c=dbput    -a=demo -u=admin -p= -d=demo2 -arg='Demo Test'
    // bin/exeJava -cp build/service/WEB-INF/classes org.opengts.war.service.Service -url=http://localhost:8080/ws/Service -c=dbschema -a=demo -u=admin -p= -arg=Device
    // bin/exeJava -cp build/service/WEB-INF/classes org.opengts.war.service.Service -url=http://localhost:8080/ws/Service -c=report   -a=demo -u=admin -p= -d=demo2
    // bin/exeJava -cp build/service/WEB-INF/classes org.opengts.war.service.Service -url=http://localhost:8080/ws/Service -c=mapdata  -a=demo -u=admin -p= -d=demo2
    // bin/exeJava -cp build/service/WEB-INF/classes org.opengts.war.service.Service -url=http://localhost:8080/ws/Service -c=pushpins -a=demo -u=admin -p=
    // ----------
    // bin/exeJava org.opengts.war.service.Service -url=http://localhost:8080/track/ws -c=dbschema -a=demo -u=admin -p= -arg=Device
    // bin/exeJava org.opengts.war.service.Service -url=http://localhost:8080/track/ws -c=pushpins -a=demo -u=admin -p=
    // bin/exeJava org.opengts.war.service.Service -url=http://localhost:8080/track/ws -c=report   -a=demo -u=admin -p= -device=demo2
    // bin/exeJava org.opengts.war.service.Service -url=http://localhost:8080/track/ws -c=mapdata  -a=demo -u=admin -p=
    //
    // bin/exeJava org.opengts.war.service.Service -url=http://serv1.resqgps.com:8080/track/ws -command=mapdata  -account=demo -user=admin -password=

    private static final String ARG_ACCOUNT[]   = new String[] { "account" , "a" };
    private static final String ARG_USER[]      = new String[] { "user"    , "u" };
    private static final String ARG_PASSWORD[]  = new String[] { "password", "p" };

    public static void main(String argv[])
    {
        DBConfig.cmdLineInit(argv,true);  // main
        String acctID  = RTConfig.getString(ARG_ACCOUNT , "");
        String userID  = RTConfig.getString(ARG_USER    , "");
        String passwd  = RTConfig.getString(ARG_PASSWORD, "");
    }

}

