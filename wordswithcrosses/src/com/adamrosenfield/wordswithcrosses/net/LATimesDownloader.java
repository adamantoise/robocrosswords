package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Los Angeles Times
 * URL: http://cdn.games.arkadiumhosted.com/latimes/assets/DailyCrossword/puzzle_YYMMDD.xml
 * Date: Daily
 */
public class LATimesDownloader extends AbstractJPZDownloader {

    private static final String NAME = "Los Angeles Times";

    public LATimesDownloader() {
        super("http://cdn.games.arkadiumhosted.com/latimes/assets/DailyCrossword/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return true;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("puzzle_" +
                (date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".xml");
    }
}
