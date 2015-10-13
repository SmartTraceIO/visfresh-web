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
//  Java Cron scheduler
// ----------------------------------------------------------------------------
// Change History:
//  2007/11/28  Martin D. Flynn
//     -Initial release
//  2009/01/01  Martin D. Flynn
//     -Fixed NPE if 'crontab.xml' does not exist
//  2009/05/01  Martin D. Flynn
//     -Fixed NPE if "When" was not specified (runs exactly once, in such cases).
//  2011/03/08  Martin D. Flynn
//     -Assign default Job 'name' if specified name is missing/blank.
//     -Added runtime config overrides:
//        Crontab.timeZone=[system|<Timezone>]
//        Crontab.stopOnError=[true|false]
//        Crontab.autoReload=[true|false]
//        Crontab.threadPoolSize=<Size>
//        Crontab.interval=<Seconds>
//        Crontab.<JobName>.active=[true|false]
//        Crontab.<JobName>.thread=[true|false]
//     -Added support for replacing runtime vars in arguments.
//  2012/05/27  Martin D. Flynn
//     -Property override is now used if specified (previously it would only be used
//      if the matching property/attribute was missing from the crontab.xml file).
//  2012/08/01  Martin D. Flynn
//     -Allow "\${key}" escapes in argument values.
//  2013/09/26  Martin D. Flynn
//     -Added support for overriding ThreadPool parameters.
//     -"_load(...)" no longer throws an IOException if no jobs were found.
//  2013/11/11  Martin D. Flynn
//     -Search "crontab.xml" and "crontab/crontab.xml" for default crontab file.
//  2014/03/03  Martin D. Flynn
//     -Added "cronRuleFactoryLite.xml" to the default crontab search list.
//  2015/08/16  Martin D. Flynn
//     -Added support for "execJava"
//     -Added additional "sleep" method using "<Object>.wait" (this was a work-around
//      for a bug in Java 6)
//     -Move "When" inner-class to "CronWhen.java"
//     -Added "stopOnError" optional attribute to "Job" tag.
// ----------------------------------------------------------------------------
package org.opengts.extra.util;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.net.*;
import java.security.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.opengts.util.*;

/**
*** Cron job handling tools
**/

public class Cron
{

    // ------------------------------------------------------------------------
    // <Crontab 
    //    [timeZone="US/Pacific"]                                       - Relevant timezone
    //    [interval="60"]                                               - Cron expiration check interval (seconds)
    //    [autoReload="false"]                                          - Autoreload xron XML file if changed
    //    [threadPoolSize="-1"]                                         - Maximum thread pool size [-1 = unlimited]
    //    >
    //    <Job name="SomeJob" 
    //      [thread="true"]                                             - 'true' to run in separate thread
    //      [active="true"]                                             - 'true' if this Job entry is active
    //      >
    //      <Classpath>build/;/otherdir/.</Classpath>                   - Classpath
    //      <Class>com.example.SomeJob</Class>                          - Class
    //      <Method>cron</Method>                                       - Method to execute
    //      <Arg>-account=smith</Arg>                                   - Argument
    //      <Arg>-device=jones</Arg>                                    - Argument
    //      <!-- On the first day of the month, and it is a Sunday, then every 15 minutes of every even hour -->
    //      <When monthDay="1" weekDay="sun" hour="*/2" minute="*/15"/>
    //    </Job>
    //    <Job name="SomeJob2" thread="false">
    //      <Class>com.example.SomeJob2</Class>
    //      <Method>cron</Method>
    //      <Args>-account=smith -device=jones</Args>
    //      <!-- On the first day of the month, and it is a Sunday, then every 15 minutes of every even hour -->
    //      <When>*/15 */2 * 1 sun</When>  <!-- see "man 5 crontab" -->
    //    </Job>
    //    <Job name="LS" active="false">
    //      <Class>org.opengts.opt.util.Cron</Class>
    //      <Method>exec</Method>
    //      <Arg>c:\bin\ls</Arg>
    //      <Arg>-laF</Arg>
    //      <!-- Every minute of every hour -->
    //      <When hour="*" minute="*/1"/>
    //    </Job>
    // </Crontab>
    // ------------------------------------------------------------------------

    /* title */
    private static final String  VERSION                = "0.1.13"; // 2.6.0-B78
    private static final String  COPYRIGHT              = "Copyright(C) 2007-2015 GeoTelematic Solutions, Inc.";

    // ------------------------------------------------------------------------

    private static final String  CRONTAB_DIR            = "crontab";
    private static final String  CRONTAB_XML            = "crontab.xml";
    private static final String  CRONTAB_LITE_XML       = "cronRuleFactoryLite.xml";
    private static final File    DFT_CRONTAB_FILE[]     = { 
        new File(CRONTAB_XML),                      // ./crontab.xml
        new File(CRONTAB_DIR, CRONTAB_XML),         // ./crontab/crontab.xml
        new File(CRONTAB_DIR, CRONTAB_LITE_XML),    // ./crontab/cronRuleFactoryLite.xml
    };

    private static final long    DEFAULT_INTERVAL_SEC   = DateTime.MinuteSeconds(1);
    
    private static final boolean DEFAULT_AUTO_RELOAD    = false;
    private static final boolean DEFAULT_STOP_ON_ERROR  = true;

    // ------------------------------------------------------------------------

    private static final String  TAG_Crontab            = "Crontab";
    private static final String  TAG_Job                = "Job";
    private static final String  TAG_Title              = "Title";
    private static final String  TAG_Class              = "Class";
    private static final String  TAG_Classpath          = "Classpath";
    private static final String  TAG_Method             = "Method";
    private static final String  TAG_Args               = "Args";
    private static final String  TAG_Arg                = "Arg";
    private static final String  TAG_When               = "When";
    private static final String  TAG_Include            = "Include";

    private static final String  ATTR_interval          = "interval";       // seconds
    private static final String  ATTR_timeZone          = "timeZone";
    private static final String  ATTR_timezone          = "timezone";
    private static final String  ATTR_stopOnError       = "stopOnError";
    private static final String  ATTR_autoReload        = "autoReload";
    private static final String  ATTR_threadPoolSize    = "threadPoolSize";
    private static final String  ATTR_sleepAfterEmailMS = "sleepAfterEmailMS";

    private static final String  ATTR_name              = "name";
    private static final String  ATTR_thread            = "thread";
    private static final String  ATTR_active            = "active";

    private static final String  ATTR_minute            = "minute";
    private static final String  ATTR_hour              = "hour";
    private static final String  ATTR_monthDay          = "monthDay";
    private static final String  ATTR_month             = "month";
    private static final String  ATTR_weekDay           = "weekDay";

    private static final String  ATTR_file              = "file";

    // ------------------------------------------------------------------------

    private static final String  PROCESS_INLINE_VAL     = "inline";
    private static final String  PROCESS_THREAD_VAL     = "thread";
    private static final String  PROCESS_NEW_VAL        = "new";

    private static final int     PROCESS_INLINE         = 0;
    private static final int     PROCESS_THREAD         = 1;
    private static final int     PROCESS_NEW            = 2; // not yet supported
    
    private static final int     DEFAULT_PROCESS        = PROCESS_THREAD;
    
    private static final boolean USE_THREAD_SLEEP       = true;
    private static final Object  OBJECT_SLEEP           = new Object();

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* Cron ThreadPool */
    // Cron.ThreadPool.maximumPoolSize=20
    // Cron.ThreadPool.maximumIdleSeconds=0
    // Cron.ThreadPool.maximumQueueSize=0
    private static final RTKey PROP_ThreadPool_CronTask_     = RTKey.valueOf(RTKey.ThreadPool_CronTask_);
    private static final int   ThreadPool_CronTask_Size      = 20;
    private static final int   ThreadPool_CronTask_IdleSec   =  0;
    private static final int   ThreadPool_CronTask_QueSize   =  0;
    private static ThreadPool  ThreadPool_CronTask           = new ThreadPool(
        "CronTask",
        PROP_ThreadPool_CronTask_, // property allowing default override
        ThreadPool_CronTask_Size, 
        ThreadPool_CronTask_IdleSec, 
        ThreadPool_CronTask_QueSize);

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private long                intervalSec          = DEFAULT_INTERVAL_SEC;
    private boolean             autoReload           = DEFAULT_AUTO_RELOAD;
    private long                lastCrontabsReadMS   = 0L;
    private boolean             hasLoadError         = false;
    private File                crontabXMLFile       = null;
    private OrderedSet<CronJob> cronJobs             = null;
    private ThreadPool          threadPool           = null;
    private TimeZone            timeZone             = null;

    /**
    *** Cron scheduler default constructor (with no defined 'crontab' file loaded)
    **/
    public Cron()
    {
        super();
    }

    /**
    *** Cron scheduler default constructor
    *** @param xmlFile  The crontab xml file to load
    **/
    public Cron(File xmlFile)
        throws FileNotFoundException
    {
        this();
        if (xmlFile != null) {
            this.setCrontabXMLFile(xmlFile);
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the default crontab xml file
    *** @return The default crontab xml file
    **/
    private static File _getDefaultCrontabXMLFile()
        throws FileNotFoundException
    {

        /* get configuration directory */
        File cfgFile = RTConfig.getLoadedConfigFile();
        if (cfgFile == null) {
            throw new FileNotFoundException("Default Crontab XML file directory not found");
        }

        /* return first matching file found */
        for (int i = 0; i < DFT_CRONTAB_FILE.length; i++) {
            File cronXml = new File(cfgFile.getParentFile(), DFT_CRONTAB_FILE[i].toString());
            if (cronXml.isFile()) {
                try {
                    return cronXml.getCanonicalFile();
                } catch (IOException ioe) {
                    // ignore error, just return 'cronXml' as-is
                    return cronXml;
                }
            }
        }

        /* still not found, return first entry */
        return new File(cfgFile.getParentFile(), DFT_CRONTAB_FILE[0].toString());

    }

    /** 
    *** Sets the crontab xml file to load
    *** @param xmlFile  The crontab file to load
    ***/
    public void setCrontabXMLFile(File xmlFile)
        throws FileNotFoundException
    {
        this.lastCrontabsReadMS = 0L;
        if ((xmlFile == null) || xmlFile.toString().equals("")) {
            this.crontabXMLFile = Cron._getDefaultCrontabXMLFile();
            if (!this.crontabXMLFile.isFile()) {
                throw new FileNotFoundException("Default Crontab XML file does not exist: " + this.crontabXMLFile);
            }
        } else
        if (xmlFile.isFile()) {
            this.crontabXMLFile = xmlFile;
        } else {
            this.crontabXMLFile = null;
            throw new FileNotFoundException("Crontab XML file does not exist: " + xmlFile);
        }
    }
    
    /**
    *** Returns the current crontab xml file
    *** @return the current crontab xml file
    **/
    public File getCrontabXMLFile()
        throws FileNotFoundException
    {
        if (this.crontabXMLFile == null) {
            throw new FileNotFoundException("Crontab XML file has not been specified");
        }
        return this.crontabXMLFile;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the cron wakeup interval (in seconds).
    *** @param intervSec  The cron wakeup interval (in seconds).
    **/
    public void setCronIntervalSec(long intervSec)
    {
        this.intervalSec = (intervSec > 0L)? intervSec : DEFAULT_INTERVAL_SEC;
    }

    /**
    *** Gets the current cron wakeup interval (in seconds).
    *** @return  The current cron wakeup interval (in seconds).
    **/
    public long getCronIntervalSec()
    {
        if (this.intervalSec <= 0) {
            this.intervalSec = DEFAULT_INTERVAL_SEC;
        }
        return this.intervalSec;
    }

    // ------------------------------------------------------------------------

    /**
    *** Sets the current TimeZone
    *** @param tmzStr The TimeZone (String representation)
    **/
    public void setTimeZone(String tmzStr)
    {
        this.setTimeZone(DateTime.getTimeZone(tmzStr));
    }

    /**
    *** Sets the current TimeZone
    *** @param tmz The TimeZone
    **/
    public void setTimeZone(TimeZone tmz)
    {
        this.timeZone = tmz;
    }
    
    /**
    *** Gets the current TimZone
    *** @return The current TimeZone (does not return null)
    **/
    public TimeZone getTimeZone()
    {
        if (this.timeZone == null) {
            this.timeZone = DateTime.getDefaultTimeZone();
        }
        return this.timeZone;
    }

    /**
    *** Gets the TimeZone name
    *** @return The TimeZone name
    **/
    public String getTimeZoneID()
    {
        TimeZone tz = this.getTimeZone();
        return (tz != null)? tz.getID() : "UNKNOWN";
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the ThreadPool used to run threaded jobs
    *** @return The ThreadPool used to run threaded jobs
    **/
    protected ThreadPool getThreadPool()
    {
        if (this.threadPool == null) {
            this.threadPool = new ThreadPool("Cron");
        }
        return this.threadPool;
    }

    /**
    *** Gets the maximum ThreadPool size
    *** @return The maximum ThreadPool size
    **/
    protected int getMaximumThreadPoolSize()
    {
        return this.getThreadPool().getMaxPoolSize();
    }

    /**
    *** Tells ThreadPool all threads should stop
    *** @return True if a ThreadPool was defined, false otherwise.
    **/
    protected boolean stopThreadPool()
    {
        if (this.threadPool != null) {
            this.threadPool.stopThreads(true);
            // -- once stopped, this ThreadPool cannot be used
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the current CronJob list
    *** @return The current CronJob list
    **/
    protected java.util.List<CronJob> getJobList()
    {
        if (this.cronJobs == null) {
            this.cronJobs = new OrderedSet<CronJob>();
        }
        return this.cronJobs;
    }
    
    /**
    *** Clears the current CronJob list
    **/
    public void cleanJobList()
    {
        if (this.cronJobs != null) {
            this.cronJobs.clear();
        }
    }

    /** 
    *** Returns the current number of CronJobs
    *** @return the current number of CronJobs
    **/
    public int getJobCount()
    {
        return (this.cronJobs != null)? this.cronJobs.size() : 0;
    }

    /**
    *** Returns true if "getJobCount()" is greater than 0
    *** @return True if this Cron instance contains at least one job.
    **/
    public boolean hasJobs()
    {
        return (this.getJobCount() > 0);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the current CronJob names
    *** @return The current CronJob names
    **/
    public String[] getJobNames()
    {
        java.util.List<String> jobNameList = new Vector<String>();
        for (Iterator i = this.getJobList().iterator(); i.hasNext();) {
            CronJob cj = (CronJob)i.next();
            jobNameList.add(cj.getName());
        }
        return jobNameList.toArray(new String[jobNameList.size()]);
    }

    /**
    *** Gets the description of the specified job name (case sensitive match) 
    *** @return The description of the specified job name
    **/
    public String getJobDescription(String jobName)
    {
        if (jobName != null) {
            for (Iterator i = this.getJobList().iterator(); i.hasNext();) {
                CronJob cj = (CronJob)i.next();
                if (jobName.equals(cj.getName())) {
                    return cj.toString(false); // short description
                }
            }
        } 
        return "";
    }

    // ------------------------------------------------------------------------
    
    /**
    *** Start the Cron process.  
    *** This method blocks forever, or until an error occurs (whichever comes first)
    **/
    public void runCron()
        throws IOException
    {

        /* loop forever */
        for (;;) {

            // -- check for updated crontab XML
            this._load(false/*forceReload*/, true/*checkStopOnError*/);

            // -- check/run jobs
            if (this.hasJobs()) {
                // -- check jobs
                TimeZone   tz = this.getTimeZone();
                ThreadPool tp = this.getThreadPool(); // never null
                for (Iterator i = this.getJobList().iterator(); i.hasNext();) {
                    CronJob cj = (CronJob)i.next();
                    cj.testAndRun(tz, tp);
                }
            } else {
                // -- no jobs
            }

            // -- Sleep: wake up at the beginning of the next interval (seconds)
            long intervalSec = this.getCronIntervalSec();
            long currTimeSec = DateTime.getCurrentTimeSec();
            long nextTimeSec = ((currTimeSec / intervalSec) + 1L) * intervalSec;
            long sleepSec    = nextTimeSec - currTimeSec;
            try { 
                long sleepMS = (sleepSec * 1000L) + 5L;
                // -- Found an issue with Thread.sleep(..) on the following version of Java:
                // -    java version "1.6.0_24"
                // -    OpenJDK Runtime Environment (IcedTea6 1.11.5) (rhel-1.50.1.11.5.el6_3-x86_64)
                // -    OpenJDK 64-Bit Server VM (build 20.0-b12, mixed mode)
                // -  "Thread.sleep(MS)" sleeps an inconsistant/unpredictable amount of time.
                // -  "<Object>.wait(MS)" waits for about (MS - 1000) milliseconds
                // -  Reason: This is apparently due to the "Leap Second bug", which is fixed with the
                // -  following command: date `date +"%m%d%H%M%C%y.%S"`  (reboot may also fix)
                // -  Symptoms include "ps -fe" showing Java process with nearly 100% CPU usage.
                //Print.logDebug("LeapSecondBug: Sleeping " + sleepMS + " ms ...");
                for (;;) {
                    long startSleepMS = DateTime.getCurrentTimeMillis();
                    if (USE_THREAD_SLEEP) {
                        // -- Java version 1.6.0_24: may sleep an inconsistent/unpredictable duration
                        Thread.sleep(sleepMS); 
                        // -- may be longer than sleepMS
                    } else {
                        // -- Java version 1.6.0_24: seems to sleep (sleepMS - 1000) milliseconds
                        synchronized (OBJECT_SLEEP) { OBJECT_SLEEP.wait(sleepMS); }
                    }
                    long deltaSleepMS = DateTime.getCurrentTimeMillis() - startSleepMS;
                    if (deltaSleepMS < sleepMS) {
                        sleepMS -= deltaSleepMS;
                        continue; // sleep some more
                    }
                    break;
                }
                //Print.logDebug("LeapSecondBug: ... Done sleeping.");
            } catch (Throwable th) {
                Print.logWarn("Sleep interrupted ...");
            }

        }

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    /**
    *** Returns true if a "load" error has occurred
    **/
    public boolean hasLoadError()
    {
        return this.hasLoadError;
    }

    /** 
    *** Loads the specified crontab xml file
    *** @param xmlFile  The crontab xml file to load
    *** @return True if jobs were found, false otherwise
    **/
    public boolean load(File xmlFile)
        throws IOException // FileNotFoundException, ...
    {
        return this.load(xmlFile, true/*checkStopOnError*/);
    }

    /** 
    *** Loads the specified crontab xml file
    *** @param xmlFile  The crontab xml file to load
    *** @return True if jobs were found, false otherwise
    **/
    public boolean load(File xmlFile, boolean checkStopOnError)
        throws IOException // FileNotFoundException, ...
    {
        this.setCrontabXMLFile(xmlFile);
        return this._load(true/*forceReload*/, checkStopOnError);
    }

    /** 
    *** Loads/Reloads the current crontab xml file
    *** @param forceReload  True to force a reload of the current crontab xml file
    *** @return True if jobs were found, false otherwise
    **/
    private boolean _load(boolean forceReload, boolean checkStopOnError)
        throws IOException
    {

        /* XML file */
        File xmlFile = this.getCrontabXMLFile();
        if (this.lastCrontabsReadMS > 0L) {
            // -- we are reloading
            if (!forceReload && (this.lastCrontabsReadMS == xmlFile.lastModified())) {
                // -- already up-to-date
                return this.hasJobs();
            } else
            if (!this.autoReload) {
                // -- ignore the reload
                Print.logDebug("File changed, but 'autoReload' has been disabled");
                this.lastCrontabsReadMS = xmlFile.lastModified();
                return this.hasJobs();
            }
            Print.logInfo("Reloading Crontab XML file: " + xmlFile);
        } else {
            Print.logDebug("Loading Crontab XML file: " + xmlFile);
        }

        /* clear all existing jobs */
        this.cleanJobList();

        /* get XML document */
        this.lastCrontabsReadMS = xmlFile.lastModified();
        Document xmlDoc = this.getDocument(xmlFile);
        if (xmlDoc == null) {
            throw new FileNotFoundException("Crontab XML file open/parse error: " + xmlFile);
        }

        /* get top-level tag */
        Element crontabs = xmlDoc.getDocumentElement();
        if (!crontabs.getTagName().equalsIgnoreCase(TAG_Crontab)) {
            throw new IOException("Invalid root XML tag (expected 'Crontab'): " + xmlFile);
        }

        /* runtime properties prefix: "CronTab." ... */
        String rtPrefix_ = TAG_Crontab + ".";

        /* stop on error: eg "Crontab.stopOnError=true" */
        this.hasLoadError = false;
        boolean stopOnError = false;
        if (checkStopOnError) {
            String soeKey = rtPrefix_ + ATTR_stopOnError;
            stopOnError = RTConfig.hasProperty(soeKey)?
                RTConfig.getBoolean(soeKey, DEFAULT_STOP_ON_ERROR) :
                this.getAttributeBoolean(crontabs, ATTR_stopOnError, DEFAULT_STOP_ON_ERROR);
        }

        /* reload ok? eg "Crontab.autoReload=false" */
        {
            String autoKey = rtPrefix_ + ATTR_autoReload;
            this.autoReload = RTConfig.hasProperty(autoKey)?
                RTConfig.getBoolean(autoKey, DEFAULT_AUTO_RELOAD) :
                this.getAttributeBoolean(crontabs, ATTR_autoReload, DEFAULT_AUTO_RELOAD);
        }

        /* threadPool size: eg "Crontab.threadPoolSize=5" */
        {
            String tpsKey = rtPrefix_ + ATTR_threadPoolSize;
            long threadPoolSize = RTConfig.hasProperty(tpsKey)?
                RTConfig.getLong(tpsKey, -1L) :
                this.getAttributeLong(crontabs, ATTR_threadPoolSize, -1L);
            if (threadPoolSize > 0L) {
                this.getThreadPool().setMaxPoolSize((int)threadPoolSize);
            }
        }

        /* sleepAfterEmailMS (milliseconds): eg "Crontab.sleepAfterEmailMS=10000" */
        {
            String saeKey = rtPrefix_ + ATTR_sleepAfterEmailMS;
            long sleepAfterEmailMS = RTConfig.hasProperty(saeKey)?
                RTConfig.getLong(saeKey, -1L) :
                this.getAttributeLong(crontabs, ATTR_sleepAfterEmailMS, -1L);
            if (sleepAfterEmailMS > 0L) {
                SendMail.SetSleepAfterEMailMS(sleepAfterEmailMS);
            }
        }

        /* cron schedule interval (seconds): eg "Crontab.interval=60" */
        {
            String intvKey = rtPrefix_ + ATTR_interval; // [2.6.0-B42] 
            long interval = RTConfig.hasProperty(intvKey)?
                RTConfig.getLong(intvKey, -1L) :
                this.getAttributeLong(crontabs, ATTR_interval, -1L);
            this.setCronIntervalSec(interval);
        }

        /* timeZone: eg "Crontab.timeZone=system" */
        {
            String tmzKey[] = { rtPrefix_ + ATTR_timeZone, rtPrefix_ + ATTR_timezone };
            String tmzStr   = RTConfig.hasProperty(tmzKey)?
                RTConfig.getString(tmzKey, null) :
                this.getAttributeString(crontabs, ATTR_timeZone, null);
            if (!StringTools.isBlank(tmzStr)) {
                if (tmzStr.equalsIgnoreCase("system" ) || 
                    tmzStr.equalsIgnoreCase("local"  ) || 
                    tmzStr.equalsIgnoreCase("default")   ) {
                    // -- local system timezone
                    TimeZone tmz = DateTime.getDefaultTimeZone();
                    this.setTimeZone(tmz);
                    //Print.logInfo("TimeZone = " + tmz.getID());
                } else {
                    // -- explicit specified timezone
                    if (!DateTime.isValidTimeZone(tmzStr)) {
                        throw new IOException("Invalid TimeZone specified: " + tmzStr);
                    }
                    this.setTimeZone(tmzStr);
                    //Print.logInfo("TimeZone = " + tmzStr);
                }
            }
        }

        /* parse <Job>s */
        NodeList jobNodeList = XMLTools.getChildElements(crontabs,TAG_Job);
        for (int j = 0; j < jobNodeList.getLength(); j++) {
            Element jobTag = (Element)jobNodeList.item(j);

            /* job name */
            String jobName = this.getAttributeString(jobTag, ATTR_name, null);
            if (StringTools.isBlank(jobName)) {
                jobName = TAG_Job + "_" + j;
                Print.logWarn("Job 'name' attribute missing/blank, assigning name '"+jobName+"'");
            }

            /* Job active? eg "Crontab.HourlyCron.active=true" */
            {
                String actvKey = rtPrefix_ + jobName + "." + ATTR_active;
                boolean active = RTConfig.hasProperty(actvKey)?
                    RTConfig.getBoolean(actvKey, true) :
                    this.getAttributeBoolean(jobTag, ATTR_active, true);
                if (!active) {
                    Print.logInfo("Job: [" + jobName + "] inactive (ignored)");
                    continue;
                }
            }

            /* Job stopOnError? eg "Crontab.HourlyCron.stopOnError=true" */
            boolean jobStopOnError;
            {
                String jsoeKey = rtPrefix_ + jobName + "." + ATTR_stopOnError;
                jobStopOnError = RTConfig.hasProperty(jsoeKey)?
                    RTConfig.getBoolean(jsoeKey, DEFAULT_STOP_ON_ERROR) :
                    this.getAttributeBoolean(jobTag, ATTR_stopOnError, true);
            }

            /* Job process mode: eg "Crontab.HourlyCron.thread=true" */
            int procMode;
            {
                String threadKey = rtPrefix_ + jobName + "." + ATTR_thread;
                boolean threadMode = RTConfig.hasProperty(threadKey)?
                    RTConfig.getBoolean(threadKey, true) :
                    this.getAttributeBoolean(jobTag, ATTR_thread, true);
                procMode = threadMode? PROCESS_THREAD : PROCESS_INLINE;
                // -- inline (non-thread) mode should only be used for debug purposes
            }

            /* Job vars */
            String title       = "";
            String className   = "";
            String classPath[] = null;
            String methName    = "";
            java.util.List<String> argList = new Vector<String>();
            StringBuffer argSB = new StringBuffer();
            CronWhen when      = null;

            /* parse Job node */
            NodeList attrList = jobTag.getChildNodes();
            for (int c = 0; c < attrList.getLength(); c++) {

                /* get Node (only interested in 'Element's) */
                Node attrNode = attrList.item(c);
                if (!(attrNode instanceof Element)) {
                    continue;
                }

                /* parse node */
                String attrName = attrNode.getNodeName();
                Element attrElem = (Element)attrNode;
                if (attrName.equalsIgnoreCase(TAG_Title)) {
                    // -- save title
                    String t = this.getNodeText(attrElem);
                    if (t != null) {
                        String ttl[] = StringTools.parseStringArray(StringTools.replace(t.trim(),"\\n","\n"),'\n');
                        for (int i = 0; i < ttl.length; i++) { ttl[i] = ttl[i].trim(); }
                        title = StringTools.join(ttl,'\n');
                    } else {
                        title = null;
                    }
                } else
                if (attrName.equalsIgnoreCase(TAG_Class)) {
                    // -- class name
                    className = this.getNodeText(attrElem);
                    if (className.equals("Cron")) { // case sensitive
                        // -- this Cron class (typically used for "exec" method call)
                        className = StringTools.className(Cron.class);
                    }
                } else
                if (attrName.equalsIgnoreCase(TAG_Classpath)) {
                    // -- class path
                    classPath = StringTools.parseArray(this.getNodeText(attrElem),';');
                } else
                if (attrName.equalsIgnoreCase(TAG_Method)) {
                    // -- method name
                    methName = this.getNodeText(attrElem);
                } else
                if (attrName.equalsIgnoreCase(TAG_Arg)) {
                    // -- trim and add argument
                    String arg = RTConfig.insertKeyValues(this.getNodeText(attrElem).trim());
                    arg = StringTools.replace(arg, "\\${", "${");
                    arg = StringTools.replace(arg, "%{", "${");
                    argList.add(arg);
                } else
                if (attrName.equalsIgnoreCase(TAG_Args)) {
                    // -- break arguments on space
                    String argStr = this.getNodeText(attrElem);
                    String args[] = ListTools.toArray(new StringTokenizer(argStr," ",false),String.class);
                    for (int i = 0; i < args.length; i++) {
                        String arg = RTConfig.insertKeyValues(args[i]);
                        argList.add(arg);
                    }
                } else
                if (attrName.equalsIgnoreCase(TAG_When)) {
                    // -- save 'when'
                    String whStr = this.getNodeText(attrElem);
                    if (!StringTools.isBlank(whStr)) {
                        when = new CronWhen(whStr);
                    } else {
                        String minute   = this.getAttributeString(attrElem, ATTR_minute  , CronWhen.ALL);
                        String hour     = this.getAttributeString(attrElem, ATTR_hour    , CronWhen.ALL);
                        String monthDay = this.getAttributeString(attrElem, ATTR_monthDay, CronWhen.ALL);
                        String month    = this.getAttributeString(attrElem, ATTR_month   , CronWhen.ALL);
                        String weekDay  = this.getAttributeString(attrElem, ATTR_weekDay , CronWhen.ALL);
                        when = new CronWhen(minute, hour, monthDay, month, weekDay);
                    }
                } else {
                    // -- unrecognized tag
                    this.hasLoadError = true;
                    if (stopOnError && jobStopOnError) {
                        throw new IOException("Unrecognized tag: " + attrName);
                    } else {
                        Print.logWarn("Unrecognized tag: " + attrName);
                    }
                }

            }

            /* create cron-job */
            String args[] = argList.toArray(new String[argList.size()]);
            try {
                CronJob cronJob = new CronJob(jobName, title, 
                    classPath, className, 
                    methName, args, 
                    when, 
                    procMode, "");
                Print.logInfo("Job: " + cronJob);
                this.getJobList().add(cronJob);
            } catch (ClassNotFoundException cnfe) {
                this.hasLoadError = true;
                String msg = "Class not found: Job " + jobName+" - " + className;
                if (stopOnError && jobStopOnError) {
                    Print.logError(msg);
                    throw new IOException(msg, cnfe);
                } else {
                    Print.logWarn(msg);
                }
            } catch (NoSuchMethodException nsme) {
                this.hasLoadError = true;
                String msg = "Class Method not found: Job "+jobName+" - " + className + "." + methName;
                if (stopOnError && jobStopOnError) {
                    Print.logError(msg);
                    throw new IOException(msg, nsme);
                } else {
                    Print.logWarn(msg);
                }
            } catch (Throwable th) { // Invocation Exception?
                this.hasLoadError = true;
                String msg = "Job " + jobName;
                if (stopOnError && jobStopOnError) {
                    Print.logError(msg + " - " + th);
                    throw new IOException(msg, th);
                } else {
                    Print.logWarn(msg + " - " + th);
                }
            }
            
        } // for (... jobNodeList ...)

        /* do we have anything to do? */
        if (this.hasJobs()) {
            return true;
        }

        /* no jobs */
        //if (stopOnError) {
        //    throw new IOException("No Jobs to perform!");
        //}
        return false;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Creates/returns an XML Document for the specific xml file
    *** @param xmlFile The XML file
    **/
    protected Document getDocument(File xmlFile)
    {
        
        /* xmlFIle must be specified */
        if (xmlFile == null) {
            return null;
        }

        /* create XML document */
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(xmlFile);
        } catch (ParserConfigurationException pce) {
            Print.logError("Parse error: " + pce);
        } catch (SAXException se) {
            Print.logError("Parse error: " + se);
        } catch (IOException ioe) {
            Print.logError("IO error: " + ioe);
        }

        /* return */
        return doc;

    }

    /**
    *** Returns the String text for the specified node
    *** @param root  The node for which the node text will be returned
    **/
    protected String getNodeText(Node root)
    {
        StringBuffer sb = new StringBuffer();
        if (root != null) {
            NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n.getNodeType() == Node.CDATA_SECTION_NODE) { // CDATASection
                    sb.append(n.getNodeValue());
                } else
                if (n.getNodeType() == Node.TEXT_NODE) {
                    sb.append(n.getNodeValue());
                } else {
                    //Print.logWarn("Unrecognized node type: " + n.getNodeType());
                }
            }
        }
        return sb.toString();
    }

    /**
    *** Gets a named attribute from the specified element.
    *** @param elem   The element from which the attribute value will be returned
    *** @param key    The name of the attribute
    *** @param dft    The default value to return if the named attribute doesn't exist
    *** @return The value of the named attribute, or the default value if the attribute does
    ***         not exist.
    **/
    protected String getAttributeString(Element elem, String key, String dft)
    {
        
        /* invalid element/key */
        if ((elem == null) || (key == null)) {
            return dft;
        }
        
        /* simple test for element with exact name */
        if (elem.hasAttribute(key)) {
            // either exists, or has a default value
            return elem.getAttribute(key);
        }
        
        /* scan through attributes for matching (case insensitive) string */
        NamedNodeMap nnm = elem.getAttributes();
        if (nnm != null) {
            int len = nnm.getLength();
            for (int i = 0; i < len; i++) {
                Attr attr = (Attr)nnm.item(i);
                String attrName = attr.getName();
                if (key.equalsIgnoreCase(attrName)) {
                    // found a case-insensitive match
                    Print.logWarn("Expected attribute '" + key + "', but found '" + attrName + "'");
                    return attr.getValue();
                }
            }
        }
        
        /* still not found, return default */
        return dft;

    }

    /**
    *** Returns the boolean value of the named attribute from the specified element
    *** @param elem   The element from which the attribute value will be returned
    *** @param key    The name of the attribute
    *** @param dft    The default boolean value to return if the named attribute doesn't exist
    *** @return The boolean value of the named attribute, or the default value if the attribute 
    ***         does not exist.
    **/
    protected boolean getAttributeBoolean(Element elem, String key, boolean dft)
    {
        return StringTools.parseBoolean(this.getAttributeString(elem,key,null),dft);
    }

    /**
    *** Returns the long value of the named attribute from the specified element
    *** @param elem   The element from which the attribute value will be returned
    *** @param key    The name of the attribute
    *** @param dft    The default long value to return if the named attribute doesn't exist
    *** @return The long value of the named attribute, or the default value if the attribute 
    ***         does not exist.
    **/
    protected long getAttributeLong(Element elem, String key, long dft)
    {
        return StringTools.parseLong(this.getAttributeString(elem,key,null),dft);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Prints the specified input string to stdout
    *** @param in  The InputStream to read/print
    *** @param sb  A StringBuffer used to accumulate stream bytes and print
    *** @return True if any data was read from the stream
    **/
    private static boolean _printOutput(InputStream in, StringBuffer sb)
        throws IOException
    {
        boolean didRead = false;
        for (;;) {
            int avail = in.available();
            if (avail > 0) {
                didRead = true;
                for (; avail > 0; avail--) {
                    int b = in.read();
                    if (b >= 0) {
                        if (b == '\n') {
                            Print.sysPrintln(sb.toString());
                            sb.setLength(0);
                        } else {
                            sb.append((char)b);
                        }
                    } else {
                        // error?
                        return didRead;
                    }
                }
            } else {
                break;
            }
        }
        return didRead;
    }

    /**
    *** Executes the specified command in a separate process
    *** @param cmdArgs  The command and arguments to execute
    **/
    public static void exec(String cmdArgs[])
    {
        if ((cmdArgs != null) && (cmdArgs.length > 0)) {
            Process process = null;
            try {

                /* start process */
                if (cmdArgs.length > 1) {
                    process = Runtime.getRuntime().exec(cmdArgs);
                } else {
                    String cmd = StringTools.join(cmdArgs,' ');
                    process = Runtime.getRuntime().exec(cmd);
                }

                /* read available stdout/stderr */
                InputStream  stdout   = new BufferedInputStream(process.getInputStream());
                StringBuffer stdoutSB = new StringBuffer();
                InputStream  stderr   = new BufferedInputStream(process.getErrorStream());
                StringBuffer stderrSB = new StringBuffer();
                for (;;) {
                    try { Thread.sleep(100L); } catch (Throwable th) { /*ignore*/ }
                    boolean didReadOut = Cron._printOutput(stdout, stdoutSB);
                    boolean didReadErr = Cron._printOutput(stderr, stderrSB);
                    if (!didReadOut && !didReadErr) {
                        try {
                            process.exitValue();
                            break;
                        } catch (Throwable th) {
                            //Print.logDebug("Process not yet complete: " + th);
                            // continue
                        }
                    }
                }

                /* flush any unprinted stdout/stderr */
                if (stdoutSB.length() > 0) {
                    Print.sysPrintln(stdoutSB.toString());
                }
                if (stderrSB.length() > 0) {
                    Print.sysPrintln(stderrSB.toString());
                }

                /* get process exit value */
                for (;;) {
                    try {
                        process.waitFor();
                        int status = process.exitValue();
                        if (status != 0) {
                            Print.logError("Job process terminated with status " + status);
                        }
                        return; // return status;
                    } catch (InterruptedException ie) {
                        // ignore
                        return; // return -1
                    }
                }
                
            } catch (Throwable th) {
                Print.logException("Job process failed", th);
                if (process != null) {
                    process.destroy();
                }
                return; // return -2;
            }
        }
        return; // return -99;
    }

    // ------------------------------------------------------------------------

    /**
    *** Executes the specified java class in a separate process, using the same 
    *** classpath defined for this JVM.<br>
    *** The classname maybe specified in the argument list in either of the following formats:<br>
    *** <code>-class=CLASS.PATH.NAME</code><br>
    *** <code>CLASS.PATH.NAME</code><br>
    *** All arguments preceding the class name will be used as JVM parameters, and 
    *** all arguments following the class name will be used as class "main(...)" parameters.
    *** Logging will occur to the same directory as specified for this "Cron" instance.
    *** @param args The java command arguments.
    **/
    public static void execJava(String args[])
    {

        /* make sure we have something to execute */
        if (ListTools.size(args) <= 0) {
            // -- nothing to execute
            Print.logError("No Jave exec arguments specified");
            return;
        }

        /* extract java command components from argument list */
        Vector<String>    javaArgs  = new Vector<String>();
        OSTools.Classpath classpath = null;
        String            className = null;
        Vector<String>    classArgs = new Vector<String>();
        for (String a : args) {
            if (StringTools.isBlank(className)) {
                // -- no classname yet, look for java-args or a classname
                if (a.startsWith("-memory=")) {
                    // -- <Arg>-memory=1024m</Arg>
                    String mem = a.substring("-memory=".length());
                    if (!StringTools.isBlank(mem)) {
                        javaArgs.add("-Xmx"+mem);
                    }
                } else
                if (a.startsWith("-classpath=")) {
                    // -- <Arg>-classpath=.:./build:./build/lib/*.jar</Arg>
                    String cp = a.substring("-classpath=".length());
                    if (!StringTools.isBlank(cp)) {
                        classpath = new OSTools.Classpath(cp);
                    }
                } else
                if (a.startsWith("-class=")) {
                    // -- <Arg>-class=org.opengts.util.DateTime</Arg>
                    className = a.substring("-class=".length());
                } else
                if (!a.startsWith("-") && (a.indexOf('.') > 0)) {
                    // -- <Arg>org.opengts.util.DateTime</Arg>
                    className = a;
                } else {
                    // -- <Arg>-Xmx256m</Arg>
                    javaArgs.add(a);
                }
            } else {
                // -- only class-args after we have a classname
                // -  eg: "-account=smith"
                classArgs.add(a);
            }
        }
        RTProperties classArgsRTP = new RTProperties(classArgs.toArray(new String[classArgs.size()]));

        /* no classname specified? */
        if (StringTools.isBlank(className)) {
            Print.logError("Classname not specified");
            return;
        }

        /* set default class args */
        // -- This section will automatically add the following parameters for GTS jobs
        // -    -conf=${GTS_HOME}/default.conf
        // -    -log.file.enable=true
        // -    -log.name=BorderCrossing
        // -    -log.file=${GTS_HOME}/logs/BorderCrossing.log
        // -    -cron=true
        {
            // -- cron loaded config file
            File confFile    = RTConfig.getLoadedConfigFile(); // should not be null
            File confFileDir = (confFile != null)? confFile.getParentFile() : null;
            File cronLogFile = Print.getLogFile(); // may be null
            File cronLogDir  = (cronLogFile != null)? cronLogFile.getParentFile() : null;
            // -- default log dir
            File dftLogDir   = cronLogDir; // log to same dir as "cron"
            if (classArgsRTP.hasProperty(RTKey.LOG_DIR)) {
                // -- override with explicitly specified log dir (if exists)
                File dir = RTConfig.getFile(RTKey.LOG_DIR,null);
                if (FileTools.isDirectory(dir)) {
                    dftLogDir = dir;
                }
            }
            if ((dftLogDir == null) && (confFileDir != null)) {
                // -- default to config file dir "./logs/" directory
                File dir = new File(confFileDir, "logs"); // ${GTS_HOME}/logs
                if (FileTools.isDirectory(dir)) {
                    dftLogDir = dir;
                }
            }
            // -- "-conf=..."
            if (!classArgsRTP.hasProperty(RTKey.COMMAND_LINE_CONF) && (confFile != null)) {
                // -- same initial config file as this "cron" instance
                classArgsRTP.setString(RTKey.COMMAND_LINE_CONF, confFile.toString());
            }
            // -- "-log.file.enable=..."
            if (!classArgsRTP.hasProperty(RTKey.LOG_FILE_ENABLE)) {
                // -- same logging enabled state as this "cron" instance
                classArgsRTP.setProperty(RTKey.LOG_FILE_ENABLE, ((cronLogFile != null)? true : false));
            }
            // -- "-log.name=..."
            String logName = classArgsRTP.getString(RTKey.LOG_NAME, null);
            if (StringTools.isBlank(logName)) {
                // -- set log name to class module id
                int p = className.lastIndexOf(".");
                logName = (p >= 0)? className.substring(p+1) : className;
                classArgsRTP.setString(RTKey.LOG_NAME, logName);
            }
            // -- "-log.file=..."
            String logFile = classArgsRTP.getString(RTKey.LOG_FILE, null);
            if (StringTools.isBlank(logFile) && (dftLogDir != null)) {
                // -- set logging to default log directory and log name
                logFile = (new File(dftLogDir, (logName + ".log"))).toString();
                classArgsRTP.setString(RTKey.LOG_FILE, logFile);
            }
            // -- "-cron=true"
            if (!classArgsRTP.hasProperty("cron")) {
                classArgsRTP.setBoolean("cron", true);
            }
        }

        /* sort class "main" arguments */
        String classArgV[] = classArgsRTP.toStringArray(true);
        ListTools.sort(classArgV, new Comparator<String>() {
            public int compare(String kv1, String kv2) {
                // -- equals
                if (kv1.equals(kv2)) {
                    return 0; // ==
                }
                // ------------------------------
                // -- "-conf=*" is sorted first
                boolean kvConf_1 = kv1.startsWith("-conf=");
                boolean kvConf_2 = kv2.startsWith("-conf=");
                if (kvConf_1) {
                    // -- ("-conf=*" < "-*")
                    return -1; // <
                } else
                if (kvConf_2) {
                    // -- ("-*" > "-conf=*")
                    return 1; // >
                }
                // ------------------------------
                // -- "-log.*" is sorted second
                boolean kvLog_1  = kv1.startsWith("-log.");
                boolean kvLog_2  = kv2.startsWith("-log.");
                if (kvLog_1 && kvLog_2) {
                    // -- ("-log.*" == "-log.*")
                    return 0; // ==
                } else
                if (kvLog_1) {
                    // -- ("-log.*" < "-*")
                    return -1; // <
                } else
                if (kvLog_2) {
                    // -- ("-*" > "-log.*")
                    return 1; // >
                }
                // ------------------------------
                // -- "-cron" is sorted third
                boolean kvCron_1 = kv1.equals("-cron") || kv1.startsWith("-cron=");
                boolean kvCron_2 = kv2.equals("-cron") || kv2.startsWith("-cron=");
                if (kvCron_1) {
                    // -- ("-cron" < "-*")
                    return -1; // <
                } else
                if (kvCron_2) {
                    // -- ("-*" > "-cron")
                    return 1; // >
                }
                // ------------------------------
                // -- ("-*" == "-*") everything else follows
                return 0; // ==
            }
        });

        /* construct java command */
        String javaArgV[] = javaArgs.toArray(new String[javaArgs.size()]);
        String jExe[] = OSTools.createJavaCommand(javaArgV, classpath, className, classArgV);
        Print.logInfo("JavaCmd: " + StringTools.join(jExe," "));

        /* execute */
        Cron.exec(jExe);

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Custom CronClassLoader
    **/
    private static class CronClassLoader
        extends URLClassLoader
    {
        public CronClassLoader(File cpFile[]) {
            super(new URL[0]);
            for (int i = 0; i < cpFile.length; i++) {
                try {
                    URL url = new URL("file:///" + cpFile[i].getAbsolutePath() + "/");
                    this.addURL(url);
                } catch (MalformedURLException mue) {
                    Print.logError("Invalid file specification: " + cpFile[i]);
                }
            }
        }
        public Class loadClassAndResolve(String className)
            throws ClassNotFoundException {
            return this.loadClass(className, true);
        }
    }

    /**
    *** Returns the Class for the specified className
    *** @param classPath  The CLASSPATH to search for the class
    *** @param className  The class name for which the Class object is returned
    *** @return The Class object for the specified class name
    **/
    private static Class getClass(String classPath[], String className)
        throws ClassNotFoundException
    {
        String errMsg = "";
        try {
            if ((classPath == null) || (classPath.length <= 0)) {
                errMsg = "Attempted to load class from default ClassLoader: " + className;
                return Class.forName(className);
            } else {
                errMsg = "Attempted to load class from custom ClassLoader: " + className;
                java.util.List<File> cpList = new Vector<File>();
                for (int i = 0; i < classPath.length; i++) {
                    File file = new File(classPath[i]);
                    if (file.exists()) {
                        cpList.add(file);
                    } else {
                        Print.logWarn("ClassPath item not found: " + file);
                    }
                }
                File filePath[] = cpList.toArray(new File[cpList.size()]);
                CronClassLoader ccl = new CronClassLoader(filePath);
                return ccl.loadClassAndResolve(className);
            }
        } catch (ClassNotFoundException cnfe) {
            Print.logError(errMsg);
            throw cnfe;
        }
    }
    
    /**
    *** CronJob class
    **/
    public class CronJob
        implements Runnable
    {
        private String       name           = "";
        private String       title          = "";
        private String       classPath[]    = null; // not currently used
        private String       className      = null;
        private String       methName       = null;
        private String       args[]         = null;
        private CronWhen     when           = null;
        private MethodAction method         = null;
        private int          processMode    = PROCESS_THREAD;
        private String       processName    = null;
        // --
        private long         lockoutTimeSec = 0L;
        private long         lastStartSec   = 0L;
        private long         lastStopSec    = 0L;
        // -- 
        public CronJob(String name, String title,
            String classPath[], String className, 
            String methName, String args[], 
            CronWhen when, 
            int processMode, String processName) 
            throws NoSuchMethodException, ClassNotFoundException {
            this.name        = (name != null)? StringTools.trim(name) : "cron";
            this.title       = StringTools.trim(title);
            this.classPath   = classPath;
            this.className   = StringTools.trim(className);
            this.methName    = StringTools.trim(methName);
            this.args        = (args != null)? args : new String[0];
            this.when        = when; // may be null
            Class clazz      = Cron.getClass(this.classPath, this.className);
            this.method      = new MethodAction(clazz, this.methName, new Class[] { String[].class });
            this.processMode = processMode;
            this.processName = StringTools.trim(processName);
        }
        public String getName() {
            return this.name;
        }
        public String getTitle() {
            return this.title;
        }
        public String[] getClassPath() {
            return this.classPath;
        }
        public String getClassName() {
            return this.className;
        }
        public String getMethodName() {
            return this.methName;
        }
        public String[] getArgs() {
            return this.args;
        }
        public int getProcessMode() {
            return this.processMode;
        }
        public String getProcessName() {
            return this.processName;
        }
        public boolean runInThread() {
            int p = this.getProcessMode();
            if (p == PROCESS_INLINE) {
                return false;
            } else
            if (p == PROCESS_NEW) {
                return true;
            } else {
                return true;
            }
        }
        public boolean runInProcess() {
            return false;
        }
        public boolean testAndRun(TimeZone tz, ThreadPool threadPool) {
            DateTime currTime = new DateTime(tz);
            long  currTimeSec = currTime.getTimeSec();
            long  lastStart   = this.getLastStartSec();
            long  intervalSec = Cron.this.getCronIntervalSec();
            if ((lastStart / intervalSec) == (currTimeSec / intervalSec)) {
                Print.logWarn("Time schedule already Tested: " + this);
                return false;
            } else
            if ((this.when == null) && (lastStart > 0L)) {
                // -- OneShot: no "When" specified, and we've already run once
                return false;
            } else
            if ((this.when == null) || this.when.isTimeMatch(currTime)) {
                // -- run once, or timer has elapsed
                if ((lastStart > 0L) && (lastStart > this.getLastStopSec())) {
                    // -- last job has not yet stopped (we are still running)
                    Print.logError("Previous Job has not yet completed!: " + this);
                } else
                if ((this.lockoutTimeSec > 0L) && (this.lockoutTimeSec > currTimeSec)) {
                    // -- we have a request to run, but our lockout time has not expired
                    Print.logDebug("Job already run during this interval: " + this);
                } else {
                    // -- set lockout time to next time-slot
                    if (this.when != null) {
                        DateTime currDT = new DateTime(currTimeSec,tz);
                        int lockoutMin = this.when.getLockoutMinutes(currDT);
                        if (lockoutMin > 0L) { 
                            this.lockoutTimeSec = currTimeSec + (((lockoutMin * 4) / 5) * 60);
                        } else {
                            this.lockoutTimeSec = 0L;
                        }
                    }
                    // -- run in process/thread/inline
                    switch (this.getProcessMode()) {
                        case PROCESS_NEW:
                            // -- process (not supported here)
                            // -  separate processes handled by "Cron.exec(...)" method
                            // -  fall through below
                        case PROCESS_THREAD:
                            // -- thread
                            if (threadPool != null) {
                                // -- thread
                                threadPool.run(this);
                            } else {
                                // -- inline (will not occur here)
                                Print.logWarn("** Running Job in-line **");
                                this.run();
                            }
                            return true; // the job may not have started yet
                        default:
                            // -- inline
                            Print.logWarn("** Running Job in-line **");
                            this.run();
                            return true; // Job completed execution
                    }
                }
            }
            return false;
        }
        public void run() {
            //Print.logInfo("==============================================================================");
            Print.logInfo("==== CronBegin: " + this);
            this.lastStartSec = DateTime.getCurrentTimeSec();
            try {
                this.method.invoke(new Object[] { this.args });
            } catch (Throwable th) { // InvocationException, RuntimeException
                //Print.logError("CronError: " + th);
                Print.logException(this.toString(), th);
            }
            this.lastStopSec = DateTime.getCurrentTimeSec();
            Print.logInfo("---- CronEnd  : " + this);
            //Print.logInfo("------------------------------------------------------------------------------");
        }
        public long getLastStartSec() {
            return this.lastStartSec;
        }
        public long getLastStopSec() {
            return this.lastStopSec;
        }
        public String toString() {
            return this.toString(true); // long version
        }
        public String toString(boolean longVers) {
            StringBuffer sb = new StringBuffer();
            if (longVers) {
                sb.append("[");
                sb.append(this.getName());
                if (!StringTools.isBlank(this.getTitle())) {
                    sb.append(":");
                    sb.append(this.getTitle());
                }
                sb.append("] ");
            }
            sb.append(this.className).append(".").append(this.methName);
            sb.append(":");
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-debugMode")) {
                    // no need to include "-debugMode"
                } else {
                    sb.append(" ").append(args[i]);
                }
            }
            if (this.when != null) {
                sb.append(" [").append(this.when.toString()).append("]");
            } else {
                sb.append(" [<RunOnce>]");
            }
            if (longVers) {
                switch (this.getProcessMode()) {
                    case PROCESS_INLINE:
                        sb.append(" {inline}");
                        break;
                    case PROCESS_NEW:
                        sb.append(" {process}");
                        break;
                    case PROCESS_THREAD:
                        sb.append(" {thread}");
                        break;
                    default:
                        sb.append(" {unknown}");
                        break;
                }
            }
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    // System.setSecurityManager(new SystemExitSecurityManager());
    private static SecurityManager OldSecurityManager = null;
    private static boolean EnableSystemExit = false;
    
    /**
    *** SystemExitSecurityManager class
    **/
    private static class SystemExitSecurityManager
        extends SecurityManager
    {
        public void checkExit(int status) {
            if (!EnableSystemExit) { 
                //throw new SecurityException(); 
                throw new RuntimeException("Job invoked 'System.exit'"); 
            }
        }
        public void checkPermission(Permission perm) {
            if (OldSecurityManager != null) {
                OldSecurityManager.checkPermission(perm);
            }
        }
    }
        
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private static int  RecursionCheck = 0;
    
    private static final String ARG_CRONTAB[]  = { "crontab", "cron", "xml" };

    /**
    *** Main entry point
    *** @param argv  The command line arguments
    **/
    public static void main(String argv[])
    {

        /* runtime vars (if not already initialized) */
        if (!RTConfig.isInitialized()) {
            RTConfig.setCommandLineArgs(argv);
        }

        /* logging */
        RTConfig.setDebugMode(true);
        Print.setLogLevel(Print.LOG_ALL);
        Print.setLogHeaderLevel(Print.LOG_ALL);

        /* show classpath */
        if (RTConfig.getBoolean("showClasspath",false)) {
            OSTools.Classpath cp = OSTools.getClasspath(true);
            Print.sysPrintln("Cron classpath: " + cp);
            return;
        }

        // --------------------------------------------------------------------

        /* crontab */
        FileNotFoundException crontabErr = null;
        File crontab = RTConfig.getFile(ARG_CRONTAB,null);
        if ((crontab == null) || crontab.toString().equals("")) {
            try {
                crontab = Cron._getDefaultCrontabXMLFile();
            } catch (FileNotFoundException fnfe) {
                crontabErr = fnfe;
            }
        }
        if ((crontabErr == null) && !crontab.isFile()) {
            try {
                String absCrontab = crontab.getAbsolutePath();
                crontabErr = new FileNotFoundException("Crontab file does not exist: " + absCrontab);
            } catch (Throwable th) {
                crontabErr = new FileNotFoundException("Crontab file does not exist: " + crontab);
            }
        }

        /* header */
        Print.logInfo("----------------------------------------------------------------");
        Print.logInfo(COPYRIGHT);
        Print.logInfo("Cron Server: Version " + VERSION);
        Print.logInfo("Crontab    : " + ((crontab!=null)?crontab:"<unknown>"));

        /* crontab file not found? */
        if (crontabErr != null) {
            Print.logException("Crontab file not found", crontabErr);
            Print.logInfo("Use \"-cron <file>\" option to override default");
            System.exit(1);
        }

        /* check resursion */
        if (RecursionCheck > 0) {
            Print.logStackTrace("Recursion not allowed in Cron scheduler!");
            System.exit(2);
        }

        /* save old SecurityManager and install new */
        OldSecurityManager = System.getSecurityManager();
        System.setSecurityManager(new SystemExitSecurityManager());

        // ----------------------------

        /* initialize and start Cron tasks */
        Cron cron = null;
        for (;;) { // single-pass loop

            /* create Cron and load initial crontab file */
            cron = new Cron();
            try {
                cron.load(crontab, true/*checkStopOnError*/);
            } catch (IOException ioe) { // FileNotFoundException
                // -- unlikely, since we prevarified the file
                Print.logException("Crontab file error", ioe);
                break;
            }
    
            /* debug log for loaded crontab */
            Print.logInfo("Interval   : " + cron.getCronIntervalSec() + " seconds");
            Print.logInfo("TimeZone   : " + cron.getTimeZoneID());
            Print.logInfo("ThreadPool : " + cron.getMaximumThreadPoolSize() + " max thread(s)");
            Print.logInfo("Memory/Mb  : " + OSTools.getMemoryUsageStringMb(false));
            Print.logInfo("----------------------------------------------------------------");

            /* init/run cron */
            RecursionCheck++;
            try {
                cron.runCron(); // blocks forever
                // -- control does not reach here
            } catch (Throwable th) {
                Print.logException("Cron Error", th);
            }
            RecursionCheck--;

            /* break single-pass loop */
            break;

        }

        /* shutdown */
        if (cron != null) {
            cron.stopThreadPool();
        }

        /* if we get here, then an error has occurred */
        EnableSystemExit = true;
        System.exit(1);

    }

}
