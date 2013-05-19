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
        return true;
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
