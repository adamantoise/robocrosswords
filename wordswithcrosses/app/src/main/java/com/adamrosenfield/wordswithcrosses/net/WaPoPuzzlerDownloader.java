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

import com.adamrosenfield.wordswithcrosses.CalendarUtil;

/**
 * Washington Post Puzzler
 * URL: http://cdn.games.arkadiumhosted.com/washingtonpost/puzzler/puzzle_130505.xml
 * Date: Sunday
 */
public class WaPoPuzzlerDownloader extends AbstractJPZDownloader {
    private static final String NAME = "Washington Post Puzzler";

    // Washington Post Puzzler ceased publishing after March 2015
    private static final Calendar END_DATE = CalendarUtil.createDate(2015, 3, 31);
    public WaPoPuzzlerDownloader() {
        super("http://cdn.games.arkadiumhosted.com/washingtonpost/puzzler/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return
            (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY &&
             date.compareTo(END_DATE) <= 0);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("puzzle_" +
                DEFAULT_NF.format(date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".xml");
    }
}
