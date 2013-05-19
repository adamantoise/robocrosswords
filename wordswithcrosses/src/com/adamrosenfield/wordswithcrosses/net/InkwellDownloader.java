package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Ink Well Crosswords
 * URL: http://herbach.dnsalias.com/Tausig/vvYYMMDD.puz
 * Date: Friday
 */
public class InkwellDownloader extends AbstractDownloader {
    private static final String NAME = "InkWellXWords.com";

    public InkwellDownloader() {
        super("http://herbach.dnsalias.com/Tausig/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("vv" +
                (date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
