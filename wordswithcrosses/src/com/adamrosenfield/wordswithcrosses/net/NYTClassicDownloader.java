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
 * New York Times Classic
 * URL: http://www.nytimes.com/specials/puzzles/classic.puz
 * Date: Monday, but no archive is available.
 */
public class NYTClassicDownloader extends AbstractDownloader {
    private static final String NAME = "New York Times Classic";

    public NYTClassicDownloader() {
        super("http://www.nytimes.com/specials/puzzles/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        if (date.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            return false;

        // Only download if requested week is same as current week, because there is no archive.
        Calendar now = Calendar.getInstance();

        if ((now.get(Calendar.YEAR) != date.get(Calendar.YEAR)) ||
            (now.get(Calendar.WEEK_OF_YEAR) != date.get(Calendar.WEEK_OF_YEAR))) {
            return false;
        }

        return true;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return "classic.puz";
    }
}
