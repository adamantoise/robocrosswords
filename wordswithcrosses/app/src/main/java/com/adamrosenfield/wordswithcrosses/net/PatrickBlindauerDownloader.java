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

/**
 * Patrick Blindauer's Free Monthly Puzzle
 * URL: http://www.patrickblindauer.com/Free_Monthly_Puzzles/Mmm_YYYY/[puzzle-name].puz
 * Date: Monday, Thursday
 */
public class PatrickBlindauerDownloader extends AbstractPageScraper
{
    private static final String BLINDAUER_SHORT_MONTHS[] =
    {
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    };

    public PatrickBlindauerDownloader()
    {
        super("Patrick Blindauer");
    }

    @Override
    protected String getScrapeURL(Calendar date)
    {
        int month = date.get(Calendar.MONTH);
        String monthStr = BLINDAUER_SHORT_MONTHS[month];
        if (month == Calendar.SEPTEMBER)
        {
            int year = date.get(Calendar.YEAR);
            if (year == 2011 || year == 2012)
            {
                monthStr = "Sept";
            }
        }

        return ("http://www.patrickblindauer.com/Free_Monthly_Puzzles/" +
                monthStr +
                "_" +
                date.get(Calendar.YEAR) +
                "/");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_MONTH) == 1);
    }
}
