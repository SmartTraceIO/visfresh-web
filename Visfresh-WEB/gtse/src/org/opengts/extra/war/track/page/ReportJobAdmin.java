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
//  2013/05/28  Martin D. Flynn
//     -Initial release
//  2014/12/18  Martin D. Flynn
//     -Check web-services enabled before displaying this page.
// ----------------------------------------------------------------------------
package org.opengts.extra.war.track.page;

import java.util.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.AclEntry.AccessLevel;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.track.*;
import org.opengts.war.report.*;

import org.opengts.extra.tables.*;
import org.opengts.extra.service.ServiceXML;

public class ReportJobAdmin
    extends WebPageAdaptor
    implements Constants
{

    // ------------------------------------------------------------------------

    private static final String _ACL_LIST[]             = new String[0];
    
    private static final boolean FLEET_REPORTS_ONLY     = true;

    // ------------------------------------------------------------------------
    // Parameters

    // forms 
    public  static final String FORM_RPTJOB_SELECT      = "ReportJobAdminSelect";
    public  static final String FORM_RPTJOB_EDIT        = "ReportJobAdminEdit";
    public  static final String FORM_RPTJOB_NEW         = "ReportJobAdminNew";

    // commands
    public  static final String COMMAND_INFO_UPDATE     = "update";
    public  static final String COMMAND_INFO_SELECT     = "select";
    public  static final String COMMAND_INFO_NEW        = "new";

    // submit
    public  static final String PARM_SUBMIT_EDIT        = "u_subedit";
    public  static final String PARM_SUBMIT_VIEW        = "u_subview";
    public  static final String PARM_SUBMIT_CHG         = "u_subchg";
    public  static final String PARM_SUBMIT_DEL         = "u_subdel";
    public  static final String PARM_SUBMIT_NEW         = "u_subnew";

    // buttons
    public  static final String PARM_BUTTON_CANCEL      = "u_btncan";
    public  static final String PARM_BUTTON_BACK        = "u_btnbak";

    // parameters
    public  static final String PARM_NEW_RPTJOB         = "u_newjob";
    public  static final String PARM_JOB_SELECT         = "u_job";
    public  static final String PARM_JOB_DESC           = "u_desc";
    public  static final String PARM_JOB_ACTIVE         = "u_active";
    public  static final String PARM_DEVICE_REPORT      = "u_devrpt";
    public  static final String PARM_GROUP_REPORT       = "u_grprpt";
    public  static final String PARM_DEVICE_ID          = "u_devid";
    public  static final String PARM_GROUP_ID           = "u_grpid";
    public  static final String PARM_FROM_TIME          = "u_frtime";
    public  static final String PARM_TO_TIME            = "u_totime";
    public  static final String PARM_RECIPIENTS         = "u_recip";
    public  static final String PARM_INTERVAL_TAG       = "u_tag";

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // WebPage interface
    
    public ReportJobAdmin()
    {
        this.setBaseURI(RequestProperties.TRACK_BASE_URI());
        this.setPageName(PAGE_RPTJOB_ADMIN);
        this.setPageNavigation(new String[] { PAGE_LOGIN, PAGE_MENU_TOP });
        this.setLoginRequired(true);
        //this.setCssDirectory("css");
    }

    // ------------------------------------------------------------------------

    /**
    *** Check page prerequisites
    **/
    public boolean isOkToDisplay(RequestProperties reqState)
    {
        // -- This page requires that the following criteria be met:
        // -   track.enableService=true
        // -   Account "allowWebService" is true

        /* check "track.enableService" */
        if (!RTConfig.getBoolean(DBConfig.PROP_track_enableService,false)) {
            Print.logWarn("Track web-services not enabled: " + DBConfig.PROP_track_enableService + "=false");
            return false;
        }

        /* check that web-services are allowed for the current Account */
        Account account = (reqState != null)? reqState.getCurrentAccount() : null;
        if (account == null) {
            // -- unlikely
            Print.logWarn("Account is null ...");
            return false;
        } else
        if (!Account.GetAllowWebService(account)) {
            // -- web-services not enabled for account, check "report" command override
            String cmdRpt = ServiceXML.CMD_report; // "report";
            String _rptEnable = RTConfig.getString(DBConfig.PROP_track_service_commandEnabled_+cmdRpt,"");
            if (StringTools.isBlank(_rptEnable)             || // assume blank means "always"
                _rptEnable.equalsIgnoreCase("always")       || 
                _rptEnable.equalsIgnoreCase("unconditional")  ) {
                // -- "report" command always enabled, continue
            } else {
                // -- Acount web-service disabled, and "report" not unconditionally allowed
                Print.logWarn("Account 'allowWebServices' disabled: " + account.getAccountID());
                return false;
            }
        }

        /* otherwise display page */
        return true;

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the menu name
    **/
    public String getMenuName(RequestProperties reqState)
    {
        return MenuBar.MENU_ADMIN;
    }

    /**
    *** Gets the menu description
    **/
    public String getMenuDescription(RequestProperties reqState, String parentMenuName)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(ReportJobAdmin.class);
        return super._getMenuDescription(reqState,i18n.getString("ReportJobAdmin.editMenuDesc","View/Edit ReportJob Information"));
    }

    /**
    *** Gets the menu help text
    **/
    public String getMenuHelp(RequestProperties reqState, String parentMenuName)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(ReportJobAdmin.class);
        return super._getMenuHelp(reqState,i18n.getString("ReportJobAdmin.editMenuHelp","View and Edit ReportJob information"));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the navigation description
    **/
    public String getNavigationDescription(RequestProperties reqState)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(ReportJobAdmin.class);
        return super._getNavigationDescription(reqState,i18n.getString("ReportJobAdmin.navDesc","ReportJob"));
    }

    /**
    *** Gets the navigation tab description
    **/
    public String getNavigationTab(RequestProperties reqState)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(ReportJobAdmin.class);
        return super._getNavigationTab(reqState,i18n.getString("ReportJobAdmin.navTab","ReportJob Admin"));
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a list of ACLs used by this page
    **/
    public String[] getChildAclList()
    {
        return _ACL_LIST;
    }

    // ------------------------------------------------------------------------

    /**
    *** Writes the HTML 
    **/
    public void writePage(
        final RequestProperties reqState,
        String pageMsg)
        throws IOException
    {
        final HttpServletRequest request = reqState.getHttpServletRequest();
        final PrivateLabel privLabel = reqState.getPrivateLabel(); // never null
        final I18N    i18n        = privLabel.getI18N(ReportJobAdmin.class);
        final Locale  locale      = reqState.getLocale();
        final String  devTitles[] = reqState.getDeviceTitles();
        final String  grpTitles[] = reqState.getDeviceGroupTitles();
        final Account currAcct    = reqState.getCurrentAccount();  // never null
        final String  currAcctID  = currAcct.getAccountID();       // never null
        final User    currUser    = reqState.getCurrentUser();     // may be null
        final String  pageName    = this.getPageName();
        String m = pageMsg;
        boolean error = false;

        /* List of ReportJobs */
        OrderedSet<String> rptJobList = null;
        try {
            boolean activeOnly = false; // get all ReportsJobs
            rptJobList = ReportJob.getReportJobIDsForAccount(currAcctID, activeOnly); // initial list
        } catch (DBException dbe) {
            rptJobList = new OrderedSet<String>();
        }

        /* selected ReportJob */
        String selRptJobID = AttributeTools.getRequestString(reqState.getHttpServletRequest(), PARM_JOB_SELECT, "");
        if (!StringTools.isBlank(selRptJobID) && !rptJobList.contains(selRptJobID)) {
            selRptJobID = "";
        }

        /* ReportJob db */
        ReportJob selRptJob = null;
        try {
            selRptJob = !selRptJobID.equals("")? 
                ReportJob.getReportJob(currAcct, selRptJobID) :  // may still be null
                null;
        } catch (DBException dbe) {
            // ignore
        }

        /* ACL allow edit/view */
        boolean allowNew     = privLabel.hasAllAccess(currUser, this.getAclName());
        boolean allowDelete  = allowNew;
        boolean allowEdit    = allowNew  || privLabel.hasWriteAccess(currUser, this.getAclName());
        boolean allowView    = allowEdit || privLabel.hasReadAccess(currUser, this.getAclName());
        //Print.logInfo("User '" + currUser + "' allowNew=" + allowNew + " [" + this.getAclName() + "]");

        /* submit buttons */
        String submitEdit    = AttributeTools.getRequestString(request, PARM_SUBMIT_EDIT, "");
        String submitView    = AttributeTools.getRequestString(request, PARM_SUBMIT_VIEW, "");
        String submitChange  = AttributeTools.getRequestString(request, PARM_SUBMIT_CHG , "");
        String submitNew     = AttributeTools.getRequestString(request, PARM_SUBMIT_NEW , "");
        String submitDelete  = AttributeTools.getRequestString(request, PARM_SUBMIT_DEL , "");

        /* command */
        String  rptJobCmd    = reqState.getCommandName();
        boolean listRptJobs  = false;
        boolean updRptJobs   = rptJobCmd.equals(COMMAND_INFO_UPDATE);
        boolean selRptJobs   = rptJobCmd.equals(COMMAND_INFO_SELECT);
        boolean newRptJob    = rptJobCmd.equals(COMMAND_INFO_NEW);
        boolean delRptJob    = false;
        boolean editRptJob   = false;
        boolean viewRptJob   = false;

        /* ui display */
        boolean uiList       = false;
        boolean uiEdit       = false;
        boolean uiView       = false;

        /* sub-command */
        String newRptJobID = null;
        if (newRptJob) {
            if (!allowNew) {
               newRptJob = false; // not authorized
            } else {
                newRptJobID = AttributeTools.getRequestString(request,PARM_NEW_RPTJOB,"").trim();
                newRptJobID = newRptJobID.toLowerCase();
                if (StringTools.isBlank(newRptJobID)) {
                    m = i18n.getString("ReportJobAdmin.enterNewnewReportJob","Please enter a new ReportJob ID.");
                    error = true;
                    newRptJob = false;
                } else
                if (!WebPageAdaptor.isValidID(reqState,newRptJobID)) {
                    m = i18n.getString("ReportJobAdmin.invalidIDChar","ID contains invalid characters");
                    error = true;
                    newRptJob = false;
                }
            }
        } else
        if (updRptJobs) {
            if (!allowEdit) {
                // not authorized to update report jobs
                updRptJobs = false;
            } else
            if (!SubmitMatch(submitChange,i18n.getString("ReportJobAdmin.change","Change"))) {
                updRptJobs = false;
            } else
            if (selRptJob == null) {
                // should not occur
                m = i18n.getString("ReportJobAdmin.unableToUpdate","Unable to update ReportJob, ID not found");
                error = true;
                updRptJobs = false;
            }
        } else
        if (selRptJobs) {
            if (SubmitMatch(submitDelete,i18n.getString("ReportJobAdmin.delete","Delete"))) {
                if (!allowDelete) {
                    delRptJob = false;
                } else
                if (selRptJob == null) {
                    m = i18n.getString("ReportJobAdmin.pleaseSelectEntity","Please select a ReportJob");
                    error = true;
                    delRptJob = false; // not selected
                } else {
                    delRptJob = true;
                }
            } else
            if (SubmitMatch(submitEdit,i18n.getString("ReportJobAdmin.edit","Edit"))) {
                if (!allowEdit) {
                    uiEdit = false; // not authorized
                } else
                if (selRptJob == null) {
                    m = i18n.getString("ReportJobAdmin.pleaseSelectReportJob","Please select a ReportJob");
                    error = true;
                    uiEdit = false; // not selected
                } else {
                    uiEdit = true;
                }
            } else
            if (SubmitMatch(submitView,i18n.getString("ReportJobAdmin.view","View"))) {
                if (!allowView) {
                    uiView = false; // not authorized
                } else
                if (selRptJob == null) {
                    m = i18n.getString("ReportJobAdmin.pleaseSelectReportJob","Please select a ReportJob");
                    error = true;
                    uiView = false;
                } else {
                    uiView = true;
                }
            } else {
                uiList = true;
            }
        } else {
            uiList = true;
        }

        /* delete ReportJob? */
        if (delRptJob) {
            try {
                ReportJob.Key rptJobKey = (ReportJob.Key)selRptJob.getRecordKey();
                Print.logWarn("Deleting ReportJob: " + rptJobKey);
                rptJobKey.delete(true); // will also delete dependencies
                selRptJobID = "";
                selRptJob = null;
                // select another ReportJob
                boolean activeOnly = false; // get all ReportsJobs
                rptJobList = ReportJob.getReportJobIDsForAccount(currAcctID, activeOnly); // delete refresh
                if (!ListTools.isEmpty(rptJobList)) {
                    selRptJobID = rptJobList.get(0);
                    try {
                        selRptJob = !selRptJobID.equals("")? 
                            ReportJob.getReportJob(currAcct, selRptJobID) : 
                            null;
                    } catch (DBException dbe) {
                        // ignore
                    }
                }
            } catch (DBException dbe) {
                Print.logException("Deleting Reportjob", dbe);
                m = i18n.getString("ReportJobAdmin.errorDelete","Internal error deleting Reportjob");
                error = true;
            }
            uiList = true;
        }

        /* new ReportJob? */
        if (newRptJob) {
            boolean createRptJobOK = true;
            for (int u = 0; u < rptJobList.size(); u++) {
                if (newRptJobID.equalsIgnoreCase(rptJobList.get(u))) {
                    m = i18n.getString("ReportJobAdmin.alreadyExists","This Reportjob already exists");
                    error = true;
                    createRptJobOK = false;
                    break;
                }
            }
            if (createRptJobOK) {
                try {
                    ReportJob rptJob = ReportJob.createNewReportJob(currAcct, newRptJobID); // already saved
                    boolean activeOnly = false; // get all ReportsJobs
                    rptJobList = ReportJob.getReportJobIDsForAccount(currAcctID, activeOnly); // create refresh
                    selRptJob = rptJob;
                    selRptJobID = rptJob.getReportJobID();
                    Print.logInfo("Created ReportJob '%s'", selRptJobID);
                    m = i18n.getString("ReportJobAdmin.createdReportjob","New ReportJob has been created");
                } catch (DBException dbe) {
                    Print.logException("Creating ReportJob", dbe);
                    m = i18n.getString("ReportJobAdmin.errorCreate","Internal error creating ReportJob");
                    error = true;
                }
            }
            uiList = true;
        }

        /* change/update the Reportjob info? */
        if (updRptJobs) {
            selRptJob.clearChanged();
            String jobDesc     = AttributeTools.getRequestString(request, PARM_JOB_DESC      , "");
            String jobActive   = AttributeTools.getRequestString(request, PARM_JOB_ACTIVE    , "");
            String recipients  = AttributeTools.getRequestString(request, PARM_RECIPIENTS    , "");
            String intervalTag = AttributeTools.getRequestString(request, PARM_INTERVAL_TAG  , "");
          //String fromTime    = AttributeTools.getRequestString(request, PARM_FROM_TIME     , "");
          //String toTime      = AttributeTools.getRequestString(request, PARM_TO_TIME       , "");
            String devReport   = AttributeTools.getRequestString(request, PARM_DEVICE_REPORT , "");
            String deviceID    = AttributeTools.getRequestString(request, PARM_DEVICE_ID     , "");
            String grpReport   = AttributeTools.getRequestString(request, PARM_GROUP_REPORT  , "");
            String groupID     = AttributeTools.getRequestString(request, PARM_GROUP_ID      , "");
            try {
                boolean saveOK = true;
                // active
                boolean jobActv = ComboOption.parseYesNoText(locale, jobActive, true);
                if (selRptJob.getIsActive() != jobActv) { 
                    selRptJob.setIsActive(jobActv); 
                }
                // description
                selRptJob.setDescription(jobDesc);
                // recipients
                selRptJob.setRecipients(recipients);
                // interval tag
                selRptJob.setIntervalTag(intervalTag);
                selRptJob.setReportTimeFrom(null);
                selRptJob.setReportTimeTo  (null);
                Map<String,ReportJob.IntervalTag> tagMap = ReportJob.GetIntervalTagMap();
                for (String tagID : tagMap.keySet()) {
                    if (intervalTag.equalsIgnoreCase(tagID)) {
                        ReportJob.IntervalTag rit = tagMap.get(tagID);
                        selRptJob.setReportTimeFrom(rit.getFromTime());
                        selRptJob.setReportTimeTo  (rit.getToTime()  );
                        break;
                    }
                }

                // Device/Group report name:option
                if (FLEET_REPORTS_ONLY) {
                    int rop = grpReport.indexOf(":");
                    String rptName = (rop >= 0)? grpReport.substring(0,rop) : grpReport;
                    String rptOpt  = (rop >= 0)? grpReport.substring(rop+1) : "";
                    selRptJob.setReportName(rptName);
                    selRptJob.setReportOption(rptOpt);
                    selRptJob.setGroupID(groupID);
                    selRptJob.setDeviceID(null);
                } else {
                    int rop = devReport.indexOf(":");
                    String rptName = (rop >= 0)? devReport.substring(0,rop) : devReport;
                    String rptOpt  = (rop >= 0)? devReport.substring(rop+1) : "";
                    selRptJob.setReportName(rptName);
                    selRptJob.setReportOption(rptOpt);
                    selRptJob.setDeviceID(deviceID);
                    selRptJob.setGroupID(null);
                }
                // save
                if (saveOK) {
                    selRptJob.save();
                    m = i18n.getString("ReportJobAdmin.reportJobUpdated","ReportJob information updated");
                } else {
                    // should stay on this page
                }
            } catch (Throwable t) {
                Print.logException("Updating ReportJob", t);
                m = i18n.getString("ReportJobAdmin.errorUpdate","Internal error updating ReportJob");
                error = true;
                //return;
            }
            uiList = true;
        }

        /* Style */
        HTMLOutput HTML_CSS = new HTMLOutput() {
            public void write(PrintWriter out) throws IOException {
                String cssDir = ReportJobAdmin.this.getCssDirectory();
                WebPageAdaptor.writeCssLink(out, reqState, "ReportJobAdmin.css", cssDir);
            }
        };

        /* JavaScript */
        HTMLOutput HTML_JS = new HTMLOutput() {
            public void write(PrintWriter out) throws IOException {
                MenuBar.writeJavaScript(out, pageName, reqState);
                JavaScriptTools.writeJSInclude(out, JavaScriptTools.qualifyJSFileRef(SORTTABLE_JS), request);
            }
        };

        /* Content */
        final OrderedSet<String> _rptJobList = rptJobList;
        final ReportJob _selRptJob    = selRptJob;
        final boolean   _allowEdit    = allowEdit;
        final boolean   _allowView    = allowView;
        final boolean   _allowNew     = allowNew;
        final boolean   _allowDelete  = allowDelete;
        final boolean   _uiEdit       = _allowEdit && uiEdit;
        final boolean   _uiView       = _uiEdit || uiView;
        final boolean   _uiList       = uiList || (!_uiEdit && !_uiView);
        HTMLOutput HTML_CONTENT = null;
        if (_uiList) {
            final String _selRptJobID = (selRptJobID.equals("") && (rptJobList.size() > 0))? rptJobList.get(0) : selRptJobID;

            HTML_CONTENT = new HTMLOutput(CommonServlet.CSS_CONTENT_FRAME, m) {
                public void write(PrintWriter out) throws IOException {
                    String pageName = ReportJobAdmin.this.getPageName();

                    // frame header
                  //String menuURL    = EncodeMakeURL(reqState,RequestProperties.TRACK_BASE_URI(),PAGE_MENU_TOP);
                    String menuURL    = privLabel.getWebPageURL(reqState, PAGE_MENU_TOP);
                    String editURL    = ReportJobAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String selectURL  = ReportJobAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String newURL     = ReportJobAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String frameTitle = _allowEdit? 
                        i18n.getString("ReportJobAdmin.viewEditReportJob","View/Edit ReportJob Information") : 
                        i18n.getString("ReportJobAdmin.viewReportJob","View ReportJob Information");
                    out.write("<span class='"+CommonServlet.CSS_MENU_TITLE+"'>"+frameTitle+"</span><br/>\n");
                    out.write("<hr>\n");

                    // ReportJob selection table (Select, ReportJob, ReportJob Desc)
                    out.write("<h1 class='"+CommonServlet.CSS_ADMIN_SELECT_TITLE+"'>"+i18n.getString("ReportJobAdmin.selectReportJob","Select a ReportJob")+":</h1>\n");
                    out.write("<div style='margin-left:25px;'>\n");
                    out.write("<form name='"+FORM_RPTJOB_SELECT+"' method='post' action='"+selectURL+"' target='_self'>"); // target='_top'
                    out.write("<input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_SELECT+"'/>");
                    out.write("<table class='"+CommonServlet.CSS_ADMIN_SELECT_TABLE+"' cellspacing=0 cellpadding=0 border=0>\n");
                    out.write(" <thead>\n");
                    out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_ROW+"'>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL_SEL+"' nowrap>"+FilterText(i18n.getString("ReportJobAdmin.select"   ,"Select"  ))+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("ReportJobAdmin.jobID"    ,"Job ID"  ))+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("ReportJobAdmin.jobDesc"  ,"Job Desc"))+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("ReportJobAdmin.jobTag"   ,"Tag"     ))+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+FilterText(i18n.getString("ReportJobAdmin.jobActive","Active"  ))+"</th>\n");
                    out.write("  </tr>\n");
                    out.write(" </thead>\n");
                    out.write(" <tbody>\n");
                    for (int u = 0; u < _rptJobList.size(); u++) {
                        String jobid = _rptJobList.get(u);
                        if ((u & 1) == 0) {
                            out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_ROW_ODD+"'>\n");
                        } else {
                            out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_ROW_EVEN+"'>\n");
                        }
                        try {
                            ReportJob rptJob = ReportJob.getReportJob(currAcct, jobid);
                            if (rptJob != null) {
                                String uid      = rptJob.getReportJobID();
                                String jobID    = FilterText(uid);
                                String jobDesc  = FilterText(rptJob.getDescription());
                                String jobTag   = FilterText(ReportJob.GetIntervalTagDescription(rptJob.getIntervalTag(),locale));
                                String jobActv  = FilterText(ComboOption.getYesNoText(locale,rptJob.isActive()));
                                String checked  = _selRptJobID.equals(uid)? " checked" : "";
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL_SEL+"' "+SORTTABLE_SORTKEY+"='"+u+"'><input type='radio' name='"+PARM_JOB_SELECT+"' id='"+jobID+"' value='"+jobID+"' "+checked+"></td>\n");
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap><label for='"+jobID+"'>"+jobID+"</label></td>\n");
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+jobDesc+"</td>\n");
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+jobTag +"</td>\n");
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+jobActv+"</td>\n");
                            }
                        } catch (DBException dbe) {
                            // 
                        }
                        out.write("  </tr>\n");
                    }
                    out.write(" </tbody>\n");
                    out.write("</table>\n");
                    out.write("<table cellpadding='0' cellspacing='0' border='0' style='width:95%; margin-top:5px; margin-left:5px; margin-bottom:5px;'>\n");
                    out.write("<tr>\n");
                    if (_allowView  ) { 
                        out.write("<td style='padding-left:5px;'>");
                        out.write("<input type='submit' name='"+PARM_SUBMIT_VIEW+"' value='"+i18n.getString("ReportJobAdmin.view","View")+"'>");
                        out.write("</td>\n"); 
                    }
                    if (_allowEdit  ) { 
                        out.write("<td style='padding-left:5px;'>");
                        out.write("<input type='submit' name='"+PARM_SUBMIT_EDIT+"' value='"+i18n.getString("ReportJobAdmin.edit","Edit")+"'>");
                        out.write("</td>\n"); 
                    }
                    out.write("<td style='width:100%; text-align:right; padding-right:10px;'>");
                    if (_allowDelete) {
                        out.write("<input type='submit' name='"+PARM_SUBMIT_DEL+"' value='"+i18n.getString("ReportJobAdmin.delete","Delete")+"' "+Onclick_ConfirmDelete(locale)+">");
                    } else {
                        out.write("&nbsp;"); 
                    }
                    out.write("</td>\n"); 
                    out.write("</tr>\n");
                    out.write("</table>\n");
                    out.write("</form>\n");
                    out.write("</div>\n");
                    out.write("<hr>\n");

                    /* new ReportJob */
                    if (_allowNew) {
                    out.write("<h1 class='"+CommonServlet.CSS_ADMIN_SELECT_TITLE+"'>"+i18n.getString("ReportJobAdmin.createNewReportJob","Create a new ReportJob")+":</h1>\n");
                    out.write("<div style='margin-top:5px; margin-left:5px; margin-bottom:5px;'>\n");
                    out.write("<form name='"+FORM_RPTJOB_NEW+"' method='post' action='"+newURL+"' target='_self'>"); // target='_top'
                    out.write(" <input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_NEW+"'/>");
                    out.write(i18n.getString("ReportJobAdmin.reportjobID","ReportJob ID")+": <input type='text' class='"+CommonServlet.CSS_TEXT_INPUT+"' class='"+CommonServlet.CSS_TEXT_INPUT+"' name='"+PARM_NEW_RPTJOB+"' value='' size='32' maxlength='32'><br>\n");
                    out.write(" <input type='submit' name='"+PARM_SUBMIT_NEW+"' value='"+i18n.getString("ReportJobAdmin.new","New")+"' style='margin-top:5px; margin-left:10px;'>\n");
                    out.write("</form>\n");
                    out.write("</div>\n");
                    out.write("<hr>\n");
                    }
                }
            };
        } else
        if (_uiEdit || _uiView) {
            final String _selRptJobID = selRptJobID;

            HTML_CONTENT = new HTMLOutput(CommonServlet.CSS_CONTENT_FRAME, m) {
                public void write(PrintWriter out) throws IOException {
                    String pageName = ReportJobAdmin.this.getPageName();

                    // frame header
                  //String menuURL    = EncodeMakeURL(reqState,RequestProperties.TRACK_BASE_URI(),PAGE_MENU_TOP);
                    String menuURL    = privLabel.getWebPageURL(reqState, PAGE_MENU_TOP);
                    String editURL    = ReportJobAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String selectURL  = ReportJobAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String newURL     = ReportJobAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                    String frameTitle = _allowEdit? 
                        i18n.getString("ReportJobAdmin.viewEditReportJob","View/Edit ReportJob Information") : 
                        i18n.getString("ReportJobAdmin.viewReportJob","View ReportJob Information");
                    out.write("<span class='"+CommonServlet.CSS_MENU_TITLE+"'>"+frameTitle+"</span><br/>\n");
                    out.write("<hr>\n");

                    /* start of form */
                    out.write("<form name='"+FORM_RPTJOB_EDIT+"' method='post' action='"+editURL+"' target='_self'>\n"); // target='_top'
                    out.write("  <input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_UPDATE+"'/>\n");

                    /* cron tag options */
                    ComboMap tagCombo = new ComboMap();
                    Map<String,ReportJob.IntervalTag> tagMap = ReportJob.GetIntervalTagMap();
                    for (String tagID : tagMap.keySet()) {
                        String desc = ReportJob.GetIntervalTagDescription(tagID, locale);
                        tagCombo.add(new ComboOption(tagID,desc));
                    }
                    String selTag = (_selRptJob != null)? _selRptJob.getIntervalTag() : "";

                    /* reports */
                    Map<String,String> rptDeviceMap = new OrderedMap<String,String>();
                    Map<String,String> rptGroupMap  = new OrderedMap<String,String>();
                    rptDeviceMap.put(":", "---"); // i18n.getString("ReportJobAdmin.noReportName","None"));
                    rptGroupMap .put(":", "---"); // i18n.getString("ReportJobAdmin.noReportName","None"));
                    //Collection<ReportFactory> rptFactList = ReportFactory.getReportFactories();
                    Map<String,ReportEntry> rptEntries = privLabel.getReportMap();
                    if (!ListTools.isEmpty(rptEntries)) {
                        for (String ren : rptEntries.keySet()) {
                            ReportEntry    re = rptEntries.get(ren);
                            ReportFactory  rf = re.getReportFactory();
                            String        acl = re.getAclName();
                            boolean    saOnly = rf.isSysAdminOnly();
                            if (saOnly && !Account.isSystemAdmin(currAcct)) { continue; }
                            if (!privLabel.hasReadAccess(currUser,acl)) { continue; }
                            String   rptName  = rf.getReportName();
                            String  rptTitle  = rf.getReportTitle(locale, "");
                            String   rptType  = rf.getReportType();
                            boolean  isGroup  = rf.getReportTypeIsGroup();
                            boolean  isDevice = rf.getReportTypeIsDevice();
                            String  typeTitle = ReportFactory.getReportTypeShortTitle(reqState, rptType);
                            OrderedMap<String,ReportOption> rptOpt = rf.getReportOptionMap(reqState);
                            if (ListTools.isEmpty(rptOpt)) {
                                String title = ReportLayout.expandHeaderText(rptTitle, reqState, null);
                                String desc  = typeTitle + ": " + title;
                                if (isGroup) {
                                    rptGroupMap .put(rptName, desc);
                                } else
                                if (isDevice) {
                                    rptDeviceMap.put(rptName, desc);
                                }
                            } else {
                                for (String optName : rptOpt.keySet()) {
                                    ReportOption ro = rptOpt.get(optName);
                                    String title = ReportLayout.expandHeaderText(rptTitle, reqState, ro);
                                    String desc  = typeTitle + ": " + title;
                                    if (isGroup) {
                                        rptGroupMap .put(rptName + ":" + optName, desc);
                                    } else
                                    if (isDevice) {
                                        rptDeviceMap.put(rptName + ":" + optName, desc);
                                    }
                                }
                            }
                        }
                    }
                    ComboMap rptDeviceCombo = new ComboMap(rptDeviceMap);
                    ComboMap rptGroupCombo  = new ComboMap(rptGroupMap);

                    /* current Report name:option, group */
                    String rptName = (_selRptJob != null)? _selRptJob.getReportName()   : "";
                    String rptOpt  = (_selRptJob != null)? _selRptJob.getReportOption() : "";
                    String rptNameOpt = rptName;
                    if (!StringTools.isBlank(rptOpt)) {
                        rptNameOpt += ":" + rptOpt;
                    }
                    ReportFactory rptFact = !StringTools.isBlank(rptName)? ReportFactory.getReportFactory(rptName) : null;
                    //boolean rptIsGroup  = (rptFact != null)? rptFact.getReportTypeIsGroup() : false;
                    //boolean rptIsDevice = (rptFact != null)? rptFact.getReportTypeIsDevice() : false;

                    /* group list */
                    ComboMap grpCombo = new ComboMap(reqState.createGroupDescriptionMap(true/*includeID*/,true/*inclAll*/));
                    String grpSel = (_selRptJob != null)? _selRptJob.getGroupID() : "";

                    /* device list */
                  //ComboMap devCombo = new ComboMap(reqState.createDeviceDescriptionMap(true/*includeID*/,true/*inclAll*/));
                    String devSel = (_selRptJob != null)? _selRptJob.getDeviceID() : "";

                    /* ReportJob fields */
                    ComboOption rptJobActive = ComboOption.getYesNoOption(locale, ((_selRptJob != null) && _selRptJob.isActive()));
                    out.println("<table class='"+CommonServlet.CSS_ADMIN_VIEW_TABLE+"' cellspacing='0' callpadding='0' border='0'>");
                    out.println(FormRow_TextField(PARM_JOB_SELECT    , false  , i18n.getString("ReportJobAdmin.reportJobID","ReportJob ID")+":"            , _selRptJobID, 40, 40));
                    out.println(FormRow_ComboBox (PARM_JOB_ACTIVE    , _uiEdit, i18n.getString("ReportJobAdmin.active","Active")+":", rptJobActive, ComboMap.getYesNoMap(locale), "", -1));
                    out.println(FormRow_TextField(PARM_JOB_DESC      , _uiEdit, i18n.getString("ReportJobAdmin.reportJobDescription","ReportJob Description")+":" , (_selRptJob!=null)?_selRptJob.getDescription() :"", 75, 75));
                    out.println(FormRow_ComboBox (PARM_INTERVAL_TAG  , _uiEdit, i18n.getString("ReportJobAdmin.intervalTag","Interval Tag")+":", selTag, tagCombo, "", -1));
                  //out.println(FormRow_TextField(PARM_FROM_TIME     , _uiEdit, i18n.getString("ReportJobAdmin.reportFromTime","Report From Time")+":" , (_selRptJob!=null)?_selRptJob.getReportTimeFrom() :"", 32, 32));
                  //out.println(FormRow_TextField(PARM_TO_TIME       , _uiEdit, i18n.getString("ReportJobAdmin.reportToTime","Report To Time")+":" , (_selRptJob!=null)?_selRptJob.getReportTimeTo() :"", 32, 32));
                    out.println(FormRow_TextField(PARM_RECIPIENTS    , _uiEdit, i18n.getString("ReportJobAdmin.recipients","Report Recipients")+":" , (_selRptJob!=null)?_selRptJob.getRecipients() :"", 110, 180));

                    if (FLEET_REPORTS_ONLY) {
                    out.println(FormRow_Separator());
                    out.println(FormRow_ComboBox (PARM_GROUP_REPORT  , _uiEdit, i18n.getString("ReportJobAdmin.reportName","{0} Report",grpTitles)+":" , rptNameOpt, rptGroupCombo, "", -1));
                    out.println(FormRow_ComboBox (PARM_GROUP_ID      , _uiEdit, i18n.getString("ReportJobAdmin.groupID","{0} ID",grpTitles)+":", grpSel, grpCombo, "", -1));
                    } else {
                    out.println(FormRow_Separator());
                    out.println(FormRow_ComboBox (PARM_DEVICE_REPORT , _uiEdit, i18n.getString("ReportJobAdmin.reportName","{0} Report",devTitles)+":" , rptNameOpt, rptDeviceCombo, "", -1));
                    out.println(FormRow_TextField(PARM_DEVICE_ID     , _uiEdit, i18n.getString("ReportJobAdmin.deviceID","{0} ID",devTitles)+":" , devSel, 24, 24));
                    }

                    /* end table */
                    out.println("</table>");

                    /* end of form */
                    out.write("<hr style='margin-bottom:5px;'>\n");
                    out.write("<span style='padding-left:10px'>&nbsp;</span>\n");
                    if (_uiEdit) {
                        out.write("<input type='submit' name='"+PARM_SUBMIT_CHG+"' value='"+i18n.getString("ReportJobAdmin.change","Change")+"'>\n");
                        out.write("<span style='padding-left:10px'>&nbsp;</span>\n");
                        out.write("<input type='button' name='"+PARM_BUTTON_CANCEL+"' value='"+i18n.getString("ReportJobAdmin.cancel","Cancel")+"' onclick=\"javascript:openURL('"+editURL+"','_self');\">\n"); // target='_top'
                    } else {
                        out.write("<input type='button' name='"+PARM_BUTTON_BACK+"' value='"+i18n.getString("ReportJobAdmin.back","Back")+"' onclick=\"javascript:openURL('"+editURL+"','_self');\">\n"); // target='_top'
                    }
                    out.write("</form>\n");

                }
            };
            
        }

        /* write frame */
        String onload = error? JS_alert(true,m) : null;
        CommonServlet.writePageFrame(
            reqState,
            onload,null,                // onLoad/onUnload
            HTML_CSS,                   // Style sheets
            HTML_JS,                    // Javascript
            null,                       // Navigation
            HTML_CONTENT);              // Content

    }

    // ------------------------------------------------------------------------
}
