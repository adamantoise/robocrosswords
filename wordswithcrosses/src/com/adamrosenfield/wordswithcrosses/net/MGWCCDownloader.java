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
 * Matt Gaffney's Weekly Crossword Contest
 * URL: http://xwordcontest.com/YYYY/MM/page/[page]
 * Date: Friday
 */
public class MGWCCDownloader extends ManualDownloader
{
    private static final String BASE_URL = "http://xwordcontest.com/";

    public MGWCCDownloader()
    {
        super("Matt Gaffney's Weekly Crossword Contest");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    protected String getManualDownloadUri(Calendar date)
    {
        // Figure out which archive page we want
        Calendar now = Calendar.getInstance();
        Calendar refDate = now;
        if (date.get(Calendar.MONTH) != now.get(Calendar.MONTH))
        {
            refDate = (Calendar)date.clone();
            refDate.set(Calendar.DATE, refDate.getMaximum(Calendar.DATE));
        }

        // The page number is the number of Fridays between the given date and
        // the reference date (now or the end of the given month), inclusive
        int fridayCount = 0;
        while (refDate.compareTo(date) >= 0)
        {
            int dayOfWeek = refDate.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.FRIDAY)
            {
                fridayCount++;
                refDate.add(Calendar.DATE, -7);
            }
            else
            {
                refDate.add(Calendar.DATE, -((dayOfWeek - Calendar.FRIDAY + 7) % 7));
            }
        }

        // This might not be the exactly correct URI (if today is Friday
        // morning and the puzzle hasn't been posted yet, or if there's an
        // extra blog post), but it'll be close enough, and the user can
        // manually find the correct download page in a browser if this is
        // wrong.
        String uri =
            BASE_URL +
            date.get(Calendar.YEAR) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "/";
        if (fridayCount != 1)
        {
            uri += "page/" + fridayCount + "/";
        }

        return uri;
    }
}
