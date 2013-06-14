package com.adamrosenfield.wordswithcrosses.net;

import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public NevilleFogartyDownloader()
    {
        super("", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date)
    {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }

    @Override
    public boolean download(Calendar date) throws IOException
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
            return false;
        }

        // Then, scrape the blog post for the puzzle download link
        scrapeUrl = resolveUrl(scrapeUrl, matcher.group(1));
        scrapedData = downloadUrlToString(scrapeUrl);

        matcher = PUZZLE_PATTERN.matcher(scrapedData);
        if (!matcher.find())
        {
            LOG.warning("Failed to find puzzle link on page: " + scrapeUrl);
            return false;
        }

        // Finally, download the puzzle
        String puzzleUrl = resolveUrl(scrapeUrl, matcher.group(1));
        return super.download(date, puzzleUrl);
    }
}
