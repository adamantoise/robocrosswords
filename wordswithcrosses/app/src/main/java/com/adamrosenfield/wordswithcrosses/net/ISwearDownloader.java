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
 * Daily Record "I Swear" crossword
 * URL: http://wij.theworld.com/puzzles/dailyrecord/DR110401.puz
 * Date: Friday
 *
 * @author robert.cooper
 */
public class ISwearDownloader extends AbstractDownloader {
    private static final String NAME = "I Swear";

    public ISwearDownloader(){
        super("http://wij.theworld.com/puzzles/dailyrecord/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return
            date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY &&
            date.get(Calendar.YEAR) <= 2013;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("DR" +
                (date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
