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
 * Muller's Monthly Music Meta
 * URL: http://pmxwords.com/
 * Date: First Tuesday of each month at 12:00 U.S. Eastern time
 */
public class MMMMDownloader extends AbstractDownloader
{
    private static final String NAME = "Muller Monthly Music Meta";
    private static final String BASE_URL = "http://pmxwords.com/wp-content/";

    public MMMMDownloader()
    {
        super("", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        // Puzzles are available on the first Tuesday of each month, starting
        // from April 2012, as well as on New Years Eve.
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int dayOfMonth = date.get(Calendar.DATE);
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        return
            (year == 2012 && month >= Calendar.APRIL ||
             year > 2012 && month >= Calendar.FEBRUARY) &&
            ((dayOfWeek == Calendar.TUESDAY && dayOfMonth <= 7) ||
             (month == Calendar.DECEMBER && dayOfMonth == 31));
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }

    @Override
    public void download(Calendar date) throws IOException
    {
        Calendar now = Calendar.getInstance();
        Calendar releaseTime = getReleaseTime(date);
        if (now.before(releaseTime))
        {
            throw new IOException("Puzzle has not yet been released!");
        }

        String scrapeUrl = BASE_URL;
        if (date.get(Calendar.YEAR) != 2012)
        {
            scrapeUrl += date.get(Calendar.YEAR);
        }
        scrapeUrl += "puzzles/";

        String scrapedData = downloadUrlToString(scrapeUrl);

        // Look for this month's puzzle in the directory listing.  Also look
        // at the last 10 days from the previous month, since sometimes that's
        // when the timestamps are.
        // TODO: Use the JPZ instead of the PUZ
        Pattern puzzlePattern;
        if (date.get(Calendar.MONTH) == Calendar.DECEMBER && date.get(Calendar.DATE) == 31)
        {
            puzzlePattern = Pattern.compile("<a href=\"([^\"]*\\.puz)\">[^<]*</a>\\s*[23]\\d-" + SHORT_MONTHS[Calendar.DECEMBER] + "-" + date.get(Calendar.YEAR));
        }
        else
        {
            String month = SHORT_MONTHS[date.get(Calendar.MONTH)];
            String prevMonth = SHORT_MONTHS[(date.get(Calendar.MONTH) + 11) % 12];
            puzzlePattern = Pattern.compile("<a href=\"([^\"]*\\.puz)\">[^<]*</a>\\s*([01]\\d-" + month + "|[23]\\d-" + prevMonth + ")-" + date.get(Calendar.YEAR));
        }

        Matcher matcher = puzzlePattern.matcher(scrapedData);

        if (matcher.find())
        {
            // If we found a match, convert the relative URL into an absolute
            // one and download it
            String puzzleUrl = matcher.group(1);
            String fullUrl = resolveUrl(scrapeUrl, puzzleUrl);
            super.download(date, fullUrl);
        }
        else
        {
            LOG.warning("Failed to find puzzle link for " + date + " on page: " + scrapeUrl);
            throw new IOException("Failed to find puzzle link");
        }
    }

    /**
     * Gets the exact time (12:00 EST or EDT) when the puzzle for the given
     * date is released.
     */
    private static Calendar getReleaseTime(Calendar date)
    {
        return CalendarUtil.createDate(CalendarUtil.TZ_US_EASTERN, date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DATE), 12, 0, 0);
    }
}
