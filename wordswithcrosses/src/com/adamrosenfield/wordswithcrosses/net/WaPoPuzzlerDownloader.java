package com.adamrosenfield.wordswithcrosses.net;

import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Washington Post Puzzler
 * URL: http://cdn.games.arkadiumhosted.com/washingtonpost/puzzler/puzzle_130505.xml
 * Date = Sundays
 */
public class WaPoPuzzlerDownloader extends AbstractJPZDownloader {
    private static final String NAME = "Washington Post Puzzler";
    NumberFormat nf = NumberFormat.getInstance();

    public WaPoPuzzlerDownloader() {
        super("http://cdn.games.arkadiumhosted.com/washingtonpost/puzzler/", NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_SUNDAY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("puzzle_" +
                nf.format(date.get(Calendar.YEAR) % 100) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".xml");
    }
}
