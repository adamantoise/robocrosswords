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
 * New York Times monthly bonus puzzle
 * URL: http://www.nytimes.com/svc/crosswords/v2/puzzle/bonus-YYYY-MM.puz
 * Date: 1st of the month
 */
public class NYTBonusDownloader extends NYTBaseDownloader
{
    private static final String NAME = "New York Times Bonus Puzzle";

    public NYTBonusDownloader(String username, String password)
    {
        super(NAME, username, password);
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DATE) == 1);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return ("bonus-" +
                date.get(Calendar.YEAR) +
                "-" +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                ".puz");
    }
}
