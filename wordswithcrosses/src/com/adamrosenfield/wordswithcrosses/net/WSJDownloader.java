package com.adamrosenfield.wordswithcrosses.net;

import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Wall Street Journal
 * URL: http://mazerlm.home.comcast.net/~mazerlm/wsjYYMMDD.puz
 * Date = Fridays
 */
public class WSJDownloader extends AbstractDownloader {
    private static final String NAME = "Wall Street Journal";
    NumberFormat nf = NumberFormat.getInstance();

    public WSJDownloader() {
        super("http://mazerlm.home.comcast.net/~mazerlm/", NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_FRIDAY;
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("wsj" +
                nf.format(date.get(Calendar.YEAR) % 100) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
