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

package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

import com.adamrosenfield.wordswithcrosses.CalendarUtil;

/**
 * Andy Kravis
 * URL: http://cruciverbalistatlaw.blogspot.com
 * Date: Sunday
 */
public class AndyKravisDownloader extends BloggerDownloader
{
    private static final Calendar SWITCHOVER_DATE = CalendarUtil.createDate(2013, 10, 14);

    public AndyKravisDownloader()
    {
        super("Andy Kravis", "http://cruciverbalistatlaw.blogspot.com");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        // Puzzles up through 10/12/13 were on Saturdays; puzzles
        // after 10/20/13 were on Sundays
        if (date.compareTo(SWITCHOVER_DATE) >= 0)
        {
            return (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
        }
        else
        {
            return (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY);
        }
    }
}
