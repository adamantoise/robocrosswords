package com.adamrosenfield.wordswithcrosses.net;

import java.io.File;
import java.text.NumberFormat;
import java.util.Calendar;

/**
 * Chronicle of Higher Education
 * URL: http://chronicle.com/items/biz/puzzles/YYYYMMDD.puz
 * Date = Fridays
 */
public class CHEDownloader extends AbstractDownloader {
    private static final String NAME = "Chronicle of Higher Education";
    NumberFormat nf = NumberFormat.getInstance();

    public CHEDownloader() {
        super("http://chronicle.com/items/biz/puzzles/", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
        return DATE_FRIDAY;
    }

    public String getName() {
        return NAME;
    }

    public File download(Calendar date) {
        return super.download(date, this.createUrlSuffix(date));
    }

    @Override
    protected String createUrlSuffix(Calendar date) {
        return (date.get(Calendar.YEAR) +
                nf.format(date.get(Calendar.MONTH) + 1) +
                nf.format(date.get(Calendar.DAY_OF_MONTH)) +
                ".puz");
    }
}
