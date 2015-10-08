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
 * Ink Well Crosswords
 * URL: http://herbach.dnsalias.com/Tausig/vvYYMMDD.puz
 * Date: Friday
 */
public class InkwellDownloader extends AbstractDownloader {
    private static final String NAME = "InkWellXWords.com";

    // Ink Well Crosswords ceased publishing at the end of June 2014
    private static final Calendar END_DATE = CalendarUtil.createDate(2014, 6, 30);

    public InkwellDownloader() {
        super("http://herbach.dnsalias.com/Tausig/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return
            (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY &&
             date.compareTo(END_DATE) <= 0);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("vv" +
                (date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
