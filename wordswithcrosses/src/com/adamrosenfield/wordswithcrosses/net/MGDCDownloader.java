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

/**
 * Matt Gaffney's Daily Crossword
 * URL: http://mattgaffneydaily.blogspot.com/YYYY/MM/mgdc-####-weekday-month-dayth-year.html
 * Date: Monday-Friday
 */
public class MGDCDownloader extends AbstractDownloader
{
    private static final Calendar START_DATE;

    private static final String[] WEEKDAY_NAMES =
    {
        "monday",
        "tuesday",
        "wednesday",
        "thursday",
        "friday",
        "saturday",  // Unused
        "sunday"     // Unused
    };

    private static final String[] MONTH_NAMES =
    {
        "january",
        "february",
        "march",
        "april",
        "may",
        "june",
        "july",
        "august",
        "september",
        "october",
        "november",
        "december"
    };

    private static final String PUZZLE_REGEX = "\\bid=([A-Za-z0-9_]*\\.puz)\\b";
    private static final Pattern PUZZLE_PATTERN = Pattern.compile(PUZZLE_REGEX);

    static
    {
        Calendar startDate = Calendar.getInstance();
        startDate.clear();
        startDate.set(2011, 8, 21);  // 2011-09-21 (months start at 0 for Calendar!)
        START_DATE = startDate;
    }

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

        int day = date.get(Calendar.DAY_OF_WEEK);
        return (day >= Calendar.MONDAY && day <= Calendar.FRIDAY);
    }

    @Override
    public boolean download(Calendar date) throws IOException
    {
        long millisSinceStart = (date.getTimeInMillis() - START_DATE.getTimeInMillis());
        int daysSinceStart = (int)(millisSinceStart / 1000 / 86400);
        if (daysSinceStart < 0)
        {
            return false;
        }

        int puzzleNum = ((daysSinceStart + 2) / 7) * 5 + ((daysSinceStart + 2) % 7) - 1;
        String scrapeUrl =
            "http://mattgaffneydaily.blogspot.com/" +
            date.get(Calendar.YEAR) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "/mgdc-" +
            String.format("%04d", puzzleNum) +
            "-" +
            WEEKDAY_NAMES[(date.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7] +
            "-" +
            MONTH_NAMES[date.get(Calendar.MONTH)] +
            "-" +
            date.get(Calendar.DATE) +
            getDaySuffix(date.get(Calendar.DATE)) +
            "-" +
            date.get(Calendar.YEAR) +
            ".html";

        String scrapedPage = downloadUrlToString(scrapeUrl);

        Matcher matcher = PUZZLE_PATTERN.matcher(scrapedPage);
        if (!matcher.find())
        {
            LOG.warning("Failed to find puzzle ID in page: " + scrapeUrl);
            return false;
        }

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", USER_AGENT);

        String id = matcher.group(1);
        String url = "http://icrossword.com/publish/server/puzzle/index.php?id=" + id;

        return super.download(date, url, headers);
    }

	@Override
    protected String createUrlSuffix(Calendar date)
	{
	    return "";
	}

	private static String getDaySuffix(int dayOfMonth)
	{
	    if (dayOfMonth >= 10 && dayOfMonth < 20)
	    {
	        return "th";
	    }

	    switch (dayOfMonth % 10)
	    {
	    case 1: return "st";
	    case 2: return "nd";
	    case 3: return "rd";
	    default: return "th";
	    }
	}
}
