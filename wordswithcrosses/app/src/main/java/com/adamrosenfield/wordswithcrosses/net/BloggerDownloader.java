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

public abstract class BloggerDownloader extends AbstractDownloader
{
    private static final String PUZZLE_REGEX = "href=(?:\"|&quot;)(https?://(?:[^\"&]|&(?!quot;))*\\.puz(?:[^\"&]|&(?!quot;))*)(?:\"|&quot;)";
    private static final Pattern PUZZLE_PATTERN = Pattern.compile(PUZZLE_REGEX);

    private final String baseUrl;

    public BloggerDownloader(String downloaderName, String baseUrl)
    {
        super("", downloaderName);
        this.baseUrl = baseUrl;
    }

    @Override
    public void download(Calendar date) throws IOException
    {
        Calendar endDate = (Calendar)date.clone();
        endDate.add(Calendar.DATE, 2);

        String scrapeUrl =
            baseUrl +
            "/feeds/posts/default?alt=atom&max-results=1&published-min=" +
            iso8601String(date) +
            "&published-max=" +
            iso8601String(endDate);
        String scrapedPage = downloadUrlToString(scrapeUrl);

        Matcher matcher = PUZZLE_PATTERN.matcher(scrapedPage);
        if (!matcher.find())
        {
            LOG.warning("Failed to find puzzle URL in page: " + scrapeUrl);
            throw new IOException("Failed to scrape puzzle URL");
        }

        String url = matcher.group(1).replaceAll("&amp;amp;", "&");
        super.download(date, url);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }

    private static String iso8601String(Calendar date)
    {
        return
            date.get(Calendar.YEAR) +
            "-" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "-" +
            DEFAULT_NF.format(date.get(Calendar.DATE)) +
            "T00:00:00";
    }
}
