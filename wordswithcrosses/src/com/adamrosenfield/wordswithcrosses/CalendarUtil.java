/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2013 Adam Rosenfield
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.adamrosenfield.wordswithcrosses;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utility class for working with java.util.Calendar instances
 */
public class CalendarUtil
{
    /** U.S. Eastern time zone */
    public static final TimeZone TZ_US_EASTERN = TimeZone.getTimeZone("America/New_York");

    /**
     * Creates a Calendar instance for the given year, month (1-based), and day
     * (1-based) in the default time zone
     */
    public static Calendar createDate(int year, int month, int day)
    {
        Calendar date = Calendar.getInstance();
        date.clear();
        date.set(year, month - 1, day);  // Months start at 0 for Calendar!
        return date;
    }

    /**
     * Creates a Calendar instance for the given year, month (1-based), and day
     * (1-based) in the given time zone
     */
    public static Calendar createDate(TimeZone timeZone, int year, int month, int day)
    {
        Calendar date = Calendar.getInstance(timeZone);
        date.clear();
        date.set(year, month - 1, day);  // Months start at 0 for Calendar!
        return date;
    }

    /**
     * Creates a Calendar instance for the given year, month (1-based), day
     * (1-based), hour, minute and second in the default time zone
     */
    public static Calendar createDate(int year, int month, int day, int hour, int minute, int second)
    {
        Calendar date = Calendar.getInstance();
        date.set(year, month - 1, day, hour, minute, second);  // Months start at 0 for Calendar!
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }

    /**
     * Creates a Calendar instance for the given year, month (1-based), day
     * (1-based), hour, minute and second in the given time zone
     */
    public static Calendar createDate(TimeZone timeZone, int year, int month, int day, int hour, int minute, int second)
    {
        Calendar date = Calendar.getInstance(timeZone);
        date.set(year, month - 1, day, hour, minute, second);  // Months start at 0 for Calendar!
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }
}
