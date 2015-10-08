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

import com.adamrosenfield.wordswithcrosses.io.JPZIO;
import com.adamrosenfield.wordswithcrosses.puz.Puzzle;

/**
 * Merl Reagle's Crossword
 * URL: http://www.sundaycrosswords.com/
 * Date: Sunday
 */
public class MerlReagleDownloader extends AbstractJPZDownloader
{
    private static final String AUTHOR = "Merl Reagle";

    // Yes, there are two spaces there between the first two words
    private static final String DATE_REGEX = "For  puzzle of (\\d+)/(\\d+)/(\\d+), click";
    private static final Pattern DATE_PATTERN = Pattern.compile(DATE_REGEX);

    private static final String PUZZLE_REGEX = "<PARAM NAME=\"DATAFILE\" VALUE=\"([^\"]*)\">";
    private static final Pattern PUZZLE_PATTERN = Pattern.compile(PUZZLE_REGEX);

    private static final String TITLE_REGEX = ">&quot;(.*)&quot;<";
    private static final Pattern TITLE_PATTERN = Pattern.compile(TITLE_REGEX);

    private static final long MILLIS_PER_WEEK = (long)7*86400*1000;

    public MerlReagleDownloader()
    {
        super("", "Merl Reagle's Crossword");
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        // Only the most recent 4 puzzles are available
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY &&
                date.getTimeInMillis() > System.currentTimeMillis() - 4*MILLIS_PER_WEEK);
    }

    @Override
    public void download(Calendar date) throws IOException
    {
        // Figure out how many weeks old the given date is, rounded down
        Calendar now = Calendar.getInstance();
        long deltaMillis = now.getTimeInMillis() - date.getTimeInMillis();
        int weeksOld = (int)(deltaMillis / MILLIS_PER_WEEK);

        if (weeksOld >= 4 || weeksOld < 0)
        {
            throw new IOException("Only the most recent 4 weeks of puzzles are available");
        }

        // If the puzzle we want is 3 weeks old, check the 2-week-old puzzle
        // first, since AFAICT there's no way to get the date of the puzzle on
        // the 3-week-old page
        if (weeksOld == 3)
        {
            weeksOld--;
        }

        Calendar prevPuzzleDate = (Calendar)date.clone();
        prevPuzzleDate.add(Calendar.DATE, -7);

        String baseUrl = "http://www.sundaycrosswords.com/ccpuz/";

        // Try to scrape what we think the right page is.  If we're wrong,
        // figure out the correct page and then re-scrape that.
        for (int i = 0; i < 2; i++)
        {
            String scrapeUrl = baseUrl;
            if (weeksOld == 0)
            {
                scrapeUrl += "MPuz.php";
            }
            else
            {
                scrapeUrl += "MPuz" + weeksOld + "WO.php";
            }

            String scrapedPage = downloadUrlToString(scrapeUrl);

            // Check the date of the next puzzle on the page we scraped; if
            // we're scraping the 3-week-old page, assume we're right
            if (weeksOld < 3)
            {
                Matcher matcher = DATE_PATTERN.matcher(scrapedPage);
                if (!matcher.find())
                {
                    LOG.warning("Failed to scrape date in page: " + scrapeUrl);
                    throw new IOException("Failed to scrape date in page");
                }

                // Get the day from the regex match
                String monthString = matcher.group(1);
                String dayString = matcher.group(2);
                int month, day;
                try
                {
                    // Ignore year, since sometimes the year on the web page is
                    // wrong (sigh...)
                    month = Integer.parseInt(monthString);
                    day = Integer.parseInt(dayString);
                }
                catch (NumberFormatException e)
                {
                    // This should never happen, since the regex group only
                    // matches digits
                    LOG.warning("Error parsing integer: " + matcher.group(0));
                    throw new IOException("Failed to parse date");
                }

                int expectedMonth = prevPuzzleDate.get(Calendar.MONTH) + 1;
                int expectedDay = prevPuzzleDate.get(Calendar.DATE);
                if (month != expectedMonth ||
                    day != expectedDay)
                {
                    // Date was wrong, try again
                    int year = prevPuzzleDate.get(Calendar.YEAR);
                    if (month > expectedMonth + 1)
                    {
                        year--;
                    }

                    Calendar scrapedPrevPuzzleDate = Calendar.getInstance();
                    scrapedPrevPuzzleDate.set(year,  month - 1,  day,  0,  0,  0);
                    deltaMillis = scrapedPrevPuzzleDate.getTimeInMillis() - prevPuzzleDate.getTimeInMillis();
                    int deltaWeeks = (int)(deltaMillis / MILLIS_PER_WEEK);
                    weeksOld += deltaWeeks;
                    LOG.info(
                        "Failed to scrape Merl Reagle, got puzzle with previous date of " +
                        year + "-" + month + "-" + day + ", expected previous date of " +
                        prevPuzzleDate);
                    continue;
                }
            }

            // Now that we got the right date (we hope), scrape the puzzle URL
            // and download it
            Matcher matcher = PUZZLE_PATTERN.matcher(scrapedPage);
            if (!matcher.find())
            {
                LOG.warning("Failed to find puzzle filename in page: " + scrapeUrl);
                throw new IOException("Failed to find puzzle filename");
            }

            String puzzleFilename = matcher.group(1);

            matcher = TITLE_PATTERN.matcher(scrapedPage);
            String title = (matcher.find() ? matcher.group(1) : puzzleFilename);
            MerlReagleMetadata metadataSetter = new MerlReagleMetadata(title, date.get(Calendar.YEAR));

            String url = baseUrl + puzzleFilename;
            super.download(date, url, EMPTY_MAP, metadataSetter);
        }

        throw new IOException("Failed to download puzzle");
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }

    private static class MerlReagleMetadata implements JPZIO.PuzzleMetadataSetter
    {
        private String title;
        private int year;

        public MerlReagleMetadata(String title, int year)
        {
            this.title = title;
            this.year = year;
        }

        public void setMetadata(Puzzle puzzle)
        {
            puzzle.setAuthor(AUTHOR);
            puzzle.setTitle(title);
            puzzle.setCopyright("\u00A9 " + year + " " + AUTHOR);
        }
    }
}
