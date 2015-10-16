/**
 * This file is part of Words With Crosses.
 *
 * Copyright (C) 2015 Adam Rosenfield
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
 * BuzzFeed
 * URL: http://game-assets.buzzfeed.com/crossword/puz/MM-DD-YY.puz
 * Date: Monday-Friday
 */
public class BuzzFeedDownloader extends AbstractDownloader {

    private static final String NAME = "BuzzFeed";

    private static final Calendar START_DATE = CalendarUtil.createDate(2015, 10, 12);


    public BuzzFeedDownloader() {
        super("http://game-assets.buzzfeed.com/crossword/puz/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY &&
                date.compareTo(START_DATE) >= 0);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                "-" +
                (date.get(Calendar.YEAR) % 100) +
                ".puz");
    }
}
