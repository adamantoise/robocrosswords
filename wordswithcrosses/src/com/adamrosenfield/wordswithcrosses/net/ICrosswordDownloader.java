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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ICrosswordDownloader extends AbstractDownloader
{
    private static final String PUZZLE_URL_REGEX = "<a href=\"([^\"]*\\.puz[^\"]*)\"";
    private static final Pattern PUZZLE_URL_PATTERN = Pattern.compile(PUZZLE_URL_REGEX);

    protected ICrosswordDownloader(String baseUrl, String downloaderName)
    {
        super(baseUrl, downloaderName);
    }

    protected void download(Calendar date, String id) throws IOException
    {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", USER_AGENT);

        String scrapeUrl = "http://icrossword.com/share/?id=" + id;
        String scrapedPage = downloadUrlToString(scrapeUrl, headers);

        Matcher matcher = PUZZLE_URL_PATTERN.matcher(scrapedPage);
        if (!matcher.find())
        {
            LOG.warning("Failed to find puzzle download URL in page: " + scrapeUrl);
            throw new IOException("Failed to find puzzle download URL in page");
        }

        String url = matcher.group(1);
        headers.put("Referer", scrapeUrl);

        super.download(date, url, headers);
    }
}
