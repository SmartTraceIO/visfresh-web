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
//  2015/08/16  Martin D. Flynn
//     -Moved from "Cron.java"
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
*** Cron job "When"
**/

public class CronWhen
{

    // ------------------------------------------------------------------------

    public  static final String  ALL                    = "*"; // WHEN_ALL 

    // ------------------------------------------------------------------------

    private static final int     TYPE_NONE              = 0;
    private static final int     TYPE_HOUR_MINUTE       = 1;
    private static final int     TYPE_HOUR              = 2;
    private static final int     TYPE_MONTH_DAY         = 3; // day of month-1
    private static final int     TYPE_MONTH             = 4;
    private static final int     TYPE_WEEK_DAY          = 5;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /* externally provided ascii "When" specification */
    private String  minute              = null;
    private String  hour                = null;
    private String  monthDay            = null;
    private String  month               = null;
    private String  weekDay             = null;

    /* calculated "When" bitmasks */
    private int     monthDayMask[]      = null; // int[12]  31-bit mask per element
    private int     weekDayMask         = -1;   // int       7-bit mask
    private long    hourMinuteMask[]    = null; // long[24] 60-bit mask per element

    /**
    *** Constructor
    **/
    public CronWhen(String minute, String hour, String monthDay, String month, String weekDay) 
    {
        this._setWhen(minute, hour, monthDay, month, weekDay);
    }

    /**
    *** Constructor
    **/
    public CronWhen(String wh) 
    {
        // -- "MINUTE HOUR DAY MONTH DOW"
        String c[] = ListTools.toArray(new StringTokenizer(wh," ",false), String.class);
        String minute   = (c.length > 0)? c[0] : null; // minute of hour
        String hour     = (c.length > 1)? c[1] : null; // hour of day
        String monthDay = (c.length > 2)? c[2] : null; // day of month
        String month    = (c.length > 3)? c[3] : null; // month1 of year
        String weekDay  = (c.length > 4)? c[4] : null; // day of week
        this._setWhen(minute, hour, monthDay, month, weekDay);
    }

    /**
    *** Sets the "When" attributes for this instance
    **/
    private void _setWhen(String minute, String hour, String monthDay, String month, String weekDay) 
    {
        this.minute   = !StringTools.isBlank(minute)  ? minute.trim()   : CronWhen.ALL;
        this.hour     = !StringTools.isBlank(hour)    ? hour.trim()     : CronWhen.ALL;
        this.monthDay = !StringTools.isBlank(monthDay)? monthDay.trim() : CronWhen.ALL;
        this.month    = !StringTools.isBlank(month)   ? month.trim()    : CronWhen.ALL;
        this.weekDay  = !StringTools.isBlank(weekDay) ? weekDay.trim()  : CronWhen.ALL;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the minute "When" configuration
    **/
    public String getMinute() 
    {
        return this.minute;
    }

    /**
    *** Returns the minute "When" time-slots as a bitmask
    **/
    private long parseMinute() 
    {
        return this._parseRangeMask(this.getMinute(),TYPE_HOUR_MINUTE);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the hour "When" configuration
    **/
    public String getHour() 
    {
        return this.hour;
    }

    /**
    *** Returns the hour "When" time-slots as a bitmask
    **/
    private long parseHour() 
    {
        return this._parseRangeMask(this.getHour(),TYPE_HOUR);
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the day-of-month "When" configuration
    **/
    public String getMonthDay() 
    {
        return this.monthDay;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the month-of-year "When" configuration
    **/
    public String getMonth() 
    {
        return this.month;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the day-of-week "When" configuration
    **/
    public String getWeekDay() 
    {
        return this.weekDay;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Parses the divisor value
    **/
    private int _parseDivisor(String divS) 
    {

        if (StringTools.isBlank(divS) || divS.equals(CronWhen.ALL)) {
            // -- divisor indicates every timeslot
            return 1;
        } else {
            // -- divisor indicates a timeslot interval
            return Math.max(StringTools.parseInt(divS,1),1);
        }

    }

    /**
    *** Parses the RANGE/DIVISOR specification and returns the result as a bitmask
    *** @return A bitmask representing the specified RANGE/DIVISOR specification
    **/
    private long _parseRangeMask(String RD, int type) 
    {

        // -- separate RANGE/DIVISOR
        int    dP = RD.indexOf('/');
        String RV = (dP >= 0)? RD.substring(0,dP) : RD;
        String DV = (dP >= 0)? RD.substring(dP+1) : CronWhen.ALL;

        // -- parse range
        int rp = RV.indexOf('-');
        String loS = (rp >= 0)? RV.substring(0,rp) : RV;
        String hiS = (rp >= 0)? RV.substring(rp+1) : "";
        int    loV = -1, loL = -1;
        int    hiV = -1, hiL = -1;
        switch (type) {
            case TYPE_HOUR_MINUTE: {
                // -- minute of hour
                loL =  0;
                hiL = 59;
                if (StringTools.isBlank(loS) || loS.equals(CronWhen.ALL)) {
                    loV = loL;
                    hiV = hiL;
                } else {
                    loV = StringTools.parseInt(loS, -1); // ie. "54"
                    hiV = StringTools.parseInt(hiS,loV);
                }
            } break;
            case TYPE_HOUR: {
                // -- hour of day
                loL =  0;
                hiL = 23;
                if (StringTools.isBlank(loS) || loS.equals(CronWhen.ALL)) {
                    loV = loL;
                    hiV = hiL;
                } else {
                    loV = StringTools.parseInt(loS, -1); // ie. "23"
                    hiV = StringTools.parseInt(hiS,loV);
                }
            } break;
            case TYPE_MONTH: {
                // -- calendar month ("1-12")
                loL =  1;
                hiL = 12;
                if (StringTools.isBlank(loS) || loS.equals(CronWhen.ALL)) {
                    loV = loL;
                    hiV = hiL;
                } else {
                    loV = DateTime.getMonthIndex1(loS, -1); // ie. "Dec" ==> 12
                    hiV = DateTime.getMonthIndex1(hiS,loV);
                }
            } break;
            case TYPE_MONTH_DAY: {
                // -- day of month-1
                loL =  1;
                hiL = 31; // would need the current month to be more accurate
                if (StringTools.isBlank(loS) || loS.equals(CronWhen.ALL)) {
                    loV = loL;
                    hiV = hiL;
                } else {
                    loV = StringTools.parseInt(loS, -1); // ie. "16"
                    hiV = StringTools.parseInt(hiS,loV);
                }
            } break;
            case TYPE_WEEK_DAY: {
                // -- day of week ("Sun-Sat")
                loL = 0;
                hiL = 6;
                if (StringTools.isBlank(loS) || loS.equals(CronWhen.ALL)) {
                    loV = loL;
                    hiV = hiL;
                } else {
                    loV = DateTime.getDayIndex(loS, -1); // ie. "Tue" ==> 2
                    hiV = DateTime.getDayIndex(hiS,loV);
                }
            } break;
        }

        // -- check range limits
        if ((loV <   0) || (loV < loL) || (loV > hiL)) {
            // -- low value is out of range (invalid, return null)
            return 0L;
        }
        if ((hiV < loV) || (hiV < loL) || (hiV > hiL)) {
            // -- high value is out of range (set to loV)
            hiV = loV;
        }

        // -- assemble range
        long R = 0L; // maximum range size is 60
        int divV = this._parseDivisor(DV);
        for (int _v = loV; _v <= hiV; _v++) {
            int v = _v - loL;
            if ((v % divV) == 0) {
                R |= (1L << v);
            }
        }

        // -- return range
        return R; // 64-bit mask

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the day-of-month mask.  
    *** @return A 12-element int array, one element for each 0-based month. 
    ***     Each element is a bitmask of each 0-based day in the month.
    **/
    private int[] getMonthDayMask() 
    {

        if (this.monthDayMask == null) {

            // -- hour/minute mask (each element represents the hour of the day)
            int mdMask[] = new int[12]; // 1..12 months of the year
            ListTools.initArray(mdMask, 0);

            // -- day-of-month range(s)
            String dRange[] = StringTools.parseArray(this.getMonthDay(),',');
            int dMask = 0;
            for (int dr = 0; dr < dRange.length; dr++) {
                dMask |= (int)this._parseRangeMask(dRange[dr],TYPE_MONTH_DAY);
            }

            // -- per-month range(s)
            String mRange[] = StringTools.parseArray(this.getMonth(),',');
            for (int mr = 0; mr < mRange.length; mr++) {
                int mMask = (int)this._parseRangeMask(mRange[mr],TYPE_MONTH);
                //Print.sysPrintln("MonthMask: " + StringTools.toBinaryString(mMask));
                for (int m = 0; m < mdMask.length; m++) {
                    if ((mMask & (1 << m)) != 0) {
                        mdMask[m] = dMask;
                    }
                }
            }

            // -- save mask array
            this.monthDayMask = mdMask;

        }
        return this.monthDayMask;

    }

    /**
    *** Returns true if the "when" configuration matches the month/day of the 
    *** specified DateTime.
    *** @param DT  The DateTime to test
    *** @return True if match, false otherwise
    **/
    public boolean isMonthDayMatch(DateTime DT) 
    {

        // -- validate DateTime
        if (!DateTime.isValid(DT)) {
            return false;
        }

        // -- current time data
        int mm0  = DT.getMonth0();
        int dd0  = DT.getDayOfMonth() - 1;
        int ts[] = this.getMonthDayMask();
        return ((ts[mm0] & (1 << dd0)) != 0)? true : false;

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the day-of-week mask.  
    *** @return A bitmask representing each day of the week
    **/
    private int getWeekDayMask() 
    {

        if (this.weekDayMask < 0) {

            // -- initialize day-of-week mask
            this.weekDayMask = 0;
            String dowRange[] = StringTools.parseArray(this.getWeekDay(),',');
            for (int dr = 0; dr < dowRange.length; dr++) {
                //Print.sysPrintln("WeekDay: " + dowRange[dr]);
                this.weekDayMask |= (int)this._parseRangeMask(dowRange[dr],TYPE_WEEK_DAY);
            }

        }
        return this.weekDayMask;

    }

    /**
    *** Returns true if the "when" configuration matches the day-of-week of the 
    *** specified DateTime.
    *** @param DT  The DateTime to test
    *** @return True if match, false otherwise
    **/
    public boolean isWeekDayMatch(DateTime DT) 
    {

        // -- validate DateTime
        if (!DateTime.isValid(DT)) {
            return false;
        }

        // -- current time data
        int dow  = DT.getDayOfWeek();
        int DOWM = this.getWeekDayMask();
        return ((DOWM & (1 << dow)) != 0)? true : false;

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the hour/minute mask.  
    *** @return A 24-element long array, one element for each hour of the day.
    ***     Each element is a bitmask of the minute of the hour.
    **/
    private long[] getHourMinuteMask() 
    {

        if (this.hourMinuteMask == null) {

            // -- hour/minute mask (each element represents the hour of the day)
            long hmMask[] = new long[24]; // 0..23 hour of the day
            ListTools.initArray(hmMask, 0L);

            // -- minute range(s)
            String mRange[] = StringTools.parseArray(this.getMinute(),',');
            long mMask = 0L;
            for (int mr = 0; mr < mRange.length; mr++) {
                mMask |= this._parseRangeMask(mRange[mr],TYPE_HOUR_MINUTE);
            }

            // -- per-hour range(s)
            String hRange[] = StringTools.parseArray(this.getHour(),',');
            for (int hr = 0; hr < hRange.length; hr++) {
                long hMask = this._parseRangeMask(hRange[hr],TYPE_HOUR);
                for (int h = 0; h < hmMask.length; h++) {
                    if ((hMask & (1L << h)) != 0L) {
                        hmMask[h] = mMask;
                    }
                }
            }

            // -- save mask array
            this.hourMinuteMask = hmMask;

        }
        return this.hourMinuteMask;

    }

    /**
    *** Returns true if the "when" configuration matches the hour/minute of the 
    *** specified DateTime.
    *** @param DT  The DateTime to test
    *** @return True if match, false otherwise
    **/
    public boolean isHourMinuteMatch(DateTime DT) 
    {

        // -- validate DateTime
        if (!DateTime.isValid(DT)) {
            return false;
        }

        // -- current time data
        int  h24  = DT.getHour24();
        int  min  = DT.getMinute();
        long ts[] = this.getHourMinuteMask();
        return ((ts[h24] & (1L << min)) != 0L)? true : false;

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the next "when" trigger time after the time specified.
    **/
    public int getNextHourMinute(DateTime DT) 
    {

        // -- validate DateTime
        if (!DateTime.isValid(DT)) {
            return -1;
        }

        // -- current time data
        int  h24  = DT.getHour24();
        int  min  = DT.getMinute();
        long ts[] = this.getHourMinuteMask();

        // -- start at following minute
        if (++min >= 60) {
            min = min - 60;
            if (++h24 >= ts.length) {
                h24 = 0; // beginning of next hour
            }
        }

        // -- find next time slot
        for (int ih = 0; ih < ts.length; ih++) {
            int h = (h24 + ih) % ts.length;
            for (int m = min; m < 60; m++) {
                if ((ts[h] & (1L << m)) != 0L) {
                    return (h * 60) + m; // minute-of-day
                }
            }
            min = 0; // subsequent loops start at beginning of minute
        }

        // -- never will execute
        return -1;

    }

    /**
    *** Gets the previous "when" trigger time starting with the time specified.
    **/
    public int getPreviousHourMinute(DateTime DT) 
    {

        // -- validate DateTime
        if (!DateTime.isValid(DT)) {
            return -1;
        }

        // -- current time data
        int  h24  = DT.getHour24();
        int  min  = DT.getMinute();
        long ts[] = this.getHourMinuteMask();

        // -- find previous time slot
        for (int ih = 0; ih < ts.length; ih++) {
            int h = (h24 - ih + ts.length) % ts.length;
            for (int m = min; m >= 0; m--) {
                if ((ts[h] & (1L << m)) != 0L) {
                    return (h * 60) + m; // minute-of-day
                }
            }
            min = 60 - 1; // subsequent loops start at end of minute
        }

        // -- never will execute
        return -1;

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the number of minutes to the next "when" trigger time after the
    *** time specified.
    **/
    public int getLockoutMinutes(DateTime DT) 
    {

        // -- validate DateTime
        if (!DateTime.isValid(DT)) {
            return -1;
        }

        // -- current time data
        int currMod = DT.getMinuteOfDay();
        int nextMod = this.getNextHourMinute(DT);
        if (nextMod < 0) {
            // -- will never execute
            return -1;
        } else
        if (nextMod > currMod) {
            // -- current day
            return nextMod - currMod;
        } else {
            // -- following day
            return (nextMod + 1440) - currMod;
        }

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified DateTime matches this "when" trigger
    **/
    public boolean isTimeMatch(DateTime DT) 
    {

        // -- validate DateTime
        if (!DateTime.isValid(DT)) {
            return false;
        }

        // -- month/day match?
        if (!this.isMonthDayMatch(DT)) {
            return false;
        }

        // -- day-of-week match?
        if (!this.isWeekDayMatch(DT)) {
            return false;
        }

        // -- hour/minute match
        if (!this.isHourMinuteMatch(DT)) {
            return false;
        }

        // -- match
        return true;

    }

    // ------------------------------------------------------------------------

    /**
    *** Returns a String representation of this instance 
    **/
    public String toString() 
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getMinute());
        sb.append(" ");
        sb.append(this.getHour());
        sb.append(" ");
        sb.append(this.getMonthDay());
        sb.append(" ");
        sb.append(this.getMonth());
        sb.append(" ");
        sb.append(this.getWeekDay());
        return sb.toString();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static final String ARG_WHEN[]     = { "when" };
    private static final String ARG_TIME[]     = { "time" };

    /**
    *** Main entry point
    *** @param argv  The command line arguments
    **/
    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);

        /* range parse debuig test */
        if (RTConfig.hasProperty(ARG_WHEN)) {
            CronWhen when = new CronWhen(RTConfig.getString(ARG_WHEN,"")); // wh.
            DateTime timeDT;
            try {
                timeDT = DateTime.parseArgumentDate(RTConfig.getString(ARG_TIME,""));
            } catch (DateTime.DateParseException dpe) {
                timeDT = new DateTime(); 
            }
            // --
            {
                long   hhSlots = when.parseHour();
                String hhSlotS = StringTools.replicateString("X",40) + StringTools.toBinaryString(hhSlots).substring(40);
                Print.sysPrintln("Hour   Time Slots: " + hhSlotS);
                long   mmSlots = when.parseMinute();
                String mmSlotS = StringTools.replicateString("X", 4) + StringTools.toBinaryString(mmSlots).substring( 4);
                Print.sysPrintln("Minute Time Slots: " + mmSlotS);
            }
            // --
            {
                long hmSlots[] = when.getHourMinuteMask();
                for (int i = 0; i < hmSlots.length; i++) {
                    String SL = StringTools.replicateString("X", 4) + StringTools.toBinaryString(hmSlots[i]).substring( 4);
                    Print.sysPrintln(" Hour/Min Slot " + StringTools.format(i,"00") + ": " + SL);
                }
            }
            // --
            {
                int mdSLots[] = when.getMonthDayMask();
                for (int i = 0; i < mdSLots.length; i++) {
                    String SL = StringTools.replicateString("X", 1) + StringTools.toBinaryString(mdSLots[i]).substring( 1);
                    Print.sysPrintln(" Mon/Day  Slot  " + StringTools.format(i,"00") + ": " + SL);
                }
            }
            // --
            {
                int dowMask = when.getWeekDayMask();
                String SL = StringTools.replicateString("X", 1) + StringTools.toBinaryString((byte)dowMask).substring( 1);
                Print.sysPrintln(" DOW Slot      --: " + SL);
            }
            // --
            {
                int nextMod = when.getNextHourMinute(timeDT);
                int lockout = when.getLockoutMinutes(timeDT);
                Print.sysPrintln("Next time slot @ "+(nextMod/60)+":"+(nextMod%60)+", minutes " + lockout + "["+(lockout/60)+":"+(lockout%60)+"]");
            }
            // --
            Print.sysPrintln("When("+timeDT+") = " + when.isTimeMatch(timeDT));
            // --
            System.exit(0);
        }

    }

}
