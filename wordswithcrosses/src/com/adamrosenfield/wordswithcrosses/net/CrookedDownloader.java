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
 * CRooked Crosswords
 * URL: http://www.crookedcrosswords.com/
 * Date: Sunday
 */
public class CrookedDownloader extends XWordHubDownloader
{
    private static final Calendar START_DATE;

    static
    {
        Calendar startDate = Calendar.getInstance();
        startDate.clear();
        startDate.set(2013, 0, 6);  // 2013-01-06 (months start at 0 for Calendar!)
        START_DATE = startDate;
    }

    public CrookedDownloader(String username, String password)
    {
        super("CRooked Crossword", "crooked", username, password);
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return
            (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) &&
            (date.compareTo(START_DATE) >= 0);
    }
}
