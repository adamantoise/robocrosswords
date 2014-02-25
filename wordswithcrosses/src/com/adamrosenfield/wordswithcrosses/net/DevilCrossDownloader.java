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
 * Devil Cross
 * URL: http://devilcross.com/
 * Date: Every other Saturday
 */
public class DevilCrossDownloader extends ManualDownloader
{
    private static final String BASE_URL = "http://devilcross.com/";

    /** Date on which Devil Cross was first published */
    private static final Calendar START_DATE = createDate(2014, 2, 1);

    public DevilCrossDownloader()
    {
        super("Devil Cross");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        if (date.compareTo(START_DATE) < 0 || date.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
        {
            return false;
        }

        // Devil Cross is published every other week
        long millisSinceStart = (date.getTimeInMillis() - START_DATE.getTimeInMillis());
        int daysSinceStart = (int)((millisSinceStart/1000 + 86399) / 86400);
        return ((daysSinceStart / 7) % 2) == 0;
    }

    @Override
    protected String getManualDownloadUri(Calendar date)
    {
        return BASE_URL +
            date.get(Calendar.YEAR) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.DATE)) +
            "/";
    }
}
