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
 * Neville Fogarty
 * URL: http://nevillefogarty.wordpress.com/
 * Date: Fridays
 */
public class NevilleFogartyDownloader extends AbstractDownloader
{
    private static final String NAME = "Neville Fogarty";
    private static final String BASE_URL = "http://nevillefogarty.wordpress.com/";

    private static final String CONTINUE_READING_REGEX = "<a href=\"([^\"]*)\">Continue reading";
    private static final Pattern CONTINUE_READING_PATTERN = Pattern.compile(CONTINUE_READING_REGEX);

    private static final String PUZZLE_REGEX = "href=\"([^\"]*)\"><img [^>]*title=\"Download \\.puz file\"";
    private static final Pattern PUZZLE_PATTERN = Pattern.compile(PUZZLE_REGEX);

    /** Final publication date (at least for now...) */
    private static final Calendar END_DATE = CalendarUtil.createDate(2014, 9, 27);

    public NevilleFogartyDownloader()
    {
        super("", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) && (date.compareTo(END_DATE) <= 0);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }

    @Override
    public void download(Calendar date) throws IOException
    {
        // First, scrape the archives for the given date to get the
        // link to the full blog post for the given date
        String scrapeUrl =
            BASE_URL +
            date.get(Calendar.YEAR) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.DATE)) +
            "/";
        String scrapedData = downloadUrlToString(scrapeUrl);

        Matcher matcher = CONTINUE_READING_PATTERN.matcher(scrapedData);
        if (!matcher.find())
        {
            LOG.warning("Failed to find \"Continue reading\" link on page: " + scrapeUrl);
            throw new IOException("Failed to scrape archives page");
        }

        // Then, scrape the blog post for the puzzle download link
        scrapeUrl = resolveUrl(scrapeUrl, matcher.group(1));
        scrapedData = downloadUrlToString(scrapeUrl);

        matcher = PUZZLE_PATTERN.matcher(scrapedData);
        if (!matcher.find())
        {
            LOG.warning("Failed to find puzzle link on page: " + scrapeUrl);
            throw new IOException("Failed to scrape puzzle link");
        }

        // Finally, download the puzzle
        String puzzleUrl = resolveUrl(scrapeUrl, matcher.group(1));
        super.download(date, puzzleUrl);
    }
}
