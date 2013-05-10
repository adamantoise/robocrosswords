package com.adamrosenfield.wordswithcrosses.net;

import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Thinks.com
 * URL: http://thinks.com/daily-crossword/puzzles/YYYY-MM/dc1-YYYY-MM-DD.puz
 * Date = Fridays
 */
public class ThinksDownloader extends AbstractDownloader {
    private static final String NAME = "Thinks.com";
    NumberFormat nf = NumberFormat.getInstance();

    public ThinksDownloader() {
        super("http://thinks.com/daily-crossword/puzzles/", NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_DAILY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) +
                "-" +
                nf.format(date.get(Calendar.MONTH) + 1) +
                "/dc1-" +
                date.get(Calendar.YEAR) +
                "-" +
                nf.format(date.get(Calendar.MONTH) + 1) +
                "-" +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
