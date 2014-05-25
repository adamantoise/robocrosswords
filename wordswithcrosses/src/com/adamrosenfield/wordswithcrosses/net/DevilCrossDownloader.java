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

import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.adamrosenfield.wordswithcrosses.CalendarUtil;

/**
 * Devil Cross
 * URL: http://devilcross.com/
 * Date: Every other Saturday
 */
public class DevilCrossDownloader extends AbstractDownloader
{
    private static final String BASE_URL = "http://devilcross.com/";

    /** Date on which Devil Cross was first published */
    private static final Calendar START_DATE = CalendarUtil.createDate(2014, 2, 1);

    private static final String PUZZLE_REGEX = "href=\"([^\"]*/wp-content/crosswords/[^.]*\\.php)\">";
    private static final Pattern PUZZLE_PATTERN = Pattern.compile(PUZZLE_REGEX);

    public DevilCrossDownloader()
    {
        super("", "Devil Cross");
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
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }

    @Override
    public void download(Calendar date) throws IOException
    {
        // First scrape the archive page to find the .puz link
        String scrapeUrl = BASE_URL +
            date.get(Calendar.YEAR) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.DATE)) +
            "/";
        String scrapedData = downloadUrlToString(scrapeUrl);

        Matcher matcher = PUZZLE_PATTERN.matcher(scrapedData);
        if (!matcher.find())
        {
            LOG.warning("Failed to find puzzle link on page: " + scrapeUrl);
            throw new IOException("Failed to scrape puzzle link");
        }

        // Now download the puzzle
        String puzzleUrl = resolveUrl(scrapeUrl, matcher.group(1));
        super.download(date, puzzleUrl);
    }
}
