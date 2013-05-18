package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * New York Times Classic
 * URL: http://www.nytimes.com/specials/puzzles/classic.puz
 * Date: Monday, but no archive is available.
 */
public class NYTClassicDownloader extends AbstractDownloader {
    private static final String NAME = "New York Times Classic";

    public NYTClassicDownloader() {
        super("http://www.nytimes.com/specials/puzzles/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        if (date.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            return false;

        // Only download if requested week is same as current week, because there is no archive.
        Calendar now = Calendar.getInstance();

        if ((now.get(Calendar.YEAR) != date.get(Calendar.YEAR)) ||
            (now.get(Calendar.WEEK_OF_YEAR) != date.get(Calendar.WEEK_OF_YEAR))) {
            return false;
        }

        return true;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return "classic.puz";
    }
}
