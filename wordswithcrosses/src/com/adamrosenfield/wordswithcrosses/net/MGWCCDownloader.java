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
 * Matt Gaffney's Weekly Crossword Contest
 * URL: http://xwordcontest.com/submissions/[number]/mgwcc[number].puz
 * Date: Friday
 */
public class MGWCCDownloader extends AbstractDownloader
{
    private static final String BASE_URL = "http://xwordcontest.com/submissions/";

    /** Date on which MGWCC was first published */
    private static final Calendar START_DATE = CalendarUtil.createDate(2008, 6, 6);

    public MGWCCDownloader()
    {
        super(BASE_URL, "Matt Gaffney's Weekly Crossword Contest");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        // Figure out what the puzzle number is
        long millisSinceStart = (date.getTimeInMillis() - START_DATE.getTimeInMillis());
        int daysSinceStart = (int)((millisSinceStart/1000 + 86399) / 86400);
        int puzzleNum = (daysSinceStart / 7) + 1;

        return
            puzzleNum +
            "/mgwcc" +
            puzzleNum +
            ".puz";
    }
}
