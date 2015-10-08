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
 * CrosSynergy/Washington Post downloader
 * URL: https://washingtonpost.as.arkadiumhosted.com/clients/washingtonpost-content/crossynergy/csYYMMDD.jpz
 * Date: Daily
 */
public class WaPoDownloader extends AbstractJPZDownloader {
    private static final String NAME = "Washington Post";

    public WaPoDownloader() {
        super("https://washingtonpost.as.arkadiumhosted.com/clients/washingtonpost-content/crossynergy/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        // Only the most recent 2 weeks of puzzles are available
        Calendar now = Calendar.getInstance();
        long millisOld = now.getTimeInMillis() - date.getTimeInMillis();
        int daysOld = (int)(millisOld / ((long)86400 * 1000));
        return (daysOld <= 14);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("cs" +
                (date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".jpz");
    }
}
