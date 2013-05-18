package com.adamrosenfield.wordswithcrosses.net;

import java.text.NumberFormat;
import java.util.Calendar;

// FIXME: This downloader doesn't work

/**
 * Philadelphia Inquirer
 * URL: http://mazerlm.home.comcast.net/~mazerlm/piYYMMDD.puz
 * Date: Sunday
 */
public class PhillyDownloader extends AbstractDownloader {
    private static final String NAME = "Phil Inquirer";
    NumberFormat nf = NumberFormat.getInstance();

    public PhillyDownloader() {
        super("http://mazerlm.home.comcast.net/~mazerlm/", NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("pi" +
                nf.format(date.get(Calendar.YEAR) % 100) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
