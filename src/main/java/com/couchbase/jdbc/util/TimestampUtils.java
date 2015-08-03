/*
 * //  Copyright (c) 2015 Couchbase, Inc.
 * //  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * //  except in compliance with the License. You may obtain a copy of the License at
 * //    http://www.apache.org/licenses/LICENSE-2.0
 * //  Unless required by applicable law or agreed to in writing, software distributed under the
 * //  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * //  either express or implied. See the License for the specific language governing permissions
 * //  and limitations under the License.
 */

package com.couchbase.jdbc.util;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by davec on 2015-08-03.
 */
public class TimestampUtils
{
    private StringBuffer sbuf = new StringBuffer();
    private Calendar defaultCal = new GregorianCalendar();
    private final TimeZone defaultTz = defaultCal.getTimeZone();

    public synchronized String toString(Calendar cal, Timestamp x) {
        if (cal == null)
            cal = defaultCal;

        cal.setTime(x);
        sbuf.setLength(0);

        appendDate(sbuf, cal);
        sbuf.append(' ');
        appendTime(sbuf, cal, x.getNanos());
        appendTimeZone(sbuf, cal);
        appendEra(sbuf, cal);

        return sbuf.toString();
    }

    public synchronized String toString(Calendar cal, Time x)
    {
        if (cal == null)
            cal = defaultCal;

        cal.setTime(x);
        sbuf.setLength(0);

        appendTime(sbuf, cal, cal.get(Calendar.MILLISECOND) * 1000000);

        appendTimeZone(sbuf, cal);

        return sbuf.toString();
    }

    public synchronized String toString(Calendar cal, Date x)
    {
        if (cal == null)
            cal = defaultCal;

        cal.setTime(x);
        sbuf.setLength(0);

        appendDate(sbuf, cal);
        appendEra(sbuf, cal);
        appendTimeZone(sbuf, cal);

        return sbuf.toString();
    }

    private static void appendDate(StringBuffer sb, Calendar cal)
    {
        int l_year = cal.get(Calendar.YEAR);
        // always use at least four digits for the year so very
        // early years, like 2, don't get misinterpreted
        //
        int l_yearlen = String.valueOf(l_year).length();
        for (int i = 4; i > l_yearlen; i--)
        {
            sb.append("0");
        }

        sb.append(l_year);
        sb.append('-');
        int l_month = cal.get(Calendar.MONTH) + 1;
        if (l_month < 10)
            sb.append('0');
        sb.append(l_month);
        sb.append('-');
        int l_day = cal.get(Calendar.DAY_OF_MONTH);
        if (l_day < 10)
            sb.append('0');
        sb.append(l_day);
    }

    private static void appendTime(StringBuffer sb, Calendar cal, int nanos)
    {
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        if (hours < 10)
            sb.append('0');
        sb.append(hours);

        sb.append(':');
        int minutes = cal.get(Calendar.MINUTE);
        if (minutes < 10)
            sb.append('0');
        sb.append(minutes);

        sb.append(':');
        int seconds = cal.get(Calendar.SECOND);
        if (seconds < 10)
            sb.append('0');
        sb.append(seconds);

        // Add nanoseconds.
        // This won't work for server versions < 7.2 which only want
        // a two digit fractional second, but we don't need to support 7.1
        // anymore and getting the version number here is difficult.
        //
        char[] decimalStr = {'0', '0', '0', '0', '0', '0', '0', '0', '0'};
        char[] nanoStr = Integer.toString(nanos).toCharArray();
        System.arraycopy(nanoStr, 0, decimalStr, decimalStr.length - nanoStr.length, nanoStr.length);
        sb.append('.');
        sb.append(decimalStr, 0, 6);
    }

    private void appendTimeZone(StringBuffer sb, java.util.Calendar cal)
    {
        int offset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 1000;

        int absoff = Math.abs(offset);
        int hours = absoff / 60 / 60;
        int mins = (absoff - hours * 60 * 60) / 60;
        int secs = absoff - hours * 60 * 60 - mins * 60;

        sb.append((offset >= 0) ? " +" : " -");

        if (hours < 10)
            sb.append('0');
        sb.append(hours);

        sb.append(':');

        if (mins < 10)
            sb.append('0');
        sb.append(mins);

        sb.append(':');
        if (secs < 10)
            sb.append('0');
        sb.append(secs);
    }

    private static void appendEra(StringBuffer sb, Calendar cal)
    {
        if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
            sb.append(" BC");
        }
    }
}
