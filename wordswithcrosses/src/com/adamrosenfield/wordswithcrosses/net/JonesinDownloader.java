package com.adamrosenfield.wordswithcrosses.net;

import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Jonesin' Crosswords Downloader
 * URL: http://herbach.dnsalias.com/Jonesin/jzYYMMDD.puz
 * Date = Thursdays
 */
public class JonesinDownloader extends AbstractDownloader {
    private static final String NAME = "Jonesin' Crosswords";
    NumberFormat nf = NumberFormat.getInstance();

    public JonesinDownloader() {
        super("http://herbach.dnsalias.com/Jonesin/", NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_THURSDAY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("jz" +
                (date.get(Calendar.YEAR) % 100) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
