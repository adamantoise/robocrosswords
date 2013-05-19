package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Wall Street Journal
 * URL: http://mazerlm.home.comcast.net/~mazerlm/wsjYYMMDD.puz
 * Date: Friday
 */
public class WSJDownloader extends AbstractDownloader {
    private static final String NAME = "Wall Street Journal";

    public WSJDownloader() {
        super("http://mazerlm.home.comcast.net/~mazerlm/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("wsj" +
                DEFAULT_NF.format(date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
