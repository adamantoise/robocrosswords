package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.util.Calendar;

/**
 * New York Times Classic
 * URL: http://www.nytimes.com/specials/puzzles/classic.puz
 * Date = Mondays, but no archive is available.
 */
public class NYTClassicDownloader extends AbstractDownloader {
    private static final String NAME = "New York Times Classic";

    public NYTClassicDownloader() {
        super("http://www.nytimes.com/specials/puzzles/", DOWNLOAD_DIR, NAME);
    }

    public int[] getDownloadDates() {
        return DATE_MONDAY;
    }

    public File download(Calendar date) {
        Calendar now = Calendar.getInstance();

        // Only download if requested week is same as current week, because there is no archive.
        if ((now.get(Calendar.YEAR) != date.get(Calendar.YEAR)) ||
            (now.get(Calendar.WEEK_OF_YEAR) != date.get(Calendar.WEEK_OF_YEAR))) {
            return null;
        }

        return super.download(date, this.createUrlSuffix(date));
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return "classic.puz";
    }
}
