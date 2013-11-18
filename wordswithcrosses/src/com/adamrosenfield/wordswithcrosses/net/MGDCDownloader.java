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

/**
 * Matt Gaffney's Daily Crossword
 * URL: http://mattgaffneydaily.blogspot.com/YYYY/MM/mgdc-####-weekday-month-dayth-year.html
 * Date: Monday-Friday
 */
public class MGDCDownloader extends ICrosswordDownloader
{
    private static final String BASE_URL = "http://mattgaffneydaily.blogspot.com/";
    /** Date on which MGDC started (5 days/week) */
    private static final Calendar START_DATE = createDate(2011, 9, 21);
    /** Date on which MGDC became 7 days/week */
    private static final Calendar DAILY_START_DATE = createDate(2013, 9, 21);

    private static final String EMBED_REGEX = "src=\"([^\"]*\\.puz)\"";
    private static final Pattern EMBED_PATTERN = Pattern.compile(EMBED_REGEX);

    public MGDCDownloader()
    {
        super("", "Matt Gaffney's Daily Crossword");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        if (date.compareTo(START_DATE) <= 0)
        {
            return false;
        }
        else if (date.compareTo(DAILY_START_DATE) >= 0)
        {
            return true;
        }
        else
        {
            int day = date.get(Calendar.DAY_OF_WEEK);
            return (day >= Calendar.MONDAY && day <= Calendar.FRIDAY);
        }
    }

    @Override
    public void download(Calendar date) throws IOException
    {
        String dateStr =
            date.get(Calendar.YEAR) +
            "-" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "-" +
            date.get(Calendar.DATE);

        String scrapeUrl =
            BASE_URL +
            "search?updated-min=" +
            dateStr +
            "T00:00:00-05:00&updated-max=" +
            dateStr +
            "T23:59:59-05:00&max-results=1";

        String scrapedPage = downloadUrlToString(scrapeUrl);

        Matcher matcher = EMBED_PATTERN.matcher(scrapedPage);
        if (!matcher.find())
        {
            LOG.warning("Failed to find puzzle iframe in page: " + scrapeUrl);
            throw new IOException("Failed to find puzzle iframe in page");
        }

        String embedUrl = matcher.group(1);
        download(date, embedUrl, BASE_URL);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }
}
