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
 * Patrick Blindauer Free Monthly Puzzle
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
        "Sept",  // At least for 2011 and 2012.  We'll see about 2013...
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
        return ("http://www.patrickblindauer.com/Free_Monthly_Puzzles/" +
                BLINDAUER_SHORT_MONTHS[date.get(Calendar.MONTH)] +
                "_" +
                date.get(Calendar.YEAR) +
                "/");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_MONTH) == 1);
    }
}
