<%@ taglib uri="./Track" prefix="gts" %>
<%@ page isELIgnored="true" contentType="text/html; charset=UTF-8" %>
<%
//response.setContentType("text/html; charset=UTF-8");
//response.setCharacterEncoding("UTF-8");
response.setHeader("CACHE-CONTROL", "NO-CACHE");
response.setHeader("PRAGMA"       , "NO-CACHE");
response.setDateHeader("EXPIRES"  , 0         );
//response.addHeader("P3P","CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<!-- <!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> -->
<html xmlns='http://www.w3.org/1999/xhtml' xmlns:v='urn:schemas-microsoft-com:vml'>
<!-- jsp/loginSession_panel.jsp: <gts:var>${version} [${privateLabelName}]</gts:var>
  =======================================================================================
  Copyright(C) 2007-2012 GeoTelematic Solutions, Inc., All rights reserved
  Project: OpenGTS - Open GPS Tracking System [http://www.opengts.org]
  =======================================================================================
-->
<gts:var ifKey="notDefined" value="true">
<!--
  =======================================================================================
  Change History:
   2010/04/11  Martin D. Flynn
      -Initial creation
  =======================================================================================
-->
</gts:var>

<!-- Head -->
<head>

  <!-- meta -->
  <gts:var>
  <meta name="author" content="GeoTelematic Solutions, Inc."/>
  <meta http-equiv="content-type" content='text/html; charset=UTF-8'/>
  <meta http-equiv="cache-control" content='no-cache'/>
  <meta http-equiv="pragma" content="no-cache"/>
  <meta http-equiv="expires" content="0"/>
  <meta name="copyright" content="${copyright}"/>
  <meta name="robots" content="none"/>
  </gts:var>

  <!-- page title -->
  <gts:var>
  <title>${pageTitle}</title>
  </gts:var>

  <!-- default style -->
  <link rel='stylesheet' type='text/css' href='css/General.css'/>
  <link rel='stylesheet' type='text/css' href='css/Controls.css'/>

  <!-- custom overrides style -->
  <link rel='stylesheet' type='text/css' href='custom/General.css'/>
  <link rel='stylesheet' type='text/css' href='custom/Controls.css'/>

  <!-- javascript -->
  <gts:track section="javascript"/>

  <!-- page specific style -->
  <gts:track section="stylesheet"/>

  <!-- custom override style -->
  <link rel='stylesheet' type='text/css' href='custom/Custom.css'/>

  <style type="text/css">
    BODY { 
        background-color: white; /* #9AAEC3; */ 
<gts:var if="isLocaleRTL" value="true">
        direction: RTL;
</gts:var>
    }
  </style>

</head>

<!-- ======================================================================================= -->

<body onload="<gts:track section='body.onload'/>" onunload="<gts:track section='body.onunload'/>">

<gts:track section="content.body"/>

</body>

<!-- ======================================================================================= -->

</html>
