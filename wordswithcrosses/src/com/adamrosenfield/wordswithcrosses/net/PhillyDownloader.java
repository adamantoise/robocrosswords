package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Philadelphia Inquirer
 * URL: http://mazerlm.home.comcast.net/~mazerlm/piYYMMDD.puz
 * Date = Sundays
 */
public class PhillyDownloader extends AbstractDownloader {
    private static final String NAME = "Phil Inquirer";
    NumberFormat nf = NumberFormat.getInstance();

    public PhillyDownloader() {
        super("http://mazerlm.home.comcast.net/~mazerlm/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_SUNDAY;
    }

    public File download(Calendar date) {
        return super.download(date, this.createUrlSuffix(date));
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
