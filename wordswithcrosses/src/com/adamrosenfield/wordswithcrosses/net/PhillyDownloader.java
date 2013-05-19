package com.adamrosenfield.wordswithcrosses.net;

import java.util.Calendar;

// FIXME: This downloader doesn't work

/**
 * Philadelphia Inquirer
 * URL: http://mazerlm.home.comcast.net/~mazerlm/piYYMMDD.puz
 * Date: Sunday
 */
public class PhillyDownloader extends AbstractDownloader {
    private static final String NAME = "Phil Inquirer";

    public PhillyDownloader() {
        super("http://mazerlm.home.comcast.net/~mazerlm/", NAME);
    }

    public boolean isPuzzleAvailable(Calendar date) {
        return (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return ("pi" +
                DEFAULT_NF.format(date.get(Calendar.YEAR) % 100) +
                DEFAULT_NF.format(date.get(Calendar.MONTH) + 1) +
                DEFAULT_NF.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
