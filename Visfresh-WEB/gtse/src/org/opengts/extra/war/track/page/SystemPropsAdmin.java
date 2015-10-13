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
//  2013/11/11  Martin D. Flynn
//     -Initial release (cloned from StatusCodeInfo.java)
// ----------------------------------------------------------------------------
package org.opengts.extra.war.track.page;

import java.util.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.*;
import org.opengts.db.tables.*;

import org.opengts.war.tools.*;
import org.opengts.war.track.*;

public class SystemPropsAdmin
    extends WebPageAdaptor
    implements Constants
{

    // ------------------------------------------------------------------------
    // Parameters

    // forms 
    public  static final String FORM_PROP_SELECT        = "SysPropsSelect";
    public  static final String FORM_PROP_EDIT          = "SysPropsEdit";
    public  static final String FORM_PROP_NEW           = "SysPropsNew";

    // commands
    public  static final String COMMAND_INFO_UPDATE     = "update";
    public  static final String COMMAND_INFO_SELECT     = "select";
    public  static final String COMMAND_INFO_NEW        = "new";

    // submit
    public  static final String PARM_SUBMIT_EDIT        = "c_subedit";
    public  static final String PARM_SUBMIT_VIEW        = "c_subview";
    public  static final String PARM_SUBMIT_CHG         = "c_subchg";
    public  static final String PARM_SUBMIT_DEL         = "c_subdel";
    public  static final String PARM_SUBMIT_NEW         = "c_subnew";

    // buttons
    public  static final String PARM_BUTTON_CANCEL      = "p_btncan";
    public  static final String PARM_BUTTON_BACK        = "p_btnbak";

    // parameters
    public  static final String PARM_NEW_PROP           = "p_newprop";
    public  static final String PARM_PROP_SELECT        = "p_prop";
    public  static final String PARM_PROP_DESC          = "p_desc";
    public  static final String PARM_PROP_TYPE          = "p_type";
    public  static final String PARM_PROP_VALUE         = "p_value";

    // ------------------------------------------------------------------------
    // WebPage interface
    
    public SystemPropsAdmin()
    {
        this.setBaseURI(RequestProperties.TRACK_BASE_URI());
        this.setPageName(PAGE_SYSPROP_ADMIN);
        this.setPageNavigation(new String[] { PAGE_LOGIN, PAGE_MENU_TOP });
        this.setLoginRequired(true);
    }

    // ------------------------------------------------------------------------
   
    public String getMenuName(RequestProperties reqState)
    {
        return MenuBar.MENU_ADMIN;
    }

    public String getMenuDescription(RequestProperties reqState, String parentMenuName)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(SystemPropsAdmin.class);
        return super._getMenuDescription(reqState,i18n.getString("SystemPropsAdmin.editMenuDesc","View/Edit SystemProperty Keys"));
    }
   
    public String getMenuHelp(RequestProperties reqState, String parentMenuName)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(SystemPropsAdmin.class);
        return super._getMenuHelp(reqState,i18n.getString("SystemPropsAdmin.editMenuHelp","View and Edit SystemProperty Keys"));
    }
    
    // ------------------------------------------------------------------------

    public String getNavigationDescription(RequestProperties reqState)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(SystemPropsAdmin.class);
        return super._getNavigationDescription(reqState,i18n.getString("SystemPropsAdmin.navDesc","SystemProperty"));
    }

    public String getNavigationTab(RequestProperties reqState)
    {
        PrivateLabel privLabel = reqState.getPrivateLabel();
        I18N i18n = privLabel.getI18N(SystemPropsAdmin.class);
        return i18n.getString("SystemPropsAdmin.navTab","SystemProp Admin");
    }

    // ------------------------------------------------------------------------

    /* true if this page iis for the system admin only */
    public boolean systemAdminOnly()
    {
        return true;
    }

    // ------------------------------------------------------------------------

    private String filterBlank(String s)
    {
        if (StringTools.isBlank(s)) {
            return StringTools.HTML_SP;
        } else {
            return s;
        }
    }

    private String filterText(String s)
    {
        if (StringTools.isBlank(s)) {
            return StringTools.HTML_SP;
        } else {
            return StringTools.htmlFilterText(s);
        }
    }

    public void writePage(
        final RequestProperties reqState,
        String pageMsg)
        throws IOException
    {
        final HttpServletRequest request = reqState.getHttpServletRequest();
        final PrivateLabel privLabel = reqState.getPrivateLabel(); // never null
        final I18N    i18n           = privLabel.getI18N(SystemPropsAdmin.class);
        final Locale  locale         = reqState.getLocale();
        final Account currAcct       = reqState.getCurrentAccount(); // never null
        final String  currAcctID     = currAcct.getAccountID(); // never null
        final User    currUser       = reqState.getCurrentUser(); // may be null
        final String  pageName       = this.getPageName();
        String m = pageMsg;
        boolean error = false;

        /* list of properties */
        String propList[] = null;
        try {
            propList = SystemProps.getSystemPropsKeyArray();
        } catch (DBException dbe) {
            propList = new String[0];
        }

        /* selected prop */
        String selPropID = AttributeTools.getRequestString(request, PARM_PROP_SELECT, "");
        if (StringTools.isBlank(selPropID)) {
            if ((propList.length > 0) && (propList[0] != null)) {
                selPropID = propList[0];
            } else {
                selPropID = "";
            }
        }
        if (propList.length == 0) {
            propList = new String[] { selPropID };
        }

        /* SystemProps db */
        SystemProps selProp = null;
        try {
            selProp = SystemProps.getProperty(selPropID); // may still be null
        } catch (DBException dbe) {
            // ignore
        }
        boolean readOnly = (selProp != null)? selProp.isReadOnly() : false;

        /* ACL allow edit/view */
        boolean allowNew    = privLabel.hasAllAccess(currUser, this.getAclName());
        boolean allowDelete = allowNew;
        boolean allowEdit   = allowNew  || privLabel.hasWriteAccess(currUser, this.getAclName());
        boolean allowView   = allowEdit || privLabel.hasReadAccess(currUser, this.getAclName());

        /* command */
        String  propCmd     = reqState.getCommandName();
        boolean listProps   = false;
        boolean updateProp  = propCmd.equals(COMMAND_INFO_UPDATE);
        boolean selectProp  = propCmd.equals(COMMAND_INFO_SELECT);
        boolean newProp     = propCmd.equals(COMMAND_INFO_NEW);
        boolean deleteProp  = false;
        boolean editProp    = false;
        boolean viewProp    = false;

        /* submit buttons */
        String submitEdit   = AttributeTools.getRequestString(request, PARM_SUBMIT_EDIT, "");
        String submitView   = AttributeTools.getRequestString(request, PARM_SUBMIT_VIEW, "");
        String submitChange = AttributeTools.getRequestString(request, PARM_SUBMIT_CHG , "");
        String submitNew    = AttributeTools.getRequestString(request, PARM_SUBMIT_NEW , "");
        String submitDelete = AttributeTools.getRequestString(request, PARM_SUBMIT_DEL , "");

        /* sub-command */
        if (newProp) {
            if (!allowNew) {
               newProp = false; // not authorized
            } else {
                String propStr = AttributeTools.getRequestString(request, PARM_NEW_PROP, "");
                if (StringTools.isBlank(propStr)) {
                    m = i18n.getString("SystemPropsAdmin.enterNewProp","Please enter a valid new SystemProperty Key."); // UserErrMsg
                    error = true;
                    newProp = false;
                }
            }
        } else
        if (updateProp) {
            if (!allowEdit) {
                // not authorized to update props
                updateProp = false;
            } else
            if (!SubmitMatch(submitChange,i18n.getString("SystemPropsAdmin.change","Change"))) {
                updateProp = false;
            } else
            if (selProp == null) {
                // should not occur
                m = i18n.getString("SystemPropsAdmin.unableToUpdate","Unable to update SystemProperty, key not found");
                error = true;
                updateProp = false;
            }
        } else
        if (selectProp) {
            if (SubmitMatch(submitDelete,i18n.getString("SystemPropsAdmin.delete","Delete"))) {
                if (allowDelete) {
                    deleteProp = true;
                }
            } else
            if (SubmitMatch(submitEdit,i18n.getString("SystemPropsAdmin.edit","Edit"))) {
                if (allowEdit) {
                    if (selProp == null) {
                        m = i18n.getString("SystemPropsAdmin.pleaseSelectProp","Please select a SystemProperty key"); // UserErrMsg
                        error = true;
                        listProps = true;
                    } else {
                        editProp = !readOnly;
                        viewProp = true;
                    }
                }
            } else
            if (SubmitMatch(submitView,i18n.getString("SystemPropsAdmin.view","View"))) {
                if (allowView) {
                    if (selProp == null) {
                        m = i18n.getString("SystemPropsAdmin.pleaseSelectProp","Please select a SystemProperty key"); // UserErrMsg
                        error = true;
                        listProps = true;
                    } else {
                        viewProp = true;
                    }
                }
            } else {
                listProps = true;
            }
        } else {
            listProps = true;
        }

        /* delete SystemProps key? */
        if (deleteProp) {
            if (selProp == null) {
                m = i18n.getString("SystemPropsAdmin.pleaseSelectProp","Please select a SystemProperty key"); // UserErrMsg
                error = true;
            } else {
                try {
                    SystemProps.Key propKey = (SystemProps.Key)selProp.getRecordKey();
                    Print.logWarn("Deleting SystemProperty key: " + propKey);
                    propKey.delete(true); // will also delete dependencies
                    selPropID = null;
                    selProp = null;
                    propList = SystemProps.getSystemPropsKeyArray();
                    if (propList.length > 0) {
                        selPropID = propList[0];
                        try {
                            selProp = SystemProps.getProperty(selPropID);
                        } catch (DBException dbe) {
                            // ignore
                        }
                    }
                } catch (DBException dbe) {
                    Print.logException("Deleting SystemProperty key", dbe);
                    m = i18n.getString("SystemPropsAdmin.errorDelete","Internal error deleting SystemProperty key"); // UserErrMsg
                    error = true;
                }
            }
            listProps = true;
        }

        /* new prop? */
        if (newProp) {
            boolean createPropOK = true;
            String newPropID = AttributeTools.getRequestString(request, PARM_NEW_PROP, "");
            for (int u = 0; u < propList.length; u++) {
                if (newPropID.equalsIgnoreCase(propList[u])) {
                    m = i18n.getString("SystemPropsAdmin.alreadyExists","This SystemProperty key already exists"); // UserErrMsg
                    error = true;
                    createPropOK = false;
                    break;
                }
            }
            if (createPropOK) {
                try {
                    SystemProps prop = SystemProps.createNewProperty(newPropID,null/*type*/,null/*desc*/); // saved
                    propList = SystemProps.getSystemPropsKeyArray();
                    selProp = prop;
                    selPropID = newPropID;
                    m = i18n.getString("SystemPropsAdmin.createdProp","New SystemProperty key has been created"); // UserErrMsg
                } catch (DBException dbe) {
                    Print.logException("Creating SystemProps key", dbe);
                    m = i18n.getString("SystemPropsAdmin.errorCreate","Internal error creating SystemProperty key"); // UserErrMsg
                    error = true;
                }
            }
            listProps = true;
        }

        /* change/update the SystemProps key? */
        if (updateProp) {
            // 'selProp' guaranteed non-null here
            String propDesc = AttributeTools.getRequestString(request, PARM_PROP_DESC , "");
            String propType = AttributeTools.getRequestString(request, PARM_PROP_TYPE , "");
            String propVal  = AttributeTools.getRequestString(request, PARM_PROP_VALUE, "");
            listProps = true;
            try {
                if (selProp != null) {
                    boolean saveOK = true;
                    // description
                    if (!propDesc.equals(selProp.getDescription())) {
                        selProp.setDescription(propDesc);
                    }
                    // date-type
                    if (!StringTools.isBlank(propType) && 
                        !propType.equals(selProp.getDataType())) {
                        selProp.setDataType(propType);
                    }
                    // value
                    if (!propVal.equals(selProp.getValue())) {
                        selProp.setValue(propVal);
                    }
                    // save
                    if (saveOK) {
                        selProp.save();
                        m = i18n.getString("SystemPropsAdmin.propUpdated","SystemProperty key updated"); // UserErrMsg
                    } else {
                        // should stay on this page
                        editProp = !readOnly;
                    }
                } else {
                    m = i18n.getString("SystemPropsAdmin.noProps","There are currently no defined SystemProperty keys"); // UserErrMsg
                }
                //return;
            } catch (Throwable t) {
                Print.logException("Updating SystemProperty key", t);
                m = i18n.getString("SystemPropsAdmin.errorUpdate","Internal error updating SystemProperty key"); // UserErrMsg
                error = true;
                //return;
            }
        }

        /* Style */
        HTMLOutput HTML_CSS = new HTMLOutput() {
            public void write(PrintWriter out) throws IOException {
                String cssDir = SystemPropsAdmin.this.getCssDirectory();
                //WebPageAdaptor.writeCssLink(out, reqState, "SystemPropsAdmin.css", cssDir);
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
        final String      _selPropID   = selPropID;
        final SystemProps _selProp     = selProp;
        final String      _propList[]  = propList;
        final boolean     _allowEdit   = allowEdit;
        final boolean     _allowView   = allowView;
        final boolean     _allowNew    = allowNew;
        final boolean     _allowDelete = allowDelete;
        final boolean     _editProp    = _allowEdit && editProp;
        final boolean     _viewProp    = _editProp || viewProp;
        final boolean     _listProps   = listProps || (!_editProp && !_viewProp);
        HTMLOutput HTML_CONTENT = new HTMLOutput(CommonServlet.CSS_CONTENT_FRAME, m) {
            public void write(PrintWriter out) throws IOException {
                String pageName = SystemPropsAdmin.this.getPageName();

                // frame header
              //String menuURL    = EncodeMakeURL(reqState,RequestProperties.TRACK_BASE_URI(),PAGE_MENU_TOP);
                String menuURL    = privLabel.getWebPageURL(reqState, PAGE_MENU_TOP);
                String editURL    = SystemPropsAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                String selectURL  = SystemPropsAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                String newURL     = SystemPropsAdmin.this.encodePageURL(reqState);//,RequestProperties.TRACK_BASE_URI());
                String frameTitle = _allowEdit? 
                    i18n.getString("SystemPropsAdmin.viewEditProp","View/Edit SystemProperty Keys") : 
                    i18n.getString("SystemPropsAdmin.viewProp","View SystemProperty Keys");
                out.write("<span class='"+CommonServlet.CSS_MENU_TITLE+"'>"+frameTitle+"</span><br/>\n");
                out.write("<hr>\n");

                if (_listProps) {
                    
                    // SystemPropsAdmin selection table (Select, ID, Description, Value)
                    out.write("<h1 class='"+CommonServlet.CSS_ADMIN_SELECT_TITLE+"'>"+i18n.getString("SystemPropsAdmin.selectProp","Select a SystemProperty Key")+":</h1>\n");
                    out.write("<div style='margin-left:25px;'>\n");
                    out.write("<form name='"+FORM_PROP_SELECT+"' method='post' action='"+selectURL+"' target='_self'>"); // target='_top'
                    out.write("<input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_SELECT+"'/>");
                    out.write("<table class='"+CommonServlet.CSS_ADMIN_SELECT_TABLE+"' cellspacing=0 cellpadding=0 border=0>\n");
                    out.write(" <thead>\n");
                    out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_ROW+"'>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL_SEL+"' nowrap>"+i18n.getString("SystemPropsAdmin.select","Select")+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+i18n.getString("SystemPropsAdmin.propID","Key")+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+i18n.getString("SystemPropsAdmin.description","Description")+"</th>\n");
                    out.write("   <th class='"+CommonServlet.CSS_ADMIN_TABLE_HEADER_COL    +"' nowrap>"+i18n.getString("SystemPropsAdmin.value","Value")+"</th>\n");
                    out.write("  </tr>\n");
                    out.write(" </thead>\n");
                    out.write(" <tbody>\n");
                    for (int u = 0; u < _propList.length; u++) {
                        if ((u & 1) == 0) {
                            out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_ROW_ODD+"'>\n");
                        } else {
                            out.write("  <tr class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_ROW_EVEN+"'>\n");
                        }
                        try {
                            SystemProps prop = SystemProps.getProperty(_propList[u]);
                            if (prop != null) {
                                String  propID   = prop.getPropertyID();
                                String  propDesc = SystemPropsAdmin.this.filterText(prop.getDescription());
                                String  propVal  = SystemPropsAdmin.this.filterText(prop.getValue());
                                String  checked  = _selPropID.equalsIgnoreCase(propID)? " checked" : "";
                                boolean readOnly = prop.isReadOnly();
                                String  roStyle  = readOnly? "background-color:#E5E5E5;" : "background-color:#FFFFFF;";
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL_SEL+"' "+SORTTABLE_SORTKEY+"='"+u+"' style='"+roStyle+"'><input type='radio' name='"+PARM_PROP_SELECT+"' id='"+propID+"' value='"+propID+"' "+checked+"></td>\n");
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap><label for='"+propID+"'>"+propID+"</label></td>\n");
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+propDesc+"</td>\n");
                                out.write("   <td class='"+CommonServlet.CSS_ADMIN_TABLE_BODY_COL    +"' nowrap>"+propVal+"</td>\n");
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
                        out.write("<input type='submit' name='"+PARM_SUBMIT_VIEW+"' value='"+i18n.getString("SystemPropsAdmin.view","View")+"'>");
                        out.write("</td>\n"); 
                    }
                    if (_allowEdit  ) { 
                        out.write("<td style='padding-left:5px;'>");
                        out.write("<input type='submit' name='"+PARM_SUBMIT_EDIT+"' value='"+i18n.getString("SystemPropsAdmin.edit","Edit")+"'>");
                        out.write("</td>\n"); 
                    }
                    out.write("<td style='width:100%; text-align:right; padding-right:10px;'>");
                    if (_allowDelete) {
                        out.write("<input type='submit' name='"+PARM_SUBMIT_DEL+"' value='"+i18n.getString("SystemPropsAdmin.delete","Delete")+"' "+Onclick_ConfirmDelete(locale)+">");
                    } else {
                        out.write("&nbsp;"); 
                    }
                    out.write("</td>\n"); 
                    out.write("</tr>\n");
                    out.write("</table>\n");
                    out.write("</form>\n");
                    out.write("</div>\n");
                    out.write("<hr>\n");

                    /* new prop */
                    if (_allowNew) {
                        out.write("<h1 class='"+CommonServlet.CSS_ADMIN_SELECT_TITLE+"'>"+i18n.getString("SystemPropsAdmin.createNewProp","Create a new SystemProperty Key")+":</h1>\n");
                        out.write("<div style='margin-top:5px; margin-left:5px; margin-bottom:5px;'>\n");
                        out.write("<form name='"+FORM_PROP_NEW+"' method='post' action='"+newURL+"' target='_self'>"); // target='_top'
                        out.write("<input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_NEW+"'/>");
                        out.write(i18n.getString("SystemPropsAdmin.systemPropID","SystemProperty Key")+": <input type='text' class='"+CommonServlet.CSS_TEXT_INPUT+"' name='"+PARM_NEW_PROP+"' value='' size='50' maxlength='50'>");
                        out.write("<input type='submit' name='"+PARM_SUBMIT_NEW+"' value='"+i18n.getString("SystemPropsAdmin.new","New")+"' style='margin-top:5px; margin-left:10px;'>\n");
                        out.write("</form>\n");
                        out.write("</div>\n");
                        out.write("<hr>\n");
                    }

                } else {
                    // prop view/edit form

                    /* start of form */
                    out.write("<form name='"+FORM_PROP_EDIT+"' method='post' action='"+editURL+"' target='_self'>\n"); // target='_top'
                    out.write("  <input type='hidden' name='"+PARM_COMMAND+"' value='"+COMMAND_INFO_UPDATE+"'/>\n");

                    /* SystemProps fields */
                    String propID = _selPropID;
                    String readOnlyStr = ((_selProp!=null) && _selProp.isReadOnly())? ("&nbsp;"+i18n.getString("SystemPropsAdmin.viewOnlyProperty","(View-Only Property)")) : "";
                    out.println("<table class='"+CommonServlet.CSS_ADMIN_VIEW_TABLE+"' cellspacing='0' callpadding='0' border='0'>");
                    out.println(FormRow_TextField(PARM_PROP_SELECT   , false    , i18n.getString("SystemPropsAdmin.propID","Key")+":"               , propID                                        ,  50,  50, readOnlyStr));
                    out.println(FormRow_TextField(PARM_PROP_DESC     , _editProp, i18n.getString("SystemPropsAdmin.description","Description")+":"  , (_selProp!=null)?_selProp.getDescription() :"",  50,  80));
                  //out.println(FormRow_TextField(PARM_PROP_TYPE     , _editProp, i18n.getString("SystemPropsAdmin.dataType","Data Type")+":"       , (_selProp!=null)?_selProp.getDataType() :""   ,  40,  70));
                    out.println(FormRow_TextField(PARM_PROP_VALUE    , _editProp, i18n.getString("SystemPropsAdmin.value","Value")+":"              , (_selProp!=null)?_selProp.getValue() :""      , 100, 200));
                    out.println("</table>");

                    /* end of form */
                    out.write("<hr style='margin-bottom:5px;'>\n");
                    out.write("<span style='padding-left:10px'>&nbsp;</span>\n");
                    if (_editProp) {
                        out.write("<input type='submit' name='"+PARM_SUBMIT_CHG+"' value='"+i18n.getString("SystemPropsAdmin.change","Change")+"'>\n");
                        out.write("<span style='padding-left:10px'>&nbsp;</span>\n");
                        out.write("<input type='button' name='"+PARM_BUTTON_CANCEL+"' value='"+i18n.getString("SystemPropsAdmin.cancel","Cancel")+"' onclick=\"javascript:openURL('"+editURL+"','_top');\">\n");
                    } else {
                        out.write("<input type='button' name='"+PARM_BUTTON_BACK+"' value='"+i18n.getString("SystemPropsAdmin.back","Back")+"' onclick=\"javascript:openURL('"+editURL+"','_top');\">\n");
                    }
                    out.write("</form>\n");

                }

            }
        };

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
