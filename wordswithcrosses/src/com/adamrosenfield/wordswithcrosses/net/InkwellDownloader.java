package com.adamrosenfield.wordswithcrosses.net;

import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Ink Well Crosswords
 * URL: http://herbach.dnsalias.com/Tausig/vvYYMMDD.puz
 * Date = Fridays
 */
public class InkwellDownloader extends AbstractDownloader {
    private static final String NAME = "InkWellXWords.com";
    NumberFormat nf = NumberFormat.getInstance();

    public InkwellDownloader() {
        super("http://herbach.dnsalias.com/Tausig/", NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_FRIDAY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("vv" +
                (date.get(Calendar.YEAR) % 100) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
