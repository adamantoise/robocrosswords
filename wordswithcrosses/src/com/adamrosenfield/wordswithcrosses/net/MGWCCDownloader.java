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
 * Matt Gaffney's Weekly Crossword Contest
 * URL: http://xwordcontest.com/YYYY/MM/page/[page]
 * Date: Friday
 */
public class MGWCCDownloader extends AbstractDownloader
{
    private static final String PUZZLE_REGEX = "\\bid=([A-Za-z0-9_]*\\.puz)\\b";
    private static final Pattern PUZZLE_PATTERN = Pattern.compile(PUZZLE_REGEX);

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

    public MGWCCDownloader()
    {
        super("", "Matt Gaffney's Weekly Crossword Contest");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    public boolean download(Calendar date) throws IOException
    {
        // Figure out which archive page to scrape
        Calendar now = Calendar.getInstance();
        Calendar refDate = now;
        if (date.get(Calendar.MONTH) != now.get(Calendar.MONTH))
        {
            refDate = (Calendar)date.clone();
            refDate.set(Calendar.DATE, refDate.getMaximum(Calendar.DATE));
        }

        // The page number is the number of Fridays between the given date and
        // the reference date (now or the end of the given month), inclusive
        int fridayCount = 0;
        while (refDate.compareTo(date) >= 0)
        {
            int dayOfWeek = refDate.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.FRIDAY)
            {
                fridayCount++;
                refDate.add(Calendar.DATE, -7);
            }
            else
            {
                refDate.add(Calendar.DATE, -((dayOfWeek - Calendar.FRIDAY + 7) % 7));
            }
        }

        // Make sure we scrape the correct page; if we scrape at the wrong time
        // (e.g. on a Friday morning), the blog post might not be up, and that
        // could screw up our page number calculation.
        String dateRegex =
            "<a href=\"http://xwordcontest.com/" +
            date.get(Calendar.YEAR) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "/mgwcc-[0-9]+-(?:friday-)?" +
            MONTH_NAMES[date.get(Calendar.MONTH)] +
            "-([0-9]+)[a-z][a-z]-" +
            date.get(Calendar.YEAR) +
            "-";
        Pattern datePattern = Pattern.compile(dateRegex);

        String baseUrl =
            "http://xwordcontest.com/" +
            date.get(Calendar.YEAR) +
            "/" +
            DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
            "/";

        // Try to scrape what we think the right page is.  If we're wrong,
        // figure out the correct page and then re-scrape that.
        int pageNum = fridayCount;
        for (int i = 0; i < 2; i++)
        {
            String scrapeUrl = baseUrl;
            if (pageNum != 1)
            {
                scrapeUrl +=
                    "page/" +
                    pageNum +
                    "/";
            }

            String scrapedPage = downloadUrlToString(scrapeUrl);

            // Check the date on the page we scraped
            Matcher matcher = datePattern.matcher(scrapedPage);
            if (!matcher.find())
            {
                LOG.warning("Failed to scrape date URL in page: " + scrapeUrl);
                return false;
            }

            // Get the day from the regex match
            String dayString = matcher.group(1);
            int day;
            try
            {
                day = Integer.parseInt(dayString);
            }
            catch (NumberFormatException e)
            {
                // This should never happen, since the regex group only matches
                // digits
                LOG.warning("Error parsing integer: " + dayString);
                return false;
            }

            if (day == date.get(Calendar.DATE))
            {
                // If we got the right date, scrape the puzzle ID and download it
                matcher = PUZZLE_PATTERN.matcher(scrapedPage);
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
            else
            {
                // Something went wrong, figure out the real correct page and
                // try again
                pageNum += (day - date.get(Calendar.DATE)) / 7;
                if (pageNum < 1)
                {
                    // If we got here, it probably means it's Friday morning and
                    // the puzzle hasn't been posted yet
                    LOG.warning("Failed to scrape MGWCC; is the puzzle out yet?");
                    return false;
                }
                else
                {
                    LOG.info("Failed to scrape MGWCC, got puzzle for day " + dayString + " instead; trying again with page " + pageNum);
                }
            }
        }

        return false;
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }
}
