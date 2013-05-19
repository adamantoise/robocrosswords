package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

/**
 * Jonesin' Crosswords Downloader
 * URL: http://herbach.dnsalias.com/Jonesin/jzYYMMDD.puz
 * Date: Thursday
 */
public class JonesinDownloader extends AbstractDownloader {
    private static final String NAME = "Jonesin' Crosswords";

    public JonesinDownloader() {
        super("http://herbach.dnsalias.com/Jonesin/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("jz" +
                (date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
