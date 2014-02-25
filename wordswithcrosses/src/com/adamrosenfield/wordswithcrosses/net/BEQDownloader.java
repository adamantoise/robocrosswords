/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2009-2010 Robert Cooper
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
 * Brendan Emmett Quigley
 * URL: http://www.brendanemmettquigley.com/files/[puzzle-name].puz
 * Date: Monday, Thursday
 */
public class BEQDownloader extends ManualDownloader
{
    // BEQ has requested that his puzzles not be automatically downloaded,
    // so we use a manual downloader for now
    public BEQDownloader()
    {
        super("Brendan Emmett Quigley");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        int day = date.get(Calendar.DAY_OF_WEEK);
        return (day == Calendar.MONDAY || day == Calendar.THURSDAY);
    }

    @Override
    protected String getManualDownloadUri(Calendar date)
    {
        // AFAICT there aren't any easy-to-form archive URLs or search by
        // aate URLs, so we just go to the home page for now.
        return "http://www.brendanemmettquigley.com/";
    }
}
