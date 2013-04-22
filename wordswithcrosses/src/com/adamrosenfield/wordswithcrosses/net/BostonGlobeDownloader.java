package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Boston Globe
 * URL: http://standalone.com/dl/puz/boston/boston_MMDDYY.puz
 * Date = Sundays
 */
public class BostonGlobeDownloader extends AbstractDownloader {
    private static final String NAME = "Boston Globe";
    NumberFormat nf = NumberFormat.getInstance();

    public BostonGlobeDownloader() {
        super("http://standalone.com/dl/puz/boston/", DOWNLOAD_DIR, NAME);
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
        return ("boston_" +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                date.get(Calendar.YEAR) +
                ".puz");
    }
}
