package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Washington Post Puzzler
 * URL: http://cdn.games.arkadiumhosted.com/washingtonpost/puzzler/puzzle_130505.xml
 * Date: Sunday
 */
public class WaPoPuzzlerDownloader extends AbstractJPZDownloader {
    private static final String NAME = "Washington Post Puzzler";

    public WaPoPuzzlerDownloader() {
        super("http://cdn.games.arkadiumhosted.com/washingtonpost/puzzler/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("puzzle_" +
                DEFAULT_NF.format(date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".xml");
    }
}
