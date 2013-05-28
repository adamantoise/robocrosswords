package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * CrosSynergy/Washington Post downloader
 * URL: http://cdn.games.arkadiumhosted.com/washingtonpost/crossynergy/csYYMMDD.jpz
 * Date: Daily
 */
public class WaPoDownloader extends AbstractJPZDownloader {
    private static final String NAME = "Washington Post";

    public WaPoDownloader() {
        super("http://cdn.games.arkadiumhosted.com/washingtonpost/crossynergy/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        // Only the most recent 2 weeks of puzzles are available
        Calendar now = Calendar.getInstance();
        long millisOld = now.getTimeInMillis() - date.getTimeInMillis();
        int daysOld = (int)(millisOld / ((long)86400 * 1000));
        return (daysOld <= 14);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("cs" +
                (date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".jpz");
    }
}
