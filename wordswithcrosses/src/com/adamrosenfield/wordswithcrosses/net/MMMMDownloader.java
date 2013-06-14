package com.adamrosenfield.wordswithcrosses.net;

import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Muller's Monthly Music Meta
 * URL: http://pmxwords.com/
 * Date: First Tuesday of each month
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
        // Puzzles are available on the first Tuesday of each month
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY &&
                date.get(Calendar.DATE) <= 7);
    }

    @Override
    protected String createUrlSuffix(Calendar date)
    {
        return "";
    }

    @Override
    public boolean download(Calendar date) throws IOException
    {
        String scrapeUrl = BASE_URL + date.get(Calendar.YEAR) + "puzzles/";
        String scrapedData = downloadUrlToString(scrapeUrl);

        // Look for this month's puzzle in the directory listing.
        // TODO: Use the JPZ instead of the PUZ
        String month = SHORT_MONTHS[date.get(Calendar.MONTH)];
        Pattern puzzlePattern = Pattern.compile("<a href=\"([^\"]*\\.puz)\">[^<]*</a>\\s*\\d\\d-" + month);
        Matcher matcher = puzzlePattern.matcher(scrapedData);

        if (matcher.find())
        {
            // If we found a match, convert the relative URL into an absolute
            // one and download it
            String puzzleUrl = matcher.group(1);
            String fullUrl = resolveUrl(scrapeUrl, puzzleUrl);
            return super.download(date, fullUrl);
        }
        else
        {
            LOG.warning("Failed to find puzzle link for " + date + " on page: " + scrapeUrl);
            return false;
        }
    }
}
